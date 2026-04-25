// port-lint: source src/values/layout/heap/profile.rs
package io.github.kotlinmania.starlark_kotlin.values.layout.heap.profile

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
 * Submodules:
 * - [aggregated][io.github.kotlinmania.starlark_kotlin.values.layout.heap.profile.Aggregated] - Aggregated profile data
 * - [allocCounts][io.github.kotlinmania.starlark_kotlin.values.layout.heap.profile.AllocCounts] - Allocation counters
 * - [byType][io.github.kotlinmania.starlark_kotlin.values.layout.heap.profile.ByType] - Profile data grouped by type
 * - [stringIndex][io.github.kotlinmania.starlark_kotlin.values.layout.heap.profile.StringIndex] - String interning index for profiles
 * - [summaryByFunction][io.github.kotlinmania.starlark_kotlin.values.layout.heap.profile.SummaryByFunction] - Summary organized by function
 */
