// port-lint: source values/thinBoxSliceFrozenValue/thinBox.rs
package io.github.kotlinmania.starlark.values.thinboxslicefrozenvalue

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
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import kotlin.test.Test
import kotlin.test.assertEquals

class ThinBoxTest {
    @Test
    fun testEmpty() {
        val thin = AllocatedThinBoxSlice.empty<String>()
        assertEquals(0, thin.size)
        thin.runDrop()
    }

    @Test
    fun testFromIterSized() {
        val thin = AllocatedThinBoxSlice.fromIter(listOf("a", "bb", "ccc"))
        assertEquals(listOf("a", "bb", "ccc"), thin.toList())
        thin.runDrop()
    }

    @Test
    fun testFromIterUnknownSize() {
        val thin = AllocatedThinBoxSlice.fromIter(
            listOf("a", "b", "c").filter { true }
        )
        assertEquals(listOf("a", "b", "c"), thin.toList())
        thin.runDrop()
    }

    /** If there are obvious memory violations, this test will catch them. */
    @Test
    fun testStress() {
        for (i in 0 until 1000) {
            val thin = AllocatedThinBoxSlice.fromIter((0 until i).map { j -> j.toString() })
            assertEquals(i, thin.size)
            assertEquals((0 until i).map { j -> j.toString() }, thin.toList())
            thin.runDrop()
        }
    }
}
