// port-lint: source src/values/types/int.rs
package io.github.kotlinmania.starlark_kotlin.values.types

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
 * The integer type.
 *
 * For small values, we try not to allocate on the [Heap][io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap],
 * but instead use special values. If the value doesn't fit in the special representation,
 * we use BigInt to store it.
 *
 * Submodules:
 * - [globals][io.github.kotlinmania.starlark_kotlin.values.types.int.Globals] - global int functions
 * - [i32][io.github.kotlinmania.starlark_kotlin.values.types.int.I32] - 32-bit int operations
 * - [inlineInt][io.github.kotlinmania.starlark_kotlin.values.types.int.InlineInt] - inline int representation
 * - [intOrBig][io.github.kotlinmania.starlark_kotlin.values.types.int.IntOrBig] - int or big int union
 * - [pointerI32][io.github.kotlinmania.starlark_kotlin.values.types.int.PointerI32] - pointer-based i32
 */

// Re-export public API (mirrors Rust's `pub use pointer_i32::INT_TYPE`)
internal val INT_TYPE = io.github.kotlinmania.starlark_kotlin.values.types.int.INT_TYPE
