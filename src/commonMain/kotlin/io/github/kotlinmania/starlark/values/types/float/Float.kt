// port-lint: source src/values/types/float/float.rs
package io.github.kotlinmania.starlark.values.types.float

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
import io.github.kotlinmania.starlark.collections.StarlarkHasher
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.typing.TyBasic
import io.github.kotlinmania.starlark.typing.oracle.TypingBinOp
import io.github.kotlinmania.starlark.values.AllocFrozenValue
import io.github.kotlinmania.starlark.values.AllocValue
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.ValueError
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.types.num.NumRef
import io.github.kotlinmania.starlark.values.types.num.NumTy
import io.github.kotlinmania.starlark.values.types.num.typecheckNumBinOp
import kotlin.math.*

private const val WRITE_PRECISION: Int = 6

/** Format a Double with a fixed number of decimal places (like "%.Nf"). */
private fun formatFixed(f: Double, decimals: Int): String {
    val negative = f < 0.0 || (1.0 / f) == Double.NEGATIVE_INFINITY
    val abs = if (negative) -f else f
    val factor = 10.0.pow(decimals.toDouble())
    val rounded = (abs * factor + 0.5).toLong()
    val intPart = rounded / factor.toLong()
    val fracPart = rounded % factor.toLong()
    val fracStr = fracPart.toString().padStart(decimals, '0')
    return buildString {
        if (negative) append('-')
        append(intPart)
        append('.')
        append(fracStr)
    }
}

/** Format an Int with sign and zero-padded to at least [width] digits (like "%+0Nd"). */
private fun formatSignedPadded(value: Int, width: Int): String {
    val sign = if (value >= 0) "+" else "-"
    val digits = abs(value).toString()
    val padded = digits.padStart(width - 1, '0')
    return "$sign$padded"
}

internal fun writeNonFinite(output: Appendable, f: Double) {
    require(f.isNaN() || f.isInfinite())
    if (f.isNaN()) {
        output.append("nan")
    } else {
        output.append(if (f.toBits() >= 0L) "+inf" else "-inf")
    }
}

internal fun writeDecimal(output: Appendable, f: Double) {
    if (!f.isFinite()) {
        writeNonFinite(output, f)
    } else {
        output.append(formatFixed(f, WRITE_PRECISION))
    }
}

internal fun writeScientific(
    output: Appendable,
    f: Double,
    exponentChar: Char,
    stripTrailingZeros: Boolean,
) {
    if (!f.isFinite()) {
        writeNonFinite(output, f)
    } else {
        val abs = f.absoluteValue
        val exponent = if (f == 0.0) 0 else floor(log10(abs)).toInt()
        val normal = if (f == 0.0) 0.0 else abs / 10.0.pow(exponent.toDouble())

        // start with "-" for a negative number
        if (f.toBits() < 0L) output.append('-')

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
        if (revTailLen != 0) output.append('.')
        for (i in (revTailLen - 1) downTo 0) {
            output.append(('0'.code + revTail[i].toInt()).toChar())
        }

        // add exponent part
        output.append(exponentChar)
        output.append(formatSignedPadded(exponent, 3))
    }
}

internal fun writeCompact(output: Appendable, f: Double, exponentChar: Char) {
    if (!f.isFinite()) {
        writeNonFinite(output, f)
    } else {
        val abs = f.absoluteValue
        val exponent = if (f == 0.0) 0 else floor(log10(abs)).toInt()

        if (abs(exponent) >= WRITE_PRECISION) {
            // use scientific notation if exponent is outside of our precision (but strip 0s)
            writeScientific(output, f, exponentChar, true)
        } else if (f - floor(f) == 0.0) {
            // make sure there's a fractional part even if the number doesn't have it
            output.append(formatFixed(f, 1))
        } else {
            // rely on the built-in formatting otherwise
            output.append(f.toString())
        }
    }
}

/** Runtime representation of Starlark `float` type. */
data class StarlarkFloat(
    val value: Double,
) : StarlarkTypeRepr,
    StarlarkValue,
    AllocValue,
    AllocFrozenValue {
    override val TYPE: String get() = Companion.TYPE
    override val HAS_equals: Boolean get() = true

    companion object {
        /** The result of calling `type()` on floats. */
        const val TYPE: String = "float"

        internal fun compareImpl(a: Double, b: Double): Int {
            // According to the spec, all NaN values compare equal to each other,
            // but greater than any non-NaN float value.
            return if (!a.isNaN() && !b.isNaN()) {
                a.compareTo(b)
            } else {
                a.isNaN().compareTo(b.isNaN())
            }
        }

        internal fun floorDivImpl(a: Double, b: Double): Result<Double> =
            if (b == 0.0) Result.failure(ValueError.DivisionByZero) else Result.success(floor(a / b))

        internal fun percentImpl(a: Double, b: Double): Result<Double> {
            if (b == 0.0) return Result.failure(ValueError.DivisionByZero)
            val r = a % b
            return if (r == 0.0) {
                Result.success(0.0)
            } else {
                Result.success(if (b.sign != r.sign) r + b else r)
            }
        }
    }

    override fun starlarkTypeRepr(): Ty = Ty.float()

    override fun allocValue(heap: Heap): Value = heap.allocSimple(this)

    override fun allocFrozenValue(heap: FrozenHeap): FrozenValue = heap.allocSimple(this)

    override fun toString(): String {
        val s = StringBuilder()
        writeCompact(s, value, 'e')
        return s.toString()
    }

    // --- StarlarkValue implementation ---
    // Rust: #[starlark_value(type = StarlarkFloat::TYPE)]
    // Rust: impl<'v> StarlarkValue<'v> for StarlarkFloat

    override fun equals(other: Value): Result<Boolean> =
        Result.success(NumRef.Float(StarlarkFloat(value)) == other.unpackNum())

    override fun collectRepr(collector: StringBuilder) {
        collector.append(toString())
    }

    override fun toBool(): Boolean = value != 0.0

    override fun writeHash(hasher: StarlarkHasher): Result<Unit> {
        hasher.writeU64(NumRef.from(value).getHash64())
        return Result.success(Unit)
    }

    override fun getHash(): Result<StarlarkHashValue> =
        Result.success(NumRef.Float(this).getHash())

    override fun plus(heap: Heap): Result<Value> = Result.success(heap.alloc(this))

    override fun minus(heap: Heap): Result<Value> =
        Result.success(heap.alloc(StarlarkFloat(-value)))

    override fun add(rhs: Value, heap: Heap): Result<Value>? {
        val rhsNum = rhs.unpackNum() ?: return null
        return Result.success(heap.alloc(NumRef.Float(this) + rhsNum))
    }

    override fun sub(other: Value, heap: Heap): Result<Value> {
        val rhs = other.unpackNum() ?: return ValueError.unsupportedWith(TYPE, "-", other)
        return Result.success(heap.alloc(NumRef.Float(this) - rhs))
    }

    override fun mul(rhs: Value, heap: Heap): Result<Value>? {
        val rhsNum = rhs.unpackNum() ?: return null
        return Result.success(heap.alloc(NumRef.Float(this) * rhsNum))
    }

    override fun div(other: Value, heap: Heap): Result<Value> {
        val rhs = other.unpackNum() ?: return ValueError.unsupportedWith(TYPE, "/", other)
        return NumRef.Float(this).div(rhs).map { heap.alloc(StarlarkFloat(it)) }
    }

    override fun percent(other: Value, heap: Heap): Result<Value> {
        val rhs = other.unpackNum() ?: return ValueError.unsupportedWith(TYPE, "%", other)
        return NumRef.Float(this).percent(rhs).map { heap.alloc(it) }
    }

    override fun floorDiv(other: Value, heap: Heap): Result<Value> {
        val rhs = other.unpackNum() ?: return ValueError.unsupportedWith(TYPE, "//", other)
        return NumRef.Float(this).floorDiv(rhs).map { heap.alloc(it) }
    }

    override fun compare(other: Value): Result<Int> {
        val rhs = other.unpackNum() ?: return ValueError.unsupportedWith(TYPE, "compare", other)
        return Result.success(NumRef.Float(this).compareTo(rhs))
    }

    override fun typecheckerTy(): Ty = Ty.float()
}

// impl AllocValue for StarlarkFloat -- implemented via AllocValue interface on StarlarkFloat

// impl AllocFrozenValue for StarlarkFloat -- implemented via AllocFrozenValue interface on StarlarkFloat

// impl StarlarkTypeRepr for f64
fun Double.starlarkTypeRepr(): Ty = Ty.float()

// impl AllocValue for f64
fun Double.allocValue(heap: Heap): Value = heap.alloc(StarlarkFloat(this))

// impl AllocFrozenValue for f64
fun Double.allocFrozenValue(heap: FrozenHeap): FrozenValue = heap.alloc(StarlarkFloat(this))

/** Allows only a float - an int will not be accepted. */
fun StarlarkFloat.Companion.unpackValueImpl(value: Value): StarlarkFloat? =
    value.downcastRef<StarlarkFloat>()

fun StarlarkFloat.Companion.binOpTy(op: TypingBinOp, rhs: TyBasic): Ty? =
    typecheckNumBinOp(NumTy.Float, op, rhs)
