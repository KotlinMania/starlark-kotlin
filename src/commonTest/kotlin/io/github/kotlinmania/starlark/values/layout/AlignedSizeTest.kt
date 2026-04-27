// port-lint: source src/values/layout/alignedSize.rs
package io.github.kotlinmania.starlark.values.layout

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

import io.github.kotlinmania.starlark.values.layout.heap.AValueHeader
import kotlin.test.Test
import kotlin.test.assertEquals

class AlignedSizeTest {
    @Test
    fun testCheckedNextPowerOfTwo() {
        assertEquals(
            AlignedSize.newBytes(AValueHeader.ALIGN),
            AlignedSize.newBytes(AValueHeader.ALIGN)
                .checkedNextPowerOfTwo()!!,
        )
        assertEquals(
            AlignedSize.newBytes(2 * AValueHeader.ALIGN),
            AlignedSize.newBytes(2 * AValueHeader.ALIGN)
                .checkedNextPowerOfTwo()!!,
        )
        assertEquals(
            AlignedSize.newBytes(4 * AValueHeader.ALIGN),
            AlignedSize.newBytes(3 * AValueHeader.ALIGN)
                .checkedNextPowerOfTwo()!!,
        )
        assertEquals(
            AlignedSize.newBytes(8 * AValueHeader.ALIGN),
            AlignedSize.newBytes(5 * AValueHeader.ALIGN)
                .checkedNextPowerOfTwo()!!,
        )
    }

    @Test
    fun testSub() {
        assertEquals(
            AlignedSize.newBytes(2 * AValueHeader.ALIGN),
            AlignedSize.newBytes(5 * AValueHeader.ALIGN)
                - AlignedSize.newBytes(3 * AValueHeader.ALIGN),
        )
    }
}
