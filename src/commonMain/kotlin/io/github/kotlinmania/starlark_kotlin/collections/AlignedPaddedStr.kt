// port-lint: source src/collections/aligned_padded_str.rs
package io.github.kotlinmania.starlark_kotlin.collections.aligned_padded_str

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
 * String which is `usize` aligned with zeros padding in the end.
 *
 * In Rust, this is a low-level optimization that compares strings word-by-word
 * using aligned pointer access. In Kotlin, we wrap a regular [String] since
 * Kotlin/JVM strings are already interned and efficiently compared.
 *
 * The Rust implementation uses raw pointer arithmetic for SIMD-style comparison;
 * in Kotlin we delegate to the standard string equality which is already optimized
 * by the JVM/native runtime.
 */
// #[derive(Copy, Clone, Dupe)]
// pub(crate) struct AlignedPaddedStr<'a>
internal class AlignedPaddedStr(
    /** The string data. */
    val str: String,
) {
    // impl AlignedPaddedStr

    /** Length of the string in bytes. */
    val len: Int get() = str.length

    // In Rust: unsafe fn new(len: usize, data: *const usize) -> AlignedPaddedStr
    // In Kotlin, we construct from a String directly.

    // impl PartialEq for AlignedPaddedStr
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AlignedPaddedStr) return false
        return str == other.str
    }

    override fun hashCode(): Int = str.hashCode()
}
