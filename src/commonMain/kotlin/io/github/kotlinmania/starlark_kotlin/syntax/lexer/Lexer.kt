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

import io.github.kotlinmania.starlark_kotlin.codemap.CodeMap
import io.github.kotlinmania.starlark_kotlin.codemap.Pos
import io.github.kotlinmania.starlark_kotlin.codemap.Span
import io.github.kotlinmania.starlark_kotlin.syntax.dialect.Dialect
import io.github.kotlinmania.starlark_kotlin.typing.EvalException
import io.github.kotlinmania.starlark_kotlin.typing.StarlarkError

// type Lexeme = Result<(usize, Token, usize), EvalException>
typealias Lexeme = Triple<Int, Token, Int>

sealed class LexemeError(val message: String) {
    data object Indentation : LexemeError("Parse error: incorrect indentation")
    data class InvalidInput(val input: String) : LexemeError("Parse error: invalid input `$input`")
    data object InvalidTab : LexemeError("Parse error: tabs are not allowed")
    data object UnfinishedStringLiteral : LexemeError("Parse error: unfinished string literal")
    data class InvalidEscapeSequence(val seq: String) : LexemeError("Parse error: invalid string escape sequence `$seq`")
    data object EmptyEscapeSequence : LexemeError("Parse error: missing string escape sequence, only saw `\\`")
    data class ReservedKeyword(val keyword: String) : LexemeError("Parse error: cannot use reserved keyword `$keyword`")
    data class StartsZero(val literal: String) : LexemeError("Parse error: integer cannot have leading 0, got `$literal`")
    data class IntParse(val literal: String) : LexemeError("Parse error: failed to parse integer: `$literal`")
    data class CannotParse(val literal: String, val base: Int) : LexemeError("Cannot parse `$literal` as an integer in base $base")
}

class Lexer(
    input: String,
    @Suppress("unused_parameter") dialect: Dialect,
    val codemap: CodeMap,
) : Iterator<Lexeme> {
    private val source: String = input
    private var pos: Int = 0
    private val indentLevels: MutableList<Int> = mutableListOf()
    private val buffer: ArrayDeque<Lexeme> = ArrayDeque()
    private var parens: Int = 0
    private var done: Boolean = false

    init {
        calculateIndent()
    }

    private fun errSpan(msg: LexemeError, start: Int, end: Int): EvalException {
        return EvalException.new(
            StarlarkError(msg.message),
            Span(Pos(start), Pos(end)),
            codemap
        )
    }

    private fun errPos(msg: LexemeError, pos: Int): EvalException {
        return errSpan(msg, pos, pos)
    }

    // --- Character scanning helpers ---

    private fun peek(): Char? = if (pos < source.length) source[pos] else null

    private fun peekAt(offset: Int): Char? {
        val i = pos + offset
        return if (i < source.length) source[i] else null
    }

    private fun advance(): Char? {
        if (pos >= source.length) return null
        val c = source[pos]
        pos++
        return c
    }

    private fun remaining(): String = source.substring(pos)

    private fun startsWith(prefix: String): Boolean = source.startsWith(prefix, pos)

    // --- Indentation handling ---

    private fun calculateIndent() {
        var spaces = 0
        var tabs = 0
        var indentStart = pos
        loop@ while (pos < source.length) {
            when (source[pos]) {
                ' ' -> { spaces++; pos++ }
                '\t' -> { tabs++; pos++ }
                '\n' -> {
                    // Blank line: don't consume the newline itself
                    return
                }
                '\r' -> { pos++ }
                '#' -> {
                    // Comment-only line: skip to newline
                    spaces = 0; tabs = 0
                    val commentStart = pos
                    pos++ // skip '#'
                    while (pos < source.length && source[pos] != '\n') pos++
                    // Comment token (content excludes leading '#')
                    val commentText = source.substring(commentStart + 1, pos).trimEnd('\r')
                    buffer.addLast(Triple(commentStart, Token.Comment(commentText), pos))
                    if (pos < source.length && source[pos] == '\n') {
                        // Don't consume the newline, let the main loop handle it
                    }
                    indentStart = pos
                    if (pos >= source.length) return
                    if (source[pos] == '\n') return
                    continue@loop
                }
                else -> break@loop
            }
        }
        if (pos >= source.length) return

        val indent = spaces + tabs * 8
        if (tabs > 0) {
            buffer.addLast(Triple(pos, Token.Newline, pos)) // will become error
            throw errPos(LexemeError.InvalidTab, pos)
        }
        val now = indentLevels.lastOrNull() ?: 0

        if (indent > now) {
            indentLevels.add(indent)
            buffer.addLast(Triple(indentStart, Token.Indent, pos))
        } else if (indent < now) {
            var dedents = 1
            indentLevels.removeLast()
            while (true) {
                val current = indentLevels.lastOrNull() ?: 0
                if (current == indent) break
                else if (current > indent) {
                    dedents++
                    indentLevels.removeLast()
                } else {
                    throw errSpan(LexemeError.Indentation, indentStart, pos)
                }
            }
            for (i in 0 until dedents) {
                buffer.addLast(Triple(indentStart, Token.Dedent, indentStart))
            }
        }
    }

    // --- String parsing ---

    private fun escapeChar(chars: CharIteratorWithPos, min: Int, max: Int, radix: Int): Char? {
        var value = 0
        var count = 0
        while (count < max) {
            val c = chars.peek() ?: if (count >= min) break else return null
            val digit = c.digitToIntOrNull(radix)
            if (digit == null) {
                if (count >= min) break
                return null
            }
            chars.next()
            count++
            value = value * radix + digit
        }
        return value.toChar()
    }

    private fun escape(chars: CharIteratorWithPos, res: StringBuilder): Boolean {
        val c = chars.next() ?: return false
        when (c) {
            'n' -> res.append('\n')
            'r' -> res.append('\r')
            't' -> res.append('\t')
            'a' -> res.append('\u0007')
            'b' -> res.append('\u0008')
            'f' -> res.append('\u000C')
            'v' -> res.append('\u000B')
            '\n' -> {} // line continuation
            '\r' -> {
                // Windows line ending
                if (chars.peek() != '\n') return false
                chars.next()
            }
            'x' -> {
                val ch = escapeChar(chars, 2, 2, 16) ?: return false
                res.append(ch)
            }
            'u' -> {
                val ch = escapeChar(chars, 4, 4, 16) ?: return false
                res.append(ch)
            }
            'U' -> {
                val ch = escapeChar(chars, 8, 8, 16) ?: return false
                res.append(ch)
            }
            in '0'..'7' -> {
                chars.unnext(c)
                val ch = escapeChar(chars, 1, 3, 8) ?: return false
                res.append(ch)
            }
            '"', '\'', '\\' -> res.append(c)
            else -> {
                res.append('\\')
                res.append(c)
            }
        }
        return true
    }

    /**
     * Parse a string literal. The opening quote(s) have been consumed already.
     * Returns (content, contentStartOffset) relative to the token start.
     */
    private fun parseString(
        stringStart: Int,
        triple: Boolean,
        raw: Boolean,
        quoteChar: Char
    ): Pair<String, Int> {
        val afterQuote = pos
        if (triple) {
            // Skip the two additional quote chars (first was consumed before calling)
            pos += 2
        }
        val contentsStart = pos

        // Fast path: scan for end without escape sequences
        val fastStart = pos
        while (pos < source.length) {
            val c = source[pos]
            if (c == quoteChar) {
                if (triple) {
                    if (pos + 2 < source.length && source[pos + 1] == quoteChar && source[pos + 2] == quoteChar) {
                        val content = source.substring(contentsStart, pos)
                        pos += 3
                        return Pair(content, contentsStart - stringStart)
                    }
                    pos++
                    continue
                } else {
                    val content = source.substring(contentsStart, pos)
                    pos++
                    return Pair(content, contentsStart - stringStart)
                }
            } else if (c == '\\' || c == '\r' || (c == '\n' && !triple)) {
                // Need to fall to slow path
                break
            }
            pos++
        }

        if (pos >= source.length && (pos == fastStart || source[pos - 1] != quoteChar)) {
            throw errSpan(LexemeError.UnfinishedStringLiteral, stringStart, pos)
        }

        // Slow path: character by character with escape handling
        val res = StringBuilder()
        res.append(source.substring(contentsStart, pos))
        val chars = CharIteratorWithPos(source, pos)
        while (chars.hasNext()) {
            val c = chars.next()!!
            if (c == quoteChar) {
                if (triple) {
                    if (chars.peek() == quoteChar && chars.peekAt(1) == quoteChar) {
                        chars.next(); chars.next()
                        // Remove the 2 extra quote chars we accumulated before matching triple
                        if (res.length >= 2) {
                            res.setLength(res.length - 2)
                        }
                        pos = chars.pos
                        return Pair(res.toString(), contentsStart - stringStart)
                    }
                    res.append(c)
                    continue
                } else {
                    pos = chars.pos
                    return Pair(res.toString(), contentsStart - stringStart)
                }
            }
            when {
                c == '\n' && !triple -> {
                    throw errSpan(LexemeError.UnfinishedStringLiteral, stringStart, chars.pos)
                }
                c == '\r' -> {} // ignore \r in all modes
                c == '\\' -> {
                    if (raw) {
                        val next = chars.next() ?: break
                        if (next != '\'' && next != '"') {
                            res.append('\\')
                        }
                        res.append(next)
                    } else {
                        val escapeStart = chars.pos - 1
                        if (!escape(chars, res)) {
                            val bad = source.substring(escapeStart, chars.pos)
                            throw errSpan(
                                if (bad.isEmpty()) LexemeError.EmptyEscapeSequence
                                else LexemeError.InvalidEscapeSequence(bad),
                                stringStart + escapeStart,
                                stringStart + chars.pos
                            )
                        }
                    }
                }
                else -> res.append(c)
            }
        }
        throw errSpan(LexemeError.UnfinishedStringLiteral, stringStart, pos)
    }

    // --- Integer parsing ---

    private fun parseInt(literal: String, start: Int, end: Int, radix: Int): Lexeme {
        try {
            val value = TokenInt.fromStrRadix(literal, radix)
            return Triple(start, Token.IntToken(value), end)
        } catch (e: Exception) {
            throw errSpan(LexemeError.IntParse(literal), start, end)
        }
    }

    // --- Main tokenization ---

    private fun scanToken(): Lexeme? {
        // Skip whitespace (spaces only - tabs are handled by indentation)
        while (pos < source.length && source[pos] == ' ') pos++

        // Skip escaped newlines
        while (pos < source.length && startsWith("\\\n")) { pos += 2 }
        while (pos < source.length && startsWith("\\\r\n")) { pos += 3 }

        if (pos >= source.length) return null

        val start = pos
        val c = source[pos]

        // Newline
        if (c == '\n' || (c == '\r' && peekAt(1) == '\n')) {
            if (c == '\r') pos++ // skip \r
            pos++ // skip \n
            if (parens == 0) {
                val newlineEnd = pos
                calculateIndent()
                return Triple(start, Token.Newline, newlineEnd)
            }
            // Inside parens: skip newlines
            return scanToken()
        }

        // Tab outside indentation context
        if (c == '\t') {
            pos++
            while (pos < source.length && source[pos] == '\t') pos++
            throw errSpan(LexemeError.InvalidTab, start, pos)
        }

        // Comment
        if (c == '#') {
            pos++
            while (pos < source.length && source[pos] != '\n' && source[pos] != '\r') pos++
            val text = source.substring(start + 1, pos).trimEnd('\r')
            return Triple(start, Token.Comment(text), pos)
        }

        // String literals (check for triple-quoted by peeking ahead)
        if (c == '\'' || c == '"') {
            pos++ // skip opening quote
            val triple = peek() == c && peekAt(1) == c
            val (content, contentOffset) = parseString(start, triple, false, c)
            return Triple(start, Token.StringToken(content), pos)
        }
        if (c == 'r' && (peekAt(1) == '\'' || peekAt(1) == '"')) {
            pos++ // skip 'r'
            val quote = source[pos]
            pos++ // skip opening quote
            val triple = peek() == quote && peekAt(1) == quote
            val (content, _) = parseString(start, triple, true, quote)
            return Triple(start, Token.StringToken(content), pos)
        }
        // F-strings
        if (c == 'f' && (peekAt(1) == '\'' || peekAt(1) == '"')) {
            pos++ // skip 'f'
            val quote = source[pos]
            pos++ // skip opening quote
            val triple = peek() == quote && peekAt(1) == quote
            val spanLen = if (triple) 4 else 2 // "f" + quote(s)
            val (content, contentStartOffset) = parseString(start, triple, false, quote)
            return Triple(start, Token.FStringToken(TokenFString(content, contentStartOffset + spanLen)), pos)
        }
        if (c == 'f' && peekAt(1) == 'r' && (peekAt(2) == '\'' || peekAt(2) == '"')) {
            pos += 2 // skip 'fr'
            val quote = source[pos]
            pos++ // skip opening quote
            val triple = peek() == quote && peekAt(1) == quote
            val spanLen = if (triple) 5 else 3 // "fr" + quote(s)
            val (content, contentStartOffset) = parseString(start, triple, true, quote)
            return Triple(start, Token.FStringToken(TokenFString(content, contentStartOffset + spanLen)), pos)
        }

        // Numbers
        if (c in '0'..'9') {
            return scanNumber(start)
        }
        if (c == '.' && peekAt(1) != null && peekAt(1)!! in '0'..'9') {
            return scanFloat(start)
        }

        // Identifiers and keywords (string prefix cases r/f/fr already handled above)
        if (c.isLetter() || c == '_') {
            pos++
            while (pos < source.length && (source[pos].isLetterOrDigit() || source[pos] == '_')) pos++
            val ident = source.substring(start, pos)
            // Check for keywords
            val kw = keywordToken(ident)
            if (kw != null) return Triple(start, kw, pos)
            // Check for reserved keywords
            if (ident in RESERVED_KEYWORDS) {
                throw errSpan(LexemeError.ReservedKeyword(ident), start, pos)
            }
            return Triple(start, Token.Identifier(ident), pos)
        }

        // Operators and symbols (multi-char first, then single-char)
        pos++
        return when (c) {
            ',' -> Triple(start, Token.Comma, pos)
            ';' -> Triple(start, Token.Semicolon, pos)
            ':' -> Triple(start, Token.Colon, pos)
            '~' -> Triple(start, Token.Tilde, pos)
            '(' -> { parens++; Triple(start, Token.OpeningRound, pos) }
            ')' -> { parens--; Triple(start, Token.ClosingRound, pos) }
            '[' -> { parens++; Triple(start, Token.OpeningSquare, pos) }
            ']' -> { parens--; Triple(start, Token.ClosingSquare, pos) }
            '{' -> { parens++; Triple(start, Token.OpeningCurly, pos) }
            '}' -> { parens--; Triple(start, Token.ClosingCurly, pos) }
            '+' -> if (peek() == '=') { pos++; Triple(start, Token.PlusEqual, pos) }
                   else Triple(start, Token.Plus, pos)
            '-' -> when (peek()) {
                '=' -> { pos++; Triple(start, Token.MinusEqual, pos) }
                '>' -> { pos++; Triple(start, Token.MinusGreater, pos) }
                else -> Triple(start, Token.Minus, pos)
            }
            '*' -> when (peek()) {
                '*' -> { pos++; Triple(start, Token.StarStar, pos) }
                '=' -> { pos++; Triple(start, Token.StarEqual, pos) }
                else -> Triple(start, Token.Star, pos)
            }
            '/' -> when (peek()) {
                '/' -> {
                    pos++
                    if (peek() == '=') { pos++; Triple(start, Token.SlashSlashEqual, pos) }
                    else Triple(start, Token.SlashSlash, pos)
                }
                '=' -> { pos++; Triple(start, Token.SlashEqual, pos) }
                else -> Triple(start, Token.Slash, pos)
            }
            '%' -> if (peek() == '=') { pos++; Triple(start, Token.PercentEqual, pos) }
                   else Triple(start, Token.Percent, pos)
            '=' -> if (peek() == '=') { pos++; Triple(start, Token.EqualEqual, pos) }
                   else Triple(start, Token.Equal, pos)
            '!' -> if (peek() == '=') { pos++; Triple(start, Token.BangEqual, pos) }
                   else throw errSpan(LexemeError.InvalidInput("!"), start, pos)
            '<' -> when (peek()) {
                '=' -> { pos++; Triple(start, Token.LessEqual, pos) }
                '<' -> {
                    pos++
                    if (peek() == '=') { pos++; Triple(start, Token.LessLessEqual, pos) }
                    else Triple(start, Token.LessLess, pos)
                }
                else -> Triple(start, Token.LessThan, pos)
            }
            '>' -> when (peek()) {
                '=' -> { pos++; Triple(start, Token.GreaterEqual, pos) }
                '>' -> {
                    pos++
                    if (peek() == '=') { pos++; Triple(start, Token.GreaterGreaterEqual, pos) }
                    else Triple(start, Token.GreaterGreater, pos)
                }
                else -> Triple(start, Token.GreaterThan, pos)
            }
            '&' -> if (peek() == '=') { pos++; Triple(start, Token.AmpersandEqual, pos) }
                   else Triple(start, Token.Ampersand, pos)
            '|' -> if (peek() == '=') { pos++; Triple(start, Token.PipeEqual, pos) }
                   else Triple(start, Token.Pipe, pos)
            '^' -> if (peek() == '=') { pos++; Triple(start, Token.CaretEqual, pos) }
                   else Triple(start, Token.Caret, pos)
            '.' -> {
                if (peek() == '.' && peekAt(1) == '.') {
                    pos += 2
                    Triple(start, Token.Ellipsis, pos)
                } else {
                    Triple(start, Token.Dot, pos)
                }
            }
            else -> throw errSpan(LexemeError.InvalidInput(c.toString()), start, pos)
        }
    }

    private fun scanNumber(start: Int): Lexeme {
        // Check for hex, octal, binary prefixes
        if (source[pos] == '0' && pos + 1 < source.length) {
            when (source[pos + 1]) {
                'x', 'X' -> {
                    pos += 2
                    val digitStart = pos
                    while (pos < source.length && source[pos].isHexDigit()) pos++
                    if (pos == digitStart) throw errSpan(LexemeError.IntParse("0${source[pos-1]}"), start, pos)
                    return parseInt(source.substring(digitStart, pos), start, pos, 16)
                }
                'o', 'O' -> {
                    pos += 2
                    val digitStart = pos
                    while (pos < source.length && source[pos] in '0'..'7') pos++
                    if (pos == digitStart) throw errSpan(LexemeError.IntParse("0${source[pos-1]}"), start, pos)
                    return parseInt(source.substring(digitStart, pos), start, pos, 8)
                }
                'b', 'B' -> {
                    pos += 2
                    val digitStart = pos
                    while (pos < source.length && source[pos] in '0'..'1') pos++
                    if (pos == digitStart) throw errSpan(LexemeError.IntParse("0${source[pos-1]}"), start, pos)
                    return parseInt(source.substring(digitStart, pos), start, pos, 2)
                }
            }
        }

        // Decimal integer or float
        while (pos < source.length && source[pos] in '0'..'9') pos++

        // Check for float
        if (pos < source.length && (source[pos] == '.' || source[pos] == 'e' || source[pos] == 'E')) {
            return scanFloatRest(start)
        }

        val literal = source.substring(start, pos)
        if (literal.length > 1 && literal[0] == '0') {
            throw errSpan(LexemeError.StartsZero(literal), start, pos)
        }
        return parseInt(literal, start, pos, 10)
    }

    private fun scanFloat(start: Int): Lexeme {
        // Starts with '.', digits already checked
        pos++ // skip '.'
        while (pos < source.length && source[pos] in '0'..'9') pos++
        // Optional exponent
        if (pos < source.length && (source[pos] == 'e' || source[pos] == 'E')) {
            pos++
            if (pos < source.length && (source[pos] == '+' || source[pos] == '-')) pos++
            while (pos < source.length && source[pos] in '0'..'9') pos++
        }
        val literal = source.substring(start, pos)
        val value = literal.toDoubleOrNull() ?: throw errSpan(LexemeError.IntParse(literal), start, pos)
        return Triple(start, Token.FloatToken(value), pos)
    }

    private fun scanFloatRest(start: Int): Lexeme {
        // We've consumed digits, now handle '.', 'e'/'E'
        if (pos < source.length && source[pos] == '.') {
            pos++
            while (pos < source.length && source[pos] in '0'..'9') pos++
        }
        if (pos < source.length && (source[pos] == 'e' || source[pos] == 'E')) {
            pos++
            if (pos < source.length && (source[pos] == '+' || source[pos] == '-')) pos++
            while (pos < source.length && source[pos] in '0'..'9') pos++
        }
        val literal = source.substring(start, pos)
        val value = literal.toDoubleOrNull() ?: throw errSpan(LexemeError.IntParse(literal), start, pos)
        return Triple(start, Token.FloatToken(value), pos)
    }

    private fun Char.isHexDigit(): Boolean = this in '0'..'9' || this in 'a'..'f' || this in 'A'..'F'

    // --- Iterator implementation ---

    override fun hasNext(): Boolean {
        if (buffer.isNotEmpty()) return true
        if (done) return false
        // Try to produce the next token
        try {
            val next = produceNext()
            if (next != null) {
                buffer.addFirst(next)
                return true
            }
        } catch (_: EvalException) {
            // Error tokens terminate iteration
        }
        return false
    }

    override fun next(): Lexeme {
        while (true) {
            if (buffer.isNotEmpty()) {
                val item = buffer.removeFirst()
                // Skip comment tokens - they don't go to the parser
                if (item.second is Token.Comment) continue
                return item
            }
            if (done) throw NoSuchElementException("No more tokens")
            val next = produceNext() ?: throw NoSuchElementException("No more tokens")
            // Skip comment tokens
            if (next.second is Token.Comment) continue
            return next
        }
    }

    /**
     * Produce the next token or null if at EOF.
     * May add additional tokens to the buffer (e.g. DEDENT at EOF).
     * Throws EvalException on lexing errors.
     */
    private fun produceNext(): Lexeme? {
        // Drain buffered tokens first
        if (buffer.isNotEmpty()) return buffer.removeFirst()

        if (done) return null

        val token = scanToken()
        if (token == null) {
            // EOF: emit final newline + remaining dedents
            done = true
            val eofPos = source.length
            for (i in indentLevels.indices) {
                buffer.addLast(Triple(eofPos, Token.Dedent, eofPos))
            }
            indentLevels.clear()
            return Triple(eofPos, Token.Newline, eofPos)
        }
        return token
    }
}

/** Helper for character-by-character iteration with position tracking and unnext. */
private class CharIteratorWithPos(private val source: String, startPos: Int) {
    var pos: Int = startPos
        private set

    fun hasNext(): Boolean = pos < source.length

    fun peek(): Char? = if (pos < source.length) source[pos] else null

    fun peekAt(offset: Int): Char? {
        val i = pos + offset
        return if (i < source.length) source[i] else null
    }

    fun next(): Char? {
        if (pos >= source.length) return null
        val c = source[pos]
        pos++
        return c
    }

    fun unnext(c: Char) {
        pos--
    }
}

fun lexExactlyOneIdentifier(s: String): String? {
    val trimmed = s.trim()
    if (trimmed.isEmpty()) return null
    // Must start with letter or underscore
    if (!trimmed[0].isLetter() && trimmed[0] != '_') return null
    // Must be all identifier chars
    if (!trimmed.all { it.isLetterOrDigit() || it == '_' }) return null
    // Must not be a keyword or reserved word
    if (keywordToken(trimmed) != null) return null
    if (trimmed in RESERVED_KEYWORDS) return null
    return trimmed
}
