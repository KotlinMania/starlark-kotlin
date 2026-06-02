// port-lint: tests src/values/types/starlark_value_as_type.rs
package io.github.kotlinmania.starlark.values.types

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
import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.AllocValue
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.types.starlarkvalueastype.StarlarkValueAsType
import kotlin.test.Test

class StarlarkValueAsTypeTest {
    private class CompilerArgs(
        val value: String,
    ) : StarlarkValue,
        AllocValue {
        override val TYPE: String get() = "compiler_args"

        override fun toString(): String = value

        override fun starlarkTypeRepr(): Ty = getTypeStarlarkRepr()

        override fun allocValue(heap: Heap): Value = heap.allocSimple(this)
    }

    private fun compilerArgsGlobals(globals: GlobalsBuilder) {
        fun compilerArgs(x: String): Result<CompilerArgs> = Result.success(CompilerArgs(x))

        globals.setConst("CompilerArgs", StarlarkValueAsType.new(CompilerArgs("")))
        globals.setFunction("compiler_args") { args, eval ->
            val arg = args.positionalAll()[0].unpackStrErr().getOrThrow()
            compilerArgs(arg).map { it.allocValue(eval.heap()) }
        }
    }

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
            """Expected type `compiler_args` but got""",
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
            """Value `1` of type `int` does not match the type annotation""",
        )
    }
}
