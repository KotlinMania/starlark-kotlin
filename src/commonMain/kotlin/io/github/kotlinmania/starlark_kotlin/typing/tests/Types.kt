// port-lint: source src/typing/tests/types.rs
package io.github.kotlinmania.starlark_kotlin.typing.tests

import io.github.kotlinmania.starlark_kotlin.typing.TypeCheck

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

/** Type-related operations. */

// #[test]
// fn test_type_alias()
internal fun testTypeAlias() {
    TypeCheck().ty("x").check(
        "type_alias",
        """
MyList = list[int]

def f(x: MyList):
    pass
""",
    )
}

// #[test]
// fn test_incorrect_type_dot()
internal fun testIncorrectTypeDot() {
    TypeCheck().check(
        "incorrect_type_dot",
        """
def foo(x: list.foo.bar):
    pass
""",
    )
}

// #[test]
// fn test_function_as_type_bit_or()
internal fun testFunctionAsTypeBitOr() {
    TypeCheck().ty("t").check(
        "function_as_type_bit_or",
        """
def test():
    # This test should work even if `t` is global. There's a bug in test framework somewhere.
    t = int | str
""",
    )
}
