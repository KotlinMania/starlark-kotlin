// port-lint: tests src/tests/bc/definitely_assigned.rs
package io.github.kotlinmania.starlark_kotlin.tests.bc

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

/** Test for bug reported in D36808160. */
// #[test]
// fn test_definitely_assigned_bug()
internal fun testDefinitelyAssignedBug() {
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

// #[test]
// fn test_definitely_assigned_slot_range_in_list()
internal fun testDefinitelyAssignedSlotRangeInList() {
    bcGoldenTest(
        "definitely_assigned_slot_range_in_list",
        "def test(x, y): return [x, y]",
    )
}

// #[test]
// fn test_definitely_assigned_slot_range_in_call()
internal fun testDefinitelyAssignedSlotRangeInCall() {
    bcGoldenTest(
        "definitely_assigned_slot_range_in_call",
        "def test(x, y): noop(x, y)",
    )
}

// #[test]
// fn test_mov_is_used()
internal fun testMovIsUsed() {
    // `Mov`, not `LoadLocal` should be used to load `y` and `x`.
    bcGoldenTest(
        "definitely_assigned_mov_is_used",
        "def test(x, y): noop(y, x)",
    )
}

// #[test]
// fn test_no_op_movs()
internal fun testNoOpMovs() {
    bcGoldenTest("definitely_assigned_no_op_movs", "def test(x): x = x")
}
