// port-lint: source src/values/types/list.rs
@file:Suppress("unused", "UNUSED_VARIABLE")
package io.github.kotlinmania.starlark_kotlin.values.types.list
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

/** The list type, a mutable sequence of values. */

private fun init() {
    val alloc = AllocList::class
    val globals = ListTypeFunction::class
    val listType = ListType::class
    val methods = ListRef::class
    val refs = FrozenListRef::class
    val unpack = UnpackList::class
    val value = ListGen::class
    val pubAllocList = io.github.kotlinmania.starlark_kotlin.values.types.list.AllocList::class
    val pubListType = io.github.kotlinmania.starlark_kotlin.values.types.list.ListType::class
    val pubListRef = io.github.kotlinmania.starlark_kotlin.values.types.list.ListRef::class
    val pubUnpackList = io.github.kotlinmania.starlark_kotlin.values.types.list.UnpackList::class
}
