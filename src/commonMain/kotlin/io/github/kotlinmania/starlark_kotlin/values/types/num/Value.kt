// port-lint: source src/values/types/num/value.rs
package io.github.kotlinmania.starlark_kotlin.values.types.num

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

import io.github.kotlinmania.starlark_kotlin.collections.StarlarkHashValue
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.AllocFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.AllocValue
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.UnpackValue
import io.github.kotlinmania.starlark_kotlin.values.types.float.StarlarkFloat
import io.github.kotlinmania.starlark_kotlin.values.types.int.StarlarkInt
import io.github.kotlinmania.starlark_kotlin.values.types.int.StarlarkIntRef
import kotlin.math.floor
import io.github.kotlinmania.starlark_kotlin.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.types.tuple.it
import io.github.kotlinmania.starlark_kotlin.values.types.bigint.toI32
import io.github.kotlinmania.starlark_kotlin.values.types.bigint.toF64
import io.github.kotlinmania.starlark_kotlin.values.percent
import io.github.kotlinmania.starlark_kotlin.values.owned_frozen_ref.toOwned
import io.github.kotlinmania.starlark_kotlin.any.downcastRef

sealed class NumError : Exception() {
    data class DivisionByZero(val a: Num, val b: Num) : NumError() {
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
    data class Int(val value: StarlarkIntRef) : NumRef()
    // `StarlarkFloat` not `Double` here because `Double` unpacks from `int` too.
    data class Float(val value: StarlarkFloat) : NumRef()

    /**
     * Get underlying value as float
     */
    fun asFloat(): Double = when (this) {
        is Int -> value.toF64()
        is Float -> value.value
    }

    /**
     * Get underlying value as int (if it can be precisely expressed as int)
     */
    fun asInt(): kotlin.Int? = when (this) {
        is Int -> value.toI32()
        is Float -> f64ToI32Exact(value.value)
    }

    /**
     * Get hash of the underlying number
     */
    fun getHash64(): ULong {
        fun floatHash(f: Double): ULong {
            return if (f.isNaN()) {
                // all possible NaNs should hash to the same value
                0u
            } else if (f.isInfinite()) {
                ULong.MAX_VALUE
            } else if (f == 0.0) {
                // Both 0.0 and -0.0 need the same hash, but are both equal to 0.0
                0.0.toBits().toULong()
            } else {
                f.toBits().toULong()
            }
        }

        return when (val i = asInt()) {
            // equal ints and floats should have the same hash
            null -> when (this) {
                is Float -> floatHash(value.value)
                is Int -> when (value) {
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
            else -> i.toULong()
        }
    }

    fun getHash(): StarlarkHashValue {
        return StarlarkHashValue.hash64(getHash64())
    }

    private fun toOwned(): Num = when (this) {
        is Int -> Num.Int(value.toOwned())
        is Float -> Num.Float(value.value)
    }

    fun div(other: NumRef): Result<Double> {
        val a = this.asFloat()
        val b = other.asFloat()
        return if (b == 0.0) {
            Result.failure(NumError.DivisionByZero(this.toOwned(), other.toOwned()))
        } else {
            Result.success(a / b)
        }
    }

    fun floorDiv(other: NumRef): Result<Num> {
        return if (this is Int && other is Int) {
            value.floorDiv(other.value).map { Num.Int(it) }
        } else {
            StarlarkFloat.floorDivImpl(this.asFloat(), other.asFloat()).map { Num.Float(it) }
        }
    }

    fun percent(other: NumRef): Result<Num> {
        return if (this is Int && other is Int) {
            value.percent(other.value).map { Num.Int(it) }
        } else {
            StarlarkFloat.percentImpl(this.asFloat(), other.asFloat()).map { Num.Float(it) }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NumRef) return false

        return if (this is Int && other is Int) {
            value == other.value
        } else {
            StarlarkFloat.compareImpl(this.asFloat(), other.asFloat()) == 0
        }
    }

    override fun hashCode(): kotlin.Int {
        return getHash64().hashCode()
    }

    operator fun compareTo(other: NumRef): kotlin.Int {
        return if (this is Int && other is Int) {
            value.compareTo(other.value)
        } else {
            StarlarkFloat.compareImpl(this.asFloat(), other.asFloat())
        }
    }

    operator fun plus(rhs: NumRef): Num {
        return if (this is Int && rhs is Int) {
            Num.Int(value + rhs.value)
        } else {
            Num.Float(this.asFloat() + rhs.asFloat())
        }
    }

    operator fun minus(rhs: NumRef): Num {
        return if (this is Int && rhs is Int) {
            Num.Int(value - rhs.value)
        } else {
            Num.Float(this.asFloat() - rhs.asFloat())
        }
    }

    operator fun times(rhs: NumRef): Num {
        return if (this is Int && rhs is Int) {
            Num.Int(value * rhs.value)
        } else {
            Num.Float(this.asFloat() * rhs.asFloat())
        }
    }

    companion object {
        fun f64ToI32Exact(f: Double): kotlin.Int? {
            val i = f.toInt()
            return if (i.toDouble() == f) i else null
        }

        fun from(f: Double): NumRef {
            return Float(StarlarkFloat(f))
        }
    }
}

sealed class Num : StarlarkTypeRepr, AllocValue, AllocFrozenValue {
    data class Int(val value: StarlarkInt) : Num() {
        override fun toString(): String = value.toString()
    }

    data class Float(val value: Double) : Num() {
        override fun toString(): String = StarlarkFloat(value).toString()
    }

    override fun starlarkTypeRepr(): Ty {
        return when (this) {
            is Int -> Ty.int()
            is Float -> Ty.float()
        }
    }

    override fun <V> allocValue(heap: Heap<V>): Value<V> {
        return when (this) {
            is Int -> value.allocValue(heap)
            is Float -> StarlarkFloat(value).allocValue(heap)
        }
    }

    override fun allocFrozenValue(heap: FrozenHeap): FrozenValue {
        return when (this) {
            is Int -> heap.alloc(value)
            is Float -> heap.alloc(StarlarkFloat(value))
        }
    }
}

// UnpackValue implementation for NumRef
fun <V> NumRef.Companion.unpackValue(value: Value<V>): Result<NumRef?> {
    // Try to unpack as StarlarkIntRef first
    StarlarkIntRef.unpack(value)?.let {
        return Result.success(NumRef.Int(it))
    }

    // Try to unpack as StarlarkFloat
    value.downcastRef<StarlarkFloat>()?.let {
        return Result.success(NumRef.Float(it))
    }

    return Result.success(null)
}
