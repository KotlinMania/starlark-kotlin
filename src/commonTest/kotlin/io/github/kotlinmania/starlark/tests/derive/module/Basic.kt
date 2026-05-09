// port-lint: source tests/derive/module/basic.rs
package io.github.kotlinmania.starlark.tests.derive.module

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
import io.github.kotlinmania.starlark.environment.MethodsBuilder
import io.github.kotlinmania.starlark.eval.runtime.Arguments
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.eval.runtime.optionalNamed
import io.github.kotlinmania.starlark.eval.runtime.positional
import io.github.kotlinmania.starlark.eval.runtime.params.spec.ParametersSpec
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value
import kotlin.test.Test

// The examples from the starlarkModule documentation.
class BasicTests {
    @Test
    fun testStarlarkModule() {
        fun global(builder: GlobalsBuilder) {
            fun ccBinary(name: String, srcs: Value, eval: Evaluator): Value {
                // real implementation may write it to a global variable
                return eval.heap().allocStr("\"$name\" $srcs").toValue()
            }
            builder.setFunction("cc_binary") { args: Arguments, eval: Evaluator ->
                val name = args.positional<String>(0)
                val srcs = args.positional<Value>(1)
                ccBinary(name, srcs, eval)
            }
        }

        val a = Assert()
        a.globalsAdd(::global)
        val v = a.pass("cc_binary(name='star', srcs=['a.cc', 'b.cc'])")
        check(
            v.value().unpackStr()!!
                == "\"star\" [\"a.cc\", \"b.cc\"]"
        )
    }

    @Test
    fun testStarlarkMethods() {
        fun methods(builder: MethodsBuilder) {
            fun `enum`(thisVal: Value, index: Int, eval: Evaluator): Result<Value> {
                val sv = eval.heap().allocStr("$thisVal $index")
                return Result.success(sv.toValue())
            }
            builder.setMethod("enum") { eval: Evaluator, thisVal: Value, _: ParametersSpec<FrozenValue>, args: Arguments ->
                val index = args.optionalNamed<Int>("index") ?: 3
                `enum`(thisVal, index, eval)
            }
        }

        MethodsBuilder.new().with(::methods).build()
    }

    @Test
    fun testStaticAllowed() {
        fun globals(globals: GlobalsBuilder) {
            globals.setFunction("test") { _: Arguments, _: Evaluator ->
                throw AssertionError("should not be called")
            }
        }

        GlobalsBuilder.standard().with(::globals).build()
    }
}
