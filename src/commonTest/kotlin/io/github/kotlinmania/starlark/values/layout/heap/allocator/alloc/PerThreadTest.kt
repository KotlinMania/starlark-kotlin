// port-lint: source values/layout/heap/allocator/alloc/per_thread.rs
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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PerThreadTest {

    /** AValueHeader::ALIGN = 8 in Rust */
    private val ALIGN = 8

    @Test
    fun testReleasePartial() {
        val allocator = PerThreadChunkCache()
        val chunk = ChunkPart.allocAtLeast(AlignedSize.newBytes(10 * ALIGN))
        val (a, b) = chunk.splitAtOffset(AlignedSize.newBytes(5 * ALIGN))
        val oldABegin = a.begin()
        val oldBBegin = b.begin()
        allocator.store(a)
        allocator.store(b)
        val fetchedA = allocator.fetch(AlignedSize.newBytes(3 * ALIGN))
        val fetchedB = allocator.fetch(AlignedSize.newBytes(3 * ALIGN))
        assertNotNull(fetchedA)
        assertNotNull(fetchedB)
        // Verify we got back the same chunks (in some order).
        assertTrue(
            (fetchedA.begin() == oldABegin || fetchedB.begin() == oldABegin),
            "Expected to recover original chunk A"
        )
        assertTrue(
            (fetchedA.begin() == oldBBegin || fetchedB.begin() == oldBBegin),
            "Expected to recover original chunk B"
        )
    }

    @Test
    fun testStoreAndFetchOrder() {
        val allocator = PerThreadChunkCache()
        // Store keeps largest chunks — verify fetch returns them.
        val small = ChunkPart.allocAtLeast(AlignedSize.newBytes(2 * ALIGN))
        val large = ChunkPart.allocAtLeast(AlignedSize.newBytes(20 * ALIGN))
        allocator.store(small)
        allocator.store(large)
        // Fetch requiring large size should succeed.
        val fetched = allocator.fetch(AlignedSize.newBytes(15 * ALIGN))
        assertNotNull(fetched)
        assertTrue(fetched.len() >= AlignedSize.newBytes(15 * ALIGN))
    }

    @Test
    fun testFetchEmpty() {
        val allocator = PerThreadChunkCache()
        val result = allocator.fetch(AlignedSize.newBytes(ALIGN))
        assertNull(result)
    }

    @Test
    fun testNextChunkSize() {
        // First chunk: 512
        assertEquals(512u, nextChunkSize(0).bytes())
        // Second chunk: 1024
        assertEquals(1024u, nextChunkSize(1).bytes())
        // Third chunk: 2048
        assertEquals(2048u, nextChunkSize(2).bytes())
    }
}
