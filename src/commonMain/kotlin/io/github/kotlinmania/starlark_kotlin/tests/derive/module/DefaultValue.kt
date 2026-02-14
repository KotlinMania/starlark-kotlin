// port-lint: source src/tests/derive/module/default_value.rs
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

// #[starlark_module]
// fn default_value_functions(globals: &mut GlobalsBuilder)
private fun defaultValueFunctions(globals: GlobalsBuilder) {
    // fn foo(#[starlark(default = 75)] x: i32) -> anyhow::Result<i32>
    globals.setFunction("foo") { x: Int? ->
        Result.success(x ?: 75)
    }
}

// #[test]
// fn test_default_value()
internal fun testDefaultValue() {
    val a = Assert()
    a.globalsAdd(::defaultValueFunctions)
    a.eq("74", "foo(74)")
    a.eq("75", "foo()")
}
