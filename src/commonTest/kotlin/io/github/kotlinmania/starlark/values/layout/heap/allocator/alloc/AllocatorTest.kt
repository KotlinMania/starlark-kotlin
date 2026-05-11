// port-lint: source values/layout/heap/allocator/alloc/allocator.rs
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
import io.github.kotlinmania.starlark.values.layout.heap.allocator.alloc.chunk.Chunk
import io.github.kotlinmania.starlark.values.layout.ValueAllocSize
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AllocatorTest {

    /** AValueHeader::ALIGN = 8 in Rust */
    private val ALIGN = 8

    @Test
    fun testSmall() {
        val allocator = ChunkAllocator()
        val p0 = allocator.alloc(ValueAllocSize.new(AlignedSize.newBytes(3 * ALIGN)))
        val p1 = allocator.alloc(ValueAllocSize.new(AlignedSize.newBytes(4 * ALIGN)))
        val p2 = allocator.alloc(ValueAllocSize.new(AlignedSize.newBytes(5 * ALIGN)))
        // Verify allocations returned offsets.
        assertTrue(p0 is Int)
        assertTrue(p1 is Int)
        assertTrue(p2 is Int)
        // Verify contiguous allocation: p1 - p0 = 3*ALIGN, p2 - p1 = 4*ALIGN
        assertEquals(3 * ALIGN, (p1) - (p0))
        assertEquals(4 * ALIGN, (p2) - (p1))

        val chunks = allocator.iterAllocatedChunksRev().toList()
        assertEquals(1, chunks.size)
        assertEquals((3 + 4 + 5) * ALIGN, chunks[0].size)
    }

    @Test
    fun testBig() {
        val allocator = ChunkAllocator()
        allocator.alloc(ValueAllocSize.new(
            AlignedSize.newBytes(128 shl 10) - Chunk.HEADER_SIZE
        ))
    }

    private fun randomIteration(seed: Int) {
        val rng = Random(seed.toLong())

        var expectedTotalSizeBytes = 0
        val allocator = ChunkAllocator()
        for (j in 0 until seed) {
            val size = when (rng.nextInt(3)) {
                0 -> rng.nextInt(10)
                1 -> rng.nextInt(100)
                2 -> rng.nextInt(1000)
                else -> error("unreachable")
            }
            val valueSize = ValueAllocSize.tryNew(AlignedSize.newBytes(size * ALIGN))
                ?: continue
            allocator.alloc(valueSize)
            expectedTotalSizeBytes += valueSize.bytes().toInt()
        }

        val actualTotalSizeBytes = allocator.iterAllocatedChunksRev()
            .sumOf { it.size }
        assertEquals(expectedTotalSizeBytes, actualTotalSizeBytes)

        // And do the same assertion after finishing.
        allocator.finish()

        val actualAfterFinish = allocator.iterAllocatedChunksRev()
            .sumOf { it.size }
        assertEquals(expectedTotalSizeBytes, actualAfterFinish)
    }

    @Test
    fun testMany() {
        for (i in 0 until 100) {
            // Reduced from 10000 in Rust for faster test runs.
            randomIteration(i)
        }
    }
}
