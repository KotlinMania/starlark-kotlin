// port-lint: tests src/values/typing/iter.rs
package io.github.kotlinmania.starlark_kotlin.values.typing

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

import io.github.kotlinmania.starlark_kotlin.assert.Assert
import kotlin.test.Test

internal class IterTest {
    @Test
    fun testIterableRuntime() {
        Assert.isTrue("isinstance([1, 2, 3], typing.Iterable)")
        Assert.isTrue("isinstance((1, 2, 3), typing.Iterable)")
        Assert.isTrue("isinstance(range(10), typing.Iterable)")
        Assert.isFalse("isinstance('', typing.Iterable)")
        Assert.isFalse("isinstance(1, typing.Iterable)")
    }

    @Test
    fun testIterableCompileTimePass() {
        Assert.pass(
            """
def foo(x: typing.Iterable):
    pass

def bar():
    foo([1, 2, 3])
""",
        )
    }

    @Test
    fun testIterableCompileTimeFail() {
        Assert.fail(
            """
def foo(x: typing.Iterable):
    pass

def bar():
    foo(1)
""",
            "Expected type `typing.Iterable`",
        )
    }
}
