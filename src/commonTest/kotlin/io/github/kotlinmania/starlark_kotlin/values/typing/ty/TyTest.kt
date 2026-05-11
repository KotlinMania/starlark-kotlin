// port-lint: tests src/values/typing/ty.rs
package io.github.kotlinmania.starlark_kotlin.values.typing.ty

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

import io.github.kotlinmania.starlark_kotlin.assert.Assert
import kotlin.test.Test

internal class TyTest {
    @Test
    fun testIsinstance() {
        Assert.isTrue("isinstance(int, type)")
        Assert.isFalse("isinstance(1, type)")
        Assert.isTrue("isinstance(list[str], type)")
        Assert.isTrue("isinstance(eval_type(list), type)")
    }

    @Test
    fun testPass() {
        Assert.pass(
            """
def accepts_type(t: type):
    pass

def test():
    accepts_type(int)
    accepts_type(list[str])
    accepts_type(None | int)

test()
""",
        )
    }

    @Test
    fun testFailCompileTime() {
        Assert.fail(
            """
def accepts_type(t: type):
    pass

def test():
    accepts_type(1)
""",
            "Expected type `type` but got `int`",
        )
    }
}
