// port-lint: source src/values/layout/heap/allocator/alloc/per_thread.rs
package io.github.kotlinmania.starlark_kotlin.values.layout.heap.allocator.alloc

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

import io.github.kotlinmania.starlark_kotlin.values.layout.AlignedSize
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.allocator.alloc.chunk.Chunk
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.allocator.alloc.chunk_part.ChunkPart
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.arena.MIN_ALLOC

/**
 * Minimum usable cached allocation.
 * All chunks are used to store chains, so we need chain header + at least one object.
 */
internal val MIN_USABLE_ALLOC: AlignedSize = AlignedSize.newBytes(
    (ChunkChain.HEADER_SIZE.bytes() + MIN_ALLOC.bytes()).toInt()
)

/**
 * Chunk cache for reuse across arena allocations.
 *
 * Frozen heap has two arenas: drop and non-drop. So we keep at least two chunks.
 * In Rust this uses `thread_local!` with `RefCell`. In Kotlin with coroutines,
 * we use a coroutine-safe shared cache.
 */
internal class PerThreadChunkCache {
    /** Keep a few last chunks. */
    private val lastChunks: Array<ChunkPart?> = arrayOfNulls(4)

    /**
     * Save a chunk to the cache if it is large enough.
     * Keeps the largest chunks in the pool by swapping.
     */
    fun store(chunk: ChunkPart) {
        var current: ChunkPart? = chunk
        for (i in lastChunks.indices) {
            val next = lastChunks[i]
            // Keep the largest chunks in the pool.
            if ((current?.len() ?: AlignedSize.ZERO) > (next?.len() ?: AlignedSize.ZERO)) {
                lastChunks[i] = current
                current = next
            }
        }
    }

    /** Fetch a chunk from the cache if the cache has a chunk large enough. */
    fun fetch(len: AlignedSize): ChunkPart? {
        for (i in lastChunks.indices) {
            val next = lastChunks[i]
            // Pick any chunk which is large enough.
            if (next != null && next.len() >= len) {
                lastChunks[i] = null
                return next
            }
        }
        return null
    }
}

/**
 * Allocator chunk cache.
 * In Rust: `thread_local! { static PER_THREAD_ALLOCATOR: RefCell<PerThreadChunkCache> }`
 * In Kotlin with coroutines, this is a shared cache instance.
 */
private val PER_THREAD_ALLOCATOR: PerThreadChunkCache = PerThreadChunkCache()

/**
 * Compute next chunk size based on chunk count.
 * Replicates bumpalo behavior: 512 in the first chunk, double each next,
 * but not greater than 2G.
 */
internal fun nextChunkSize(chunkCountInBump: Int): AlignedSize {
    val shifted = 512u.shl(chunkCountInBump)
    return if (shifted == 0u) {
        AlignedSize.newBytes(1 shl 31)
    } else {
        AlignedSize.newBytes(shifted.toInt())
    }
}

/** Allocate chunk which is large enough for given number of words. */
internal fun threadLocalAllocAtLeast(
    len: AlignedSize,
    chunkCountInBump: Int,
): ChunkPart {
    val chunk = PER_THREAD_ALLOCATOR.fetch(len)
        ?: run {
            val nextSize = nextChunkSize(chunkCountInBump) - Chunk.HEADER_SIZE
            val allocLen = maxOf(len, nextSize)
            ChunkPart.allocAtLeast(allocLen)
        }
    check(chunk.len() >= len) { "Allocated chunk too small: ${chunk.len()} < $len" }
    return chunk
}

/** Release chunk part to thread-local pool. */
internal fun threadLocalRelease(chunk: ChunkPart) {
    if (chunk.isFull()) {
        // Chunk part is the full chunk. Better return it to malloc.
        return
    } else if (chunk.len() < MIN_USABLE_ALLOC) {
        // It is not reusable.
        return
    } else if (chunk.chunkRefCount() == 1) {
        // We could reuse the chunk, but since it is not shared,
        // better return it to malloc.
        return
    } else {
        PER_THREAD_ALLOCATOR.store(chunk)
    }
}
