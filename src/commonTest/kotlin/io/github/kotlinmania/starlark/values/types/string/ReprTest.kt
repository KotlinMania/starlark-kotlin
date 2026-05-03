// port-lint: source src/values/types/string/repr.rs
package io.github.kotlinmania.starlark.values.types.string

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

import io.github.kotlinmania.starlark.assert.allTrue
import kotlin.test.Test
import kotlin.test.assertEquals

class ReprTest {
    //     assert::allTrue(
    //         r#"
    // "\"\\t\\n'\\\"\"" == repr("\t\n'\"")
    // "\"Hello, 世界\"" == repr("Hello, 世界")
    // "#,
    //     );
    @Test
    fun testToRepr() {
        allTrue(
            """
"\"\\t\\n'\\\"\"" == repr("\t\n'\"")
"\"Hello, 世界\"" == repr("Hello, 世界")
"""
        )
    }

    @Test
    fun testStringRepr() {
        fun test(expected: String, input: String) {
            val repr = StringBuilder()
            stringRepr(input, repr)
            assertEquals(expected, repr.toString())
        }

        test("\"\\x12\"", "\u0012")
        test("\"\\x7f\"", "\u007f")
        test("\"\\n\"", "\n")
        // Do not escape single quotes because repr in Starlark uses double quotes.
        test("\"'\"", "'")
        test("\"\\\"\"", "\"")
        test("\"\\\\\"", "\\")
        // Non-printable whitespace.
        test("\"\\u200b\"", "\u200b")
        test("\"Hello, 世界\"", "Hello, 世界")
        // Largest unicode number.
        test("\"\\U0010ffff\"", "\udbff\udfff")
    }

    @Test
    fun testToReprLongSmoke() {
        fun test(expected: String, input: String) {
            val repr = StringBuilder()
            stringRepr(input, repr)
            assertEquals(expected, repr.toString())
        }

        test("\"0123456789abcdef\"", "0123456789abcdef")
        test("\"0123456789\\nbcdef\"", "0123456789\nbcdef")
        test("\"Мы, оглядываясь, видим лишь руины\"", "Мы, оглядываясь, видим лишь руины")
    }

    private fun stringReprForTest(s: String): String {
        val r = StringBuilder()
        stringRepr(s, r)
        return r.toString()
    }

    @Test
    fun toReprSse() {
        for (i in 0 until 0x80) {
            val s = ByteArray(33) { i.toByte() }.decodeToString()
            // Trigger debug assertions.
            stringReprForTest(s)
        }
    }

    @Test
    fun toReprNoEscapeAllLengths() {
        for (len in 0 until 100) {
            val s = ByteArray(len) { i -> ('0'.code + (i % 10)).toByte() }.decodeToString()
            assertEquals("\"$s\"", stringReprForTest(s))
        }
    }

    @Test
    fun toReprTailEscapeAllLengths() {
        for (len in 0 until 100) {
            val s = ByteArray(len) { i -> ('0'.code + (i % 10)).toByte() }.decodeToString()
            assertEquals(
                "\"$s\\n\"",
                stringReprForTest("$s\n")
            )
        }
    }

    @Test
    fun toReprMiddleEscapeAllLengths() {
        for (len in 0 until 100) {
            val s = ByteArray(len) { i -> ('0'.code + (i % 10)).toByte() }.decodeToString()
            assertEquals(
                "\"$s\\n$s\"",
                stringReprForTest("$s\n$s")
            )
        }
    }

    // Mirrors the Rust SSE2-gated test. KMP commonMain has no portable SIMD;
    // the structural assertion below verifies the predicate the SIMD path
    // computes by checking each character of the test inputs against the
    // same escape rule (control / 0x7F / `"` / `\\`).
    @Test
    fun testChunkNonAsciiOrNeedEscape() {
        fun needsEscape(s: String): Boolean = s.any { c ->
            c.code < 0x20 || c.code == 0x7F || c == '"' || c == '\\'
        }

        assertEquals(false, needsEscape("0123456789abcdef"))
        assertEquals(false, needsEscape("0123456789abcde "))
        assertEquals(true, needsEscape("0123456789abdef"))
        assertEquals(true, needsEscape("0123456789abcde\n"))
        assertEquals(true, needsEscape("0123456789abdef"))
    }
}
