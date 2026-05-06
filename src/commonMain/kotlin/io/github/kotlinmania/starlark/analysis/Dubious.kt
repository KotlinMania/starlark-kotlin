// port-lint: source analysis/dubious.rs
package io.github.kotlinmania.starlark.analysis

import io.github.kotlinmania.starlark.syntax.ast.ExprP
import io.github.kotlinmania.starlark.syntax.ast.StmtP
import io.github.kotlinmania.starlark.values.types.int.StarlarkInt
import io.github.kotlinmania.starlark.values.types.num.NumRef
import io.github.kotlinmania.starlark.syntax.ast.AstLiteral
import io.github.kotlinmania.starlarksyntax.codemap.Spanned as Spanned
import io.github.kotlinmania.starlark.syntax.ast.AstNoPayload
import io.github.kotlinmania.starlarksyntax.codemap.FileSpan as FileSpan
import io.github.kotlinmania.starlarksyntax.codemap.CodeMap as CodeMap
import io.github.kotlinmania.starlarksyntax.codemap.Span as Span
import io.github.kotlinmania.starlark.syntax.AstModule

/*
 * Copyright 2019 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not import this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// --- Dubious lint types and functions ---

sealed class Dubious : LintWarning {
    /** Duplicate dictionary key `{key}`, also used at {span} */
    class DuplicateKey(val key: String, val span: FileSpan) : Dubious() {
        override fun toString(): String = "Duplicate dictionary key `$key`, also used at $span"
    }

    /** Variable `{name}` will either do nothing or fail if uninitialised */
    class IdentifierAsStatement(val name: String) : Dubious() {
        override fun toString(): String = "Variable `$name` will either do nothing or fail if uninitialised"
    }

    override fun severity(): EvalSeverity = EvalSeverity.Warning

    override fun shortName(): String = when (this) {
        is DuplicateKey -> "duplicate-key"
        is IdentifierAsStatement -> "ident-as-statement"
    }

    fun about(): String = when (this) {
        is DuplicateKey -> key
        is IdentifierAsStatement -> name
    }
}

/** Helper sealed class for duplicate dictionary key detection. */
private sealed class DubiousKey {
    class IntKey(val value: StarlarkInt) : DubiousKey() {
        override fun equals(other: Any?): Boolean = other is IntKey && value == other.value
        override fun hashCode(): kotlin.Int = value.hashCode()
    }

    class FloatKey(val bits: Long) : DubiousKey() {
        override fun equals(other: Any?): Boolean = other is FloatKey && bits == other.bits
        override fun hashCode(): kotlin.Int = bits.hashCode()
    }

    class StringKey(val value: String) : DubiousKey() {
        override fun equals(other: Any?): Boolean = other is StringKey && value == other.value
        override fun hashCode(): kotlin.Int = value.hashCode()
    }

    class IdentifierKey(val value: String) : DubiousKey() {
        override fun equals(other: Any?): Boolean = other is IdentifierKey && value == other.value
        override fun hashCode(): kotlin.Int = value.hashCode()
    }
}

private fun toKey(x: Spanned<ExprP<*>>): Pair<DubiousKey, Span>? {
    return when (val node = x.node) {
        is ExprP.Literal -> when (val lit = node.literal) {
            is AstLiteral.Int -> DubiousKey.IntKey(StarlarkInt.from(lit.value.node)) to lit.value.span
            is AstLiteral.Float -> {
                val n = NumRef.from(lit.value.node)
                val asInt = n.asInt()
                if (asInt != null) {
                    // make an integer float always collide with other ints
                    DubiousKey.IntKey(StarlarkInt.from(asInt)) to lit.value.span
                } else {
                    // First normalise -0.0
                    val v = if (lit.value.node == 0.0) 0.0 else lit.value.node
                    DubiousKey.FloatKey(v.toBits()) to lit.value.span
                }
            }
            is AstLiteral.String -> DubiousKey.StringKey(lit.value.node) to lit.value.span
            is AstLiteral.Ellipsis -> null
        }
        is ExprP.Identifier<*, *> -> DubiousKey.IdentifierKey(node.ident.node.ident) to node.ident.span
        else -> null
    }
}

// Go implementation of Starlark disallows duplicate top-level assignments,
// it's likely that will become Starlark standard sooner or later, so check now.
// The one place we allow it is to export something you grabbed with load.
internal fun duplicateDictionaryKey(module: AstModule, res: MutableList<LintT<Dubious>>) {
    fun expr(x: Spanned<ExprP<AstNoPayload>>, codemap: CodeMap, results: MutableList<LintT<Dubious>>) {
        when (val node = x.node) {
            is ExprP.Dict -> {
                val seen = HashMap<DubiousKey, Span>()
                for ((key, _) in node.elements) {
                    val keyPair = toKey(key)
                    if (keyPair != null) {
                        val (keyId, pos) = keyPair
                        val old = seen.put(keyId, pos)
                        if (old != null) {
                            results.add(
                                LintT.new(
                                    codemap,
                                    old,
                                    Dubious.DuplicateKey(key.toString(), codemap.fileSpan(pos))
                                )
                            )
                        }
                    }
                }
            }
            else -> {}
        }
        x.node.visitChildExprs { child -> expr(child, codemap, results) }
    }

    module
        .statement
        .visitExprs { x -> expr(x, module.codemap, res) }
}

internal fun identifierAsStatement(module: AstModule, res: MutableList<LintT<Dubious>>) {
    fun stmt(x: Spanned<StmtP<AstNoPayload>>, codemap: CodeMap, results: MutableList<LintT<Dubious>>) {
        when (val node = x.node) {
            is StmtP.Expression -> when (val exprNode = node.expr.node) {
                is ExprP.Identifier<AstNoPayload, *> -> results.add(
                    LintT.new(
                        codemap,
                        exprNode.ident.span,
                        Dubious.IdentifierAsStatement(exprNode.ident.node.ident)
                    )
                )
                else -> {}
            }
            else -> x.visitStmtChildren { child -> stmt(child, codemap, results) }
        }
    }

    stmt(module.statement, module.codemap, res)
}

internal fun lintDubious(module: AstModule): List<LintT<Dubious>> {
    val res = mutableListOf<LintT<Dubious>>()
    duplicateDictionaryKey(module, res)
    identifierAsStatement(module, res)
    return res
}
