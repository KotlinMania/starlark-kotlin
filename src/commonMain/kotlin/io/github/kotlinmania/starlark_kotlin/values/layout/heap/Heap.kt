// port-lint: source src/values/layout/heap.rs
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

/** Starlark heap implementation. */

// Rust `mod` declarations have no direct Kotlin equivalent; Kotlin uses packages/files.
// To preserve the line-by-line transliteration structure (and keep the module list explicit),
// we represent Rust `mod` items as empty marker objects with matching names and visibility.

// pub(crate) mod allocator;
internal object allocator

// pub(crate) mod arena;
internal object arena

// mod branding;
private object branding

// pub(crate) mod call_enter_exit;
internal object call_enter_exit

// mod fast_cell;
private object fast_cell

// pub(crate) mod heap_type;
internal object heap_type

// pub(crate) mod maybe_uninit_slice_util;
internal object maybe_uninit_slice_util

// pub(crate) mod profile;
internal object profile

// pub(crate) mod repr;
internal object repr

// pub(crate) mod send;
internal object send
