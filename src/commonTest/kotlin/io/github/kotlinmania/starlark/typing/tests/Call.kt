// port-lint: source src/typing/tests/call.rs
package io.github.kotlinmania.starlark.typing.tests

import io.github.kotlinmania.starlark.typing.TypeCheck
import kotlin.test.Test

/*
 * Copyright 2019 The Starlark in Rust Authors.
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

/** Tests for functions, callables and calls. */

class CallTests {
    @Test
    fun testTypeKwargs() {
        TypeCheck().check(
            "type_kwargs",
            """
    def foo(**kwargs):
        pass

    def bar():
        foo(**{1: "x"})
    """,
        )
    }

    @Test
    fun testTypesOfArgsKwargs() {
        TypeCheck().ty("args").ty("kwargs").check(
            "types_of_args_kwargs",
            """
    def foo(*args: str, **kwargs: int):
        pass

    def test():
        # Good
        foo("a")
        foo(b=1)
        # Bad
        foo(1)
        foo(c="x")
    """,
        )
    }

    @Test
    fun testKwargsInNativeCode() {
        TypeCheck().check(
            "kwargs_in_native_code",
            """
    def test():
        # Good.
        accepts_typed_kwargs(x=1)
        # Bad.
        accepts_typed_kwargs(x=None)
    """,
        )
    }

    @Test
    fun testCallCallable() {
        TypeCheck().check(
            "call_callable",
            """
    def foo(x: typing.Callable):
        x()
    """,
        )
    }

    @Test
    fun testCallNotCallable() {
        TypeCheck().check(
            "call_not_callable",
            """
    def foo(x: list):
        x()
    """,
        )
    }

    @Test
    fun testCallCallableOrNotCallable() {
        TypeCheck().check(
            "call_callable_or_not_callable",
            """
    def foo(x: [typing.Callable, str], y: [str, typing.Callable]):
        x()
        y()
    """,
        )
    }

    @Test
    fun testCalls() {
        TypeCheck().check(
            "calls",
            """
    def f(y): pass

    def g():
        # Extra parameter.
        f(1, 2)

        # Not enough parameters.
        f()
    """,
        )
    }

    @Test
    fun testNeverCallBug() {
        TypeCheck().ty("y").check(
            "never_call_bug",
            """
    def foo(x: typing.Never):
        y = x(1)
    """,
        )
    }

    @Test
    fun testCallPosOnly() {
        TypeCheck().check(
            "call_pos_only",
            """
    def f(x, /):
        pass

    def test():
        f("good")
        f(x="bad")
    """,
        )
    }
}
