// port-lint: source src/values/types/structs.rs
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
 * The struct type, an associative-map created with `struct()`.
 *
 * This struct type is related to both the [dictionary][io.github.kotlinmania.starlark_kotlin.values.types.dict]
 * and the [record][io.github.kotlinmania.starlark_kotlin.values.types.record] types, all being associative maps.
 *
 * * Like a record, a struct is immutable, fields can be referred to with `struct.field`, and
 *   it uses strings for keys.
 * * Like a dictionary, the struct is untyped, and manipulating structs from Kotlin is ergonomic.
 *
 * The `struct()` function creates a struct. It accepts keyword arguments, keys become
 * struct field names, and values become field values.
 *
 * ```
 * ip_address = struct(host='localhost', port=80)
 * ip_address.port == 80
 * ```
 *
 * Submodules:
 * - [alloc][io.github.kotlinmania.starlark_kotlin.values.types.structs.Alloc] - allocation helpers
 * - [refs][io.github.kotlinmania.starlark_kotlin.values.types.structs.Refs] - struct references
 * - [structs][io.github.kotlinmania.starlark_kotlin.values.types.structs.Structs] - struct implementation
 * - [unorderedHasher][io.github.kotlinmania.starlark_kotlin.values.types.structs.UnorderedHasher] - hash support
 * - [value][io.github.kotlinmania.starlark_kotlin.values.types.structs.Value] - StarlarkStruct value
 */

// Re-exports (mirrors Rust's pub use declarations) are done via direct imports at call sites.
