// port-lint: source src/values/layout/aligned_size.rs
package io.github.kotlinmania.starlark.values.layout

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

import io.github.kotlinmania.starlark.values.layout.heap.AValueHeader

// / Allocations in Starlark are word-aligned, and this type represents the size of an allocation.
data class AlignedSize(
    // / Starlark only supports objects smaller than 1<<32.
    // bytes: u32
    val bytes: UInt,
) : Comparable<AlignedSize> {
    override fun compareTo(other: AlignedSize): Int = bytes.compareTo(other.bytes)

    override fun toString(): String = bytes.toString()


    companion object {
        val ZERO: AlignedSize = AlignedSize(0u)

        private val MAX_SIZE: AlignedSize =
            AlignedSize((UInt.MAX_VALUE - AValueHeader.ALIGN.toUInt() + 1u))

        fun newBytes(bytes: Int): AlignedSize {
            val ubytes = bytes.toUInt()
            require(ubytes % AValueHeader.ALIGN.toUInt() == 0u) {
                "AlignedSize must be aligned"
            }
            require(ubytes.toInt().toUInt() == ubytes) {
                "AlignedSize must not exceed u32::MAX"
            }
            return AlignedSize(ubytes)
        }

        fun alignUp(bytes: Int): AlignedSize {
            require(bytes.toUInt() <= MAX_SIZE.bytes) {
                "AlignedSize must not exceed u32::MAX"
            }
            val aligned = (bytes.toUInt() + AValueHeader.ALIGN.toUInt() - 1u) and (AValueHeader.ALIGN.toUInt() - 1u).inv()
            return AlignedSize(aligned)
        }

        // Kotlin: No mem::size_of::<T>(). Callers must provide explicit sizes.
        fun of(sizeOfT: Int): AlignedSize = alignUp(sizeOfT)
    }

    fun bytes(): UInt = bytes

    // Kotlin: No std::alloc::Layout equivalent.

    fun checkedNextPowerOfTwo(): AlignedSize? {
        val nextBytes = bytes.checkedNextPowerOfTwo() ?: return null
        return AlignedSize(nextBytes)
    }

    fun uncheckedSub(rhs: AlignedSize): AlignedSize {
        check(bytes >= rhs.bytes) { "$this - $rhs" }
        return AlignedSize(bytes - rhs.bytes)
    }

    // Kotlin: No raw pointer arithmetic. Not transliterable.

    operator fun plus(rhs: AlignedSize): AlignedSize {
        val result = bytes.checkedAdd(rhs.bytes)
        checkNotNull(result) { "AlignedSize overflow" }
        return AlignedSize(result)
    }

    operator fun minus(rhs: AlignedSize): AlignedSize {
        val result = bytes.checkedSub(rhs.bytes)
        checkNotNull(result) { "AlignedSize underflow" }
        return AlignedSize(result)
    }

    operator fun times(rhs: UInt): AlignedSize {
        val result = bytes.checkedMul(rhs)
        checkNotNull(result) { "AlignedSize overflow" }
        return AlignedSize(result)
    }
}

// u32 checked arithmetic helpers
private fun UInt.checkedAdd(other: UInt): UInt? {
    val result = this + other
    return if (result < this) null else result
}

private fun UInt.checkedSub(other: UInt): UInt? = if (this < other) null else this - other

private fun UInt.checkedMul(other: UInt): UInt? {
    if (other == 0u) return 0u
    val result = this * other
    return if (result / other != this) null else result
}

private fun UInt.checkedNextPowerOfTwo(): UInt? {
    if (this == 0u) return 1u
    val v = this - 1u
    val next = v or (v shr 1)
    val next2 = next or (next shr 2)
    val next3 = next2 or (next2 shr 4)
    val next4 = next3 or (next3 shr 8)
    val next5 = next4 or (next4 shr 16)
    val result = next5 + 1u
    return if (result == 0u) null else result
}

//
//         assert_eq!(
//                 .checked_next_power_of_two()
//         );
//         assert_eq!(
//                 .checked_next_power_of_two()
//         );
//         assert_eq!(
//                 .checked_next_power_of_two()
//         );
//         assert_eq!(
//                 .checked_next_power_of_two()
//         );
//
//         assert_eq!(
//                 - AlignedSize::new_bytes(3 * AValueHeader::ALIGN)
//         );
