@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)
// port-lint: source src/values/layout/heap/profile/by_type.rs
package io.github.kotlinmania.starlark.values.layout.heap.profile

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

import io.github.kotlinmania.starlark.values.layout.heap.profile.alloccounts.AllocCounts
import io.github.kotlinmania.starlark.values.layout.heap.profile.alloccounts.sum
import kotlin.native.HiddenFromObjC

/**
 * Information about the data stored on a heap. Accessible through
 * the function `allocated_summary` available on [`Heap`](crate::values::Heap)
 * and [`FrozenHeap`](crate::values::FrozenHeap)
 */
class HeapSummary(
    /**
     * For each type, give the (number of entries, size of all entries).
     * The size may be approximate as it includes information from
     * the approximate `memory_size` function.
     */
    internal val summary: SmallMap<String, AllocCounts> = SmallMap(),
) {
    /** (Count, total size) by type. */
    @HiddenFromObjC
    fun summary(): Map<String, Pair<Int, Long>> {
        val result = mutableMapOf<String, Pair<Int, Long>>()
        for ((k, v) in summary) {
            result[k] = Pair(v.count, v.bytes)
        }
        return result
    }

    internal fun total(): AllocCounts = summary.values().toList().sum()

    /** Total number of bytes allocated. */
    fun totalAllocatedBytes(): Long = total().bytes

    internal fun add(t: String, s: AllocCounts) {
        val existing = summary.entry(t).orDefault { AllocCounts() }
        existing += s
    }

    companion object {
        internal fun merge(heaps: Iterable<HeapSummary>): HeapSummary {
            val summary = SmallMap<String, AllocCounts>()
            for (heap in heaps) {
                for ((k, v) in heap.summary) {
                    val existing = summary.entry(k).orDefault { AllocCounts() }
                    existing += v
                }
            }
            return HeapSummary(summary)
        }
    }

    fun copy(): HeapSummary {
        val newSummary = SmallMap<String, AllocCounts>()
        for ((k, v) in summary) {
            newSummary.entry(k).orDefault { v.copy() }
        }
        return HeapSummary(newSummary)
    }

    internal fun normalizeForGoldenTests() {
        for (v in summary.values()) {
            v.normalizeForGoldenTests()
        }
    }
}
