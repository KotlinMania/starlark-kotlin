// port-lint: tests src/values/types/tuple/alloc.rs (tests)
package io.github.kotlinmania.starlark_kotlin.values.types.tuple

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
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.types.num.Num
import io.github.kotlinmania.starlark_kotlin.values.types.int.StarlarkInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AllocTest {
    @Test
    fun testAllocTuple() {
        Heap.temp { heap ->
            // Rust uses [""; 0] (empty string array) and [1,2,3] (i32 array).
            // In Kotlin, raw primitives don't implement AllocValue, so we use Num.Int.
            val a = heap.alloc(AllocTuple(emptyList<Num>()))
            val b = heap.alloc(AllocTuple(
                listOf(Num.Int(StarlarkInt.from(1)), Num.Int(StarlarkInt.from(2)), Num.Int(StarlarkInt.from(3)))
                    .filter { false }
            ))
            assertEquals(0, TupleRef.fromValue(a)!!.content().size)
            assertTrue(a.ptrEq(b))

            // Fixed length iterator.
            val c = heap.alloc(AllocTuple(listOf(Num.Int(StarlarkInt.from(1)), Num.Int(StarlarkInt.from(2)))))
            assertEquals(2, TupleRef.fromValue(c)!!.content().size)

            // Iterator of unknown length.
            val d = heap.alloc(AllocTuple(
                listOf(Num.Int(StarlarkInt.from(1)), Num.Int(StarlarkInt.from(2)), Num.Int(StarlarkInt.from(3)))
                    .filterIndexed { i, _ -> i > 0 }
            ))
            assertEquals(2, TupleRef.fromValue(d)!!.content().size)
        }
    }

    @Test
    fun testAllocFrozenTuple() {
        val heap = FrozenHeap()

        val a = heap.alloc(AllocTuple(emptyList<Num>()))
        val b = heap.alloc(AllocTuple(
            listOf(Num.Int(StarlarkInt.from(1)), Num.Int(StarlarkInt.from(2)), Num.Int(StarlarkInt.from(3)))
                .filter { false }
        ))
        assertEquals(0, TupleRef.fromFrozenValue(a)!!.content().size)
        assertTrue(a.toValue().ptrEq(b.toValue()))

        // Fixed length iterator.
        val c = heap.alloc(AllocTuple(listOf(Num.Int(StarlarkInt.from(1)), Num.Int(StarlarkInt.from(2)))))
        assertEquals(2, FrozenTupleRef.fromFrozenValue(c)!!.content().size)

        // Iterator of unknown length.
        val d = heap.alloc(AllocTuple(
            listOf(Num.Int(StarlarkInt.from(1)), Num.Int(StarlarkInt.from(2)), Num.Int(StarlarkInt.from(3)))
                .filterIndexed { i, _ -> i > 0 }
        ))
        assertEquals(2, FrozenTupleRef.fromFrozenValue(d)!!.content().size)
    }
}
