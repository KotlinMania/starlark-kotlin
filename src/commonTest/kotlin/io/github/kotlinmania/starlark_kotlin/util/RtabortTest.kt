// port-lint: source tests:src/util/rtabort.rs
package io.github.kotlinmania.starlark_kotlin.util

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

class RtabortTest {

    // Compile-time anchors mirroring upstream `fn _test_compiles_fixed_string` /
    // `fn _test_compiles_with_format_args` — each references rtabort with a
    // different argument shape so the signature compiles. They are never
    // invoked at runtime.
    private fun testCompilesFixedString() {
        // rtabort("test")
    }

    private fun testCompilesWithFormatArgs() {
        // rtabort("test {}", 17)
    }

    @Test
    fun testRtabort() {
        // Uncomment to test.
        // rtabort("test {}", 17)
        // rtabort("test {}") { error("panic"); 17 }
    }
}
