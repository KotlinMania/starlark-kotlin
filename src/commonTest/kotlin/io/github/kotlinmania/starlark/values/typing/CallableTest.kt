// port-lint: source tests:src/values/typing/callable.rs
package io.github.kotlinmania.starlark.values.typing

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
 */

import io.github.kotlinmania.starlark.assert.Assert
import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.values.types.none.NoneType
import kotlin.test.Test

class CallableTest {

    @Test
    fun testNativeCallablePass() {
        fun myModule(globals: GlobalsBuilder) {
            fun acceptF(x: StarlarkCallable<StarlarkCallableParamAny, StarlarkTypeRepr>): Result<NoneType> {
                x.toString()
                return Result.success(NoneType)
            }
            globals.setFunction("accept_f") { args, _ ->
                acceptF(args.positional<StarlarkCallable<StarlarkCallableParamAny, StarlarkTypeRepr>>(0))
            }
        }

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
    fun testCallableCheckedRuntime() {
        fun module(globals: GlobalsBuilder) {
            fun acceptF(f: StarlarkCallableChecked<StarlarkCallableParamAny, StarlarkTypeRepr>): Result<NoneType> {
                f.toString()
                return Result.success(NoneType)
            }
            fun good(): Result<NoneType> = Result.success(NoneType)
            fun bad(): Result<Int> = Result.success(10)

            globals.setFunction("accept_f") { args, _ ->
                acceptF(args.positional<StarlarkCallableChecked<StarlarkCallableParamAny, StarlarkTypeRepr>>(0))
            }
            globals.setFunction("good") { _, _ -> good() }
            globals.setFunction("bad") { _, _ -> bad() }
        }

        val a = Assert()
        a.globalsAdd(::module)

        a.pass("accept_f(good)")

        a.fail(
            """
accept_f(bad)
""",
            "Expected type",
        )
    }
}
