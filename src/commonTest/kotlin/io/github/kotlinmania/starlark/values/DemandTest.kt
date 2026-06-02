// port-lint: source tests:src/values/demand.rs
package io.github.kotlinmania.starlark.values

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

import io.github.kotlinmania.starlark.values.layout.heap.Heap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DemandTest {
    private interface SomeTrait {
        fun payload(): Int
    }

    private class MyValue(
        val payload: Int,
    ) : StarlarkValue,
        SomeTrait {
        override val TYPE: String get() = "MyValue"

        override fun payload(): Int = payload

        override fun provide(demand: Demand) {
            demand.provideValue<SomeTrait>(this)
        }
    }

    @Test
    fun testTraitDowncast() {
        Heap.temp { heap ->
            val value = heap.allocSimple(MyValue(17))

            assertNull(value.requestValue<String>())

            val someTrait = value.requestValue<SomeTrait>()
            assertNotNull(someTrait)
            assertEquals(17, someTrait.payload())
        }
    }
}
