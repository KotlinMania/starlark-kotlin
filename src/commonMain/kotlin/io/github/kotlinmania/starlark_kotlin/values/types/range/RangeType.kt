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

/**
 * Kotlin equivalent of Rust's NonZeroI32.
 * Represents a non-zero 32-bit integer.
 */
@JvmInline
value class NonZeroI32 private constructor(val value: Int) {
    companion object {
        /**
         * Creates a new NonZeroI32 if the value is not zero, returns null otherwise.
         */
        fun new(value: Int): NonZeroI32? {
            return if (value != 0) NonZeroI32(value) else null
        }

        /**
         * Creates a new NonZeroI32, throwing an exception if the value is zero.
         * Used in contexts where we know the value is non-zero.
         */
        fun newUnchecked(value: Int): NonZeroI32 {
            require(value != 0) { "NonZeroI32 value cannot be zero" }
            return NonZeroI32(value)
        }
    }

    fun get(): Int = value
}

/**
 * Representation of `range()` type.
 */
data class Range(
    val start: Int,
    val stop: Int,
    val step: NonZeroI32
) {
    companion object {
        /** The result of calling `type()` on a range. */
        const val TYPE: String = "range"

        /**
         * Create a new [Range].
         */
        fun new(start: Int, stop: Int, step: NonZeroI32): Range {
            return Range(start, stop, step)
        }
    }

    override fun toString(): String {
        return when {
            step.get() != 1 -> "range($start, $stop, ${step.get()})"
            start != 0 -> "range($start, $stop)"
            else -> "range($stop)"
        }
    }

    private fun equalsRange(other: Range): Result<Boolean> = runCatching {
        val selfLength = length().getOrThrow()
        val otherLength = other.length().getOrThrow()
        if (selfLength == 0 || otherLength == 0) {
            return@runCatching selfLength == otherLength
        }
        if (start != other.start) {
            return@runCatching false
        }
        if (selfLength == 1 || otherLength == 1) {
            return@runCatching selfLength == otherLength
        }
        check(selfLength > 1)
        check(otherLength > 1)
        if (step.get() == other.step.get()) {
            return@runCatching selfLength == otherLength
        } else {
            return@runCatching false
        }
    }

    private fun remRangeAtIter(index: ULong): Range? {
        // Convert index to i64 if possible
        if (index > Long.MAX_VALUE.toULong()) {
            return null
        }
        val indexI64 = index.toLong()

        // Saturating multiplication and addition
        val stepI64 = step.get().toLong()
        val product = when {
            indexI64 > 0 && stepI64 > 0 && indexI64 > Long.MAX_VALUE / stepI64 -> Long.MAX_VALUE
            indexI64 > 0 && stepI64 < 0 && indexI64 > Long.MIN_VALUE / stepI64 -> Long.MIN_VALUE
            indexI64 < 0 && stepI64 > 0 && indexI64 < Long.MIN_VALUE / stepI64 -> Long.MIN_VALUE
            indexI64 < 0 && stepI64 < 0 && indexI64 < Long.MAX_VALUE / stepI64 -> Long.MAX_VALUE
            else -> indexI64 * stepI64
        }

        val startI64 = start.toLong()
        val newStartI64 = when {
            product > 0 && startI64 > Long.MAX_VALUE - product -> Long.MAX_VALUE
            product < 0 && startI64 < Long.MIN_VALUE - product -> Long.MIN_VALUE
            else -> startI64 + product
        }

        // Try to convert back to i32
        if (newStartI64 !in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) {
            return null
        }

        return Range(
            start = newStartI64.toInt(),
            stop = stop,
            step = step
        )
    }

    /**
     * Returns true if the range is non-empty.
     */
    fun toBool(): Boolean {
        return (start < stop && step.get() > 0) || (start > stop && step.get() < 0)
    }

    /**
     * Returns the length of the range.
     */
    fun length(): Result<Int> = runCatching {
        if (start == stop) {
            return@runCatching 0
        }

        // If step is into opposite direction of stop, then length is zero.
        if ((stop >= start) != (step.get() > 0)) {
            return@runCatching 0
        }

        // Convert range and step to `ULong`
        val (dist, stepUnsigned) = if (step.get() >= 0) {
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
        val i = ((dist - 1u) / stepUnsigned + 1u).toInt()
        if (i >= 0) {
            i
        } else {
            throw ValueError.IntegerOverflow
        }
    }

    /**
     * Gets the element at the specified index.
     */
    fun at(index: Value<*>, heap: Heap<*>): Result<Value<*>> = runCatching {
        val idx = convertIndex(index, length().getOrThrow()).getOrThrow()
        // Must not overflow if `length` is computed correctly
        heap.alloc(start + step.get() * idx)
    }

    /**
     * Checks equality with another value.
     */
    fun equals(other: Value<*>): Result<Boolean> = runCatching {
        val otherRange = other.downcastRef<Range>()
        if (otherRange != null) {
            equalsRange(otherRange).getOrThrow()
        } else {
            false
        }
    }

    /**
     * Creates a sliced range.
     */
    fun slice(
        start: Value<*>?,
        stop: Value<*>?,
        stride: Value<*>?,
        heap: Heap<*>
    ): Result<Value<*>> = runCatching {
        val (sliceStart, sliceStop, sliceStep) = convertSliceIndices(
            length().getOrThrow(),
            start,
            stop,
            stride
        ).getOrThrow()

        val newStart = (sliceStart.toLong() * this.step.get().toLong()).let { product ->
            (this.start.toLong() + product).let { sum ->
                if (sum in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) {
                    sum.toInt()
                } else {
                    throw ValueError.IntegerOverflow
                }
            }
        }

        val newStop = (sliceStop.toLong() * this.step.get().toLong()).let { product ->
            (this.start.toLong() + product).let { sum ->
                if (sum in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) {
                    sum.toInt()
                } else {
                    throw ValueError.IntegerOverflow
                }
            }
        }

        val newStep = (sliceStep.toLong() * this.step.get().toLong()).let { product ->
            if (product in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) {
                NonZeroI32.new(product.toInt())
                    ?: throw IllegalStateException("Step cannot be zero after multiplication")
            } else {
                throw ValueError.IntegerOverflow
            }
        }

        heap.alloc(Range(
            start = newStart,
            stop = newStop,
            step = newStep
        ))
    }

    /**
     * Returns the iterator value (the range itself).
     */
    fun iterate(me: Value<*>, heap: Heap<*>): Result<Value<*>> {
        return Result.success(me)
    }

    /**
     * Returns the next value in the iteration.
     */
    fun iterNext(index: ULong, heap: Heap<*>): Value<*>? {
        val remRange = remRangeAtIter(index) ?: return null

        if (!remRange.toBool()) {
            return null
        }

        return heap.alloc(remRange.start)
    }

    /**
     * Returns the size hint for the iterator.
     */
    fun iterSizeHint(index: ULong): Pair<ULong, ULong?> {
        val remRange = remRangeAtIter(index) ?: return Pair(0u, 0u)

        return remRange.length().fold(
            onSuccess = { len -> Pair(len.toULong(), len.toULong()) },
            onFailure = { Pair(0u, null) }
        )
    }

    /**
     * Called when iteration stops (no-op for Range).
     */
    fun iterStop() {
        // No-op
    }

    /**
     * Checks if a value is in the range.
     */
    fun isIn(other: Value<*>): Result<Boolean> = runCatching {
        val otherInt = other.unpackNum()?.asInt() ?: run {
            // Consider `"a" in range(3)`
            //
            // Should we error or return false?
            // Go Starlark errors. Python returns false.
            // Discussion at https://github.com/bazelbuild/starlark/issues/175
            return@runCatching false
        }

        if (!toBool()) {
            return@runCatching false
        }
        if (start == otherInt) {
            return@runCatching true
        }
        if (step.get() > 0) {
            if (otherInt < start || otherInt >= stop) {
                return@runCatching false
            }
            // Use wrapping_sub semantics - convert to unsigned for safe subtraction
            val diff = (otherInt - start).toUInt().toULong()
            val stepUnsigned = step.get().toUInt().toULong()
            diff % stepUnsigned == 0uL
        } else {
            if (otherInt > start || otherInt <= stop) {
                return@runCatching false
            }
            // Use wrapping_sub and wrapping_neg semantics
            val diff = (start - otherInt).toUInt().toULong()
            val stepNegated = (-step.get()).toUInt().toULong()
            diff % stepNegated == 0uL
        }
    }

    /**
     * Returns the Starlark type representation.
     */
    fun getTypeStarlarkRepr(): Ty {
        return Ty.starlarkValue<Range>()
    }

    /**
     * Equals implementation for tests.
     */
    override fun equals(other: Any?): Boolean {
        if (other !is Range) return false
        return equalsRange(other).getOrDefault(false)
    }

    override fun hashCode(): Int {
        var result = start
        result = 31 * result + stop
        result = 31 * result + step.hashCode()
        return result
    }
}

// Placeholder types - these need to be properly imported/defined elsewhere
typealias Value<V> = Any
typealias Heap<V> = Any
typealias Ty = Any

// Placeholder functions - these need to be properly implemented elsewhere
fun convertIndex(value: Value<*>, len: Int): Result<Int> {
    throw NotImplementedError("convertIndex needs to be implemented")
}

fun convertSliceIndices(
    len: Int,
    start: Value<*>?,
    stop: Value<*>?,
    stride: Value<*>?
): Result<Triple<Int, Int, Int>> {
    throw NotImplementedError("convertSliceIndices needs to be implemented")
}

fun Value<*>.downcastRef<T>(): T? {
    throw NotImplementedError("downcastRef needs to be implemented")
}

fun Value<*>.unpackNum(): Num? {
    throw NotImplementedError("unpackNum needs to be implemented")
}

interface Num {
    fun asInt(): Int?
}

fun Heap<*>.alloc(value: Int): Value<*> {
    throw NotImplementedError("Heap.alloc(Int) needs to be implemented")
}

fun Heap<*>.alloc(value: Range): Value<*> {
    throw NotImplementedError("Heap.alloc(Range) needs to be implemented")
}

object ValueError {
    object IntegerOverflow : Throwable("Integer overflow")
}

inline fun <reified T> Ty.Companion.starlarkValue(): Ty {
    throw NotImplementedError("Ty.starlarkValue needs to be implemented")
}
