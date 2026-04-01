// port-lint: tests src/typing/tests/tuple.rs
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

// #[test]
// fn test_tuple()
internal fun testTuple() {
    TypeCheck().check(
        "tuple",
        """
def empty_tuple_fixed_name() -> (): return tuple()
def empty_tuple_name_fixed() -> tuple: return ()
""",
    )
}

// #[test]
// fn test_tuple_ellipsis()
internal fun testTupleEllipsis() {
    TypeCheck().check(
        "tuple_ellipsis",
        """
def f(t: tuple[int, ...]) -> int:
    return t[0]

def g():
    # Good.
    f((1, 2, 3))

    # Bad.
    f((1, "x"))
""",
    )
}
