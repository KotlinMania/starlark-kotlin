// port-lint: source src/analysis/incompatible.rs

package io.github.kotlinmania.starlark.analysis

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

import io.github.kotlinmania.starlark.codemap.CodeMap
import io.github.kotlinmania.starlark.codemap.FileSpan
import io.github.kotlinmania.starlark.codemap.Span
import io.github.kotlinmania.starlark.syntax.AstModule
import io.github.kotlinmania.starlark.syntax.ast.AssignTargetP
import io.github.kotlinmania.starlark.syntax.ast.AstAssignIdentP
import io.github.kotlinmania.starlark.syntax.ast.AstAssignTarget
import io.github.kotlinmania.starlark.syntax.ast.AstExpr
import io.github.kotlinmania.starlark.syntax.ast.AstNoPayload
import io.github.kotlinmania.starlark.syntax.ast.AstStmt
import io.github.kotlinmania.starlark.syntax.ast.BinOp
import io.github.kotlinmania.starlark.syntax.ast.ExprP
import io.github.kotlinmania.starlark.syntax.ast.StmtP
import io.github.kotlinmania.starlark.syntax.ast.toSourceString

sealed class Incompatibility : LintWarning {
    /** Type check should be written differently. */
    data class IncompatibleTypeCheck(
        val original: String,
        val replacement: String,
    ) : Incompatibility() {
        override fun toString(): String =
            "Type check `$original` should be written `$replacement`"
    }

    /** Duplicate top-level assignment. */
    data class DuplicateTopLevelAssign(
        val name: String,
        val firstDefined: FileSpan,
    ) : Incompatibility() {
        override fun toString(): String =
            "Duplicate top-level assignment of `$name`, first defined at $firstDefined"
    }

    override fun severity(): EvalSeverity = EvalSeverity.Warning

    override fun shortName(): String =
        when (this) {
            is IncompatibleTypeCheck -> "incompatible-type-check"
            is DuplicateTopLevelAssign -> "duplicate-top-level-assign"
        }
}

private val TYPES: Map<String, String> =
    mapOf(
        "bool" to "True",
        "tuple" to "()",
        "str" to "\"\"",
        "list" to "[]",
        "int" to "0",
    )

private fun AstStmt.visitStmt(visitor: (AstStmt) -> Unit) {
    when (val s = this.node) {
        is StmtP.Statements<AstNoPayload> -> s.stmts.forEach(visitor)
        is StmtP.Def<AstNoPayload, *> -> visitor(s.def.body)
        else -> {}
    }
}

private fun AstStmt.visitStmtChildrenExpr(visitor: (AstExpr) -> Unit) {
    when (val s = this.node) {
        is StmtP.Expression<AstNoPayload> -> visitor(s.expr)
        is StmtP.Statements<AstNoPayload> -> s.stmts.forEach { it.visitStmtChildrenExpr(visitor) }
        is StmtP.Def<AstNoPayload, *> -> s.def.body.visitStmtChildrenExpr(visitor)
        is StmtP.Assign<AstNoPayload> -> {
            visitor(s.assign.rhs)
        }
        is StmtP.AssignModify<AstNoPayload> -> {
            visitor(s.rhs)
        }
        else -> {}
    }
}

private fun AstExpr.visitExprChildren(visitor: (AstExpr) -> Unit) {
    when (val e = this.node) {
        is ExprP.Call<AstNoPayload> -> {
            visitor(e.expr)
            for (arg in e.args.args) {
                visitor(arg.node.expr())
            }
        }
        is ExprP.Op<AstNoPayload> -> {
            visitor(e.lhs)
            visitor(e.rhs)
        }
        else -> {}
    }
}

private fun AstAssignTarget.visitLvalue(visitor: (AstAssignIdentP<AstNoPayload, *>) -> Unit) {
    when (val t = this.node) {
        is AssignTargetP.Identifier<AstNoPayload, *> -> visitor(t.ident)
        is AssignTargetP.Tuple<AstNoPayload> -> t.elements.forEach { it.visitLvalue(visitor) }
        else -> {} // Index, Dot don't contain identifiers
    }
}

private fun lookupType(x: AstExpr, types: Map<String, String>): String? =
    when (val e = x.node) {
        is ExprP.Identifier<AstNoPayload, *> -> types[e.ident.node.ident]
        else -> null
    }

// Return true if this expression matches `type($x)`
private fun isTypeCall(x: AstExpr): Boolean =
    when (val e = x.node) {
        is ExprP.Call<AstNoPayload> -> {
            if (e.args.args.size == 1) {
                val func = e.expr.node
                func is ExprP.Identifier<AstNoPayload, *> && func.ident.node.ident == "type"
            } else {
                false
            }
        }
        else -> false
    }

private fun matchBadTypeEquality(
    codemap: CodeMap,
    x: AstExpr,
    types: Map<String, String>,
    res: MutableList<LintT<Incompatibility>>,
) {
    when (val e = x.node) {
        is ExprP.Op<AstNoPayload> -> {
            if ((e.op == BinOp.Equal || e.op == BinOp.NotEqual) && isTypeCall(e.lhs)) {
                val replacement = lookupType(e.rhs, types)
                if (replacement != null) {
                    res.add(
                        LintT.new(
                            codemap,
                            x.span,
                            Incompatibility.IncompatibleTypeCheck(
                                x.toSourceString(),
                                "${e.lhs.toSourceString()}${e.op.toSourceString()}type($replacement)",
                            ),
                        ),
                    )
                }
            }
        }
        else -> {}
    }
}

internal fun badTypeEquality(module: AstModule, res: MutableList<LintT<Incompatibility>>) {
    val types = TYPES

    fun check(
        codemap: CodeMap,
        x: AstExpr,
        types: Map<String, String>,
        res: MutableList<LintT<Incompatibility>>,
    ) {
        matchBadTypeEquality(codemap, x, types, res)
        x.visitExprChildren { check(codemap, it, types, res) }
    }
    module.statement.visitExprs { check(module.codemap, it, types, res) }
}

// Go implementation of Starlark disallows duplicate top-level assignments,
// it's likely that will become Starlark standard sooner or later, so check now.
// The one place we allow it is to export something you grabbed with load.

internal fun duplicateTopLevelAssignment(module: AstModule, res: MutableList<LintT<Incompatibility>>) {
    val defined = HashMap<String, Pair<Span, Boolean>>()
    val exported = HashSet<String>() // name's already exported

    fun ident(
        x: AstAssignIdentP<AstNoPayload, *>,
        isLoad: Boolean,
        codemap: CodeMap,
        defined: HashMap<String, Pair<Span, Boolean>>,
        res: MutableList<LintT<Incompatibility>>,
    ) {
        val old = defined[x.node.ident]
        if (old != null) {
            res.add(
                LintT.new(
                    codemap,
                    x.span,
                    Incompatibility.DuplicateTopLevelAssign(x.node.ident, codemap.fileSpan(old.first)),
                ),
            )
        } else {
            defined[x.node.ident] = Pair(x.span, isLoad)
        }
    }

    fun stmt(
        x: AstStmt,
        codemap: CodeMap,
        defined: HashMap<String, Pair<Span, Boolean>>,
        exported: HashSet<String>,
        res: MutableList<LintT<Incompatibility>>,
    ) {
        when (val s = x.node) {
            is StmtP.Assign<AstNoPayload> -> {
                val lhsNode = s.assign.lhs.node
                val rhsNode = s.assign.rhs.node
                when {
                    lhsNode is AssignTargetP.Identifier<AstNoPayload, *> &&
                        rhsNode is ExprP.Identifier<AstNoPayload, *> &&
                        lhsNode.ident.node.ident ==
                        rhsNode.ident.node.ident &&
                        defined[lhsNode.ident.node.ident]?.second == true &&
                        !exported.contains(lhsNode.ident.node.ident) -> {
                        // Normally this would be an error, but if we load()'d it,
                        // this is how we'd reexport through Starlark.
                        // But only allow one export
                        exported.add(lhsNode.ident.node.ident)
                    }
                    else -> s.assign.lhs.visitLvalue { ident(it, false, codemap, defined, res) }
                }
            }
            is StmtP.AssignModify<AstNoPayload> -> {
                s.lhs.visitLvalue { ident(it, false, codemap, defined, res) }
            }
            is StmtP.Def<AstNoPayload, *> -> {
                ident(s.def.name, false, codemap, defined, res)
            }
            is StmtP.Load<AstNoPayload, *> -> {
                for (arg in s.loadStmt.args) {
                    ident(arg.local, true, codemap, defined, res)
                }
            }
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

/** Lint an AST module for incompatibilities. */
internal fun incompatibleLint(module: AstModule): List<LintT<Incompatibility>> {
    val res = mutableListOf<LintT<Incompatibility>>()
    badTypeEquality(module, res)
    duplicateTopLevelAssignment(module, res)
    return res
}
