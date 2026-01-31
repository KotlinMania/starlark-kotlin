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

import io.github.kotlinmania.starlark_kotlin.likely
import kotlin.jvm.JvmInline

/**
 * Integer which is stored inline in `RawPointer`.
 *
 * This is a value class wrapper around [Int] (i32 in Rust) that enforces
 * bit width constraints based on platform pointer size. The range of valid
 * values depends on the target platform's pointer width.
 */
@JvmInline
internal value class InlineInt private constructor(private val value: Int) : Comparable<InlineInt> {

    companion object {
        private fun minMaxForBits(bits: Int): Pair<Int, Int> {
            val max = ((1L shl (bits - 1)) - 1).toInt()
            val min = -max - 1
            return Pair(min, max)
        }

        /**
         * Number of bits in the integer.
         */
        // In Kotlin Multiplatform, we use 32 bits for now.
        // The Rust code uses conditional compilation based on target_pointer_width:
        // - 64-bit platforms use 32 bits
        // - 32-bit platforms use 29 bits
        // For semantic parity, this should be adjusted based on platform when needed.
        internal const val BITS: Int = 32

        internal val ZERO: InlineInt = InlineInt(0)
        internal val MINUS_ONE: InlineInt = InlineInt(-1)
        internal val MIN: InlineInt = InlineInt(minMaxForBits(BITS).first)
        internal val MAX: InlineInt = InlineInt(minMaxForBits(BITS).second)

        /**
         * This type does not contain full range of `i32`.
         */
        internal fun smallerThanI32(): Boolean {
            return BITS < 32
        }

        @Suppress("NOTHING_TO_INLINE")
        internal inline fun newUnchecked(i: Int): InlineInt {
            return InlineInt(i)
        }

        internal fun testingNew(i: Int): InlineInt {
            return tryFrom(i).getOrThrow()
        }

        internal fun tryFrom(value: Int): Result<InlineInt> {
            return tryFromImpl(value)
        }

        internal fun tryFrom(value: UInt): Result<InlineInt> {
            val i = value.toInt()
            if (i < 0) return Result.failure(InlineIntOverflow())
            return tryFromImpl(i).flatMap { result ->
                if (result.toI32() == i) {
                    Result.success(result)
                } else {
                    Result.failure(InlineIntOverflow())
                }
            }
        }

        internal fun tryFrom(value: Long): Result<InlineInt> {
            return tryFromImpl(value)
        }

        internal fun tryFrom(value: ULong): Result<InlineInt> {
            return tryFromImpl(value)
        }

        // Kotlin doesn't have USize/ISize as platform-specific types
        // We use Long as the closest equivalent

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

            // Only absurd for certain bit widths
            @Suppress("KotlinConstantConditions")
            if (likely(i >= MIN.value && i <= MAX.value)) {
                return Result.success(InlineInt(i))
            } else {
                return Result.failure(InlineIntOverflow())
            }
        }

        internal fun tryFromBigInt(value: java.math.BigInteger): Result<InlineInt> {
            // Try to convert BigInt to Int first
            if (value < Int.MIN_VALUE.toBigInteger() || value > Int.MAX_VALUE.toBigInteger()) {
                return Result.failure(InlineIntOverflow())
            }
            return tryFromImpl(value.toInt())
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun toI32(): Int {
        return value
    }

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun toU64(): ULong? {
        return if (value >= 0) value.toULong() else null
    }

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun toU32(): UInt? {
        return if (value >= 0) value.toUInt() else null
    }

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun toF64(): Double {
        return value.toDouble()
    }

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun signum(): Int {
        return value.compareTo(0)
    }

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun checkedAdd(rhs: InlineInt): InlineInt? {
        val result = value.toLong() + rhs.value.toLong()
        if (result < Int.MIN_VALUE || result > Int.MAX_VALUE) {
            return null
        }
        return tryFrom(result.toInt()).getOrNull()
    }

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun checkedSub(rhs: InlineInt): InlineInt? {
        return checkedSubI32(rhs.value)
    }

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun checkedSubI32(rhs: Int): InlineInt? {
        val result = value.toLong() - rhs.toLong()
        if (result < Int.MIN_VALUE || result > Int.MAX_VALUE) {
            return null
        }
        return tryFrom(result.toInt()).getOrNull()
    }

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun checkedNeg(): InlineInt? {
        if (value == Int.MIN_VALUE) {
            return null
        }
        return tryFrom(-value).getOrNull()
    }

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

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun checkedMulI32(rhs: Int): InlineInt? {
        val result = value.toLong() * rhs.toLong()
        if (result < Int.MIN_VALUE || result > Int.MAX_VALUE) {
            return null
        }
        return tryFrom(result.toInt()).getOrNull()
    }

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun checkedShr(rhs: UInt): InlineInt? {
        if (rhs >= 32u) {
            return null
        }
        val result = value shr rhs.toInt()
        return tryFrom(result).getOrNull()
    }

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun checkedShl(rhs: UInt): InlineInt? {
        if (rhs >= 32u) {
            return null
        }
        val result = value shl rhs.toInt()
        // Check for overflow: if the sign changed or bits were lost
        if ((value >= 0 && result < 0) || (value < 0 && result >= 0)) {
            return null
        }
        // Also check if the shift back doesn't restore the original
        if ((result shr rhs.toInt()) != value) {
            return null
        }
        return tryFrom(result).getOrNull()
    }

    internal fun toBigInt(): java.math.BigInteger {
        return value.toBigInteger()
    }

    internal fun abs(): StarlarkInt {
        if (value == Int.MIN_VALUE) {
            // Int.MIN_VALUE.absoluteValue overflows, need to use BigInt
            return StarlarkInt.fromBigInt(toBigInt().abs())
        }
        val abs = kotlin.math.abs(value)
        return StarlarkInt.fromI32(abs)
    }

    // Bitwise operators
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

    // Remainder operator
    operator fun rem(other: InlineInt): InlineInt {
        return InlineInt(value % other.value)
    }

    // Comparison operators
    override fun compareTo(other: InlineInt): Int {
        return value.compareTo(other.value)
    }

    operator fun compareTo(other: Int): Int {
        return value.compareTo(other)
    }

    // Equality with Int
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

// Extension functions for Int to support comparison with InlineInt
internal operator fun Int.compareTo(other: InlineInt): Int {
    return this.compareTo(other.toI32())
}

/**
 * Error type for InlineInt overflow.
 */
internal class InlineIntOverflow : Exception("InlineInt overflow")

// Placeholder types - these will be replaced when the actual implementations are ported
// from their respective source files

/**
 * Placeholder for StarlarkInt until int_or_big.rs is ported.
 */
internal class StarlarkInt private constructor() {
    companion object {
        fun fromI32(value: Int): StarlarkInt = StarlarkInt()
        fun fromBigInt(value: java.math.BigInteger): StarlarkInt = StarlarkInt()
    }
}

/**
 * Placeholder for Ty until typing/ty.rs is ported.
 */
internal class Ty private constructor()

/**
 * Placeholder for PointerI32 until pointer_i32.rs is ported.
 */
internal object PointerI32 {
    fun starlarkTypeRepr(): Ty = Ty()
}

/**
 * Placeholder for StarlarkTypeRepr until values/type_repr.rs is ported.
 */
internal interface StarlarkTypeRepr {
    val canonical: StarlarkTypeRepr
    fun starlarkTypeRepr(): Ty
}

/**
 * StarlarkTypeRepr implementation for InlineInt.
 */
internal object InlineIntStarlarkTypeRepr : StarlarkTypeRepr {
    override val canonical: StarlarkTypeRepr
        get() = StarlarkIntStarlarkTypeRepr.canonical

    override fun starlarkTypeRepr(): Ty {
        return PointerI32.starlarkTypeRepr()
    }
}

/**
 * Helper object for StarlarkInt's StarlarkTypeRepr.
 */
private object StarlarkIntStarlarkTypeRepr : StarlarkTypeRepr {
    override val canonical: StarlarkTypeRepr
        get() = this

    override fun starlarkTypeRepr(): Ty {
        return PointerI32.starlarkTypeRepr()
    }
}

/**
 * Placeholder for UnpackValue until it's ported.
 */
internal interface UnpackValue<T> {
    fun unpackValueImpl(value: Value<*>): Result<T?>
}

/**
 * UnpackValue implementation for InlineInt.
 */
internal object InlineIntUnpackValue : UnpackValue<InlineInt> {
    override fun unpackValueImpl(value: Value<*>): Result<InlineInt?> {
        // TODO: return error on too big integer.
        return Result.success(value.unpackInt())
    }
}

/**
 * Placeholder for Value until it's ported.
 */
internal class Value<T> private constructor() {
    fun unpackInt(): InlineInt? = null

    companion object {
        fun newInt(i: InlineInt): Value<*> = Value<Any>()
    }
}

/**
 * Placeholder for FrozenValue until it's ported.
 */
internal class FrozenValue private constructor() {
    companion object {
        fun newInt(i: InlineInt): FrozenValue = FrozenValue()
    }
}

/**
 * Placeholder for Heap until it's ported.
 */
internal class Heap<T> private constructor()

/**
 * Placeholder for FrozenHeap until it's ported.
 */
internal class FrozenHeap private constructor()

/**
 * Placeholder for AllocValue until it's ported.
 */
internal interface AllocValue<T> {
    fun allocValue(heap: Heap<T>): Value<T>
}

/**
 * InlineInt implementation of AllocValue.
 */
internal object InlineIntAllocValue : AllocValue<Any> {
    override fun allocValue(heap: Heap<Any>): Value<Any> {
        // This will be properly implemented when Value is ported
        return Value.newInt(InlineInt.ZERO)
    }
}

/**
 * Placeholder for AllocFrozenValue until it's ported.
 */
internal interface AllocFrozenValue {
    fun allocFrozenValue(heap: FrozenHeap): FrozenValue
}

/**
 * InlineInt implementation of AllocFrozenValue.
 */
internal object InlineIntAllocFrozenValue : AllocFrozenValue {
    override fun allocFrozenValue(heap: FrozenHeap): FrozenValue {
        return FrozenValue.newInt(InlineInt.ZERO)
    }
}
