// port-lint: source tests:src/values/types/set/value.rs
package io.github.kotlinmania.starlark.values.types.set

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

class SetValueTest {
    @Test
    fun testBitOr() {
        Assert.eq("set([1, 2, 3]) | set([3, 4])", "set([1, 2, 3, 4])")
    }

    @Test
    fun testBitOrLhsEmpty() {
        Assert.eq("set() | set([3, 4])", "set([3, 4])")
    }

    @Test
    fun testBitOrRhsEmpty() {
        Assert.eq("set([1, 2, 3]) | set()", "set([1, 2, 3])")
    }

    @Test
    fun testBitOrFailIter() {
        Assert.fail(
            "set([1, 2, 3]) | []",
            "Operation `|` not supported for types `set` and `list`",
        )
    }

    @Test
    fun testBitOrOrd() {
        Assert.eq("list(set([5, 1, 3]) | set([4, 5, 2]))", "[5, 1, 3, 4, 2]")
    }

    @Test
    fun testBitAnd() {
        Assert.eq("set([1, 2, 3]) & set([3, 4])", "set([3])")
    }

    @Test
    fun testBitAndLhsEmpty() {
        Assert.eq("set() & set([3, 4])", "set([])")
    }

    @Test
    fun testBitAndRhsEmpty() {
        Assert.eq("set([1, 2, 3]) & set()", "set([])")
    }

    @Test
    fun testBitAndOrd() {
        Assert.eq("list(set([1, 2, 3]) & set([4, 3, 1]))", "[3, 1]")
    }

    @Test
    fun testBitAndFailIter() {
        Assert.fail(
            "set([1, 2, 3]) & []",
            "Operation `&` not supported for types `set` and `list`",
        )
    }

    @Test
    fun testBitXor() {
        Assert.eq("set([1, 2, 3]) ^ set([3, 4])", "set([4, 2, 1])")
    }

    @Test
    fun testBitXorOrd() {
        Assert.eq("list(set([1, 2, 3, 7]) ^ set([4, 3, 1]))", "[2, 7, 4]")
    }

    @Test
    fun testBitXorLhsEmpty() {
        Assert.eq("set() ^ set([3, 4])", "set([3, 4])")
    }

    @Test
    fun testBitXorRhsEmpty() {
        Assert.eq("set([1, 2, 3]) ^ set()", "set([3, 2, 1])")
    }

    @Test
    fun testBitXorFailIter() {
        Assert.fail(
            "set([1, 2, 3]) ^ []",
            "Operation `^` not supported for types `set` and `list`",
        )
    }

    @Test
    fun testSub() {
        Assert.eq("set([1, 2, 3]) - set([2])", "set([1, 3])")
    }

    @Test
    fun testSubEmptyLhs() {
        Assert.eq("set([]) - set([2])", "set([])")
    }

    @Test
    fun testSubEmptyRhs() {
        Assert.eq("set([1, 2]) - set([])", "set([2, 1])")
    }

    @Test
    fun testSubFailIter() {
        Assert.fail(
            "set([1, 2, 3]) - []",
            "Operation `-` not supported for types `set` and `list`",
        )
    }
}
