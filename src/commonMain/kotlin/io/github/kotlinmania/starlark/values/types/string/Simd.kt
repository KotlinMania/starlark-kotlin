// port-lint: source src/values/types/string/simd.rs
package io.github.kotlinmania.starlark.values.types.string

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
 * Fixed length byte vector API.
 *
 * Note: In Rust this trait requires `Copy`, which allows passing by value.
 * In Kotlin, we model this as a value-like interface. Implementations should
 * be lightweight and efficiently copyable.
 */
internal interface Vector {
    /**
     * Fill the vector with given byte value.
     */
    fun splat(byte: Byte): Vector

    /**
     * Load the vector from given memory address.
     */
    fun loadUnaligned(ptr: ByteArray, offset: Int): Vector

    /**
     * Store the vector to given memory address.
     */
    fun storeUnaligned(ptr: ByteArray, offset: Int)

    /**
     * **Signed** element-wise comparison of the vector.
     */
    fun cmplt(other: Vector): Vector

    /**
     * Element-wise comparison. Result elements contain 0 for false or 0xff for true.
     */
    fun cmpeq(other: Vector): Vector

    /**
     * Bitwise or.
     */
    fun or(other: Vector): Vector

    /**
     * Mask of the most significant bit of each element.
     * For 16-bytes vector this instruction fills lower 16 bits of the result.
     */
    fun movemask(): UInt
}

/**
 * Run different code depending on whether SIMD is available or not.
 */
internal interface SwitchHaveSimd<R> {
    /**
     * This function is called when SIMD is not available.
     */
    fun noSimd(): R

    /**
     * This function is called when SIMD is available.
     */
    fun <V : Vector> simd(): R

    /**
     * Call either [simd] or [noSimd] function.
     */
    fun switch(): R {
        // Any x86_64 supports SSE2.
        // In Kotlin Multiplatform, we don't currently have SIMD support,
        // so this always falls back to noSimd().
        // Platform-specific implementations can override this via expect/actual.
        return switchImpl()
    }
}

/**
 * Implementation of [SwitchHaveSimd.switch].
 *
 * The Rust version checks for SSE2 support at compile time and dispatches
 * to the appropriate implementation. In Kotlin, we always use the non-SIMD path.
 */
internal fun <R> SwitchHaveSimd<R>.switchImpl(): R = noSimd()
