// port-lint: source src/values/types/float/float.rs
package io.github.kotlinmania.starlark_kotlin.values.types.float

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

import io.github.kotlinmania.starlark_kotlin.collections.StarlarkHashValue
import io.github.kotlinmania.starlark_kotlin.collections.StarlarkHasher
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.typing.TyBasic
import io.github.kotlinmania.starlark_kotlin.typing.TypingBinOp
import io.github.kotlinmania.starlark_kotlin.values.AllocFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.AllocValue
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.ValueError
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark_kotlin.values.types.num.NumRef
import io.github.kotlinmania.starlark_kotlin.values.types.num.NumTy
import io.github.kotlinmania.starlark_kotlin.values.types.num.typecheckNumBinOp
import io.github.kotlinmania.starlark_kotlin.any.downcastRef
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.sign

private const val WRITE_PRECISION: Int = 6

private fun writeNonFinite(output: Appendable, f: Double) {
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
        output.append("%.${WRITE_PRECISION}f".format(f))
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
        if (f.toBits() < 0L) {
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
    exponentChar: Char,
) {
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
            output.append("%.1f".format(f))
        } else {
            // rely on the built-in formatting otherwise
            output.append(f.toString())
        }
    }
}

/** Runtime representation of Starlark `float` type. */
data class StarlarkFloat(val value: Double) : StarlarkTypeRepr, AllocValue, AllocFrozenValue, StarlarkValue {

    companion object {
        /** The result of calling `type()` on floats. */
        const val TYPE: String = "float"

        internal fun compareImpl(a: Double, b: Double): Int {
            // According to the spec:
            // All NaN values compare equal to each other, but greater than any non-NaN float value.
            return if (!a.isNaN() && !b.isNaN()) {
                a.compareTo(b)
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

    override fun starlarkTypeRepr(): Ty = Ty.float()

    override fun allocValue(heap: Heap): Value = heap.allocSimple(this)

    override fun allocFrozenValue(heap: FrozenHeap): FrozenValue = heap.allocSimple(this)

    override val TYPE: String get() = Companion.TYPE

    override fun toString(): String {
        val builder = StringBuilder()
        writeCompact(builder, value, 'e')
        return builder.toString()
    }

    override fun equals(other: Value): Result<Boolean> =
        Result.success(NumRef.Float(StarlarkFloat(this.value)) == other.unpackNum())

    override fun collectRepr(collector: StringBuilder) {
        collector.append(this.toString())
    }

    override fun toBool(): Boolean = value != 0.0

    override fun writeHash(hasher: StarlarkHasher): Result<Unit> {
        hasher.write(NumRef.from(this.value).getHash64())
        return Result.success(Unit)
    }

    override fun getHash(): Result<StarlarkHashValue> =
        Result.success(NumRef.Float(this).getHash())

    override fun plus(heap: Heap): Result<Value> =
        Result.success(heap.alloc(this))

    override fun minus(heap: Heap): Result<Value> =
        Result.success(heap.alloc(StarlarkFloat(-this.value)))

    override fun add(rhs: Value, heap: Heap): Result<Value>? {
        val otherNum = rhs.unpackNum() ?: return null
        return Result.success(heap.alloc(NumRef.Float(this) + otherNum))
    }

    override fun sub(other: Value, heap: Heap): Result<Value> {
        val otherNum = other.unpackNum()
            ?: return ValueError.unsupportedWith(TYPE, "-", other)
        return Result.success(heap.alloc(NumRef.Float(this) - otherNum))
    }

    override fun mul(rhs: Value, heap: Heap): Result<Value>? {
        val otherNum = rhs.unpackNum() ?: return null
        return Result.success(heap.alloc(NumRef.Float(this) * otherNum))
    }

    override fun div(other: Value, heap: Heap): Result<Value> {
        val otherNum = other.unpackNum()
            ?: return ValueError.unsupportedWith(TYPE, "/", other)
        return NumRef.Float(this).div(otherNum).map { heap.alloc(it) }
    }

    override fun percent(other: Value, heap: Heap): Result<Value> {
        val otherNum = other.unpackNum()
            ?: return ValueError.unsupportedWith(TYPE, "%", other)
        return NumRef.Float(this).percent(otherNum).map { heap.alloc(it) }
    }

    override fun floorDiv(other: Value, heap: Heap): Result<Value> {
        val otherNum = other.unpackNum()
            ?: return ValueError.unsupportedWith(TYPE, "//", other)
        return NumRef.Float(this).floorDiv(otherNum).map { heap.alloc(it) }
    }

    override fun binOpTy(op: TypingBinOp, rhs: TyBasic): Ty? =
        typecheckNumBinOp(NumTy.Float, op, rhs)

    override fun compare(other: Value): Result<Int> {
        val otherNum = other.unpackNum()
            ?: return ValueError.unsupportedWith(TYPE, "compare", other)
        return Result.success(NumRef.Float(this).compareTo(otherNum))
    }
}

// impl StarlarkTypeRepr for f64
fun Double.starlarkTypeRepr(): Ty = Ty.float()

// impl AllocValue for f64
fun Double.allocValue(heap: Heap): Value = heap.alloc(StarlarkFloat(this))

// impl AllocFrozenValue for f64
fun Double.allocFrozenValue(heap: FrozenHeap): FrozenValue = heap.alloc(StarlarkFloat(this))

/** Allows only a float - an int will not be accepted. */
fun StarlarkFloat.Companion.unpackValueImpl(value: Value): StarlarkFloat? =
    value.downcastRef<StarlarkFloat>()

// #[cfg(test)] mod tests

private fun nonFinite(f: Double): String {
    val buf = StringBuilder()
    writeNonFinite(buf, f)
    return buf.toString()
}

internal fun testWriteNonFinite() {
    check(nonFinite(Double.NaN) == "nan")
    check(nonFinite(Double.POSITIVE_INFINITY) == "+inf")
    check(nonFinite(Double.NEGATIVE_INFINITY) == "-inf")
}

private fun decimal(f: Double): String {
    val buf = StringBuilder()
    writeDecimal(buf, f)
    return buf.toString()
}

internal fun testWriteDecimal() {
    check(decimal(Double.NaN) == "nan")
    check(decimal(Double.POSITIVE_INFINITY) == "+inf")
    check(decimal(Double.NEGATIVE_INFINITY) == "-inf")

    check(decimal(0.0) == "0.000000")
    check(decimal(kotlin.math.PI) == "3.141593")
    check(decimal(-kotlin.math.E) == "-2.718282")
    check(decimal(1e10) == "10000000000.000000")
}

private fun scientific(f: Double): String {
    val buf = StringBuilder()
    writeScientific(buf, f, 'e', false)
    return buf.toString()
}

internal fun testWriteScientific() {
    check(scientific(Double.NaN) == "nan")
    check(scientific(Double.POSITIVE_INFINITY) == "+inf")
    check(scientific(Double.NEGATIVE_INFINITY) == "-inf")

    check(scientific(0.0) == "0.000000e+00")
    check(scientific(-0.0) == "-0.000000e+00")
    check(scientific(1.23e45) == "1.230000e+45")
    check(scientific(-3.14e-145) == "-3.140000e-145")
    check(scientific(1e300) == "1.000000e+300")
}

private fun compact(f: Double): String {
    val buf = StringBuilder()
    writeCompact(buf, f, 'e')
    return buf.toString()
}

internal fun testWriteCompact() {
    check(compact(Double.NaN) == "nan")
    check(compact(Double.POSITIVE_INFINITY) == "+inf")
    check(compact(Double.NEGATIVE_INFINITY) == "-inf")

    check(compact(0.0) == "0.0")
    check(compact(kotlin.math.PI) == "3.141592653589793")
    check(compact(-kotlin.math.E) == "-2.718281828459045")
    check(compact(1e10) == "1e+10")
    check(compact(1.23e45) == "1.23e+45")
    check(compact(-3.14e-145) == "-3.14e-145")
    check(compact(1e300) == "1e+300")
}

internal fun testArithmeticOperators() {
    // +1.0 == 1.0
    // -1.0 == 0. - 1.
    // 1.0 + 2.0 == 3.0
    // 1.0 - 2.0 == -1.0
    // 2.0 * 3.0 == 6.0
    // 5.0 / 2.0 == 2.5
    // 5.0 % 3.0 == 2.0
    // 5.0 // 2.0 == 2.0
}

internal fun testDictionaryKey() {
    // x = {0: 123}
    // assert_eq(x[0], 123)
    // assert_eq(x[noop(0.0)], 123)
    // assert_eq(x[noop(-0.0)], 123)
    // assert_eq(1 in x, False)
}

internal fun testComparisons() {
    // +0.0 == -0.0
    // 0.0 == 0
    // 0 == 0.0
    // 0 < 1.0
    // 0.0 < 1
    // 1 > 0.0
    // 1.0 > 0
    // 0.0 < float("nan")
    // float("+inf") < float("nan")
}

internal fun testComparisonsBySorting() {
    // sorted([float('inf'), float('-inf'), float('nan'), 1e300, -1e300, 1.0, -1.0, 1, -1, 1e-300, -1e-300, 0, 0.0, float('-0.0'), 1e-300, -1e-300])
    // == [float('-inf'), -1e+300, -1.0, -1, -1e-300, -1e-300, 0, 0.0, -0.0, 1e-300, 1e-300, 1.0, 1, 1e+300, float('+inf'), float('nan')]
}
