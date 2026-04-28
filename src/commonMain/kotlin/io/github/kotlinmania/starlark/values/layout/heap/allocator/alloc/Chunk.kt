// port-lint: source src/values/layout/heap/allocator/alloc/chunk.rs
package io.github.kotlinmania.starlark.values.layout.heap.allocator.alloc.chunk

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
import kotlin.concurrent.atomics.AtomicInt

private class ChunkData(
    val len: AlignedSize,
) {
    val refCount = AtomicInt(1)
    /** The backing data buffer. */
    val data: ByteArray = ByteArray(len.bytes().toInt())

    companion object {
        fun layoutForLen(len: AlignedSize): Int {
            return HEADER_SIZE_BYTES + len.bytes().toInt()
        }

        fun allocRefCount1(len: AlignedSize): ChunkData {
            require(len > AlignedSize.ZERO)
            return ChunkData(len)
        }

        // Approximate header size in bytes (for allocation math).
        const val HEADER_SIZE_BYTES: Int = 8
    }
}

/** Refcounted chunk of memory. */
internal class Chunk private constructor(
    private val chunkData: ChunkData?,
) {
    companion object {
        val HEADER_SIZE: AlignedSize = AlignedSize.newBytes(ChunkData.HEADER_SIZE_BYTES)

        // Equivalent to Default::default() — returns empty chunk.
        fun default(): Chunk = allocAtLeast(AlignedSize.ZERO)

        /** Allocate chunk which can hold at least `lenWords` words. */
        fun allocAtLeast(len: AlignedSize): Chunk {
            return if (len == AlignedSize.ZERO) {
                Chunk(chunkData = null)
            } else {
                allocAtLeastNotEmpty(len)
            }
        }

        private fun allocAtLeastNotEmpty(len: AlignedSize): Chunk {
            val allocLen = HEADER_SIZE + len
            // Round up to power of two to avoid spacing in allocation.
            val allocLenPow2 = allocLen.checkedNextPowerOfTwo()
            val actualLen = (allocLenPow2 ?: allocLen) - HEADER_SIZE
            return Chunk(ChunkData.allocRefCount1(actualLen))
        }
    }

    override fun toString(): String {
        return "Chunk(data=$chunkData)"
    }

    fun refCount(): Int {
        return chunkData?.refCount?.load() ?: 0
    }

    fun ptrEq(other: Chunk): Boolean {
        return chunkData === other.chunkData
    }

    private fun data(): ChunkData? = chunkData

    fun len(): AlignedSize = chunkData?.len ?: AlignedSize.ZERO

    fun allocatedBytesWithMetadata(): Int {
        return if (isEmpty()) {
            0
        } else {
            ChunkData.layoutForLen(len())
        }
    }

    fun isEmpty(): Boolean = chunkData == null

    fun ptrAtOffset(offset: AlignedSize): Int {
        return offset.bytes().toInt()
    }

    /** Access the raw backing data array. */
    fun dataBytes(): ByteArray? = chunkData?.data

    fun duplicate(): Chunk {
        if (isEmpty()) {
            return default()
        }
        val prev = chunkData!!.refCount.fetchAndAdd(1)
        if (prev > Int.MAX_VALUE / 2) {
            error("Refcount overflow")
        }
        return Chunk(chunkData)
    }

    fun release() {
        if (isEmpty()) return
        chunkData!!.refCount.fetchAndAdd(-1)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Chunk) return false
        return chunkData === other.chunkData
    }

    override fun hashCode(): Int = chunkData?.hashCode() ?: 0
}

// Tests are in commonTest, not here.
