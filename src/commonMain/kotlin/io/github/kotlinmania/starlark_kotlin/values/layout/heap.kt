// port-lint: source src/values/layout/heap.rs
@file:Suppress("unused", "ObjectPropertyName")
package io.github.kotlinmania.starlark_kotlin.values.layout.heap

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

// Starlark heap implementation.

internal val allocator = "allocator"
internal val arena = "arena"
internal val branding = "branding"
internal val call_enter_exit = "call_enter_exit"
internal val fast_cell = "fast_cell"
internal val heap_type = "heap_type"
internal val maybe_uninit_slice_util = "maybe_uninit_slice_util"
internal val profile = "profile"
internal val repr = "repr"
internal val send = "send"
