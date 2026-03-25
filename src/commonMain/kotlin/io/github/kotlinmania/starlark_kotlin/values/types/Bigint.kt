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

// mod convert;

import io.github.kotlinmania.starlark_kotlin.collections.StarlarkHasher
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.typing.TyBasic
import io.github.kotlinmania.starlark_kotlin.typing.TypingBinOp
import io.github.kotlinmania.starlark_kotlin.values.AllocFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.AllocValue
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.ValueError
import io.github.kotlinmania.starlark_kotlin.values.types.int.InlineInt
import io.github.kotlinmania.starlark_kotlin.values.types.int.StarlarkInt
import io.github.kotlinmania.starlark_kotlin.values.types.int.StarlarkIntRef
import io.github.kotlinmania.starlark_kotlin.values.types.num.NumTy
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.unpackValueOpt
import io.github.kotlinmania.starlark_kotlin.values.types.string.unpackNum
import io.github.kotlinmania.starlark_kotlin.values.types.int.Big
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.kotlinmania.starlark_kotlin.values.types.num.typecheckNumBinOp
import io.github.kotlinmania.starlark_kotlin.values.types.allocSimple
import io.github.kotlinmania.starlark_kotlin.analysis.dubious.Int
import io.github.kotlinmania.starlark_kotlin.analysis.dubious.NumRef

/// `int` implementation for larger integers.
// #[derive(Clone, Debug, Default, Display, ProvidesStaticType, Ord, PartialOrd, Eq, PartialEq, Hash, Allocative)]
// #[display("{}", value)]
// pub struct StarlarkBigInt { value: BigInt }
class StarlarkBigInt private constructor(
    /// `value` is strictly either smaller than `i32::MIN` or larger than `i32::MAX`.
    /// Many operation implementations depend on this fact.
    /// For example, `non_zero_int << positive_big_int` is considered to be overflow
    /// without checking the actual value of `positive_big_int`.
    // value: BigInt
    private val value: BigInteger,
) : Comparable<StarlarkBigInt> {

    // impl StarlarkBigInt

    companion object {
        // pub(crate) fn unchecked_new(value: BigInt) -> Self
        internal fun uncheckedNew(value: BigInteger): StarlarkBigInt {
            return StarlarkBigInt(value)
        }

        // pub(crate) fn cmp_small_big(a: InlineInt, b: &StarlarkBigInt) -> Ordering
        internal fun cmpSmallBig(a: InlineInt, b: StarlarkBigInt): Int {
            val aSign = a.signum()
            val bSign = when (b.value.sign) {
                Sign.POSITIVE -> 2
                Sign.NEGATIVE -> -2
                Sign.ZERO -> 0
            }
            // Sign comparison is enough because `StarlarkBigInt` is out of range of `i32`.
            return aSign.compareTo(bSign)
        }

        // pub(crate) fn cmp_big_small(a: &StarlarkBigInt, b: InlineInt) -> Ordering
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

    // pub(crate) fn get(&self) -> &BigInt
    internal fun get(): BigInteger = value

    // pub(crate) fn to_f64(&self) -> f64
    internal fun toF64(): Double {
        // `to_f64` is infallible.
        return value.doubleValue(exactRequired = false)
    }

    // pub(crate) fn to_i32(&self) -> Option<i32>
    internal fun toI32(): Int? {
        // Avoid calling `to_i32` if the value is known to be out of range.
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

    // pub(crate) fn unpack_integer<'v, I: TryFrom<&'v BigInt>>(&'v self) -> Option<I>
    internal fun unpackLong(): Long? {
        return try {
            value.longValue(exactRequired = true)
        } catch (_: ArithmeticException) {
            null
        }
    }

    // impl PartialEq<i32> for StarlarkBigInt
    // fn eq(&self, _other: &i32) -> bool
    fun equalsI32(@Suppress("UNUSED_PARAMETER") other: Int): Boolean {
        return false
    }

    // impl Serialize for StarlarkBigInt
    // Kotlin: Serialization handled separately.

    // impl<'v> AllocValue<'v> for StarlarkBigInt
    // fn alloc_value(self, heap: Heap<'v>) -> Value<'v>
    fun allocValue(heap: Heap): Value {
        return heap.allocSimple(this)
    }

    // impl AllocFrozenValue for StarlarkBigInt
    // fn alloc_frozen_value(self, heap: &FrozenHeap) -> FrozenValue
    fun allocFrozenValue(heap: FrozenHeap): FrozenValue {
        return heap.allocSimple(this)
    }

    // #[starlark_value(type = "int")]
    // impl<'v> StarlarkValue<'v> for StarlarkBigInt

    // fn to_bool(&self) -> bool
    /** `StarlarkBigInt` is non-zero. */
    fun toBool(): Boolean = true

    // fn minus(&self, heap: Heap<'v>) -> starlark::Result<Value<'v>>
    fun minus(heap: Heap): Result<Value> {
        return Result.success(heap.alloc(StarlarkInt.from(-value)))
    }

    // fn plus(&self, heap: Heap<'v>) -> starlark::Result<Value<'v>>
    fun plus(heap: Heap): Result<Value> {
        return Result.success(heap.alloc(StarlarkInt.from(value)))
    }

    // fn equals(&self, other: Value<'v>) -> crate::Result<bool>
    fun equals(other: Value): Result<Boolean> {
        return Result.success(
            NumRef.Int(StarlarkIntRef.Big(this)) == other.unpackNum()
        )
    }

    // fn compare(&self, other: Value<'v>) -> crate::Result<Ordering>
    fun compare(other: Value): Result<Int> {
        val otherNum = other.unpackNum()
            ?: return ValueError.unsupportedWith(this, "compare", other)
        return Result.success(NumRef.Int(StarlarkIntRef.Big(this)).compareTo(otherNum))
    }

    // fn add(&self, rhs: Value<'v>, heap: Heap<'v>) -> Option<crate::Result<Value<'v>>>
    fun add(rhs: Value, heap: Heap): Result<Value>? {
        val rhsNum = rhs.unpackNum() ?: return null
        return Result.success(heap.alloc(NumRef.Int(StarlarkIntRef.Big(this)) + rhsNum))
    }

    // fn sub(&self, other: Value<'v>, heap: Heap<'v>) -> crate::Result<Value<'v>>
    fun sub(other: Value, heap: Heap): Result<Value> {
        val otherNum = other.unpackNum()
            ?: return ValueError.unsupportedWith(this, "-", other)
        return Result.success(heap.alloc(NumRef.Int(StarlarkIntRef.Big(this)) - otherNum))
    }

    // fn mul(&self, other: Value<'v>, heap: Heap<'v>) -> Option<crate::Result<Value<'v>>>
    fun mul(other: Value, heap: Heap): Result<Value>? {
        val otherNum = other.unpackNum() ?: return null
        return Result.success(heap.alloc(NumRef.Int(StarlarkIntRef.Big(this)) * otherNum))
    }

    // fn div(&self, other: Value<'v>, heap: Heap<'v>) -> crate::Result<Value<'v>>
    fun div(other: Value, heap: Heap): Result<Value> {
        val otherNum = other.unpackNum()
            ?: return ValueError.unsupportedWith(this, "/", other)
        return Result.success(heap.alloc(NumRef.Int(StarlarkIntRef.Big(this)).div(otherNum)))
    }

    // fn floor_div(&self, other: Value<'v>, heap: Heap<'v>) -> crate::Result<Value<'v>>
    fun floorDiv(other: Value, heap: Heap): Result<Value> {
        val rhs = other.unpackNum()
            ?: return ValueError.unsupportedWith(this, "//", other)
        return Result.success(heap.alloc(NumRef.Int(StarlarkIntRef.Big(this)).floorDiv(rhs)))
    }

    // fn percent(&self, other: Value<'v>, heap: Heap<'v>) -> crate::Result<Value<'v>>
    fun percent(other: Value, heap: Heap): Result<Value> {
        val rhs = other.unpackNum()
            ?: return ValueError.unsupportedWith(this, "%", other)
        return Result.success(heap.alloc(NumRef.Int(StarlarkIntRef.Big(this)).percent(rhs)))
    }

    // fn bit_and(&self, other: Value<'v>, heap: Heap<'v>) -> crate::Result<Value<'v>>
    fun bitAnd(other: Value, heap: Heap): Result<Value> {
        val rhs = StarlarkIntRef.unpackValueOpt(other)
            ?: return ValueError.unsupportedWith(this, "&", other)
        return Result.success(heap.alloc(StarlarkIntRef.Big(this) and rhs))
    }

    // fn bit_xor(&self, other: Value<'v>, heap: Heap<'v>) -> crate::Result<Value<'v>>
    fun bitXor(other: Value, heap: Heap): Result<Value> {
        val rhs = StarlarkIntRef.unpackValueOpt(other)
            ?: return ValueError.unsupportedWith(this, "^", other)
        return Result.success(heap.alloc(StarlarkIntRef.Big(this) xor rhs))
    }

    // fn bit_or(&self, other: Value<'v>, heap: Heap<'v>) -> crate::Result<Value<'v>>
    fun bitOr(other: Value, heap: Heap): Result<Value> {
        val rhs = StarlarkIntRef.unpackValueOpt(other)
            ?: return ValueError.unsupportedWith(this, "|", other)
        return Result.success(heap.alloc(StarlarkIntRef.Big(this) or rhs))
    }

    // fn bit_not(&self, heap: Heap<'v>) -> crate::Result<Value<'v>>
    fun bitNot(heap: Heap): Result<Value> {
        return Result.success(heap.alloc(StarlarkIntRef.Big(this).not()))
    }

    // fn left_shift(&self, other: Value<'v>, heap: Heap<'v>) -> crate::Result<Value<'v>>
    fun leftShift(other: Value, heap: Heap): Result<Value> {
        val rhs = StarlarkIntRef.unpackValueOpt(other)
            ?: return ValueError.unsupportedWith(this, "<<", other)
        return Result.success(heap.alloc(StarlarkIntRef.Big(this).leftShift(rhs)))
    }

    // fn right_shift(&self, other: Value<'v>, heap: Heap<'v>) -> crate::Result<Value<'v>>
    fun rightShift(other: Value, heap: Heap): Result<Value> {
        val rhs = StarlarkIntRef.unpackValueOpt(other)
            ?: return ValueError.unsupportedWith(this, ">>", other)
        return Result.success(heap.alloc(StarlarkIntRef.Big(this).rightShift(rhs)))
    }

    // fn bin_op_ty(op: TypingBinOp, rhs: &TyBasic) -> Option<Ty>
    fun binOpTy(op: TypingBinOp, rhs: TyBasic): Ty? {
        return typecheckNumBinOp(NumTy.Int, op, rhs)
    }

    // fn write_hash(&self, hasher: &mut StarlarkHasher) -> crate::Result<()>
    fun writeHash(hasher: StarlarkHasher): Result<Unit> {
        NumRef.Int(StarlarkIntRef.Big(this))
            .getHash64()
            .let { hasher.write(it) }
        return Result.success(Unit)
    }

    // fn typechecker_ty(&self) -> Option<Ty>
    fun typecheckerTy(): Ty? = Ty.int()
}

// #[cfg(test)] mod tests { ... }
// Tests are in commonTest, not here.
