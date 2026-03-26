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

/** The dictionary type, a mutable associative-map, which iterates in insertion order. */

// Rust mod declarations — in Kotlin, these are separate files in the dict/ package.
// mod alloc         -> dict/Alloc.kt
// mod dict_type     -> dict/DictType.kt
// mod globals       -> dict/Globals.kt
// mod methods       -> dict/Methods.kt
// mod refs          -> dict/Refs.kt
// mod traits        -> dict/Traits.kt
// mod unpack        -> dict/Unpack.kt
// mod value         -> dict/Value.kt

// Re-exports matching Rust `pub use` declarations.

typealias AllocDict<D> = io.github.kotlinmania.starlark_kotlin.values.types.dict.AllocDict<D>
typealias DictType<K, V> = io.github.kotlinmania.starlark_kotlin.values.types.dict.DictType<K, V>
typealias DictMut<V_> = io.github.kotlinmania.starlark_kotlin.values.types.dict.DictMut<V_>
typealias DictRef<V_> = io.github.kotlinmania.starlark_kotlin.values.types.dict.DictRef<V_>
typealias FrozenDictRef = io.github.kotlinmania.starlark_kotlin.values.types.dict.FrozenDictRef
typealias UnpackDictEntries<K, V> = io.github.kotlinmania.starlark_kotlin.values.types.dict.UnpackDictEntries<K, V>
typealias Dict<V_> = io.github.kotlinmania.starlark_kotlin.values.types.dict.Dict<V_>
