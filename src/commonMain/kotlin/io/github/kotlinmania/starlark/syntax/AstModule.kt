// port-lint: source src/syntax/module.rs
package io.github.kotlinmania.starlark.syntax

/*
 * Copyright 2018 The Starlark in Rust Authors.
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
import io.github.kotlinmania.starlark.codemap.Spanned
import io.github.kotlinmania.starlark.syntax.ast.ArgumentP
import io.github.kotlinmania.starlark.syntax.ast.AssignP
import io.github.kotlinmania.starlark.syntax.ast.AssignTargetP
import io.github.kotlinmania.starlark.syntax.ast.AstExpr
import io.github.kotlinmania.starlark.syntax.ast.AstNoPayload
import io.github.kotlinmania.starlark.syntax.ast.AstStmt
import io.github.kotlinmania.starlark.syntax.ast.BinOp
import io.github.kotlinmania.starlark.syntax.ast.CallArgsP
import io.github.kotlinmania.starlark.syntax.ast.DefP
import io.github.kotlinmania.starlark.syntax.ast.ExprP
import io.github.kotlinmania.starlark.syntax.ast.ForP
import io.github.kotlinmania.starlark.syntax.ast.IdentP
import io.github.kotlinmania.starlark.syntax.ast.StmtP
import io.github.kotlinmania.starlark.syntax.dialect.Dialect
import io.github.kotlinmania.starlark.syntax.lexer.Lexer
import io.github.kotlinmania.starlark.syntax.parser.Parser
import io.github.kotlinmania.starlark.syntax.state.ParserState

class AstLoad(
    val span: FileSpan,
    val moduleId: String,
    val symbols: Map<String, String>,
)

data class AstModuleParts(
    val codemap: CodeMap,
    val statement: AstStmt,
    val dialect: Dialect,
    val typecheck: Boolean,
)

class AstModule(
    val codemap: CodeMap,
    var statement: AstStmt,
    val dialect: Dialect,
    val typecheck: Boolean,
) {
    fun codemap(): CodeMap = codemap

    fun statement(): AstStmt = statement

    fun intoParts(): AstModuleParts =
        AstModuleParts(codemap, statement, dialect, typecheck)

    companion object {
        fun parse(filename: String, content: String, dialect: Dialect): Result<AstModule> {
            val codemap = CodeMap(filename, content)
            val lexer = Lexer(content, dialect, codemap)
            val parserState = ParserState(dialect, codemap, mutableListOf())
            return try {
                val statement = Parser.parse(parserState, lexer)
                if (parserState.errors.isNotEmpty()) {
                    Result.failure(parserState.errors.first())
                } else {
                    Result.success(AstModule(codemap, statement, dialect, false))
                }
            } catch (e: io.github.kotlinmania.starlark.typing.EvalException) {
                Result.failure(e)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun loads(): List<AstLoad> {
        val loads = mutableListOf<AstLoad>()

        fun walk(ast: AstStmt) {
            when (val node = ast.node) {
                is StmtP.Load<*, *> -> {
                    loads.add(
                        AstLoad(
                            span = FileSpan(codemap, node.loadStmt.module.span),
                            moduleId = node.loadStmt.module.node,
                            symbols =
                                node.loadStmt.args.associate {
                                    it.local.node.ident to it.their.node
                                },
                        ),
                    )
                }
                is StmtP.Statements<*> -> {
                    for (stmt in node.stmts) {
                        walk(stmt as AstStmt)
                    }
                }
                else -> {}
            }
        }
        walk(statement)
        return loads
    }

    fun replaceBinaryOperators(replace: Map<String, String>) {
        statement = rewriteStmt(statement, replace)
    }

    fun fileSpan(span: Span): FileSpan = codemap.fileSpan(span)

    fun stmtLocations(): List<FileSpan> {
        val res = mutableListOf<FileSpan>()

        fun walk(ast: AstStmt) {
            if (ast.node !is StmtP.Statements<*>) {
                res.add(FileSpan(codemap, ast.span))
            }
            // we should descend if possible (like visitStmt), but since we omit AstStmt's walk here,
            // we can just implement the full traversal later.
        }
        walk(statement)
        return res
    }
}

// --- replaceBinaryOperators helpers ---

/**
 * Convert a [BinOp] to its operator symbol string (trimmed).
 * Mirrors Rust's `Display for BinOp`, but trimmed (the Rust Display includes surrounding spaces).
 */
private fun BinOp.toSymbol(): String =
    when (this) {
        BinOp.Or -> "or"
        BinOp.And -> "and"
        BinOp.Equal -> "=="
        BinOp.NotEqual -> "!="
        BinOp.Less -> "<"
        BinOp.Greater -> ">"
        BinOp.LessOrEqual -> "<="
        BinOp.GreaterOrEqual -> ">="
        BinOp.In -> "in"
        BinOp.NotIn -> "not in"
        BinOp.Subtract -> "-"
        BinOp.Add -> "+"
        BinOp.Multiply -> "*"
        BinOp.Percent -> "%"
        BinOp.Divide -> "/"
        BinOp.FloorDivide -> "//"
        BinOp.BitAnd -> "&"
        BinOp.BitOr -> "|"
        BinOp.BitXor -> "^"
        BinOp.LeftShift -> "<<"
        BinOp.RightShift -> ">>"
    }

/**
 * Rewrite an expression, replacing binary operators according to the [replace] map.
 * If a binary operator's symbol is found in [replace], the Op node is replaced with
 * a Call to the named function, passing the lhs and rhs as positional arguments.
 */
private fun rewriteExpr(expr: AstExpr, replace: Map<String, String>): AstExpr {
    val node = expr.node
    val rewritten =
        when (node) {
            is ExprP.Op<AstNoPayload> -> {
                val func = replace[node.op.toSymbol()]
                if (func != null) {
                    // Replace: Op(lhs, op, rhs) -> Call(Identifier(func), [lhs, rhs])
                    val lhs = rewriteExpr(node.lhs, replace)
                    val rhs = rewriteExpr(node.rhs, replace)
                    ExprP.Call<AstNoPayload>(
                        expr =
                            Spanned(
                                ExprP.Identifier<AstNoPayload, Unit>(
                                    Spanned(IdentP<AstNoPayload, Unit>(func, Unit), expr.span),
                                ),
                                expr.span,
                            ),
                        args =
                            CallArgsP(
                                listOf(
                                    Spanned(ArgumentP.Positional<AstNoPayload>(lhs), lhs.span),
                                    Spanned(ArgumentP.Positional<AstNoPayload>(rhs), rhs.span),
                                ),
                            ),
                    )
                } else {
                    // Keep Op but rewrite children
                    ExprP.Op<AstNoPayload>(
                        rewriteExpr(node.lhs, replace),
                        node.op,
                        rewriteExpr(node.rhs, replace),
                    )
                }
            }
            is ExprP.Call<AstNoPayload> ->
                ExprP.Call(
                    rewriteExpr(node.expr, replace),
                    CallArgsP(
                        node.args.args.map { arg ->
                            Spanned(rewriteArg(arg.node, replace), arg.span)
                        },
                    ),
                )
            is ExprP.Tuple<AstNoPayload> -> ExprP.Tuple(node.elements.map { rewriteExpr(it, replace) })
            is ExprP.Dot<AstNoPayload> -> ExprP.Dot(rewriteExpr(node.expr, replace), node.field)
            is ExprP.Index<AstNoPayload> -> ExprP.Index(rewriteExpr(node.expr, replace), rewriteExpr(node.index, replace))
            is ExprP.Slice<AstNoPayload> ->
                ExprP.Slice(
                    rewriteExpr(node.expr, replace),
                    node.start?.let { rewriteExpr(it, replace) },
                    node.stop?.let { rewriteExpr(it, replace) },
                    node.step?.let { rewriteExpr(it, replace) },
                )
            is ExprP.Not<AstNoPayload> -> ExprP.Not(rewriteExpr(node.expr, replace))
            is ExprP.Minus<AstNoPayload> -> ExprP.Minus(rewriteExpr(node.expr, replace))
            is ExprP.Plus<AstNoPayload> -> ExprP.Plus(rewriteExpr(node.expr, replace))
            is ExprP.BitNot<AstNoPayload> -> ExprP.BitNot(rewriteExpr(node.expr, replace))
            is ExprP.If<AstNoPayload> ->
                ExprP.If(
                    rewriteExpr(node.cond, replace),
                    rewriteExpr(node.v1, replace),
                    rewriteExpr(node.v2, replace),
                )
            is ExprP.ListExpr<AstNoPayload> -> ExprP.ListExpr(node.elements.map { rewriteExpr(it, replace) })
            is ExprP.Dict<AstNoPayload> ->
                ExprP.Dict(
                    node.elements.map { (k, v) ->
                        Pair(rewriteExpr(k, replace), rewriteExpr(v, replace))
                    },
                )
            is ExprP.ListComprehension<AstNoPayload> ->
                ExprP.ListComprehension(
                    rewriteExpr(node.expr, replace),
                    node.forClause,
                    node.clauses,
                )
            is ExprP.DictComprehension<AstNoPayload> ->
                ExprP.DictComprehension(
                    rewriteExpr(node.key, replace),
                    rewriteExpr(node.value, replace),
                    node.forClause,
                    node.clauses,
                )
            // Leaf nodes: no children to rewrite
            is ExprP.Identifier<*, *> -> node
            is ExprP.Lambda<*, *> -> node
            is ExprP.Literal<*> -> node
            is ExprP.Index2<*> -> node
            is ExprP.FString<*> -> node
        }
    return Spanned(rewritten, expr.span)
}

private fun rewriteArg(arg: ArgumentP<AstNoPayload>, replace: Map<String, String>): ArgumentP<AstNoPayload> =
    when (arg) {
        is ArgumentP.Positional -> ArgumentP.Positional(rewriteExpr(arg.expr, replace))
        is ArgumentP.Named -> ArgumentP.Named(arg.name, rewriteExpr(arg.expr, replace))
        is ArgumentP.Args -> ArgumentP.Args(rewriteExpr(arg.expr, replace))
        is ArgumentP.KwArgs -> ArgumentP.KwArgs(rewriteExpr(arg.expr, replace))
    }

/** Rewrite a statement, recursively rewriting all contained expressions. */
@Suppress("UNCHECKED_CAST")
private fun rewriteStmt(stmt: AstStmt, replace: Map<String, String>): AstStmt {
    val node = stmt.node
    val rewritten =
        when (node) {
            is StmtP.Statements<*> ->
                StmtP.Statements<AstNoPayload>(
                    (node.stmts as List<AstStmt>).map { rewriteStmt(it, replace) },
                )
            is StmtP.Expression<*> ->
                StmtP.Expression<AstNoPayload>(
                    rewriteExpr(node.expr as AstExpr, replace),
                )
            is StmtP.Return<*> ->
                StmtP.Return<AstNoPayload>(
                    (node.expr as AstExpr?)?.let { rewriteExpr(it, replace) },
                )
            is StmtP.If<*> ->
                StmtP.If<AstNoPayload>(
                    rewriteExpr(node.cond as AstExpr, replace),
                    rewriteStmt(node.suite as AstStmt, replace),
                )
            is StmtP.IfElse<*> ->
                StmtP.IfElse<AstNoPayload>(
                    rewriteExpr(node.cond as AstExpr, replace),
                    rewriteStmt(node.suite1 as AstStmt, replace),
                    rewriteStmt(node.suite2 as AstStmt, replace),
                )
            is StmtP.For<*> -> {
                val forStmt = node.forStmt as ForP<AstNoPayload>
                StmtP.For<AstNoPayload>(
                    forStmt.copy(
                        over = rewriteExpr(forStmt.over, replace),
                        body = rewriteStmt(forStmt.body, replace),
                    ),
                )
            }
            is StmtP.Def<*, *> -> {
                val def = node.def as DefP<AstNoPayload, Any?>
                StmtP.Def<AstNoPayload, Any?>(
                    DefP(
                        name = def.name,
                        params = def.params,
                        returnType = def.returnType,
                        body = rewriteStmt(def.body, replace),
                        payload = def.payload,
                    ),
                )
            }
            is StmtP.Assign<*> -> {
                val assign = node.assign as AssignP<AstNoPayload>
                StmtP.Assign<AstNoPayload>(
                    assign.copy(
                        rhs = rewriteExpr(assign.rhs, replace),
                    ),
                )
            }
            is StmtP.AssignModify<*> ->
                StmtP.AssignModify<AstNoPayload>(
                    node.lhs as Spanned<AssignTargetP<AstNoPayload>>,
                    node.op,
                    rewriteExpr(node.rhs as AstExpr, replace),
                )
            is StmtP.Load<*, *> -> node as StmtP<AstNoPayload>
            is StmtP.Break<*> -> node as StmtP<AstNoPayload>
            is StmtP.Continue<*> -> node as StmtP<AstNoPayload>
            is StmtP.Pass<*> -> node as StmtP<AstNoPayload>
        }
    return Spanned(rewritten, stmt.span)
}
