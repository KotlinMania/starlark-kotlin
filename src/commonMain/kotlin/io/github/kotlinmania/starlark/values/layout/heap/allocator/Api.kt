// port-lint: source src/values/layout/heap/allocator/api.rs
package io.github.kotlinmania.starlark.values.layout.heap.allocator

/*
 * Copyright 2019 The Starlark in Rust Authors.
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

import io.github.kotlinmania.starlark.values.layout.ValueAllocSize

enum class ChunkAllocationDirection {
    /** Next allocation in the chunk has higher address than the previous one. */
    Up,
    /** Next allocation in the chunk has lower address than the previous one. */
    Down,
}

/**
 * Fast memory allocator for the heap.
 *
 * Sealed: the upstream `values/layout/heap/allocator/alloc.rs` mod groups the
 * closed set of allocator implementations (chain, chunk, chunk_part,
 * per_thread) alongside the `allocator` trait file, and the sibling
 * `allocator/bumpalo.rs` provides the bumpalo-based allocator. Sealing the
 * contract here gives the Kotlin compiler the same closed-variant guarantee
 * that the Rust mod declaration provides.
 */
internal sealed interface ArenaAllocator {
    /**
     * Number of bytes allocated by this allocator.
     *
     * That is:
     * * space occupied by allocated values
     * * padding
     * * reserved but not yet allocated space
     * * does not include metadata
     */
    fun allocatedBytes(): Int

    /** Number of bytes reserved but not yet allocated by this allocator. */
    fun remainingCapacity(): Int

    /** Estimate the size of allocated metadata. */
    fun allocationOverhead(): Int

    /** Allocate given number of words. */
    fun alloc(size: ValueAllocSize): Any

    /** This allocator chunk allocation direction. */
    val chunkAllocationDirection: ChunkAllocationDirection

    /** Iterate allocated chunks in the reverse order. */
    fun iterAllocatedChunksRev(): Sequence<ByteArray>

    /** No more allocation, reclaim memory if possible. */
    fun finish()
}
