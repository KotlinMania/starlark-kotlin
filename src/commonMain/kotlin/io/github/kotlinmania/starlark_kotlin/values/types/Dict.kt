// port-lint: source src/values/types/dict.rs
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
 * The dictionary type, a mutable associative-map, which iterates in insertion order.
 *
 * Submodules:
 * - [alloc][io.github.kotlinmania.starlark_kotlin.values.types.dict.AllocDict] - allocation helpers
 * - [dictType][io.github.kotlinmania.starlark_kotlin.values.types.dict.DictType] - dict type marker
 * - [globals][io.github.kotlinmania.starlark_kotlin.values.types.dict.Globals] - global dict functions
 * - [methods][io.github.kotlinmania.starlark_kotlin.values.types.dict.Methods] - dict methods
 * - [refs][io.github.kotlinmania.starlark_kotlin.values.types.dict.Refs] - dict references (DictRef, DictMut, FrozenDictRef)
 * - [traits][io.github.kotlinmania.starlark_kotlin.values.types.dict.Traits] - SmallMap unpack traits
 * - [unpack][io.github.kotlinmania.starlark_kotlin.values.types.dict.UnpackDictEntries] - unpacking helpers
 * - [value][io.github.kotlinmania.starlark_kotlin.values.types.dict.Dict] - dict value type
 */

// Re-exports (mirrors Rust's pub use declarations)
internal typealias AllocDictExport<D> = io.github.kotlinmania.starlark_kotlin.values.types.dict.AllocDict<D>
internal typealias DictTypeExport<K, V> = io.github.kotlinmania.starlark_kotlin.values.types.dict.DictType<K, V>
internal typealias DictMutExport = io.github.kotlinmania.starlark_kotlin.values.types.dict.DictMut
internal typealias DictRefExport = io.github.kotlinmania.starlark_kotlin.values.types.dict.DictRef
internal typealias FrozenDictRefExport = io.github.kotlinmania.starlark_kotlin.values.types.dict.FrozenDictRef
internal typealias UnpackDictEntriesExport<K, V> = io.github.kotlinmania.starlark_kotlin.values.types.dict.UnpackDictEntries<K, V>
internal typealias DictExport = io.github.kotlinmania.starlark_kotlin.values.types.dict.Dict
