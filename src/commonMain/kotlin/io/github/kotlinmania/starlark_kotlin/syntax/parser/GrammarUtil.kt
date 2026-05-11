// port-lint: source ../starlark_syntax/src/syntax/grammar_util.rs
<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/syntax/parser/GrammarUtil.kt
package io.github.kotlinmania.starlark.syntax.parser
=======
package io.github.kotlinmania.starlark_kotlin.syntax.parser
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/syntax/parser/GrammarUtil.kt

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

/** Code called by the parser to handle complex cases not handled by the grammar. */

<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/syntax/parser/GrammarUtil.kt
import io.github.kotlinmania.starlarksyntax.codemap.CodeMap as CodeMap
import io.github.kotlinmania.starlarksyntax.codemap.Pos as Pos
import io.github.kotlinmania.starlarksyntax.codemap.Span as Span
import io.github.kotlinmania.starlarksyntax.codemap.Spanned as Spanned
import io.github.kotlinmania.starlarksyntax.evalexception.EvalException
import io.github.kotlinmania.starlark.syntax.ast.*
import io.github.kotlinmania.starlark.syntax.dialect.DialectTypes
import io.github.kotlinmania.starlark.syntax.lexer.TokenFString
import io.github.kotlinmania.starlark.syntax.state.ParserState
import io.github.kotlinmania.starlark.syntax.typeexpr.TypeExprUnpackP
import io.github.kotlinmania.starlark.typing.CallArgsUnpack
import io.github.kotlinmania.starlark.values.types.string.FormatConv
import io.github.kotlinmania.starlark.values.types.string.FormatParser
import io.github.kotlinmania.starlark.values.types.string.FormatToken
=======
import io.github.kotlinmania.starlark_kotlin.codemap.CodeMap
import io.github.kotlinmania.starlark_kotlin.codemap.Pos
import io.github.kotlinmania.starlark_kotlin.codemap.Span
import io.github.kotlinmania.starlark_kotlin.codemap.Spanned
import io.github.kotlinmania.starlark_kotlin.typing.EvalException
import io.github.kotlinmania.starlark_kotlin.syntax.ast.*
import io.github.kotlinmania.starlark_kotlin.syntax.dialect.DialectTypes
import io.github.kotlinmania.starlark_kotlin.syntax.lexer.TokenFString
import io.github.kotlinmania.starlark_kotlin.syntax.state.ParserState
import io.github.kotlinmania.starlark_kotlin.syntax.type_expr.TypeExprUnpackP
import io.github.kotlinmania.starlark_kotlin.typing.CallArgsUnpack
import io.github.kotlinmania.starlark_kotlin.values.types.string.FormatConv
import io.github.kotlinmania.starlark_kotlin.values.types.string.FormatParser
import io.github.kotlinmania.starlark_kotlin.values.types.string.FormatToken
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/syntax/parser/GrammarUtil.kt

// #[derive(Debug, thiserror::Error)]
// enum GrammarUtilError
private enum class GrammarUtilError(val message: String) {
    InvalidLhs("left-hand-side of assignment must take the form `a`, `a.b` or `a[b]`"),
    InvalidModifyLhs("left-hand-side of modifying assignment cannot be a list or tuple"),
    TypeAnnotationOnAssignOp("type annotations not allowed on augmented assignments"),
    TypeAnnotationOnTupleAssign("type annotations not allowed on multiple assignments"),
    LoadRequiresAtLeastTwoArguments("`load` statement requires at least two arguments"),
}

// #[derive(thiserror::Error, Debug)]
// enum DialectError
private enum class DialectError(val message: String) {
    Types("type annotations are not allowed in this dialect"),
}

object GrammarUtil {
    /** Ensure we produce normalised Statements, rather than singleton Statements. */
    // pub fn statements(mut xs: Vec<AstStmt>, begin: usize, end: usize) -> AstStmt
    fun statements(xs: List<AstStmt>, begin: Int, end: Int): AstStmt {
        return if (xs.size == 1) {
            xs[0]
        } else {
            StmtP.Statements<AstNoPayload>(xs).ast(begin, end)
        }
    }

    // pub fn check_assign(codemap: &CodeMap, x: AstExpr) -> Result<AstAssignTarget, EvalException>
    fun checkAssign(codemap: CodeMap, x: AstExpr): AstAssignTarget {
        val node: AssignTargetP<AstNoPayload> = when (val expr = x.node) {
            is ExprP.Tuple -> AssignTargetP.Tuple(
                expr.elements.map { checkAssign(codemap, it) }
            )
            is ExprP.ListExpr -> AssignTargetP.Tuple(
                expr.elements.map { checkAssign(codemap, it) }
            )
            is ExprP.Dot -> AssignTargetP.Dot(expr.expr, expr.field)
            is ExprP.Index -> AssignTargetP.Index(expr.expr, expr.index)
            is ExprP.Identifier<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                val ident = expr.ident as AstIdent
                AssignTargetP.Identifier(ident.map { s ->
                    AssignIdentP<AstNoPayload, Unit>(
                        ident = s.ident,
                        payload = Unit
                    )
                })
            }
            else -> throw EvalException.newAnyhow(
                IllegalArgumentException(GrammarUtilError.InvalidLhs.message),
                x.span,
                codemap
            )
        }
        return Spanned(node, x.span)
    }

    // pub fn check_assignment(...)
    fun checkAssignment(
        codemap: CodeMap,
        lhs: AstExpr,
        ty: AstTypeExpr?,
        op: AssignOp?,
        rhs: AstExpr
    ): Stmt {
        if (op != null) {
            // for augmented assignment, Starlark doesn't allow tuple/list
            when (lhs.node) {
                is ExprP.Tuple, is ExprP.ListExpr -> throw EvalException.newAnyhow(
                    IllegalArgumentException(GrammarUtilError.InvalidModifyLhs.message),
                    lhs.span,
                    codemap
                )
                else -> {}
            }
        }
        val assignTarget = checkAssign(codemap, lhs)
        if (ty != null) {
            val err = if (op != null) {
                GrammarUtilError.TypeAnnotationOnAssignOp
            } else if (assignTarget.node is AssignTargetP.Tuple) {
                GrammarUtilError.TypeAnnotationOnTupleAssign
            } else {
                null
            }
            if (err != null) {
                throw EvalException.newAnyhow(
                    IllegalArgumentException(err.message),
                    ty.span,
                    codemap
                )
            }
        }
        return when (op) {
            null -> StmtP.Assign(AssignP(
                lhs = assignTarget,
                ty = ty,
                rhs = rhs
            ))
            else -> StmtP.AssignModify(assignTarget, op, rhs)
        }
    }

    // pub(crate) fn check_load_0(module: AstString, parser_state: &mut ParserState) -> Stmt
    fun checkLoad0(module: AstString, parserState: ParserState): Stmt {
        parserState.errors.add(
            EvalException.newAnyhow(
                IllegalArgumentException(GrammarUtilError.LoadRequiresAtLeastTwoArguments.message),
                module.span,
                parserState.codemap
            )
        )
        return StmtP.Load(LoadP(
            module = module,
            args = emptyList(),
            payload = Unit
        ))
    }

    // pub(crate) fn check_load(...)
    fun checkLoad(
        module: AstString,
        args: List<Pair<Pair<AstAssignIdent, AstString>, Spanned<Comma>>>,
        last: Pair<AstAssignIdent, AstString>?,
        parserState: ParserState
    ): Stmt {
        if (args.isEmpty() && last == null) {
            return checkLoad0(module, parserState)
        }

        @Suppress("UNCHECKED_CAST")
        val loadArgs = args.map { (localTheir, comma) ->
            val (local, their) = localTheir
            LoadArgP(
                local = local,
                their = their,
                comma = comma as Spanned<io.github.kotlinmania.starlark_kotlin.syntax.ast.Comma>?
            )
        } + if (last != null) {
            listOf(LoadArgP(
                local = last.first,
                their = last.second,
                comma = null
            ))
        } else {
            emptyList()
        }

        return StmtP.Load(LoadP(
            module = module,
            args = loadArgs,
            payload = Unit
        ))
    }

    // pub(crate) fn fstring(...)
    fun fstring(
        fstring: TokenFString,
        begin: Int,
        end: Int,
        parserState: ParserState
    ): AstFString {
        if (!parserState.dialect.enableFStrings) {
            parserState.error(
                Span(Pos(begin), Pos(end)),
                "Your Starlark dialect must enable f-strings to use them"
            )
        }

        val content = fstring.content
        val contentStartOffset = fstring.contentStartOffset

        val format = StringBuilder(content.length)
        val expressions = mutableListOf<AstExpr>()

        val parser = FormatParser(content)
        while (true) {
            val res = parser.next()
            val token = res.getOrElse { e ->
                parserState.error(
                    Span(Pos(begin), Pos(end)),
                    "Invalid format: ${e.message}"
                )
                break
            } ?: break

            when (token) {
                is FormatToken.Text -> format.append(token.text)
                is FormatToken.Escape -> {
                    // We are producing a format string here so we need to escape this back!
                    format.append(token.escape.backToEscape())
                }
                is FormatToken.Capture -> {
                    val captureBegin = begin + contentStartOffset + token.pos
                    val captureEnd = captureBegin + token.capture.length

                    val ident = lexExactlyOneIdentifier(token.capture)
                    if (ident == null) {
                        parserState.error(
                            Span(Pos(captureBegin), Pos(captureEnd)),
                            "Not a valid identifier: `${token.capture}`"
                        )
                        // Might as well keep going here. This doesn't compromise the parsing of
                        // the rest of the format string.
                        continue
                    }

                    val expr = ExprP.Identifier<AstNoPayload, Unit>(
                        IdentP<AstNoPayload, Unit>(ident = ident, payload = Unit).ast(captureBegin, captureEnd)
                    ).ast(captureBegin, captureEnd)
                    expressions.add(expr)
                    // Positional format.
                    when (token.conv) {
                        FormatConv.Str -> format.append("{}")
                        FormatConv.Repr -> format.append("{!r}")
                    }
                }
            }
        }

        return FStringP<AstNoPayload>(
            format = format.toString().ast(begin, end),
            expressions = expressions
        ).ast(begin, end)
    }

    // pub(crate) fn dialect_check_type(...)
    fun dialectCheckType(
        state: ParserState,
        x: AstExpr
    ): AstTypeExpr {
        if (state.dialect.enableTypes == DialectTypes.Disable) {
            throw EvalException.newAnyhow(
                IllegalArgumentException(DialectError.Types.message),
                x.span,
                state.codemap
            )
        }

        // Validate the type expression
        TypeExprUnpackP.unpack<AstNoPayload, Unit>(x, state.codemap)

        return x.map { node ->
            TypeExprP<AstNoPayload, Unit>(
                expr = Spanned(node, x.span),
                payload = Unit
            )
        }
    }

    // pub(crate) fn check_call(...) from validate.rs
    fun checkCall(
        e: AstExpr,
        a: List<AstArgument>,
        state: ParserState
    ): Expr {
        val args = CallArgsP<AstNoPayload>(args = a)

        try {
            CallArgsUnpack.unpack(args, state.codemap)
        } catch (ex: EvalException) {
            state.errors.add(ex)
        }

        return ExprP.Call(e, args)
    }
}

/**
 * Check if the string is exactly one identifier and return it.
 * Port of starlark_syntax::lexer::lex_exactly_one_identifier.
 */
private fun lexExactlyOneIdentifier(s: String): String? {
    if (s.isEmpty()) return null
    // First char must be letter or underscore
    if (!s[0].isLetter() && s[0] != '_') return null
    // Rest must be alphanumeric or underscore
    for (i in 1 until s.length) {
        if (!s[i].isLetterOrDigit() && s[i] != '_') return null
    }
    return s
}
