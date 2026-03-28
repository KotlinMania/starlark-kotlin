// port-lint: source src/values/types/string/repr.rs
package io.github.kotlinmania.starlark_kotlin.values.types.string

import io.github.kotlinmania.starlark_kotlin.unlikely

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


/**
 * Implementation of `repr()`.
 */

/**
 * Check if any byte in the buffer is non-ASCII or need escape.
 *
 * Note: This function is currently unused in the Kotlin port because SIMD
 * optimizations are not yet implemented. It's kept for future use when
 * platform-specific SIMD support is added.
 */
@Suppress("UNUSED")
private fun <V : Vector> chunkNonAsciiOrNeedEscape(chunk: V): Boolean {
    /**
     * Combine four vectors with OR operation.
     */
    // Note `cmplt` is signed comparison.
    val anyControlOrNonAscii = chunk.cmplt(chunk.splat(32))
    val any7f = chunk.cmpeq(chunk.splat(0x7f.toByte()))
    val anyDoubleQuote = chunk.cmpeq(chunk.splat('"'.code.toByte()))
    val anyBackslash = chunk.cmpeq(chunk.splat('\\'.code.toByte()))

    val needEscape = anyControlOrNonAscii.or(any7f).or(anyDoubleQuote).or(anyBackslash)
    return needEscape.movemask() != 0u
}

/**
 * Append the escaped representation of a character to the buffer.
 */
private fun pushEscape(toEscape: Char, buffer: StringBuilder) {
    // Starlark behavior of `repr` is underspecified,
    // so use mix of Starlark spec and PEP-3138.

    when (toEscape) {
        '\n' -> buffer.append("\\n")
        '\r' -> buffer.append("\\r")
        '\t' -> buffer.append("\\t")
        '\\' -> buffer.append("\\\\")
        '"' -> buffer.append("\\\"")
        // These branches are rare.
        else -> {
            val codePoint = toEscape.code
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
    val codePoint = c.code
    return when {
        codePoint < 0x20 -> true
        c == '"' -> true
        c == '\\' -> true
        // Note 0x7f needs to be escaped.
        codePoint < 0x7f -> false
        // Now all 8bit characters are covered:
        codePoint <= 0xff -> true
        // Rust does not expose `is_printable`.
        // PEP-3138 goes long way defining precisely the Unicode groups which need escaping.
        // We could pick more character groups here,
        // but `is_alphanumeric` is practically enough for now.
        else -> !c.isLetterOrDigit()
    }
}

/**
 * Convert a string to its repr() representation.
 *
 * This method is surprisingly hot, so we first try and do a fast pass
 * that only works for ASCII-only strings.
 */
internal fun stringRepr(str: String, buffer: StringBuilder) {
    // Simple but definitely correct version
    fun loopUnicode(value: String, buffer: StringBuilder) {
        for (x in value) {
            if (needEscape(x)) {
                pushEscape(x, buffer)
            } else {
                buffer.append(x)
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
     * Note: This function is currently not used because the Kotlin port
     * doesn't have SIMD support yet. The SIMD path in the switch below
     * is not enabled, so this function exists for future use.
     */
    @Suppress("UNUSED")
    fun <V : Vector> loopAsciiSimd(value: String, buffer: StringBuilder) {
        // In the Rust version, this function uses SIMD operations to process
        // chunks of bytes at a time. Since Kotlin doesn't have portable SIMD
        // support, we would fall back to loopAscii here.
        //
        // The Rust implementation:
        // - Requires buffer to have enough capacity for value + 1 (trailing quote)
        // - Processes full SIMD chunks first
        // - Handles the tail with overlapping reads
        // - Bails out to loopAscii if any character needs escaping
        //
        // For now, we just delegate to the non-SIMD version.
        loopAscii(value, buffer)
    }

    /**
     * Switch implementation that chooses between SIMD and non-SIMD paths.
     */
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
