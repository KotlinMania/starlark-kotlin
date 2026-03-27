// port-lint: source src/tests/derive/module/generic.rs
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
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.AllocValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark_kotlin.values.types.none.NoneType
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.tests.derive.starlarkTypeRepr

// #[starlark_module]
// fn global_builder<T: Default, U>(globals: &mut GlobalsBuilder)
// where U: std::fmt::Display + Default
private fun <T, U> globalBuilder(
    globals: GlobalsBuilder,
    defaultT: () -> T,
    defaultU: () -> U,
) {
    globals.setConst("MY_STR", defaultU().toString())
}

// struct CustomNone<T>(PhantomData<T>)
private class CustomNone<T> : StarlarkTypeRepr, AllocValue {
    companion object : StarlarkTypeRepr {
        // impl<T> StarlarkTypeRepr for CustomNone<T>
        override fun starlarkTypeRepr(): Ty = NoneType.starlarkTypeRepr()
    }

    override fun starlarkTypeRepr(): Ty = Companion.starlarkTypeRepr()

    // impl<'v, T> AllocValue<'v> for CustomNone<T>
    // fn alloc_value(self, _heap: Heap) -> Value
    override fun allocValue(heap: Heap): Value = Value.newNone()
}

// #[starlark_module]
// fn method_builder<T: Default, U>(globals: &mut MethodsBuilder)
// where U: std::fmt::Display + Default
private fun <T, U> methodBuilder(
    builder: MethodsBuilder,
    defaultT: () -> T,
    defaultU: () -> U,
) {
    // Just check that this compiles
    // #[starlark(attribute)]
    // fn test_attribute(this: u32) -> starlark::Result<CustomNone<T>>
    builder.setAttribute("test_attribute") { _this: UInt ->
        val _u = defaultU().toString()
        val _t = defaultT()
        Result.success(CustomNone<T>())
    }
}

// #[starlark_module]
// fn global_builder_for_func<T: Default, U>(globals: &mut GlobalsBuilder)
// where U: std::fmt::Display + Default
private fun <T, U> globalBuilderForFunc(
    globals: GlobalsBuilder,
    defaultT: () -> T,
    defaultU: () -> U,
) {
    // fn make_my_str() -> starlark::Result<String>
    globals.setFunction("make_my_str") {
        val _t = defaultT()
        Result.success(defaultU().toString())
    }
}

// #[test]
// fn test_generic_builder()
internal fun testGenericBuilder() {
    val a = Assert()
    a.globalsAdd { g ->
        globalBuilder<UByte, UByte>(g, { 0u.toUByte() }, { 0u.toUByte() })
        globalBuilderForFunc<UByte, UByte>(g, { 0u.toUByte() }, { 0u.toUByte() })
    }
    a.eq("\"0\"", "MY_STR")
    a.eq("\"0\"", "make_my_str()")
}
