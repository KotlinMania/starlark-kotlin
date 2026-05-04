// port-lint: source values/types/bigint.rs
package io.github.kotlinmania.starlark.values.types.bigint

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

/** Outside of [Int] range int. */

import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.kotlinmania.starlarkmap.StarlarkHasher
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
import io.github.kotlinmania.starlark.values.types.int.and
import io.github.kotlinmania.starlark.values.types.int.not
import io.github.kotlinmania.starlark.values.types.int.or
import io.github.kotlinmania.starlark.values.types.int.xor
import io.github.kotlinmania.starlark.values.types.float.allocValue
import io.github.kotlinmania.starlark.values.types.num.NumRef
import io.github.kotlinmania.starlark.values.types.num.NumTy
import io.github.kotlinmania.starlark.values.types.num.typecheckNumBinOp

/// `int` implementation for larger integers.
class StarlarkBigInt private constructor(
    /// `value` is strictly either smaller than `i32::MIN` or larger than `i32::MAX`.
    /// Many operation implementations depend on this fact.
    /// For example, `nonZeroInt << positiveBigInt` is considered to be overflow
    /// without checking the actual value of `positiveBigInt`.
    private val value: BigInteger,
) : Comparable<StarlarkBigInt>, StarlarkValue {

    companion object {
        internal fun uncheckedNew(value: BigInteger): StarlarkBigInt {
            return StarlarkBigInt(value)
        }

        internal fun cmpSmallBig(a: InlineInt, b: StarlarkBigInt): Int {
            val aSign = a.signum()
            val bSign = b.value.compareTo(BigInteger.ZERO) * 2
            return aSign.compareTo(bSign)
        }

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

    internal fun get(): BigInteger = value

    internal fun toF64(): Double {
        // `toF64` is infallible.
        return value.doubleValue(exactRequired = false)
    }

    internal fun toI32(): Int? {
        if (InlineInt.smallerThanI32()) {
            val v = try {
                value.intValue(exactRequired = true)
            } catch (_: ArithmeticException) {
                return null
            }
            return v
        } else {
            return null
        }
    }

    internal fun unpackLong(): Long? {
        return try {
            value.longValue(exactRequired = true)
        } catch (_: ArithmeticException) {
            null
        }
    }

    fun equalsI32(other: Int): Boolean {
        return false
    }

    fun serialize(): Any {
        // Always serialize as a number, prefer signed Long if it fits, otherwise unsigned Long.
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

    fun allocValue(heap: Heap): Value {
        return heap.allocSimple(this)
    }

    fun allocFrozenValue(heap: FrozenHeap): FrozenValue {
        return heap.allocSimple(this)
    }

    override val TYPE: String get() = INT_TYPE

    override fun toBool(): Boolean {
        // `StarlarkBigInt` is non-zero.
        return true
    }

    override fun minus(heap: Heap): Result<Value> {
        return Result.success(StarlarkInt.from(-value).allocValue(heap))
    }

    override fun plus(heap: Heap): Result<Value> {
        // This unnecessarily allocates, could return `self`.
        // But practically people rarely write `+NNN` except in constants,
        // and in constants we fold `+NNN` into `NNN`.
        return Result.success(StarlarkInt.from(value).allocValue(heap))
    }

    override fun equals(other: Value): Result<Boolean> {
        return Result.success(
            NumRef.Int(StarlarkIntRef.Big(this)) == other.unpackNum()
        )
    }

    override fun compare(other: Value): Result<Int> {
        val otherNum = other.unpackNum()
            ?: return ValueError.unsupportedWith(INT_TYPE, "compare", other)
        return Result.success(NumRef.Int(StarlarkIntRef.Big(this)).compareTo(otherNum))
    }

    override fun add(other: Value, heap: Heap): Result<Value>? {
        val otherNum = other.unpackNum() ?: return null
        return Result.success(heap.alloc(NumRef.Int(StarlarkIntRef.Big(this)) + otherNum))
    }

    override fun sub(other: Value, heap: Heap): Result<Value> {
        val otherNum = other.unpackNum()
            ?: return ValueError.unsupportedWith(INT_TYPE, "-", other)
        return Result.success(heap.alloc(NumRef.Int(StarlarkIntRef.Big(this)) - otherNum))
    }

    override fun mul(other: Value, heap: Heap): Result<Value>? {
        val otherNum = other.unpackNum() ?: return null
        return Result.success(heap.alloc(NumRef.Int(StarlarkIntRef.Big(this)) * otherNum))
    }

    override fun div(other: Value, heap: Heap): Result<Value> {
        val otherNum = other.unpackNum()
            ?: return ValueError.unsupportedWith(INT_TYPE, "/", other)
        return NumRef.Int(StarlarkIntRef.Big(this)).div(otherNum).map { it.allocValue(heap) }
    }

    override fun floorDiv(other: Value, heap: Heap): Result<Value> {
        val rhs = other.unpackNum()
            ?: return ValueError.unsupportedWith(INT_TYPE, "//", other)
        return NumRef.Int(StarlarkIntRef.Big(this)).floorDiv(rhs).map { heap.alloc(it) }
    }

    override fun percent(other: Value, heap: Heap): Result<Value> {
        val rhs = other.unpackNum()
            ?: return ValueError.unsupportedWith(INT_TYPE, "%", other)
        return NumRef.Int(StarlarkIntRef.Big(this)).percent(rhs).map { heap.alloc(it) }
    }

    override fun bitAnd(other: Value, heap: Heap): Result<Value> {
        val rhs = StarlarkIntRef.unpackValueOpt(other)
            ?: return ValueError.unsupportedWith(INT_TYPE, "&", other)
        return Result.success((StarlarkIntRef.Big(this) and rhs).allocValue(heap))
    }

    override fun bitXor(other: Value, heap: Heap): Result<Value> {
        val rhs = StarlarkIntRef.unpackValueOpt(other)
            ?: return ValueError.unsupportedWith(INT_TYPE, "^", other)
        return Result.success((StarlarkIntRef.Big(this) xor rhs).allocValue(heap))
    }

    override fun bitOr(other: Value, heap: Heap): Result<Value> {
        val rhs = StarlarkIntRef.unpackValueOpt(other)
            ?: return ValueError.unsupportedWith(INT_TYPE, "|", other)
        return Result.success((StarlarkIntRef.Big(this) or rhs).allocValue(heap))
    }

    override fun bitNot(heap: Heap): Result<Value> {
        return Result.success(StarlarkIntRef.Big(this).not().allocValue(heap))
    }

    override fun leftShift(other: Value, heap: Heap): Result<Value> {
        val rhs = StarlarkIntRef.unpackValueOpt(other)
            ?: return ValueError.unsupportedWith(INT_TYPE, "<<", other)
        return StarlarkIntRef.Big(this).leftShift(rhs).map { it.allocValue(heap) }
    }

    override fun rightShift(other: Value, heap: Heap): Result<Value> {
        val rhs = StarlarkIntRef.unpackValueOpt(other)
            ?: return ValueError.unsupportedWith(INT_TYPE, ">>", other)
        return StarlarkIntRef.Big(this).rightShift(rhs).map { it.allocValue(heap) }
    }

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

    override fun writeHash(hasher: StarlarkHasher): Result<Unit> {
        NumRef.Int(StarlarkIntRef.Big(this))
            .getHash64()
            .let { hasher.writeU64(it) }
        return Result.success(Unit)
    }

    override fun typecheckerTy(): Ty? = Ty.int()
}
