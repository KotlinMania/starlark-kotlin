// port-lint: source tests/bc/definitelyAssigned.rs
package io.github.kotlinmania.starlark.tests.bc

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

/** Test for bug reported in D36808160. */
class DefinitelyAssignedTests {
    @Test
    fun testDefinitelyAssignedBug() {
        val a = Assert()
        a.module(
            "a.star",
            """
    def f(a):
      # The bug was: compilation of `and` expression reassigned `10` to `a`.
      noop(a and 10)
      return a
    """,
        )
        a.eq("33", "load('a.star', 'f')\nf(33)")
    }

    @Test
    fun testDefinitelyAssignedSlotRangeInList() {
        bcGoldenTest(
            "definitely_assigned_slot_range_in_list",
            "def test(x, y): return [x, y]",
        )
    }

    @Test
    fun testDefinitelyAssignedSlotRangeInCall() {
        bcGoldenTest(
            "definitely_assigned_slot_range_in_call",
            "def test(x, y): noop(x, y)",
        )
    }

    @Test
    fun testMovIsUsed() {
        // `Mov`, not `LoadLocal` should be used to load `y` and `x`.
        bcGoldenTest(
            "definitely_assigned_mov_is_used",
            "def test(x, y): noop(y, x)",
        )
    }

    @Test
    fun testNoOpMovs() {
        bcGoldenTest("definitely_assigned_no_op_movs", "def test(x): x = x")
    }
}
