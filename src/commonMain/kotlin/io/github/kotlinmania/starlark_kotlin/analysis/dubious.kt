// port-lint: source src/analysis/dubious.rs
package io.github.kotlinmania.starlark_kotlin.analysis.dubious

import io.github.kotlinmania.starlark_kotlin.syntax.ast.ExprP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.StmtP
import io.github.kotlinmania.starlark_kotlin.values.types.string.literal
import io.github.kotlinmania.starlark_kotlin.entries
import io.github.kotlinmania.starlark_kotlin.analysis.ident
import io.github.kotlinmania.starlark_kotlin.analysis.expr
import io.github.kotlinmania.starlark_kotlin.values.types.int.StarlarkInt
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.AstLiteral
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstStmt
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstExpr
import io.github.kotlinmania.starlark_kotlin.codemap.FileSpan
import io.github.kotlinmania.starlark_kotlin.codemap.CodeMap
import io.github.kotlinmania.starlark_kotlin.codemap
import io.github.kotlinmania.starlark_kotlin.analysis.visitStmt
import io.github.kotlinmania.starlark_kotlin.analysis.visitExpr
import io.github.kotlinmania.starlark_kotlin.analysis.statement
import io.github.kotlinmania.starlark_kotlin.analysis.node
import io.github.kotlinmania.starlark_kotlin.analysis.fileSpan
import io.github.kotlinmania.starlark_kotlin.analysis.description
import io.github.kotlinmania.starlark_kotlin.analysis.LintWarning
import io.github.kotlinmania.starlark_kotlin.analysis.LintT
import io.github.kotlinmania.starlark_kotlin.analysis.EvalSeverity
import io.github.kotlinmania.starlark_kotlin.values.types.ellipsis.Ellipsis
import io.github.kotlinmania.starlark_kotlin.codemap.Span
import io.github.kotlinmania.starlark_kotlin.syntax.AstModule

/*
 * Copyright 2019 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

// Placeholder types removed. Rely on imports.

class NumRef(val value: Double) {
    companion object {
        fun from(d: Double): NumRef = NumRef(d)
    }

    fun asInt(): Long? {
        val l = value.toLong()
        return if (l.toDouble() == value) l else null
    }
}



// --- Dubious lint types and functions ---

sealed class Dubious : LintWarning {
    /// Duplicate dictionary key `{key}`, also used at {span}
    class DuplicateKey(val key: String, val span: FileSpan) : Dubious() {
        override fun toString(): String = "Duplicate dictionary key `$key`, also used at ${span.description}"
    }

    /// Variable `{name}` will either do nothing or fail if uninitialised
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

// Go implementation of Starlark disallows duplicate top-level assignments,
// it's likely that will become Starlark standard sooner or later, so check now.
// The one place we allow it is to export something you grabbed with load.
internal fun duplicateDictionaryKey(module: AstModule, res: MutableList<LintT<Dubious>>) {
    sealed class Key {
        class Int(val value: StarlarkInt) : Key() {
            override fun equals(other: Any?): Boolean = other is Int && value == other.value
            override fun hashCode(): Int = value.hashCode()
        }

        class Float(val bits: Long) : Key() {
            override fun equals(other: Any?): Boolean = other is Float && bits == other.bits
            override fun hashCode(): Int = bits.hashCode()
        }

        class StringKey(val value: String) : Key() {
            override fun equals(other: Any?): Boolean = other is StringKey && value == other.value
            override fun hashCode(): Int = value.hashCode()
        }

        class Identifier(val value: String) : Key() {
            override fun equals(other: Any?): Boolean = other is Identifier && value == other.value
            override fun hashCode(): Int = value.hashCode()
        }
    }

    fun toKey(x: AstExpr): Pair<Key, Span>? {
        return when (val node = x.node) {
            is ExprP.Literal -> when (val lit = node.literal) {
                is AstLiteral.Int -> Key.Int(lit.node.node) to lit.node.span
                is AstLiteral.Float -> {
                    val n = NumRef.from(lit.node.node)
                    val asInt = n.asInt()
                    if (asInt != null) {
                        // make an integer float always collide with other ints
                        Key.Int(StarlarkInt(asInt)) to lit.node.span
                    } else {
                        // use bits representation of float to be able to always compare them for equality
                        // First normalise -0.0
                        val v = if (lit.node.node == 0.0) 0.0 else lit.node.node
                        Key.Float(v.toBits()) to lit.node.span
                    }
                }
                is AstLiteral.StringLit -> Key.StringKey(lit.node.node) to lit.node.span
                is AstLiteral.Ellipsis -> null
            }
            is ExprP.Identifier -> Key.Identifier(node.ident.node.ident) to node.ident.span
            else -> null
        }
    }

    fun expr(x: AstExpr, codemap: CodeMap, results: MutableList<LintT<Dubious>>) {
        when (val node = x.node) {
            is ExprP.Dict -> {
                val seen = HashMap<Key, Span>()
                for ((key, _) in node.entries) {
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
        x.visitExpr { child -> expr(child, codemap, results) }
    }

    module
        .statement
        .visitExpr { x -> expr(x, module.codemap, res) }
}

internal fun identifierAsStatement(module: AstModule, res: MutableList<LintT<Dubious>>) {
    fun stmt(x: AstStmt, codemap: CodeMap, results: MutableList<LintT<Dubious>>) {
        when (val node = x.node) {
            is StmtP.Expression -> when (val exprNode = node.expr.node) {
                is ExprP.Identifier -> results.add(
                    LintT.new(
                        codemap,
                        exprNode.ident.span,
                        Dubious.IdentifierAsStatement(exprNode.ident.node.ident)
                    )
                )
                else -> {}
            }
            else -> x.visitStmt { child -> stmt(child, codemap, results) }
        }
    }

    stmt(module.statement, module.codemap, res)
}

internal fun lint(module: AstModule): List<LintT<Dubious>> {
    val res = mutableListOf<LintT<Dubious>>()
    duplicateDictionaryKey(module, res)
    identifierAsStatement(module, res)
    return res
}
