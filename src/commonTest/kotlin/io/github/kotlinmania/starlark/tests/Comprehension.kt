// port-lint: source src/tests/comprehension.rs
package io.github.kotlinmania.starlark_kotlin.tests

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

/** Test dict and list comprehension. */

import io.github.kotlinmania.starlark_kotlin.assert.Assert

// comprehensions should work whether they are at the root, or under a def
// but these are actually quite different locations semantically, so test both
// fn check_comp(lines: &[&str])
private fun checkComp(lines: List<String>) {
    // NOTE(nga): typechecker is wrong here.
    Assert.isTrueSkipTypecheck(lines.joinToString("\n"))
    val last = lines.last()
    val init = lines.dropLast(1)
    Assert.isTrueSkipTypecheck(
        "def f():\n  ${init.joinToString("\n  ")}\n  return $last\nf()"
    )
}

// #[test]
// fn test_spec()
internal fun testSpec() {
    // From the Starlark spec
    checkComp(listOf("[x*x for x in [0,1,2,3,4]] == [0, 1, 4, 9, 16]"))
    checkComp(listOf("[x*x for x in [0,1,2,3,4] if x%2 == 0] == [0, 4, 16]"))
    checkComp(listOf(
        "[(x, y) for x in [0,1,2,3,4] if x%2 == 0 for y in [0,1,2,3,4] if y > x] == [(0, 1), (0, 2), (0, 3), (0, 4), (2, 3), (2, 4)]",
    ))
    checkComp(listOf("""[x*y+z for (x, y), z in [((2, 3), 5), (("o", 2), "!")]] == [11, 'oo!']"""))
    Assert.fail("[x*x for x in 1, 2, 3]", "Parse error")
    checkComp(listOf("x = 1", "_ = [x for x in [2]]", "x == 1"))
}

// #[test]
// fn test_scopes()
internal fun testScopes() {
    checkComp(listOf("[1//0 for x in [] for y in z for z in ()] == []"))
    Assert.failSkipTypecheck(
        "[1//0 for x in [1] for y in z for z in ()]",
        "Local variable `z` referenced before assignment",
    )
    Assert.failSkipTypecheck("[() for x in w for w in [1]]", "Variable `w` not found")
}

// #[test]
// fn test_dict()
internal fun testDict() {
    checkComp(listOf("{x: 1 for x in [0,1,2]} == {0: 1, 1: 1, 2: 1}"))
}

// #[test]
// fn test_nested()
internal fun testNested() {
    checkComp(listOf("[[y for y in x] for x in [[1],[2,3]]] == [[1],[2,3]]"))
    checkComp(listOf("[[x for x in x] for x in [[1],[2,3]]] == [[1],[2,3]]"))
    checkComp(listOf("[x for x in [[1],[2,3]] for x in x if x >= 2] == [2,3]"))
    checkComp(listOf(
        "items = {8: [1,2], 9: [3,4,6]}",
        "[[x for x in items[x] if x%2==0] for x in items] == [[2],[4,6]]",
    ))
}

// #[test]
// fn test_sequential()
internal fun testSequential() {
    checkComp(listOf(
        "x = [x*x for x in [0,1,2,3,4]]",
        "[x*x for x in x] == [0, 1, 16, 81, 256]",
    ))
}

// #[test]
// fn test_if_only()
internal fun testIfOnly() {
    Assert.fail("[1 if 0 == 0] == [0]", "Parse error")
}

// #[test]
// fn test_same_var_twice_in_assignment()
internal fun testSameVarTwiceInAssignment() {
    checkComp(listOf("[x for (x, x) in [(1, 2), (3, 4)]] == [2, 4]"))
}

// #[test]
// fn test_same_var_in_two_fors()
internal fun testSameVarInTwoFors() {
    checkComp(listOf("[x for x in [[1, 2], [3]] for x in x] == [1, 2, 3]"))
}

// #[test]
// fn test_comprehension_blocks()
internal fun testComprehensionBlocks() {
    Assert.failSkipTypecheck(
        """
x = [1, 2]
res = [x for _ in [3] for x in x]
assert_eq(res, [1,2])
""".trimIndent(),
        "variable `x` referenced before assignment",
    )
}
