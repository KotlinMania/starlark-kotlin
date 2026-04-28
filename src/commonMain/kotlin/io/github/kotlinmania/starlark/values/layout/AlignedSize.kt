// port-lint: source values/layout/aligned_size.rs
package io.github.kotlinmania.starlark.values.layout

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

import io.github.kotlinmania.starlark.values.layout.heap.AValueHeader

/** Allocations in Starlark are word-aligned, and this type represents the size of an allocation. */
data class AlignedSize(
    /** Starlark only supports objects smaller than 1<<32. */
    val bytes: UInt,
) : Comparable<AlignedSize> {

    override fun compareTo(other: AlignedSize): Int = bytes.compareTo(other.bytes)

    override fun toString(): String = bytes.toString()

    fun bytes(): UInt = bytes

    fun checkedNextPowerOfTwo(): AlignedSize? {
        val nextBytes = bytes.checkedNextPowerOfTwo() ?: return null
        return newBytes(nextBytes.toInt())
    }

    fun uncheckedSub(rhs: AlignedSize): AlignedSize {
        check(bytes >= rhs.bytes) { "$this - $rhs" }
        return AlignedSize(bytes - rhs.bytes)
    }

    operator fun plus(rhs: AlignedSize): AlignedSize {
        val bytes = this.bytes.checkedAdd(rhs.bytes)
        checkNotNull(bytes) { "AlignedSize overflow" }
        return AlignedSize(bytes)
    }

    operator fun minus(rhs: AlignedSize): AlignedSize {
        val bytes = this.bytes.checkedSub(rhs.bytes)
        checkNotNull(bytes) { "AlignedSize underflow" }
        return AlignedSize(bytes)
    }

    operator fun times(rhs: UInt): AlignedSize {
        val bytes = this.bytes.checkedMul(rhs)
        checkNotNull(bytes) { "AlignedSize overflow" }
        return AlignedSize(bytes)
    }

    companion object {
        val ZERO: AlignedSize = newBytes(0)

        private val MAX_SIZE: AlignedSize =
            AlignedSize(UInt.MAX_VALUE - AValueHeader.ALIGN.toUInt() + 1u)

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
            val align = AValueHeader.ALIGN.toUInt()
            val aligned = (bytes.toUInt() + align - 1u) and (align - 1u).inv()
            return AlignedSize(aligned)
        }

        fun of(sizeOfT: Int): AlignedSize = alignUp(sizeOfT)
    }
}

private fun UInt.checkedAdd(other: UInt): UInt? {
    val result = this + other
    return if (result < this) null else result
}

private fun UInt.checkedSub(other: UInt): UInt? {
    return if (this < other) null else this - other
}

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
