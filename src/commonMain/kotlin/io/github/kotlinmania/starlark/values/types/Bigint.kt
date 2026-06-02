// port-lint: source src/values/types/bigint.rs
package io.github.kotlinmania.starlark.values.types.bigint

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
import io.github.kotlinmania.starlark.collections.StarlarkHasher
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.typing.TyBasic
import io.github.kotlinmania.starlark.typing.TypingBinOp
import io.github.kotlinmania.starlark.typing.oracle.TypingBinOp as OracleTypingBinOp
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.ValueError
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.types.int.INT_TYPE
import io.github.kotlinmania.starlark.values.types.int.InlineInt
import io.github.kotlinmania.starlark.values.types.int.StarlarkInt
import io.github.kotlinmania.starlark.values.types.int.StarlarkIntRef
import io.github.kotlinmania.starlark.values.types.int.allocValue
import io.github.kotlinmania.starlark.values.types.int.and
import io.github.kotlinmania.starlark.values.types.int.not
import io.github.kotlinmania.starlark.values.types.int.or
import io.github.kotlinmania.starlark.values.types.int.xor
import io.github.kotlinmania.starlark.values.types.float.allocValue
import io.github.kotlinmania.starlark.values.types.num.NumRef
import io.github.kotlinmania.starlark.values.types.num.NumTy
import io.github.kotlinmania.starlark.values.types.num.typecheckNumBinOp

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
) : Comparable<StarlarkBigInt>, StarlarkValue {

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
            // Rust: Sign::Plus => 2, Sign::Minus => -2, Sign::NoSign => 0
            // BigInteger.sign is internal in ionspin; use compareTo instead.
            val bSign = b.value.compareTo(BigInteger.ZERO) * 2
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

    // Rust: impl Serialize for StarlarkBigInt
    /**
     * Serializes this big int as a number.
     * Prefers Long (i64) if it fits, otherwise ULong (u64), otherwise string-based number.
     *
     * Rust: `fn serialize<S>(&self, serializer: S) -> Result<S::Ok, S::Error>`
     */
    fun serialize(): Any {
        // Always serialize as a number, prefer i64 if it fits, otherwise u64
        val longVal = try {
            value.longValue(exactRequired = true)
        } catch (_: ArithmeticException) {
            null
        }
        if (longVal != null) {
            return longVal
        }
        val ulongVal = try {
            value.ulongValue(exactRequired = true)
        } catch (_: ArithmeticException) {
            null
        }
        if (ulongVal != null) {
            return ulongVal
        }
        return value.toString()
    }

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

    override val TYPE: String get() = INT_TYPE
    override val HAS_equals: Boolean get() = true

    /** StarlarkBigInt is always non-zero, so always truthy. */
    override fun toBool(): Boolean = true

    /** Unary minus. Rust: `fn minus` */
    override fun minus(heap: Heap): Result<Value> {
        return Result.success(StarlarkInt.from(-value).allocValue(heap))
    }

    /** Unary plus. Rust: `fn plus` */
    override fun plus(heap: Heap): Result<Value> {
        return Result.success(StarlarkInt.from(value).allocValue(heap))
    }

    /** Equality check against another Starlark value. Rust: `fn equals` */
    override fun equals(other: Value): Result<Boolean> {
        return Result.success(
            NumRef.Int(StarlarkIntRef.Big(this)) == other.unpackNum()
        )
    }

    /** Comparison against another Starlark value. Rust: `fn compare` */
    override fun compare(other: Value): Result<Int> {
        val otherNum = other.unpackNum()
            ?: return ValueError.unsupportedWith(INT_TYPE, "compare", other)
        return Result.success(NumRef.Int(StarlarkIntRef.Big(this)).compareTo(otherNum))
    }

    /** Addition. Returns null if rhs is not numeric. Rust: `fn add` */
    override fun add(rhs: Value, heap: Heap): Result<Value>? {
        val rhsNum = rhs.unpackNum() ?: return null
        return Result.success(heap.alloc(NumRef.Int(StarlarkIntRef.Big(this)) + rhsNum))
    }

    /** Subtraction. Rust: `fn sub` */
    override fun sub(other: Value, heap: Heap): Result<Value> {
        val otherNum = other.unpackNum()
            ?: return ValueError.unsupportedWith(INT_TYPE, "-", other)
        return Result.success(heap.alloc(NumRef.Int(StarlarkIntRef.Big(this)) - otherNum))
    }

    /** Multiplication. Returns null if rhs is not numeric. Rust: `fn mul` */
    override fun mul(rhs: Value, heap: Heap): Result<Value>? {
        val rhsNum = rhs.unpackNum() ?: return null
        return Result.success(heap.alloc(NumRef.Int(StarlarkIntRef.Big(this)) * rhsNum))
    }

    /** True division. Rust: `fn div` */
    override fun div(other: Value, heap: Heap): Result<Value> {
        val otherNum = other.unpackNum()
            ?: return ValueError.unsupportedWith(INT_TYPE, "/", other)
        return NumRef.Int(StarlarkIntRef.Big(this)).div(otherNum).map { it.allocValue(heap) }
    }

    /** Floor division. Rust: `fn floor_div` */
    override fun floorDiv(other: Value, heap: Heap): Result<Value> {
        val rhs = other.unpackNum()
            ?: return ValueError.unsupportedWith(INT_TYPE, "//", other)
        return NumRef.Int(StarlarkIntRef.Big(this)).floorDiv(rhs).map { heap.alloc(it) }
    }

    /** Modulo. Rust: `fn percent` */
    override fun percent(other: Value, heap: Heap): Result<Value> {
        val rhs = other.unpackNum()
            ?: return ValueError.unsupportedWith(INT_TYPE, "%", other)
        return NumRef.Int(StarlarkIntRef.Big(this)).percent(rhs).map { heap.alloc(it) }
    }

    /** Bitwise AND. Rust: `fn bit_and` */
    override fun bitAnd(other: Value, heap: Heap): Result<Value> {
        val rhs = StarlarkIntRef.unpackValueOpt(other)
            ?: return ValueError.unsupportedWith(INT_TYPE, "&", other)
        return Result.success((StarlarkIntRef.Big(this) and rhs).allocValue(heap))
    }

    /** Bitwise XOR. Rust: `fn bit_xor` */
    override fun bitXor(other: Value, heap: Heap): Result<Value> {
        val rhs = StarlarkIntRef.unpackValueOpt(other)
            ?: return ValueError.unsupportedWith(INT_TYPE, "^", other)
        return Result.success((StarlarkIntRef.Big(this) xor rhs).allocValue(heap))
    }

    /** Bitwise OR. Rust: `fn bit_or` */
    override fun bitOr(other: Value, heap: Heap): Result<Value> {
        val rhs = StarlarkIntRef.unpackValueOpt(other)
            ?: return ValueError.unsupportedWith(INT_TYPE, "|", other)
        return Result.success((StarlarkIntRef.Big(this) or rhs).allocValue(heap))
    }

    /** Bitwise NOT. Rust: `fn bit_not` */
    override fun bitNot(heap: Heap): Result<Value> {
        return Result.success(StarlarkIntRef.Big(this).not().allocValue(heap))
    }

    /** Left shift. Rust: `fn left_shift` */
    override fun leftShift(other: Value, heap: Heap): Result<Value> {
        val rhs = StarlarkIntRef.unpackValueOpt(other)
            ?: return ValueError.unsupportedWith(INT_TYPE, "<<", other)
        return StarlarkIntRef.Big(this).leftShift(rhs).map { it.allocValue(heap) }
    }

    /** Right shift. Rust: `fn right_shift` */
    override fun rightShift(other: Value, heap: Heap): Result<Value> {
        val rhs = StarlarkIntRef.unpackValueOpt(other)
            ?: return ValueError.unsupportedWith(INT_TYPE, ">>", other)
        return StarlarkIntRef.Big(this).rightShift(rhs).map { it.allocValue(heap) }
    }

    /** Type-checks a binary operation. Rust: `fn bin_op_ty` */
    override fun binOpTy(op: TypingBinOp, rhs: TyBasic): Ty? {
        val oracleOp = when (op) {
            TypingBinOp.Add -> OracleTypingBinOp.ADD
            TypingBinOp.Sub -> OracleTypingBinOp.SUB
            TypingBinOp.Mul -> OracleTypingBinOp.MUL
            TypingBinOp.Div -> OracleTypingBinOp.DIV
            TypingBinOp.FloorDiv -> OracleTypingBinOp.FLOOR_DIV
            TypingBinOp.Percent -> OracleTypingBinOp.PERCENT
            TypingBinOp.BitAnd -> OracleTypingBinOp.BIT_AND
            TypingBinOp.BitOr -> OracleTypingBinOp.BIT_OR
            TypingBinOp.BitXor -> OracleTypingBinOp.BIT_XOR
            TypingBinOp.LeftShift -> OracleTypingBinOp.LEFT_SHIFT
            TypingBinOp.RightShift -> OracleTypingBinOp.RIGHT_SHIFT
            TypingBinOp.In -> OracleTypingBinOp.IN
            TypingBinOp.Less -> OracleTypingBinOp.LESS
        }
        return typecheckNumBinOp(NumTy.Int, oracleOp, rhs)
    }

    /** Writes this value's hash. Rust: `fn write_hash` */
    override fun writeHash(hasher: StarlarkHasher): Result<Unit> {
        NumRef.Int(StarlarkIntRef.Big(this))
            .getHash64()
            .let { hasher.writeU64(it) }
        return Result.success(Unit)
    }

    /** Returns the typechecker type. Rust: `fn typechecker_ty` */
    override fun typecheckerTy(): Ty? = Ty.int()
}

// #[cfg(test)] mod tests -- see BigintTest.kt in commonTest
