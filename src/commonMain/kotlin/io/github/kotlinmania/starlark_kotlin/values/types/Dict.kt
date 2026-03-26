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

private fun modules() {
    val alloc = AllocDict::class
    val dictType = DictType::class
    val globals = ::registerDict
    val methods = ::dictMethods
    val refs = DictRef::class
    val traits = SmallMapUnpackValue::class
    val unpack = UnpackDictEntries::class
    val value = Dict::class
    val allocDict = AllocDict::class
    val pubDictType = DictType::class
    val dictMut = DictMut::class
    val dictRef = DictRef::class
    val frozenDictRef = FrozenDictRef::class
    val unpackDictEntries = UnpackDictEntries::class
    val dict = Dict::class
}
