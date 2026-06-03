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
import io.github.kotlinmania.starlark.syntax.ast.AstNoPayload
import io.github.kotlinmania.starlark.syntax.ast.AstStmt
import io.github.kotlinmania.starlark.syntax.ast.ClauseP
import io.github.kotlinmania.starlark.syntax.ast.ExprP
import io.github.kotlinmania.starlark.syntax.ast.ForClauseP
import io.github.kotlinmania.starlark.syntax.ast.ParameterP
import io.github.kotlinmania.starlark.syntax.ast.StmtP

/**
 * Find the location of a top level function call that has a kwarg `name`, and a string value
 * matching [name].
 */
internal interface AstModuleFindCallName {
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
internal fun AstModule.findFunctionCallWithName(name: String): Span? {
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
                                    value.node.literal is AstLiteral.StringLit &&
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
internal fun ExprP<AstNoPayload>.visitChildExprs(f: (AstExpr) -> Unit) {
    when (this) {
        is ExprP.Tuple<AstNoPayload> -> elements.forEach(f)
        is ExprP.Dot<AstNoPayload> -> f(expr)
        is ExprP.Call<AstNoPayload> -> {
            f(expr)
            args.args.forEach { f(it.node.expr()) }
        }
        is ExprP.Index<AstNoPayload> -> {
            f(expr)
            f(index)
        }
        is ExprP.Index2<AstNoPayload> -> {
            f(expr)
            f(index0)
            f(index1)
        }
        is ExprP.Slice<AstNoPayload> -> {
            f(expr)
            start?.let(f)
            stop?.let(f)
            step?.let(f)
        }
        is ExprP.Identifier<AstNoPayload, *> -> { /* leaf */ }
        is ExprP.Lambda<AstNoPayload, *> -> {
            lambda.params.forEach { param ->
                when (val p = param.node) {
                    is ParameterP.Normal<AstNoPayload> -> {
                        p.typ
                            ?.node
                            ?.expr
                            ?.let(f)
                        p.defaultVal?.let(f)
                    }
                    is ParameterP.Args<AstNoPayload> -> {
                        p.typ
                            ?.node
                            ?.expr
                            ?.let(f)
                    }
                    is ParameterP.KwArgs<AstNoPayload> -> {
                        p.typ
                            ?.node
                            ?.expr
                            ?.let(f)
                    }
                    is ParameterP.NoArgs<AstNoPayload>,
                    is ParameterP.Slash<AstNoPayload>,
                    -> { /* no expr */ }
                }
            }
            f(lambda.body)
        }
        is ExprP.Literal<AstNoPayload> -> { /* leaf */ }
        is ExprP.Not<AstNoPayload> -> f(expr)
        is ExprP.Minus<AstNoPayload> -> f(expr)
        is ExprP.Plus<AstNoPayload> -> f(expr)
        is ExprP.BitNot<AstNoPayload> -> f(expr)
        is ExprP.Op<AstNoPayload> -> {
            f(lhs)
            f(rhs)
        }
        is ExprP.If<AstNoPayload> -> {
            f(cond)
            f(v1)
            f(v2)
        }
        is ExprP.ListExpr<AstNoPayload> -> elements.forEach(f)
        is ExprP.Dict<AstNoPayload> ->
            elements.forEach { (k, v) ->
                f(k)
                f(v)
            }
        is ExprP.ListComprehension<AstNoPayload> -> {
            visitForClauseExprs(forClause, f)
            clauses.forEach { visitClauseExprs(it, f) }
            f(expr)
        }
        is ExprP.DictComprehension<AstNoPayload> -> {
            visitForClauseExprs(forClause, f)
            clauses.forEach { visitClauseExprs(it, f) }
            f(key)
            f(value)
        }
        is ExprP.FString<AstNoPayload> -> {
            fstring.node.expressions.forEach(f)
        }
    }
}

internal fun visitForClauseExprs(forClause: ForClauseP<AstNoPayload>, f: (AstExpr) -> Unit) {
    visitAssignTargetExprs(forClause.varTarget.node, f)
    f(forClause.over)
}

internal fun visitClauseExprs(clause: ClauseP<AstNoPayload>, f: (AstExpr) -> Unit) {
    when (clause) {
        is ClauseP.For<AstNoPayload> -> visitForClauseExprs(clause.forClause, f)
        is ClauseP.If<AstNoPayload> -> f(clause.cond)
    }
}

internal fun visitAssignTargetExprs(target: AssignTargetP<AstNoPayload>, f: (AstExpr) -> Unit) {
    when (target) {
        is AssignTargetP.Tuple<AstNoPayload> -> target.elements.forEach { visitAssignTargetExprs(it.node, f) }
        is AssignTargetP.Dot<AstNoPayload> -> f(target.expr)
        is AssignTargetP.Index<AstNoPayload> -> {
            f(target.expr)
            f(target.index)
        }
        is AssignTargetP.Identifier<AstNoPayload, *> -> { /* leaf */ }
    }
}

/**
 * Visit all expressions within an [AstStmt] by recursing into child statements and
 * calling [f] on each expression found.
 */
internal fun AstStmt.visitExprs(f: (AstExpr) -> Unit) {
    when (val s = node) {
        is StmtP.Statements<AstNoPayload> -> s.stmts.forEach { it.visitExprs(f) }
        is StmtP.Expression<AstNoPayload> -> f(s.expr)
        is StmtP.Return<AstNoPayload> -> s.expr?.let(f)
        is StmtP.Assign<AstNoPayload> -> {
            visitAssignTargetExprs(s.assign.lhs.node, f)
            s.assign.ty?.let { f(it.node.expr) }
            f(s.assign.rhs)
        }
        is StmtP.AssignModify<AstNoPayload> -> {
            visitAssignTargetExprs(s.lhs.node, f)
            f(s.rhs)
        }
        is StmtP.If<AstNoPayload> -> {
            f(s.cond)
            s.suite.visitExprs(f)
        }
        is StmtP.IfElse<AstNoPayload> -> {
            f(s.cond)
            s.suite1.visitExprs(f)
            s.suite2.visitExprs(f)
        }
        is StmtP.For<AstNoPayload> -> {
            visitAssignTargetExprs(s.forStmt.varTarget.node, f)
            f(s.forStmt.over)
            s.forStmt.body.visitExprs(f)
        }
        is StmtP.Def<AstNoPayload, *> -> {
            s.def.params.forEach { param ->
                when (val p = param.node) {
                    is ParameterP.Normal<AstNoPayload> -> {
                        p.typ
                            ?.node
                            ?.expr
                            ?.let(f)
                        p.defaultVal?.let(f)
                    }
                    is ParameterP.Args<AstNoPayload> -> {
                        p.typ
                            ?.node
                            ?.expr
                            ?.let(f)
                    }
                    is ParameterP.KwArgs<AstNoPayload> -> {
                        p.typ
                            ?.node
                            ?.expr
                            ?.let(f)
                    }
                    is ParameterP.NoArgs<AstNoPayload>,
                    is ParameterP.Slash<AstNoPayload>,
                    -> { /* no expr */ }
                }
            }
            s.def.returnType?.let { f(it.node.expr) }
            s.def.body.visitExprs(f)
        }
        is StmtP.Load<AstNoPayload, *> -> { /* no expressions */ }
        is StmtP.Break<AstNoPayload>,
        is StmtP.Continue<AstNoPayload>,
        is StmtP.Pass<AstNoPayload>,
        -> { /* no expressions */ }
    }
}

/**
 * Visit immediate child statements in an [AstStmt] node.
 */
internal fun AstStmt.visitStmtChildren(f: (AstStmt) -> Unit) {
    when (val s = node) {
        is StmtP.Statements<AstNoPayload> -> s.stmts.forEach(f)
        is StmtP.If<AstNoPayload> -> f(s.suite)
        is StmtP.IfElse<AstNoPayload> -> {
            f(s.suite1)
            f(s.suite2)
        }
        is StmtP.For<AstNoPayload> -> f(s.forStmt.body)
        is StmtP.Def<AstNoPayload, *> -> f(s.def.body)
        is StmtP.Expression<AstNoPayload>,
        is StmtP.Return<AstNoPayload>,
        is StmtP.Assign<AstNoPayload>,
        is StmtP.AssignModify<AstNoPayload>,
        is StmtP.Load<AstNoPayload, *>,
        is StmtP.Break<AstNoPayload>,
        is StmtP.Continue<AstNoPayload>,
        is StmtP.Pass<AstNoPayload>,
        -> { /* no child statements */ }
    }
}

// Tests in commonTest
