// port-lint: source src/values/types/none/globals.rs
package io.github.kotlinmania.starlark_kotlin.values.types.none

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
    fun const(name: String, value: NoneType)
}

expect class NoneType

/**
 * Register None constant to global scope.
 *
 * This is the Kotlin port of the Rust `#[starlark_module]` annotated function.
 * The macro in Rust generates code to register these globals; in Kotlin, we
 * implement this explicitly as a regular function.
 */
internal fun registerNone(globals: GlobalsBuilder) {
    /**
     * The `None` value, used to represent nothing.
     * Implicitly returned from functions that don't have an explicit return.
     */
    globals.const("None", NoneType)
}
