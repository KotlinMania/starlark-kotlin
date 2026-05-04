// port-lint: source tests/derive/module/otherAttributes.rs
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

/**
 * In these tests, parameters are declared as unused,
 * attributes should be preserved and no warnings should be emitted.
 */

import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.environment.MethodsBuilder
import io.github.kotlinmania.starlark.eval.runtime.positional
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.types.none.NoneType

private fun testOtherAttributesInGlobals(globals: GlobalsBuilder) {
    fun testGlobal(foo: UInt): Result<NoneType> {
        // Mirrors Rust `fn test_global(#[allow(unused_variables)] foo: u32) -> Result<NoneType>` —
        // parameter is consumed only for type-binding by the unpacker.
        foo.toLong()
        return Result.success(NoneType)
    }

    globals.setFunction("test_global") { args, _ ->
        testGlobal(args.positional<Int>(0).toUInt())
    }
}

private fun testOtherAttributesInMethods(methods: MethodsBuilder) {
    fun testMethod(thisU32: UInt): Result<NoneType> {
        thisU32.toLong()
        return Result.success(NoneType)
    }

    methods.setMethod("test_method") { _, _, _, _ ->
        testMethod(0u).map { Value.newNone() }
    }
}

// Note: Rust upstream contains a typo (`atributes` instead of `attributes`). The Kotlin
// port preserves it so the function name matches the upstream symbol exactly.
private fun testOtherAttributesInAtributes(methods: MethodsBuilder) {
    fun testAttribute(thisU32: UInt): Result<NoneType> {
        thisU32.toLong()
        return Result.success(NoneType)
    }

    methods.setAttribute("test_attribute") { _, _ ->
        // NOTE(nga): this marker is no-op.
        testAttribute(0u).map { Value.newNone() }
    }
}
