// port-lint: tests values/layout/constFrozenString.rs
package io.github.kotlinmania.starlark.values.layout

/*
 * Copyright 2019 The Starlark in Rust Authors.
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

import io.github.kotlinmania.starlark.values.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConstFrozenStringTest {
    @Test
    fun testConstFrozenStringForShortStrings() {
        assertTrue(
            constFrozenString("a").toValue().ptrEq(constFrozenString("a").toValue())
        )

        Heap.temp { heap ->
            assertTrue(
                constFrozenString("a").toValue().ptrEq(heap.allocStr("a").toValue())
            )
        }

        val frozenHeap = FrozenHeap.new()
        assertTrue(
            constFrozenString("a").toValue().ptrEq(frozenHeap.allocStr("a").toValue())
        )
    }

    @Test
    fun testConstFrozenString() {
        assertEquals("", constFrozenString("").asStr())
        assertEquals("a", constFrozenString("a").asStr())
        assertEquals("ab", constFrozenString("ab").asStr())
        assertEquals("abc", constFrozenString("abc").asStr())
        assertEquals("abcd", constFrozenString("abcd").asStr())
        assertEquals("abcde", constFrozenString("abcde").asStr())
        assertEquals("abcdef", constFrozenString("abcdef").asStr())
        assertEquals("abcdefg", constFrozenString("abcdefg").asStr())
        assertEquals("abcdefgh", constFrozenString("abcdefgh").asStr())
        assertEquals("abcdefghi", constFrozenString("abcdefghi").asStr())
        assertEquals("abcdefghij", constFrozenString("abcdefghij").asStr())
        assertEquals("abcdefghijk", constFrozenString("abcdefghijk").asStr())
    }
}
