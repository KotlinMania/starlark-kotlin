// port-lint: source src/analysis/find_call_name.rs
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

import io.github.kotlinmania.starlark_kotlin.codemap.Span
import io.github.kotlinmania.starlark_kotlin.codemap.Spanned
import io.github.kotlinmania.starlark_kotlin.syntax.AstModule
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstLiteral
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstNoPayload
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ArgumentP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AssignTargetP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ClauseP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstPayload
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ExprP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.StmtP

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

    fun visitExpr(node: Spanned<ExprP<AstNoPayload>>) {
        if (ret != null) {
            return
        }

        when (val expr = node.node) {
            is ExprP.Call<*> -> {
                val identifier = expr.expr
                if (identifier.node is ExprP.Identifier<*, *> || identifier.node is ExprP.Dot<*>) {
                    val found = expr.args.args.firstNotNullOfOrNull { argument ->
                        val arg = argument.node
                        if (arg is ArgumentP.Named<*>) {
                            val value = arg.expr
                            if (arg.name.node == "name" &&
                                value.node is ExprP.Literal<*> &&
                                (value.node as ExprP.Literal<*>).literal is AstLiteral.String &&
                                ((value.node as ExprP.Literal<*>).literal as AstLiteral.String).value.node == name
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
 * Mirrors `ExprP::visit_expr` from Rust's `uniplate.rs`.
 */
internal fun <P : AstPayload> ExprP<P>.visitChildExprs(f: (Spanned<ExprP<AstNoPayload>>) -> Unit) {
    @Suppress("UNCHECKED_CAST")
    when (this) {
        is ExprP.Tuple<*> -> elements.forEach { f(it as Spanned<ExprP<AstNoPayload>>) }
        is ExprP.Dot<*> -> f(expr as Spanned<ExprP<AstNoPayload>>)
        is ExprP.Call<*> -> {
            f(expr as Spanned<ExprP<AstNoPayload>>)
            args.args.forEach { f(it.node.expr() as Spanned<ExprP<AstNoPayload>>) }
        }
        is ExprP.Index<*> -> {
            f(expr as Spanned<ExprP<AstNoPayload>>)
            f(index as Spanned<ExprP<AstNoPayload>>)
        }
        is ExprP.Index2<*> -> {
            f(expr as Spanned<ExprP<AstNoPayload>>)
            f(index0 as Spanned<ExprP<AstNoPayload>>)
            f(index1 as Spanned<ExprP<AstNoPayload>>)
        }
        is ExprP.Slice<*> -> {
            f(expr as Spanned<ExprP<AstNoPayload>>)
            (start as Spanned<ExprP<AstNoPayload>>?)?.let(f)
            (stop as Spanned<ExprP<AstNoPayload>>?)?.let(f)
            (step as Spanned<ExprP<AstNoPayload>>?)?.let(f)
        }
        is ExprP.Identifier<*, *> -> { /* leaf */ }
        is ExprP.Lambda<*, *> -> {
            lambda.params.forEach { param ->
                val p = param.node
                @Suppress("UNCHECKED_CAST")
                when (p) {
                    is io.github.kotlinmania.starlark_kotlin.syntax.ast.ParameterP.Normal<*> -> {
                        p.typ?.node?.expr?.let { f(it as Spanned<ExprP<AstNoPayload>>) }
                        (p.defaultVal as Spanned<ExprP<AstNoPayload>>?)?.let(f)
                    }
                    is io.github.kotlinmania.starlark_kotlin.syntax.ast.ParameterP.Args<*> -> {
                        p.typ?.node?.expr?.let { f(it as Spanned<ExprP<AstNoPayload>>) }
                    }
                    is io.github.kotlinmania.starlark_kotlin.syntax.ast.ParameterP.KwArgs<*> -> {
                        p.typ?.node?.expr?.let { f(it as Spanned<ExprP<AstNoPayload>>) }
                    }
                    is io.github.kotlinmania.starlark_kotlin.syntax.ast.ParameterP.NoArgs<*>,
                    is io.github.kotlinmania.starlark_kotlin.syntax.ast.ParameterP.Slash<*> -> { /* no expr */ }
                }
            }
            f(lambda.body as Spanned<ExprP<AstNoPayload>>)
        }
        is ExprP.Literal<*> -> { /* leaf */ }
        is ExprP.Not<*> -> f(expr as Spanned<ExprP<AstNoPayload>>)
        is ExprP.Minus<*> -> f(expr as Spanned<ExprP<AstNoPayload>>)
        is ExprP.Plus<*> -> f(expr as Spanned<ExprP<AstNoPayload>>)
        is ExprP.BitNot<*> -> f(expr as Spanned<ExprP<AstNoPayload>>)
        is ExprP.Op<*> -> {
            f(lhs as Spanned<ExprP<AstNoPayload>>)
            f(rhs as Spanned<ExprP<AstNoPayload>>)
        }
        is ExprP.If<*> -> {
            f(cond as Spanned<ExprP<AstNoPayload>>)
            f(v1 as Spanned<ExprP<AstNoPayload>>)
            f(v2 as Spanned<ExprP<AstNoPayload>>)
        }
        is ExprP.ListExpr<*> -> elements.forEach { f(it as Spanned<ExprP<AstNoPayload>>) }
        is ExprP.Dict<*> -> elements.forEach { (k, v) ->
            f(k as Spanned<ExprP<AstNoPayload>>)
            f(v as Spanned<ExprP<AstNoPayload>>)
        }
        is ExprP.ListComprehension<*> -> {
            visitForClauseExprs(forClause, f)
            clauses.forEach { visitClauseExprs(it, f) }
            f(expr as Spanned<ExprP<AstNoPayload>>)
        }
        is ExprP.DictComprehension<*> -> {
            visitForClauseExprs(forClause, f)
            clauses.forEach { visitClauseExprs(it, f) }
            f(key as Spanned<ExprP<AstNoPayload>>)
            f(value as Spanned<ExprP<AstNoPayload>>)
        }
        is ExprP.FString<*> -> {
            fstring.node.expressions.forEach { f(it as Spanned<ExprP<AstNoPayload>>) }
        }
    }
}

@Suppress("UNCHECKED_CAST")
internal fun <P : AstPayload> visitForClauseExprs(forClause: io.github.kotlinmania.starlark_kotlin.syntax.ast.ForClauseP<P>, f: (Spanned<ExprP<AstNoPayload>>) -> Unit) {
    visitAssignTargetExprs(forClause.varTarget.node, f)
    f(forClause.over as Spanned<ExprP<AstNoPayload>>)
}

@Suppress("UNCHECKED_CAST")
internal fun <P : AstPayload> visitClauseExprs(clause: ClauseP<P>, f: (Spanned<ExprP<AstNoPayload>>) -> Unit) {
    when (clause) {
        is ClauseP.For<*> -> visitForClauseExprs(clause.forClause, f)
        is ClauseP.If<*> -> f(clause.cond as Spanned<ExprP<AstNoPayload>>)
    }
}

@Suppress("UNCHECKED_CAST")
internal fun <P : AstPayload> visitAssignTargetExprs(target: AssignTargetP<P>, f: (Spanned<ExprP<AstNoPayload>>) -> Unit) {
    when (target) {
        is AssignTargetP.Tuple<*> -> target.elements.forEach { visitAssignTargetExprs(it.node, f) }
        is AssignTargetP.Dot<*> -> f(target.expr as Spanned<ExprP<AstNoPayload>>)
        is AssignTargetP.Index<*> -> {
            f(target.expr as Spanned<ExprP<AstNoPayload>>)
            f(target.index as Spanned<ExprP<AstNoPayload>>)
        }
        is AssignTargetP.Identifier<*, *> -> { /* leaf */ }
    }
}

/**
 * Visit all expressions within an [Spanned<StmtP<AstNoPayload>>] by recursing into child statements and
 * calling [f] on each expression found.
 * Mirrors `StmtP::visit_expr` from Rust's `uniplate.rs`.
 */
@Suppress("UNCHECKED_CAST")
internal fun Spanned<StmtP<AstNoPayload>>.visitExprs(f: (Spanned<ExprP<AstNoPayload>>) -> Unit) {
    when (val s = node) {
        is StmtP.Statements<*> -> s.stmts.forEach { (it as Spanned<StmtP<AstNoPayload>>).visitExprs(f) }
        is StmtP.Expression<*> -> f(s.expr as Spanned<ExprP<AstNoPayload>>)
        is StmtP.Return<*> -> (s.expr as Spanned<ExprP<AstNoPayload>>?)?.let(f)
        is StmtP.Assign<*> -> {
            visitAssignTargetExprs(s.assign.lhs.node, f)
            s.assign.ty?.let { f(it.node.expr as Spanned<ExprP<AstNoPayload>>) }
            f(s.assign.rhs as Spanned<ExprP<AstNoPayload>>)
        }
        is StmtP.AssignModify<*> -> {
            visitAssignTargetExprs(s.lhs.node, f)
            f(s.rhs as Spanned<ExprP<AstNoPayload>>)
        }
        is StmtP.If<*> -> {
            f(s.cond as Spanned<ExprP<AstNoPayload>>)
            (s.suite as Spanned<StmtP<AstNoPayload>>).visitExprs(f)
        }
        is StmtP.IfElse<*> -> {
            f(s.cond as Spanned<ExprP<AstNoPayload>>)
            (s.suite1 as Spanned<StmtP<AstNoPayload>>).visitExprs(f)
            (s.suite2 as Spanned<StmtP<AstNoPayload>>).visitExprs(f)
        }
        is StmtP.For<*> -> {
            visitAssignTargetExprs(s.forStmt.varTarget.node, f)
            f(s.forStmt.over as Spanned<ExprP<AstNoPayload>>)
            (s.forStmt.body as Spanned<StmtP<AstNoPayload>>).visitExprs(f)
        }
        is StmtP.Def<*, *> -> {
            s.def.params.forEach { param ->
                val p = param.node
                when (p) {
                    is io.github.kotlinmania.starlark_kotlin.syntax.ast.ParameterP.Normal<*> -> {
                        p.typ?.node?.expr?.let { f(it as Spanned<ExprP<AstNoPayload>>) }
                        (p.defaultVal as Spanned<ExprP<AstNoPayload>>?)?.let(f)
                    }
                    is io.github.kotlinmania.starlark_kotlin.syntax.ast.ParameterP.Args<*> -> {
                        p.typ?.node?.expr?.let { f(it as Spanned<ExprP<AstNoPayload>>) }
                    }
                    is io.github.kotlinmania.starlark_kotlin.syntax.ast.ParameterP.KwArgs<*> -> {
                        p.typ?.node?.expr?.let { f(it as Spanned<ExprP<AstNoPayload>>) }
                    }
                    is io.github.kotlinmania.starlark_kotlin.syntax.ast.ParameterP.NoArgs<*>,
                    is io.github.kotlinmania.starlark_kotlin.syntax.ast.ParameterP.Slash<*> -> { /* no expr */ }
                }
            }
            s.def.returnType?.let { f(it.node.expr as Spanned<ExprP<AstNoPayload>>) }
            (s.def.body as Spanned<StmtP<AstNoPayload>>).visitExprs(f)
        }
        is StmtP.Load<*, *> -> { /* no expressions */ }
        is StmtP.Break<*>,
        is StmtP.Continue<*>,
        is StmtP.Pass<*> -> { /* no expressions */ }
    }
}

/**
 * Visit immediate child statements in an [Spanned<StmtP<AstNoPayload>>] node.
 * Mirrors `StmtP::visit_stmt` from Rust's `uniplate.rs`.
 */
@Suppress("UNCHECKED_CAST")
internal fun Spanned<StmtP<AstNoPayload>>.visitStmtChildren(f: (Spanned<StmtP<AstNoPayload>>) -> Unit) {
    when (val s = node) {
        is StmtP.Statements<*> -> s.stmts.forEach { f(it as Spanned<StmtP<AstNoPayload>>) }
        is StmtP.If<*> -> f(s.suite as Spanned<StmtP<AstNoPayload>>)
        is StmtP.IfElse<*> -> {
            f(s.suite1 as Spanned<StmtP<AstNoPayload>>)
            f(s.suite2 as Spanned<StmtP<AstNoPayload>>)
        }
        is StmtP.For<*> -> f(s.forStmt.body as Spanned<StmtP<AstNoPayload>>)
        is StmtP.Def<*, *> -> f(s.def.body as Spanned<StmtP<AstNoPayload>>)
        is StmtP.Expression<*>,
        is StmtP.Return<*>,
        is StmtP.Assign<*>,
        is StmtP.AssignModify<*>,
        is StmtP.Load<*, *>,
        is StmtP.Break<*>,
        is StmtP.Continue<*>,
        is StmtP.Pass<*> -> { /* no child statements */ }
    }
}

// Tests in commonTest
