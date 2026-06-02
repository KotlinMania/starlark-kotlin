// port-lint: source src/values/types/int/inline_int.rs
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
import io.github.kotlinmania.starlark.likely
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.AllocFrozenValue
import io.github.kotlinmania.starlark.values.AllocValue
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark.values.UnpackValue
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import kotlin.ConsistentCopyVisibility

/** Integer which is stored inline in `RawPointer`. */
// Rust: #[derive(Clone, Copy, Dupe, derive_more::Display, Eq, PartialEq, Hash, Ord, PartialOrd, Serialize)]
// Rust: #[serde(transparent)]
// Rust: pub struct InlineInt(i32)
@ConsistentCopyVisibility
data class InlineInt internal constructor(
    private val value: Int,
) : Comparable<InlineInt> {
    // Rust: impl Debug for InlineInt
    // }
    override fun toString(): String = value.toString()

    companion object {
        // Rust: const fn min_max_for_bits(bits: usize) -> (i32, i32)
        internal fun minMaxForBits(bits: Int): Pair<Int, Int> {
            val max = ((1L shl (bits - 1)) - 1).toInt()
            val min = -max - 1
            return Pair(min, max)
        }

        /** Number of bits in the integer. */
        // Rust: #[cfg(target_pointer_width = "64")]
        // Kotlin Multiplatform targets 64-bit, so we use 32.
        internal const val BITS: Int = 32

        internal val ZERO: InlineInt = InlineInt(0)
        internal val MINUS_ONE: InlineInt = InlineInt(-1)
        internal val MIN: InlineInt = InlineInt(minMaxForBits(BITS).first)
        internal val MAX: InlineInt = InlineInt(minMaxForBits(BITS).second)

        /** This type does not contain full range of `i32`. */
        internal fun smallerThanI32(): Boolean = BITS < 32

        // Rust: fn new_unchecked(i: i32) -> InlineInt
        @Suppress("NOTHING_TO_INLINE")
        internal inline fun newUnchecked(i: Int): InlineInt = InlineInt(i)

        // Rust: fn testing_new(i: i32) -> InlineInt
        internal fun testingNew(i: Int): InlineInt = tryFrom(i).getOrThrow()

        // --- TryFrom impls ---

        // Rust: impl TryFrom<i32> for InlineInt
        internal fun tryFrom(value: Int): Result<InlineInt> = tryFromImpl(value)

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
        internal fun tryFrom(value: Long): Result<InlineInt> = tryFromImpl(value)

        // Rust: impl TryFrom<u64> for InlineInt
        internal fun tryFrom(value: ULong): Result<InlineInt> {
            if (value > Int.MAX_VALUE.toULong()) return Result.failure(InlineIntOverflow())
            return tryFromImpl(value.toInt())
        }

        // Rust: impl TryFrom<&BigInt> for InlineInt
        internal fun tryFrom(value: BigInteger): Result<InlineInt> =
            try {
                val i = value.intValue(exactRequired = true)
                tryFromImpl(i)
            } catch (_: ArithmeticException) {
                Result.failure(InlineIntOverflow())
            }

        // Rust: fn try_from_impl<I>(i: I) -> Result<InlineInt, InlineIntOverflow>
        private fun <T> tryFromImpl(value: T): Result<InlineInt>
            where T : Number, T : Comparable<T> {
            val i =
                when (value) {
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
    internal inline fun toI32(): Int = value

    /** Rust: fn to_u64(self) -> Option<u64> */
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun toU64(): ULong? = if (value >= 0) value.toULong() else null

    /** Rust: fn to_u32(self) -> Option<u32> */
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun toU32(): UInt? = if (value >= 0) value.toUInt() else null

    /** Rust: fn to_f64(self) -> f64 */
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun toF64(): Double = value.toDouble()

    /** Rust: fn signum(self) -> i32 */
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun signum(): Int = value.compareTo(0)

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
    internal inline fun checkedSub(rhs: InlineInt): InlineInt? = checkedSubI32(rhs.value)

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
    internal fun toBigInt(): BigInteger = BigInteger.fromInt(value)

    /** Rust: fn abs(self) -> StarlarkInt */
    internal fun abs(): StarlarkInt {
        // Rust: match self.0.checked_abs() {
        //     Some(i) => StarlarkInt::from(i),
        //     None => StarlarkInt::from(self.to_bigint().abs()),
        // }
        return if (value == Int.MIN_VALUE) {
            // checked_abs returns None for Int.MIN_VALUE
            StarlarkInt.from(toBigInt().abs())
        } else {
            StarlarkInt.from(kotlin.math.abs(value))
        }
    }

    // --- Bitwise operators (Rust: impl BitAnd, BitOr, BitXor, Not) ---

    // Rust: impl BitAnd for InlineInt
    infix fun and(other: InlineInt): InlineInt = InlineInt(value and other.value)

    // Rust: impl BitOr for InlineInt
    infix fun or(other: InlineInt): InlineInt = InlineInt(value or other.value)

    // Rust: impl BitXor for InlineInt
    infix fun xor(other: InlineInt): InlineInt = InlineInt(value xor other.value)

    // Rust: impl Not for InlineInt
    operator fun not(): InlineInt = InlineInt(value.inv())

    // --- Comparison (Rust: PartialEq/PartialOrd with i32, Comparable<InlineInt>) ---

    override fun compareTo(other: InlineInt): Int = value.compareTo(other.value)

    // Rust: impl PartialOrd<i32> for InlineInt
    operator fun compareTo(other: Int): Int = value.compareTo(other)

    // Rust: impl PartialEq<i32> for InlineInt
    // Value class equals/hashCode are derived from the underlying Int.
    // For explicit comparison with Int, use equalsInt.
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun equalsInt(other: Int): Boolean = value == other

    // --- Rem (Rust: impl Rem for InlineInt) ---

    operator fun rem(other: InlineInt): InlineInt = InlineInt(value % other.value)
}

// Rust: impl PartialEq<InlineInt> for i32
// Rust: impl PartialOrd<InlineInt> for i32
internal operator fun Int.compareTo(other: InlineInt): Int = this.compareTo(other.toI32())

/** Rust: pub struct InlineIntOverflow */
internal class InlineIntOverflow : Exception("InlineInt overflow")

// --- Trait impls that InlineInt cannot directly implement as a value class ---

/** Rust: impl StarlarkTypeRepr for InlineInt */
object InlineIntStarlarkTypeRepr : StarlarkTypeRepr {
    // Rust: fn starlark_type_repr() -> Ty { PointerI32::starlark_type_repr() }
    override fun starlarkTypeRepr(): Ty = Ty.int()
}

/** Rust: impl<'v> UnpackValue<'v> for InlineInt */
object InlineIntUnpackValue : UnpackValue<InlineInt> {
    // Rust: fn starlark_type_repr() -> Ty
    override fun starlarkTypeRepr(): Ty = InlineIntStarlarkTypeRepr.starlarkTypeRepr()

    // Rust: fn unpack_value_impl(value: Value<'v>) -> crate::Result<Option<Self>>
    override fun unpackValueImpl(value: Value): Result<InlineInt?> = Result.success(value.unpackInlineInt())
}

/** Rust: impl<'v> AllocValue<'v> for InlineInt */
object InlineIntAllocValue : AllocValue {
    // Rust: fn starlark_type_repr() -> Ty
    override fun starlarkTypeRepr(): Ty = InlineIntStarlarkTypeRepr.starlarkTypeRepr()

    // Rust: fn alloc_value(self, _heap: Heap<'v>) -> Value<'v> { Value::new_int(self) }
    override fun allocValue(heap: Heap): Value {
        error("Use InlineInt.allocValue(heap) extension instead")
    }
}

/** Extension to allocate an [InlineInt] as a [Value]. */
// Rust: impl<'v> AllocValue<'v> for InlineInt
internal fun InlineInt.allocValue(
    @Suppress("UNUSED_PARAMETER") heap: Heap,
): Value = Value.newInt(this)

/** Rust: impl AllocFrozenValue for InlineInt */
object InlineIntAllocFrozenValue : AllocFrozenValue {
    // Rust: fn starlark_type_repr() -> Ty
    override fun starlarkTypeRepr(): Ty = InlineIntStarlarkTypeRepr.starlarkTypeRepr()

    // Rust: fn alloc_frozen_value(self, _heap: &FrozenHeap) -> FrozenValue { FrozenValue::new_int(self) }
    override fun allocFrozenValue(heap: FrozenHeap): FrozenValue {
        error("Use InlineInt.allocFrozenValue(heap) extension instead")
    }
}

/** Extension to allocate an [InlineInt] as a [FrozenValue]. */
// Rust: impl AllocFrozenValue for InlineInt
internal fun InlineInt.allocFrozenValue(
    @Suppress("UNUSED_PARAMETER") heap: FrozenHeap,
): FrozenValue = FrozenValue.newInt(this)
