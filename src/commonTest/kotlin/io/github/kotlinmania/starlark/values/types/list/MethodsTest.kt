// port-lint: tests src/values/types/list/methods.rs
package io.github.kotlinmania.starlark.values.types.list

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

import io.github.kotlinmania.starlark.assert.Assert
import kotlin.test.Test

internal class MethodsTest {
    @Test
    fun testErrorCodes() {
        val a = Assert()
        a.fail(
            "x = [1, 2, 3, 2]; x.remove(2); x.remove(2); x.remove(2)",
            "not found in list",
        )
    }

    @Test
    fun testIndex() {
        val a = Assert()
        a.fail("[True].index(True, 1, 0)", "not found")
    }

    @Test
    fun testRecursiveList() {
        val a = Assert()
        a.isTrue(
            """
cyclic = [1, 2, 3]
cyclic[1] = cyclic
len(cyclic) == 3 and len(cyclic[1]) == 3
""",
        )
    }
}
