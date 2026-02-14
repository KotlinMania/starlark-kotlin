// port-lint: source src/analysis/dubious.rs
package io.github.kotlinmania.starlark_kotlin.analysis.dubious

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

// Placeholder types referenced from other modules
// These will be replaced with real imports as the port progresses
class AstExpr(val node: Expr, val span: Span) {
    fun visitExpr(visitor: (AstExpr) -> Unit) {
        node.visitExpr(this, visitor)
    }

    override fun toString(): String = node.toString()
}

sealed class Expr {
    class Literal(val literal: AstLiteral) : Expr()
    class Identifier(val ident: AstIdentifier) : Expr()
    class Dict(val entries: List<Pair<AstExpr, AstExpr>>) : Expr()
    class Other : Expr()

    fun visitExpr(parent: AstExpr, visitor: (AstExpr) -> Unit) {
        when (this) {
            is Dict -> entries.forEach { (k, v) -> visitor(k); visitor(v) }
            else -> {}
        }
    }
}

sealed class AstLiteral {
    class Int(val node: Spanned<StarlarkInt>) : AstLiteral()
    class Float(val node: Spanned<Double>) : AstLiteral()
    class StringLit(val node: Spanned<String>) : AstLiteral()
    data object Ellipsis : AstLiteral()
}

class AstIdentifier(val node: IdentNode, val span: Span)
class IdentNode(val ident: String)
class Spanned<T>(val node: T, val span: Span)

class AstStmt(val node: Stmt, val span: Span) {
    fun visitStmt(visitor: (AstStmt) -> Unit) {
        node.visitStmt(this, visitor)
    }

    fun visitExpr(visitor: (AstExpr) -> Unit) {
        node.visitExpr(visitor)
    }
}

sealed class Stmt {
    class Expression(val expr: AstExpr) : Stmt()
    class Other(val children: List<AstStmt> = emptyList()) : Stmt()

    fun visitStmt(parent: AstStmt, visitor: (AstStmt) -> Unit) {
        when (this) {
            is Expression -> {}
            is Other -> children.forEach(visitor)
        }
    }

    fun visitExpr(visitor: (AstExpr) -> Unit) {
        when (this) {
            is Expression -> visitor(expr)
            is Other -> {}
        }
    }
}

class StarlarkInt(val value: Long) {
    override fun equals(other: Any?): Boolean = other is StarlarkInt && value == other.value
    override fun hashCode(): Int = value.hashCode()
}

class Span
class FileSpan(val description: String)
class CodeMap {
    fun fileSpan(span: Span): FileSpan = FileSpan("$span")
}

enum class EvalSeverity {
    Warning,
    Error,
}

class LintT<T : LintWarning>(
    val codemap: CodeMap,
    val span: Span,
    val problem: T,
) {
    companion object {
        fun <T : LintWarning> new(codemap: CodeMap, span: Span, problem: T): LintT<T> {
            return LintT(codemap, span, problem)
        }
    }
}

interface LintWarning {
    fun severity(): EvalSeverity
    fun shortName(): String
}

class NumRef(val value: Double) {
    companion object {
        fun from(d: Double): NumRef = NumRef(d)
    }

    fun asInt(): Long? {
        val l = value.toLong()
        return if (l.toDouble() == value) l else null
    }
}

class AstModule(
    private val statement: AstStmt,
    private val codemap: CodeMap,
) {
    fun statement(): AstStmt = statement
    fun codemap(): CodeMap = codemap
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
            is Expr.Literal -> when (val lit = node.literal) {
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
            is Expr.Identifier -> Key.Identifier(node.ident.node.ident) to node.ident.span
            else -> null
        }
    }

    fun expr(x: AstExpr, codemap: CodeMap, results: MutableList<LintT<Dubious>>) {
        when (val node = x.node) {
            is Expr.Dict -> {
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
        .statement()
        .visitExpr { x -> expr(x, module.codemap(), res) }
}

internal fun identifierAsStatement(module: AstModule, res: MutableList<LintT<Dubious>>) {
    fun stmt(x: AstStmt, codemap: CodeMap, results: MutableList<LintT<Dubious>>) {
        when (val node = x.node) {
            is Stmt.Expression -> when (val exprNode = node.expr.node) {
                is Expr.Identifier -> results.add(
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

    stmt(module.statement(), module.codemap(), res)
}

internal fun lint(module: AstModule): List<LintT<Dubious>> {
    val res = mutableListOf<LintT<Dubious>>()
    duplicateDictionaryKey(module, res)
    identifierAsStatement(module, res)
    return res
}
