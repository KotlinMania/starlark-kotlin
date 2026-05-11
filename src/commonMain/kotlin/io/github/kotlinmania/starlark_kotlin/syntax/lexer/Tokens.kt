<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/syntax/lexer/Tokens.kt
// port-lint: source ../starlark_syntax/src/lexer.rs
package io.github.kotlinmania.starlark.syntax.lexer
=======
// port-lint: source src/lexer.rs
package io.github.kotlinmania.starlark_kotlin.syntax.lexer
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/syntax/lexer/Tokens.kt

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

import com.ionspin.kotlin.bignum.integer.BigInteger

sealed class TokenInt {
    data class I32(val value: Int) : TokenInt()
    /** Only if larger than `i32`. */
    data class BigInt(val value: BigInteger) : TokenInt()

    override fun toString(): String = when (this) {
        is I32 -> value.toString()
        is BigInt -> value.toString()
    }

    companion object {
        fun fromStrRadix(s: String, base: Int): TokenInt {
            val i = s.toIntOrNull(base)
            if (i != null) return I32(i)
            return BigInt(BigInteger.parseString(s, base))
        }
    }
}

data class TokenFString(
    /** The content of this TokenFString. */
    val content: String,
    /** Relative to the token, where does the actual string content start? */
    val contentStartOffset: Int
)

// Token indices 0-65 must match the LALRPOP-generated __token_to_integer mapping exactly.
// This ordering is used by GrammarState.ACTION[state * 66 + integer].
// See tmp/target/debug/build/starlark_syntax-.../out/syntax/grammar.rs __token_to_integer.
sealed class Token {
    // LALRPOP indices: Newline=0, symbols 1-34, Dedent=35, literals 36-41,
    //                  brackets 42-43, operators 44-45, keywords 46-60,
    //                  OpeningCurly=61, Pipe=62, PipeEqual=63, ClosingCurly=64, Tilde=65
    data object Newline : Token()                          // 0
    data object BangEqual : Token()                        // 1
    data object Percent : Token()                          // 2
    data object PercentEqual : Token()                     // 3
    data object Ampersand : Token()                        // 4
    data object AmpersandEqual : Token()                   // 5
    data object OpeningRound : Token()                     // 6
    data object ClosingRound : Token()                     // 7
    data object Star : Token()                             // 8
    data object StarStar : Token()                         // 9
    data object StarEqual : Token()                        // 10
    data object Plus : Token()                             // 11
    data object PlusEqual : Token()                        // 12
    data object Comma : Token()                            // 13
    data object Minus : Token()                            // 14
    data object MinusEqual : Token()                       // 15
    data object MinusGreater : Token()                     // 16
    data object Dot : Token()                              // 17
    data object Ellipsis : Token()                         // 18
    data object Slash : Token()                            // 19
    data object SlashSlash : Token()                       // 20
    data object SlashSlashEqual : Token()                  // 21
    data object SlashEqual : Token()                       // 22
    data object Colon : Token()                            // 23
    data object Semicolon : Token()                        // 24
    data object LessThan : Token()                         // 25
    data object LessLess : Token()                         // 26
    data object LessLessEqual : Token()                    // 27
    data object LessEqual : Token()                        // 28
    data object Equal : Token()                            // 29
    data object EqualEqual : Token()                       // 30
    data object GreaterThan : Token()                      // 31
    data object GreaterEqual : Token()                     // 32
    data object GreaterGreater : Token()                   // 33
    data object GreaterGreaterEqual : Token()              // 34
    data object Dedent : Token()                           // 35
    data class FloatToken(val value: Double) : Token()     // 36
    data class FStringToken(val value: TokenFString) : Token() // 37
    data class Identifier(val name: String) : Token()      // 38
    data object Indent : Token()                           // 39
    data class IntToken(val value: TokenInt) : Token()     // 40
    data class StringToken(val value: String) : Token()    // 41
    data object OpeningSquare : Token()                    // 42
    data object ClosingSquare : Token()                    // 43
    data object Caret : Token()                            // 44
    data object CaretEqual : Token()                       // 45
    data object And : Token()                              // 46
    data object Break : Token()                            // 47
    data object Continue : Token()                         // 48
    data object Def : Token()                              // 49
    data object Elif : Token()                             // 50
    data object Else : Token()                             // 51
    data object For : Token()                              // 52
    data object If : Token()                               // 53
    data object In : Token()                               // 54
    data object Lambda : Token()                           // 55
    data object Load : Token()                             // 56
    data object Not : Token()                              // 57
    data object Or : Token()                               // 58
    data object Pass : Token()                             // 59
    data object Return : Token()                           // 60
    data object OpeningCurly : Token()                     // 61
    data object Pipe : Token()                             // 62
    data object PipeEqual : Token()                        // 63
    data object ClosingCurly : Token()                     // 64
    data object Tilde : Token()                            // 65

    // Non-grammar tokens (not in the LR tables, consumed by lexer internally)
    data class Comment(val text: String) : Token()
    data object Reserved : Token()

    /** Convert this token to the integer index used by the LR parser tables.
     *  These indices MUST match the LALRPOP-generated __token_to_integer mapping
     *  in grammar.rs exactly. */
    fun toInteger(): Int = when (this) {
        is Newline -> 0
        is BangEqual -> 1
        is Percent -> 2
        is PercentEqual -> 3
        is Ampersand -> 4
        is AmpersandEqual -> 5
        is OpeningRound -> 6
        is ClosingRound -> 7
        is Star -> 8
        is StarStar -> 9
        is StarEqual -> 10
        is Plus -> 11
        is PlusEqual -> 12
        is Comma -> 13
        is Minus -> 14
        is MinusEqual -> 15
        is MinusGreater -> 16
        is Dot -> 17
        is Ellipsis -> 18
        is Slash -> 19
        is SlashSlash -> 20
        is SlashSlashEqual -> 21
        is SlashEqual -> 22
        is Colon -> 23
        is Semicolon -> 24
        is LessThan -> 25
        is LessLess -> 26
        is LessLessEqual -> 27
        is LessEqual -> 28
        is Equal -> 29
        is EqualEqual -> 30
        is GreaterThan -> 31
        is GreaterEqual -> 32
        is GreaterGreater -> 33
        is GreaterGreaterEqual -> 34
        is Dedent -> 35
        is FloatToken -> 36
        is FStringToken -> 37
        is Identifier -> 38
        is Indent -> 39
        is IntToken -> 40
        is StringToken -> 41
        is OpeningSquare -> 42
        is ClosingSquare -> 43
        is Caret -> 44
        is CaretEqual -> 45
        is And -> 46
        is Break -> 47
        is Continue -> 48
        is Def -> 49
        is Elif -> 50
        is Else -> 51
        is For -> 52
        is If -> 53
        is In -> 54
        is Lambda -> 55
        is Load -> 56
        is Not -> 57
        is Or -> 58
        is Pass -> 59
        is Return -> 60
        is OpeningCurly -> 61
        is Pipe -> 62
        is PipeEqual -> 63
        is ClosingCurly -> 64
        is Tilde -> 65
        is Comment -> error("Comment tokens should not reach the parser")
        is Reserved -> error("Reserved tokens should not reach the parser")
    }

    /** Wrap this token into the appropriate GrammarSymbol variant for the parser stack. */
    fun toSymbol(): io.github.kotlinmania.starlark_kotlin.syntax.parser.GrammarSymbol = when (this) {
        is FloatToken -> io.github.kotlinmania.starlark_kotlin.syntax.parser.GrammarSymbol.Variant1(value)
        is FStringToken -> io.github.kotlinmania.starlark_kotlin.syntax.parser.GrammarSymbol.Variant2(value)
        is Identifier -> io.github.kotlinmania.starlark_kotlin.syntax.parser.GrammarSymbol.Variant3(name)
        is IntToken -> io.github.kotlinmania.starlark_kotlin.syntax.parser.GrammarSymbol.Variant4(value)
        is StringToken -> io.github.kotlinmania.starlark_kotlin.syntax.parser.GrammarSymbol.Variant3(value)
        else -> io.github.kotlinmania.starlark_kotlin.syntax.parser.GrammarSymbol.Variant0(this)
    }

    override fun toString(): String = when (this) {
        is Indent -> "new indentation block"
        is Dedent -> "end of indentation block"
        is Newline -> "new line"
        is And -> "keyword 'and'"
        is Else -> "keyword 'else'"
        is Load -> "keyword 'load'"
        is Break -> "keyword 'break'"
        is For -> "keyword 'for'"
        is Not -> "keyword 'not'"
        is Continue -> "keyword 'continue'"
        is If -> "keyword 'if'"
        is Or -> "keyword 'or'"
        is Def -> "keyword 'def'"
        is In -> "keyword 'in'"
        is Pass -> "keyword 'pass'"
        is Elif -> "keyword 'elif'"
        is Return -> "keyword 'return'"
        is Lambda -> "keyword 'lambda'"
        is Comma -> "symbol ','"
        is Semicolon -> "symbol ';'"
        is Colon -> "symbol ':'"
        is PlusEqual -> "symbol '+='"
        is MinusEqual -> "symbol '-='"
        is StarEqual -> "symbol '*='"
        is SlashEqual -> "symbol '/='"
        is SlashSlashEqual -> "symbol '//='"
        is PercentEqual -> "symbol '%='"
        is EqualEqual -> "symbol '=='"
        is BangEqual -> "symbol '!='"
        is LessEqual -> "symbol '<='"
        is GreaterEqual -> "symbol '>='"
        is StarStar -> "symbol '**'"
        is MinusGreater -> "symbol '->'"
        is Equal -> "symbol '='"
        is LessThan -> "symbol '<'"
        is GreaterThan -> "symbol '>'"
        is Minus -> "symbol '-'"
        is Plus -> "symbol '+'"
        is Star -> "symbol '*'"
        is Percent -> "symbol '%'"
        is Slash -> "symbol '/'"
        is SlashSlash -> "symbol '//'"
        is Dot -> "symbol '.'"
        is Ampersand -> "symbol '&'"
        is Pipe -> "symbol '|'"
        is Caret -> "symbol '^'"
        is LessLess -> "symbol '<<'"
        is GreaterGreater -> "symbol '>>'"
        is Tilde -> "symbol '~'"
        is AmpersandEqual -> "symbol '&='"
        is PipeEqual -> "symbol '|='"
        is CaretEqual -> "symbol '^='"
        is LessLessEqual -> "symbol '<<='"
        is GreaterGreaterEqual -> "symbol '>>='"
        is Ellipsis -> "symbol '...'"
        is OpeningSquare -> "symbol '['"
        is OpeningCurly -> "symbol '{'"
        is OpeningRound -> "symbol '('"
        is ClosingSquare -> "symbol ']'"
        is ClosingCurly -> "symbol '}'"
        is ClosingRound -> "symbol ')'"
        is Reserved -> "reserved keyword"
        is Identifier -> "identifier '$name'"
        is IntToken -> "integer literal '$value'"
        is FloatToken -> "float literal '$value'"
        is StringToken -> "string literal \"$value\""
        is FStringToken -> "f-string \"${value.content}\""
        is Comment -> "comment '$text'"
    }
}

/** The set of reserved keywords that cannot be used as identifiers. */
val RESERVED_KEYWORDS = setOf(
    "as", "assert", "async", "await", "class", "del", "except",
    "finally", "from", "global", "import", "is", "nonlocal",
    "raise", "try", "while", "with", "yield"
)

/** Map keyword strings to their Token variants. */
fun keywordToken(s: String): Token? = when (s) {
    "and" -> Token.And
    "break" -> Token.Break
    "continue" -> Token.Continue
    "def" -> Token.Def
    "elif" -> Token.Elif
    "else" -> Token.Else
    "for" -> Token.For
    "if" -> Token.If
    "in" -> Token.In
    "lambda" -> Token.Lambda
    "load" -> Token.Load
    "not" -> Token.Not
    "or" -> Token.Or
    "pass" -> Token.Pass
    "return" -> Token.Return
    else -> null
}

class TokenString
