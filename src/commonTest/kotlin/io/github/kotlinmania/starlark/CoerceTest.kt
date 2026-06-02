// port-lint: tests src/coerce.rs
package io.github.kotlinmania.starlark

/*
 * Copyright 2018 The Starlark in Rust Authors.
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

import io.github.kotlinmania.starlark.values.Tuple1
import kotlin.test.Test
import kotlin.test.assertEquals

class CoerceTest {
    @Test
    fun testSameTypeCoerce() {
        fun f(x: Tuple1<String>): Tuple1<String> = coerce(x)

        assertEquals(Tuple1("test"), f(Tuple1("test")))
    }

    // Layout reinterpretation across distinct generic instantiations has no Kotlin Multiplatform semantic equivalent.
    // The unsound associated-type reinterpretation case has no Kotlin Multiplatform semantic equivalent.
}
