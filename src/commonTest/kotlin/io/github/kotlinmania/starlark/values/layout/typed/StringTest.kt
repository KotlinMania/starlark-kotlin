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

import io.github.kotlinmania.starlarkmap.Hashed
import io.github.kotlinmania.starlark.values.layout.FrozenStringValue
import io.github.kotlinmania.starlark.values.layout.StringValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import kotlin.test.Test
import kotlin.test.assertEquals

class StringTest {

    @Test
    fun testStringHashes() {
        val expected = Hashed.new("xyz").hash()

        Heap.temp { heap ->
            val s: StringValue = heap.allocStr("xyz")
            assertEquals(expected, Hashed.new(s).hash())
            assertEquals(s.getHashed().hash(), s.hashed()!!.hash())
            val v: Value = heap.allocStr("xyz").toValue()
            assertEquals(expected, v.getHashed().getOrThrow().hash())
        }

        val heap = FrozenHeap.new()
        val fs: FrozenStringValue = heap.allocStr("xyz")
        assertEquals(expected, Hashed.new(fs).hash())
        val fv = heap.allocStr("xyz").toFrozenValue()
        assertEquals(expected, fv.getHashed().getOrThrow().hash())
    }
}
