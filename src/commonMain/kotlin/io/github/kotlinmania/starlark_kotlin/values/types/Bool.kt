// port-lint: source src/values/types/bool.rs
package io.github.kotlinmania.starlark_kotlin.values.types

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

/**
 * The boolean type (`False` and `True`).
 *
 * Can be created with [Value.newBool][io.github.kotlinmania.starlark_kotlin.values.layout.Value]
 * and unwrapped with [Value.unpackBool][io.github.kotlinmania.starlark_kotlin.values.layout.Value].
 * Unlike most Starlark values, these aren't actually allocated on the
 * [Heap][io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap],
 * but as special values.
 *
 * Submodules:
 * - [alloc][io.github.kotlinmania.starlark_kotlin.values.types.bool.Alloc] - allocation helpers
 * - [globals][io.github.kotlinmania.starlark_kotlin.values.types.bool.Globals] - global bool functions
 * - [typeRepr][io.github.kotlinmania.starlark_kotlin.values.types.bool.TypeRepr] - type representation
 * - [unpack][io.github.kotlinmania.starlark_kotlin.values.types.bool.Unpack] - unpacking helpers
 * - [value][io.github.kotlinmania.starlark_kotlin.values.types.bool.Value] - StarlarkBool value
 */

// Re-exports (mirrors Rust's `pub use value::BOOL_TYPE` and `pub use value::StarlarkBool`)
internal val BOOL_TYPE = io.github.kotlinmania.starlark_kotlin.values.types.bool.BOOL_TYPE
