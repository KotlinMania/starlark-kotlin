// port-lint: source tests/opt/speculativeExec.rs
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

class SpeculativeExecTests {
    @Test
    fun testMethodsInvokedSpeculatively() {
        bcGoldenTest(
            "speculative_exec_methods_invoked_speculatively",
            """
    def test():
        return "foo".startswith("f")
    """,
        )
    }

    @Test
    fun testFormatSpeculativelyBeforeFormatInstr() {
        bcGoldenTest(
            "speculative_exec_format_speculatively_before_format_instr",
            """
    def test():
        # Test this expression is compiled to constant, not to `FormatOne` instruction.
        return "x{}y".format(1)
    """,
        )
    }

    @Test
    fun testSpeculativelyInlineEnum() {
        bcGoldenTest(
            "speculative_exec_enum_inline",
            """
    MyEnum = enum("red", "green", "blue")

    def test(x):
        # Test there is no enum evaluation.
        return x == MyEnum("red")
    """,
        )
    }
}
