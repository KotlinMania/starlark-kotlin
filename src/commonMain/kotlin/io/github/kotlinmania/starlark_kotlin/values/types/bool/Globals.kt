// port-lint: source src/values/types/bool/globals.rs
package io.github.kotlinmania.starlark_kotlin.values.types.bool

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

// Placeholder types until the actual implementations are ported
expect class GlobalsBuilder {
    fun const(name: String, value: Boolean)
    fun <T : Any> function(
        name: String,
        asType: kotlin.reflect.KClass<T>,
        speculativeExecSafe: Boolean,
        fn: (Value?) -> Result<Boolean>
    )
}

expect class Value {
    fun toBool(): Boolean
}

expect class StarlarkBool

/**
 * Register boolean-related global functions and constants.
 *
 * This is the Kotlin port of the Rust `#[starlark_module]` annotated function.
 * The macro in Rust generates code to register these globals; in Kotlin, we
 * implement this explicitly as a regular function.
 */
internal fun registerBool(globals: GlobalsBuilder) {
    // A boolean representing true.
    globals.const("True", true)

    // A boolean representing false.
    globals.const("False", false)

    // [bool](
    // https://github.com/bazelbuild/starlark/blob/master/spec.md#bool
    // ): returns the truth value of any starlark value.
    //
    // ```
    // # starlark::assert::all_true(r#"
    // bool() == False
    // bool([]) == False
    // bool([1]) == True
    // bool(True) == True
    // bool(False) == False
    // bool(None) == False
    // bool(bool) == True
    // bool(1) == True
    // bool(0) == False
    // bool({}) == False
    // bool({1:2}) == True
    // bool(()) == False
    // bool((1,)) == True
    // bool("") == False
    // bool("1") == True
    // # "#);
    // ```
    globals.function("bool", asType = StarlarkBool::class, speculativeExecSafe = true) { x: Value? ->
        when (x) {
            null -> Result.success(false)
            else -> Result.success(x.toBool())
        }
    }
}
