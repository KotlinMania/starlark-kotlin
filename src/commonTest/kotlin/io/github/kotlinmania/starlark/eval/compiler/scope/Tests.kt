// port-lint: tests src/eval/compiler/scope/tests.rs
@file:Suppress("UNCHECKED_CAST", "USELESS_CAST", "CAST_NEVER_SUCCEED")

package io.github.kotlinmania.starlark.eval.compiler.scope

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

import io.github.kotlinmania.starlark.environment.Globals
import io.github.kotlinmania.starlark.environment.MutableNames
import io.github.kotlinmania.starlark.eval.compiler.AssignCount
import io.github.kotlinmania.starlark.eval.compiler.Captured
import io.github.kotlinmania.starlark.eval.compiler.ModuleScopes
import io.github.kotlinmania.starlark.eval.compiler.ResolvedIdent
import io.github.kotlinmania.starlark.eval.compiler.Slot
import io.github.kotlinmania.starlark.syntax.AstModule
import io.github.kotlinmania.starlark.syntax.ast.AssignP
import io.github.kotlinmania.starlark.syntax.ast.AssignTargetP
import io.github.kotlinmania.starlark.syntax.ast.ClauseP
import io.github.kotlinmania.starlark.syntax.ast.DefP
import io.github.kotlinmania.starlark.syntax.ast.ExprP
import io.github.kotlinmania.starlark.syntax.ast.FStringP
import io.github.kotlinmania.starlark.syntax.ast.ForClauseP
import io.github.kotlinmania.starlark.syntax.ast.ForP
import io.github.kotlinmania.starlark.syntax.ast.ParameterP
import io.github.kotlinmania.starlark.syntax.ast.StmtP
import io.github.kotlinmania.starlark.syntax.dialect.Dialect
import io.github.kotlinmania.starlark.values.FrozenRef
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap

private fun testWithModule(program: String, expected: String, module: MutableNames) {
    val ast = AstModule.parse("t.star", program, Dialect.AllOptionsInternal).getOrThrow()
    val frozenHeap = FrozenHeap.new()
    val codemap = FrozenRef.new(ast.codemap)
    val parts = ast.intoParts()
    val scopes =
        ModuleScopes.checkModuleErr(
            module,
            frozenHeap,
            mapOf(),
            parts.statement,
            ScopeResolverGlobals(
                globals = FrozenRef.new(Globals.new()),
            ),
            codemap,
            Dialect.AllOptionsInternal,
        )
    val cst = scopes.cst
    val scopeData = scopes.scopeData

    val r = StringBuilder()
    for ((i, binding) in scopeData.bindings.withIndex()) {
        if (i != 0) {
            r.append(' ')
        }
        val slot =
            when (val s = binding.slot!!) {
                is Slot.Module -> "m=${s.id.index}"
                is Slot.Local -> "l=${s.id.index}"
            }
        val assignCount =
            when (binding.assignCount) {
                AssignCount.AtMostOnce -> ""
                AssignCount.Any -> "+"
            }
        val captured =
            when (binding.captured) {
                Captured.Yes -> "&"
                Captured.No -> ""
            }
        r.append("$i:$slot$assignCount$captured")
    }

    r.append(" |")

    @Suppress("UNCHECKED_CAST")
    class Visitor(
        val r: StringBuilder,
    ) {
        fun visitExpr(expr: CstExpr) {
            val node = expr.node
            if (node is ExprP.Identifier<*, *>) {
                val ident = node.ident as CstIdent
                val resolved =
                    when (val payload = ident.node.payload!!) {
                        is ResolvedIdent.Slot -> payload.bindingId.id.toString()
                        is ResolvedIdent.Global -> "G"
                    }
                r.append(" ${ident.node.ident}:$resolved")
            }

            visitExprChildren(expr) { e -> visitExpr(e) }
        }

        fun visitExprs(exprs: Iterable<CstExpr>) {
            for (expr in exprs) {
                visitExpr(expr)
            }
        }

        fun visitLvalue(ident: CstAssignIdent) {
            r.append(" ${ident.node.ident}:${ident.node.payload!!.id}")
        }

        fun visitStmtChildren(stmt: CstStmt) {
            visitStmtChildrenImpl(stmt) { visit ->
                when (visit) {
                    is Visit.Stmt -> visitStmt(visit.stmt)
                    is Visit.Expr -> visitExpr(visit.expr)
                }
            }
        }

        fun visitAssign(assign: CstAssignTarget) {
            visitAssignLvalue(assign) { ident -> visitLvalue(ident) }
        }

        fun visitStmt(stmt: CstStmt) {
            when (val node = stmt.node) {
                is StmtP.Assign<*> -> visitAssign((node.assign as AssignP<CstPayload>).lhs)
                is StmtP.Def<*, *> -> {
                    val def = node.def as DefP<CstPayload, *>
                    visitLvalue(def.name as CstAssignIdent)
                    for (param in def.params) {
                        val (name, typ, defVal) = splitParam(param.node as ParameterP<CstPayload>)
                        if (name != null) {
                            visitLvalue(name as CstAssignIdent)
                        }
                        if (defVal != null) {
                            visitExprs(listOf(defVal))
                        }
                        if (typ != null) {
                            visitExprs(listOf(typ.node.expr))
                        }
                    }
                }
                is StmtP.For<*> -> {
                    val forP = node.forStmt as ForP<CstPayload>
                    visitAssign(forP.varTarget)
                }
                else -> {}
            }
            visitStmtChildren(stmt)
        }
    }

    Visitor(r).visitStmt(cst)

    check(expected == r.toString()) { "Expected: $expected\nActual: $r" }
}

/**
 * sealed class Visit is in typing/Bindings.kt but scoped to that file.
 * We need our own local Visit for this test file.
 */
private sealed class Visit {
    data class Stmt(
        val stmt: CstStmt,
    ) : Visit()

    data class Expr(
        val expr: CstExpr,
    ) : Visit()
}

/**
 * Split a parameter into name, type, default value.
 * Port of Rust's ParameterP::split.
 */
@Suppress("UNCHECKED_CAST")
private fun splitParam(param: ParameterP<CstPayload>): Triple<CstAssignIdent?, CstTypeExpr?, CstExpr?> =
    when (param) {
        is ParameterP.Normal<*> ->
            Triple(
                param.name as CstAssignIdent,
                param.typ as CstTypeExpr?,
                param.defaultVal as CstExpr?,
            )
        is ParameterP.Args<*> ->
            Triple(
                param.name as CstAssignIdent,
                param.typ as CstTypeExpr?,
                null,
            )
        is ParameterP.KwArgs<*> ->
            Triple(
                param.name as CstAssignIdent,
                param.typ as CstTypeExpr?,
                null,
            )
        is ParameterP.NoArgs<*>, is ParameterP.Slash<*> -> Triple(null, null, null)
    }

/** Visit lvalues in an assign target (port of AssignTargetP::visit_lvalue). */
@Suppress("UNCHECKED_CAST")
private fun visitAssignLvalue(assign: CstAssignTarget, f: (CstAssignIdent) -> Unit) {
    when (val node = assign.node) {
        is AssignTargetP.Identifier<*, *> -> f(node.ident as CstAssignIdent)
        is AssignTargetP.Tuple<*> -> (node.elements as List<CstAssignTarget>).forEach { visitAssignLvalue(it, f) }
        else -> {}
    }
}

/** Visit child expressions of an ExprP (port of ExprP::visit_expr). */
@Suppress("UNCHECKED_CAST")
private fun visitExprChildren(expr: CstExpr, f: (CstExpr) -> Unit) {
    when (val node = expr.node) {
        is ExprP.Tuple<*> -> (node.elements as List<CstExpr>).forEach { f(it) }
        is ExprP.Dot<*> -> f(node.expr as CstExpr)
        is ExprP.Call<*> -> {
            f(node.expr as CstExpr)
            (node as ExprP.Call<CstPayload>).args.args.forEach { f(it.node.expr()) }
        }
        is ExprP.Index<*> -> {
            f(node.expr as CstExpr)
            f(node.index as CstExpr)
        }
        is ExprP.Index2<*> -> {
            f(node.expr as CstExpr)
            f(node.index0 as CstExpr)
            f(node.index1 as CstExpr)
        }
        is ExprP.Slice<*> -> {
            f(node.expr as CstExpr)
            (node.start as CstExpr?)?.let { f(it) }
            (node.stop as CstExpr?)?.let { f(it) }
            (node.step as CstExpr?)?.let { f(it) }
        }
        is ExprP.Identifier<*, *> -> {}
        is ExprP.Lambda<*, *> -> {
            val l = node as ExprP.Lambda<CstPayload, *>
            l.lambda.params.forEach { p ->
                val (_, typ, defVal) = splitParam(p.node as ParameterP<CstPayload>)
                typ?.let { f(it.node.expr) }
                defVal?.let { f(it) }
            }
            f(l.lambda.body)
        }
        is ExprP.Literal<*> -> {}
        is ExprP.Not<*> -> f(node.expr as CstExpr)
        is ExprP.Minus<*> -> f(node.expr as CstExpr)
        is ExprP.Plus<*> -> f(node.expr as CstExpr)
        is ExprP.BitNot<*> -> f(node.expr as CstExpr)
        is ExprP.Op<*> -> {
            f(node.lhs as CstExpr)
            f(node.rhs as CstExpr)
        }
        is ExprP.If<*> -> {
            f(node.cond as CstExpr)
            f(node.v1 as CstExpr)
            f(node.v2 as CstExpr)
        }
        is ExprP.ListExpr<*> -> (node.elements as List<CstExpr>).forEach { f(it) }
        is ExprP.Dict<*> ->
            (node.elements as List<Pair<CstExpr, CstExpr>>).forEach { (k, v) ->
                f(k)
                f(v)
            }
        is ExprP.ListComprehension<*> -> {
            val lc = node as ExprP.ListComprehension<CstPayload>
            visitForClauseExprs(lc.forClause, f)
            lc.clauses.forEach { visitClauseExprs(it, f) }
            f(lc.expr)
        }
        is ExprP.DictComprehension<*> -> {
            val dc = node as ExprP.DictComprehension<CstPayload>
            visitForClauseExprs(dc.forClause, f)
            dc.clauses.forEach { visitClauseExprs(it, f) }
            f(dc.key)
            f(dc.value)
        }
        is ExprP.FString<*> -> (node.fstring as Any as FStringP<CstPayload>).expressions.forEach { f(it as CstExpr) }
    }
}

/** Visit child statements/expressions of a StmtP (port of StmtP::visit_children). */
@Suppress("UNCHECKED_CAST")
private fun visitStmtChildrenImpl(stmt: CstStmt, f: (Visit) -> Unit) {
    when (val node = stmt.node) {
        is StmtP.Statements<*> -> (node.stmts as List<CstStmt>).forEach { f(Visit.Stmt(it)) }
        is StmtP.If<*> -> {
            f(Visit.Expr(node.cond as CstExpr))
            f(Visit.Stmt(node.suite as CstStmt))
        }
        is StmtP.IfElse<*> -> {
            f(Visit.Expr(node.cond as CstExpr))
            f(Visit.Stmt(node.suite1 as CstStmt))
            f(Visit.Stmt(node.suite2 as CstStmt))
        }
        is StmtP.Def<*, *> -> {
            val def = node.def as DefP<CstPayload, *>
            def.params.forEach { p ->
                val (_, typ, defVal) = splitParam(p.node as ParameterP<CstPayload>)
                typ?.let { f(Visit.Expr(it.node.expr)) }
                defVal?.let { f(Visit.Expr(it)) }
            }
            def.returnType?.let { f(Visit.Expr(it.node.expr)) }
            f(Visit.Stmt(def.body))
        }
        is StmtP.For<*> -> {
            val fp = node.forStmt as ForP<CstPayload>
            visitAssignTargetExprs(fp.varTarget) { f(Visit.Expr(it)) }
            f(Visit.Expr(fp.over))
            f(Visit.Stmt(fp.body))
        }
        is StmtP.Return<*> -> {
            val r = node.expr as CstExpr?
            if (r != null) f(Visit.Expr(r))
        }
        is StmtP.Expression<*> -> f(Visit.Expr(node.expr as CstExpr))
        is StmtP.Assign<*> -> {
            val a = node.assign as AssignP<CstPayload>
            visitAssignTargetExprs(a.lhs) { f(Visit.Expr(it)) }
            a.ty?.let { f(Visit.Expr(it.node.expr)) }
            f(Visit.Expr(a.rhs))
        }
        is StmtP.AssignModify<*> -> {
            visitAssignTargetExprs(node.lhs as CstAssignTarget) { f(Visit.Expr(it)) }
            f(Visit.Expr(node.rhs as CstExpr))
        }
        is StmtP.Break<*>, is StmtP.Continue<*>, is StmtP.Pass<*>, is StmtP.Load<*, *> -> {}
    }
}

/** Visit expressions within an assign target (port of AssignTargetP::visit_expr). */
@Suppress("UNCHECKED_CAST")
private fun visitAssignTargetExprs(target: CstAssignTarget, f: (CstExpr) -> Unit) {
    when (val node = target.node) {
        is AssignTargetP.Tuple<*> -> (node.elements as List<CstAssignTarget>).forEach { visitAssignTargetExprs(it, f) }
        is AssignTargetP.Dot<*> -> f(node.expr as CstExpr)
        is AssignTargetP.Index<*> -> {
            f(node.expr as CstExpr)
            f(node.index as CstExpr)
        }
        is AssignTargetP.Identifier<*, *> -> {}
    }
}

/** Visit expressions within a for clause. */
@Suppress("UNCHECKED_CAST")
private fun visitForClauseExprs(forClause: ForClauseP<CstPayload>, f: (CstExpr) -> Unit) {
    visitAssignTargetExprs(forClause.varTarget, f)
    f(forClause.over)
}

/** Visit expressions within a clause. */
@Suppress("UNCHECKED_CAST")
private fun visitClauseExprs(clause: ClauseP<CstPayload>, f: (CstExpr) -> Unit) {
    when (clause) {
        is ClauseP.For<*> -> visitForClauseExprs(clause.forClause as ForClauseP<CstPayload>, f)
        is ClauseP.If<*> -> f(clause.cond as CstExpr)
    }
}

private fun t(program: String, expected: String) {
    val module = MutableNames()
    testWithModule(program, expected, module)
}

// Expected test output (second parameter to `t` function) is:
// * list of bindings in format like `1:l=2` means binding id = 1, local slot 2
// * list of variables with references to binding ids

internal fun basic() {
    t("x = 1; y = 2", "0:m=0 1:m=1 | x:0 y:1")
}

internal fun moduleReassignment() {
    t("x = 1; x = 2", "0:m=0+ | x:0 x:0")
}

internal fun reassignmentInLoop() {
    t("for x in []: y = x", "0:m=0+ 1:m=1+ | x:0 y:1 x:0")
}

internal fun defCapture() {
    t("x = 1\ndef f(): x", "0:m=0& 1:m=1 | x:0 f:1 x:0")
}

internal fun defShadow() {
    t("x = 1\ndef f(): x = 2", "0:m=0 1:m=1 2:l=0 | x:0 f:1 x:2")
}

internal fun defParamBindings() {
    t("def f(x): return x", "0:m=0 1:l=0 | f:0 x:1 x:1")
}

internal fun nestedDefCapture() {
    t(
        "def f():\n    x = 1\n    def g(): return x",
        "0:m=0 1:l=0& 2:l=1 | f:0 x:1 g:2 x:1",
    )
}

internal fun existingModuleWithNames() {
    val frozenHeap = FrozenHeap.new()
    val module = MutableNames()
    module.addName(frozenHeap.allocStrIntern("x"))
    module.addName(frozenHeap.allocStrIntern("y"))
    testWithModule("x = y", "0:m=0+ 1:m=1 | x:0 y:1", module)
}
