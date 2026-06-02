// port-lint: source src/values/types/num/value.rs
package io.github.kotlinmania.starlark.values.types.num

/*
 * Copyright 2018 The Starlark in Rust Authors.
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

import io.github.kotlinmania.starlark.collections.StarlarkHashValue
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.AllocFrozenValue
import io.github.kotlinmania.starlark.values.AllocValue
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.types.float.StarlarkFloat
import io.github.kotlinmania.starlark.values.types.int.StarlarkInt
import io.github.kotlinmania.starlark.values.types.int.StarlarkIntRef
import io.github.kotlinmania.starlark.values.types.int.allocFrozenValue
import io.github.kotlinmania.starlark.values.types.int.allocValue
import io.github.kotlinmania.starlark.values.types.int.minus
import io.github.kotlinmania.starlark.values.types.int.plus
import io.github.kotlinmania.starlark.values.types.int.times

/** Error type for numeric operations. */
sealed class NumError : Exception() {
    /** Float division by zero: {a} / {b}. */
    data class DivisionByZero(
        val a: Num,
        val b: Num,
    ) : NumError() {
        override val message: String = "float division by zero: $a / $b"
    }
}

/**
 * [NumRef] represents a numerical value that can be unpacked from a [Value].
 *
 * It's an intermediate representation that facilitates conversions between
 * numerical types and helps in implementation of arithmetical operations
 * between them.
 */
sealed class NumRef {
    data class Int(
        val value: StarlarkIntRef,
    ) : NumRef()

    // `StarlarkFloat` not `Double` here because `Double` unpacks from `int` too.
    data class Float(
        val value: StarlarkFloat,
    ) : NumRef()

    /** Get underlying value as float. */
    fun asFloat(): Double =
        when (this) {
            is Int -> value.toF64()
            is Float -> value.value
        }

    /** Get underlying value as int (if it can be precisely expressed as int). */
    fun asInt(): kotlin.Int? =
        when (this) {
            is Int -> value.toI32()
            is Float -> f64ToI32Exact(value.value)
        }

    /** Get hash of the underlying number. */
    fun getHash64(): ULong {
        fun floatHash(f: Double): ULong =
            if (f.isNaN()) {
                // all possible NaNs should hash to the same value
                0u
            } else if (f.isInfinite()) {
                ULong.MAX_VALUE
            } else if (f == 0.0) {
                // Both 0.0 and -0.0 need the same hash, but are both equal to 0.0
                (0.0).toBits().toULong()
            } else {
                f.toBits().toULong()
            }

        val i = asInt()
        // equal ints and floats should have the same hash
        if (i != null) return i.toULong()
        return when (this) {
            is Float -> floatHash(value.value)
            is Int ->
                when (value) {
                    is StarlarkIntRef.Small -> {
                        // shouldn't happen - asInt() should have resulted in an int
                        value.value.toI32().toULong()
                    }
                    is StarlarkIntRef.Big -> {
                        // Not perfect, but OK: `1000000000000000000000003` and `1000000000000000000000005`
                        // flush to the same float, and neither is exact float,
                        // so we could use better hash for such numbers.
                        floatHash(value.toF64())
                    }
                }
        }
    }

    /** Get hash as [StarlarkHashValue]. */
    fun getHash(): StarlarkHashValue = StarlarkHashValue.hash64(getHash64())

    private fun toOwned(): Num =
        when (this) {
            is Int -> Num.Int(value.toOwned())
            is Float -> Num.Float(value.value)
        }

    /** Float division: self / other. */
    fun div(other: NumRef): Result<Double> {
        val a = asFloat()
        val b = other.asFloat()
        return if (b == 0.0) {
            Result.failure(NumError.DivisionByZero(toOwned(), other.toOwned()))
        } else {
            Result.success(a / b)
        }
    }

    /** Floor division: self // other. */
    fun floorDiv(other: NumRef): Result<Num> =
        if (this is Int && other is Int) {
            value.floorDiv(other.value).map { Num.Int(it) }
        } else {
            StarlarkFloat.floorDivImpl(asFloat(), other.asFloat()).map { Num.Float(it) }
        }

    /** Percent (modulo): self % other. */
    fun percent(other: NumRef): Result<Num> =
        if (this is Int && other is Int) {
            value.percent(other.value).map { Num.Int(it) }
        } else {
            StarlarkFloat.percentImpl(asFloat(), other.asFloat()).map { Num.Float(it) }
        }

    // This is total eq per starlark spec, not Kotlin's partial eq.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NumRef) return false
        return if (this is Int && other is Int) {
            value == other.value
        } else {
            StarlarkFloat.compareImpl(asFloat(), other.asFloat()) == 0
        }
    }

    override fun hashCode(): kotlin.Int = getHash64().hashCode()

    /** Ord impl: total ordering for Starlark values. */
    operator fun compareTo(other: NumRef): kotlin.Int =
        if (this is Int && other is Int) {
            value.compareTo(other.value)
        } else {
            StarlarkFloat.compareImpl(asFloat(), other.asFloat())
        }

    /** Add operator. */
    operator fun plus(rhs: NumRef): Num =
        if (this is Int && rhs is Int) {
            Num.Int(value + rhs.value)
        } else {
            Num.Float(asFloat() + rhs.asFloat())
        }

    /** Sub operator. */
    operator fun minus(rhs: NumRef): Num =
        if (this is Int && rhs is Int) {
            Num.Int(value - rhs.value)
        } else {
            Num.Float(asFloat() - rhs.asFloat())
        }

    /** Mul operator. */
    operator fun times(rhs: NumRef): Num =
        if (this is Int && rhs is Int) {
            Num.Int(value * rhs.value)
        } else {
            Num.Float(asFloat() * rhs.asFloat())
        }

    companion object {
        fun f64ToI32Exact(f: Double): kotlin.Int? {
            val i = f.toInt()
            return if (i.toDouble() == f) i else null
        }

        /** From f64. */
        fun from(f: Double): NumRef = Float(StarlarkFloat(f))

        /** Unpack a [NumRef] from a [Value]. */
        fun unpackValue(value: Value): Result<NumRef?> {
            StarlarkIntRef.unpack(value)?.let { return Result.success(Int(it)) }
            value.downcastRef<StarlarkFloat>()?.let { return Result.success(Float(it)) }
            return Result.success(null)
        }

        /** Unpack a [NumRef] from a [Value], returning null on type mismatch (no error). */
        fun unpackValueImpl(value: Value): NumRef? {
            StarlarkIntRef.unpack(value)?.let { return Int(it) }
            value.downcastRef<StarlarkFloat>()?.let { return Float(it) }
            return null
        }

        /**
         * Unpack a [NumRef] from a [Value], returning an error if the value
         * is not a numeric type (instead of returning null).
         */
        fun unpackParam(value: Value): Result<NumRef> =
            when (val num = unpackValueImpl(value)) {
                null ->
                    Result.failure(
                        IllegalArgumentException(
                            "Type of parameters mismatch, expected `int | float`, actual `$value`",
                        ),
                    )
                else -> Result.success(num)
            }
    }
}

/** Owned numeric value (int or float). */
sealed class Num :
    StarlarkTypeRepr,
    AllocValue,
    AllocFrozenValue {
    data class Int(
        val value: StarlarkInt,
    ) : Num() {
        override fun toString(): String = value.toString()
    }

    data class Float(
        val value: Double,
    ) : Num() {
        override fun toString(): String = StarlarkFloat(value).toString()
    }

    override fun starlarkTypeRepr(): Ty =
        when (this) {
            is Int -> Ty.int()
            is Float -> Ty.float()
        }

    override fun allocValue(heap: Heap): Value =
        when (this) {
            is Int -> value.allocValue(heap)
            is Float -> StarlarkFloat(value).allocValue(heap)
        }

    override fun allocFrozenValue(heap: FrozenHeap): FrozenValue =
        when (this) {
            is Int -> value.allocFrozenValue(heap)
            is Float -> StarlarkFloat(value).allocFrozenValue(heap)
        }
}
