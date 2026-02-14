// port-lint: source src/values/layout/heap/allocator/api.rs
package io.github.kotlinmania.starlark_kotlin.values.layout.heap.allocator

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

import io.github.kotlinmania.starlark_kotlin.values.layout.value_alloc_size.ValueAllocSize

/// Direction of chunk allocation within an arena allocator.
///
/// pub(crate) enum ChunkAllocationDirection
enum class ChunkAllocationDirection {
    /// Next allocation in the chunk has higher address than the previous one.
    Up,
    /// Next allocation in the chunk has lower address than the previous one.
    Down,
}

/// Fast memory allocator for the heap.
///
/// In Rust this is a trait with associated types and const generics.
/// In Kotlin we model it as an interface.
///
/// pub(crate) trait ArenaAllocator
internal interface ArenaAllocator {
    /// Number of bytes allocated by this allocator.
    ///
    /// That is:
    /// * space occupied by allocated values
    /// * padding
    /// * reserved but not yet allocated space
    /// * does not include metadata
    fun allocatedBytes(): Int

    /// Number of bytes reserved but not yet allocated by this allocator.
    fun remainingCapacity(): Int

    /// Estimate the size of allocated metadata.
    fun allocationOverhead(): Int

    /// Allocate given number of words.
    /// In Rust returns `NonNull<u8>`. In Kotlin returns an opaque allocation handle.
    fun alloc(size: ValueAllocSize): Any

    /// This allocator's chunk allocation direction.
    val chunkAllocationDirection: ChunkAllocationDirection

    /// Iterate allocated chunks in the reverse order.
    /// In Rust this returns an associated type `ChunkRevIterator<'a>: Iterator<Item = &'a [MaybeUninit<u8>]>`.
    /// In Kotlin, we use a sequence of ByteArray.
    fun iterAllocatedChunksRev(): Sequence<ByteArray>

    /// No more allocation, reclaim memory if possible.
    fun finish()
}
