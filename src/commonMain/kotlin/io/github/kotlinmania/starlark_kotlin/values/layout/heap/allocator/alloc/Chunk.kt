// port-lint: source src/values/layout/heap/allocator/alloc/chunk.rs
package io.github.kotlinmania.starlark_kotlin.values.layout.heap.allocator.alloc.chunk

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

// #[repr(C)]
// struct ChunkData {
//     ref_count: AtomicU32,
//     len: AlignedSize,
//     data: [MaybeUninit<usize>; 0],
// }
// Kotlin: ChunkData manages a ByteArray instead of raw pointers.
private class ChunkData(
    val len: AlignedSize,
) {
    val refCount = atomic(1)
    /// The backing data buffer.
    val data: ByteArray = ByteArray(len.bytes().toInt())

    companion object {
        // fn layout_for_len(len: AlignedSize) -> Layout
        fun layoutForLen(len: AlignedSize): Int {
            return HEADER_SIZE_BYTES + len.bytes().toInt()
        }

        // fn alloc_ref_count_1(len: AlignedSize) -> NonNull<ChunkData>
        fun allocRefCount1(len: AlignedSize): ChunkData {
            require(len > AlignedSize.ZERO)
            return ChunkData(len)
        }

        // Approximate header size in bytes (for allocation math).
        const val HEADER_SIZE_BYTES: Int = 8 // ref_count (4) + len (4)
    }

    // #[inline]
    // fn begin(&self) -> NonNull<usize>
    // Kotlin: offset 0 into data array.
}

/// Identical to `ChunkData`, but does not have `UnsafeCell`, so it is statically allocated.
// struct ChunkDataEmpty { ref_count: u32, len_words: AlignedSize, data: [...] }
// Kotlin: represented by null ChunkData in Chunk.

// static EMPTY_ALLOC: ChunkDataEmpty
// Kotlin: represented by Chunk with null data.

// impl fmt::Debug for ChunkData
// Kotlin: toString() on ChunkData

/// Refcounted chunk of memory.
// #[derive(PartialEq, Eq)]
// pub(crate) struct Chunk {
//     ptr: NonNull<ChunkData>,
// }
internal class Chunk private constructor(
    private val chunkData: ChunkData?,
) {
    // impl Default for Chunk
    // fn default() -> Chunk
    // Kotlin: use companion empty() factory.

    companion object {
        // pub(crate) const HEADER_SIZE: AlignedSize = AlignedSize::of::<ChunkData>();
        val HEADER_SIZE: AlignedSize = AlignedSize.newBytes(ChunkData.HEADER_SIZE_BYTES)

        // Equivalent to Default::default() — returns empty chunk.
        fun default(): Chunk = allocAtLeast(AlignedSize.ZERO)

        /// Allocate chunk which can hold at least `len_words` words.
        // #[inline]
        // pub(crate) fn alloc_at_least(len: AlignedSize) -> Chunk
        fun allocAtLeast(len: AlignedSize): Chunk {
            return if (len == AlignedSize.ZERO) {
                Chunk(chunkData = null)
            } else {
                allocAtLeastNotEmpty(len)
            }
        }

        // fn alloc_at_least_not_empty(len: AlignedSize) -> Chunk
        private fun allocAtLeastNotEmpty(len: AlignedSize): Chunk {
            val allocLen = HEADER_SIZE + len
            // Round up to power of two to avoid spacing in allocation.
            val allocLenPow2 = allocLen.checkedNextPowerOfTwo()
            val actualLen = allocLenPow2 - HEADER_SIZE
            return Chunk(ChunkData.allocRefCount1(actualLen))
        }
    }

    // impl fmt::Debug for Chunk
    override fun toString(): String {
        return "Chunk(data=$chunkData)"
    }

    // #[inline]
    // pub(crate) fn ref_count(&self) -> u32
    fun refCount(): Int {
        return chunkData?.refCount?.value ?: 0
    }

    // #[cfg(test)]
    // pub(crate) fn ptr_eq(&self, other: &Chunk) -> bool
    fun ptrEq(other: Chunk): Boolean {
        return chunkData === other.chunkData
    }

    // #[inline]
    // fn data(&self) -> &ChunkData
    private fun data(): ChunkData? = chunkData

    // #[inline]
    // pub(crate) fn len(&self) -> AlignedSize
    fun len(): AlignedSize = chunkData?.len ?: AlignedSize.ZERO

    // #[inline]
    // pub(crate) fn allocated_bytes_with_metadata(&self) -> Int
    fun allocatedBytesWithMetadata(): Int {
        return if (isEmpty()) {
            0
        } else {
            ChunkData.layoutForLen(len())
        }
    }

    // #[inline]
    // fn is_empty(&self) -> bool
    fun isEmpty(): Boolean = chunkData == null

    // #[inline]
    // pub(crate) fn begin(&self) -> NonNull<usize>
    // Kotlin: returns offset 0 — callers use ptrAtOffset.

    // #[inline]
    // pub(crate) fn ptr_at_offset(&self, offset: AlignedSize) -> NonNull<usize>
    // Kotlin: returns the byte offset into the data array.
    fun ptrAtOffset(offset: AlignedSize): Int {
        return offset.bytes().toInt()
    }

    /// Access the raw backing data array.
    fun dataBytes(): ByteArray? = chunkData?.data

    // impl Clone for Chunk
    // fn clone(&self) -> Self
    fun duplicate(): Chunk {
        if (isEmpty()) {
            return default()
        }
        val prev = chunkData!!.refCount.getAndIncrement()
        if (prev > Int.MAX_VALUE / 2) {
            error("Refcount overflow")
        }
        return Chunk(chunkData)
    }

    // impl Drop for Chunk
    // fn drop(&mut self)
    // Kotlin: GC handles deallocation; decrement ref count for accounting.
    fun release() {
        if (isEmpty()) return
        chunkData!!.refCount.decrementAndGet()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Chunk) return false
        return chunkData === other.chunkData
    }

    override fun hashCode(): Int = chunkData?.hashCode() ?: 0
}

// #[cfg(test)] mod tests
// Tests are in commonTest, not here.
