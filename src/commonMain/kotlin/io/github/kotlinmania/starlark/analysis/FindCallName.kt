// port-lint: source src/analysis/findCallName.rs
package io.github.kotlinmania.starlark.analysis

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

import io.github.kotlinmania.starlark.codemap.Span
import io.github.kotlinmania.starlark.codemap.Spanned
import io.github.kotlinmania.starlark.syntax.AstModule
import io.github.kotlinmania.starlark.syntax.ast.AstLiteral
import io.github.kotlinmania.starlark.syntax.ast.AstNoPayload
import io.github.kotlinmania.starlark.syntax.ast.ArgumentP
import io.github.kotlinmania.starlark.syntax.ast.AssignTargetP
import io.github.kotlinmania.starlark.syntax.ast.ClauseP
import io.github.kotlinmania.starlark.syntax.ast.ExprP
import io.github.kotlinmania.starlark.syntax.ast.StmtP

/**
 * Find the location of a top level function call that has a kwarg "name", and a string value
 * matching [name].
 */
interface AstModuleFindCallName {
    /**
     * Find the location of a top level function call that has a kwarg "name", and a string value
     * matching [name].
     *
     * NOTE: If the AST is exposed in the future, this function may be removed and implemented
     *       by specific programs instead.
     */
    fun findFunctionCallWithName(name: String): Span?
}

/**
 * Find the location of a top level function call that has a kwarg "name", and a string value
 * matching [name].
 *
 * @see AstModuleFindCallName.findFunctionCallWithName
 */
fun AstModule.findFunctionCallWithName(name: String): Span? {
    var ret: Span? = null

    fun visitExpr(node: Spanned<ExprP<AstNoPayload>>) {
        if (ret != null) {
            return
        }

        when (val expr = node.node) {
            is ExprP.Call -> {
                val identifier = expr.expr
                val identNode = identifier.node
                if (identNode is ExprP.Identifier<AstNoPayload, *> || identNode is ExprP.Dot) {
                    val found = expr.args.args.firstNotNullOfOrNull { argument ->
                        val arg = argument.node
                        if (arg is ArgumentP.Named) {
                            val value = arg.expr
                            val valueNode = value.node
                            if (arg.name.node == "name" &&
                                valueNode is ExprP.Literal &&
                                valueNode.literal is AstLiteral.String &&
                                valueNode.literal.value.node == name
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

// --- Visitor helpers (internal for import by other analysis files) ---

/**
 * Visit immediate child expressions in an [ExprP] node.
 */
internal fun ExprP<AstNoPayload>.visitChildExprs(f: (Spanned<ExprP<AstNoPayload>>) -> Unit) {
    when (this) {
        is ExprP.Tuple -> elements.forEach(f)
        is ExprP.Dot -> f(expr)
        is ExprP.Call -> {
            f(expr)
            args.args.forEach { f(it.node.expr()) }
        }
        is ExprP.Index -> {
            f(expr)
            f(index)
        }
        is ExprP.Index2 -> {
            f(expr)
            f(index0)
            f(index1)
        }
        is ExprP.Slice -> {
            f(expr)
            start?.let(f)
            stop?.let(f)
            step?.let(f)
        }
        is ExprP.Identifier<AstNoPayload, *> -> { /* leaf */ }
        is ExprP.Lambda<AstNoPayload, *> -> {
            lambda.params.forEach { param ->
                val p = param.node
                when (p) {
                    is io.github.kotlinmania.starlark.syntax.ast.ParameterP.Normal -> {
                        p.typ?.node?.expr?.let(f)
                        p.defaultVal?.let(f)
                    }
                    is io.github.kotlinmania.starlark.syntax.ast.ParameterP.Args -> {
                        p.typ?.node?.expr?.let(f)
                    }
                    is io.github.kotlinmania.starlark.syntax.ast.ParameterP.KwArgs -> {
                        p.typ?.node?.expr?.let(f)
                    }
                    is io.github.kotlinmania.starlark.syntax.ast.ParameterP.NoArgs,
                    is io.github.kotlinmania.starlark.syntax.ast.ParameterP.Slash -> { /* no expr */ }
                }
            }
            f(lambda.body)
        }
        is ExprP.Literal -> { /* leaf */ }
        is ExprP.Not -> f(expr)
        is ExprP.Minus -> f(expr)
        is ExprP.Plus -> f(expr)
        is ExprP.BitNot -> f(expr)
        is ExprP.Op -> {
            f(lhs)
            f(rhs)
        }
        is ExprP.If -> {
            f(cond)
            f(v1)
            f(v2)
        }
        is ExprP.ListExpr -> elements.forEach(f)
        is ExprP.Dict -> elements.forEach { (k, v) ->
            f(k)
            f(v)
        }
        is ExprP.ListComprehension -> {
            visitForClauseExprs(forClause, f)
            clauses.forEach { visitClauseExprs(it, f) }
            f(expr)
        }
        is ExprP.DictComprehension -> {
            visitForClauseExprs(forClause, f)
            clauses.forEach { visitClauseExprs(it, f) }
            f(key)
            f(value)
        }
        is ExprP.FString -> {
            fstring.node.expressions.forEach(f)
        }
    }
}

internal fun visitForClauseExprs(forClause: io.github.kotlinmania.starlark.syntax.ast.ForClauseP<AstNoPayload>, f: (Spanned<ExprP<AstNoPayload>>) -> Unit) {
    visitAssignTargetExprs(forClause.varTarget.node, f)
    f(forClause.over)
}

internal fun visitClauseExprs(clause: ClauseP<AstNoPayload>, f: (Spanned<ExprP<AstNoPayload>>) -> Unit) {
    when (clause) {
        is ClauseP.For -> visitForClauseExprs(clause.forClause, f)
        is ClauseP.If -> f(clause.cond)
    }
}

internal fun visitAssignTargetExprs(target: AssignTargetP<AstNoPayload>, f: (Spanned<ExprP<AstNoPayload>>) -> Unit) {
    when (target) {
        is AssignTargetP.Tuple -> target.elements.forEach { visitAssignTargetExprs(it.node, f) }
        is AssignTargetP.Dot -> f(target.expr)
        is AssignTargetP.Index -> {
            f(target.expr)
            f(target.index)
        }
        is AssignTargetP.Identifier<AstNoPayload, *> -> { /* leaf */ }
    }
}

/**
 * Visit all expressions within a statement by recursing into child statements and
 * calling [f] on each expression found.
 */
internal fun Spanned<StmtP<AstNoPayload>>.visitExprs(f: (Spanned<ExprP<AstNoPayload>>) -> Unit) {
    when (val s = node) {
        is StmtP.Statements -> s.stmts.forEach { it.visitExprs(f) }
        is StmtP.Expression -> f(s.expr)
        is StmtP.Return -> s.expr?.let(f)
        is StmtP.Assign -> {
            visitAssignTargetExprs(s.assign.lhs.node, f)
            s.assign.ty?.let { f(it.node.expr) }
            f(s.assign.rhs)
        }
        is StmtP.AssignModify -> {
            visitAssignTargetExprs(s.lhs.node, f)
            f(s.rhs)
        }
        is StmtP.If -> {
            f(s.cond)
            s.suite.visitExprs(f)
        }
        is StmtP.IfElse -> {
            f(s.cond)
            s.suite1.visitExprs(f)
            s.suite2.visitExprs(f)
        }
        is StmtP.For -> {
            visitAssignTargetExprs(s.forStmt.varTarget.node, f)
            f(s.forStmt.over)
            s.forStmt.body.visitExprs(f)
        }
        is StmtP.Def<AstNoPayload, *> -> {
            s.def.params.forEach { param ->
                val p = param.node
                when (p) {
                    is io.github.kotlinmania.starlark.syntax.ast.ParameterP.Normal -> {
                        p.typ?.node?.expr?.let(f)
                        p.defaultVal?.let(f)
                    }
                    is io.github.kotlinmania.starlark.syntax.ast.ParameterP.Args -> {
                        p.typ?.node?.expr?.let(f)
                    }
                    is io.github.kotlinmania.starlark.syntax.ast.ParameterP.KwArgs -> {
                        p.typ?.node?.expr?.let(f)
                    }
                    is io.github.kotlinmania.starlark.syntax.ast.ParameterP.NoArgs,
                    is io.github.kotlinmania.starlark.syntax.ast.ParameterP.Slash -> { /* no expr */ }
                }
            }
            s.def.returnType?.let { f(it.node.expr) }
            s.def.body.visitExprs(f)
        }
        is StmtP.Load<AstNoPayload, *> -> { /* no expressions */ }
        is StmtP.Break,
        is StmtP.Continue,
        is StmtP.Pass -> { /* no expressions */ }
    }
}

/**
 * Visit immediate child statements within a statement node.
 */
internal fun Spanned<StmtP<AstNoPayload>>.visitStmtChildren(f: (Spanned<StmtP<AstNoPayload>>) -> Unit) {
    when (val s = node) {
        is StmtP.Statements -> s.stmts.forEach(f)
        is StmtP.If -> f(s.suite)
        is StmtP.IfElse -> {
            f(s.suite1)
            f(s.suite2)
        }
        is StmtP.For -> f(s.forStmt.body)
        is StmtP.Def<AstNoPayload, *> -> f(s.def.body)
        is StmtP.Expression,
        is StmtP.Return,
        is StmtP.Assign,
        is StmtP.AssignModify,
        is StmtP.Load<AstNoPayload, *>,
        is StmtP.Break,
        is StmtP.Continue,
        is StmtP.Pass -> { /* no child statements */ }
    }
}

// Tests in commonTest
