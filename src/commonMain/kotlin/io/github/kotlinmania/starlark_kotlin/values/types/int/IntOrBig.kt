// port-lint: source src/values/types/int/int_or_big.rs
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

import io.github.kotlinmania.starlark_kotlin.values.types.bigint.StarlarkBigInt
import io.github.kotlinmania.starlark_kotlin.values.layout.value
import io.github.kotlinmania.starlark_kotlin.values.owned.asRef
import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.Sign
import io.github.kotlinmania.starlark_kotlin.syntax.lexer.TokenInt
import io.github.kotlinmania.starlark_kotlin.values.types.string.BigInt
import io.github.kotlinmania.starlark_kotlin.values.types.tuple.it
import io.github.kotlinmania.starlark_kotlin.values.types.num.abs
import io.github.kotlinmania.starlark_kotlin.values.owned_frozen_ref.toOwned
import io.github.kotlinmania.starlark_kotlin.util.arc_or_static.clone
import io.github.kotlinmania.starlark_kotlin.any.downcastRef
import io.github.kotlinmania.starlark_kotlin.values.owned_frozen_ref.asRef

/**
 * Starlark integer error types.
 */
sealed class StarlarkIntError : Exception() {
    data class CannotRepresentAsExact(val value: Double) : StarlarkIntError() {
        override val message: String = "Float `$value` cannot be represented as exact integer"
    }

    data class FloorDivisionByZero(val a: StarlarkInt, val b: StarlarkInt) : StarlarkIntError() {
        override val message: String = "Floor division by zero: $a // $b"
    }

    data class ModuloByZero(val a: StarlarkInt, val b: StarlarkInt) : StarlarkIntError() {
        override val message: String = "Modulo by zero: $a % $b"
    }

    object LeftShiftOverflow : StarlarkIntError() {
        override val message: String = "Integer overflow computing left shift"
    }

    object LeftShiftNegative : StarlarkIntError() {
        override val message: String = "Negative left shift"
    }

    object RightShiftNegative : StarlarkIntError() {
        override val message: String = "Negative right shift"
    }
}

/**
 * Starlark integer that can be either a small inline value or a big integer.
 */
sealed class StarlarkInt {
    data class Small(val value: InlineInt) : StarlarkInt() {
        override fun toString(): String = value.toString()
    }

    data class Big(val value: StarlarkBigInt) : StarlarkInt() {
        override fun toString(): String = value.toString()
    }

    fun asRef(): StarlarkIntRef = when (this) {
        is Small -> StarlarkIntRef.Small(value)
        is Big -> StarlarkIntRef.Big(value)
    }

    operator fun unaryMinus(): StarlarkInt {
        // TODO(nga): can negate without allocating in most cases.
        return -asRef()
    }

    companion object {
        fun fromStrRadix(s: String, base: Int): Result<StarlarkInt> = runCatching {
            from(TokenInt.fromStrRadix(s, base).getOrThrow())
        }

        fun fromF64Exact(f: Double): Result<StarlarkInt> = runCatching {
            val i = InlineInt.tryFrom(f.toInt()).getOrElse { InlineInt.ZERO }
            if (i.toF64() == f) {
                Small(i)
            } else {
                val bi = BigInteger.tryFromDouble(f, exactRequired = true)
                if (bi != null && bi.doubleValue(exactRequired = false) == f) {
                    from(bi)
                } else {
                    throw StarlarkIntError.CannotRepresentAsExact(f)
                }
            }
        }

        @PublishedApi
        internal inline fun <I> fromImpl(value: I, tryInline: (I) -> InlineInt?, toBig: (I) -> BigInteger): StarlarkInt {
            val inlineValue = tryInline(value)
            return if (inlineValue != null) {
                Small(inlineValue)
            } else {
                Big(StarlarkBigInt.uncheckedNew(toBig(value)))
            }
        }

        fun from(value: Int): StarlarkInt = fromImpl(
            value,
            { InlineInt.tryFrom(it).getOrNull() },
            { BigInteger.fromInt(it) }
        )

        fun from(value: BigInteger): StarlarkInt {
            return when (val inline = InlineInt.tryFrom(value).getOrNull()) {
                null -> Big(StarlarkBigInt.uncheckedNew(value))
                else -> Small(inline)
            }
        }

        fun from(value: TokenInt): StarlarkInt = when (value) {
            is TokenInt.I32 -> from(value.value)
            is TokenInt.BigInt -> from(value.value)
        }

        fun from(value: UInt): StarlarkInt = fromImpl(
            value,
            { v -> InlineInt.tryFrom(v.toInt()).getOrNull()?.takeIf { it.toI32().toUInt() == v } },
            { BigInteger.fromUInt(it) }
        )

        fun from(value: Long): StarlarkInt = fromImpl(
            value,
            { InlineInt.tryFrom(it.toInt()).getOrNull()?.takeIf { inline -> inline.toI32().toLong() == it } },
            { BigInteger.fromLong(it) }
        )

        fun from(value: ULong): StarlarkInt = fromImpl(
            value,
            { v -> InlineInt.tryFrom(v.toInt()).getOrNull()?.takeIf { it.toI32().toULong() == v } },
            { BigInteger.fromULong(it) }
        )
    }
}

/**
 * Reference to a StarlarkInt that can be either a small inline value or a reference to a big integer.
 */
sealed class StarlarkIntRef {
    data class Small(val value: InlineInt) : StarlarkIntRef()
    data class Big(val value: StarlarkBigInt) : StarlarkIntRef()

    fun toOwned(): StarlarkInt = when (this) {
        is Small -> StarlarkInt.Small(value)
        is Big -> StarlarkInt.Big(value.clone())
    }

    fun toBig(): BigInteger = when (this) {
        is Small -> value.toBigInt()
        is Big -> value.get().copy()
    }

    fun toF64(): Double = when (this) {
        is Small -> value.toF64()
        is Big -> value.toF64()
    }

    fun toI32(): Int? = when (this) {
        is Small -> value.toI32()
        is Big -> value.toI32()
    }

    fun toU64(): ULong? = when (this) {
        is Small -> value.toU64()
        is Big -> value.get().ulongValue(exactRequired = false).takeIf {
            it >= 0.toULong() && BigInteger.fromULong(it) == value.get()
        }
    }

    private fun isNegative(): Boolean = when (this) {
        is Small -> value < 0
        is Big -> value.get().sign == Sign.NEGATIVE
    }

    private fun isZero(): Boolean = when (this) {
        is Small -> value == 0
        is Big -> false
    }

    private fun signumBig(b: BigInteger): Int = when (b.sign) {
        Sign.POSITIVE -> 1
        Sign.NEGATIVE -> -1
        Sign.ZERO -> 0
    }

    private fun floorDivSmallSmall(a: InlineInt, b: InlineInt): Result<StarlarkInt> = runCatching {
        if (b == 0) {
            throw StarlarkIntError.FloorDivisionByZero(StarlarkInt.Small(a), StarlarkInt.Small(b))
        }
        val sig = b.signum() * a.signum()
        val offset = if (sig < 0 && a % b != 0) 1 else 0
        when (val div = a.checkedDiv(b)) {
            null -> floorDivBigBig(a.toBigInt(), b.toBigInt()).getOrThrow()
            else -> {
                val result = div.checkedSubI32(offset).getOrElse {
                    throw Exception("unreachable")
                }
                StarlarkInt.Small(result)
            }
        }
    }

    private fun floorDivBigBig(a: BigInteger, b: BigInteger): Result<StarlarkInt> = runCatching {
        if (b.isZero()) {
            throw StarlarkIntError.FloorDivisionByZero(
                StarlarkInt.from(a.copy()),
                StarlarkInt.from(b.copy())
            )
        }
        val sig = signumBig(b) * signumBig(a)
        // TODO(nga): optimize.
        val offset = if (sig < 0 && !(a % b).isZero()) {
            1
        } else {
            0
        }
        StarlarkInt.from((a / b) - BigInteger.fromInt(offset))
    }

    /**
     * Floor division operator `//`.
     */
    fun floorDiv(other: StarlarkIntRef): Result<StarlarkInt> = when (this) {
        is Small -> when (other) {
            is Small -> floorDivSmallSmall(value, other.value)
            is Big -> floorDivBigBig(value.toBigInt(), other.value.get())
        }
        is Big -> when (other) {
            is Small -> floorDivBigBig(value.get(), other.value.toBigInt())
            is Big -> floorDivBigBig(value.get(), other.value.get())
        }
    }

    private fun percentSmall(a: InlineInt, b: InlineInt): Result<InlineInt> = runCatching {
        if (b == 0) {
            throw StarlarkIntError.ModuloByZero(StarlarkInt.Small(a), StarlarkInt.Small(b))
        }
        // In Rust `i32::min_value() % -1` is overflow, but we should eval it to zero.
        if (a == Int.MIN_VALUE && b == -1) {
            return@runCatching InlineInt.ZERO
        }
        val r = a % b
        if (r == 0) {
            InlineInt.ZERO
        } else {
            if (b.signum() != r.signum()) {
                r.checkedAdd(b).getOrElse {
                    throw Exception("unreachable")
                }
            } else {
                r
            }
        }
    }

    private fun percentBig(a: BigInteger, b: BigInteger): Result<StarlarkInt> = runCatching {
        if (b.isZero()) {
            throw StarlarkIntError.ModuloByZero(
                StarlarkInt.from(a.copy()),
                StarlarkInt.from(b.copy())
            )
        }
        val r = a % b
        if (r.isZero()) {
            StarlarkInt.Small(InlineInt.ZERO)
        } else {
            StarlarkInt.from(if (b.sign != r.sign) {
                r + b
            } else {
                r
            })
        }
    }

    /**
     * Modulo operator `%`.
     */
    fun percent(other: StarlarkIntRef): Result<StarlarkInt> = when (this) {
        is Small -> when (other) {
            is Small -> percentSmall(value, other.value).map { StarlarkInt.Small(it) }
            is Big -> percentBig(value.toBigInt(), other.value.get())
        }
        is Big -> when (other) {
            is Small -> percentBig(value.get(), other.value.toBigInt())
            is Big -> percentBig(value.get(), other.value.get())
        }
    }

    /**
     * Left shift operator `<<`.
     */
    fun leftShift(other: StarlarkIntRef): Result<StarlarkInt> = runCatching {
        // Handle the most common case first.
        if (this is Small && other is Small) {
            other.value.toU32()?.let { b ->
                value.checkedShl(b)?.let { r ->
                    return@runCatching StarlarkInt.Small(r)
                }
            }
        }

        if (other.isNegative()) {
            throw StarlarkIntError.LeftShiftNegative
        }
        if (this.isZero() || other.isZero()) {
            return@runCatching this.toOwned()
        }
        if (other > 100_000) {
            // Limit the size of the BigInt to avoid accidentally consuming
            // too much memory. 100_000 is practically enough for most use cases.
            throw StarlarkIntError.LeftShiftOverflow
        }

        when (other) {
            is Big -> throw StarlarkIntError.LeftShiftOverflow
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
    fun rightShift(other: StarlarkIntRef): Result<StarlarkInt> = runCatching {
        // Handle the most common case first.
        if (this is Small && other is Small) {
            other.value.toU32()?.let { b ->
                value.checkedShr(b)?.let { r ->
                    return@runCatching StarlarkInt.Small(r)
                }
            }
        }

        if (other.isNegative()) {
            throw StarlarkIntError.RightShiftNegative
        }
        if (this.isZero() || other.isZero()) {
            return@runCatching this.toOwned()
        }
        val otherU64 = other.toU64() ?: run {
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

    fun abs(): StarlarkInt = when (this) {
        is Small -> value.abs()
        is Big -> StarlarkInt.from(value.get().abs())
    }

    operator fun compareTo(other: StarlarkIntRef): Int = when (this) {
        is Small -> when (other) {
            is Small -> value.compareTo(other.value)
            is Big -> StarlarkBigInt.cmpSmallBig(value, other.value)
        }
        is Big -> when (other) {
            is Small -> StarlarkBigInt.cmpBigSmall(value, other.value)
            is Big -> value.compareTo(other.value)
        }
    }

    operator fun compareTo(other: Int): Int {
        // TODO(nga): this is inefficient if `i32` cannot fit in `InlineInt`.
        return this.compareTo(StarlarkInt.from(other).asRef())
    }

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is StarlarkIntRef -> false
        else -> when (this) {
            is Small -> when (other) {
                is Small -> value == other.value
                is Big -> false
            }
            is Big -> when (other) {
                is Small -> false
                is Big -> value == other.value
            }
        }
    }

    override fun hashCode(): Int = when (this) {
        is Small -> value.hashCode()
        is Big -> value.hashCode()
    }

    companion object {
        fun unpack(value: Value): StarlarkIntRef? {
            value.unpackInlineInt()?.let { return Small(it) }
            value.downcastRef<StarlarkBigInt>()?.let { return Big(it) }
            return null
        }
    }
}

// Bitwise operators for StarlarkIntRef
infix fun StarlarkIntRef.and(other: StarlarkIntRef): StarlarkInt = when (this) {
    is StarlarkIntRef.Small -> when (other) {
        is StarlarkIntRef.Small -> StarlarkInt.Small(value and other.value)
        is StarlarkIntRef.Big -> StarlarkInt.from(toBig() and other.toBig())
    }
    is StarlarkIntRef.Big -> StarlarkInt.from(toBig() and other.toBig())
}

infix fun StarlarkIntRef.or(other: StarlarkIntRef): StarlarkInt = when (this) {
    is StarlarkIntRef.Small -> when (other) {
        is StarlarkIntRef.Small -> StarlarkInt.Small(value or other.value)
        is StarlarkIntRef.Big -> StarlarkInt.from(toBig() or other.toBig())
    }
    is StarlarkIntRef.Big -> StarlarkInt.from(toBig() or other.toBig())
}

infix fun StarlarkIntRef.xor(other: StarlarkIntRef): StarlarkInt = when (this) {
    is StarlarkIntRef.Small -> when (other) {
        is StarlarkIntRef.Small -> StarlarkInt.Small(value xor other.value)
        is StarlarkIntRef.Big -> StarlarkInt.from(toBig() xor other.toBig())
    }
    is StarlarkIntRef.Big -> StarlarkInt.from(toBig() xor other.toBig())
}

operator fun StarlarkIntRef.not(): StarlarkInt = when (this) {
    is StarlarkIntRef.Small -> StarlarkInt.Small(!value)
    is StarlarkIntRef.Big -> StarlarkInt.from(!toBig())
}

operator fun StarlarkIntRef.unaryMinus(): StarlarkInt {
    if (this is StarlarkIntRef.Small) {
        value.checkedNeg()?.let { return StarlarkInt.Small(it) }
    }
    return StarlarkInt.from(-toBig())
}

operator fun StarlarkIntRef.plus(other: StarlarkIntRef): StarlarkInt {
    if (this is StarlarkIntRef.Small && other is StarlarkIntRef.Small) {
        value.checkedAdd(other.value)?.let { return StarlarkInt.Small(it) }
    }
    return StarlarkInt.from(toBig() + other.toBig())
}

operator fun StarlarkIntRef.minus(other: StarlarkIntRef): StarlarkInt {
    if (this is StarlarkIntRef.Small && other is StarlarkIntRef.Small) {
        value.checkedSub(other.value)?.let { return StarlarkInt.Small(it) }
    }
    return StarlarkInt.from(toBig() - other.toBig())
}

operator fun StarlarkIntRef.times(rhs: Int): StarlarkInt = when (this) {
    is StarlarkIntRef.Small -> {
        value.checkedMulI32(rhs)?.let { return StarlarkInt.Small(it) }
        StarlarkInt.from(value.toBigInt() * BigInteger.fromInt(rhs))
    }
    is StarlarkIntRef.Big -> StarlarkInt.from(value.get() * BigInteger.fromInt(rhs))
}

operator fun Int.times(rhs: StarlarkIntRef): StarlarkInt = rhs * this

operator fun StarlarkIntRef.times(other: StarlarkIntRef): StarlarkInt = when (this) {
    is StarlarkIntRef.Small -> value.toI32() * other
    is StarlarkIntRef.Big -> when (other) {
        is StarlarkIntRef.Small -> this * other.value.toI32()
        is StarlarkIntRef.Big -> StarlarkInt.from(value.get() * other.value.get())
    }
}

// Extension for Int comparison with StarlarkIntRef
operator fun Int.compareTo(other: StarlarkIntRef): Int {
    // TODO(nga): this is inefficient if `i32` cannot fit in `InlineInt`.
    return StarlarkInt.from(this).asRef().compareTo(other)
}

/**
 * Parse a StarlarkInt from a string.
 */
fun String.toStarlarkInt(): Result<StarlarkInt> = runCatching {
    // Not very efficient, but only used in tests.
    StarlarkInt.from(BigInteger.parseString(this, 10))
}
