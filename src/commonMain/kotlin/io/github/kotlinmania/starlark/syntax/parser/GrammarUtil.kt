// port-lint: source ../starlarkSyntax/src/syntax/grammarUtil.rs
package io.github.kotlinmania.starlark.syntax.parser

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

/** Code called by the parser to handle complex cases not handled by the grammar. */

import io.github.kotlinmania.starlark.codemap.CodeMap
import io.github.kotlinmania.starlark.codemap.Pos
import io.github.kotlinmania.starlark.codemap.Span
import io.github.kotlinmania.starlark.codemap.Spanned
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

private enum class GrammarUtilError(val message: String) {
    InvalidLhs("left-hand-side of assignment must take the form `a`, `a.b` or `a[b]`"),
    InvalidModifyLhs("left-hand-side of modifying assignment cannot be a list or tuple"),
    TypeAnnotationOnAssignOp("type annotations not allowed on augmented assignments"),
    TypeAnnotationOnTupleAssign("type annotations not allowed on multiple assignments"),
    LoadRequiresAtLeastTwoArguments("`load` statement requires at least two arguments"),
}

private enum class DialectError(val message: String) {
    Types("type annotations are not allowed in this dialect"),
}

object GrammarUtil {
    /** Ensure we produce normalised Statements, rather than singleton Statements. */
    fun statements(xs: List<Spanned<StmtP<AstNoPayload>>>, begin: Int, end: Int): Spanned<StmtP<AstNoPayload>> {
        return if (xs.size == 1) {
            xs[0]
        } else {
            StmtP.Statements<AstNoPayload>(xs).ast(begin, end)
        }
    }

    fun checkAssign(codemap: CodeMap, x: Spanned<ExprP<AstNoPayload>>): Spanned<AssignTargetP<AstNoPayload>> {
        val node: AssignTargetP<AstNoPayload> = when (val expr = x.node) {
            is ExprP.Tuple -> AssignTargetP.Tuple(
                expr.elements.map { checkAssign(codemap, it) }
            )
            is ExprP.ListExpr -> AssignTargetP.Tuple(
                expr.elements.map { checkAssign(codemap, it) }
            )
            is ExprP.Dot -> AssignTargetP.Dot(expr.expr, expr.field)
            is ExprP.Index -> AssignTargetP.Index(expr.expr, expr.index)
            is ExprP.Identifier<AstNoPayload, *> -> {
                val ident = expr.ident as Spanned<IdentP<AstNoPayload, Unit>>
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

    fun checkAssignment(
        codemap: CodeMap,
        lhs: Spanned<ExprP<AstNoPayload>>,
        ty: Spanned<TypeExprP<AstNoPayload, Unit>>?,
        op: AssignOp?,
        rhs: Spanned<ExprP<AstNoPayload>>
    ): StmtP<AstNoPayload> {
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

    fun checkLoad0(module: Spanned<String>, parserState: ParserState): StmtP<AstNoPayload> {
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

    fun checkLoad(
        module: Spanned<String>,
        args: List<Pair<Pair<Spanned<AssignIdentP<AstNoPayload, Unit>>, Spanned<String>>, Spanned<Comma>>>,
        last: Pair<Spanned<AssignIdentP<AstNoPayload, Unit>>, Spanned<String>>?,
        parserState: ParserState
    ): StmtP<AstNoPayload> {
        if (args.isEmpty() && last == null) {
            return checkLoad0(module, parserState)
        }

        val loadArgs = args.map { (localTheir, comma) ->
            val (local, their) = localTheir
            LoadArgP(
                local = local,
                their = their,
                comma = comma as Spanned<io.github.kotlinmania.starlark.syntax.ast.Comma>?
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

    fun fstring(
        fstring: TokenFString,
        begin: Int,
        end: Int,
        parserState: ParserState
    ): Spanned<FStringP<AstNoPayload>> {
        if (!parserState.dialect.enableFStrings) {
            parserState.error(
                Span(Pos(begin), Pos(end)),
                "Your Starlark dialect must enable f-strings to use them"
            )
        }

        val content = fstring.content
        val contentStartOffset = fstring.contentStartOffset

        val format = StringBuilder(content.length)
        val expressions = mutableListOf<Spanned<ExprP<AstNoPayload>>>()

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

    fun dialectCheckType(
        state: ParserState,
        x: Spanned<ExprP<AstNoPayload>>
    ): Spanned<TypeExprP<AstNoPayload, Unit>> {
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

    fun checkCall(
        e: Spanned<ExprP<AstNoPayload>>,
        a: List<Spanned<ArgumentP<AstNoPayload>>>,
        state: ParserState
    ): ExprP<AstNoPayload> {
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
 * Port of starlarkSyntax::lexer::lexExactlyOneIdentifier.
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
