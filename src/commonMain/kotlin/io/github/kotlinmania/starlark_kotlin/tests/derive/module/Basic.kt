// port-lint: source src/tests/derive/module/basic.rs
package io.github.kotlinmania.starlark_kotlin.tests.derive.module

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

import io.github.kotlinmania.starlark_kotlin.assert.Assert
import io.github.kotlinmania.starlark_kotlin.environment.GlobalsBuilder
import io.github.kotlinmania.starlark_kotlin.environment.MethodsBuilder
import io.github.kotlinmania.starlark_kotlin.values.Heap
import io.github.kotlinmania.starlark_kotlin.values.StringValue
import io.github.kotlinmania.starlark_kotlin.values.Value
import io.github.kotlinmania.starlark_kotlin.values.ValueOfUnchecked
import io.github.kotlinmania.starlark_kotlin.values.list_or_tuple.UnpackListOrTuple

// The examples from the starlark_module documentation.
// #[test]
// fn test_starlark_module()
internal fun testStarlarkModule() {
    // #[starlark_module]
    // fn global(builder: &mut GlobalsBuilder)
    fun global(builder: GlobalsBuilder) {
        // fn cc_binary(name: &str, srcs: UnpackListOrTuple<&str>) -> anyhow::Result<String>
        builder.setFunction("cc_binary") { name: String, srcs: UnpackListOrTuple<String> ->
            // real implementation may write it to a global variable
            Result.success("\"$name\" ${srcs.items}")
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

// #[test]
// fn test_starlark_methods()
internal fun testStarlarkMethods() {
    // #[starlark_module]
    // fn methods(builder: &mut MethodsBuilder)
    fun methods(builder: MethodsBuilder) {
        // fn enum(this: Value, #[starlark(require = named, default = 3)] index: i32, heap: Heap) -> anyhow::Result<StringValue>
        builder.setMethod("enum") { thisVal: Value, index: Int?, heap: Heap ->
            val idx = index ?: 3
            Result.success(heap.allocStr("$thisVal $idx"))
        }
    }

    MethodsBuilder().with(::methods).build()
}

// #[test]
// fn test_static_allowed()
internal fun testStaticAllowed() {
    // #[starlark_module]
    // fn globals(globals: &mut GlobalsBuilder)
    fun globals(globals: GlobalsBuilder) {
        // fn test() -> anyhow::Result<ValueOfUnchecked<&'static str>>
        globals.setFunction("test") {
            throw AssertionError("should not be called")
        }
    }

    GlobalsBuilder.standard().with(::globals).build()
}
