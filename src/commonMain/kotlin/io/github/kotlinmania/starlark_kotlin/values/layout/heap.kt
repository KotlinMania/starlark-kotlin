// port-lint: source src/values/layout/heap.rs
package io.github.kotlinmania.starlark_kotlin.values.layout

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
// This file corresponds to the Rust module declaration `mod heap;` in
// values/layout/mod.rs. The actual heap module contents are in the
// values.layout.heap package (see Heap.kt in the heap/ subdirectory).
//
// Submodules (Kotlin packages under values.layout.heap):
//   pub(crate) mod allocator    -> heap.allocator package
//   pub(crate) mod arena        -> heap/Arena.kt
//   mod branding                -> heap/Branding.kt (private)
//   pub(crate) mod call_enter_exit -> heap/CallEnterExit.kt
//   mod fast_cell               -> heap/FastCell.kt (private)
//   pub(crate) mod heap_type    -> heap/HeapType.kt
//   pub(crate) mod maybe_uninit_slice_util -> heap/MaybeUninitSliceUtil.kt
//   pub(crate) mod profile      -> heap.profile package
//   pub(crate) mod repr         -> heap/Repr.kt
//   pub(crate) mod send         -> heap/Send.kt
