// port-lint: source src/values/types/dict.rs
package io.github.kotlinmania.starlark_kotlin.values.types.dict

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
 * - [Alloc.kt][io.github.kotlinmania.starlark_kotlin.values.types.dict.AllocDict] — allocation helper
 * - [DictType.kt][io.github.kotlinmania.starlark_kotlin.values.types.dict.DictType] — type descriptor
 * - [Globals.kt] — global dict constructor functions
 * - [Methods.kt] — dict methods (keys, values, items, etc.)
 * - [Refs.kt] — [DictMut], [DictRef], [FrozenDictRef] reference types
 * - [Traits.kt] — StarlarkValue trait implementations for Dict
 * - [Unpack.kt][io.github.kotlinmania.starlark_kotlin.values.types.dict.UnpackDictEntries] — dict unpacking
 * - [Value.kt][io.github.kotlinmania.starlark_kotlin.values.types.dict.Dict] — the Dict value type
 */

// Re-exports from submodules (Kotlin: all public types in this package are inherently accessible)
// Rust: pub use crate::values::dict::alloc::AllocDict;
// Rust: pub use crate::values::dict::dict_type::DictType;
// Rust: pub use crate::values::dict::refs::DictMut;
// Rust: pub use crate::values::dict::refs::DictRef;
// Rust: pub use crate::values::dict::refs::FrozenDictRef;
// Rust: pub use crate::values::dict::unpack::UnpackDictEntries;
// Rust: pub use crate::values::dict::value::Dict;
