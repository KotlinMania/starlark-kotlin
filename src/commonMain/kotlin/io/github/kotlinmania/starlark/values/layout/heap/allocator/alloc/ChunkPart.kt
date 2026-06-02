// port-lint: source src/values/layout/heap/allocator/alloc/chunk_part.rs
package io.github.kotlinmania.starlark.values.layout.heap.allocator.alloc.chunkpart

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
import io.github.kotlinmania.starlark.values.layout.heap.allocator.alloc.chunk.Chunk

/** Chunk is shared by multiple `ChunkPart`s. */
//     allocation: Chunk,
//     begin: AlignedSize,
//     end: AlignedSize,
// }
internal class ChunkPart(
    internal val allocation: Chunk,
    /** Offset from the chunk data. */
    private val begin: AlignedSize,
    /** Offset from the chunk data. */
    private val end: AlignedSize,
) {
    companion object {
        // impl Default for ChunkPart
        fun default(): ChunkPart =
            ChunkPart(
                allocation = Chunk.default(),
                begin = AlignedSize.ZERO,
                end = AlignedSize.ZERO,
            )

        /** Create a chunk part from a whole chunk. */
        fun new(allocation: Chunk): ChunkPart {
            val len = allocation.len()
            return newSubslice(allocation, AlignedSize.ZERO, len)
        }

        fun newSubslice(
            allocation: Chunk,
            begin: AlignedSize,
            end: AlignedSize,
        ): ChunkPart {
            require(begin <= end)
            require(end <= allocation.len())
            return ChunkPart(allocation, begin, end)
        }

        /** Allocate a chunk part to store at least `len`. */
        fun allocAtLeast(len: AlignedSize): ChunkPart = new(Chunk.allocAtLeast(len))
    }

    fun len(): AlignedSize = end.uncheckedSub(begin)

    fun begin(): Int = allocation.ptrAtOffset(begin)

    fun ptrAtOffset(offset: AlignedSize): Int = allocation.ptrAtOffset(begin + offset)

    fun end(): Int = allocation.ptrAtOffset(end)

    fun allocatedBytesWithMetadata(): Int =
        if (chunkRefCount() == 1) {
            allocation.allocatedBytesWithMetadata()
        } else {
            // We cannot know for sure, so try the best to estimate.
            (len().bytes() + Chunk.HEADER_SIZE.bytes() / chunkRefCount().toUInt()).toInt()
        }

    /** Does this chunk part occupy the whole chunk? */
    fun isFull(): Boolean = len() == allocation.len()

    fun splitAtOffset(offset: AlignedSize): Pair<ChunkPart, ChunkPart> =
        if (offset == AlignedSize.ZERO) {
            Pair(default(), this)
        } else if (offset == len()) {
            Pair(this, default())
        } else {
            require(offset <= len())
            val offsetRelativeToChunk = begin + offset
            Pair(
                newSubslice(allocation.duplicate(), begin, offsetRelativeToChunk),
                newSubslice(allocation, offsetRelativeToChunk, end),
            )
        }

    fun chunkRefCount(): Int = allocation.refCount()

    fun chunkPtrEq(other: ChunkPart): Boolean = allocation.ptrEq(other.allocation)

    // impl PartialEq for ChunkPart
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ChunkPart) return false
        return allocation == other.allocation && begin == other.begin && end == other.end
    }

    override fun hashCode(): Int {
        var result = allocation.hashCode()
        result = 31 * result + begin.hashCode()
        result = 31 * result + end.hashCode()
        return result
    }

    // impl Debug for ChunkPart
    override fun toString(): String = "ChunkPart(begin=$begin, end=$end)"
}

// Tests are in commonTest, not here.
