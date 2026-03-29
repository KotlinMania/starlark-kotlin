// port-lint: source src/values/layout/heap.rs
@file:Suppress("unused")
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
//
// This is a Rust module declaration file (heap.rs). In Kotlin, Rust modules
// map to packages. Each submodule below corresponds to a Kotlin file or
// sub-package within `values.layout.heap`.
//
// Rust module declarations and their Kotlin equivalents:
//
// pub(crate) mod allocator;
//   -> package values.layout.heap.allocator (Allocator.kt + allocator/ sub-package)
//   Heap memory allocator.
//
// pub(crate) mod arena;
//   -> Arena.kt
//   Arena-based allocation.
//
// mod branding;
//   -> Branding.kt (private in Rust, internal in Kotlin)
//   Heap branding for safety.
//
// pub(crate) mod call_enter_exit;
//   -> CallEnterExit.kt
//   Call stack enter/exit tracking for profiling.
//
// mod fast_cell;
//   -> FastCell.kt (private in Rust, internal in Kotlin)
//   Fast interior mutability cell.
//
// pub(crate) mod heap_type;
//   -> HeapType.kt
//   Core Heap and FrozenHeap implementations.
//
// pub(crate) mod maybe_uninit_slice_util;
//   -> MaybeUninitSliceUtil.kt
//   Uninitialized slice utilities.
//
// pub(crate) mod profile;
//   -> package values.layout.heap.profile (Profile.kt + profile/ sub-package)
//   Heap profiling (aggregated, alloc_counts, by_type, string_index, summary_by_function).
//
// pub(crate) mod repr;
//   -> Repr.kt
//   AValue representation types (AValueHeader, AValueRepr, AValueOrForward).
//
// pub(crate) mod send;
//   -> Send.kt
//   Send/Sync safety wrappers.
