// port-lint: source src/values/types/bigint.rs
package io.github.kotlinmania.starlark_kotlin.values.types.bigint

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
import com.ionspin.kotlin.bignum.integer.Sign
import io.github.kotlinmania.starlark_kotlin.analysis.dubious.Int
import io.github.kotlinmania.starlark_kotlin.analysis.dubious.NumRef
import io.github.kotlinmania.starlark_kotlin.collections.StarlarkHasher
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.typing.TyBasic
import io.github.kotlinmania.starlark_kotlin.typing.TypingBinOp
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.ValueError
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.types.allocSimple
import io.github.kotlinmania.starlark_kotlin.values.types.int.Big
import io.github.kotlinmania.starlark_kotlin.values.types.int.InlineInt
import io.github.kotlinmania.starlark_kotlin.values.types.int.StarlarkInt
import io.github.kotlinmania.starlark_kotlin.values.types.int.StarlarkIntRef
import io.github.kotlinmania.starlark_kotlin.values.types.num.NumTy
import io.github.kotlinmania.starlark_kotlin.values.types.num.typecheckNumBinOp
import io.github.kotlinmania.starlark_kotlin.values.types.string.unpackNum
import io.github.kotlinmania.starlark_kotlin.values.unpackValueOpt

/**
 * `int` implementation for larger integers.
 *
 * Rust: `struct StarlarkBigInt` with derives for Clone, Debug, Default, Display,
 * Ord, PartialOrd, Eq, PartialEq, Hash, Allocative.
 */
class StarlarkBigInt private constructor(
    /**
     * `value` is strictly either smaller than `Int.MIN_VALUE` or larger than `Int.MAX_VALUE`.
     * Many operation implementations depend on this fact.
     * For example, `non_zero_int << positive_big_int` is considered to be overflow
     * without checking the actual value of `positive_big_int`.
     */
    private val value: BigInteger,
) : Comparable<StarlarkBigInt> {

    companion object {
        /** Creates a [StarlarkBigInt] without range checking. Caller must ensure value is outside InlineInt range. */
        internal fun uncheckedNew(value: BigInteger): StarlarkBigInt {
            return StarlarkBigInt(value)
        }

        /**
         * Compares a small inline int against a big int.
         * Sign comparison is enough because [StarlarkBigInt] is out of range of `Int`.
         */
        internal fun cmpSmallBig(a: InlineInt, b: StarlarkBigInt): Int {
            val aSign = a.signum()
            val bSign = when (b.value.sign) {
                // Rust: Sign::Plus => 2, Sign::Minus => -2, Sign::NoSign => 0
                Sign.POSITIVE -> 2
                Sign.NEGATIVE -> -2
                Sign.ZERO -> 0
            }
            return aSign.compareTo(bSign)
        }

        /** Compares a big int against a small inline int. Reverse of [cmpSmallBig]. */
        internal fun cmpBigSmall(a: StarlarkBigInt, b: InlineInt): Int {
            return -cmpSmallBig(b, a)
        }
    }

    override fun compareTo(other: StarlarkBigInt): Int {
        return value.compareTo(other.value)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StarlarkBigInt) return false
        return value == other.value
    }

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = value.toString()

    /** Returns the underlying [BigInteger] value. */
    internal fun get(): BigInteger = value

    /** Converts to Double. Infallible (may lose precision for very large values). */
    internal fun toF64(): Double {
        return value.doubleValue(exactRequired = false)
    }

    /** Converts to Int if InlineInt range is smaller than i32. Returns null if out of range. */
    internal fun toI32(): Int? {
        return if (InlineInt.smallerThanI32()) {
            val v = try {
                value.intValue(exactRequired = true)
            } catch (_: ArithmeticException) {
                return null
            }
            v
        } else {
            null
        }
    }

    /**
     * Attempts to unpack as a Long.
     * Rust: generic `unpack_integer<I: TryFrom<&BigInt>>` -- here specialized to Long.
     */
    internal fun unpackLong(): Long? {
        return try {
            value.longValue(exactRequired = true)
        } catch (_: ArithmeticException) {
            null
        }
    }

    /**
     * A [StarlarkBigInt] is never equal to an Int, because big ints are outside Int range.
     * Rust: `impl PartialEq<i32> for StarlarkBigInt`
     */
    fun equalsI32(@Suppress("UNUSED_PARAMETER") other: Int): Boolean {
        return false
    }

    // Rust: impl Serialize -- serialization handled separately in Kotlin.

    /** Allocates this value on the given heap. Rust: `impl AllocValue for StarlarkBigInt` */
    fun allocValue(heap: Heap): Value {
        return heap.allocSimple(this)
    }

    /** Allocates this as a frozen value. Rust: `impl AllocFrozenValue for StarlarkBigInt` */
    fun allocFrozenValue(heap: FrozenHeap): FrozenValue {
        return heap.allocSimple(this)
    }

    // --- StarlarkValue implementation ---
    // Rust: #[starlark_value(type = "int")]
    // Rust: impl<'v> StarlarkValue<'v> for StarlarkBigInt

    /** StarlarkBigInt is always non-zero, so always truthy. */
    fun toBool(): Boolean = true

    /** Unary minus. Rust: `fn minus` */
    fun minus(heap: Heap): Result<Value> {
        return Result.success(heap.alloc(StarlarkInt.from(-value)))
    }

    /** Unary plus. Rust: `fn plus` */
    fun plus(heap: Heap): Result<Value> {
        return Result.success(heap.alloc(StarlarkInt.from(value)))
    }

    /** Equality check against another Starlark value. Rust: `fn equals` */
    fun equals(other: Value): Result<Boolean> {
        return Result.success(
            NumRef.Int(StarlarkIntRef.Big(this)) == other.unpackNum()
        )
    }

    /** Comparison against another Starlark value. Rust: `fn compare` */
    fun compare(other: Value): Result<Int> {
        val otherNum = other.unpackNum()
            ?: return ValueError.unsupportedWith(this, "compare", other)
        return Result.success(NumRef.Int(StarlarkIntRef.Big(this)).compareTo(otherNum))
    }

    /** Addition. Returns null if rhs is not numeric. Rust: `fn add` */
    fun add(rhs: Value, heap: Heap): Result<Value>? {
        val rhsNum = rhs.unpackNum() ?: return null
        return Result.success(heap.alloc(NumRef.Int(StarlarkIntRef.Big(this)) + rhsNum))
    }

    /** Subtraction. Rust: `fn sub` */
    fun sub(other: Value, heap: Heap): Result<Value> {
        val otherNum = other.unpackNum()
            ?: return ValueError.unsupportedWith(this, "-", other)
        return Result.success(heap.alloc(NumRef.Int(StarlarkIntRef.Big(this)) - otherNum))
    }

    /** Multiplication. Returns null if rhs is not numeric. Rust: `fn mul` */
    fun mul(other: Value, heap: Heap): Result<Value>? {
        val otherNum = other.unpackNum() ?: return null
        return Result.success(heap.alloc(NumRef.Int(StarlarkIntRef.Big(this)) * otherNum))
    }

    /** True division. Rust: `fn div` */
    fun div(other: Value, heap: Heap): Result<Value> {
        val otherNum = other.unpackNum()
            ?: return ValueError.unsupportedWith(this, "/", other)
        return Result.success(heap.alloc(NumRef.Int(StarlarkIntRef.Big(this)).div(otherNum)))
    }

    /** Floor division. Rust: `fn floor_div` */
    fun floorDiv(other: Value, heap: Heap): Result<Value> {
        val rhs = other.unpackNum()
            ?: return ValueError.unsupportedWith(this, "//", other)
        return Result.success(heap.alloc(NumRef.Int(StarlarkIntRef.Big(this)).floorDiv(rhs)))
    }

    /** Modulo. Rust: `fn percent` */
    fun percent(other: Value, heap: Heap): Result<Value> {
        val rhs = other.unpackNum()
            ?: return ValueError.unsupportedWith(this, "%", other)
        return Result.success(heap.alloc(NumRef.Int(StarlarkIntRef.Big(this)).percent(rhs)))
    }

    /** Bitwise AND. Rust: `fn bit_and` */
    fun bitAnd(other: Value, heap: Heap): Result<Value> {
        val rhs = StarlarkIntRef.unpackValueOpt(other)
            ?: return ValueError.unsupportedWith(this, "&", other)
        return Result.success(heap.alloc(StarlarkIntRef.Big(this) and rhs))
    }

    /** Bitwise XOR. Rust: `fn bit_xor` */
    fun bitXor(other: Value, heap: Heap): Result<Value> {
        val rhs = StarlarkIntRef.unpackValueOpt(other)
            ?: return ValueError.unsupportedWith(this, "^", other)
        return Result.success(heap.alloc(StarlarkIntRef.Big(this) xor rhs))
    }

    /** Bitwise OR. Rust: `fn bit_or` */
    fun bitOr(other: Value, heap: Heap): Result<Value> {
        val rhs = StarlarkIntRef.unpackValueOpt(other)
            ?: return ValueError.unsupportedWith(this, "|", other)
        return Result.success(heap.alloc(StarlarkIntRef.Big(this) or rhs))
    }

    /** Bitwise NOT. Rust: `fn bit_not` */
    fun bitNot(heap: Heap): Result<Value> {
        return Result.success(heap.alloc(StarlarkIntRef.Big(this).not()))
    }

    /** Left shift. Rust: `fn left_shift` */
    fun leftShift(other: Value, heap: Heap): Result<Value> {
        val rhs = StarlarkIntRef.unpackValueOpt(other)
            ?: return ValueError.unsupportedWith(this, "<<", other)
        return Result.success(heap.alloc(StarlarkIntRef.Big(this).leftShift(rhs)))
    }

    /** Right shift. Rust: `fn right_shift` */
    fun rightShift(other: Value, heap: Heap): Result<Value> {
        val rhs = StarlarkIntRef.unpackValueOpt(other)
            ?: return ValueError.unsupportedWith(this, ">>", other)
        return Result.success(heap.alloc(StarlarkIntRef.Big(this).rightShift(rhs)))
    }

    /** Type-checks a binary operation. Rust: `fn bin_op_ty` */
    fun binOpTy(op: TypingBinOp, rhs: TyBasic): Ty? {
        return typecheckNumBinOp(NumTy.Int, op, rhs)
    }

    /** Writes this value's hash. Rust: `fn write_hash` */
    fun writeHash(hasher: StarlarkHasher): Result<Unit> {
        NumRef.Int(StarlarkIntRef.Big(this))
            .getHash64()
            .let { hasher.write(it) }
        return Result.success(Unit)
    }

    /** Returns the typechecker type. Rust: `fn typechecker_ty` */
    fun typecheckerTy(): Ty? = Ty.int()
}

// Tests are in commonTest.
