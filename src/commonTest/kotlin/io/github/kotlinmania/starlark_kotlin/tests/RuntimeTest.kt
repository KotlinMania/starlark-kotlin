// port-lint: tests tests/runtime.rs (tests)
package io.github.kotlinmania.starlark_kotlin.tests

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

import io.github.kotlinmania.starlark_kotlin.assert.fail
import io.github.kotlinmania.starlark_kotlin.assert.pass
import kotlin.test.Test
import kotlin.test.assertTrue

class RuntimeTest {
    @Test
    fun testGarbageCollect() {
        pass(
            """
x = (100, [{"test": None}], True)
y = str(x)
garbage_collect()
assert_eq(y, str(x))
            """.trimIndent(),
        )
    }

    @Test
    fun testCallstack() {
        val e = fail(
            """
def f():
    fail("bad")
f()
            """.trimIndent(),
            "bad",
        )
        assertTrue(e.toString().contains("fail(\"bad\")"))
    }
}

