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

import io.github.kotlinmania.starlark_kotlin.collections.SmallMap
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
    fun summary(): Map<String, Pair<Int, Int>> {
        return summary.entries.associate { (k, v) ->
            k to Pair(v.count, v.bytes)
        }
    }

    // pub(crate) fn total(&self) -> AllocCounts
    internal fun total(): AllocCounts {
        var result = AllocCounts()
        for (v in summary.values) {
            result += v
        }
        return result
    }

    /// Total number of bytes allocated.
    // pub fn total_allocated_bytes(&self) -> usize
    fun totalAllocatedBytes(): Int {
        return total().bytes
    }

    // pub(crate) fn add(&mut self, t: &'static str, s: AllocCounts)
    internal fun add(t: String, s: AllocCounts) {
        val existing = summary[t]
        if (existing != null) {
            existing += s
        } else {
            summary[t] = AllocCounts(s.bytes, s.count)
        }
    }

    companion object {
        // pub(crate) fn merge(heaps: impl IntoIterator<Item = &HeapSummary>) -> HeapSummary
        internal fun merge(heaps: Iterable<HeapSummary>): HeapSummary {
            val summary = SmallMap<String, AllocCounts>()
            for (heap in heaps) {
                for ((k, v) in heap.summary) {
                    val existing = summary[k]
                    if (existing != null) {
                        existing += v
                    } else {
                        summary[k] = AllocCounts(v.bytes, v.count)
                    }
                }
            }
            return HeapSummary(summary)
        }
    }

    // #[cfg(test)]
    // pub(crate) fn normalize_for_golden_tests(&mut self)
    internal fun normalizeForGoldenTests() {
        for (v in summary.values) {
            v.normalizeForGoldenTests()
        }
    }
}
