// port-lint: source src/values/types/int/inline_int.rs (tests)
package io.github.kotlinmania.starlark.values.types.int

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

import kotlin.test.Test
import kotlin.test.assertEquals

class InlineIntTest {

    @Test
    fun testMinMaxForBits() {
        assertEquals(Pair(-1, 0), InlineInt.minMaxForBits(1))
        assertEquals(Pair(-2, 1), InlineInt.minMaxForBits(2))
        assertEquals(Pair(-4, 3), InlineInt.minMaxForBits(3))
        assertEquals(Pair(Int.MIN_VALUE, Int.MAX_VALUE), InlineInt.minMaxForBits(32))
    }
}

