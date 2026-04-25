// port-lint: source src/values/types/range/range_type.rs
package io.github.kotlinmania.starlark.values.types.range

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

import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.typing.TyStarlarkValue
import io.github.kotlinmania.starlark.values.ValueError
import io.github.kotlinmania.starlark.values.convertIndex
import io.github.kotlinmania.starlark.values.convertSliceIndices
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.types.bigint.allocValue

@ConsistentCopyVisibility
data class NonZeroI32 private constructor(val value: Int) {
    companion object {
        fun new(value: Int): NonZeroI32? =
            if (value != 0) NonZeroI32(value) else null
    }

    fun get(): Int = value
}

/** Representation of `range()` type. */
data class Range(
    val start: Int,
    val stop: Int,
    val step: NonZeroI32
) : io.github.kotlinmania.starlark.values.StarlarkValue, io.github.kotlinmania.starlark.values.AllocValue {
    override val TYPE: String get() = Companion.TYPE
    companion object {
        /** The result of calling `type()` on a range. */
        const val TYPE: String = "range"

        /** Create a new [Range]. */
        fun new(start: Int, stop: Int, step: NonZeroI32): Range {
            return Range(start, stop, step)
        }
    }

    override fun toString(): String {
        if (step.get() != 1) {
            return "range($start, $stop, ${step.get()})"
        } else if (start != 0) {
            return "range($start, $stop)"
        } else {
            return "range($stop)"
        }
    }

    override fun allocValue(heap: Heap): Value = heap.allocSimple(this)

    override fun starlarkTypeRepr(): Ty = Ty.starlarkValue(TyStarlarkValue.new(TYPE))

    private fun equalsRange(other: Range): Result<Boolean> {
        val selfLength = length().getOrElse { return Result.failure(it) }
        val otherLength = other.length().getOrElse { return Result.failure(it) }
        if (selfLength == 0 || otherLength == 0) {
            return Result.success(selfLength == otherLength)
        }
        if (start != other.start) {
            return Result.success(false)
        }
        if (selfLength == 1 || otherLength == 1) {
            return Result.success(selfLength == otherLength)
        }
        check(selfLength > 1)
        check(otherLength > 1)
        if (step.get() == other.step.get()) {
            return Result.success(selfLength == otherLength)
        } else {
            return Result.success(false)
        }
    }

    internal fun remRangeAtIter(index: Int): Range? {
        val index = index.toLong()
        val step = this.step.get().toLong()

        // saturating_mul then saturating_add
        var product = index * step
        if (index != 0L && product / index != step) {
            product = if ((index xor step) >= 0) Long.MAX_VALUE else Long.MIN_VALUE
        }
        var start = this.start.toLong() + product
        if ((product > 0 && start < this.start.toLong()) || (product < 0 && start > this.start.toLong())) {
            start = if (product > 0) Long.MAX_VALUE else Long.MIN_VALUE
        }

        if (start < Int.MIN_VALUE.toLong() || start > Int.MAX_VALUE.toLong()) {
            return null
        }

        return Range(
            start = start.toInt(),
            stop = this.stop,
            step = this.step
        )
    }

    override fun toBool(): Boolean {
        return (start < stop && step.get() > 0)
            || (start > stop && step.get() < 0)
    }

    override fun length(): Result<Int> {
        if (start == stop) {
            return Result.success(0)
        }

        // If step is into opposite direction of stop, then length is zero.
        if ((stop >= start) != (step.get() > 0)) {
            return Result.success(0)
        }

        // Convert range and step to unsigned
        val (dist, step) = if (step.get() >= 0) {
            Pair(
                (stop - start).toULong(),
                step.get().toULong()
            )
        } else {
            Pair(
                (start - stop).toULong(),
                (-step.get()).toULong()
            )
        }
        val i = ((dist - 1u) / step + 1u).toInt()
        if (i >= 0) {
            return Result.success(i)
        } else {
            return Result.failure(ValueError.IntegerOverflow)
        }
    }

    override fun at(index: Value, heap: Heap): Result<Value> {
        val len = length().getOrElse { return Result.failure(it) }
        val idx = convertIndex(index, len).getOrElse { return Result.failure(it) }
        // Must not overflow if `length` is computed correctly
        return Result.success((start + step.get() * idx).allocValue(heap))
    }

    override fun equals(other: Value): Result<Boolean> {
        val otherRange = other.downcastRef<Range>()
        return if (otherRange != null) {
            equalsRange(otherRange)
        } else {
            Result.success(false)
        }
    }

    override fun slice(
        start: Value?,
        stop: Value?,
        stride: Value?,
        heap: Heap
    ): Result<Value> {
        val len = length().getOrElse { return Result.failure(it) }
        val (sliceStart, sliceStop, sliceStep) = convertSliceIndices(len, start, stop, stride)
            .getOrElse { return Result.failure(it) }
        return Result.success(heap.allocSimple(Range(
            start = this.start
                .checkedAdd(
                    sliceStart
                        .checkedMul(this.step.get())
                ),
            stop = this.start
                .checkedAdd(
                    sliceStop.checkedMul(this.step.get())
                ),
            step = NonZeroI32.new(
                sliceStep.checkedMul(this.step.get())
            )!!
        )))
    }

    override fun iterate(me: Value, heap: Heap): Result<Value> {
        return Result.success(me)
    }

    override fun iterNext(index: Int, heap: Heap): Value? {
        val remRange = remRangeAtIter(index) ?: return null

        if (!remRange.toBool()) {
            return null
        }

        return remRange.start.allocValue(heap)
    }

    override fun iterSizeHint(index: Int): Pair<Int, Int?> {
        val remRange = remRangeAtIter(index) ?: return Pair(0, 0)
        val len = remRange.length()
        return if (len.isSuccess) {
            val l = len.getOrThrow()
            Pair(l, l)
        } else {
            Pair(0, null)
        }
    }

    override fun iterStop() {}

    override fun isIn(other: Value): Result<Boolean> {
        val otherInt = other.unpackNum()?.asInt() ?: run {
            // Consider `"a" in range(3)`
            //
            // Should we error or return false?
            // Go Starlark errors. Python returns false.
            // Discussion at https://github.com/bazelbuild/starlark/issues/175
            return Result.success(false)
        }
        if (!toBool()) {
            return Result.success(false)
        }
        if (start == otherInt) {
            return Result.success(true)
        }
        if (step.get() > 0) {
            if (otherInt < start || otherInt >= stop) {
                return Result.success(false)
            }
            return Result.success((otherInt - start).toULong() % step.get().toULong() == 0uL)
        } else {
            if (otherInt > start || otherInt <= stop) {
                return Result.success(false)
            }
            return Result.success((start - otherInt).toULong() % (-step.get()).toULong() == 0uL)
        }
    }

    override fun getTypeStarlarkRepr(): Ty {
        return Ty.starlarkValue(TyStarlarkValue.new("range"))
    }

    /** For tests. */
    override fun equals(other: Any?): Boolean {
        if (other !is Range) return false
        return equalsRange(other).getOrThrow()
    }

    override fun hashCode(): Int {
        var result = start
        result = 31 * result + stop
        result = 31 * result + step.hashCode()
        return result
    }
}

private fun Int.checkedMul(other: Int): Int {
    val result = this.toLong() * other.toLong()
    if (result < Int.MIN_VALUE || result > Int.MAX_VALUE) throw ValueError.IntegerOverflow
    return result.toInt()
}

private fun Int.checkedAdd(other: Int): Int {
    val result = this.toLong() + other.toLong()
    if (result < Int.MIN_VALUE || result > Int.MAX_VALUE) throw ValueError.IntegerOverflow
    return result.toInt()
}

// Tests

private fun range(start: Int, stop: Int, step: Int): Range {
    return Range(start, stop, NonZeroI32.new(step)!!)
}

private fun rangeStartStop(start: Int, stop: Int): Range {
    return range(start, stop, 1)
}

private fun rangeStop(stop: Int): Range {
    return rangeStartStop(0, stop)
}

internal fun testLengthStop() {
    check(0 == rangeStop(0).length().getOrThrow())
    check(17 == rangeStop(17).length().getOrThrow())
}

internal fun testLengthStartStop() {
    check(20 == rangeStartStop(10, 30).length().getOrThrow())
    check(0 == rangeStartStop(10, -30).length().getOrThrow())
    check(Int.MAX_VALUE == rangeStartStop(0, Int.MAX_VALUE).length().getOrThrow())
    check(rangeStartStop(-1, Int.MAX_VALUE).length().isFailure)
}

internal fun testLengthStartStopStep() {
    check(5 == range(0, 10, 2).length().getOrThrow())
    check(5 == range(0, 9, 2).length().getOrThrow())
    check(0 == range(0, 10, -2).length().getOrThrow())
    check(5 == range(10, 0, -2).length().getOrThrow())
    check(5 == range(9, 0, -2).length().getOrThrow())
    check(1 == range(4, 14, 10).length().getOrThrow())
}

internal fun testEq() {
    check(rangeStop(0) == range(2, 1, 3))
}

internal fun testRangeExhaustive() {
    // The range implementation is fairly hairy. Lots of corner cases etc.
    // Especially around equality, length.
    // Therefore, generate ranges exhaustively over a very small range
    // and test lots of properties about them.
    val ranges = mutableListOf<Range>()
    for (start in -3..3) {
        for (stop in -3..3) {
            for (step in -3..2) {
                val step = if (step >= 0) step + 1 else step
                ranges.add(range(start, stop, step))
            }
        }
    }
    check(ranges.size == 294) // Assert we don't accidentally take too long

    for (x in ranges) {
        val full = iterateRange(x)
        check(x.length().getOrThrow() == full.size)
        for ((i, v) in full.withIndex()) {
            check(x.start + x.step.get() * i == v)
        }
    }

    // Takes 294^2 steps - but completes instantly
    for (x in ranges) {
        for (y in ranges) {
            check((x == y) == (iterateRange(x) == iterateRange(y)))
        }
    }
}

private fun iterateRange(r: Range): List<Int> {
    val result = mutableListOf<Int>()
    var index = 0
    while (true) {
        val rem = r.remRangeAtIter(index) ?: break
        if (!rem.toBool()) break
        result.add(rem.start)
        index++
    }
    return result
}
