// port-lint: source src/values/types/anyArray.rs
package io.github.kotlinmania.starlark.values.types.anyarray

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

//! Utility to heap allocate arrays of values.

import io.github.kotlinmania.starlark.values.StarlarkValue

//     content: [T; 0],
// Kotlin: GC handles memory layout. AnyArray is a simple wrapper around a list.
internal class AnyArray<T>(
    private val content: MutableList<T>,
) : StarlarkValue {

    val len: Int get() = content.size

    companion object {
        /**
         * This function is unsafe in Rust because it does not initialize content array,
         * but drops in destructor.
         */
        // Kotlin: creates an empty array with the given capacity.
        fun <T> new(len: Int): AnyArray<T> {
            return AnyArray(ArrayList(len))
        }
    }

    fun asSlice(): List<T> = content

    // Kotlin: not applicable (no C repr layout). Kept for API parity.
    @Suppress("UNUSED")
    fun offsetOfContent(): Int = 0

    operator fun get(index: Int): T = content[index]

    operator fun set(index: Int, value: T) {
        content[index] = value
    }

    fun add(value: T) {
        content.add(value)
    }

    override fun toString(): String {
        return "AnyArray(${asSlice()})"
    }

    // Kotlin: GC handles cleanup. No explicit drop needed.

    override val TYPE: String get() = "AnyArray"
}

// Tests are in commonTest.
