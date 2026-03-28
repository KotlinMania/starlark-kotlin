// port-lint: source src/collections.rs
package io.github.kotlinmania.starlark_kotlin

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
 * Collections with deterministic iteration and small memory footprint.
 *
 * These structures use vector backed storage if there are only a few elements, and an index
 * for larger collections. The API mirrors standard Kotlin collections.
 *
 * Submodules:
 * - [alignedPaddedStr][io.github.kotlinmania.starlark_kotlin.collections.AlignedPaddedStr] - aligned padded string
 * - [alloca][io.github.kotlinmania.starlark_kotlin.collections.Alloca] - stack allocator
 * - [maybeUninitBackport][io.github.kotlinmania.starlark_kotlin.collections.MaybeUninitBackport] - maybe uninit
 * - [stringPool][io.github.kotlinmania.starlark_kotlin.collections.StringPool] - string pool
 * - [symbol][io.github.kotlinmania.starlark_kotlin.collections.Symbol] - symbol table
 */

// Re-exports from starlark_map equivalent (mirrors Rust's pub use starlark_map::*)
internal typealias EquivalentExport<T> = io.github.kotlinmania.starlark_kotlin.collections.Equivalent<T>
internal typealias HashedExport<K> = io.github.kotlinmania.starlark_kotlin.collections.Hashed<K>
internal typealias StarlarkHashValueExport = io.github.kotlinmania.starlark_kotlin.collections.StarlarkHashValue
internal typealias StarlarkHasherExport = io.github.kotlinmania.starlark_kotlin.collections.StarlarkHasher
internal typealias SmallMapExport<K, V> = io.github.kotlinmania.starlark_kotlin.collections.SmallMap<K, V>
internal typealias SmallSetExport<T> = io.github.kotlinmania.starlark_kotlin.collections.SmallSet<T>
