// port-lint: source values/thinBoxSliceFrozenValue/packed_impl.rs
package io.github.kotlinmania.starlark.values.thinboxslicefrozenvalue

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

import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.avalues.str.allocStr
import io.github.kotlinmania.starlark.values.types.int.InlineInt
import kotlin.test.Test
import kotlin.test.assertEquals

class PackedImplTest {

    private fun acrossLengths(a: List<FrozenValue>) {
        for (len in 0..a.size) {
            val value = ThinBoxSliceFrozenValue.fromIter(a.take(len))
            assertEquals(len, value.size)
            assertEquals(a.take(len), value.toList())
        }
    }

    @Test
    fun testStrings() {
        val h = FrozenHeap()
        val strs = listOf("", "abc", "def", "ghijkl")
        val s = (strs + strs + strs + strs).map { it -> h.allocStr(it).toFrozenValue() }
        acrossLengths(s)
    }

    @Test
    fun testInts() {
        val ints = listOf(0, 1, 2, 3, 4, 5, 1000, 1 shl 20)
        val i = (ints + ints).map { v -> FrozenValue.newInt(InlineInt.testingNew(v)) }
        acrossLengths(i)
    }

    @Test
    fun testMixedTypes() {
        val items = listOf(
            FrozenValue.newNone(),
            FrozenValue.newInt(InlineInt.testingNew(0)),
            FrozenValue.newEmptyList(),
            FrozenValue.newBool(true),
        )
        val a = items + items + items + items
        acrossLengths(a)
    }

    @Test
    fun testDefault() {
        val value = ThinBoxSliceFrozenValue.default()
        assertEquals(0, value.size)
    }

    @Test
    fun testEmpty() {
        val valA = ThinBoxSliceFrozenValue.empty()
        val valB = ThinBoxSliceFrozenValue.empty()
        assertEquals(valA, valB)
    }
}
