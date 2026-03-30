// port-lint: source src/tests/derive/module/other_attributes.rs
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

/**
 * In these tests, parameters are declared as unused,
 * attributes should be preserved and no warnings should be emitted.
 */

import io.github.kotlinmania.starlark_kotlin.environment.GlobalsBuilder
import io.github.kotlinmania.starlark_kotlin.environment.MethodsBuilder
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.types.none.NoneType

// #[starlark_module]
// fn test_other_attributes_in_globals(globals: &mut GlobalsBuilder)
@Suppress("unused")
private fun testOtherAttributesInGlobals(globals: GlobalsBuilder) {
    // fn test_global(#[allow(unused_variables)] foo: u32) -> Result<NoneType>
    globals.setFunction("test_global") { args, _ ->
        @Suppress("UNUSED_VARIABLE")
        val foo = args.positional<Int>(0)
        Result.success(NoneType)
    }
}

// #[starlark_module]
// fn test_other_attributes_in_methods(methods: &mut MethodsBuilder)
@Suppress("unused")
private fun testOtherAttributesInMethods(methods: MethodsBuilder) {
    // fn test_method(#[allow(unused_variables)] this: u32) -> Result<NoneType>
    methods.setMethod("test_method") { _, _, _, _ ->
        Result.success(Value.newNone())
    }
}

// #[starlark_module]
// fn test_other_attributes_in_atributes(methods: &mut MethodsBuilder)
@Suppress("unused")
private fun testOtherAttributesInAttributes(methods: MethodsBuilder) {
    // #[starlark(attribute)]
    // fn test_attribute(#[allow(unused_variables)] this: u32) -> Result<NoneType>
    methods.setAttribute("test_attribute") { _, _ ->
        // TODO(nga): this marker is no-op.
        Result.success(Value.newNone())
    }
}
