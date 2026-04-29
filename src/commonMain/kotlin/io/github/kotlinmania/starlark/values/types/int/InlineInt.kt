// port-lint: source values/types/int/inline_int.rs
package io.github.kotlinmania.starlark.values.types.int

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

import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.kotlinmania.starlark.likely
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.AllocFrozenValue
import io.github.kotlinmania.starlark.values.AllocValue
import io.github.kotlinmania.starlark.values.UnpackValue
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.Heap

/** Integer which is stored inline in `RawPointer`. */
@ConsistentCopyVisibility
data class InlineInt internal constructor(private val value: Int) :
    Comparable<InlineInt>,
    StarlarkTypeRepr,
    AllocValue,
    AllocFrozenValue {

    override fun toString(): String {
        return value.toString()
    }

    companion object {
        internal fun minMaxForBits(bits: Int): Pair<Int, Int> {
            val max = ((1L shl (bits - 1)) - 1).toInt()
            val min = -max - 1
            return Pair(min, max)
        }

        /** Number of bits in the integer. */
        // Kotlin Multiplatform targets 64-bit, so we import 32.
        internal const val BITS: Int = 32

        internal val ZERO: InlineInt = InlineInt(0)
        internal val MINUS_ONE: InlineInt = InlineInt(-1)
        internal val MIN: InlineInt = InlineInt(minMaxForBits(BITS).first)
        internal val MAX: InlineInt = InlineInt(minMaxForBits(BITS).second)

        /** This type does not contain full range of `i32`. */
        internal fun smallerThanI32(): Boolean {
            return BITS < 32
        }

        internal fun newUnchecked(i: Int): InlineInt {
            return InlineInt(i)
        }

        internal fun testingNew(i: Int): InlineInt {
            return tryFrom(i).getOrThrow()
        }

        // --- TryFrom impls ---

        internal fun tryFrom(value: Int): Result<InlineInt> {
            return tryFromImpl(value)
        }

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

        internal fun tryFrom(value: Long): Result<InlineInt> {
            return tryFromImpl(value)
        }

        internal fun tryFrom(value: ULong): Result<InlineInt> {
            if (value > Int.MAX_VALUE.toULong()) return Result.failure(InlineIntOverflow())
            return tryFromImpl(value.toInt())
        }

        internal fun tryFrom(value: BigInteger): Result<InlineInt> {
            return try {
                val i = value.intValue(exactRequired = true)
                tryFromImpl(i)
            } catch (_: ArithmeticException) {
                Result.failure(InlineIntOverflow())
            }
        }

        //     where i32: TryFrom<I>,
        private fun <T> tryFromImpl(value: T): Result<InlineInt>
            where T : Number, T : Comparable<T> {
            val i = when (value) {
                is Int -> value
                is Long -> {
                    if (value < Int.MIN_VALUE || value > Int.MAX_VALUE) {
                        return Result.failure(InlineIntOverflow())
                    }
                    value.toInt()
                }
                else -> return Result.failure(InlineIntOverflow())
            }

            // Only absurd for certain bit widths
            if (likely(i >= MIN.value && i <= MAX.value)) {
                return Result.success(InlineInt(i))
            } else {
                return Result.failure(InlineIntOverflow())
            }
        }
    }

    override fun starlarkTypeRepr(): Ty {
        return Ty.int()
    }

    override fun allocValue(_heap: Heap): Value {
        return Value.newInt(this)
    }

    override fun allocFrozenValue(_heap: FrozenHeap): FrozenValue {
        return FrozenValue.newInt(this)
    }

    // --- Conversion methods ---

    internal fun toI32(): Int {
        return value
    }

    internal fun toU64(): ULong? {
        return if (value >= 0) value.toULong() else null
    }

    internal fun toU32(): UInt? {
        return if (value >= 0) value.toUInt() else null
    }

    internal fun toF64(): Double {
        return value.toDouble()
    }

    internal fun signum(): Int {
        return value.compareTo(0)
    }

    // --- Checked arithmetic ---

    internal fun checkedAdd(rhs: InlineInt): InlineInt? {
        val result = value.toLong() + rhs.value.toLong()
        if (result < Int.MIN_VALUE || result > Int.MAX_VALUE) {
            return null
        }
        return tryFrom(result.toInt()).getOrNull()
    }

    internal fun checkedSub(rhs: InlineInt): InlineInt? {
        return checkedSubI32(rhs.value)
    }

    internal fun checkedSubI32(rhs: Int): InlineInt? {
        val result = value.toLong() - rhs.toLong()
        if (result < Int.MIN_VALUE || result > Int.MAX_VALUE) {
            return null
        }
        return tryFrom(result.toInt()).getOrNull()
    }

    internal fun checkedNeg(): InlineInt? {
        if (value == Int.MIN_VALUE) {
            return null
        }
        return tryFrom(-value).getOrNull()
    }

    internal fun checkedDiv(rhs: InlineInt): InlineInt? {
        if (rhs.value == 0) {
            return null
        }
        if (value == Int.MIN_VALUE && rhs.value == -1) {
            return null
        }
        val result = value / rhs.value
        return tryFrom(result).getOrNull()
    }

    internal fun checkedMulI32(rhs: Int): InlineInt? {
        val result = value.toLong() * rhs.toLong()
        if (result < Int.MIN_VALUE || result > Int.MAX_VALUE) {
            return null
        }
        return tryFrom(result.toInt()).getOrNull()
    }

    internal fun checkedShr(rhs: UInt): InlineInt? {
        if (rhs >= 32u) {
            return null
        }
        val result = value shr rhs.toInt()
        return tryFrom(result).getOrNull()
    }

    internal fun checkedShl(rhs: UInt): InlineInt? {
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

    internal fun toBigint(): BigInteger {
        return BigInteger.fromInt(value)
    }

    internal fun abs(): StarlarkInt {
        return if (value == Int.MIN_VALUE) {
            // abs(Int.MIN_VALUE) overflows; promote to BigInt.
            StarlarkInt.from(toBigint().abs())
        } else {
            StarlarkInt.from(kotlin.math.abs(value))
        }
    }


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


    override fun compareTo(other: InlineInt): Int {
        return value.compareTo(other.value)
    }

    operator fun compareTo(other: Int): Int {
        return value.compareTo(other)
    }

    // Value class equals/hashCode are derived from the underlying Int.
    // For explicit comparison with Int, import equalsInt.
    internal fun equalsInt(other: Int): Boolean {
        return value == other
    }


    operator fun rem(other: InlineInt): InlineInt {
        return InlineInt(value % other.value)
    }
}

internal operator fun Int.compareTo(other: InlineInt): Int {
    return this.compareTo(other.toI32())
}

internal class InlineIntOverflow : Exception("InlineInt overflow")

/**
 * Unpack values into [InlineInt].
 *
 */
internal object InlineIntUnpack : UnpackValue<InlineInt> {
    override fun starlarkTypeRepr(): Ty = Ty.int()

    override fun unpackValueImpl(value: Value): Result<InlineInt?> {
        return Result.success(value.unpackInlineInt())
    }
}
