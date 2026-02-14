// port-lint: source src/values/layout/heap/allocator/alloc/chunk_part.rs
package io.github.kotlinmania.starlark_kotlin.values.layout.heap.allocator.alloc.chunk_part

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

import io.github.kotlinmania.starlark_kotlin.values.layout.aligned_size.AlignedSize
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.allocator.alloc.chunk.Chunk

/// Chunk is shared by multiple `ChunkPart`s.
// #[derive(Debug, Default, PartialEq)]
// pub(crate) struct ChunkPart {
//     allocation: Chunk,
//     begin: AlignedSize,
//     end: AlignedSize,
// }
internal class ChunkPart(
    internal val allocation: Chunk,
    /// Offset from the chunk data.
    private val begin: AlignedSize,
    /// Offset from the chunk data.
    private val end: AlignedSize,
) {
    companion object {
        // impl Default for ChunkPart
        fun default(): ChunkPart = ChunkPart(
            allocation = Chunk.default(),
            begin = AlignedSize.ZERO,
            end = AlignedSize.ZERO,
        )

        /// Create a chunk part from a whole chunk.
        // pub(crate) fn new(allocation: Chunk) -> ChunkPart
        fun new(allocation: Chunk): ChunkPart {
            val len = allocation.len()
            return newSubslice(allocation, AlignedSize.ZERO, len)
        }

        // pub(crate) fn new_subslice(allocation, begin, end) -> ChunkPart
        fun newSubslice(
            allocation: Chunk,
            begin: AlignedSize,
            end: AlignedSize,
        ): ChunkPart {
            require(begin <= end)
            require(end <= allocation.len())
            return ChunkPart(allocation, begin, end)
        }

        /// Allocate a chunk part to store at least `len`.
        // pub(crate) fn alloc_at_least(len: AlignedSize) -> ChunkPart
        fun allocAtLeast(len: AlignedSize): ChunkPart {
            return new(Chunk.allocAtLeast(len))
        }
    }

    // pub(crate) fn len(&self) -> AlignedSize
    fun len(): AlignedSize {
        return end.uncheckedSub(begin)
    }

    // pub(crate) fn begin(&self) -> NonNull<usize>
    fun begin(): Int {
        return allocation.ptrAtOffset(begin)
    }

    // pub(crate) fn ptr_at_offset(&self, offset: AlignedSize) -> NonNull<usize>
    fun ptrAtOffset(offset: AlignedSize): Int {
        return allocation.ptrAtOffset(begin + offset)
    }

    // pub(crate) fn end(&self) -> NonNull<usize>
    fun end(): Int {
        return allocation.ptrAtOffset(end)
    }

    // pub(crate) fn allocated_bytes_with_metadata(&self) -> usize
    fun allocatedBytesWithMetadata(): Int {
        return if (chunkRefCount() == 1) {
            allocation.allocatedBytesWithMetadata()
        } else {
            // We cannot know for sure, so try the best to estimate.
            len().bytes() + Chunk.HEADER_SIZE.bytes() / chunkRefCount()
        }
    }

    /// Does this chunk part occupy the whole chunk?
    // pub(crate) fn is_full(&self) -> bool
    fun isFull(): Boolean {
        return len() == allocation.len()
    }

    // pub(crate) fn split_at_offset(self, offset: AlignedSize) -> (ChunkPart, ChunkPart)
    fun splitAtOffset(offset: AlignedSize): Pair<ChunkPart, ChunkPart> {
        return if (offset == AlignedSize.ZERO) {
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
    }

    // pub(crate) fn chunk_ref_count(&self) -> u32
    fun chunkRefCount(): Int {
        return allocation.refCount()
    }

    // #[cfg(test)]
    // pub(crate) fn chunk_ptr_eq(&self, other: &ChunkPart) -> bool
    fun chunkPtrEq(other: ChunkPart): Boolean {
        return allocation.ptrEq(other.allocation)
    }

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
    override fun toString(): String {
        return "ChunkPart(begin=$begin, end=$end)"
    }
}

// #[cfg(test)] mod tests
// Tests are in commonTest, not here.
