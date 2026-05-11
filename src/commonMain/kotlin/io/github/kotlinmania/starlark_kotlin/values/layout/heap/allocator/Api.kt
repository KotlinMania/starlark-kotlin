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

// use std::mem::MaybeUninit;
// use std::ptr::NonNull;

// use crate::values::layout::value_alloc_size::ValueAllocSize;

import io.github.kotlinmania.starlark_kotlin.values.layout.ValueAllocSize

// pub(crate) enum ChunkAllocationDirection {
enum class ChunkAllocationDirection {
    /// Next allocation in the chunk has higher address than the previous one.
    Up,
    /// Next allocation in the chunk has lower address than the previous one.
    Down,
}

/// Fast memory allocator for the heap.
// pub(crate) trait ArenaAllocator {
internal interface ArenaAllocator {
    /// Number of bytes allocated by this allocator.
    ///
    /// That is:
    /// * space occupied by allocated values
    /// * padding
    /// * reserved but not yet allocated space
    /// * does not include metadata
    // fn allocated_bytes(&self) -> usize;
    fun allocatedBytes(): Int

    /// Number of bytes reserved but not yet allocated by this allocator.
    // fn remaining_capacity(&self) -> usize;
    fun remainingCapacity(): Int

    /// Estimate the size of allocated metadata.
    // fn allocation_overhead(&self) -> usize;
    fun allocationOverhead(): Int

    /// Allocate given number of words.
    // fn alloc(&self, size: ValueAllocSize) -> NonNull<u8>;
    fun alloc(size: ValueAllocSize): Any

    /// This allocator chunk allocation direction.
    // const CHUNK_ALLOCATION_DIRECTION: ChunkAllocationDirection;
    val chunkAllocationDirection: ChunkAllocationDirection

    // type ChunkRevIterator<'a>: Iterator<Item = &'a [MaybeUninit<u8>]>
    // where Self: 'a;

    /// Iterate allocated chunks in the reverse order.
    // unsafe fn iter_allocated_chunks_rev(&self) -> Self::ChunkRevIterator<'_>;
    fun iterAllocatedChunksRev(): Sequence<ByteArray>

    /// No more allocation, reclaim memory if possible.
    // fn finish(&mut self);
    fun finish()
}
