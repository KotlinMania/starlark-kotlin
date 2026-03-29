// port-lint: source src/values/layout/heap/profile/by_type.rs
package io.github.kotlinmania.starlark_kotlin.values.layout.heap.profile

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

import io.github.kotlinmania.starlark_kotlin.values.layout.heap.profile.alloc_counts.AllocCounts

/// Information about the data stored on a heap. Accessible through
/// the function `allocated_summary` available on [`Heap`](crate::values::Heap)
/// and [`FrozenHeap`](crate::values::FrozenHeap)
// #[derive(Debug, Default, Clone, Allocative)]
// pub struct HeapSummary
class HeapSummary(
    /// For each type, give the (number of entries, size of all entries).
    /// The size may be approximate as it includes information from
    /// the approximate `memory_size` function.
    internal val summary: SmallMap<String, AllocCounts> = SmallMap(),
) {
    /// (Count, total size) by type.
    // pub fn summary(&self) -> HashMap<String, (usize, usize)>
    fun summary(): Map<String, Pair<Int, Long>> {
        val result = mutableMapOf<String, Pair<Int, Long>>()
        for ((k, v) in summary) {
            result[k] = Pair(v.count, v.bytes)
        }
        return result
    }

    // pub(crate) fn total(&self) -> AllocCounts
    internal fun total(): AllocCounts {
        return AllocCounts.sum(summary.values())
    }

    /// Total number of bytes allocated.
    // pub fn total_allocated_bytes(&self) -> usize
    fun totalAllocatedBytes(): Long {
        return total().bytes
    }

    // pub(crate) fn add(&mut self, t: &'static str, s: AllocCounts)
    internal fun add(t: String, s: AllocCounts) {
        val existing = summary.entry(t).orDefault { AllocCounts() }
        existing += s
    }

    companion object {
        // pub(crate) fn merge(heaps: impl IntoIterator<Item = &HeapSummary>) -> HeapSummary
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

    // #[derive(Clone)]
    fun copy(): HeapSummary {
        val newSummary = SmallMap<String, AllocCounts>()
        for ((k, v) in summary) {
            newSummary.entry(k).orDefault { v.copy() }
        }
        return HeapSummary(newSummary)
    }

    // #[cfg(test)]
    // pub(crate) fn normalize_for_golden_tests(&mut self)
    internal fun normalizeForGoldenTests() {
        for (v in summary.values()) {
            v.normalizeForGoldenTests()
        }
    }
}
