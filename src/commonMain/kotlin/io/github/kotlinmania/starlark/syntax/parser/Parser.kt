// port-lint: source src/syntax/grammar.lalrpop
package io.github.kotlinmania.starlark.syntax.parser

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

import io.github.kotlinmania.starlark.codemap.Pos
import io.github.kotlinmania.starlark.codemap.Span
import io.github.kotlinmania.starlark.codemap.Spanned
import io.github.kotlinmania.starlark.syntax.ast.AstNoPayload
import io.github.kotlinmania.starlark.syntax.ast.StmtP
import io.github.kotlinmania.starlark.syntax.lexer.Token
import io.github.kotlinmania.starlark.syntax.state.ParserState
import io.github.kotlinmania.starlark.typing.EvalException
import io.github.kotlinmania.starlark.typing.StarlarkError

/**
 * LR(1) parser driven by pre-computed ACTION/GOTO tables from GrammarState.
 *
 * Table encoding:
 *  - ACTION[state * 66 + token_index]: positive = shift, negative = reduce, 0 = error
 *  - EOF_ACTION[state]: action when at end-of-file
 *  - Reduce rule: ruleId = -(action) - 1
 *  - Accept: ruleId 296 or 297 (augmented start rules, not in GrammarReducers)
 */
object Parser {
    // Rule ID for the LALRPOP augmented start production (__Starlark = Starlark)
    private const val ACCEPT_RULE = 297

    fun parse(state: ParserState, tokens: Iterator<Triple<Int, Token, Int>>): Spanned<StmtP<AstNoPayload>> {
        val states = mutableListOf(0)
        val symbols = mutableListOf<Triple<Int, GrammarSymbol, Int>>()

        var lookahead: Triple<Int, Token, Int>? = if (tokens.hasNext()) tokens.next() else null

        while (true) {
            val currentState = states.last()

            val action: Int = if (lookahead != null) {
                val (_, token, _) = lookahead
                Grammar.__action(currentState, token.toInteger())
            } else {
                GrammarState.EOF_ACTION[currentState].toInt()
            }

            when {
                action > 0 -> {
                    // Shift: push state (action - 1), push token as symbol.
                    // LALRPOP convention: shift target state = action - 1.
                    val (start, token, end) = lookahead
                        ?: throw parseError(state, currentState, null)
                    states.add(action - 1)
                    symbols.add(Triple(start, token.toSymbol(), end))
                    lookahead = if (tokens.hasNext()) tokens.next() else null
                }

                action < 0 -> {
                    // Reduce (or accept)
                    val ruleId = -(action) - 1

                    if (ruleId == ACCEPT_RULE) {
                        // Accept: extract the result from the symbols stack
                        if (symbols.isEmpty()) {
                            throw parseError(state, currentState, lookahead)
                        }
                        val result = symbols.last().second
                        return (result as GrammarSymbol.Variant9).value
                    }

                    val lookaheadStart = lookahead?.first

                    val (consumed, nt) = GrammarReducers.reduce(
                        ruleId, symbols, state, lookaheadStart
                    )

                    // Pop consumed states
                    for (i in 0 until consumed) {
                        states.removeLast()
                    }

                    // GOTO: find next state based on nonterminal
                    val topState = states.last()
                    val newState = Grammar.__goto(topState, nt)
                    states.add(newState)
                }

                else -> {
                    // Error: action == 0
                    throw parseError(state, currentState, lookahead)
                }
            }
        }
    }

    private fun parseError(
        parserState: ParserState,
        _currentLRState: Int,
        lookahead: Triple<Int, Token, Int>?
    ): EvalException {
        val msg = if (lookahead != null) {
            val (start, token, end) = lookahead
            "Parse error: unexpected ${token}"
        } else {
            "Parse error: unexpected end of file"
        }
        val span = if (lookahead != null) {
            Span(Pos(lookahead.first), Pos(lookahead.third))
        } else {
            Span(Pos(parserState.codemap.fullSpan().end.value), Pos(parserState.codemap.fullSpan().end.value))
        }
        return EvalException.new(StarlarkError(msg), span, parserState.codemap)
    }
}
