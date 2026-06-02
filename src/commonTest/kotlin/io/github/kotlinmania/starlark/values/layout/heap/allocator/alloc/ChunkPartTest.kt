// port-lint: tests src/values/layout/heap/allocator/alloc/chunk_part.rs
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
import io.github.kotlinmania.starlark.values.layout.heap.allocator.alloc.chunkpart.ChunkPart
import kotlin.test.Test
import kotlin.test.assertTrue

class ChunkPartTest {
    @Test
    fun testIsFull() {
        val chunkPart = ChunkPart.new(Chunk.allocAtLeast(AlignedSize.newBytes(100 * AValueHeader.ALIGN)))
        assertTrue(chunkPart.isFull())
    }
}
