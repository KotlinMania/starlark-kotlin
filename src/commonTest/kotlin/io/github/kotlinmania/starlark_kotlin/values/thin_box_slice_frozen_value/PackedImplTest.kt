// port-lint: tests src/values/thin_box_slice_frozen_value/packed_impl.rs
package io.github.kotlinmania.starlark_kotlin.values.thin_box_slice_frozen_value

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

import io.github.kotlinmania.starlark_kotlin.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.str_.allocStr
import io.github.kotlinmania.starlark_kotlin.values.types.int.InlineInt
import kotlin.test.Test
import kotlin.test.assertEquals

private fun acrossLengths(values: List<FrozenValue>) {
    for (length in 0..values.size) {
        val value = ThinBoxSliceFrozenValue.fromIter(values.take(length))
        assertEquals(length, value.size)
        assertEquals(values.take(length), value.toList())
    }
}

internal class PackedImplTest {
    @Test
    fun testStrings() {
        val heap = FrozenHeap()
        val strings = listOf("", "abc", "def", "ghijkl")
        val values = (strings + strings + strings + strings).map { text ->
            heap.allocStr(text).toFrozenValue()
        }
        acrossLengths(values)
    }

    @Test
    fun testInts() {
        val ints = listOf(0, 1, 2, 3, 4, 5, 1000, 1_048_576)
        val values = (ints + ints).map { number ->
            FrozenValue.newInt(InlineInt.testingNew(number))
        }
        acrossLengths(values)
    }

    @Test
    fun testMixedTypes() {
        val items = listOf(
            FrozenValue.newNone(),
            FrozenValue.newInt(InlineInt.testingNew(0)),
            FrozenValue.newEmptyList(),
            FrozenValue.newBool(true),
        )
        acrossLengths(items + items + items + items)
    }

    @Test
    fun testDefault() {
        val value = ThinBoxSliceFrozenValue.fromIter(emptyList())
        assertEquals(0, value.size)
    }

    @Test
    fun testEmpty() {
        val valA = ThinBoxSliceFrozenValue.empty()
        val valB = ThinBoxSliceFrozenValue.empty()
        assertEquals(valA, valB)
    }
}
