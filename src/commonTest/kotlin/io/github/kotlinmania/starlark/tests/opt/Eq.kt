// port-lint: source src/tests/opt/eq.rs
package io.github.kotlinmania.starlark.tests.opt

import io.github.kotlinmania.starlark.tests.bc.bcGoldenTest
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

/** Test for `a == b` optimizations. */

class EqTests {
    @Test
    fun testEqInt() {
        bcGoldenTest(
            "eq_int",
            """
    def test(x):
        return x == 10
    """,
        )
    }

    @Test
    fun testEqStr() {
        bcGoldenTest(
            "eq_str",
            """
    def test(x):
        return x == "hello"
    """,
        )
    }

    @Test
    fun testEqShortStrIsPtrEq() {
        bcGoldenTest(
            "eq_short_str",
            """
    def test(x):
        return x == "a"
    """,
        )
    }

    @Test
    fun testEqBoolIsPtrEq() {
        bcGoldenTest(
            "eq_bool",
            """
    def test(x):
        return x == True
    """,
        )
    }

    /** Enum values do not override `equals` method, so we can import pointer equality. */
    @Test
    fun testEqEnumIsPtrEq() {
        bcGoldenTest(
            "eq_enum",
            """
    Color = enum("RED", "GREEN", "BLUE")

    def test(x):
        return x == Color("RED")
    """,
        )
    }

    /** Enum values do not override `equals` method, so we can import pointer equality. */
    @Test
    fun testEqEnumAttrIsPtrEq() {
        bcGoldenTest(
            "eq_enum_attr",
            """
    Color = enum("RED", "GREEN", "BLUE")

    def test(x):
        return x == Color.RED
    """,
        )
    }

    @Test
    fun testEqConst() {
        bcGoldenTest(
            "eq_const",
            """
    S = struct(a = 2)

    def test(x):
        return x == S
    """,
        )
    }
}
