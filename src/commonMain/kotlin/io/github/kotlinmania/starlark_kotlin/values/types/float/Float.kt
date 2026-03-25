// port-lint: source src/values/types/float/float.rs
package io.github.kotlinmania.starlark_kotlin.values.types.float

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

import io.github.kotlinmania.starlark_kotlin.Private
import io.github.kotlinmania.starlark_kotlin.collections.StarlarkHashValue
import io.github.kotlinmania.starlark_kotlin.collections.StarlarkHasher
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.typing.TyBasic
import io.github.kotlinmania.starlark_kotlin.typing.TypingBinOp
import io.github.kotlinmania.starlark_kotlin.values.*
import kotlin.math.*
import io.github.kotlinmania.starlark_kotlin.values.types.num.NumTy
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.types.string.unpackNum
import io.github.kotlinmania.starlark_kotlin.values.types.tuple.it
import io.github.kotlinmania.starlark_kotlin.values.types.string.format
import io.github.kotlinmania.starlark_kotlin.values.types.num.typecheckNumBinOp
import io.github.kotlinmania.starlark_kotlin.values.types.allocSimple
import io.github.kotlinmania.starlark_kotlin.any.downcastRef
import io.github.kotlinmania.starlark_kotlin.analysis.dubious.from
import io.github.kotlinmania.starlark_kotlin.analysis.dubious.Float

private const val WRITE_PRECISION: Int = 6

private fun writeNonFinite(output: Appendable, f: Double): Result<Unit> = runCatching {
    check(f.isNaN() || f.isInfinite()) { "Expected NaN or infinite value" }
    if (f.isNaN()) {
        output.append("nan")
    } else {
        output.append(if (f.sign >= 0.0) "+inf" else "-inf")
    }
}

internal fun writeDecimal(output: Appendable, f: Double): Result<Unit> = runCatching {
    if (!f.isFinite()) {
        writeNonFinite(output, f).getOrThrow()
    } else {
        // Format with WRITE_PRECISION decimal places
        val formatted = "%.${WRITE_PRECISION}f".format(f)
        output.append(formatted)
    }
}

internal fun writeScientific(
    output: Appendable,
    f: Double,
    exponentChar: Char,
    stripTrailingZeros: Boolean
): Result<Unit> = runCatching {
    if (!f.isFinite()) {
        writeNonFinite(output, f).getOrThrow()
    } else {
        val abs = absoluteValue(f)
        val exponent = if (f == 0.0) {
            0
        } else {
            floor(log10(abs)).toInt()
        }
        val normal = if (f == 0.0) {
            0.0
        } else {
            abs / 10.0.pow(exponent.toDouble())
        }

        // start with "-" for a negative number
        if (f.sign < 0.0) {
            output.append('-')
        }

        // use the whole integral part of normal (a single digit)
        output.append(floor(normal).toInt().toString())

        // calculate the fractional tail for given precision
        var tail = ((normal - floor(normal)) * 10.0.pow(WRITE_PRECISION.toDouble())).roundToLong()
        val revTail = ByteArray(WRITE_PRECISION)
        var revTailLen = 0
        var removingTrailingZeros = stripTrailingZeros
        for (i in 0 until WRITE_PRECISION) {
            val tailDigit = (tail % 10).toByte()
            if (tailDigit != 0.toByte() || !removingTrailingZeros) {
                removingTrailingZeros = false
                revTail[revTailLen] = tailDigit
                revTailLen += 1
            }
            tail /= 10
        }

        // write fractional part
        if (revTailLen != 0) {
            output.append('.')
        }
        for (i in (revTailLen - 1) downTo 0) {
            output.append(('0'.code + revTail[i].toInt()).toChar())
        }

        // add exponent part
        output.append(exponentChar)
        output.append("%+03d".format(exponent))
    }
}

internal fun writeCompact(
    output: Appendable,
    f: Double,
    exponentChar: Char
): Result<Unit> = runCatching {
    if (!f.isFinite()) {
        writeNonFinite(output, f).getOrThrow()
    } else {
        val abs = absoluteValue(f)
        val exponent = if (f == 0.0) {
            0
        } else {
            floor(log10(abs)).toInt()
        }

        if (absoluteValue(exponent) >= WRITE_PRECISION) {
            // use scientific notation if exponent is outside of our precision (but strip 0s)
            writeScientific(output, f, exponentChar, true).getOrThrow()
        } else if (f - floor(f) == 0.0) {
            // make sure there's a fractional part even if the number doesn't have it
            output.append("%.1f".format(f))
        } else {
            // rely on the built-in formatting otherwise
            output.append(f.toString())
        }
    }
}

/**
 * Runtime representation of Starlark `float` type.
 */
data class StarlarkFloat(val value: Double) : StarlarkTypeRepr {
    companion object {
        /** The result of calling `type()` on floats. */
        const val TYPE: String = "float"

        internal fun compareImpl(a: Double, b: Double): Int {
            // According to the spec (https://github.com/bazelbuild/starlark/blob/689f54426951638ef5b7c41a14d8fc48e65c5f77/spec.md#floating-point-numbers)
            // All NaN values compare equal to each other, but greater than any non-NaN float value.
            val partialCmp = a.compareTo(b)
            return if (!partialCmp.isNaN()) {
                partialCmp.sign.toInt()
            } else {
                a.isNaN().compareTo(b.isNaN())
            }
        }

        internal fun floorDivImpl(a: Double, b: Double): Result<Double> {
            return if (b == 0.0) {
                Result.failure(ValueError.DivisionByZero)
            } else {
                Result.success(floor(a / b))
            }
        }

        internal fun percentImpl(a: Double, b: Double): Result<Double> {
            return if (b == 0.0) {
                Result.failure(ValueError.DivisionByZero)
            } else {
                val r = a % b
                if (r == 0.0) {
                    Result.success(0.0)
                } else {
                    Result.success(if (b.sign != r.sign) r + b else r)
                }
            }
        }
    }

    override fun starlarkTypeRepr(): Ty {
        return Ty.float()
    }

    override fun toString(): String {
        val builder = StringBuilder()
        writeCompact(builder, value, E_').getOrThrow()
        return builder.toString()
    }
}

// Extension for Double to support StarlarkTypeRepr
fun Double.starlarkTypeRepr(): Ty {
    return StarlarkFloat(this).starlarkTypeRepr()
}

// AllocValue implementation for StarlarkFloat
fun <V> StarlarkFloat.allocValue(heap: Heap<V>): Value<V> {
    return heap.allocSimple(this)
}

// AllocFrozenValue implementation for StarlarkFloat
fun StarlarkFloat.allocFrozenValue(heap: FrozenHeap): FrozenValue {
    return heap.allocSimple(this)
}

// AllocValue implementation for Double
fun <V> Double.allocValue(heap: Heap<V>): Value<V> {
    return StarlarkFloat(this).allocValue(heap)
}

// AllocFrozenValue implementation for Double
fun Double.allocFrozenValue(heap: FrozenHeap): FrozenValue {
    return StarlarkFloat(this).allocFrozenValue(heap)
}

/**
 * Allows only a float - an int will not be accepted.
 */
fun <V> StarlarkFloat.unpackValueImpl(value: Value<V>): Result<StarlarkFloat?> {
    val downcast = value.downcastRef<StarlarkFloat>()
    return if (downcast == null) {
        Result.success(null)
    } else {
        Result.success(downcast)
    }
}

// StarlarkValue implementation for StarlarkFloat
fun <V> StarlarkFloat.equals(other: Value<V>): Result<Boolean> {
    return Result.success(NumRef.Float(StarlarkFloat(this.value)) == other.unpackNum())
}

fun StarlarkFloat.collectRepr(s: StringBuilder) {
    s.append(this.toString())
}

fun StarlarkFloat.toBool(): Boolean {
    return value != 0.0
}

fun StarlarkFloat.writeHash(hasher: StarlarkHasher): Result<Unit> {
    hasher.writeU64(NumRef.from(this.value).getHash64())
    return Result.success(Unit)
}

fun StarlarkFloat.getHash(private: Private): Result<StarlarkHashValue> {
    return Result.success(NumRef.Float(this).getHash())
}

fun <V> StarlarkFloat.plus(heap: Heap<V>): Result<Value<V>> {
    return Result.success(this.allocValue(heap))
}

fun <V> StarlarkFloat.minus(heap: Heap<V>): Result<Value<V>> {
    return Result.success(StarlarkFloat(-this.value).allocValue(heap))
}

fun <V> StarlarkFloat.add(other: Value<V>, heap: Heap<V>): Result<Value<V>>? {
    val otherNum = other.unpackNum() ?: return null
    return Result.success((NumRef.Float(this) + otherNum).allocValue(heap))
}

fun <V> StarlarkFloat.sub(other: Value<V>, heap: Heap<V>): Result<Value<V>> {
    val otherNum = other.unpackNum()
    return if (otherNum == null) {
        ValueError.unsupportedWith(this, "-", other)
    } else {
        Result.success((NumRef.Float(this) - otherNum).allocValue(heap))
    }
}

fun <V> StarlarkFloat.mul(other: Value<V>, heap: Heap<V>): Result<Value<V>>? {
    val otherNum = other.unpackNum() ?: return null
    return Result.success((NumRef.Float(this) * otherNum).allocValue(heap))
}

fun <V> StarlarkFloat.div(other: Value<V>, heap: Heap<V>): Result<Value<V>> {
    val otherNum = other.unpackNum()
    return if (otherNum == null) {
        ValueError.unsupportedWith(this, "/", other)
    } else {
        val result = NumRef.Float(this).div(otherNum)
        result.map { it.allocValue(heap) }
    }
}

fun <V> StarlarkFloat.percent(other: Value<V>, heap: Heap<V>): Result<Value<V>> {
    val otherNum = other.unpackNum()
    return if (otherNum == null) {
        ValueError.unsupportedWith(this, "%", other)
    } else {
        val result = NumRef.Float(this).percent(otherNum)
        result.map { it.allocValue(heap) }
    }
}

fun <V> StarlarkFloat.floorDiv(other: Value<V>, heap: Heap<V>): Result<Value<V>> {
    val otherNum = other.unpackNum()
    return if (otherNum == null) {
        ValueError.unsupportedWith(this, "//", other)
    } else {
        val result = NumRef.Float(this).floorDiv(otherNum)
        result.map { it.allocValue(heap) }
    }
}

fun StarlarkFloat.binOpTy(op: TypingBinOp, rhs: TyBasic): Ty? {
    return typecheckNumBinOp(NumTy.Float, op, rhs)
}

fun <V> StarlarkFloat.compare(other: Value<V>): Result<Int> {
    val otherNum = other.unpackNum()
    return if (otherNum == null) {
        ValueError.unsupportedWith(this, "compare", other)
    } else {
        Result.success(NumRef.Float(this).compareTo(otherNum))
    }
}
