// port-lint: source ../starlark_syntax/src/syntax/module.rs
package io.github.kotlinmania.starlark.syntax

/*
 * Copyright 2018 The Starlark in Rust Authors.
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

import io.github.kotlinmania.starlarksyntax.codemap.CodeMap as CodeMap
import io.github.kotlinmania.starlarksyntax.codemap.FileSpan as FileSpan
import io.github.kotlinmania.starlarksyntax.codemap.Spanned as Spanned
import io.github.kotlinmania.starlark.syntax.ast.AstNoPayload
import io.github.kotlinmania.starlark.syntax.ast.ArgumentP
import io.github.kotlinmania.starlark.syntax.ast.CallArgsP
import io.github.kotlinmania.starlark.syntax.ast.DefP
import io.github.kotlinmania.starlark.syntax.ast.ExprP
import io.github.kotlinmania.starlark.syntax.ast.IdentP
import io.github.kotlinmania.starlark.syntax.ast.LoadArgP
import io.github.kotlinmania.starlark.syntax.ast.BinOp
import io.github.kotlinmania.starlark.syntax.ast.StmtP
import io.github.kotlinmania.starlark.syntax.ast.ForP
import io.github.kotlinmania.starlark.syntax.ast.AssignP
import io.github.kotlinmania.starlark.syntax.ast.AssignTargetP
import io.github.kotlinmania.starlark.syntax.dialect.Dialect
import io.github.kotlinmania.starlark.syntax.lexer.Lexer
import io.github.kotlinmania.starlark.syntax.parser.Parser
import io.github.kotlinmania.starlark.syntax.state.ParserState
import io.github.kotlinmania.starlarksyntax.codemap.Span as Span

class AstLoad(
    val span: FileSpan,
    val moduleId: String,
    val symbols: Map<String, String>
)

data class AstModuleParts(
    val codemap: CodeMap,
    val statement: Spanned<StmtP<AstNoPayload>>,
    val dialect: Dialect,
    val typecheck: Boolean,
)

/**
 * A representation of a Starlark module abstract syntax tree.
 *
 * Created with [AstModule.parse], and evaluated with `Evaluator.evalModule`.
 *
 * The internal details (statements/expressions) are deliberately omitted, as they change
 * more regularly. A few methods to obtain information about the AST are provided.
 */
class AstModule(
    val codemap: CodeMap,
    var statement: Spanned<StmtP<AstNoPayload>>,
    val dialect: Dialect,
    /**
     * Opt-in typecheck.
     * Specified with a `typecheck` directive in the source.
     */
    val typecheck: Boolean
) {
    fun intoParts(): AstModuleParts =
        AstModuleParts(codemap, statement, dialect, typecheck)
    companion object {
        /**
         * Parse a Starlark module to produce an [AstModule], or an error if there are syntax errors.
         * The [filename] is for error messages only, and does not have to be a valid file.
         * The [Dialect] selects which Starlark constructs are valid.
         *
         * The returned error may contain diagnostic information.
         */
        fun parse(filename: String, content: String, dialect: Dialect): Result<AstModule> {
            val codemap = CodeMap.new(filename, content)
            val lexer = Lexer(content, dialect, codemap)
            val parserState = ParserState(dialect, codemap, mutableListOf())
            return try {
                val statement = Parser.parse(parserState, lexer)
                if (parserState.errors.isNotEmpty()) {
                    Result.failure(parserState.errors.first())
                } else {
                    Result.success(AstModule(codemap, statement, dialect, false))
                }
            } catch (e: io.github.kotlinmania.starlarksyntax.evalexception.EvalException) {
                Result.failure(e)
            }
        }
    }

    /**
     * Return the file names of all the `load` statements in the module.
     * If the [Dialect] had `enableLoad` set to `false` this will be an empty list.
     */
    fun loads(): List<AstLoad> {
        val loads = mutableListOf<AstLoad>()
        fun walk(ast: Spanned<StmtP<AstNoPayload>>) {
            when (val node = ast.node) {
                is StmtP.Load<AstNoPayload, *> -> {
                    loads.add(
                        AstLoad(
                            span = FileSpan(codemap, node.loadStmt.module.span),
                            moduleId = node.loadStmt.module.node,
                            symbols = node.loadStmt.args.associate {
                                it.local.node.ident to it.their.node
                            }
                        )
                    )
                }
                is StmtP.Statements -> {
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

    /**
     * Function to help people who want to write deeper AST transformations in Starlark.
     * Likely to break type checking and LSP support to some extent.
     *
     * Replacement must be a map from operator name (e.g. `+` or `==`) to a function name
     * (e.g. `myPlus` or `myEquals`).
     */
    fun replaceBinaryOperators(replace: Map<String, String>) {
        statement = rewriteStmt(statement, replace)
    }

    /** Look up a [Span] contained in this module to a [FileSpan]. */
    fun fileSpan(span: Span): FileSpan = codemap.fileSpan(span)

    /** Locations where statements occur. */
    fun stmtLocations(): List<FileSpan> {
        val res = mutableListOf<FileSpan>()
        fun walk(ast: Spanned<StmtP<AstNoPayload>>) {
            if (ast.node !is StmtP.Statements) {
                res.add(FileSpan(codemap, ast.span))
            }
            // we should descend if possible (like visitStmt), but since we omit Spanned<StmtP<AstNoPayload>>'s walk here,
            // we can just implement the full traversal later.
        }
        walk(statement)
        return res
    }
}

// --- replaceBinaryOperators helpers ---

/**
 * Convert a [BinOp] to its operator symbol string (trimmed, no surrounding spaces).
 */
private fun BinOp.toSymbol(): String = when (this) {
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
private fun rewriteExpr(expr: Spanned<ExprP<AstNoPayload>>, replace: Map<String, String>): Spanned<ExprP<AstNoPayload>> {
    val node = expr.node
    val rewritten: ExprP<AstNoPayload> = when (node) {
        is ExprP.Op -> {
            val func = replace[node.op.toSymbol()]
            if (func != null) {
                // Replace a binary operation with a function call: `func(lhs, rhs)`.
                val lhs = rewriteExpr(node.lhs, replace)
                val rhs = rewriteExpr(node.rhs, replace)
                ExprP.Call<AstNoPayload>(
                    expr = Spanned(
                        ExprP.Identifier<AstNoPayload, Unit>(
                            Spanned(IdentP<AstNoPayload, Unit>(func, Unit), expr.span)
                        ),
                        expr.span,
                    ),
                    args = CallArgsP(listOf(
                        Spanned(ArgumentP.Positional<AstNoPayload>(lhs), lhs.span),
                        Spanned(ArgumentP.Positional<AstNoPayload>(rhs), rhs.span),
                    )),
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
        is ExprP.Call -> ExprP.Call(
            rewriteExpr(node.expr, replace),
            CallArgsP(node.args.args.map { arg ->
                Spanned(rewriteArg(arg.node, replace), arg.span)
            }),
        )
        is ExprP.Tuple -> ExprP.Tuple(node.elements.map { rewriteExpr(it, replace) })
        is ExprP.Dot -> ExprP.Dot(rewriteExpr(node.expr, replace), node.field)
        is ExprP.Index -> ExprP.Index(rewriteExpr(node.expr, replace), rewriteExpr(node.index, replace))
        is ExprP.Slice -> ExprP.Slice(
            rewriteExpr(node.expr, replace),
            node.start?.let { rewriteExpr(it, replace) },
            node.stop?.let { rewriteExpr(it, replace) },
            node.step?.let { rewriteExpr(it, replace) },
        )
        is ExprP.Not -> ExprP.Not(rewriteExpr(node.expr, replace))
        is ExprP.Minus -> ExprP.Minus(rewriteExpr(node.expr, replace))
        is ExprP.Plus -> ExprP.Plus(rewriteExpr(node.expr, replace))
        is ExprP.BitNot -> ExprP.BitNot(rewriteExpr(node.expr, replace))
        is ExprP.If -> ExprP.If(
            rewriteExpr(node.cond, replace),
            rewriteExpr(node.v1, replace),
            rewriteExpr(node.v2, replace),
        )
        is ExprP.ListExpr -> ExprP.ListExpr(node.elements.map { rewriteExpr(it, replace) })
        is ExprP.Dict -> ExprP.Dict(node.elements.map { (k, v) ->
            Pair(rewriteExpr(k, replace), rewriteExpr(v, replace))
        })
        is ExprP.ListComprehension -> ExprP.ListComprehension(
            rewriteExpr(node.expr, replace), node.forClause, node.clauses,
        )
        is ExprP.DictComprehension -> ExprP.DictComprehension(
            rewriteExpr(node.key, replace), rewriteExpr(node.value, replace),
            node.forClause, node.clauses,
        )
        // Leaf nodes: no children to rewrite
        is ExprP.Identifier<AstNoPayload, *> -> node
        is ExprP.Lambda<AstNoPayload, *> -> node
        is ExprP.Literal -> node
        is ExprP.Index2 -> node
        is ExprP.FString -> node
    }
    return Spanned(rewritten, expr.span)
}

private fun rewriteArg(arg: ArgumentP<AstNoPayload>, replace: Map<String, String>): ArgumentP<AstNoPayload> {
    return when (arg) {
        is ArgumentP.Positional -> ArgumentP.Positional(rewriteExpr(arg.expr, replace))
        is ArgumentP.Named -> ArgumentP.Named(arg.name, rewriteExpr(arg.expr, replace))
        is ArgumentP.Args -> ArgumentP.Args(rewriteExpr(arg.expr, replace))
        is ArgumentP.KwArgs -> ArgumentP.KwArgs(rewriteExpr(arg.expr, replace))
    }
}

/** Rewrite a statement, recursively rewriting all contained expressions. */
private fun rewriteStmt(stmt: Spanned<StmtP<AstNoPayload>>, replace: Map<String, String>): Spanned<StmtP<AstNoPayload>> {
    val node = stmt.node
    val rewritten: StmtP<AstNoPayload> = when (node) {
        is StmtP.Statements -> StmtP.Statements<AstNoPayload>(
            node.stmts.map { rewriteStmt(it, replace) },
        )
        is StmtP.Expression -> StmtP.Expression<AstNoPayload>(
            rewriteExpr(node.expr, replace),
        )
        is StmtP.Return -> StmtP.Return<AstNoPayload>(
            node.expr?.let { rewriteExpr(it, replace) },
        )
        is StmtP.If -> StmtP.If<AstNoPayload>(
            rewriteExpr(node.cond, replace),
            rewriteStmt(node.suite, replace),
        )
        is StmtP.IfElse -> StmtP.IfElse<AstNoPayload>(
            rewriteExpr(node.cond, replace),
            rewriteStmt(node.suite1, replace),
            rewriteStmt(node.suite2, replace),
        )
        is StmtP.For -> {
            val forStmt = node.forStmt
            StmtP.For<AstNoPayload>(forStmt.copy(
                over = rewriteExpr(forStmt.over, replace),
                body = rewriteStmt(forStmt.body, replace),
            ))
        }
        is StmtP.Def<AstNoPayload, *> -> rewriteDef(node, replace)
        is StmtP.Assign -> {
            val assign = node.assign
            StmtP.Assign<AstNoPayload>(assign.copy(
                rhs = rewriteExpr(assign.rhs, replace),
            ))
        }
        is StmtP.AssignModify -> StmtP.AssignModify<AstNoPayload>(
            node.lhs,
            node.op,
            rewriteExpr(node.rhs, replace),
        )
        is StmtP.Load<AstNoPayload, *> -> node
        is StmtP.Break -> node
        is StmtP.Continue -> node
        is StmtP.Pass -> node
    }
    return Spanned(rewritten, stmt.span)
}

// IAP for Def is captured through a star projection — extracted to a generic
// helper so the IAP type variable is bound rather than asserted via UNCHECKED_CAST.
private fun <IAP> rewriteDef(
    node: StmtP.Def<AstNoPayload, IAP>,
    replace: Map<String, String>,
): StmtP.Def<AstNoPayload, IAP> {
    val def = node.def
    return StmtP.Def(DefP(
        name = def.name,
        params = def.params,
        returnType = def.returnType,
        body = rewriteStmt(def.body, replace),
        payload = def.payload,
    ))
}
