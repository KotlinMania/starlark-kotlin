// port-lint: source src/values/types/string.rs
package io.github.kotlinmania.starlark_kotlin.values.types

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
 * The string type. All strings must be valid UTF8.
 *
 * Submodules:
 * - [allocUnpack][io.github.kotlinmania.starlark_kotlin.values.types.string.AllocUnpack] - alloc/unpack helpers
 * - [dotFormat][io.github.kotlinmania.starlark_kotlin.values.types.string.DotFormat] - dot format implementation
 * - [globals][io.github.kotlinmania.starlark_kotlin.values.types.string.Globals] - global string functions
 * - [intern][io.github.kotlinmania.starlark_kotlin.values.types.string.Intern] - string interning
 * - [interpolation][io.github.kotlinmania.starlark_kotlin.values.types.string.Interpolation] - string interpolation
 * - [iter][io.github.kotlinmania.starlark_kotlin.values.types.string.Iter] - string iteration
 * - [methods][io.github.kotlinmania.starlark_kotlin.values.types.string.Methods] - string methods
 * - [repr][io.github.kotlinmania.starlark_kotlin.values.types.string.Repr] - string repr formatting
 * - [simd][io.github.kotlinmania.starlark_kotlin.values.types.string.Simd] - SIMD-like operations
 * - [strType][io.github.kotlinmania.starlark_kotlin.values.types.string.StrType] - StarlarkStr type
 */

// Re-exports (mirrors Rust's `pub use str_type::STRING_TYPE` and `pub use str_type::StarlarkStr`)
internal val STRING_TYPE_EXPORT = io.github.kotlinmania.starlark_kotlin.values.types.string.STRING_TYPE
internal typealias StarlarkStrExport = io.github.kotlinmania.starlark_kotlin.values.types.string.StarlarkStr
