// port-lint: source src/values/layout/heap/allocator/alloc/chain.rs
package io.github.kotlinmania.starlark.values.layout.heap.allocator.alloc

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

import io.github.kotlinmania.starlark.values.layout.AlignedSize
import io.github.kotlinmania.starlark.values.layout.heap.allocator.alloc.chunkpart.ChunkPart

/** What is stored inside each node of the ChunkChain linked list. */
private class ChunkChainData(
    val prev: ChunkChain,
)

/**
 * Linked list of chunk parts.
 *
 * Each node in the chain owns a [ChunkPart] whose first [HEADER_SIZE] bytes
 * are reserved for the chain header ([ChunkChainData]).
 * The remaining bytes in the chunk part are available for allocation.
 */
internal class ChunkChain private constructor(
    /**
     * Chunk part data is [ChunkChainData].
     * `null` means that the chain is empty.
     */
    private var chunk: ChunkPart?,
    /** The chain header stored separately (Kotlin doesn't embed into raw memory). */
    private var header: ChunkChainData?,
) {
    companion object {
        /** The header size in aligned bytes. */
        val HEADER_SIZE: AlignedSize = AlignedSize.newBytes(8)

        /** Create an empty chain. */
        fun default(): ChunkChain = ChunkChain(chunk = null, header = null)

        /** Create a new chain node with the given chunk part and previous chain. */
        fun new(chunk: ChunkPart, prev: ChunkChain): ChunkChain {
            // Does not have to be strictly greater, but it is pointless otherwise.
            check(chunk.len() > HEADER_SIZE)
            return ChunkChain(
                chunk = chunk,
                header = ChunkChainData(prev = prev),
            )
        }
    }

    /** Get the previous chain node, or null if this is the last/empty node. */
    fun prev(): ChunkChain? {
        return header?.prev
    }

    /**
     * Size of memory available for allocation in the current chunk part,
     * that is the size of the current chunk part minus the header.
     */
    fun currentChunkAvailableLen(): AlignedSize {
        val c = chunk ?: return AlignedSize.ZERO
        return c.len().uncheckedSub(HEADER_SIZE)
    }

    /** Get the begin pointer (offset) for the data area. */
    fun begin(): Int {
        val c = chunk ?: return 0
        return c.ptrAtOffset(HEADER_SIZE)
    }

    /** Get the end pointer (offset) for the data area. */
    fun end(): Int {
        val c = chunk ?: return 0
        return c.end()
    }

    /** Get the data bytes as a [ByteArray]. */
    fun dataBytes(): ByteArray {
        return ByteArray(currentChunkAvailableLen().bytes().toInt())
    }

    /** Split current chunk in the chain at the given offset. */
    fun splitAt(offset: AlignedSize): Pair<ChunkChain, ChunkPart> {
        val currentChunk = this.chunk
        // Take ownership: clear our chunk.
        this.chunk = null
        this.header = null

        if (currentChunk == null) {
            check(offset == AlignedSize.ZERO)
            return Pair(default(), ChunkPart.default())
        }

        check(currentChunk.len() > HEADER_SIZE)

        val (before, after) = currentChunk.splitAtOffset(offset + HEADER_SIZE)
        check(before.len() >= HEADER_SIZE)

        return if (before.len() == HEADER_SIZE) {
            // This branch is only taken in tests of ChunkChain,
            // because real allocator never finishes with an empty last chunk part.
            val prevChain = header?.prev ?: default()
            Pair(prevChain, after)
        } else {
            Pair(
                ChunkChain(
                    chunk = before,
                    header = header,
                ),
                after,
            )
        }
    }

    /** Split at a raw pointer offset. */
    fun splitAtPtr(ptr: Int): Pair<ChunkChain, ChunkPart> {
        check(ptr >= begin())
        check(ptr <= end())
        val offset = AlignedSize.newBytes(ptr - begin())
        return splitAt(offset)
    }

    /** Clear the content invoking provided callback to release the chunks. */
    fun clearWith(chunkDrop: (ChunkPart) -> Unit) {
        val currentChunk = chunk ?: return
        chunk = null

        check(currentChunk.len() >= HEADER_SIZE)

        // Recursively clear the previous chain.
        header?.prev?.clearWith(chunkDrop)
        header = null

        chunkDrop(currentChunk)
    }

    /** Iterator over chain elements. */
    fun iter(): ChunkChainIterator {
        return ChunkChainIterator(next = this)
    }

    /** Number of chain links (depth), not counting the empty tail. */
    fun depth(): Int {
        return iter().asSequence().count().let { (it - 1).coerceAtLeast(0) }
    }

    /** Total allocated bytes across all chunks (data area only). */
    fun allocatedBytes(): Int {
        var total = 0
        for (chain in iter()) {
            total += chain.currentChunkAvailableLen().bytes().toInt()
        }
        return total
    }

    /**
     * Returns the total size of the allocation backing this chunk,
     * including any overhead used by the allocator itself.
     */
    fun allocatedBytesWithMetadata(): Int {
        var total = 0
        for (chain in iter()) {
            val c = chain.chunk
            if (c != null) {
                total += c.allocatedBytesWithMetadata()
            }
        }
        return total
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ChunkChain) return false
        return chunk == other.chunk
    }

    override fun hashCode(): Int = chunk?.hashCode() ?: 0

    override fun toString(): String = "ChunkChain(chunk=$chunk)"
}

/** Iterator over [ChunkChain] elements. */
internal class ChunkChainIterator(
    private var next: ChunkChain?,
) : Iterator<ChunkChain> {

    override fun hasNext(): Boolean = next != null

    override fun next(): ChunkChain {
        val current = next ?: throw NoSuchElementException()
        next = current.prev()
        return current
    }
}
