// port-lint: source src/collections/maybeUninitBackport.rs
package io.github.kotlinmania.starlark.collections

/*
 * Copyright 2019 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not import this file except in compliance with the License.
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

// All values are initialized by construction in Kotlin, so these helpers reduce to
// simple array copies that mirror the slice semantics of the original backport.

/**
 * Copy-paste of the slice-cloned write helper. Replace back when stabilized.
 */
fun <T> maybeUninitWriteSliceCloned(dest: Array<T?>, src: List<T>): Array<T?> {
    require(dest.size == src.size) {
        "destination and source slices have different lengths"
    }
    val len = dest.size
    for (i in 0 until len) {
        dest[i] = src[i]
    }
    return dest
}

/**
 * Copy-paste of the slice write helper. Replace back when stabilized.
 */
fun <T> maybeUninitWriteSlice(dest: Array<T?>, src: List<T>): Array<T?> {
    require(dest.size == src.size) {
        "destination and source slices have different lengths"
    }
    src.forEachIndexed { i, v -> dest[i] = v }
    return dest
}
