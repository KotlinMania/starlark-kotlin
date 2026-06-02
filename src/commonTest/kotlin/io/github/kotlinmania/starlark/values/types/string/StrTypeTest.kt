// port-lint: tests src/values/types/string/str_type.rs
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

import io.github.kotlinmania.starlark.assert.Assert
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class StrTypeTest {
    private val examples: List<String> =
        listOf(
            "",
            "short",
            "longer string which is all ASCII!#",
            "🤗",
            "mix of prefix ASCII and 🤗 some emjoi",
            "🤗 and the emjoi can go first",
            "😥🍊🍉🫐🥥🥬🥒🥑🍈🍋",
            "© and other characters Ŕ",
            "ça va bien merci",
            "Диана is a name in Russia",
        )

    @Test
    fun testStringCorruption() {
        Assert.fail("'U4V6'[93]", "out of bound")
        Assert.fail("''[2]", "out of bound")
    }

    @Test
    fun testEscapeCharacters() {
        // Test cases from the Starlark spec
        assertEquals(
            "\u0007\b\u000C\n\r\t\u000B",
            Assert.pass("""'\a\b\f\n\r\t\v'""").value().unpackStr()!!,
        )
        assertEquals("\u0000", Assert.pass("""'\0'""").value().unpackStr()!!)
        assertEquals("\n", Assert.pass("""'\12'""").value().unpackStr()!!)
        assertEquals("A-Z", Assert.pass("""'\101-\132'""").value().unpackStr()!!)
        // 9 is not an octal digit, so it terminates early
        assertEquals("\t9", Assert.pass("""'\119'""").value().unpackStr()!!)
        assertEquals("O", Assert.pass("""'\117'""").value().unpackStr()!!)
        assertEquals("A", Assert.pass("""'A'""").value().unpackStr()!!)
        assertEquals("Д", Assert.pass("""'Д'""").value().unpackStr()!!)
        assertEquals("界", Assert.pass("""'界'""").value().unpackStr()!!)
        assertEquals("😀", Assert.pass("""'\U0001F600'""").value().unpackStr()!!)
    }

    @Test
    fun testStringHash() {
        Heap.temp { heap ->
            for (x in examples) {
                assertEquals(
                    heap
                        .allocStr(x)
                        .getHashed()
                        .getOrThrow()
                        .hash(),
                    heap
                        .allocStr(x)
                        .getHashed()
                        .getOrThrow()
                        .hash(),
                )
            }
        }
    }

    // If hash was zero, we'd need to mask the value in the hash cache.

    @Test
    fun testZeroLengthStringHashIsNotZero() {
        Heap.temp { heap ->
            assertNotEquals(
                0u,
                heap
                    .allocStr("")
                    .getHash()
                    .getOrThrow()
                    .get(),
            )
        }
    }

    @Test
    fun testStringLen() {
        Assert.allTrue(
            """
len("😿") == 1
""",
        )
    }

    @Test
    fun testArithmeticOnString() {
        Assert.allTrue(
            """
"abc" + "def" == "abcdef"
"abc" * 3 == "abcabcabc"
""",
        )
    }

    @Test
    fun testSliceString() {
        Heap.temp { heap ->
            for (example in examples) {
                val s = heap.allocStr(example).toValue()
                for (i in -5..6) {
                    for (j in -5..6) {
                        val start =
                            if (i == 6) {
                                null
                            } else {
                                io.github.kotlinmania.starlark.values.layout.Value
                                    .testingNewInt(i)
                            }
                        val stop =
                            if (j == 6) {
                                null
                            } else {
                                io.github.kotlinmania.starlark.values.layout.Value
                                    .testingNewInt(j)
                            }
                        // Compare list slicing (comparatively simple) to string slicing (complex unicode)
                        val codePoints = mutableListOf<String>()
                        var idx = 0
                        while (idx < example.length) {
                            val c = example[idx]
                            if (c.isHighSurrogate() && idx + 1 < example.length && example[idx + 1].isLowSurrogate()) {
                                codePoints.add(example.substring(idx, idx + 2))
                                idx += 2
                            } else {
                                codePoints.add(c.toString())
                                idx += 1
                            }
                        }
                        val res1 =
                            io.github.kotlinmania.starlark.values
                                .applySlice(codePoints, start, stop, null)
                                .getOrThrow()
                                .joinToString("")
                        val res2 = s.slice(start, stop, null, heap).getOrThrow().unpackStr()!!
                        assertEquals(res1, res2, "$example[${start ?: ""}:${stop ?: ""}]")
                    }
                }
            }

            Assert.allTrue(
                """
"abc"[1:] == "bc" # Remove the first element
"abc"[:-1] == "ab" # Remove the last element
"abc"[1:-1] == "b" # Remove the first and the last element
"banana"[1::2] == "aaa" # Select one element out of 2, skipping the first
"banana"[4::-2] == "nnb" # Select one element out of 2 in reverse order, starting at index 4
"242"[ -0:-2:-1] == "" # From https://github.com/facebook/starlark-rust/issues/35
""",
            )
        }
    }

    @Test
    fun testStringIsIn() {
        Assert.allTrue(
            """
("a" in "abc") == True
("b" in "abc") == True
("bc" in "abc") == True
("bd" in "abc") == False
("z" in "abc") == False
""",
        )
    }

    @Test
    fun testSuccessiveAdd() {
        // we hope these get optimised away with adjacent plus optimisation
        Assert.eq("x = 'c'\n'a' + 'b' + x + 'd' + 'e'", "'abcde'")
    }

    @Test
    fun testStringIndex() {
        fun testStr(str: String) {
            Heap.temp { heap ->
                val codePoints = mutableListOf<String>()
                var idx = 0
                while (idx < str.length) {
                    val c = str[idx]
                    if (c.isHighSurrogate() && idx + 1 < str.length && str[idx + 1].isLowSurrogate()) {
                        codePoints.add(str.substring(idx, idx + 2))
                        idx += 2
                    } else {
                        codePoints.add(c.toString())
                        idx += 1
                    }
                }
                val v = heap.allocStr(str)
                val len = codePoints.size
                assertEquals(len, v.length().getOrThrow())
                for ((i, charStr) in codePoints.withIndex()) {
                    assertEquals(charStr, v.at(Value.testingNewInt(i), heap).getOrThrow().unpackStr())
                    assertEquals(charStr, v.at(Value.testingNewInt(-len + i), heap).getOrThrow().unpackStr())
                }
                assertTrue(v.at(Value.testingNewInt(len), heap).isFailure)
                assertTrue(v.at(Value.testingNewInt(-(len + 1)), heap).isFailure)
            }
        }

        for (x in examples) {
            // We use all trailing substrings of the test, for better coverage (especially around smart prefix algorithms)
            var idx = 0
            while (true) {
                testStr(x.substring(idx))
                if (idx >= x.length) break
                idx++
            }
        }
    }
}
