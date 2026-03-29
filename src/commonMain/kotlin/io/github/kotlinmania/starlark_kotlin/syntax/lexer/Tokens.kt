// port-lint: source src/lexer.rs
package io.github.kotlinmania.starlark_kotlin.syntax.lexer

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

// Token indices 0-65 must match the LALRPOP extern block ordering exactly.
// This ordering is used by GrammarState.ACTION[state * 66 + integer].
sealed class Token {
    // Index 0-2: Indentation and whitespace
    data object Indent : Token()       // 0
    data object Dedent : Token()       // 1
    data object Newline : Token()      // 2

    // Index 3-17: Keywords
    data object And : Token()          // 3
    data object Else : Token()         // 4
    data object Load : Token()         // 5
    data object Break : Token()        // 6
    data object For : Token()          // 7
    data object Not : Token()          // 8
    data object Continue : Token()     // 9
    data object If : Token()           // 10
    data object Or : Token()           // 11
    data object Def : Token()          // 12
    data object In : Token()           // 13
    data object Pass : Token()         // 14
    data object Elif : Token()         // 15
    data object Return : Token()       // 16
    data object Lambda : Token()       // 17

    // Index 18-54: Symbols / operators
    data object Comma : Token()              // 18
    data object Semicolon : Token()          // 19
    data object Colon : Token()              // 20
    data object PlusEqual : Token()          // 21
    data object MinusEqual : Token()         // 22
    data object StarEqual : Token()          // 23
    data object SlashEqual : Token()         // 24
    data object SlashSlashEqual : Token()    // 25
    data object PercentEqual : Token()       // 26
    data object EqualEqual : Token()         // 27
    data object BangEqual : Token()          // 28
    data object LessEqual : Token()          // 29
    data object GreaterEqual : Token()       // 30
    data object StarStar : Token()           // 31
    data object MinusGreater : Token()       // 32
    data object Equal : Token()              // 33
    data object LessThan : Token()           // 34
    data object GreaterThan : Token()        // 35
    data object Minus : Token()              // 36
    data object Plus : Token()               // 37
    data object Star : Token()               // 38
    data object Percent : Token()            // 39
    data object Slash : Token()              // 40
    data object SlashSlash : Token()         // 41
    data object Dot : Token()                // 42
    data object Ampersand : Token()          // 43
    data object Pipe : Token()               // 44
    data object Caret : Token()              // 45
    data object LessLess : Token()           // 46
    data object GreaterGreater : Token()     // 47
    data object Tilde : Token()              // 48
    data object AmpersandEqual : Token()     // 49
    data object PipeEqual : Token()          // 50
    data object CaretEqual : Token()         // 51
    data object LessLessEqual : Token()      // 52
    data object GreaterGreaterEqual : Token() // 53
    data object Ellipsis : Token()           // 54

    // Index 55-60: Brackets
    data object OpeningSquare : Token()      // 55
    data object OpeningCurly : Token()       // 56
    data object OpeningRound : Token()       // 57
    data object ClosingSquare : Token()      // 58
    data object ClosingCurly : Token()       // 59
    data object ClosingRound : Token()       // 60

    // Index 61-65: Literals / identifiers
    data class Identifier(val name: String) : Token()     // 61
    data class IntToken(val value: TokenInt) : Token()     // 62
    data class FloatToken(val value: Double) : Token()     // 63
    data class StringToken(val value: String) : Token()    // 64
    data class FStringToken(val value: TokenFString) : Token() // 65

    // Non-grammar tokens (not in the LR tables, consumed by lexer internally)
    data class Comment(val text: String) : Token()
    data object Reserved : Token()

    /** Convert this token to the integer index used by the LR parser tables. */
    fun toInteger(): Int = when (this) {
        is Indent -> 0
        is Dedent -> 1
        is Newline -> 2
        is And -> 3
        is Else -> 4
        is Load -> 5
        is Break -> 6
        is For -> 7
        is Not -> 8
        is Continue -> 9
        is If -> 10
        is Or -> 11
        is Def -> 12
        is In -> 13
        is Pass -> 14
        is Elif -> 15
        is Return -> 16
        is Lambda -> 17
        is Comma -> 18
        is Semicolon -> 19
        is Colon -> 20
        is PlusEqual -> 21
        is MinusEqual -> 22
        is StarEqual -> 23
        is SlashEqual -> 24
        is SlashSlashEqual -> 25
        is PercentEqual -> 26
        is EqualEqual -> 27
        is BangEqual -> 28
        is LessEqual -> 29
        is GreaterEqual -> 30
        is StarStar -> 31
        is MinusGreater -> 32
        is Equal -> 33
        is LessThan -> 34
        is GreaterThan -> 35
        is Minus -> 36
        is Plus -> 37
        is Star -> 38
        is Percent -> 39
        is Slash -> 40
        is SlashSlash -> 41
        is Dot -> 42
        is Ampersand -> 43
        is Pipe -> 44
        is Caret -> 45
        is LessLess -> 46
        is GreaterGreater -> 47
        is Tilde -> 48
        is AmpersandEqual -> 49
        is PipeEqual -> 50
        is CaretEqual -> 51
        is LessLessEqual -> 52
        is GreaterGreaterEqual -> 53
        is Ellipsis -> 54
        is OpeningSquare -> 55
        is OpeningCurly -> 56
        is OpeningRound -> 57
        is ClosingSquare -> 58
        is ClosingCurly -> 59
        is ClosingRound -> 60
        is Identifier -> 61
        is IntToken -> 62
        is FloatToken -> 63
        is StringToken -> 64
        is FStringToken -> 65
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
