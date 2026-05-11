<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/collections/MaybeUninitBackport.kt
// port-lint: source collections/maybe_uninit_backport.rs
package io.github.kotlinmania.starlark.collections
=======
// port-lint: source src/collections/maybe_uninit_backport.rs
package io.github.kotlinmania.starlark_kotlin.collections
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/collections/MaybeUninitBackport.kt

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

// In Rust, MaybeUninit<T> is used to work with uninitialized memory safely.
// Kotlin has no equivalent concept — all values are initialized by construction.
// These functions translate Rust's MaybeUninit slice write operations
// to simple Kotlin array copy/clone operations.

/**
 * Write a cloned copy of each element from [src] into the destination array [dest].
 *
 * Analogous to `MaybeUninit::write_slice_cloned` (unstable in std).
 * In Kotlin, this is a simple array copy since all values are always initialized.
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
 * Write a copy of each element from [src] into the destination array [dest].
 *
 * Analogous to `MaybeUninit::write_slice` (unstable in std).
 * In Kotlin, this is a simple array copy since all values are always initialized.
 */
fun <T> maybeUninitWriteSlice(dest: Array<T?>, src: List<T>): Array<T?> {
    require(dest.size == src.size) {
        "destination and source slices have different lengths"
    }
    src.forEachIndexed { i, v -> dest[i] = v }
    return dest
}
