// port-lint: source src/values/iter.rs
package io.github.kotlinmania.starlark.values

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

// use std::fmt::Debug;

// use crate::values::Heap;
// use crate::values::Value;

import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.Heap

/// Iterator of starlark values.
// #[derive(Debug)]
// pub struct StarlarkIterator<'v> {
//     /// Iterator implementation. Typically an iterable itself.
//     value: Value<'v>,
//     /// Current index.
//     index: usize,
//     /// Heap to allocate values on.
//     heap: Heap<'v>,
// }
class StarlarkIterator private constructor(
    /// Iterator implementation. Typically an iterable itself.
    private var value: Value,
    /// Current index.
    private var index: Int,
    /// Heap to allocate values on.
    private val heap: Heap,
) : Iterator<Value> {

    private var stopped: Boolean = false
    private var nextValue: Value? = null

    // impl<'v> Iterator for StarlarkIterator<'v> {
    //     type Item = Value<'v>;
    //     fn next(&mut self) -> Option<Value<'v>>
    override fun hasNext(): Boolean {
        if (stopped) return false
        // let r = self.value.get_ref().iter_next(self.index, self.heap);
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

    override fun next(): Value {
        if (nextValue != null || hasNext()) {
            val v = nextValue!!
            nextValue = null
            index += 1
            return v
        }
        throw NoSuchElementException()
    }

    // #[inline]
    // fn size_hint(&self) -> (usize, Option<usize>)
    fun sizeHint(): Pair<Int, Int?> {
        return value.getRef().iterSizeHint(index)
    }

    // impl<'v> Drop for StarlarkIterator<'v>
    //     fn drop(&mut self)
    // `iter_stop` is no-op for empty tuple, this saves us from virtual call
    // after iterator is exhausted.
    fun close() {
        if (!value.ptrEq(Value.newEmptyTuple())) {
            value.getRef().iterStop()
        }
    }

    // impl<'v> StarlarkIterator<'v>
    companion object {
        /// Construct iterator from the given value.
        // #[inline]
        // pub(crate) fn new(value: Value<'v>, heap: Heap<'v>) -> StarlarkIterator<'v>
        internal fun new(value: Value, heap: Heap): StarlarkIterator {
            return StarlarkIterator(
                value = value,
                index = 0,
                heap = heap,
            )
        }

        /// Iterator yielding no values.
        // #[inline]
        // pub fn empty(heap: Heap<'v>) -> StarlarkIterator<'v>
        fun empty(heap: Heap): StarlarkIterator {
            return new(Value.newEmptyTuple(), heap)
        }
    }
}
