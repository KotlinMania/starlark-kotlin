// port-lint: tests src/values/types/string/intern/interner.rs
package io.github.kotlinmania.starlark.values.types.string.intern

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

import io.github.kotlinmania.starlark.collections.Hashed
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.types.string.allocFrozenStringValue
import io.github.kotlinmania.starlark.values.types.string.allocStringValue
import kotlin.test.Test
import kotlin.test.assertTrue

class InternerTest {
    @Test
    fun testIntern() {
        val heap1 = FrozenHeap.new()
        val heap2 = FrozenHeap.new()
        val intern = FrozenStringValueInterner()

        val xx1 = intern.intern(Hashed.new("xx")) { "xx".allocFrozenStringValue(heap1) }
        val xx2 = intern.intern(Hashed.new("xx")) { "xx".allocFrozenStringValue(heap2) }
        assertTrue(xx1.toValue().ptrEq(xx2.toValue()))
    }

    @Test
    fun testStringValueIntern() {
        Heap.temp { heap1 ->
            val intern = StringValueInterner()

            val xx1 = intern.intern(Hashed.new("xx")) { "xx".allocStringValue(heap1) }
            val xx2 =
                intern.intern(Hashed.new("xx")) {
                    error("alloc_str should be only called once")
                }
            assertTrue(xx1.toValue().ptrEq(xx2.toValue()))
        }
    }
}
