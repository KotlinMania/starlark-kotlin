// port-lint: source tests:src/values/layout/pointer.rs
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

import kotlin.test.Test
import kotlin.test.assertEquals

class PointerTest {

    @Test
    fun testIntTag() {
        fun check(x: Int) {
            assertEquals(x, RawPointer.newInt(x).unpackInt())
        }

        for (x in -10..9) {
            check(x)
        }
        check(Int.MAX_VALUE)
        check(Int.MIN_VALUE)
    }
}
