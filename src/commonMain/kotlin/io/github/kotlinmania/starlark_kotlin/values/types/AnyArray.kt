// port-lint: source src/values/types/any_array.rs
package io.github.kotlinmania.starlark_kotlin.values.types.any_array

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

/// Utility to heap allocate arrays of values.

import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue

// #[derive(derive_more::Display, ProvidesStaticType, NoSerialize, Allocative)]
// #[repr(C)]
// pub(crate) struct AnyArray<T: Debug + 'static> {
//     pub(crate) len: usize,
//     content: [T; 0],
// }
// Kotlin: GC handles memory layout. AnyArray is a simple wrapper around a list.
internal class AnyArray<T>(
    private val content: MutableList<T>,
) : StarlarkValue {

    // pub(crate) len: usize
    val len: Int get() = content.size

    companion object {
        /// This function is unsafe in Rust because it does not initialize content array,
        /// but drops in destructor.
        // pub(crate) unsafe fn new(len: usize) -> AnyArray<T>
        // Kotlin: creates an empty array with the given capacity.
        fun <T> new(len: Int): AnyArray<T> {
            return AnyArray(ArrayList(len))
        }
    }

    // fn as_slice(&self) -> &[T]
    fun asSlice(): List<T> = content

    // pub(crate) fn offset_of_content() -> usize
    // Kotlin: not applicable (no C repr layout). Kept for API parity.
    @Suppress("UNUSED")
    fun offsetOfContent(): Int = 0

    // Indexed access (needed for Kotlin interop since Rust uses array indexing)
    operator fun get(index: Int): T = content[index]

    operator fun set(index: Int, value: T) {
        content[index] = value
    }

    fun add(value: T) {
        content.add(value)
    }

    // impl Debug for AnyArray<T>
    // fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result
    override fun toString(): String {
        return "AnyArray(${asSlice()})"
    }

    // impl Drop for AnyArray<T>
    // Kotlin: GC handles cleanup. No explicit drop needed.

    // #[starlark_value(type = "AnyArray")]
    // impl StarlarkValue for AnyArray<T>
    override val TYPE: String get() = "AnyArray"
}

// #[cfg(test)] mod tests
// Tests are in commonTest.
