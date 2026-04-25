// port-lint: source src/eval/compiler/scope/tests.rs
package io.github.kotlinmania.starlark_kotlin.eval.compiler.scope

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

import io.github.kotlinmania.starlark_kotlin.environment.Globals
import io.github.kotlinmania.starlark_kotlin.environment.MutableNames
import io.github.kotlinmania.starlark_kotlin.eval.compiler.AssignCount
import io.github.kotlinmania.starlark_kotlin.eval.compiler.BindingId
import io.github.kotlinmania.starlark_kotlin.eval.compiler.Captured
import io.github.kotlinmania.starlark_kotlin.eval.compiler.ModuleScopes
import io.github.kotlinmania.starlark_kotlin.eval.compiler.ResolvedIdent
import io.github.kotlinmania.starlark_kotlin.eval.compiler.Slot
import io.github.kotlinmania.starlark_kotlin.syntax.AstModule
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AssignP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AssignIdentP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AssignTargetP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.DefP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ExprP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ForP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.IdentP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ParameterP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.StmtP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.TypeExprP
import io.github.kotlinmania.starlark_kotlin.syntax.dialect.Dialect
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ClauseP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ForClauseP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.FStringP
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenRef
import io.github.kotlinmania.starlark_kotlin.codemap.*

// fn test_with_module(program: &str, expected: &str, module: &MutableNames)
private fun testWithModule(program: String, expected: String, module: MutableNames) {
    val ast = AstModule.parse("t.star", program, Dialect.AllOptionsInternal).getOrThrow()
    val frozenHeap = FrozenHeap.new()
    val codemap = FrozenRef.new(ast.codemap)
    val parts = ast.intoParts()
    val scopes = ModuleScopes.checkModuleErr(
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
        val slot = when (val s = binding.slot!!) {
            is Slot.Module -> "m=${s.id.index}"
            is Slot.Local -> "l=${s.id.index}"
        }
        val assignCount = when (binding.assignCount) {
            AssignCount.AtMostOnce -> ""
            AssignCount.Any -> "+"
        }
        val captured = when (binding.captured) {
            Captured.Yes -> "&"
            Captured.No -> ""
        }
        r.append("$i:$slot$assignCount$captured")
    }

    r.append(" |")

    // struct Visitor
    @Suppress("UNCHECKED_CAST")
    class Visitor(val r: StringBuilder) {
        fun visitExpr(expr: Spanned<ExprP<CstPayload>>) {
            val node = expr.node
            if (node is ExprP.Identifier<*, *>) {
                val ident = node.ident as Spanned<IdentP<CstPayload, ResolvedIdent?>>
                val resolved = when (val payload = ident.node.payload!!) {
                    is ResolvedIdent.Slot -> payload.bindingId.id.toString()
                    is ResolvedIdent.Global -> "G"
                }
                r.append(" ${ident.node.ident}:$resolved")
            }

            visitExprChildren(expr) { e -> visitExpr(e) }
        }

        fun visitExprs(exprs: Iterable<Spanned<ExprP<CstPayload>>>) {
            for (expr in exprs) {
                visitExpr(expr)
            }
        }

        fun visitLvalue(ident: Spanned<AssignIdentP<CstPayload, *>>) {
            val bindingId = ident.node.payload as? BindingId ?: error("binding not assigned for ident")
            r.append(" ${ident.node.ident}:${bindingId.id}")
        }

        fun visitStmtChildren(stmt: Spanned<StmtP<CstPayload>>) {
            visitStmtChildrenImpl(stmt) { visit ->
                when (visit) {
                    is Visit.Stmt -> visitStmt(visit.stmt)
                    is Visit.Expr -> visitExpr(visit.expr)
                }
            }
        }

        fun visitAssign(assign: Spanned<AssignTargetP<CstPayload>>) {
            visitAssignLvalue(assign) { ident -> visitLvalue(ident) }
        }

        fun visitStmt(stmt: Spanned<StmtP<CstPayload>>) {
            when (val node = stmt.node) {
                is StmtP.Assign<*> -> visitAssign((node.assign as AssignP<CstPayload>).lhs)
                is StmtP.Def<*, *> -> {
                    val def = node.def as DefP<CstPayload, *>
                    visitLvalue(def.name as Spanned<AssignIdentP<CstPayload, *>>)
                    for (param in def.params) {
                        val (name, typ, defVal) = splitParam(param.node as ParameterP<CstPayload>)
                        if (name != null) {
                            visitLvalue(name)
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
    data class Stmt(val stmt: Spanned<StmtP<CstPayload>>) : Visit()
    data class Expr(val expr: Spanned<ExprP<CstPayload>>) : Visit()
}

/**
 * Split a parameter into name, type, default value.
 * Port of Rust's ParameterP::split.
 */
@Suppress("UNCHECKED_CAST")
private fun splitParam(
    param: ParameterP<CstPayload>,
): Triple<Spanned<AssignIdentP<CstPayload, *>>?, Spanned<TypeExprP<CstPayload, *>>?, Spanned<ExprP<CstPayload>>?> {
    return when (param) {
        is ParameterP.Normal<*> -> Triple(
            param.name as Spanned<AssignIdentP<CstPayload, *>>,
            param.typ as Spanned<TypeExprP<CstPayload, *>>?,
            param.defaultVal as Spanned<ExprP<CstPayload>>?,
        )
        is ParameterP.Args<*> -> Triple(
            param.name as Spanned<AssignIdentP<CstPayload, *>>,
            param.typ as Spanned<TypeExprP<CstPayload, *>>?,
            null,
        )
        is ParameterP.KwArgs<*> -> Triple(
            param.name as Spanned<AssignIdentP<CstPayload, *>>,
            param.typ as Spanned<TypeExprP<CstPayload, *>>?,
            null,
        )
        is ParameterP.NoArgs<*>, is ParameterP.Slash<*> -> Triple(null, null, null)
        else -> Triple(null, null, null)
    }
}

/** Visit lvalues in an assign target (port of AssignTargetP::visit_lvalue). */
@Suppress("UNCHECKED_CAST")
private fun visitAssignLvalue(
    assign: Spanned<AssignTargetP<CstPayload>>,
    f: (Spanned<AssignIdentP<CstPayload, *>>) -> Unit,
) {
    when (val node = assign.node) {
        is AssignTargetP.Identifier<*, *> -> f(node.ident as Spanned<AssignIdentP<CstPayload, *>>)
        is AssignTargetP.Tuple<*> ->
            (node.elements as List<Spanned<AssignTargetP<CstPayload>>>).forEach { visitAssignLvalue(it, f) }
        else -> {}
    }
}

/** Visit child expressions of an ExprP (port of ExprP::visit_expr). */
@Suppress("UNCHECKED_CAST")
private fun visitExprChildren(
    expr: Spanned<ExprP<CstPayload>>,
    f: (Spanned<ExprP<CstPayload>>) -> Unit,
) {
    when (val node = expr.node) {
        is ExprP.Tuple<*> -> (node.elements as List<Spanned<ExprP<CstPayload>>>).forEach { f(it) }
        is ExprP.Dot<*> -> f(node.expr as Spanned<ExprP<CstPayload>>)
        is ExprP.Call<*> -> {
            f(node.expr as Spanned<ExprP<CstPayload>>)
            (node as ExprP.Call<CstPayload>).args.args.forEach { f(it.node.expr()) }
        }
        is ExprP.Index<*> -> { f(node.expr as Spanned<ExprP<CstPayload>>); f(node.index as Spanned<ExprP<CstPayload>>) }
        is ExprP.Index2<*> -> {
            f(node.expr as Spanned<ExprP<CstPayload>>)
            f(node.index0 as Spanned<ExprP<CstPayload>>)
            f(node.index1 as Spanned<ExprP<CstPayload>>)
        }
        is ExprP.Slice<*> -> {
            f(node.expr as Spanned<ExprP<CstPayload>>)
            (node.start as Spanned<ExprP<CstPayload>>?)?.let { f(it) }
            (node.stop as Spanned<ExprP<CstPayload>>?)?.let { f(it) }
            (node.step as Spanned<ExprP<CstPayload>>?)?.let { f(it) }
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
        is ExprP.Not<*> -> f(node.expr as Spanned<ExprP<CstPayload>>)
        is ExprP.Minus<*> -> f(node.expr as Spanned<ExprP<CstPayload>>)
        is ExprP.Plus<*> -> f(node.expr as Spanned<ExprP<CstPayload>>)
        is ExprP.BitNot<*> -> f(node.expr as Spanned<ExprP<CstPayload>>)
        is ExprP.Op<*> -> { f(node.lhs as Spanned<ExprP<CstPayload>>); f(node.rhs as Spanned<ExprP<CstPayload>>) }
        is ExprP.If<*> -> {
            f(node.cond as Spanned<ExprP<CstPayload>>)
            f(node.v1 as Spanned<ExprP<CstPayload>>)
            f(node.v2 as Spanned<ExprP<CstPayload>>)
        }
        is ExprP.ListExpr<*> -> (node.elements as List<Spanned<ExprP<CstPayload>>>).forEach { f(it) }
        is ExprP.Dict<*> ->
            (node.elements as List<Pair<Spanned<ExprP<CstPayload>>, Spanned<ExprP<CstPayload>>>>).forEach { (k, v) -> f(k); f(v) }
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
            f(dc.key); f(dc.value)
        }
        is ExprP.FString<*> ->
            (node.fstring as FStringP<CstPayload>).expressions.forEach { f(it as Spanned<ExprP<CstPayload>>) }
    }
}

/** Visit child statements/expressions of a StmtP (port of StmtP::visit_children). */
@Suppress("UNCHECKED_CAST")
private fun visitStmtChildrenImpl(stmt: Spanned<StmtP<CstPayload>>, f: (Visit) -> Unit) {
    when (val node = stmt.node) {
        is StmtP.Statements<*> -> (node.stmts as List<Spanned<StmtP<CstPayload>>>).forEach { f(Visit.Stmt(it)) }
        is StmtP.If<*> -> { f(Visit.Expr(node.cond as Spanned<ExprP<CstPayload>>)); f(Visit.Stmt(node.suite as Spanned<StmtP<CstPayload>>)) }
        is StmtP.IfElse<*> -> {
            f(Visit.Expr(node.cond as Spanned<ExprP<CstPayload>>))
            f(Visit.Stmt(node.suite1 as Spanned<StmtP<CstPayload>>)); f(Visit.Stmt(node.suite2 as Spanned<StmtP<CstPayload>>))
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
            f(Visit.Expr(fp.over)); f(Visit.Stmt(fp.body))
        }
        is StmtP.Return<*> -> { val r = node.expr as Spanned<ExprP<CstPayload>>?; if (r != null) f(Visit.Expr(r)) }
        is StmtP.Expression<*> -> f(Visit.Expr(node.expr as Spanned<ExprP<CstPayload>>))
        is StmtP.Assign<*> -> {
            val a = node.assign as AssignP<CstPayload>
            visitAssignTargetExprs(a.lhs) { f(Visit.Expr(it)) }
            a.ty?.let { f(Visit.Expr(it.node.expr)) }
            f(Visit.Expr(a.rhs))
        }
        is StmtP.AssignModify<*> -> {
            visitAssignTargetExprs(node.lhs as Spanned<AssignTargetP<CstPayload>>) { f(Visit.Expr(it)) }
            f(Visit.Expr(node.rhs as Spanned<ExprP<CstPayload>>))
        }
        is StmtP.Break<*>, is StmtP.Continue<*>, is StmtP.Pass<*>, is StmtP.Load<*, *> -> {}
    }
}

/** Visit expressions within an assign target (port of AssignTargetP::visit_expr). */
@Suppress("UNCHECKED_CAST")
private fun visitAssignTargetExprs(target: Spanned<AssignTargetP<CstPayload>>, f: (Spanned<ExprP<CstPayload>>) -> Unit) {
    when (val node = target.node) {
        is AssignTargetP.Tuple<*> ->
            (node.elements as List<Spanned<AssignTargetP<CstPayload>>>).forEach { visitAssignTargetExprs(it, f) }
        is AssignTargetP.Dot<*> -> f(node.expr as Spanned<ExprP<CstPayload>>)
        is AssignTargetP.Index<*> -> { f(node.expr as Spanned<ExprP<CstPayload>>); f(node.index as Spanned<ExprP<CstPayload>>) }
        is AssignTargetP.Identifier<*, *> -> {}
    }
}

/** Visit expressions within a for clause. */
@Suppress("UNCHECKED_CAST")
private fun visitForClauseExprs(
    forClause: ForClauseP<CstPayload>,
    f: (Spanned<ExprP<CstPayload>>) -> Unit,
) {
    visitAssignTargetExprs(forClause.varTarget, f)
    f(forClause.over)
}

/** Visit expressions within a clause. */
@Suppress("UNCHECKED_CAST")
private fun visitClauseExprs(clause: ClauseP<CstPayload>, f: (Spanned<ExprP<CstPayload>>) -> Unit) {
    when (clause) {
        is ClauseP.For<*> -> visitForClauseExprs(clause.forClause as ForClauseP<CstPayload>, f)
        is ClauseP.If<*> -> f(clause.cond as Spanned<ExprP<CstPayload>>)
    }
}

// fn t(program: &str, expected: &str)
private fun t(program: String, expected: String) {
    val module = MutableNames()
    testWithModule(program, expected, module)
}

// Expected test output (second parameter to `t` function) is:
// * list of bindings in format like `1:l=2` means binding id = 1, local slot 2
// * list of variables with references to binding ids

// #[test] fn basic()
internal fun basic() {
    t("x = 1; y = 2", "0:m=0 1:m=1 | x:0 y:1")
}

// #[test] fn module_reassignment()
internal fun moduleReassignment() {
    t("x = 1; x = 2", "0:m=0+ | x:0 x:0")
}

// #[test] fn reassignment_in_loop()
internal fun reassignmentInLoop() {
    t("for x in []: y = x", "0:m=0+ 1:m=1+ | x:0 y:1 x:0")
}

// #[test] fn def_capture()
internal fun defCapture() {
    t("x = 1\ndef f(): x", "0:m=0& 1:m=1 | x:0 f:1 x:0")
}

// #[test] fn def_shadow()
internal fun defShadow() {
    t("x = 1\ndef f(): x = 2", "0:m=0 1:m=1 2:l=0 | x:0 f:1 x:2")
}

// #[test] fn def_param_bindings()
internal fun defParamBindings() {
    t("def f(x): return x", "0:m=0 1:l=0 | f:0 x:1 x:1")
}

// #[test] fn nested_def_capture()
internal fun nestedDefCapture() {
    t(
        "def f():\n    x = 1\n    def g(): return x",
        "0:m=0 1:l=0& 2:l=1 | f:0 x:1 g:2 x:1",
    )
}

// #[test] fn existing_module_with_names()
internal fun existingModuleWithNames() {
    val frozenHeap = FrozenHeap.new()
    val module = MutableNames()
    module.addName(frozenHeap.allocStrIntern("x"))
    module.addName(frozenHeap.allocStrIntern("y"))
    testWithModule("x = y", "0:m=0+ 1:m=1 | x:0 y:1", module)
}
