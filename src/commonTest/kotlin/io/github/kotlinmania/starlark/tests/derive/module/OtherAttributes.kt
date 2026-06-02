// port-lint: tests tests/derive/module/other_attributes.rs
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

/**
 * In these tests, parameters are declared as unused,
 * attributes should be preserved and no warnings should be emitted.
 */

import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.environment.MethodsBuilder
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.types.none.NoneType

// #[starlark_module]
@Suppress("unused")
private fun testOtherAttributesInGlobals(globals: GlobalsBuilder) {
    globals.setFunction("test_global") { args, _ ->
        @Suppress("UNUSED_VARIABLE")
        val foo = args.positional<Int>(0)
        Result.success(NoneType)
    }
}

// #[starlark_module]
@Suppress("unused")
private fun testOtherAttributesInMethods(methods: MethodsBuilder) {
    methods.setMethod("test_method") { _, _, _, _ ->
        Result.success(Value.newNone())
    }
}

// #[starlark_module]
@Suppress("unused")
private fun testOtherAttributesInAttributes(methods: MethodsBuilder) {
    // #[starlark(attribute)]
    methods.setAttribute("test_attribute") { _, _ ->
        // TODO(nga): this marker is no-op.
        Result.success(Value.newNone())
    }
}
