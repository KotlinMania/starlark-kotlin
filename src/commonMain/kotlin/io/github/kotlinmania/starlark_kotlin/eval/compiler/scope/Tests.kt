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
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.scope_resolver_globals.ScopeResolverGlobals
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenRef
import io.github.kotlinmania.starlark_kotlin.syntax.ast.Slot
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ResolvedIdent
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ExprP
import io.github.kotlinmania.starlark_kotlin.eval.compiler.ModuleScopes
import io.github.kotlinmania.starlark_kotlin.eval.compiler.Captured
import io.github.kotlinmania.starlark_kotlin.eval.compiler.AssignCount
import io.github.kotlinmania.starlark_kotlin.environment.MutableNames
import io.github.kotlinmania.starlark_kotlin.analysis.unused_loads.StmtP
import io.github.kotlinmania.starlark_kotlin.analysis.ForP
import io.github.kotlinmania.starlark_kotlin.analysis.DefP
import io.github.kotlinmania.starlark_kotlin.values.layout.value
import io.github.kotlinmania.starlark_kotlin.syntax.payload_and_span.Payload
import io.github.kotlinmania.starlark_kotlin.syntax.dialect.Dialect
import io.github.kotlinmania.starlark_kotlin.syntax.ast.Expr
import io.github.kotlinmania.starlark_kotlin.assert.parse
import io.github.kotlinmania.starlark_kotlin.analysis.For
import io.github.kotlinmania.starlark_kotlin.analysis.Def
import io.github.kotlinmania.starlark_kotlin.analysis.Assign
import io.github.kotlinmania.starlark_kotlin.values.types.allocAny
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.str_.allocStrIntern
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.Slot
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.ResolvedIdent
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.For
import io.github.kotlinmania.starlark_kotlin.eval.compiler.compr.variable
import io.github.kotlinmania.starlark_kotlin.analysis.visitLvalue
import io.github.kotlinmania.starlark_kotlin.analysis.visitExpr
import io.github.kotlinmania.starlark_kotlin.analysis.unused_loads.bindingId
import io.github.kotlinmania.starlark_kotlin.analysis.node
import io.github.kotlinmania.starlark_kotlin.analysis.lhs
import io.github.kotlinmania.starlark_kotlin.analysis.ident
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.ForP
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.DefP
import io.github.kotlinmania.starlark_kotlin.docs.params
import io.github.kotlinmania.starlark_kotlin.docs.name
import io.github.kotlinmania.starlark_kotlin.syntax.ast.Stmt
import io.github.kotlinmania.starlark_kotlin.syntax.AstModule
import io.github.kotlinmania.starlark_kotlin.codemap

// fn test_with_module(program: &str, expected: &str, module: &MutableNames)
private fun testWithModule(program: String, expected: String, module: MutableNames) {
    val ast = AstModule.parse("t.star", program, Dialect.AllOptionsInternal)
    val frozenHeap = FrozenHeap.new()
    val codemap = frozenHeap.allocAny(ast.codemap())
    val scopes = ModuleScopes.checkModuleErr(
        module,
        frozenHeap,
        mapOf(),
        ast.intoParts().second,
        ScopeResolverGlobals(
            globals = FrozenRef.new(Globals.empty()),
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
            is Slot.Module -> "m=${s.value}"
            is Slot.Local -> "l=${s.value}"
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
    class Visitor(val r: StringBuilder) {
        fun visitExpr(expr: CstExpr) {
            val node = expr.node
            if (node is ExprP.Identifier) {
                val ident = node.ident
                val resolved = when (val payload = ident.node.payload!!) {
                    is ResolvedIdent.Slot -> payload.bindingId.value.toString()
                    is ResolvedIdent.Global -> "G"
                }
                r.append(" ${ident.node.ident}:$resolved")
            }
            expr.visitExpr { e -> visitExpr(e) }
        }

        fun visitExprs(exprs: Iterable<CstExpr>) {
            for (expr in exprs) {
                visitExpr(expr)
            }
        }

        fun visitLvalue(ident: CstAssignIdent) {
            r.append(" ${ident.ident}:${ident.Payload!!.value}")
        }

        fun visitStmtChildren(stmt: CstStmt) {
            stmt.visitChildren { visit ->
                when (visit) {
                    is Visit.Stmt -> visitStmt(visit.Stmt)
                    is Visit.Expr -> visitExpr(visit.Expr)
                }
            }
        }

        fun visitAssign(assign: CstAssignTarget) {
            assign.visitLvalue { ident -> visitLvalue(ident) }
        }

        fun visitStmt(stmt: CstStmt) {
            when (val node = stmt.node) {
                is StmtP.Assign -> visitAssign(node.lhs)
                is StmtP.Def -> {
                    val def = node as DefP
                    visitLvalue(def.name)
                    for (param in def.params) {
                        val (name, defVal, typ) = param.split()
                        if (name != null) {
                            visitLvalue(name)
                        }
                        if (defVal != null) {
                            visitExprs(listOf(defVal.node.expr))
                        }
                        if (typ != null) {
                            visitExprs(listOf(typ))
                        }
                    }
                }
                is StmtP.For -> {
                    val forP = node as ForP
                    visitAssign(forP.variable)
                }
                else -> {}
            }
            visitStmtChildren(stmt)
        }
    }

    Visitor(r).visitStmt(cst)

    check(expected == r.toString()) { "Expected: $expected\nActual: $r" }
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
