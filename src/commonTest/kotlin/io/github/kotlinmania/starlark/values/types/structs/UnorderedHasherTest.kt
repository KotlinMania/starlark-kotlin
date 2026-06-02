// port-lint: tests src/values/types/structs/unordered_hasher.rs
package io.github.kotlinmania.starlark.values.types.structs

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

class UnorderedHasherTest {
    @Test
    fun testUnorderedHasher() {
        val h = UnorderedHasher()
        h.writeHash(10uL)
        h.writeHash(20uL)
        val h0 = h.finish()

        val h2 = UnorderedHasher()
        h2.writeHash(20uL)
        h2.writeHash(10uL)
        val h1 = h2.finish()

        assertEquals(h0, h1)
    }
}
