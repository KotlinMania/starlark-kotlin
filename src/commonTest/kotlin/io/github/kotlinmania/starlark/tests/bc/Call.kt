// port-lint: source tests/bc/call.rs
package io.github.kotlinmania.starlark.tests.bc

import kotlin.test.Test

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

class CallTests {
    @Test
    fun testCall() {
        bcGoldenTest(
            "call",
            """
    def test(a, k):
        noop(
            10,
            20,
            p=30,
            q=40,
            r=50,
            *a,
            **k,
        )
    """,
        )
    }
}
