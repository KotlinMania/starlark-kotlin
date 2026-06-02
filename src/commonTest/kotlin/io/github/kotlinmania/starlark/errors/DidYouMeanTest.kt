// port-lint: tests src/errors/did_you_mean.rs (tests)
package io.github.kotlinmania.starlark.errors

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

import io.github.kotlinmania.starlark.errors.didYouMean
import kotlin.test.Test
import kotlin.test.assertEquals

class DidYouMeanTest {
    @Test
    fun prefixes() {
        assertEquals(
            "cxx_library",
            didYouMean("cxx_librar", listOf("cxx_library")),
        )
        assertEquals(
            "cxx_library",
            didYouMean("cxx_libra", listOf("cxx_library")),
        )
        assertEquals(null, didYouMean("cxx_libr", listOf("cxx_library")))
    }

    @Test
    fun typos() {
        assertEquals(
            "cxx_library",
            didYouMean("cxx_librarx", listOf("cxx_library")),
        )
        assertEquals(
            "cxx_library",
            didYouMean("cxx_libraxx", listOf("cxx_library")),
        )
        assertEquals(null, didYouMean("cxx_librxxx", listOf("cxx_library")))
    }

    @Test
    fun best() {
        assertEquals("abc", didYouMean("abx", listOf("abc", "abcd")))
    }

    @Test
    fun veryShort() {
        assertEquals("a", didYouMean("b", listOf("a")))
        assertEquals("ab", didYouMean("b", listOf("ab")))
        assertEquals(null, didYouMean("b", listOf("cd")))

        assertEquals(null, didYouMean("bc", listOf("de")))
    }

    @Test
    fun earlierVariantsAreMoreImportant() {
        assertEquals("aaaay", didYouMean("aaaax", listOf("aaaay", "aaaaz")))
        assertEquals("aaaaz", didYouMean("aaaax", listOf("aaaaz", "aaaay")))
    }
}
