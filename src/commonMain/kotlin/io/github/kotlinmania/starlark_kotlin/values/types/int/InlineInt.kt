// port-lint: source src/values/types/int/inline_int.rs
package io.github.kotlinmania.starlark_kotlin.values.types.int

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

import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.kotlinmania.starlark_kotlin.likely
import kotlin.jvm.JvmInline

/**
 * Integer which is stored inline in `RawPointer`.
 *
 * This is a value class wrapper around [Int] (i32 in Rust) that enforces
 * bit width constraints based on platform pointer size.
 */
// Rust: pub struct InlineInt(i32)
@JvmInline
value class InlineInt internal constructor(private val value: Int) : Comparable<InlineInt> {

    companion object {
        // Rust: const fn min_max_for_bits(bits: usize) -> (i32, i32)
        private fun minMaxForBits(bits: Int): Pair<Int, Int> {
            val max = ((1L shl (bits - 1)) - 1).toInt()
            val min = -max - 1
            return Pair(min, max)
        }

        /**
         * Number of bits in the integer.
         *
         * In Rust this is 32 on 64-bit platforms, 29 on 32-bit.
         * Kotlin Multiplatform targets 64-bit, so we use 32.
         */
        internal const val BITS: Int = 32

        internal val ZERO: InlineInt = InlineInt(0)
        internal val MINUS_ONE: InlineInt = InlineInt(-1)
        internal val MIN: InlineInt = InlineInt(minMaxForBits(BITS).first)
        internal val MAX: InlineInt = InlineInt(minMaxForBits(BITS).second)

        /** This type does not contain full range of i32. */
        internal fun smallerThanI32(): Boolean {
            return BITS < 32
        }

        // Rust: fn new_unchecked(i: i32) -> InlineInt
        @Suppress("NOTHING_TO_INLINE")
        internal inline fun newUnchecked(i: Int): InlineInt {
            return InlineInt(i)
        }

        // Rust: fn testing_new(i: i32) -> InlineInt
        internal fun testingNew(i: Int): InlineInt {
            return tryFrom(i).getOrThrow()
        }

        // --- TryFrom impls ---

        // Rust: impl TryFrom<i32> for InlineInt
        internal fun tryFrom(value: Int): Result<InlineInt> {
            return tryFromImpl(value)
        }

        // Rust: impl TryFrom<u32> for InlineInt
        internal fun tryFrom(value: UInt): Result<InlineInt> {
            val i = value.toInt()
            if (i < 0) return Result.failure(InlineIntOverflow())
            return tryFromImpl(i).mapCatching { result ->
                if (result.toI32().toUInt() == value) {
                    result
                } else {
                    throw InlineIntOverflow()
                }
            }
        }

        // Rust: impl TryFrom<i64> for InlineInt
        internal fun tryFrom(value: Long): Result<InlineInt> {
            return tryFromImpl(value)
        }

        // Rust: impl TryFrom<u64> for InlineInt
        internal fun tryFrom(value: ULong): Result<InlineInt> {
            return tryFromImpl(value)
        }

        // Rust: impl TryFrom<&BigInt> for InlineInt
        internal fun tryFrom(value: BigInteger): Result<InlineInt> {
            return try {
                val i = value.intValue(exactRequired = true)
                tryFromImpl(i)
            } catch (_: ArithmeticException) {
                Result.failure(InlineIntOverflow())
            }
        }

        // Rust: fn try_from_impl<I>(i: I) -> Result<InlineInt, InlineIntOverflow>
        private inline fun <T> tryFromImpl(value: T): Result<InlineInt>
            where T : Number, T : Comparable<T> {
            val i = when (value) {
                is Int -> value
                is Long -> {
                    if (value < Int.MIN_VALUE || value > Int.MAX_VALUE) {
                        return Result.failure(InlineIntOverflow())
                    }
                    value.toInt()
                }
                is UInt -> {
                    if (value > Int.MAX_VALUE.toUInt()) {
                        return Result.failure(InlineIntOverflow())
                    }
                    value.toInt()
                }
                is ULong -> {
                    if (value > Int.MAX_VALUE.toULong()) {
                        return Result.failure(InlineIntOverflow())
                    }
                    value.toInt()
                }
                else -> return Result.failure(InlineIntOverflow())
            }

            @Suppress("KotlinConstantConditions")
            if (likely(i >= MIN.value && i <= MAX.value)) {
                return Result.success(InlineInt(i))
            } else {
                return Result.failure(InlineIntOverflow())
            }
        }
    }

    // --- Conversion methods ---

    /** Rust: fn to_i32(self) -> i32 */
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun toI32(): Int {
        return value
    }

    /** Rust: fn to_u64(self) -> Option<u64> */
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun toU64(): ULong? {
        return if (value >= 0) value.toULong() else null
    }

    /** Rust: fn to_u32(self) -> Option<u32> */
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun toU32(): UInt? {
        return if (value >= 0) value.toUInt() else null
    }

    /** Rust: fn to_f64(self) -> f64 */
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun toF64(): Double {
        return value.toDouble()
    }

    /** Rust: fn signum(self) -> i32 */
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun signum(): Int {
        return value.compareTo(0)
    }

    // --- Checked arithmetic ---

    /** Rust: fn checked_add(self, rhs: InlineInt) -> Option<InlineInt> */
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun checkedAdd(rhs: InlineInt): InlineInt? {
        val result = value.toLong() + rhs.value.toLong()
        if (result < Int.MIN_VALUE || result > Int.MAX_VALUE) {
            return null
        }
        return tryFrom(result.toInt()).getOrNull()
    }

    /** Rust: fn checked_sub(self, rhs: InlineInt) -> Option<InlineInt> */
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun checkedSub(rhs: InlineInt): InlineInt? {
        return checkedSubI32(rhs.value)
    }

    /** Rust: fn checked_sub_i32(self, rhs: i32) -> Option<InlineInt> */
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun checkedSubI32(rhs: Int): InlineInt? {
        val result = value.toLong() - rhs.toLong()
        if (result < Int.MIN_VALUE || result > Int.MAX_VALUE) {
            return null
        }
        return tryFrom(result.toInt()).getOrNull()
    }

    /** Rust: fn checked_neg(self) -> Option<InlineInt> */
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun checkedNeg(): InlineInt? {
        if (value == Int.MIN_VALUE) {
            return null
        }
        return tryFrom(-value).getOrNull()
    }

    /** Rust: fn checked_div(self, rhs: InlineInt) -> Option<InlineInt> */
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun checkedDiv(rhs: InlineInt): InlineInt? {
        if (rhs.value == 0) {
            return null
        }
        if (value == Int.MIN_VALUE && rhs.value == -1) {
            return null
        }
        val result = value / rhs.value
        return tryFrom(result).getOrNull()
    }

    /** Rust: fn checked_mul_i32(self, rhs: i32) -> Option<InlineInt> */
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun checkedMulI32(rhs: Int): InlineInt? {
        val result = value.toLong() * rhs.toLong()
        if (result < Int.MIN_VALUE || result > Int.MAX_VALUE) {
            return null
        }
        return tryFrom(result.toInt()).getOrNull()
    }

    /** Rust: fn checked_shr(self, rhs: u32) -> Option<InlineInt> */
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun checkedShr(rhs: UInt): InlineInt? {
        if (rhs >= 32u) {
            return null
        }
        val result = value shr rhs.toInt()
        return tryFrom(result).getOrNull()
    }

    /** Rust: fn checked_shl(self, rhs: u32) -> Option<InlineInt> */
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun checkedShl(rhs: UInt): InlineInt? {
        if (rhs >= 32u) {
            return null
        }
        val result = value shl rhs.toInt()
        if ((result shr rhs.toInt()) != value) {
            return null
        }
        return tryFrom(result).getOrNull()
    }

    // --- BigInt / abs ---

    /** Rust: fn to_bigint(self) -> BigInt */
    internal fun toBigInt(): BigInteger {
        return BigInteger.fromInt(value)
    }

    /** Rust: fn abs(self) -> StarlarkInt */
    internal fun abs(): StarlarkInt {
        if (value == Int.MIN_VALUE) {
            return StarlarkInt.from(toBigInt().abs())
        }
        return StarlarkInt.from(kotlin.math.abs(value))
    }

    // --- Bitwise operators (Rust: BitAnd, BitOr, BitXor, Not) ---

    infix fun and(other: InlineInt): InlineInt {
        return InlineInt(value and other.value)
    }

    infix fun or(other: InlineInt): InlineInt {
        return InlineInt(value or other.value)
    }

    infix fun xor(other: InlineInt): InlineInt {
        return InlineInt(value xor other.value)
    }

    operator fun not(): InlineInt {
        return InlineInt(value.inv())
    }

    // --- Rem (Rust: impl Rem for InlineInt) ---

    operator fun rem(other: InlineInt): InlineInt {
        return InlineInt(value % other.value)
    }

    // --- Comparison (Rust: PartialEq/PartialOrd with i32, Comparable<InlineInt>) ---

    override fun compareTo(other: InlineInt): Int {
        return value.compareTo(other.value)
    }

    operator fun compareTo(other: Int): Int {
        return value.compareTo(other)
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is InlineInt -> value == other.value
            is Int -> value == other
            else -> false
        }
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return value.toString()
    }
}

// Rust: impl PartialOrd<InlineInt> for i32
internal operator fun Int.compareTo(other: InlineInt): Int {
    return this.compareTo(other.toI32())
}

/** Rust: pub struct InlineIntOverflow */
internal class InlineIntOverflow : Exception("InlineInt overflow")
