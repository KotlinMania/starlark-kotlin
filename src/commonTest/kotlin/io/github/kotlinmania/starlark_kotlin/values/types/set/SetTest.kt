// port-lint: tests src/values/types/set/set.rs
package io.github.kotlinmania.starlark_kotlin.values.types.set

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

internal class SetTest {
    @Test
    fun testSetTypeAsTypeCompileTime() {
        Assert.fail(
            """
def f_fail_ct(x: set[int]):
    return x

s = set(['not_int'])

f_fail_ct(s)
""",
            "Value `set([\"not_int\"])` of type `set` does not match the type annotation `set[int]` for argument `x`",
        )
    }

    @Test
    fun testReturnSetTypeAsTypeCompileTime() {
        Assert.fail(
            """
def f_fail_ct(x: str) -> set[int]:
    return set([x])

f_fail_ct('not_int')
""",
            "Value `set([\"not_int\"])` of type `set` does not match the type annotation `set[int]` for return type",
        )
    }

    @Test
    fun testSetTypeAsTypeRunTime() {
        Assert.fail(
            """
def f_fail_rt(x: set[int]):
    return x

s = set(['not_int'])

noop(f_fail_rt)(s)
""",
            "Value `set([\"not_int\"])` of type `set` does not match the type annotation `set[int]` for argument `x`",
        )
    }
}
