// port-lint: tests tests/derive/module/basic.rs
package io.github.kotlinmania.starlark.tests.derive.module

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

import io.github.kotlinmania.starlark.assert.Assert
import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.environment.MethodsBuilder
import io.github.kotlinmania.starlark.eval.runtime.Arguments
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.eval.runtime.params.spec.ParametersSpec
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value

// The examples from the starlark_module documentation.
// #[test]
internal fun testStarlarkModule() {
    // #[starlark_module]
    fun global(builder: GlobalsBuilder) {
        builder.setFunction("cc_binary") { args: Arguments, eval: Evaluator ->
            val name = args.positional<String>(0)
            val srcs = args.positional<Value>(1)
            // real implementation may write it to a global variable
            eval.heap().allocStr("\"$name\" $srcs")
        }
    }

    val a = Assert()
    a.globalsAdd(::global)
    val v = a.pass("cc_binary(name='star', srcs=['a.cc', 'b.cc'])")
    check(
        v.value().unpackStr()!!
            == "\"star\" [\"a.cc\", \"b.cc\"]",
    )
}

// #[test]
internal fun testStarlarkMethods() {
    // #[starlark_module]
    fun methods(builder: MethodsBuilder) {
        builder.setMethod("enum") { eval: Evaluator, thisVal: Value, _: ParametersSpec<FrozenValue>, args: Arguments ->
            val index = args.optionalNamed<Int>("index") ?: 3
            val sv = eval.heap().allocStr("$thisVal $index")
            Result.success(sv)
        }
    }

    MethodsBuilder.new().with(::methods).build()
}

// #[test]
internal fun testStaticAllowed() {
    // #[starlark_module]
    fun globals(globals: GlobalsBuilder) {
        globals.setFunction("test") { _: Arguments, _: Evaluator ->
            throw AssertionError("should not be called")
        }
    }

    GlobalsBuilder.standard().with(::globals).build()
}
