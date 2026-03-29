// port-lint: source src/values/layout/heap.rs
@file:Suppress("unused")

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
 * Starlark heap implementation.
 *
 * In Rust, this module declares submodules:
 * - [allocator][io.github.kotlinmania.starlark_kotlin.values.layout.heap.allocator] - Memory allocation
 * - arena - Arena-based allocation ([Arena])
 * - branding - Heap branding for safety
 * - call_enter_exit - Call stack enter/exit tracking
 * - fast_cell - Fast interior mutability cell
 * - heap_type - Core [Heap] and [FrozenHeap] types
 * - maybe_uninit_slice_util - Uninitialized slice utilities
 * - profile - Heap profiling support
 * - repr - Value representation ([AValueHeader], [AValueRepr])
 * - send - Send/Sync safety wrappers
 *
 * In Kotlin, these are all individual files within this package.
 */
package io.github.kotlinmania.starlark_kotlin.values.layout.heap
