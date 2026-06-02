// port-lint: source src/vec_map/simd.rs
package io.github.kotlinmania.starlark.collections.vec_map.simd

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
 * Find a hash value in an array of hashes.
 *
 * In Rust, this has an optimized SIMD path (128-bit, 4 lanes) for nightly builds.
 * Kotlin does not have portable SIMD, so we use the scalar fallback.
 *
 * Corresponds to Rust `find_hash_in_array_without_simd` and `find_hash_in_array`.
 */
internal fun findHashInArrayWithoutSimd(array: UIntArray, hash: UInt): Int? {
    var i = 0
    while (i < array.size) {
        if (array[i] == hash) {
            return i
        }
        i += 1
    }
    return null
}

/**
 * Find a hash value in an array of hashes.
 *
 * This is the public entry point. In Rust, this dispatches to SIMD on nightly
 * and falls back to scalar otherwise. In Kotlin, we always use the scalar path.
 */
internal fun findHashInArray(array: UIntArray, hash: UInt): Int? {
    return findHashInArrayWithoutSimd(array, hash)
}
