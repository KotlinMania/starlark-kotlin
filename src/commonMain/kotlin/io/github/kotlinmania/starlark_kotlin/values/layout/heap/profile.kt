// port-lint: source src/values/layout/heap/profile.rs
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

/**
 * Summary of heap allocations and function times with stacks.
 *
 * Submodules (Kotlin packages under values.layout.heap.profile):
 * - pub(crate) mod aggregated     -> values.layout.heap.profile.aggregated
 * - pub(crate) mod alloc_counts   -> values.layout.heap.profile.alloc_counts
 * - pub(crate) mod by_type        -> values.layout.heap.profile.by_type
 * - pub(crate) mod string_index   -> values.layout.heap.profile.string_index
 * - mod summary_by_function       -> values.layout.heap.profile.summary_by_function (private)
 */
