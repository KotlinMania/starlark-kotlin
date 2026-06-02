// port-lint: source src/syntax/module.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

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
import io.github.kotlinmania.starlark.codemap.Pos
import io.github.kotlinmania.starlark.codemap.Span
import io.github.kotlinmania.starlark.codemap.Spanned
import io.github.kotlinmania.starlark.syntax.ast.ArgumentP
import io.github.kotlinmania.starlark.syntax.ast.AssignIdentP
import io.github.kotlinmania.starlark.syntax.ast.AssignP
import io.github.kotlinmania.starlark.syntax.ast.AssignTargetP
import io.github.kotlinmania.starlark.syntax.ast.AstExpr
import io.github.kotlinmania.starlark.syntax.ast.AstNoPayload
import io.github.kotlinmania.starlark.syntax.ast.AstNoTypeExprPayload
import io.github.kotlinmania.starlark.syntax.ast.AstStmt
import io.github.kotlinmania.starlark.syntax.ast.BinOp
import io.github.kotlinmania.starlark.syntax.ast.CallArgsP
import io.github.kotlinmania.starlark.syntax.ast.ClauseP
import io.github.kotlinmania.starlark.syntax.ast.DefP
import io.github.kotlinmania.starlark.syntax.ast.ExprP
import io.github.kotlinmania.starlark.syntax.ast.FStringP
import io.github.kotlinmania.starlark.syntax.ast.ForClauseP
import io.github.kotlinmania.starlark.syntax.ast.ForP
import io.github.kotlinmania.starlark.syntax.ast.IdentP
import io.github.kotlinmania.starlark.syntax.ast.LambdaP
import io.github.kotlinmania.starlark.syntax.ast.LoadArgP
import io.github.kotlinmania.starlark.syntax.ast.LoadP
import io.github.kotlinmania.starlark.syntax.ast.ParameterP
import io.github.kotlinmania.starlark.syntax.ast.StmtP
import io.github.kotlinmania.starlark.syntax.ast.TypeExprP
import io.github.kotlinmania.starlark.syntax.dialect.Dialect
import io.github.kotlinmania.starlark.syntax.lexer.Lexer
import io.github.kotlinmania.starlark.syntax.lexer.Token
import io.github.kotlinmania.starlark.syntax.parser.Parser
import io.github.kotlinmania.starlark.syntax.state.ParserState
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
class AstLoad(
    val span: FileSpan,
    val moduleId: String,
    val symbols: Map<String, String>,
)

internal data class AstModuleParts(
    val codemap: CodeMap,
    val statement: AstStmt,
    val dialect: Dialect,
    val typecheck: Boolean,
)

class AstModule(
    val codemap: CodeMap,
    internal var statement: AstStmt,
    val dialect: Dialect,
    val typecheck: Boolean,
    private val lintSuppressions: LintSuppressions = LintSuppressions.EMPTY,
) {
    fun codemap(): CodeMap = codemap

    internal fun statement(): AstStmt = statement

    internal fun intoParts(): AstModuleParts =
        AstModuleParts(codemap, statement, dialect, typecheck)

    companion object {
        fun parse(filename: String, content: String, dialect: Dialect): Result<AstModule> {
            val codemap = CodeMap(filename, content)
            val lexer = Lexer(content, dialect, codemap)
            val parserState = ParserState(dialect, codemap, mutableListOf())
            return try {
                val statement = Parser.parse(parserState, lexer)
                if (parserState.errors.isEmpty()) {
                    validateModule(statement, parserState)
                }
                if (parserState.errors.isNotEmpty()) {
                    Result.failure(parserState.errors.first())
                } else {
                    Result.success(
                        AstModule(
                            codemap,
                            statement,
                            dialect,
                            content.contains("@starlark-rust: typecheck"),
                            parseLintSuppressions(codemap, dialect),
                        ),
                    )
                }
            } catch (e: io.github.kotlinmania.starlark.typing.EvalException) {
                Result.failure(e)
            }
        }

        private fun parseLintSuppressions(codemap: CodeMap, dialect: Dialect): LintSuppressions {
            val lexer = Lexer(codemap.source, dialect, codemap)
            val builder = LintSuppressionsBuilder()
            var inCommentBlock = false
            var lastCommentLine = -1
            while (true) {
                val lexeme = lexer.nextIncludingComments() ?: break
                val (start, token, end) = lexeme
                if (token is Token.Comment) {
                    val commentLine = codemap.findLine(Pos(start))
                    if (inCommentBlock && commentLine > lastCommentLine + 1) {
                        builder.endOfCommentBlock(codemap)
                        inCommentBlock = false
                    }
                    builder.parseComment(codemap, token.text, start, end)
                    inCommentBlock = true
                    lastCommentLine = commentLine
                } else if (inCommentBlock && token !is Token.Newline) {
                    val tokenLine = codemap.findLine(Pos(start))
                    builder.endOfCommentBlock(codemap)
                    inCommentBlock = false
                    lastCommentLine = if (tokenLine > lastCommentLine + 1) -1 else lastCommentLine
                }
            }
            if (inCommentBlock) {
                builder.endOfCommentBlock(codemap)
            }
            return builder.build()
        }
    }

    fun isSuppressed(issueShortName: String, issueSpan: Span): Boolean =
        lintSuppressions.isSuppressed(issueShortName, issueSpan)

    internal fun loads(): List<AstLoad> {
        val loads = mutableListOf<AstLoad>()

        fun walk(ast: Spanned<StmtP<*>>) {
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
                        walk(stmt)
                    }
                }
                else -> {}
            }
        }
        walk(statement)
        return loads
    }

    internal fun replaceBinaryOperators(replace: Map<String, String>) {
        statement = rewriteStmt(statement, replace)
    }

    fun fileSpan(span: Span): FileSpan = codemap.fileSpan(span)

    internal fun stmtLocations(): List<FileSpan> {
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
private fun rewriteExpr(expr: Spanned<ExprP<*>>, replace: Map<String, String>): AstExpr {
    val node = expr.node
    val rewritten =
        when (node) {
            is ExprP.Op -> {
                val func = replace[node.op.toSymbol()]
                if (func != null) {
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
                    ExprP.Op<AstNoPayload>(
                        rewriteExpr(node.lhs, replace),
                        node.op,
                        rewriteExpr(node.rhs, replace),
                    )
                }
            }
            is ExprP.Call ->
                ExprP.Call(
                    rewriteExpr(node.expr, replace),
                    CallArgsP(
                        node.args.args.map { arg ->
                            Spanned(rewriteArg(arg.node, replace), arg.span)
                        },
                    ),
                )
            is ExprP.Tuple -> ExprP.Tuple(node.elements.map { rewriteExpr(it, replace) })
            is ExprP.Dot -> ExprP.Dot(rewriteExpr(node.expr, replace), node.field)
            is ExprP.Index -> ExprP.Index(rewriteExpr(node.expr, replace), rewriteExpr(node.index, replace))
            is ExprP.Slice ->
                ExprP.Slice(
                    rewriteExpr(node.expr, replace),
                    node.start?.let { rewriteExpr(it, replace) },
                    node.stop?.let { rewriteExpr(it, replace) },
                    node.step?.let { rewriteExpr(it, replace) },
                )
            is ExprP.Not -> ExprP.Not(rewriteExpr(node.expr, replace))
            is ExprP.Minus -> ExprP.Minus(rewriteExpr(node.expr, replace))
            is ExprP.Plus -> ExprP.Plus(rewriteExpr(node.expr, replace))
            is ExprP.BitNot -> ExprP.BitNot(rewriteExpr(node.expr, replace))
            is ExprP.If ->
                ExprP.If(
                    rewriteExpr(node.cond, replace),
                    rewriteExpr(node.v1, replace),
                    rewriteExpr(node.v2, replace),
                )
            is ExprP.ListExpr -> ExprP.ListExpr(node.elements.map { rewriteExpr(it, replace) })
            is ExprP.Dict ->
                ExprP.Dict(
                    node.elements.map { (k, v) ->
                        Pair(rewriteExpr(k, replace), rewriteExpr(v, replace))
                    },
                )
            is ExprP.ListComprehension ->
                ExprP.ListComprehension(
                    rewriteExpr(node.expr, replace),
                    rewriteForClause(node.forClause, replace),
                    node.clauses.map { rewriteClause(it, replace) },
                )
            is ExprP.DictComprehension ->
                ExprP.DictComprehension(
                    rewriteExpr(node.key, replace),
                    rewriteExpr(node.value, replace),
                    rewriteForClause(node.forClause, replace),
                    node.clauses.map { rewriteClause(it, replace) },
                )
            is ExprP.Identifier<*, *> -> {
                val ident = node.ident
                ExprP.Identifier<AstNoPayload, Unit>(
                    Spanned(IdentP(ident.node.ident, Unit), ident.span),
                )
            }
            is ExprP.Lambda<*, *> -> {
                val lambda = node.lambda
                ExprP.Lambda<AstNoPayload, Any?>(
                    LambdaP(
                        params = lambda.params.map { rewriteParameter(it, replace) },
                        body = rewriteExpr(lambda.body, replace),
                        payload = lambda.payload,
                    ),
                )
            }
            is ExprP.Literal -> ExprP.Literal<AstNoPayload>(node.literal)
            is ExprP.Index2 ->
                ExprP.Index2<AstNoPayload>(
                    rewriteExpr(node.expr, replace),
                    rewriteExpr(node.index0, replace),
                    rewriteExpr(node.index1, replace),
                )
            is ExprP.FString -> {
                val fstring = node.fstring
                ExprP.FString<AstNoPayload>(
                    Spanned(
                        FStringP(
                            format = fstring.node.format,
                            expressions = fstring.node.expressions.map { rewriteExpr(it, replace) },
                        ),
                        fstring.span,
                    ),
                )
            }
        }
    return Spanned(rewritten, expr.span)
}

private fun rewriteArg(arg: ArgumentP<*>, replace: Map<String, String>): ArgumentP<AstNoPayload> =
    when (arg) {
        is ArgumentP.Positional -> ArgumentP.Positional(rewriteExpr(arg.expr, replace))
        is ArgumentP.Named -> ArgumentP.Named(arg.name, rewriteExpr(arg.expr, replace))
        is ArgumentP.Args -> ArgumentP.Args(rewriteExpr(arg.expr, replace))
        is ArgumentP.KwArgs -> ArgumentP.KwArgs(rewriteExpr(arg.expr, replace))
    }

private fun rewriteAssignTarget(target: Spanned<AssignTargetP<*>>, replace: Map<String, String>): Spanned<AssignTargetP<AstNoPayload>> {
    val node = target.node
    val rewritten =
        when (node) {
            is AssignTargetP.Tuple ->
                AssignTargetP.Tuple<AstNoPayload>(
                    node.elements.map { rewriteAssignTarget(it, replace) },
                )
            is AssignTargetP.Index ->
                AssignTargetP.Index<AstNoPayload>(
                    rewriteExpr(node.expr, replace),
                    rewriteExpr(node.index, replace),
                )
            is AssignTargetP.Dot ->
                AssignTargetP.Dot<AstNoPayload>(
                    rewriteExpr(node.expr, replace),
                    node.field,
                )
            is AssignTargetP.Identifier<*, *> -> {
                val ident = node.ident
                AssignTargetP.Identifier<AstNoPayload, Unit>(
                    Spanned(AssignIdentP(ident.node.ident, Unit), ident.span),
                )
            }
        }
    return Spanned(rewritten, target.span)
}

private fun rewriteParameter(param: Spanned<ParameterP<*>>, replace: Map<String, String>): Spanned<ParameterP<AstNoPayload>> {
    val node = param.node
    val rewritten =
        when (node) {
            is ParameterP.Slash -> ParameterP.Slash<AstNoPayload>()
            is ParameterP.NoArgs -> ParameterP.NoArgs<AstNoPayload>()
            is ParameterP.Normal -> {
                val name = node.name
                ParameterP.Normal<AstNoPayload>(
                    name = Spanned(AssignIdentP(name.node.ident, Unit), name.span),
                    typ = node.typ?.let { rewriteTypeExpr(it, replace) },
                    defaultVal = node.defaultVal?.let { rewriteExpr(it, replace) },
                )
            }
            is ParameterP.Args -> {
                val name = node.name
                ParameterP.Args<AstNoPayload>(
                    name = Spanned(AssignIdentP(name.node.ident, Unit), name.span),
                    typ = node.typ?.let { rewriteTypeExpr(it, replace) },
                )
            }
            is ParameterP.KwArgs -> {
                val name = node.name
                ParameterP.KwArgs<AstNoPayload>(
                    name = Spanned(AssignIdentP(name.node.ident, Unit), name.span),
                    typ = node.typ?.let { rewriteTypeExpr(it, replace) },
                )
            }
        }
    return Spanned(rewritten, param.span)
}

private fun rewriteTypeExpr(te: Spanned<TypeExprP<*>>, replace: Map<String, String>): Spanned<TypeExprP<AstNoPayload>> =
    Spanned(
        TypeExprP(
            expr = rewriteExpr(te.node.expr, replace),
            payload = AstNoTypeExprPayload,
        ),
        te.span,
    )

private fun rewriteForClause(fc: ForClauseP<*>, replace: Map<String, String>): ForClauseP<AstNoPayload> =
    ForClauseP(
        varTarget = rewriteAssignTarget(fc.varTarget, replace),
        over = rewriteExpr(fc.over, replace),
    )

private fun rewriteClause(c: ClauseP<*>, replace: Map<String, String>): ClauseP<AstNoPayload> =
    when (c) {
        is ClauseP.For -> ClauseP.For(rewriteForClause(c.forClause, replace))
        is ClauseP.If -> ClauseP.If(rewriteExpr(c.cond, replace))
    }

/** Rewrite a statement, recursively rewriting all contained expressions. */
private fun rewriteStmt(stmt: Spanned<StmtP<*>>, replace: Map<String, String>): AstStmt {
    val node = stmt.node
    val rewritten =
        when (node) {
            is StmtP.Statements<*> ->
                StmtP.Statements<AstNoPayload>(
                    node.stmts.map { rewriteStmt(it, replace) },
                )
            is StmtP.Expression<*> ->
                StmtP.Expression<AstNoPayload>(
                    rewriteExpr(node.expr, replace),
                )
            is StmtP.Return<*> ->
                StmtP.Return<AstNoPayload>(
                    node.expr?.let { rewriteExpr(it, replace) },
                )
            is StmtP.If<*> ->
                StmtP.If<AstNoPayload>(
                    rewriteExpr(node.cond, replace),
                    rewriteStmt(node.suite, replace),
                )
            is StmtP.IfElse<*> ->
                StmtP.IfElse<AstNoPayload>(
                    rewriteExpr(node.cond, replace),
                    rewriteStmt(node.suite1, replace),
                    rewriteStmt(node.suite2, replace),
                )
            is StmtP.For<*> -> {
                val forStmt = node.forStmt
                StmtP.For<AstNoPayload>(
                    ForP(
                        varTarget = rewriteAssignTarget(forStmt.varTarget, replace),
                        over = rewriteExpr(forStmt.over, replace),
                        body = rewriteStmt(forStmt.body, replace),
                    ),
                )
            }
            is StmtP.Def<*, *> -> {
                val def = node.def
                StmtP.Def<AstNoPayload, Any?>(
                    DefP(
                        name = Spanned(AssignIdentP(def.name.node.ident, Unit), def.name.span),
                        params = def.params.map { rewriteParameter(it, replace) },
                        returnType = def.returnType?.let { rewriteTypeExpr(it, replace) },
                        body = rewriteStmt(def.body, replace),
                        payload = def.payload,
                    ),
                )
            }
            is StmtP.Assign<*> -> {
                val assign = node.assign
                StmtP.Assign<AstNoPayload>(
                    AssignP(
                        lhs = rewriteAssignTarget(assign.lhs, replace),
                        ty = assign.ty?.let { rewriteTypeExpr(it, replace) },
                        rhs = rewriteExpr(assign.rhs, replace),
                    ),
                )
            }
            is StmtP.AssignModify<*> ->
                StmtP.AssignModify<AstNoPayload>(
                    rewriteAssignTarget(node.lhs, replace),
                    node.op,
                    rewriteExpr(node.rhs, replace),
                )
            is StmtP.Load<*, *> -> {
                val load = node.loadStmt
                StmtP.Load<AstNoPayload, Unit>(
                    LoadP(
                        module = load.module,
                        args =
                            load.args.map { arg ->
                                LoadArgP(
                                    local = Spanned(AssignIdentP(arg.local.node.ident, Unit), arg.local.span),
                                    their = arg.their,
                                    comma = arg.comma,
                                )
                            },
                        payload = Unit,
                    ),
                )
            }
            is StmtP.Break<*> -> StmtP.Break<AstNoPayload>()
            is StmtP.Continue<*> -> StmtP.Continue<AstNoPayload>()
            is StmtP.Pass<*> -> StmtP.Pass<AstNoPayload>()
        }
    return Spanned(rewritten, stmt.span)
}
