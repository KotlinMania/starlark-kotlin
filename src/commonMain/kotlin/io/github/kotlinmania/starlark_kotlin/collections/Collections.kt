// port-lint: source src/collections.rs
@file:Suppress("unused", "ObjectPropertyName")
package io.github.kotlinmania.starlark_kotlin.collections

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

/// Defines [`SmallMap`] and [`SmallSet`] - collections with deterministic iteration and small memory footprint.
///
/// These structures use vector backed storage if there are only a few elements, and and index
/// for larger collections. The API mirrors standard Rust collections.

// pub use starlark_map::Equivalent;
typealias Hashed<K> = starlark_map.Hashed<K>
typealias StarlarkHashValue = starlark_map.StarlarkHashValue
typealias StarlarkHasher = starlark_map.StarlarkHasher
// pub use starlark_map::small_map::IntoIter;
// pub use starlark_map::small_map::Iter;
// pub use starlark_map::small_map::IterMut;
typealias SmallMap<K, V> = starlark_map.small_map.SmallMap<K, V>
typealias SmallSet<V> = starlark_map.small_set.SmallSet<V>

// pub(crate) mod aligned_padded_str;
internal val aligned_padded_str = "aligned_padded_str"
// pub(crate) mod alloca;
internal val alloca = "alloca"
// pub(crate) mod maybe_uninit_backport;
internal val maybe_uninit_backport = "maybe_uninit_backport"
// pub(crate) mod string_pool;
internal val string_pool = "string_pool"
// pub(crate) mod symbol;
// internal val symbol = "symbol" // conflicts with collections.symbol package
