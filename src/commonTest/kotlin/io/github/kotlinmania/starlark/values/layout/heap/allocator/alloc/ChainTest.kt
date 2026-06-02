// port-lint: tests src/values/layout/heap/allocator/alloc/chain.rs
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
import io.github.kotlinmania.starlark.values.layout.heap.AValueHeader
import io.github.kotlinmania.starlark.values.layout.heap.allocator.alloc.chunkpart.ChunkPart
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChainTest {
    @Test
    fun testDefault() {
        val chain = ChunkChain.default()
        assertEquals(AlignedSize.ZERO, chain.currentChunkAvailableLen())
    }

    @Test
    fun testNewDrop() {
        val chunkPart =
            ChunkPart.allocAtLeast(
                AlignedSize.newBytes(10 * AValueHeader.ALIGN),
            )
        val chunkLen = chunkPart.len()
        val chain = ChunkChain.new(chunkPart, ChunkChain.default())
        assertEquals(
            chunkLen,
            chain.currentChunkAvailableLen() + ChunkChain.HEADER_SIZE,
        )
        var dropCalled = false
        chain.clearWith { _ ->
            assertTrue(!dropCalled)
            dropCalled = true
        }
        assertTrue(dropCalled)
    }

    @Test
    fun testNewDropMany() {
        var chain =
            ChunkChain.new(
                ChunkPart.allocAtLeast(AlignedSize.newBytes(10 * AValueHeader.ALIGN)),
                ChunkChain.default(),
            )
        chain =
            ChunkChain.new(
                ChunkPart.allocAtLeast(AlignedSize.newBytes(10 * AValueHeader.ALIGN)),
                chain,
            )
        chain =
            ChunkChain.new(
                ChunkPart.allocAtLeast(AlignedSize.newBytes(10 * AValueHeader.ALIGN)),
                chain,
            )
        var dropCount = 0
        chain.clearWith { _ ->
            dropCount += 1
        }
        assertEquals(3, dropCount)
    }

    @Test
    fun testSplitAt() {
        val chunkPart =
            ChunkPart.allocAtLeast(
                AlignedSize.newBytes(20 * AValueHeader.ALIGN),
            )
        val chunkLen = chunkPart.len()
        val chain = ChunkChain.new(chunkPart, ChunkChain.default())

        val splitSize = AlignedSize.newBytes(3 * AValueHeader.ALIGN)
        val (newChain, chunk) = chain.splitAt(splitSize)
        assertEquals(splitSize, newChain.currentChunkAvailableLen())
        assertEquals(
            chunkLen - splitSize - ChunkChain.HEADER_SIZE,
            chunk.len(),
        )
    }

    @Test
    fun testSplitAtLen() {
        val chunkPart =
            ChunkPart.allocAtLeast(
                AlignedSize.newBytes(20 * AValueHeader.ALIGN),
            )
        val chain = ChunkChain.new(chunkPart, ChunkChain.default())
        val chainLen = chain.currentChunkAvailableLen()

        val (newChain, rem) = chain.splitAt(chainLen)
        assertEquals(chainLen, newChain.currentChunkAvailableLen())
        assertEquals(AlignedSize.ZERO, rem.len())
    }

    @Test
    fun testSplitAtZero() {
        val chunkPart =
            ChunkPart.allocAtLeast(
                AlignedSize.newBytes(20 * AValueHeader.ALIGN),
            )
        val chain = ChunkChain.new(chunkPart, ChunkChain.default())
        val chainLen = chain.currentChunkAvailableLen()
        val (newChain, rem) = chain.splitAt(AlignedSize.ZERO)
        // Should be replaced with underlying chain.
        assertEquals(AlignedSize.ZERO, newChain.currentChunkAvailableLen())
        assertEquals(chainLen, rem.len())
    }

    @Test
    fun testDepth() {
        var chain = ChunkChain.default()
        assertEquals(0, chain.depth())

        chain =
            ChunkChain.new(
                ChunkPart.allocAtLeast(AlignedSize.newBytes(10 * AValueHeader.ALIGN)),
                chain,
            )
        assertEquals(1, chain.depth())

        chain =
            ChunkChain.new(
                ChunkPart.allocAtLeast(AlignedSize.newBytes(20 * AValueHeader.ALIGN)),
                chain,
            )
        assertEquals(2, chain.depth())
    }
}
