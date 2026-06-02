// port-lint: tests src/values/types/list/value.rs
package io.github.kotlinmania.starlark_kotlin.values.types.list

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

class ValueTest {
    @Test
    fun testToStr() {
        val a = Assert()
        a.allTrue(
            """
str([1, 2, 3]) == "[1, 2, 3]"
str([1, [2, 3]]) == "[1, [2, 3]]"
str([]) == "[]"
""",
        )
    }

    @Test
    fun testReprCycle() {
        val a = Assert()
        a.eq("l = []; l.append(l); repr(l)", "'[[...]]'")
        a.eq("l = []; l.append(l); str(l)", "'[[...]]'")
    }

    @Test
    fun testMutateList() {
        val a = Assert()
        a.isTrue(
            """
v = [1, 2, 3]
v[1] = 1
v[2] = [2, 3]
v == [1, 1, [2, 3]]
""",
        )
    }

    @Test
    fun testArithmeticOnList() {
        val a = Assert()
        a.allTrue(
            """
[1, 2, 3] + [2, 3] == [1, 2, 3, 2, 3]
[1, 2, 3] * 3 == [1, 2, 3, 1, 2, 3, 1, 2, 3]
""",
        )
    }

    @Test
    fun testValueAlias() {
        val a = Assert()
        a.isTrue(
            """
v1 = [1, 2, 3]
v2 = v1
v2[2] = 4
v1 == [1, 2, 4] and v2 == [1, 2, 4]
""",
        )
    }

    @Test
    fun testMutatingImports() {
        val a = Assert()
        a.module(
            "x",
            """
frozen_list = [1, 2]
frozen_list += [4]
def frozen_list_result():
    return frozen_list
def list_result():
    return [1, 2, 4]
""",
        )
        a.fail("load('x', 'frozen_list')\nfrozen_list += [1]", "Immutable")
        a.fail(
            "load('x', 'frozen_list_result')\nx = frozen_list_result()\nx += [1]",
            "Immutable",
        )
        a.isTrue("load('x', 'list_result')\nx = list_result()\nx += [8]\nx == [1, 2, 4, 8]")
    }

    @Test
    fun testCompare() {
        val a = Assert()
        a.isTrue("[1, 2] < [10]")
    }
}
