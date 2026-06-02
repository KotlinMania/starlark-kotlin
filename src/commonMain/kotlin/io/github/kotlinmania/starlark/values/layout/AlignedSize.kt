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

// use std::alloc::Layout;
// use std::mem;
// use std::ops::Add;
// use std::ops::Mul;
// use std::ops::Sub;
// use std::ptr::NonNull;

// use allocative::Allocative;
// use dupe::Dupe;

import io.github.kotlinmania.starlark.values.layout.heap.AValueHeader

// / Allocations in Starlark are word-aligned, and this type represents the size of an allocation.
// #[derive(Copy, Clone, Dupe, Default, Debug, Eq, PartialEq, Ord, PartialOrd, Hash, Allocative, derive_more::Display)]
// #[repr(transparent)]
data class AlignedSize(
    // / Starlark only supports objects smaller than 1<<32.
    // bytes: u32
    val bytes: UInt,
) : Comparable<AlignedSize> {
    override fun compareTo(other: AlignedSize): Int = bytes.compareTo(other.bytes)

    override fun toString(): String = bytes.toString()

    // impl AlignedSize

    companion object {
        // pub(crate) const ZERO: AlignedSize = AlignedSize::new_bytes(0);
        val ZERO: AlignedSize = AlignedSize(0u)

        // const MAX_SIZE: AlignedSize = AlignedSize::new_bytes(u32::MAX as usize - AValueHeader::ALIGN + 1);
        private val MAX_SIZE: AlignedSize =
            AlignedSize((UInt.MAX_VALUE - AValueHeader.ALIGN.toUInt() + 1u))

        // #[track_caller]
        // #[inline]
        // pub(crate) const fn new_bytes(bytes: usize) -> AlignedSize
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

        // #[track_caller]
        // #[inline]
        // pub(crate) const fn align_up(bytes: usize) -> AlignedSize
        fun alignUp(bytes: Int): AlignedSize {
            require(bytes.toUInt() <= MAX_SIZE.bytes) {
                "AlignedSize must not exceed u32::MAX"
            }
            val aligned = (bytes.toUInt() + AValueHeader.ALIGN.toUInt() - 1u) and (AValueHeader.ALIGN.toUInt() - 1u).inv()
            return AlignedSize(aligned)
        }

        // #[inline]
        // pub(crate) const fn of<T>() -> AlignedSize
        // AlignedSize::align_up(mem::size_of::<T>())
        // Kotlin: No mem::size_of::<T>(). Callers must provide explicit sizes.
        fun of(sizeOfT: Int): AlignedSize = alignUp(sizeOfT)
    }

    // #[inline]
    // pub(crate) const fn bytes(self) -> u32
    fun bytes(): UInt = bytes

    // #[inline]
    // pub(crate) const fn layout(self) -> Layout
    // Layout::from_size_align(self.bytes as usize, AValueHeader::ALIGN)
    // Kotlin: No std::alloc::Layout equivalent.

    // #[inline]
    // pub(crate) fn checked_next_power_of_two(self) -> Option<AlignedSize>
    fun checkedNextPowerOfTwo(): AlignedSize? {
        val nextBytes = bytes.checkedNextPowerOfTwo() ?: return null
        return AlignedSize(nextBytes)
    }

    // #[inline]
    // pub(crate) fn unchecked_sub(self, rhs: AlignedSize) -> AlignedSize
    fun uncheckedSub(rhs: AlignedSize): AlignedSize {
        check(bytes >= rhs.bytes) { "$this - $rhs" }
        return AlignedSize(bytes - rhs.bytes)
    }

    // #[inline]
    // pub(crate) fn ptr_diff(begin: NonNull<usize>, end: NonNull<usize>) -> AlignedSize
    // unsafe { AlignedSize::new_bytes(end.as_ptr().byte_offset_from(begin.as_ptr()) as usize) }
    // Kotlin: No raw pointer arithmetic. Not transliterable.

    // impl Add for AlignedSize
    // type Output = AlignedSize;
    // #[track_caller]
    // #[inline]
    // fn add(self, rhs: AlignedSize) -> AlignedSize
    operator fun plus(rhs: AlignedSize): AlignedSize {
        val result = bytes.checkedAdd(rhs.bytes)
        checkNotNull(result) { "AlignedSize overflow" }
        return AlignedSize(result)
    }

    // impl Sub for AlignedSize
    // type Output = AlignedSize;
    // #[track_caller]
    // #[inline]
    // fn sub(self, rhs: AlignedSize) -> AlignedSize
    operator fun minus(rhs: AlignedSize): AlignedSize {
        val result = bytes.checkedSub(rhs.bytes)
        checkNotNull(result) { "AlignedSize underflow" }
        return AlignedSize(result)
    }

    // impl Mul<u32> for AlignedSize
    // type Output = AlignedSize;
    // #[track_caller]
    // #[inline]
    // fn mul(self, rhs: u32) -> Self::Output
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

// #[cfg(test)]
// mod tests {
//     use crate::values::layout::aligned_size::AlignedSize;
//     use crate::values::layout::heap::repr::AValueHeader;
//
//     #[test]
//     fn test_checked_next_power_of_two() {
//         assert_eq!(
//             AlignedSize::new_bytes(AValueHeader::ALIGN),
//             AlignedSize::new_bytes(AValueHeader::ALIGN)
//                 .checked_next_power_of_two()
//                 .unwrap()
//         );
//         assert_eq!(
//             AlignedSize::new_bytes(2 * AValueHeader::ALIGN),
//             AlignedSize::new_bytes(2 * AValueHeader::ALIGN)
//                 .checked_next_power_of_two()
//                 .unwrap()
//         );
//         assert_eq!(
//             AlignedSize::new_bytes(4 * AValueHeader::ALIGN),
//             AlignedSize::new_bytes(3 * AValueHeader::ALIGN)
//                 .checked_next_power_of_two()
//                 .unwrap()
//         );
//         assert_eq!(
//             AlignedSize::new_bytes(8 * AValueHeader::ALIGN),
//             AlignedSize::new_bytes(5 * AValueHeader::ALIGN)
//                 .checked_next_power_of_two()
//                 .unwrap()
//         );
//     }
//
//     #[test]
//     fn test_sub() {
//         assert_eq!(
//             AlignedSize::new_bytes(2 * AValueHeader::ALIGN),
//             AlignedSize::new_bytes(5 * AValueHeader::ALIGN)
//                 - AlignedSize::new_bytes(3 * AValueHeader::ALIGN)
//         );
//     }
// }
