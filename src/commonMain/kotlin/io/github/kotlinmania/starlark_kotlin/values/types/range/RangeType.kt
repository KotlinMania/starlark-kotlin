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

import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.typing.TyStarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.ValueError
import io.github.kotlinmania.starlark_kotlin.values.convertIndex
import io.github.kotlinmania.starlark_kotlin.values.convertSliceIndices
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.types.bigint.allocValue
import io.github.kotlinmania.starlark_kotlin.values.types.int.InlineInt

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
) : io.github.kotlinmania.starlark_kotlin.values.StarlarkValue {
    override val TYPE: String get() = Companion.TYPE
    override val HAS_iterate: Boolean get() = true
    override val HAS_equals: Boolean get() = true
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

        val newStepValue = sliceStep.checkedMul(this.step.get())
        val newStep = NonZeroI32.new(newStepValue)
            ?: return Result.failure(ValueError.IntegerOverflow)

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
            step = newStep
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

private fun Int.checkedMul(other: Int): Int =
    InlineInt.tryFrom(this).getOrNull()
        ?.checkedMulI32(other)
        ?.toI32()
        ?: throw ValueError.Runtime("Integer overflow in InlineInt multiplication: Int($this) * Int($other)")

private fun Int.checkedAdd(other: Int): Int {
    val lhs = InlineInt.tryFrom(this).getOrNull()
        ?: throw ValueError.Runtime("Integer overflow converting left Int to InlineInt for addition: $this")
    val rhs = InlineInt.tryFrom(other).getOrNull()
        ?: throw ValueError.Runtime("Integer overflow converting right Int to InlineInt for addition: $other")
    return lhs.checkedAdd(rhs)?.toI32()
        ?: throw ValueError.Runtime("Integer overflow in addition: $this + $other")
}
