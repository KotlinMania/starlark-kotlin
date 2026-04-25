// port-lint: source src/collections/alloca.rs (tests)
package io.github.kotlinmania.starlark.collections

/*
 * Copyright 2019 The Starlark in Rust Authors.
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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AllocaTest {

    @Test
    fun testRemInWordsToRemInT() {
        val a = Alloca.new()
        assertEquals(10, a.remInWordsToRemInT<Long>(10))
        assertEquals(1, a.remInWordsToRemInT<Int>(1))
        assertEquals(2, a.remInWordsToRemInT<Int>(2))
    }

    @Test
    fun testLenInTToLenInWords() {
        val a = Alloca.new()
        assertEquals(10, a.lenInTToLenInWords<Long>(10))
        assertEquals(1, a.lenInTToLenInWords<Byte>(1))
    }

    @Test
    fun testAlloca() {
        // Use a small capacity to encourage overflow behaviour
        val a = Alloca.withCapacity(100)
        a.allocaFill(3, 8) { xs ->
            xs[0] = 5
            xs[2] = xs[0] + xs[1] + 15
            a.allocaFill(200, 18) { ys ->
                assertEquals(8 + 5 + 15, xs[2])
                assertEquals(18, ys[0])
                assertEquals(18, ys[200 - 1])
            }
            a.allocaFill(3, 1L) { }
            assertEquals(8 + 5 + 15, xs[2])
        }
    }

    @Test
    fun triggerBug() {
        val a = Alloca.withCapacity(100)
        for (i in 0 until 100) {
            a.allocaFill(10, 17) { _ ->
                a.allocaFill(1000, 19) { }
            }
        }

        assertEquals(2, a.buffersLen())
    }

    @Test
    fun testAllocaBugNotAligned() {
        val a = Alloca.withCapacity(100)
        a.allocaFill(1, 17.toByte()) { xs ->
            // Bug was triggered because the end of first allocation
            // was rounded down instead of up.
            a.allocaFill(1, 19.toByte()) { ys ->
                assertEquals(listOf(17.toByte()), xs)
                assertEquals(listOf(19.toByte()), ys)
            }
        }
    }

    @Test
    fun testAllocaConcat() {
        val a = Alloca.new()
        val x = listOf("ab")
        val y = listOf("cd")
        a.allocaConcat(x, emptyList()) { xy ->
            assertTrue(xy === x)
        }
        a.allocaConcat(emptyList(), x) { xy ->
            assertTrue(xy === x)
        }
        a.allocaConcat(x, y) { xy ->
            assertEquals(listOf("ab", "cd"), xy)
        }
    }
}
