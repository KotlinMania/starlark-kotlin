// port-lint: source src/values/layout/aligned_size.rs
package io.github.kotlinmania.starlark_kotlin.values.layout.aligned_size

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

import io.github.kotlinmania.starlark_kotlin.values.layout.heap.AValueHeader

/** Allocations in Starlark are word-aligned, and this type represents the size of an allocation. */
internal data class AlignedSize(
    /** Starlark only supports objects smaller than 1 shl 32. */
    // bytes: u32
    val bytes: UInt,
) : Comparable<AlignedSize> {

    override fun compareTo(other: AlignedSize): Int {
        return bytes.compareTo(other.bytes)
    }

    override fun toString(): String = bytes.toString()

    // impl AlignedSize

    companion object {
        // pub(crate) const ZERO: AlignedSize = AlignedSize::new_bytes(0);
        val ZERO: AlignedSize = AlignedSize(0u)

        // const MAX_SIZE: AlignedSize = AlignedSize::new_bytes(u32::MAX as usize - AValueHeader::ALIGN + 1);
        private val MAX_SIZE: AlignedSize =
            AlignedSize((UInt.MAX_VALUE - AValueHeader.ALIGN.toUInt() + 1u))

        // pub(crate) const fn new_bytes(bytes: usize) -> AlignedSize
        fun newBytes(bytes: Int): AlignedSize {
            val ubytes = bytes.toUInt()
            require(ubytes % AValueHeader.ALIGN.toUInt() == 0u) {
                "AlignedSize must be aligned"
            }
            return AlignedSize(ubytes)
        }

        // pub(crate) const fn align_up(bytes: usize) -> AlignedSize
        fun alignUp(bytes: Int): AlignedSize {
            require(bytes.toUInt() <= MAX_SIZE.bytes) {
                "AlignedSize must not exceed UInt.MAX_VALUE"
            }
            val align = AValueHeader.ALIGN.toUInt()
            val aligned = (bytes.toUInt() + align - 1u) and (align - 1u).inv()
            return AlignedSize(aligned)
        }

        // pub(crate) const fn of<T>() -> AlignedSize
        // Kotlin: No `mem::size_of::<T>()`. Callers must provide explicit sizes.
        // fun of(sizeOfT: Int): AlignedSize = alignUp(sizeOfT)
    }

    // pub(crate) const fn bytes(self) -> u32
    fun bytes(): UInt = bytes

    // pub(crate) const fn layout(self) -> Layout
    // Kotlin: No `std::alloc::Layout`. Not transliterable.

    // pub(crate) fn checked_next_power_of_two(self) -> Option<AlignedSize>
    fun checkedNextPowerOfTwo(): AlignedSize? {
        // Find next power of two for bytes.
        if (bytes == 0u) return AlignedSize(0u)
        var v = bytes - 1u
        v = v or (v shr 1)
        v = v or (v shr 2)
        v = v or (v shr 4)
        v = v or (v shr 8)
        v = v or (v shr 16)
        val next = v + 1u
        if (next == 0u) return null // overflow
        return AlignedSize(next)
    }

    // pub(crate) fn unchecked_sub(self, rhs: AlignedSize) -> AlignedSize
    fun uncheckedSub(rhs: AlignedSize): AlignedSize {
        return AlignedSize(bytes - rhs.bytes)
    }

    // pub(crate) fn ptr_diff(begin: NonNull<usize>, end: NonNull<usize>) -> AlignedSize
    // Kotlin: No raw pointer arithmetic. Not transliterable.

    // impl Add for AlignedSize
    operator fun plus(rhs: AlignedSize): AlignedSize {
        val result = bytes + rhs.bytes
        check(result >= bytes) { "AlignedSize overflow" }
        return AlignedSize(result)
    }

    // impl Sub for AlignedSize
    operator fun minus(rhs: AlignedSize): AlignedSize {
        check(bytes >= rhs.bytes) { "AlignedSize underflow" }
        return AlignedSize(bytes - rhs.bytes)
    }

    // impl Mul<u32> for AlignedSize
    operator fun times(rhs: UInt): AlignedSize {
        val result = bytes * rhs
        if (rhs != 0u) {
            check(result / rhs == bytes) { "AlignedSize overflow" }
        }
        return AlignedSize(result)
    }
}

// #[cfg(test)] mod tests { ... }
// Tests are in commonTest, not here.
