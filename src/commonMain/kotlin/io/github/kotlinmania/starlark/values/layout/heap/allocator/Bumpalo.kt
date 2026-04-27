// port-lint: source src/values/layout/heap/allocator/bumpalo.rs
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

/**
 * Wrapper around allocated chunks for iteration.
 *
 * as `&[MaybeUninit<u8>]` slices. In Kotlin, chunks are represented as
 * `ByteArray` for profiling/introspection purposes.
 */
// Kotlin: not needed as a separate class; we import Sequence<ByteArray> directly.

/**
 * Bump allocator implementation of [ArenaAllocator].
 *
 * with low-level pointer management. In Kotlin, the JVM garbage collector handles
 * memory allocation, so this class provides a compatible interface that tracks
 * allocation statistics without managing raw memory.
 */
internal class BumpAllocator : ArenaAllocator {
    // Tracking bytes allocated for profiling compatibility.
    private var totalAllocated: Int = 0

    override fun allocatedBytes(): Int = totalAllocated

    override fun remainingCapacity(): Int {
        // JVM manages capacity; report 0 remaining as there is no pre-reserved arena.
        return 0
    }

    override fun allocationOverhead(): Int {
        // No arena metadata overhead in JVM-managed allocation.
        return 0
    }

    override fun alloc(size: ValueAllocSize): Any {
        // In Kotlin, allocation is handled by the JVM.
        // Track the requested size for profiling.
        totalAllocated += size.bytes().toInt()
        return ByteArray(size.bytes().toInt())
    }

    override val chunkAllocationDirection: ChunkAllocationDirection = ChunkAllocationDirection.Down

    override fun iterAllocatedChunksRev(): Sequence<ByteArray> {
        // JVM does not expose raw chunk iteration.
        return emptySequence()
    }

    override fun finish() {
        // No-op: JVM GC handles deallocation.
    }
}
