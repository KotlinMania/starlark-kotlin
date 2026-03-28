// port-lint: source src/values/iter.rs
package io.github.kotlinmania.starlark_kotlin.values

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

import io.github.kotlinmania.starlark_kotlin.values.layout.Value

/**
 * Iterator of starlark values.
 *
 * Corresponds to Rust's `StarlarkIterator<'v>`. Implements Kotlin's [Iterator] interface
 * with a two-phase `hasNext()`/`next()` protocol instead of Rust's single `next() -> Option`.
 *
 * @property value Iterator implementation. Typically an iterable itself.
 * @property index Current index.
 * @property heap Heap to allocate values on.
 */
class StarlarkIterator private constructor(
    /** Iterator implementation. Typically an iterable itself. */
    private var value: Value,
    /** Current index. */
    private var index: Int,
    /** Heap to allocate values on. */
    private val heap: Heap,
) : Iterator<Value> {

    /**
     * Whether the iterator has been exhausted.
     * Used to track when `iter_stop` has already been called.
     */
    private var stopped: Boolean = false

    /** Cached next value from [hasNext] for use by [next]. */
    private var nextValue: Value? = null

    /**
     * Check if the iterator has more elements.
     *
     * Delegates to `iter_next` on the underlying value. When no more elements
     * remain, calls `iter_stop` exactly once and replaces the value with an
     * empty tuple (for which `iter_stop` is a no-op).
     */
    override fun hasNext(): Boolean {
        if (stopped) return false
        val r = value.getRef().iterNext(index, heap)
        if (r != null) {
            nextValue = r
            return true
        } else {
            value.getRef().iterStop()
            // We must call `iter_stop` exactly once, regardless of whether
            // iterator is exhausted or not, even if `next` is called after `None`.
            // So we replace `value` with empty tuple, for which we know that `iter_stop` is no-op.
            value = Value.newEmptyTuple()
            index = 0
            stopped = true
            return false
        }
    }

    /**
     * Returns the next element in the iteration.
     *
     * @throws NoSuchElementException if the iterator is exhausted.
     */
    override fun next(): Value {
        if (nextValue != null || hasNext()) {
            val v = nextValue!!
            nextValue = null
            index += 1
            return v
        }
        throw NoSuchElementException()
    }

    /**
     * Returns a size hint for the remaining elements.
     *
     * Returns a pair of `(lower_bound, upper_bound)` where `upper_bound`
     * is `null` if the upper bound is unknown.
     */
    fun sizeHint(): Pair<Int, Int?> {
        return value.getRef().iterSizeHint(index)
    }

    /**
     * Clean up iteration state.
     *
     * Corresponds to Rust's `Drop` implementation for `StarlarkIterator`.
     * `iter_stop` is a no-op for empty tuple, which saves a virtual call
     * after the iterator is exhausted.
     */
    fun close() {
        if (!value.ptrEq(Value.newEmptyTuple())) {
            value.getRef().iterStop()
        }
    }

    companion object {
        /**
         * Construct iterator from the given value.
         */
        internal fun new(value: Value, heap: Heap): StarlarkIterator {
            return StarlarkIterator(
                value = value,
                index = 0,
                heap = heap,
            )
        }

        /**
         * Iterator yielding no values.
         */
        fun empty(heap: Heap): StarlarkIterator {
            return new(Value.newEmptyTuple(), heap)
        }
    }
}
