// port-lint: source src/values/types/int/int_or_big.rs
package io.github.kotlinmania.starlark.values.types.int

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
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.types.bigint.StarlarkBigInt

/**
 * Starlark integer error types.
 */
sealed class StarlarkIntError : Exception() {
    data class CannotRepresentAsExact(
        val value: Double,
    ) : StarlarkIntError() {
        override val message: String = "Float `$value` cannot be represented as exact integer"
    }

    data class FloorDivisionByZero(
        val a: StarlarkInt,
        val b: StarlarkInt,
    ) : StarlarkIntError() {
        override val message: String = "Floor division by zero: $a // $b"
    }

    data class ModuloByZero(
        val a: StarlarkInt,
        val b: StarlarkInt,
    ) : StarlarkIntError() {
        override val message: String = "Modulo by zero: $a % $b"
    }

    class LeftShiftOverflow : StarlarkIntError() {
        override val message: String = "Integer overflow computing left shift"
    }

    class LeftShiftNegative : StarlarkIntError() {
        override val message: String = "Negative left shift"
    }

    class RightShiftNegative : StarlarkIntError() {
        override val message: String = "Negative right shift"
    }
}

/**
 * Starlark integer that can be either a small inline value or a big integer.
 */
sealed class StarlarkInt {
    data class Small(
        val value: InlineInt,
    ) : StarlarkInt() {
        override fun toString(): String = value.toString()
    }

    internal data class Big(
        val value: StarlarkBigInt,
    ) : StarlarkInt() {
        override fun toString(): String = value.toString()
    }

    fun asRef(): StarlarkIntRef =
        when (this) {
            is Small -> StarlarkIntRef.Small(value)
            is Big -> StarlarkIntRef.Big(value)
        }

    operator fun unaryMinus(): StarlarkInt {
        // TODO(nga): can negate without allocating in most cases.
        return -asRef()
    }

    companion object {
        fun fromStrRadix(s: String, base: Int): Result<StarlarkInt> =
            runCatching {
                // Rust: TokenInt::from_str_radix(s, base)?
                // Try parsing as i32 first, fall back to BigInteger.
                val i32 = s.toIntOrNull(base)
                if (i32 != null) {
                    from(i32)
                } else {
                    from(BigInteger.parseString(s, base))
                }
            }

        private fun exactBigIntegerFromDouble(f: Double): BigInteger {
            if (!f.isFinite()) {
                throw StarlarkIntError.CannotRepresentAsExact(f)
            }
            val bits = f.toRawBits()
            val sign = if ((bits shr 63) == 0L) 1 else -1
            var exponent = ((bits shr 52) and 0x7FFL).toInt()
            var significand = bits and 0xFFFFFFFFFFFFFL

            if (exponent == 0) {
                // Subnormal number
                exponent = 1 - 1023
            } else {
                // Normal number
                significand = significand or 0x10000000000000L // Add implicit leading 1
                exponent = exponent - 1023
            }

            exponent -= 52

            var bi = BigInteger.fromLong(significand)
            if (exponent >= 0) {
                bi = bi.shl(exponent)
            } else {
                val shift = -exponent
                // If it has fractional bits, we check if they are zero
                val divisor = BigInteger.fromInt(1).shl(shift)
                val remainder = bi % divisor
                if (remainder != BigInteger.ZERO) {
                    throw StarlarkIntError.CannotRepresentAsExact(f)
                }
                bi = bi.shr(shift)
            }
            return if (sign < 0) -bi else bi
        }

        fun fromF64Exact(f: Double): Result<StarlarkInt> =
            runCatching {
                if (f.isNaN() || f.isInfinite()) {
                    throw StarlarkIntError.CannotRepresentAsExact(f)
                }
                val i = InlineInt.tryFrom(f.toInt()).getOrElse { InlineInt.ZERO }
                if (i.toF64() == f) {
                    Small(i)
                } else {
                    if (f == 0.0) {
                        return@runCatching Small(InlineInt.ZERO)
                    }
                    val bits = f.toBits()
                    val sign = if ((bits ushr 63) == 0L) 1 else -1
                    val exponent = ((bits ushr 52) and 0x7FFL).toInt()
                    val mantissa = bits and 0xFFFFFFFFFFFFFL

                    val bi =
                        if (exponent == 0) {
                            throw StarlarkIntError.CannotRepresentAsExact(f)
                        } else {
                            val significand = mantissa or 0x10000000000000L
                            val unbiasedExponent = exponent - 1023
                            val shift = 52 - unbiasedExponent
                            if (shift <= 0) {
                                BigInteger.fromLong(significand).shl(-shift)
                            } else {
                                if (shift > 52) {
                                    throw StarlarkIntError.CannotRepresentAsExact(f)
                                }
                                val mask = (1L shl shift) - 1
                                if ((significand and mask) != 0L) {
                                    throw StarlarkIntError.CannotRepresentAsExact(f)
                                }
                                BigInteger.fromLong(significand ushr shift)
                            }
                        }
                    val signedBi = if (sign < 0) -bi else bi
                    from(signedBi)
                }
            }

        internal inline fun <I> fromImpl(value: I, tryInline: (I) -> InlineInt?, toBig: (I) -> BigInteger): StarlarkInt {
            val inlineValue = tryInline(value)
            return if (inlineValue != null) {
                Small(inlineValue)
            } else {
                Big(StarlarkBigInt.uncheckedNew(toBig(value)))
            }
        }

        fun from(value: Int): StarlarkInt =
            fromImpl(
                value,
                { InlineInt.tryFrom(it).getOrNull() },
                { BigInteger.fromInt(it) },
            )

        internal fun from(value: BigInteger): StarlarkInt =
            when (val inline = InlineInt.tryFrom(value).getOrNull()) {
                null -> Big(StarlarkBigInt.uncheckedNew(value))
                else -> Small(inline)
            }

        // Rust: impl From<TokenInt> for StarlarkInt
        // TokenInt is not yet fully ported; parsing is handled by fromStrRadix.

        fun from(value: UInt): StarlarkInt =
            fromImpl(
                value,
                { v -> InlineInt.tryFrom(v.toInt()).getOrNull()?.takeIf { it.toI32().toUInt() == v } },
                { BigInteger.fromUInt(it) },
            )

        fun from(value: Long): StarlarkInt =
            fromImpl(
                value,
                { InlineInt.tryFrom(it.toInt()).getOrNull()?.takeIf { inline -> inline.toI32().toLong() == it } },
                { BigInteger.fromLong(it) },
            )

        fun from(value: ULong): StarlarkInt =
            fromImpl(
                value,
                { v -> InlineInt.tryFrom(v.toInt()).getOrNull()?.takeIf { it.toI32().toULong() == v } },
                { BigInteger.fromULong(it) },
            )

        // Rust: impl From<TokenInt> for StarlarkInt
        internal fun from(value: io.github.kotlinmania.starlark.syntax.lexer.TokenInt): StarlarkInt =
            when (value) {
                is io.github.kotlinmania.starlark.syntax.lexer.TokenInt.I32 -> from(value.value)
                is io.github.kotlinmania.starlark.syntax.lexer.TokenInt.BigInt -> Big(value.value)
            }
    }
}

// Rust: impl AllocValue for StarlarkInt
internal fun StarlarkInt.allocValue(heap: Heap): Value =
    when (this) {
        is StarlarkInt.Small -> Value.newInt(value)
        is StarlarkInt.Big -> heap.allocSimple(value)
    }

// Rust: impl AllocFrozenValue for StarlarkInt
internal fun StarlarkInt.allocFrozenValue(heap: FrozenHeap): FrozenValue =
    when (this) {
        is StarlarkInt.Small -> FrozenValue.newInt(value)
        is StarlarkInt.Big -> heap.allocSimple(value)
    }

/**
 * Reference to a StarlarkInt that can be either a small inline value or a reference to a big integer.
 */
sealed class StarlarkIntRef {
    data class Small(
        val value: InlineInt,
    ) : StarlarkIntRef() {
        override fun toString(): String = value.toString()

        override fun equals(other: Any?): Boolean = super.equals(other)

        override fun hashCode(): Int = super.hashCode()
    }

    internal data class Big(
        val value: StarlarkBigInt,
    ) : StarlarkIntRef() {
        override fun toString(): String = value.toString()

        override fun equals(other: Any?): Boolean = super.equals(other)

        override fun hashCode(): Int = super.hashCode()
    }

    fun toOwned(): StarlarkInt =
        when (this) {
            is Small -> StarlarkInt.Small(value)
            is Big -> StarlarkInt.Big(value)
        }

    internal fun toBig(): BigInteger =
        when (this) {
            is Small -> value.toBigInt()
            is Big -> value.get()
        }

    fun toF64(): Double =
        when (this) {
            is Small -> value.toF64()
            is Big -> value.toF64()
        }

    fun toI32(): Int? =
        when (this) {
            is Small -> value.toI32()
            is Big -> value.toI32()
        }

    fun toU64(): ULong? =
        when (this) {
            is Small -> value.toU64()
            is Big ->
                value.get().ulongValue(exactRequired = false).takeIf {
                    it >= 0.toULong() && BigInteger.fromULong(it) == value.get()
                }
        }

    private fun isNegative(): Boolean =
        when (this) {
            is Small -> value < 0
            is Big -> value.get() < BigInteger.ZERO
        }

    private fun isZero(): Boolean =
        when (this) {
            is Small -> value == InlineInt.ZERO
            is Big -> false
        }

    private fun signumBig(b: BigInteger): Int {
        val cmp = b.compareTo(BigInteger.ZERO)
        return when {
            cmp > 0 -> 1
            cmp < 0 -> -1
            else -> 0
        }
    }

    private fun floorDivSmallSmall(a: InlineInt, b: InlineInt): Result<StarlarkInt> =
        runCatching {
            if (b == InlineInt.ZERO) {
                throw StarlarkIntError.FloorDivisionByZero(StarlarkInt.Small(a), StarlarkInt.Small(b))
            }
            val sig = b.signum() * a.signum()
            val offset = if (sig < 0 && (a % b) != InlineInt.ZERO) 1 else 0
            when (val div = a.checkedDiv(b)) {
                null -> floorDivBigBig(a.toBigInt(), b.toBigInt()).getOrThrow()
                else -> {
                    val result =
                        div.checkedSubI32(offset)
                            ?: throw Exception("unreachable")
                    StarlarkInt.Small(result)
                }
            }
        }

    private fun floorDivBigBig(a: BigInteger, b: BigInteger): Result<StarlarkInt> =
        runCatching {
            if (b == BigInteger.ZERO) {
                throw StarlarkIntError.FloorDivisionByZero(
                    StarlarkInt.from(a),
                    StarlarkInt.from(b),
                )
            }
            val sig = signumBig(b) * signumBig(a)
            val offset =
                if (sig < 0 && (a % b) != BigInteger.ZERO) {
                    1
                } else {
                    0
                }
            StarlarkInt.from((a / b) - BigInteger.fromInt(offset))
        }

    /**
     * Floor division operator `//`.
     */
    fun floorDiv(other: StarlarkIntRef): Result<StarlarkInt> =
        when (this) {
            is Small ->
                when (other) {
                    is Small -> floorDivSmallSmall(value, other.value)
                    is Big -> floorDivBigBig(value.toBigInt(), other.value.get())
                }
            is Big ->
                when (other) {
                    is Small -> floorDivBigBig(value.get(), other.value.toBigInt())
                    is Big -> floorDivBigBig(value.get(), other.value.get())
                }
        }

    private fun percentSmall(a: InlineInt, b: InlineInt): Result<InlineInt> =
        runCatching {
            if (b == InlineInt.ZERO) {
                throw StarlarkIntError.ModuloByZero(StarlarkInt.Small(a), StarlarkInt.Small(b))
            }
            // In Rust `i32::min_value() % -1` is overflow, but we should eval it to zero.
            if (a.toI32() == Int.MIN_VALUE && b.toI32() == -1) {
                return@runCatching InlineInt.ZERO
            }
            val r = a % b
            if (r == InlineInt.ZERO) {
                InlineInt.ZERO
            } else {
                if (b.signum() != r.signum()) {
                    r.checkedAdd(b)
                        ?: throw Exception("unreachable")
                } else {
                    r
                }
            }
        }

    private fun percentBig(a: BigInteger, b: BigInteger): Result<StarlarkInt> =
        runCatching {
            if (b == BigInteger.ZERO) {
                throw StarlarkIntError.ModuloByZero(
                    StarlarkInt.from(a),
                    StarlarkInt.from(b),
                )
            }
            // Compute truncated remainder (sign follows dividend), matching Rust's BigInt::rem.
            // kotlin-bignum's % operator uses a non-standard sign convention,
            // so we compute it from absolute values.
            val absR = a.abs() % b.abs()
            val r =
                if (absR == BigInteger.ZERO) {
                    BigInteger.ZERO
                } else if (signumBig(a) < 0) {
                    -absR
                } else {
                    absR
                }
            if (r == BigInteger.ZERO) {
                StarlarkInt.Small(InlineInt.ZERO)
            } else {
                // Convert from truncated (sign of dividend) to floored (sign of divisor)
                StarlarkInt.from(
                    if (signumBig(b) != signumBig(r)) {
                        r + b
                    } else {
                        r
                    },
                )
            }
        }

    /**
     * Modulo operator `%`.
     */
    fun percent(other: StarlarkIntRef): Result<StarlarkInt> =
        when (this) {
            is Small ->
                when (other) {
                    is Small -> percentSmall(value, other.value).map { StarlarkInt.Small(it) }
                    is Big -> percentBig(value.toBigInt(), other.value.get())
                }
            is Big ->
                when (other) {
                    is Small -> percentBig(value.get(), other.value.toBigInt())
                    is Big -> percentBig(value.get(), other.value.get())
                }
        }

    /**
     * Left shift operator `<<`.
     */
    fun leftShift(other: StarlarkIntRef): Result<StarlarkInt> =
        runCatching {
            // Handle the most common case first.
            if (this is Small && other is Small) {
                other.value.toU32()?.let { b ->
                    value.checkedShl(b)?.let { r ->
                        return@runCatching StarlarkInt.Small(r)
                    }
                }
            }

            if (other.isNegative()) {
                throw StarlarkIntError.LeftShiftNegative()
            }
            if (this.isZero() || other.isZero()) {
                return@runCatching this.toOwned()
            }
            if (other > 100_000) {
                // Limit the size of the BigInt to avoid accidentally consuming
                // too much memory. 100_000 is practically enough for most use cases.
                throw StarlarkIntError.LeftShiftOverflow()
            }

            when (other) {
                is Big -> throw StarlarkIntError.LeftShiftOverflow()
                is Small -> {
                    // No overflow, checked above.
                    val b = other.value.toU64()!!
                    StarlarkInt.from(this.toBig().shl(b.toInt()))
                }
            }
        }

    /**
     * Right shift operator `>>`.
     */
    fun rightShift(other: StarlarkIntRef): Result<StarlarkInt> =
        runCatching {
            // Handle the most common case first.
            if (this is Small && other is Small) {
                other.value.toU32()?.let { b ->
                    value.checkedShr(b)?.let { r ->
                        return@runCatching StarlarkInt.Small(r)
                    }
                }
            }

            if (other.isNegative()) {
                throw StarlarkIntError.RightShiftNegative()
            }
            if (this.isZero() || other.isZero()) {
                return@runCatching this.toOwned()
            }
            val otherU64 =
                other.toU64() ?: run {
                    return@runCatching if (this.isNegative()) {
                        StarlarkInt.Small(InlineInt.MINUS_ONE)
                    } else {
                        StarlarkInt.Small(InlineInt.ZERO)
                    }
                }

            when (this) {
                is Small -> {
                    if (value < 0) {
                        StarlarkInt.Small(InlineInt.MINUS_ONE)
                    } else {
                        StarlarkInt.Small(InlineInt.ZERO)
                    }
                }
                is Big -> StarlarkInt.from(value.get().shr(otherU64.toInt()))
            }
        }

    fun abs(): StarlarkInt =
        when (this) {
            is Small -> value.abs()
            is Big -> StarlarkInt.from(value.get().abs())
        }

    operator fun compareTo(other: StarlarkIntRef): Int =
        when (this) {
            is Small ->
                when (other) {
                    is Small -> value.compareTo(other.value)
                    is Big -> StarlarkBigInt.cmpSmallBig(value, other.value)
                }
            is Big ->
                when (other) {
                    is Small -> StarlarkBigInt.cmpBigSmall(value, other.value)
                    is Big -> value.compareTo(other.value)
                }
        }

    operator fun compareTo(other: Int): Int {
        // TODO(nga): this is inefficient if `i32` cannot fit in `InlineInt`.
        return this.compareTo(StarlarkInt.from(other).asRef())
    }

    override fun equals(other: Any?): Boolean =
        when {
            this === other -> true
            other !is StarlarkIntRef -> false
            else ->
                when (this) {
                    is Small ->
                        when (other) {
                            is Small -> value == other.value
                            is Big -> false
                        }
                    is Big ->
                        when (other) {
                            is Small -> false
                            is Big -> value == other.value
                        }
                }
        }

    override fun hashCode(): Int =
        when (this) {
            is Small -> value.hashCode()
            is Big -> value.hashCode()
        }

    companion object {
        fun unpack(value: Value): StarlarkIntRef? {
            value.unpackInlineInt()?.let { return Small(it) }
            // StarlarkBigInt doesn't implement StarlarkValue yet, so we can't use
            // downcastRef. Access the raw underlying ptr instead.
            val rawPtr = value.getRef().value.starlarkValue()
            if (rawPtr is StarlarkBigInt) return Big(rawPtr)
            return null
        }

        /** Alias for [unpack] matching the Rust `UnpackValue::unpack_value_opt` trait method. */
        fun unpackValueOpt(value: Value): StarlarkIntRef? = unpack(value)
    }
}

// Bitwise operators for StarlarkIntRef
internal infix fun StarlarkIntRef.and(other: StarlarkIntRef): StarlarkInt =
    when (this) {
        is StarlarkIntRef.Small ->
            when (other) {
                is StarlarkIntRef.Small -> StarlarkInt.Small(value and other.value)
                is StarlarkIntRef.Big -> StarlarkInt.from(toBig() and other.toBig())
            }
        is StarlarkIntRef.Big -> StarlarkInt.from(toBig() and other.toBig())
    }

internal infix fun StarlarkIntRef.or(other: StarlarkIntRef): StarlarkInt =
    when (this) {
        is StarlarkIntRef.Small ->
            when (other) {
                is StarlarkIntRef.Small -> StarlarkInt.Small(value or other.value)
                is StarlarkIntRef.Big -> StarlarkInt.from(toBig() or other.toBig())
            }
        is StarlarkIntRef.Big -> StarlarkInt.from(toBig() or other.toBig())
    }

internal infix fun StarlarkIntRef.xor(other: StarlarkIntRef): StarlarkInt =
    when (this) {
        is StarlarkIntRef.Small ->
            when (other) {
                is StarlarkIntRef.Small -> StarlarkInt.Small(value xor other.value)
                is StarlarkIntRef.Big -> StarlarkInt.from(toBig() xor other.toBig())
            }
        is StarlarkIntRef.Big -> StarlarkInt.from(toBig() xor other.toBig())
    }

internal operator fun StarlarkIntRef.not(): StarlarkInt =
    when (this) {
        is StarlarkIntRef.Small -> StarlarkInt.Small(!value)
        // kotlin-bignum's BigInteger.not() does not implement two's complement NOT correctly.
        // Two's complement: ~x = -(x + 1)
        is StarlarkIntRef.Big -> StarlarkInt.from(-(toBig() + BigInteger.ONE))
    }

internal operator fun StarlarkIntRef.unaryMinus(): StarlarkInt {
    if (this is StarlarkIntRef.Small) {
        value.checkedNeg()?.let { return StarlarkInt.Small(it) }
    }
    return StarlarkInt.from(-toBig())
}

internal operator fun StarlarkIntRef.plus(other: StarlarkIntRef): StarlarkInt {
    if (this is StarlarkIntRef.Small && other is StarlarkIntRef.Small) {
        value.checkedAdd(other.value)?.let { return StarlarkInt.Small(it) }
    }
    return StarlarkInt.from(toBig() + other.toBig())
}

internal operator fun StarlarkIntRef.minus(other: StarlarkIntRef): StarlarkInt {
    if (this is StarlarkIntRef.Small && other is StarlarkIntRef.Small) {
        value.checkedSub(other.value)?.let { return StarlarkInt.Small(it) }
    }
    return StarlarkInt.from(toBig() - other.toBig())
}

internal operator fun StarlarkIntRef.times(rhs: Int): StarlarkInt =
    when (this) {
        is StarlarkIntRef.Small -> {
            value.checkedMulI32(rhs)?.let { return StarlarkInt.Small(it) }
            StarlarkInt.from(value.toBigInt() * BigInteger.fromInt(rhs))
        }
        is StarlarkIntRef.Big -> StarlarkInt.from(value.get() * BigInteger.fromInt(rhs))
    }

internal operator fun Int.times(rhs: StarlarkIntRef): StarlarkInt = rhs * this

internal operator fun StarlarkIntRef.times(other: StarlarkIntRef): StarlarkInt =
    when (this) {
        is StarlarkIntRef.Small -> value.toI32() * other
        is StarlarkIntRef.Big ->
            when (other) {
                is StarlarkIntRef.Small -> this * other.value.toI32()
                is StarlarkIntRef.Big -> StarlarkInt.from(value.get() * other.value.get())
            }
    }

// Extension for Int comparison with StarlarkIntRef
internal operator fun Int.compareTo(other: StarlarkIntRef): Int {
    // TODO(nga): this is inefficient if `i32` cannot fit in `InlineInt`.
    return StarlarkInt.from(this).asRef().compareTo(other)
}

/**
 * Parse a StarlarkInt from a string.
 */
internal fun String.toStarlarkInt(): Result<StarlarkInt> =
    runCatching {
        // Not very efficient, but only used in tests.
        StarlarkInt.from(BigInteger.parseString(this, 10))
    }
