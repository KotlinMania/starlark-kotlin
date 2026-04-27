// port-lint: source src/collections/alignedPaddedStr.rs
package io.github.kotlinmania.starlark.collections.alignedpaddedstr

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

/** String which is `Long`-aligned with zeros padding in the end. */
//     data: *const usize,
// Kotlin: uses LongArray as word-aligned storage. Index simulates pointer offset.
internal class AlignedPaddedStr(
    /** In bytes. */
    private val len: Int,
    /** Data containing `len` bytes and zero padding in the end. */
    private val data: LongArray,
    /** Offset into data array (simulates pointer). */
    private val offset: Int = 0,
) {

    companion object {
        // sizeof<usize> equivalent for Long = 8 bytes
        private const val WORD_SIZE: Int = Long.SIZE_BYTES

        fun new(len: Int, data: LongArray, offset: Int = 0): AlignedPaddedStr {
            return AlignedPaddedStr(len = len, data = data, offset = offset)
        }
    }

    /** Len of string in words. */
    private fun lenWords(): Int {
        return (len + WORD_SIZE - 1) / WORD_SIZE
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AlignedPaddedStr) return false
        if (len != other.len) {
            return false
        }

        // We know strings are aligned, zero-padded and short,
        // so we can do better than generic SIMD-optimized `memcmp`
        val lenWords = lenWords()
        for (i in 0 until lenWords) {
            if (data[offset + i] != other.data[other.offset + i]) {
                return false
            }
        }
        return true
    }

    override fun hashCode(): Int {
        var result = len
        val lenWords = lenWords()
        for (i in 0 until lenWords) {
            result = 31 * result + data[offset + i].hashCode()
        }
        return result
    }
}
