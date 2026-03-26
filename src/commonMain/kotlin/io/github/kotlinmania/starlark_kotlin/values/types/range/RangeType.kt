// port-lint: source src/values/types/range/range_type.rs
package io.github.kotlinmania.starlark_kotlin.values.types.range

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

import kotlin.jvm.JvmInline
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.ValueError
import io.github.kotlinmania.starlark_kotlin.values.convertIndex
import io.github.kotlinmania.starlark_kotlin.values.convertSliceIndices
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap

@JvmInline
value class NonZeroI32 private constructor(val value: Int) {
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
) {
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

    private fun equalsRange(other: Range): Boolean {
        val selfLength = length()
        val otherLength = other.length()
        if (selfLength == 0 || otherLength == 0) {
            return selfLength == otherLength
        }
        if (start != other.start) {
            return false
        }
        if (selfLength == 1 || otherLength == 1) {
            return selfLength == otherLength
        }
        check(selfLength > 1)
        check(otherLength > 1)
        if (step.get() == other.step.get()) {
            return selfLength == otherLength
        } else {
            return false
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

    fun toBool(): Boolean {
        return (start < stop && step.get() > 0)
            || (start > stop && step.get() < 0)
    }

    fun length(): Int {
        if (start == stop) {
            return 0
        }

        // If step is into opposite direction of stop, then length is zero.
        if ((stop >= start) != (step.get() > 0)) {
            return 0
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
            return i
        } else {
            throw ValueError.IntegerOverflow
        }
    }

    fun at(index: Value, heap: Heap): Value {
        val index = convertIndex(index, length()).getOrThrow()
        // Must not overflow if `length` is computed correctly
        return heap.alloc(start + step.get() * index)
    }

    fun equals(other: Value): Boolean {
        val other = other.downcastRef<Range>()
        return if (other != null) {
            equalsRange(other)
        } else {
            false
        }
    }

    fun slice(
        start: Value?,
        stop: Value?,
        stride: Value?,
        heap: Heap
    ): Value {
        val (start, stop, step) = convertSliceIndices(length(), start, stop, stride).getOrThrow()
        return heap.alloc(Range(
            start = this.start
                .checkedAdd(
                    start
                        .checkedMul(this.step.get())
                ),
            stop = this.start
                .checkedAdd(
                    stop.checkedMul(this.step.get())
                ),
            step = NonZeroI32.new(
                step.checkedMul(this.step.get())
            )!!
        ))
    }

    fun iterate(me: Value, heap: Heap): Value {
        return me
    }

    fun iterNext(index: Int, heap: Heap): Value? {
        val remRange = remRangeAtIter(index) ?: return null

        if (!remRange.toBool()) {
            return null
        }

        return heap.alloc(remRange.start)
    }

    fun iterSizeHint(index: Int): Pair<Int, Int?> {
        val remRange = remRangeAtIter(index) ?: return Pair(0, 0)
        return try {
            val length = remRange.length()
            Pair(length, length)
        } catch (_: Exception) {
            Pair(0, null)
        }
    }

    fun iterStop() {}

    fun isIn(other: Value): Boolean {
        val other = other.unpackNum()?.asInt() ?: run {
            // Consider `"a" in range(3)`
            //
            // Should we error or return false?
            // Go Starlark errors. Python returns false.
            // Discussion at https://github.com/bazelbuild/starlark/issues/175
            return false
        }
        if (!toBool()) {
            return false
        }
        if (start == other) {
            return true
        }
        if (step.get() > 0) {
            if (other < start || other >= stop) {
                return false
            }
            return (other - start).toULong() % step.get().toULong() == 0uL
        } else {
            if (other > start || other <= stop) {
                return false
            }
            return (start - other).toULong() % (-step.get()).toULong() == 0uL
        }
    }

    fun getTypeStarlarkRepr(): Ty {
        return Ty.starlarkValue<Range>()
    }

    /** For tests. */
    override fun equals(other: Any?): Boolean {
        if (other !is Range) return false
        return equalsRange(other)
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
    check(0 == rangeStop(0).length())
    check(17 == rangeStop(17).length())
}

internal fun testLengthStartStop() {
    check(20 == rangeStartStop(10, 30).length())
    check(0 == rangeStartStop(10, -30).length())
    check(Int.MAX_VALUE == rangeStartStop(0, Int.MAX_VALUE).length())
    check(runCatching { rangeStartStop(-1, Int.MAX_VALUE).length() }.isFailure)
}

internal fun testLengthStartStopStep() {
    check(5 == range(0, 10, 2).length())
    check(5 == range(0, 9, 2).length())
    check(0 == range(0, 10, -2).length())
    check(5 == range(10, 0, -2).length())
    check(5 == range(9, 0, -2).length())
    check(1 == range(4, 14, 10).length())
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
        check(x.length() == full.size)
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
