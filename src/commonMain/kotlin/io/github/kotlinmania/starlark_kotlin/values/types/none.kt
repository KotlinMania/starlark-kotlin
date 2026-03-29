// port-lint: source src/values/types/none.rs
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
 * The `None` type.
 *
 * Submodules:
 * - [globals][io.github.kotlinmania.starlark_kotlin.values.types.none.Globals] - global None functions
 * - [noneOr][io.github.kotlinmania.starlark_kotlin.values.types.none.NoneOr] - NoneOr type
 * - [noneType][io.github.kotlinmania.starlark_kotlin.values.types.none.NoneType] - NoneType implementation
 */

// Re-exports (mirrors Rust's `pub use none_or::NoneOr` and `pub use none_type::NoneType`)
internal typealias NoneOrExport = io.github.kotlinmania.starlark_kotlin.values.types.none.NoneOr<*>
internal typealias NoneTypeExport = io.github.kotlinmania.starlark_kotlin.values.types.none.NoneType
