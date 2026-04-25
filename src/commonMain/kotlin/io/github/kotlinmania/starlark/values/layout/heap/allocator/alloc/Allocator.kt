// port-lint: source src/values/layout/heap/allocator/alloc/allocator.rs
package io.github.kotlinmania.starlark.values.layout.heap.allocator.alloc

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

import io.github.kotlinmania.starlark.values.layout.AlignedSize
import io.github.kotlinmania.starlark.values.layout.heap.allocator.ArenaAllocator
import io.github.kotlinmania.starlark.values.layout.heap.allocator.ChunkAllocationDirection
import io.github.kotlinmania.starlark.values.layout.heap.allocator.alloc.chunkpart.ChunkPart
import io.github.kotlinmania.starlark.values.layout.ValueAllocSize

/**
 * Chunk-based arena allocator.
 *
 * In Rust this uses UnsafeCell, Cell, and NonNull for interior mutability and raw pointer
 * manipulation. In Kotlin, we use simple mutable fields with offset-based addressing
 * matching [ChunkChain]'s Int-based abstraction.
 *
 * pub(crate) struct ChunkAllocator
 */
internal class ChunkAllocator : ArenaAllocator {
    /**
     * Current chunk in the chunk chain is partially filled.
     * The rest of the chain contains allocated data.
     */
    private var chain: ChunkChain = ChunkChain.default()
    /**
     * Offset pointing to the currently filled part of the chunk.
     * In Rust: Cell<NonNull<usize>>
     */
    private var currentPtr: Int = chain.begin()
    /**
     * Offset pointing to the end of the current chunk part.
     * In Rust: Cell<NonNull<usize>>
     */
    private var endPtr: Int = chain.begin()

    /** Replace the current chain, returning the old chain and old currentPtr. */
    private fun replaceChain(newChain: ChunkChain): Pair<ChunkChain, Int> {
        val oldCurrentPtr = currentPtr
        currentPtr = newChain.begin()
        endPtr = newChain.end()
        val oldChain = chain
        chain = newChain
        return Pair(oldChain, oldCurrentPtr)
    }

    /** Take the current chain and replace with default. */
    private fun takeChain(): Pair<ChunkChain, Int> {
        return replaceChain(ChunkChain.default())
    }

    /**
     * Try to allocate from the current chunk (fast path).
     * Returns the offset if there's room, null otherwise.
     */
    private fun tryAllocFast(len: AlignedSize): Int? {
        val rem = endPtr - currentPtr
        val lenBytes = len.bytes().toInt()
        return if (rem >= lenBytes) {
            val ptr = currentPtr
            currentPtr += lenBytes
            ptr
        } else {
            null
        }
    }

    /**
     * Slow-path allocation: split current chain, release remainder,
     * allocate a new chunk, and retry.
     */
    private fun allocSlow(len: AlignedSize): Int {
        val (oldChain, oldCurrentPtr) = takeChain()
        val (remChain, after) = oldChain.splitAtPtr(oldCurrentPtr)
        threadLocalRelease(after)

        val requiredLen = len + ChunkChain.HEADER_SIZE
        val nextChunk = threadLocalAllocAtLeast(requiredLen, remChain.depth())

        val nextChain = ChunkChain.new(nextChunk, remChain)

        replaceChain(nextChain)

        return tryAllocFast(len)
            ?: error("tryAllocFast must not fail in allocSlow")
    }

    // --- ArenaAllocator implementation ---

    override fun allocatedBytes(): Int {
        return chain.allocatedBytes()
    }

    override fun remainingCapacity(): Int {
        return endPtr - currentPtr
    }

    override fun allocationOverhead(): Int {
        val allocatedBytesWithMetadata = chain.allocatedBytesWithMetadata()
        return maxOf(0, allocatedBytesWithMetadata - allocatedBytes())
    }

    override fun alloc(size: ValueAllocSize): Any {
        return tryAllocFast(size.size())
            ?: allocSlow(size.size())
    }

    override val chunkAllocationDirection: ChunkAllocationDirection =
        ChunkAllocationDirection.Up

    override fun iterAllocatedChunksRev(): Sequence<ByteArray> {
        return sequence {
            // First, yield the currently filling chunk.
            val begin = chain.begin()
            val currentLen = currentPtr - begin
            if (currentLen > 0) {
                yield(ByteArray(currentLen))
            }
            // Then iterate previous chain chunks.
            val prev = chain.prev()
            if (prev != null) {
                val iter = prev.iter()
                while (iter.hasNext()) {
                    val c = iter.next()
                    val data = c.dataBytes()
                    if (data.isNotEmpty()) {
                        yield(data)
                    }
                }
            }
        }
    }

    override fun finish() {
        val (oldChain, oldCurrentPtr) = takeChain()
        val (newChain, rem) = oldChain.splitAtPtr(oldCurrentPtr)
        threadLocalRelease(rem)
        val newCurrentPtr = newChain.end()
        replaceChain(newChain)
        currentPtr = newCurrentPtr
    }

    /**
     * Release resources by returning chunks to thread-local pool.
     * In Rust: impl Drop for ChunkAllocator
     */
    fun close() {
        chain.clearWith(::threadLocalRelease)
    }

    override fun toString(): String = "ChunkAllocator(..)"
}

/**
 * Iterator over allocated chunks in reverse order.
 *
 * pub(crate) struct ChunkRevIterator<'a>
 */
internal class ChunkRevIterator(
    private var current: ByteArray?,
    private val chainIter: ChunkChainIterator,
) : Iterator<ByteArray> {
    private var nextValue: ByteArray? = computeNext()

    private fun computeNext(): ByteArray? {
        // First, return the current chunk (partially filled top chunk).
        val c = current
        if (c != null && c.isNotEmpty()) {
            current = null
            return c
        }
        current = null
        // Then iterate through the chain.
        while (chainIter.hasNext()) {
            val chain = chainIter.next()
            val data = chain.dataBytes()
            if (data.isNotEmpty()) {
                return data
            }
        }
        return null
    }

    override fun hasNext(): Boolean = nextValue != null

    override fun next(): ByteArray {
        val result = nextValue ?: throw NoSuchElementException()
        nextValue = computeNext()
        return result
    }
}
