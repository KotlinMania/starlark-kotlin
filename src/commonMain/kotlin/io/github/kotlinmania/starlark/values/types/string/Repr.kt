// port-lint: source src/values/types/string/repr.rs
package io.github.kotlinmania.starlark.values.types.string

import io.github.kotlinmania.starlark.unlikely

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


/**
 * Implementation of `repr()`.
 */

/**
 * Check if any byte in the buffer is non-ASCII or need escape.
 */
private fun <V : Vector> chunkNonAsciiOrNeedEscape(chunk: V): Boolean {
    fun or4(a: Vector, b: Vector, c: Vector, d: Vector): Vector {
        val ab = a.or(b)
        val cd = c.or(d)
        return ab.or(cd)
    }

    // Note `cmplt` is signed comparison.
    val anyControlOrNonAscii = chunk.cmplt(chunk.splat(32))
    val any7f = chunk.cmpeq(chunk.splat(0x7f.toByte()))
    val anyDoubleQuote = chunk.cmpeq(chunk.splat('"'.code.toByte()))
    val anyBackslash = chunk.cmpeq(chunk.splat('\\'.code.toByte()))

    val needEscape = or4(anyControlOrNonAscii, any7f, anyDoubleQuote, anyBackslash)
    return needEscape.movemask() != 0u
}

/**
 * Append the escaped representation of a character to the buffer.
 */
private fun pushEscape(toEscape: Char, buffer: StringBuilder) {
    pushEscapeCodePoint(toEscape.code, buffer)
}

/**
 * Append the escaped representation of a Unicode code point to the buffer.
 */
private fun pushEscapeCodePoint(codePoint: Int, buffer: StringBuilder) {
    // Starlark behavior of `repr` is underspecified,
    // so import mix of Starlark spec and PEP-3138.

    when (codePoint) {
        '\n'.code -> buffer.append("\\n")
        '\r'.code -> buffer.append("\\r")
        '\t'.code -> buffer.append("\\t")
        '\\'.code -> buffer.append("\\\\")
        '"'.code -> buffer.append("\\\"")
        // These branches are rare.
        else -> {
            when {
                codePoint < 0x100 -> buffer.append("\\x${codePoint.toString(16).padStart(2, '0')}")
                codePoint < 0x10000 -> buffer.append("\\u${codePoint.toString(16).padStart(4, '0')}")
                else -> buffer.append("\\U${codePoint.toString(16).padStart(8, '0')}")
            }
        }
    }
}

/**
 * Check if a character needs to be escaped in `repr()`.
 */
private fun needEscape(c: Char): Boolean {
    return needEscapeCodePoint(c.code)
}

/**
 * Check if a Unicode code point needs to be escaped in `repr()`.
 */
private fun needEscapeCodePoint(codePoint: Int): Boolean {
    return when {
        codePoint < 0x20 -> true
        codePoint == '"'.code -> true
        codePoint == '\\'.code -> true
        // Note 0x7f needs to be escaped.
        codePoint < 0x7f -> false
        // Now all 8bit characters are covered:
        codePoint <= 0xff -> true
        // Supplementary characters (> 0xFFFF) — always escape.
        // Kotlin Multiplatform doesn't have codepoint-level isLetterOrDigit,
        // so we conservatively escape all supplementary characters.
        codePoint > 0xFFFF -> true
        // No portable `isPrintable` is available.
        // PEP-3138 goes long way defining precisely the Unicode groups which need escaping.
        // We could pick more character groups here,
        // but `isLetterOrDigit` is practically enough for now.
        else -> !Char(codePoint).isLetterOrDigit()
    }
}

/**
 * Convert a string to its repr() representation.
 *
 * This method is surprisingly hot, so we first try and do a fast pass
 * that only works for ASCII-only strings.
 */
internal fun stringRepr(str: String, buffer: StringBuilder) {
    // Simple but definitely correct version — iterates over Unicode code points,
    // handling surrogate pairs correctly for supplementary characters.
    fun loopUnicode(value: String, buffer: StringBuilder) {
        var i = 0
        while (i < value.length) {
            val c = value[i]
            if (c.isHighSurrogate() && i + 1 < value.length && value[i + 1].isLowSurrogate()) {
                // Decode surrogate pair to a full code point
                val hi = c.code
                val lo = value[i + 1].code
                val codePoint = 0x10000 + ((hi - 0xD800) shl 10) + (lo - 0xDC00)
                if (needEscapeCodePoint(codePoint)) {
                    pushEscapeCodePoint(codePoint, buffer)
                } else {
                    buffer.append(c)
                    buffer.append(value[i + 1])
                }
                i += 2
            } else {
                if (needEscape(c)) {
                    pushEscape(c, buffer)
                } else {
                    buffer.append(c)
                }
                i += 1
            }
        }
    }

    // Process the ASCII prefix, bailing out to loopUnicode if we fail
    fun loopAscii(value: String, buffer: StringBuilder) {
        for ((done, x) in value.withIndex()) {
            val codePoint = x.code
            // Note 0x7f is ASCII, but it is rarely used, so handle it in common case
            // to do fewer branches in common case.
            if (unlikely(codePoint >= 0x7f)) {
                // bail out into a unicode-aware version
                loopUnicode(value.substring(done), buffer)
                return
            }

            if (unlikely(needEscape(x))) {
                pushEscape(x, buffer)
            } else {
                buffer.append(x)
            }
        }
    }

    /**
     * SIMD-optimized ASCII loop.
     *
     * The Rust upstream uses portable SIMD with an unaligned vector store via
     * `push_vec_tail` to handle the trailing partial chunk. KMP commonMain has
     * no portable SIMD; this falls back to the byte-at-a-time loop. The
     * upstream `push_vec_tail` helper has no Kotlin call site in this
     * fallback and is intentionally not ported — it would be unreachable code.
     *
     * ```text
     * buffer:   [       buffer.len         |  buffer rem capacity   ]
     * vector:              [  overwriting  |  tail_len  ]
     * ```
     */
    fun <V : Vector> loopAsciiSimd(value: String, buffer: StringBuilder) {
        loopAscii(value, buffer)
    }

    /** Dispatch between SIMD and non-SIMD paths. */
    class Switch(
        private val s: String,
        private val buffer: StringBuilder
    ) : SwitchHaveSimd<Unit> {
        override fun noSimd() {
            loopAscii(s, buffer)
        }

        override fun <V : Vector> simd() {
            loopAsciiSimd<V>(s, buffer)
        }
    }

    buffer.ensureCapacity(buffer.length + 2 + str.length)
    buffer.append('"')
    Switch(str, buffer).switch()
    buffer.append('"')
}
