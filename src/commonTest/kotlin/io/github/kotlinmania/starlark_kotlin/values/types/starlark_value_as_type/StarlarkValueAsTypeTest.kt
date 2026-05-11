// port-lint: tests src/values/types/starlark_value_as_type.rs
package io.github.kotlinmania.starlark_kotlin.values.types.starlark_value_as_type

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

import io.github.kotlinmania.starlark_kotlin.assert.Assert
import io.github.kotlinmania.starlark_kotlin.environment.GlobalsBuilder
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Arguments
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Evaluator
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.typing.TyStarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.AllocValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.types.bigint.allocValue
import kotlin.test.Test

private class CompilerArgs(private val value: String) : StarlarkValue, StarlarkTypeRepr, AllocValue {
    override val TYPE: String get() = "compiler_args"

    override fun toString(): String = value

    override fun starlarkTypeRepr(): Ty = Ty.starlarkValue(TyStarlarkValue.new(TYPE))

    override fun allocValue(heap: Heap): Value {
        return heap.allocSimple(this)
    }
}

private fun compilerArgsGlobals(globals: GlobalsBuilder) {
    globals.set("CompilerArgs", StarlarkValueAsType.new(CompilerArgs("CompilerArgs")))
    globals.setFunction("compiler_args") { args: Arguments, eval: Evaluator ->
        CompilerArgs(args.positionalAll()[0].unpackStrErr().getOrThrow()).allocValue(eval.heap())
    }
}

internal class StarlarkValueAsTypeTest {
    @Test
    fun testPass() {
        val a = Assert()
        a.globalsAdd(::compilerArgsGlobals)
        a.pass(
            """
def f(x: CompilerArgs): pass

f(compiler_args("hello"))
        """,
        )
    }

    @Test
    fun testFailCompileTime() {
        val a = Assert()
        a.globalsAdd(::compilerArgsGlobals)
        a.fail(
            """
def g(x: CompilerArgs): pass

def h():
    g([])
""",
            "Expected type `compiler_args` but got",
        )
    }

    @Test
    fun testFailRuntime() {
        val a = Assert()
        a.globalsAdd(::compilerArgsGlobals)
        a.fail(
            """
def h(x: CompilerArgs): pass

noop(h)(1)
            """,
            "Value `1` of type `int` does not match the type annotation",
        )
    }
}
