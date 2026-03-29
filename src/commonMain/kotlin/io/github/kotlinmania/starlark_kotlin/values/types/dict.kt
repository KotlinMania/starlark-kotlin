// port-lint: source src/values/types/dict.rs
@file:Suppress("unused", "UNUSED_VARIABLE")
package io.github.kotlinmania.starlark_kotlin.values.types.dict
// Copyright 2018 The Starlark in Rust Authors.
// Copyright (c) Facebook, Inc. and its affiliates.
// Copyright (c) 2025 Sydney Renee, The Solace Project
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//     https://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/** The dictionary type, a mutable associative-map, which iterates in insertion order. */

private fun init() {
    // mod alloc
    val alloc = AllocDict::class
    // mod dict_type
    val dictType = DictType::class
    // pub(crate) mod globals
    val globals = ::registerDict
    // pub(crate) mod methods
    val methods = ::dictMethods
    // mod refs
    val refs = DictRef::class
    // mod traits
    val traits = SmallMapRefStarlarkTypeRepr::class
    // pub(crate) mod unpack
    val unpack = UnpackDictEntries::class
    // pub(crate) mod value
    val value = Dict::class
}
