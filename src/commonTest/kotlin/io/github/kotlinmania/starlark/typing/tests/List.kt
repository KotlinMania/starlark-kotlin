// port-lint: tests src/typing/tests/list.rs
package io.github.kotlinmania.starlark.typing.tests

import io.github.kotlinmania.starlark.typing.TypeCheck

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

// #[test]
// fn test_int_mul_list()
internal fun testIntMulList() {
    TypeCheck().ty("x").check(
        "int_mul_list",
        """
def test():
    x = 1 * ["a"]
""",
    )
}

// #[test]
// fn test_list_append()
internal fun testListAppend() {
    TypeCheck().ty("x").check(
        "list_append",
        """
def test():
    # Type of `x` should be inferred as list of either `int` or `str`.
    x = []
    x.append(1)
    x.append("")
""",
    )
}

// #[test]
// fn test_list_append_bug()
internal fun testListAppendBug() {
    TypeCheck().ty("x").check(
        "list_append_bug",
        """
def test():
    x = []
    x.append(x)
""",
    )
}

// #[test]
// fn test_list_function()
internal fun testListFunction() {
    TypeCheck().ty("x").check(
        "list_function",
        """
def test():
    x = list([1, 2])
""",
    )
}

// #[test]
// fn test_list_less()
internal fun testListLess() {
    TypeCheck().check(
        "list_less",
        """
def test(x: list[str], y: list[str]) -> bool:
    return x < y
""",
    )
}

// #[test]
// fn test_list_bin_op()
internal fun testListBinOp() {
    TypeCheck().ty("x").ty("y").ty("z").check(
        "list_bin_op",
        """
def test(a: list[str]):
    x = a + a
    y = a * 3
    z = 3 * a
""",
    )
}
