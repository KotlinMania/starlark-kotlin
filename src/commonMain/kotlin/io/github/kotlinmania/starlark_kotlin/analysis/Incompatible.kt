// port-lint: source src/analysis/incompatible.rs
package io.github.kotlinmania.starlark_kotlin.analysis

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
class IncompatSpan(val begin: Int = 0, val end: Int = 0)
class IncompatCodeMap {
    fun fileSpan(span: IncompatSpan): IncompatFileSpan = IncompatFileSpan()
}
class IncompatFileSpan {
    override fun toString(): String = "<span>"
}
class IncompatSpanned<T>(val node: T, val span: IncompatSpan = IncompatSpan())

sealed class IncompatStmt {
    data class Return(val expr: IncompatAstExpr?) : IncompatStmt()
    data class Expression(val expr: IncompatAstExpr) : IncompatStmt()
    data class Statements(val stmts: List<IncompatAstStmt>) : IncompatStmt()
    // TODO: stub - Def needs real import
    data class Def(val def: IncompatDefP) : IncompatStmt()
    data class Assign(val assign: IncompatAssign) : IncompatStmt()
    data class AssignModify(val lhs: IncompatAstAssignTarget, val op: IncompatBinOp, val rhs: IncompatAstExpr) : IncompatStmt()
    data class Load(val load: IncompatLoadStmt) : IncompatStmt()
    // TODO: stub - Other needs real import
    class Other : IncompatStmt()
}

class IncompatAssign(
    val lhs: IncompatAstAssignTarget,
    val rhs: IncompatAstExpr,
)

sealed class IncompatAssignTarget {
    // TODO: stub - Identifier needs real import
    data class Identifier(val name: IncompatSpanned<IncompatIdent>) : IncompatAssignTarget()
    // TODO: stub - Other needs real import
    class Other : IncompatAssignTarget()
}

class IncompatAstAssignTarget(val node: IncompatAssignTarget, val span: IncompatSpan = IncompatSpan()) {
    fun visitLvalue(visitor: (IncompatAstAssignIdent) -> Unit) {
        when (val t = node) {
            is IncompatAssignTarget.Identifier -> visitor(IncompatAstAssignIdent(t.name.node.ident, t.name.span))
            is IncompatAssignTarget.Other -> {}
        }
    }
}

class IncompatDefP(
    val name: IncompatAstAssignIdent,
    val body: IncompatAstStmt,
)

class IncompatLoadStmt(
    val args: List<IncompatLoadArgP>,
)

class IncompatLoadArgP(
    val local: IncompatAstAssignIdent,
    val remote: String,
)

class IncompatAstAssignIdent(val ident: String, val span: IncompatSpan = IncompatSpan())

sealed class IncompatExpr {
    data class Call(val func: IncompatAstExpr, val args: IncompatCallArgs) : IncompatExpr()
    // TODO: stub - Identifier needs real import
    data class Identifier(val name: IncompatSpanned<IncompatIdent>) : IncompatExpr()
    data class Op(val lhs: IncompatAstExpr, val op: IncompatBinOp, val rhs: IncompatAstExpr) : IncompatExpr()
    // TODO: stub - Other needs real import
    class Other : IncompatExpr()
}

class IncompatCallArgs(val args: List<IncompatAstExpr>)

class IncompatIdent(val ident: String) {
    override fun toString(): String = ident
}

enum class IncompatBinOp {
    Equal,
    NotEqual,
    Add,
    Other,
}

typealias IncompatAstStmt = IncompatSpanned<IncompatStmt>
typealias IncompatAstExpr = IncompatSpanned<IncompatExpr>

class IncompatEvalSeverity {
    companion object {
        val Warning = IncompatEvalSeverity()
    }
}

class IncompatLintT<T>(
    val location: IncompatFileSpan,
    val problem: T,
) {
    companion object {
        fun <T> new(codemap: IncompatCodeMap, span: IncompatSpan, problem: T): IncompatLintT<T> {
            return IncompatLintT(codemap.fileSpan(span), problem)
        }
    }
}

interface IncompatLintWarning {
    fun severity(): IncompatEvalSeverity
    fun shortName(): String
}

class IncompatAstModule(
    private val codemap: IncompatCodeMap,
    private val statement: IncompatAstStmt,
) {
    fun codemap(): IncompatCodeMap = codemap
    fun statement(): IncompatAstStmt = statement
}

// visit_stmt helper
private fun IncompatAstStmt.visitStmt(visitor: (IncompatAstStmt) -> Unit) {
    when (val s = this.node) {
        is IncompatStmt.Statements -> s.stmts.forEach(visitor)
        is IncompatStmt.Def -> visitor(s.def.body)
        else -> {}
    }
}

// visit_expr helper: visit immediate child expressions in a statement
private fun IncompatAstStmt.visitExpr(visitor: (IncompatAstExpr) -> Unit) {
    when (val s = this.node) {
        is IncompatStmt.Expression -> visitor(s.expr)
        is IncompatStmt.Statements -> s.stmts.forEach { it.visitExpr(visitor) }
        is IncompatStmt.Def -> s.def.body.visitExpr(visitor)
        is IncompatStmt.Assign -> {
            visitor(s.assign.rhs)
        }
        is IncompatStmt.AssignModify -> {
            visitor(s.rhs)
        }
        else -> {}
    }
}

// visit_expr helper for expressions
private fun IncompatAstExpr.visitExpr(visitor: (IncompatAstExpr) -> Unit) {
    when (val e = this.node) {
        is IncompatExpr.Call -> {
            visitor(e.func)
            e.args.args.forEach(visitor)
        }
        is IncompatExpr.Op -> {
            visitor(e.lhs)
            visitor(e.rhs)
        }
        else -> {}
    }
}

sealed class Incompatibility : IncompatLintWarning {
    /// Type check should be written differently.
    data class IncompatibleTypeCheck(
        val original: String,
        val replacement: String,
    ) : Incompatibility() {
        override fun toString(): String =
            "Type check `$original` should be written `$replacement`"
    }

    /// Duplicate top-level assignment.
    data class DuplicateTopLevelAssign(
        val name: String,
        val firstDefined: IncompatFileSpan,
    ) : Incompatibility() {
        override fun toString(): String =
            "Duplicate top-level assignment of `$name`, first defined at $firstDefined"
    }

    override fun severity(): IncompatEvalSeverity {
        return IncompatEvalSeverity.Warning
    }

    override fun shortName(): String {
        return when (this) {
            is IncompatibleTypeCheck -> "incompatible-type-check"
            is DuplicateTopLevelAssign -> "duplicate-top-level-assign"
        }
    }
}

private val TYPES: Map<String, String> = mapOf(
    "bool" to "True",
    "tuple" to "()",
    "str" to "\"\"",
    "list" to "[]",
    "int" to "0",
)

private fun lookupType(x: IncompatAstExpr, types: Map<String, String>): String? {
    return when (val e = x.node) {
        is IncompatExpr.Identifier -> types[e.name.node.ident]
        else -> null
    }
}

// Return true if this expression matches `type($x)`
private fun isTypeCall(x: IncompatAstExpr): Boolean {
    return when (val e = x.node) {
        is IncompatExpr.Call -> {
            if (e.args.args.size == 1) {
                when (val func = e.func.node) {
                    is IncompatExpr.Identifier -> func.name.node.ident == "type"
                    else -> false
                }
            } else {
                false
            }
        }
        else -> false
    }
}

private fun matchBadTypeEquality(
    codemap: IncompatCodeMap,
    x: IncompatAstExpr,
    types: Map<String, String>,
    res: MutableList<IncompatLintT<Incompatibility>>,
) {
    // If we see type(x) == y (or negated), where y is in our types table, suggest a replacement
    when (val e = x.node) {
        is IncompatExpr.Op -> {
            if ((e.op == IncompatBinOp.Equal || e.op == IncompatBinOp.NotEqual) && isTypeCall(e.lhs)) {
                val replacement = lookupType(e.rhs, types)
                if (replacement != null) {
                    res.add(
                        IncompatLintT.new(
                            codemap,
                            x.span,
                            Incompatibility.IncompatibleTypeCheck(
                                x.toString(),
                                "${e.lhs.node}${e.op}type($replacement)",
                            ),
                        )
                    )
                }
            }
        }
        else -> {}
    }
}

private fun badTypeEquality(module: IncompatAstModule, res: MutableList<IncompatLintT<Incompatibility>>) {
    val types = TYPES
    fun check(
        codemap: IncompatCodeMap,
        x: IncompatAstExpr,
        types: Map<String, String>,
        res: MutableList<IncompatLintT<Incompatibility>>,
    ) {
        matchBadTypeEquality(codemap, x, types, res)
        x.visitExpr { check(codemap, it, types, res) }
    }
    module.statement.visitExpr { check(module.codemap, it, types, res) }
}

// Go implementation of Starlark disallows duplicate top-level assignments,
// it's likely that will become Starlark standard sooner or later, so check now.
// The one place we allow it is to export something you grabbed with load.
private fun duplicateTopLevelAssignment(module: IncompatAstModule, res: MutableList<IncompatLintT<Incompatibility>>) {
    val defined = HashMap<String, Pair<IncompatSpan, Boolean>>() // (name, (location, is_load))
    val exported = HashSet<String>() // name's already exported by is_load

    fun ident(
        x: IncompatAstAssignIdent,
        isLoad: Boolean,
        codemap: IncompatCodeMap,
        defined: HashMap<String, Pair<IncompatSpan, Boolean>>,
        res: MutableList<IncompatLintT<Incompatibility>>,
    ) {
        val old = defined[x.ident]
        if (old != null) {
            res.add(
                IncompatLintT.new(
                    codemap,
                    x.span,
                    Incompatibility.DuplicateTopLevelAssign(x.ident, codemap.fileSpan(old.first)),
                )
            )
        } else {
            defined[x.ident] = Pair(x.span, isLoad)
        }
    }

    fun stmt(
        x: IncompatAstStmt,
        codemap: IncompatCodeMap,
        defined: HashMap<String, Pair<IncompatSpan, Boolean>>,
        exported: HashSet<String>,
        res: MutableList<IncompatLintT<Incompatibility>>,
    ) {
        when (val s = x.node) {
            is IncompatStmt.Assign -> {
                val lhs = s.assign.lhs
                val rhs = s.assign.rhs
                when {
                    lhs.node is IncompatAssignTarget.Identifier
                        && rhs.node is IncompatExpr.Identifier
                        && (lhs.node as IncompatAssignTarget.Identifier).name.node.ident ==
                            (rhs.node as IncompatExpr.Identifier).name.node.ident
                        && defined[(lhs.node as IncompatAssignTarget.Identifier).name.node.ident]?.second == true
                        && !exported.contains((lhs.node as IncompatAssignTarget.Identifier).name.node.ident) -> {
                        // Normally this would be an error, but if we load()'d it,
                        // this is how we'd reexport through Starlark.
                        // But only allow one export
                        exported.add((lhs.node as IncompatAssignTarget.Identifier).name.node.ident)
                    }
                    else -> lhs.visitLvalue { ident(it, false, codemap, defined, res) }
                }
            }
            is IncompatStmt.AssignModify -> {
                s.lhs.visitLvalue { ident(it, false, codemap, defined, res) }
            }
            is IncompatStmt.Def -> {
                ident(s.def.name, false, codemap, defined, res)
            }
            is IncompatStmt.Load -> {
                for (arg in s.load.args) {
                    ident(arg.local, true, codemap, defined, res)
                }
            }
            // Visit statements, but don't descend under def - only top-level statements are interesting
            else -> x.visitStmt { stmt(it, codemap, defined, exported, res) }
        }
    }

    stmt(
        module.statement,
        module.codemap,
        defined,
        exported,
        res,
    )
}

/// Lint an AST module for incompatibilities.
fun incompatibleLint(module: IncompatAstModule): List<IncompatLintT<Incompatibility>> {
    val res = mutableListOf<IncompatLintT<Incompatibility>>()
    badTypeEquality(module, res)
    duplicateTopLevelAssignment(module, res)
    return res
}
