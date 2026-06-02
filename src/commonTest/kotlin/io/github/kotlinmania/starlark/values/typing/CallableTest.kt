// port-lint: tests src/values/typing/callable.rs
package io.github.kotlinmania.starlark.values.typing

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

import io.github.kotlinmania.starlark.assert.Assert
import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.eval.runtime.Arguments
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.values.types.bigint.allocValue
import io.github.kotlinmania.starlark.values.types.none.NoneType
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.typing.ParamSpec
import io.github.kotlinmania.starlark.values.typing.callable.StarlarkCallableParamSpecNone
import kotlin.test.Test

private fun myModule(globals: GlobalsBuilder) {
    globals.setFunction(
        name = "accept_f",
        ty = Ty.function(
            ParamSpec.posOnly(listOf(Ty.callable(ParamSpec.posOnly(listOf(Ty.string())), Ty.int()))),
            Ty.none(),
        ),
    ) { _args: Arguments, _eval: Evaluator ->
        Result.success(NoneType)
    }
}

internal class CallableTest {
    @Test
    fun testCallableRuntime() {
        Assert.isTrue("isinstance(lambda: None, typing.Callable)")
        Assert.isTrue("isinstance(len, typing.Callable)")
        Assert.isTrue("Rec = record(); isinstance(Rec, typing.Callable)")
        Assert.isFalse("isinstance(37, typing.Callable)")
    }

    @Test
    fun testCallablePassCompileTime() {
        Assert.pass(
            """
Rec = record()

def foo(x: typing.Callable):
    pass

def bar():
    foo(len)
    foo(lambda x: 1)
    foo(Rec)
""",
        )
    }

    @Test
    fun testCallableFailCompileTime() {
        Assert.fail(
            """
def foo(x: typing.Callable):
    pass

def bar():
    foo(1)
""",
            "Expected type",
        )
    }

    @Test
    fun testNativeCallablePass() {
        val a = Assert()
        a.globalsAdd(::myModule)
        a.pass(
            """
def f(x: str) -> int:
    return len(x)

def test():
    accept_f(f)
""",
        )
    }

    @Test
    fun testNativeCallableFailCompileTimeWrongParamType() {
        val a = Assert()
        a.globalsAdd(::myModule)
        a.fail(
            """
def f(x: list) -> int:
    return 1

def test():
    accept_f(f)
""",
            "Expected type `typing.Callable[[str], int]` but got",
        )
    }

    @Test
    fun testNativeCallableFailCompileTimeWrongParamCount() {
        val a = Assert()
        a.globalsAdd(::myModule)
        a.fail(
            """
def f() -> int:
    return 1

def test():
    accept_f(f)
""",
            "Expected type `typing.Callable[[str], int]` but got",
        )
    }

    @Test
    fun testTypingCallablePass() {
        val a = Assert()
        a.pass(
            """
def accept_f(x: typing.Callable[[str], int]) -> None:
    pass

def f(x: str) -> int:
    return len(x)

def test():
    accept_f(f)
""",
        )
    }

    @Test
    fun testTypingCallableFailCompileTimeWrongParamType() {
        val a = Assert()
        a.fail(
            """
def accept_f(x: typing.Callable[[str], int]) -> None:
    pass

def f(x: list) -> int:
    return 1

def test():
    accept_f(f)
""",
            "Expected type `typing.Callable[[str], int]` but got",
        )
    }

    @Test
    fun testTypingCallableFailCompileTimeWrongParamCount() {
        val a = Assert()
        a.fail(
            """
def accept_f(x: typing.Callable[[str], int]) -> None:
    pass

def f() -> int:
    return 1

def test():
    accept_f(f)
""",
            "Expected type `typing.Callable[[str], int]` but got",
        )
    }

    @Test
    fun testCallableCheckedRuntime() {
        fun checkedModule(globals: GlobalsBuilder) {
            globals.setFunction(
                name = "accept_f",
                ty = Ty.function(
                    ParamSpec.posOnly(listOf(Ty.callable(ParamSpec.posOnly(emptyList(), emptyList()), Ty.none()))),
                    Ty.none(),
                ),
            ) { _args: Arguments, _eval: Evaluator ->
                val v = _args.positional1(_eval.heap()).getOrThrow()
                val unpacker = StarlarkCallableCheckedUnpackValue(StarlarkCallableParamSpecNone, NoneType)
                unpacker.unpackNamedParam(v, "_f")
                Result.success(NoneType)
            }

            globals.setFunction(
                name = "good",
                ty = Ty.function(
                    ParamSpec.posOnly(emptyList(), emptyList()),
                    Ty.none(),
                ),
            ) { _args: Arguments, _eval: Evaluator ->
                Result.success(NoneType)
            }

            globals.setFunction(
                name = "bad",
                ty = Ty.function(
                    ParamSpec.posOnly(emptyList(), emptyList()),
                    Ty.int(),
                ),
            ) { _args: Arguments, _eval: Evaluator ->
                Result.success(10.allocValue(_eval.heap()))
            }
        }

        val a = Assert()
        a.globalsAdd(::checkedModule)

        a.pass("accept_f(good)")

        a.fail(
            """
def test():
    x = noop(bad) # Hide the type from static typechecker.
    accept_f(x)

test()
        """,
            "Type of parameter `_f` doesn't match",
        )
    }
}
