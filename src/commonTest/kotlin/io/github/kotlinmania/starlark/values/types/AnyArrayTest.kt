// port-lint: source tests:src/values/types/any_array.rs
package io.github.kotlinmania.starlark.values.types

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

import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import kotlin.concurrent.atomics.AtomicInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnyArrayTest {

    @Test
    fun testDrop() {
        // The Rust upstream relies on `Drop` impls to count deallocations on
        // counter1/counter2. Kotlin uses GC instead — there is no observable
        // moment when an array element is "dropped". The structural port
        // verifies the heap allocates the slice and indexed access produces
        // the original counter handles, mirroring upstream identity checks.
        val counter1 = AtomicInt(0)
        val counter2 = AtomicInt(0)

        data class IncrementOnDrop(val counter: AtomicInt)

        val heap = FrozenHeap.new()
        val values = heap.allocAnySlice(
            listOf(
                IncrementOnDrop(counter1),
                IncrementOnDrop(counter1),
                IncrementOnDrop(counter2),
                IncrementOnDrop(counter1),
                IncrementOnDrop(counter2),
            ),
        )

        assertEquals(5, values.size)

        assertTrue(counter1 === values[0].counter)
        assertTrue(counter1 === values[1].counter)
        assertTrue(counter2 === values[2].counter)
        assertTrue(counter1 === values[3].counter)
        assertTrue(counter2 === values[4].counter)
    }

    @Test
    fun testAllocationSize() {
        val heap = FrozenHeap.new()
        heap.allocAnySlice(listOf(1, 2, 3))
        val quake = heap.allocStr("quake")
        // Test array allocation did not overwrite the string.
        assertEquals("quake", quake.asStr())
    }
}
