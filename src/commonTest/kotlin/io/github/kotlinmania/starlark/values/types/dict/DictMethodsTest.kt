// port-lint: source tests:src/values/types/dict/methods.rs
package io.github.kotlinmania.starlark.values.types.dict

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

import io.github.kotlinmania.starlark.assert.Assert
import kotlin.test.Test

class DictMethodsTest {
    @Test
    fun testErrorCodes() {
        Assert.fail("""x = {"one": 1}; x.pop("four")""", "not found")
        Assert.fail("x = {}; x.popitem()", "empty")
    }

    @Test
    fun testDictAdd() {
        Assert.fail("{1: 2} + {3: 4}", "not supported")
    }

    @Test
    fun testDictWithDuplicates() {
        // In Starlark spec this is a runtime error. In Python it's fine.
        // We make it a runtime error, plus have a lint that checks for it statically.
        Assert.fails("{40+2: 2, 6*7: 3}", listOf("key repeated", "42"))
        // Also check we fail if the entire dictionary is static (a different code path).
        Assert.fails("{42: 2, 42: 3}", listOf("key repeated", "42"))
    }

    @Test
    fun testDictUpdateWithSelfPos() {
        Assert.eq("{3: 4, 1: 2}", "d = {3: 4, 1: 2}; d.update(d); d")
    }

    @Test
    fun testDictUpdateWithSelfAsKwargs() {
        Assert.eq("{'a': 1, 'b': 2}", "d = {'a': 1, 'b': 2}; d.update(**d); d")
    }

    @Test
    fun testFrozenDictCannotBeUpdatedWithSelfPos() {
        val a = Assert()
        a.module("d.star", "D = {7: 8, 9: 0}")
        a.fail(
            """
load('d.star', 'D')

D.update(D)
""",
            "Immutable",
        )
    }

    @Test
    fun testFrozenDictCannotBeUpdatedWithSelfAsKwargs() {
        val a = Assert()
        a.module("d.star", "D = {'x': 17, 'y': 19}")
        a.fail(
            """
load('d.star', 'D')
D.update(**D)
""",
            "Immutable",
        )
    }
}
