// port-lint: tests src/values/layout/heap/profile/aggregated.rs
package io.github.kotlinmania.starlark.values.layout.heap.profile

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

import io.github.kotlinmania.starlark.values.layout.Freezer
import io.github.kotlinmania.starlark.values.layout.constFrozenString
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.heap.HeapKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AggregatedTest {
    private fun totalAllocCount(frame: StackFrame): Int =
        frame.allocs.total().count +
            frame.callees.values().sumOf { c -> totalAllocCount(c) }

    @Test
    fun testStacksCollect() {
        Heap.temp { heap ->
            heap.recordCallEnter(constFrozenString("enter").toValue())
            heap.allocStr("xxyy")
            heap.allocStr("zzww")
            heap.recordCallExit()

            val stacks = AggregateHeapProfileInfo.collect(heap, null)
            assertTrue(
                stacks.root.allocs.summary
                    .isEmpty(),
            )
            assertEquals(1, stacks.root.callees.len())
            assertEquals(2, totalAllocCount(stacks.root))
        }
    }

    @Test
    fun testStacksCollectRetained() {
        Heap.temp { heap ->
            heap.recordCallEnter(constFrozenString("enter").toValue())
            val s0 = heap.allocStr("xxyy")
            val s1 = heap.allocStr("zzww")
            heap.allocStr("rrtt")
            heap.recordCallExit()

            val frozenHeap = FrozenHeap.new()
            val freezer = Freezer(frozenHeap)
            freezer.freeze(s0).getOrThrow()
            freezer.freeze(s1).getOrThrow()

            val stacks = AggregateHeapProfileInfo.collect(heap, HeapKind.Frozen)
            assertTrue(
                stacks.root.allocs.summary
                    .isEmpty(),
            )
            assertEquals(1, stacks.root.callees.len())
            // 3 allocated, 2 retained.
            assertEquals(
                2,
                stacks.root.callees.values()
                    .first()
                    .allocs.summary["string"]!!
                    .count,
            )
            assertEquals(2, totalAllocCount(stacks.root))
        }
    }

    @Test
    fun testMerge() {
        fun make(): AggregateHeapProfileInfo =
            Heap.temp { heap ->
                heap.recordCallEnter(constFrozenString("xx").toValue())
                val s = heap.allocStr("abc")
                heap.recordCallExit()
                val frozenHeap = FrozenHeap.new()
                val freezer = Freezer(frozenHeap)
                freezer.freeze(s).getOrThrow()

                AggregateHeapProfileInfo.collect(heap, HeapKind.Frozen)
            }

        val merge = AggregateHeapProfileInfo.merge(listOf(make(), make(), make()))
        val summary = HeapSummaryByFunction.init(merge)
        assertEquals(1, summary.info().size)
        val (xxId, xxInfo) = summary.info()[0]
        assertEquals("xx", xxId.toString())
        assertEquals(3, xxInfo.allocations["string"]!!.count)
    }
}
