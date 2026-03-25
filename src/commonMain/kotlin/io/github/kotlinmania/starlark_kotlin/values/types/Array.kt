// port-lint: source src/values/types/array.rs
package io.github.kotlinmania.starlark_kotlin.values.types.array

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

//! Array type used in implementation of `List`.
//!
//! This object is used internally, and not visible outside of `starlark` crate.

import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.owned.FrozenValueTyped
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.AllocStaticSimple
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.types.list.displayList

/// Fixed-capacity list.
///
/// Mutation operations (like `insert`) panic if there's not enough remaining capacity.
// #[derive(ProvidesStaticType, Allocative)]
// #[repr(C)]
// pub(crate) struct Array<'v> {
//     len: UnsafeCell<u32>,
//     capacity: u32,
//     iter_count: UnsafeCell<u32>,
//     content: [Value<'v>; 0],
// }
internal class Array(
    /// Fixed capacity.
    private val capacity: Int,
) : StarlarkValue {

    /// Current number of elements in the array.
    // Kotlin: using MutableList instead of raw pointer + len counter.
    private val content: MutableList<Value> = ArrayList(capacity)

    /// Number of active iterators: when iterator count is non-zero, we cannot modify the array.
    private var iterCount: Int = 0

    // impl Debug for Array
    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("array(")
        displayList(content(), sb)
        sb.append(", cap=$capacity)")
        return sb.toString()
    }

    companion object {
        // pub(crate) fn offset_of_content() -> usize
        // Kotlin: No raw memory layout. Not transliterable.

        // pub(crate) const unsafe fn new(len: u32, capacity: u32) -> Array<'v>
        /// Create an array with specified length and capacity.
        fun new(len: Int, capacity: Int): Array {
            require(len <= capacity)
            val arr = Array(capacity)
            // Fill with None values up to `len`.
            repeat(len) {
                arr.content.add(Value.newNone())
            }
            return arr
        }
    }

    // pub(crate) fn len(&self) -> usize
    fun len(): Int = content.size

    // pub(crate) fn capacity(&self) -> usize
    fun capacity(): Int = capacity

    // fn is_statically_allocated(&self) -> bool
    private fun isStaticallyAllocated(): Boolean = capacity == 0

    /// Remaining capacity in the array.
    // pub(crate) fn remaining_capacity(&self) -> usize
    fun remainingCapacity(): Int {
        check(capacity >= len())
        check(!iterCountIsNonZero())
        return capacity - len()
    }

    /// Get an array content.
    // pub(crate) fn content(&self) -> &[Value<'v>]
    fun content(): List<Value> = content

    // pub(crate) fn content_mut(&mut self) -> &mut [Value<'v>]
    fun contentMut(): MutableList<Value> = content

    // pub(crate) fn set_at(&self, index: usize, value: Value<'v>)
    fun setAt(index: Int, value: Value) {
        check(!iterCountIsNonZero())
        require(index < len())
        content[index] = value
    }

    /// Has at least one iterator over the array.
    // pub(crate) fn iter_count_is_non_zero(&self) -> bool
    fun iterCountIsNonZero(): Boolean = iterCount != 0

    // pub(crate) fn inc_iter_count(&self)
    fun incIterCount() {
        // When array is statically allocated, `iter_count` variable
        // is shared between threads.
        if (!isStaticallyAllocated()) {
            iterCount += 1
        }
    }

    // pub(crate) fn dec_iter_count(&self)
    fun decIterCount() {
        if (!isStaticallyAllocated()) {
            check(iterCount >= 1)
            iterCount -= 1
        } else {
            check(iterCount == 0)
        }
    }

    // pub(crate) fn insert(&self, index: usize, value: Value<'v>)
    fun insert(index: Int, value: Value) {
        require(remainingCapacity() >= 1)
        require(index <= len())
        content.add(index, value)
    }

    // pub(crate) fn push(&self, value: Value<'v>)
    fun push(value: Value) {
        require(remainingCapacity() >= 1)
        content.add(value)
    }

    /// `self.extend_from_within(..)`.
    // pub(crate) fn double(&self)
    fun double() {
        require(remainingCapacity() >= len())
        val currentContent = content.toList()
        content.addAll(currentContent)
    }

    /// Extend with given elements.
    ///
    /// Return `Err` if any of the elements is an error.
    /// Panic if there's not enough capacity.
    // pub(crate) fn try_extend<E>(&self, iter: impl IntoIterator<Item = Result<Value<'v>, E>>) -> Result<(), E>
    fun <E : Throwable> tryExtend(iter: Iterable<Result<Value>>): Result<Unit> {
        for (item in iter) {
            val value = item.getOrElse { return Result.failure(it) }
            push(value)
        }
        return Result.success(Unit)
    }

    // pub(crate) fn extend_from_slice(&self, slice: &[Value<'v>])
    fun extendFromSlice(slice: List<Value>) {
        require(remainingCapacity() >= slice.size)
        content.addAll(slice)
    }

    // pub(crate) fn clear(&self)
    fun clear() {
        check(!iterCountIsNonZero())
        content.clear()
    }

    // pub(crate) fn remove(&self, index: usize) -> Value<'v>
    fun remove(index: Int): Value {
        check(!iterCountIsNonZero())
        require(index < len())
        return content.removeAt(index)
    }

    // #[starlark_value(type = "array")]
    // impl<'v> StarlarkValue<'v> for Array<'v>

    // fn is_special(_: Private) -> bool
    // Kotlin: handled by StarlarkValue infrastructure.

    // fn length(&self) -> crate::Result<i32>
    fun length(): Result<Int> {
        return Result.success(len())
    }

    // unsafe fn iter_next(&self, index: usize, _heap: Heap<'v>) -> Option<Value<'v>>
    fun iterNext(index: Int, heap: Heap): Value? {
        return content.getOrNull(index)
    }

    // unsafe fn iter_stop(&self)
    fun iterStop() {
        decIterCount()
    }

    // unsafe fn iter_size_hint(&self, index: usize) -> (usize, Option<usize>)
    fun iterSizeHint(index: Int): Pair<Int, Int?> {
        check(index <= len())
        val rem = len() - index
        return Pair(rem, rem)
    }
}

/// `Array` is not `Sync`, so wrap it into this struct to store it in static variable.
/// Empty `Array` is logically `Sync`.
// pub(crate) struct ValueEmptyArray(AllocStaticSimple<Array<'static>>);
// pub(crate) static VALUE_EMPTY_ARRAY: ValueEmptyArray = ...;
internal object ValueEmptyArray {
    private val emptyArray: Array = Array.new(0, 0)

    // impl ValueEmptyArray
    // pub(crate) fn unpack<'v>(&'static self) -> FrozenValueTyped<'v, Array<'v>>
    fun unpack(): Array = emptyArray
}

// #[cfg(test)] mod tests
// Tests are in commonTest, not here.
