// port-lint: source src/analysis/find_call_name.rs
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

import io.github.kotlinmania.starlark.codemap.Span
import io.github.kotlinmania.starlark.syntax.AstModule
import io.github.kotlinmania.starlark.syntax.ast.ArgumentP
import io.github.kotlinmania.starlark.syntax.ast.AssignTargetP
import io.github.kotlinmania.starlark.syntax.ast.AstExpr
import io.github.kotlinmania.starlark.syntax.ast.AstLiteral
import io.github.kotlinmania.starlark.syntax.ast.AstPayload
import io.github.kotlinmania.starlark.syntax.ast.AstStmt
import io.github.kotlinmania.starlark.syntax.ast.ClauseP
import io.github.kotlinmania.starlark.syntax.ast.ExprP
import io.github.kotlinmania.starlark.syntax.ast.StmtP

/**
 * Find the location of a top level function call that has a kwarg `name`, and a string value
 * matching [name].
 */
interface AstModuleFindCallName {
    /**
     * Find the location of a top level function call that has a kwarg `name`, and a string value
     * matching [name].
     *
     * NOTE: If the AST is exposed in the future, this function may be removed and implemented
     *       by specific programs instead.
     */
    fun findFunctionCallWithName(name: String): Span?
}

/**
 * Find the location of a top level function call that has a kwarg `name`, and a string value
 * matching [name].
 *
 * @see AstModuleFindCallName.findFunctionCallWithName
 */
fun AstModule.findFunctionCallWithName(name: String): Span? {
    var ret: Span? = null

    fun visitExpr(node: AstExpr) {
        if (ret != null) {
            return
        }

        when (val expr = node.node) {
            is ExprP.Call<*> -> {
                val identifier = expr.expr
                if (identifier.node is ExprP.Identifier<*, *> || identifier.node is ExprP.Dot<*>) {
                    val found =
                        expr.args.args.firstNotNullOfOrNull { argument ->
                            val arg = argument.node
                            if (arg is ArgumentP.Named<*>) {
                                val value = arg.expr
                                if (arg.name.node == "name" &&
                                    value.node is ExprP.Literal<*> &&
                                    value.node.literal is AstLiteral.String &&
                                    value.node.literal.value.node == name
                                ) {
                                    identifier.span
                                } else {
                                    null
                                }
                            } else {
                                null
                            }
                        }
                    if (found != null) {
                        ret = found
                    }
                }
            }
            else -> node.node.visitChildExprs(::visitExpr)
        }
    }

    statement.visitExprs(::visitExpr)
    return ret
}

// --- Visitor helpers (internal for use by other analysis files) ---

/**
 * Visit immediate child expressions in an [ExprP] node.
 */
internal fun <P : AstPayload> ExprP<P>.visitChildExprs(f: (AstExpr) -> Unit) {
    when (this) {
        is ExprP.Tuple<*> -> elements.forEach { f(it as AstExpr) }
        is ExprP.Dot<*> -> f(expr as AstExpr)
        is ExprP.Call<*> -> {
            f(expr as AstExpr)
            args.args.forEach { f(it.node.expr() as AstExpr) }
        }
        is ExprP.Index<*> -> {
            f(expr as AstExpr)
            f(index as AstExpr)
        }
        is ExprP.Index2<*> -> {
            f(expr as AstExpr)
            f(index0 as AstExpr)
            f(index1 as AstExpr)
        }
        is ExprP.Slice<*> -> {
            f(expr as AstExpr)
            (start as AstExpr?)?.let(f)
            (stop as AstExpr?)?.let(f)
            (step as AstExpr?)?.let(f)
        }
        is ExprP.Identifier<*, *> -> { /* leaf */ }
        is ExprP.Lambda<*, *> -> {
            lambda.params.forEach { param ->
                when (val p = param.node) {
                    is io.github.kotlinmania.starlark.syntax.ast.ParameterP.Normal<*> -> {
                        p.typ
                            ?.node
                            ?.expr
                            ?.let { f(it as AstExpr) }
                        (p.defaultVal as AstExpr?)?.let(f)
                    }
                    is io.github.kotlinmania.starlark.syntax.ast.ParameterP.Args<*> -> {
                        p.typ
                            ?.node
                            ?.expr
                            ?.let { f(it as AstExpr) }
                    }
                    is io.github.kotlinmania.starlark.syntax.ast.ParameterP.KwArgs<*> -> {
                        p.typ
                            ?.node
                            ?.expr
                            ?.let { f(it as AstExpr) }
                    }
                    is io.github.kotlinmania.starlark.syntax.ast.ParameterP.NoArgs<*>,
                    is io.github.kotlinmania.starlark.syntax.ast.ParameterP.Slash<*>,
                    -> { /* no expr */ }
                }
            }
            f(lambda.body as AstExpr)
        }
        is ExprP.Literal<*> -> { /* leaf */ }
        is ExprP.Not<*> -> f(expr as AstExpr)
        is ExprP.Minus<*> -> f(expr as AstExpr)
        is ExprP.Plus<*> -> f(expr as AstExpr)
        is ExprP.BitNot<*> -> f(expr as AstExpr)
        is ExprP.Op<*> -> {
            f(lhs as AstExpr)
            f(rhs as AstExpr)
        }
        is ExprP.If<*> -> {
            f(cond as AstExpr)
            f(v1 as AstExpr)
            f(v2 as AstExpr)
        }
        is ExprP.ListExpr<*> -> elements.forEach { f(it as AstExpr) }
        is ExprP.Dict<*> ->
            elements.forEach { (k, v) ->
                f(k as AstExpr)
                f(v as AstExpr)
            }
        is ExprP.ListComprehension<*> -> {
            visitForClauseExprs(forClause, f)
            clauses.forEach { visitClauseExprs(it, f) }
            f(expr as AstExpr)
        }
        is ExprP.DictComprehension<*> -> {
            visitForClauseExprs(forClause, f)
            clauses.forEach { visitClauseExprs(it, f) }
            f(key as AstExpr)
            f(value as AstExpr)
        }
        is ExprP.FString<*> -> {
            fstring.node.expressions.forEach { f(it as AstExpr) }
        }
    }
}

internal fun <P : AstPayload> visitForClauseExprs(forClause: io.github.kotlinmania.starlark.syntax.ast.ForClauseP<P>, f: (AstExpr) -> Unit) {
    visitAssignTargetExprs(forClause.varTarget.node, f)
    f(forClause.over as AstExpr)
}

internal fun <P : AstPayload> visitClauseExprs(clause: ClauseP<P>, f: (AstExpr) -> Unit) {
    when (clause) {
        is ClauseP.For<*> -> visitForClauseExprs(clause.forClause, f)
        is ClauseP.If<*> -> f(clause.cond as AstExpr)
    }
}

internal fun <P : AstPayload> visitAssignTargetExprs(target: AssignTargetP<P>, f: (AstExpr) -> Unit) {
    when (target) {
        is AssignTargetP.Tuple<*> -> target.elements.forEach { visitAssignTargetExprs(it.node, f) }
        is AssignTargetP.Dot<*> -> f(target.expr as AstExpr)
        is AssignTargetP.Index<*> -> {
            f(target.expr as AstExpr)
            f(target.index as AstExpr)
        }
        is AssignTargetP.Identifier<*, *> -> { /* leaf */ }
    }
}

/**
 * Visit all expressions within an [AstStmt] by recursing into child statements and
 * calling [f] on each expression found.
 */
internal fun AstStmt.visitExprs(f: (AstExpr) -> Unit) {
    when (val s = node) {
        is StmtP.Statements<*> -> s.stmts.forEach { (it as AstStmt).visitExprs(f) }
        is StmtP.Expression<*> -> f(s.expr as AstExpr)
        is StmtP.Return<*> -> (s.expr as AstExpr?)?.let(f)
        is StmtP.Assign<*> -> {
            visitAssignTargetExprs(s.assign.lhs.node, f)
            s.assign.ty?.let { f(it.node.expr as AstExpr) }
            f(s.assign.rhs as AstExpr)
        }
        is StmtP.AssignModify<*> -> {
            visitAssignTargetExprs(s.lhs.node, f)
            f(s.rhs as AstExpr)
        }
        is StmtP.If<*> -> {
            f(s.cond as AstExpr)
            (s.suite as AstStmt).visitExprs(f)
        }
        is StmtP.IfElse<*> -> {
            f(s.cond as AstExpr)
            (s.suite1 as AstStmt).visitExprs(f)
            (s.suite2 as AstStmt).visitExprs(f)
        }
        is StmtP.For<*> -> {
            visitAssignTargetExprs(s.forStmt.varTarget.node, f)
            f(s.forStmt.over as AstExpr)
            (s.forStmt.body as AstStmt).visitExprs(f)
        }
        is StmtP.Def<*, *> -> {
            s.def.params.forEach { param ->
                when (val p = param.node) {
                    is io.github.kotlinmania.starlark.syntax.ast.ParameterP.Normal<*> -> {
                        p.typ
                            ?.node
                            ?.expr
                            ?.let { f(it as AstExpr) }
                        (p.defaultVal as AstExpr?)?.let(f)
                    }
                    is io.github.kotlinmania.starlark.syntax.ast.ParameterP.Args<*> -> {
                        p.typ
                            ?.node
                            ?.expr
                            ?.let { f(it as AstExpr) }
                    }
                    is io.github.kotlinmania.starlark.syntax.ast.ParameterP.KwArgs<*> -> {
                        p.typ
                            ?.node
                            ?.expr
                            ?.let { f(it as AstExpr) }
                    }
                    is io.github.kotlinmania.starlark.syntax.ast.ParameterP.NoArgs<*>,
                    is io.github.kotlinmania.starlark.syntax.ast.ParameterP.Slash<*>,
                    -> { /* no expr */ }
                }
            }
            s.def.returnType?.let { f(it.node.expr as AstExpr) }
            (s.def.body as AstStmt).visitExprs(f)
        }
        is StmtP.Load<*, *> -> { /* no expressions */ }
        is StmtP.Break<*>,
        is StmtP.Continue<*>,
        is StmtP.Pass<*>,
        -> { /* no expressions */ }
    }
}

/**
 * Visit immediate child statements in an [AstStmt] node.
 */
internal fun AstStmt.visitStmtChildren(f: (AstStmt) -> Unit) {
    when (val s = node) {
        is StmtP.Statements<*> -> s.stmts.forEach { f(it as AstStmt) }
        is StmtP.If<*> -> f(s.suite as AstStmt)
        is StmtP.IfElse<*> -> {
            f(s.suite1 as AstStmt)
            f(s.suite2 as AstStmt)
        }
        is StmtP.For<*> -> f(s.forStmt.body as AstStmt)
        is StmtP.Def<*, *> -> f(s.def.body as AstStmt)
        is StmtP.Expression<*>,
        is StmtP.Return<*>,
        is StmtP.Assign<*>,
        is StmtP.AssignModify<*>,
        is StmtP.Load<*, *>,
        is StmtP.Break<*>,
        is StmtP.Continue<*>,
        is StmtP.Pass<*>,
        -> { /* no child statements */ }
    }
}

// Tests in commonTest
