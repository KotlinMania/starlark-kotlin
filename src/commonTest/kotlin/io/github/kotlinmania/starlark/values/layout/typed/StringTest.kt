// port-lint: source tests:src/values/layout/typed/string.rs
package io.github.kotlinmania.starlark.values.layout.typed

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

import io.github.kotlinmania.starlark.collections.Hashed
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.types.string.allocFrozenStringValue
import io.github.kotlinmania.starlark.values.types.string.allocStringValue
import kotlin.test.Test
import kotlin.test.assertEquals

class StringTest {
    @Test
    fun testStringHashes() {
        val expected = Hashed.new("xyz").hash()

        Heap.temp { heap ->
            val s: StringValue = "xyz".allocStringValue(heap)
            assertEquals(expected, Hashed.new(s).hash())
            assertEquals(expected, s.getHashed().hash())
            val v: Value = heap.allocStr("xyz")
            assertEquals(expected, v.getHashed().getOrThrow().hash())
        }

        val heap = FrozenHeap.new()
        val fs: FrozenStringValue = "xyz".allocFrozenStringValue(heap)
        assertEquals(expected, Hashed.new(fs).hash())
        val fv = "xyz".allocFrozenStringValue(heap).toFrozenValue()
        assertEquals(expected, fv.getHashed().getOrThrow().hash())
    }
}
