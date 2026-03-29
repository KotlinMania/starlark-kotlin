// port-lint: source src/values/types/tuple.rs
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
 * The tuple type, an immutable sequence of values.
 *
 * Submodules:
 * - [alloc][io.github.kotlinmania.starlark_kotlin.values.types.tuple.Alloc] - allocation helpers
 * - [globals][io.github.kotlinmania.starlark_kotlin.values.types.tuple.Globals] - global tuple functions
 * - [refs][io.github.kotlinmania.starlark_kotlin.values.types.tuple.Refs] - tuple references
 * - [rustTuple][io.github.kotlinmania.starlark_kotlin.values.types.tuple.RustTuple] - Kotlin tuple bridge
 * - [unpack][io.github.kotlinmania.starlark_kotlin.values.types.tuple.Unpack] - unpacking helpers
 * - [value][io.github.kotlinmania.starlark_kotlin.values.types.tuple.Value] - tuple value type
 */

// Re-exports (mirrors Rust's pub use declarations)
internal typealias AllocTupleExport<T> = io.github.kotlinmania.starlark_kotlin.values.types.tuple.AllocTuple<T>
internal typealias FrozenTupleRefExport = io.github.kotlinmania.starlark_kotlin.values.types.tuple.FrozenTupleRef
internal typealias TupleRefExport = io.github.kotlinmania.starlark_kotlin.values.types.tuple.TupleRef
internal typealias UnpackTupleExport<T> = io.github.kotlinmania.starlark_kotlin.values.types.tuple.unpack.UnpackTuple<T>
