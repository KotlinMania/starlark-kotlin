// port-lint: tests src/values/layout/heap/allocator/alloc/chunk.rs
package io.github.kotlinmania.starlark.values.layout.heap.allocator.alloc

/*
 * Copyright 2018 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not import this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 */

import io.github.kotlinmania.starlark.values.layout.AlignedSize
import io.github.kotlinmania.starlark.values.layout.heap.AValueHeader
import io.github.kotlinmania.starlark.values.layout.heap.allocator.alloc.chunk.Chunk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChunkTest {
    @Test
    fun testNewIsEmpty() {
        val chunk = Chunk.default()
        assertTrue(chunk.isEmpty())
        assertEquals(AlignedSize.ZERO, chunk.len())
        assertEquals(0, chunk.refCount())
    }

    @Test
    fun testAllocRelease() {
        val chunk = Chunk.allocAtLeast(AlignedSize.newBytes(100 * AValueHeader.ALIGN))
        assertEquals(
            AlignedSize.newBytes(128 * AValueHeader.ALIGN) - Chunk.HEADER_SIZE,
            chunk.len(),
        )
        assertEquals(1, chunk.refCount())
        val chunk2 = chunk.duplicate()
        assertEquals(2, chunk.refCount())
        chunk.release()
        assertEquals(1, chunk2.refCount())
    }
}
