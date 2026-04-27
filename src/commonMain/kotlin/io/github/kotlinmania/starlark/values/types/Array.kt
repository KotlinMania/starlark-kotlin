// port-lint: source src/values/types/array.rs
package io.github.kotlinmania.starlark.values.types.array

/*
 * Copyright 2019 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not import this file except in compliance with the License.
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

/**
 * Array type used in implementation of `List`.
 *
 * This object is used internally, and not visible outside of `starlark` crate.
 */

import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.avalues.AllocStaticSimple
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.types.list.displayList
import io.github.kotlinmania.starlark.values.layout.FrozenValueTyped

/**
 * Fixed-capacity list.
 *
 * Mutation operations (like `insert`) panic if there's not enough remaining capacity.
 */
internal class Array(
    /** Fixed capacity. */
    private val capacity: Int,
) : StarlarkValue {
    override val TYPE: String = "array"

    /** Current number of elements in the array. */
    // Kotlin: using MutableList instead of raw pointer + len counter.
    private val content: MutableList<Value> = ArrayList(capacity)

    /** Number of active iterators: when iterator count is non-zero, we cannot modify the array. */
    private var iterCount: Int = 0

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("array(")
        sb.append(displayList(content()))
        sb.append(", cap=$capacity)")
        return sb.toString()
    }

    companion object {
        // Kotlin: No raw memory layout. Not transliterable.

        /** Create an array with specified length and capacity. */
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

    fun len(): Int = content.size

    fun capacity(): Int = capacity

    private fun isStaticallyAllocated(): Boolean = capacity == 0

    /** Remaining capacity in the array. */
    fun remainingCapacity(): Int {
        check(capacity >= len())
        check(!iterCountIsNonZero())
        return capacity - len()
    }

    /** Get an array content. */
    fun content(): List<Value> = content

    fun contentMut(): MutableList<Value> = content

    fun setAt(index: Int, value: Value) {
        check(!iterCountIsNonZero())
        require(index < len())
        content[index] = value
    }

    /** Has at least one iterator over the array. */
    fun iterCountIsNonZero(): Boolean = iterCount != 0

    fun incIterCount() {
        // When array is statically allocated, `iterCount` variable
        // is shared between threads.
        if (!isStaticallyAllocated()) {
            iterCount += 1
        }
    }

    fun decIterCount() {
        if (!isStaticallyAllocated()) {
            check(iterCount >= 1)
            iterCount -= 1
        } else {
            check(iterCount == 0)
        }
    }

    fun insert(index: Int, value: Value) {
        require(remainingCapacity() >= 1)
        require(index <= len())
        content.add(index, value)
    }

    fun push(value: Value) {
        require(remainingCapacity() >= 1)
        content.add(value)
    }

    /** `self.extendFromWithin(..)`. */
    fun double() {
        require(remainingCapacity() >= len())
        val currentContent = content.toList()
        content.addAll(currentContent)
    }

    /**
     * Extend with given elements.
     *
     * Return `Err` if any of the elements is an error.
     * Panic if there's not enough capacity.
     */
    fun <E : Throwable> tryExtend(iter: Iterable<Result<Value>>): Result<Unit> {
        for (item in iter) {
            val value = item.getOrElse { return Result.failure(it) }
            push(value)
        }
        return Result.success(Unit)
    }

    fun extendFromSlice(slice: List<Value>) {
        require(remainingCapacity() >= slice.size)
        content.addAll(slice)
    }

    fun clear() {
        check(!iterCountIsNonZero())
        content.clear()
    }

    fun remove(index: Int): Value {
        check(!iterCountIsNonZero())
        require(index < len())
        return content.removeAt(index)
    }

    // Kotlin: handled by StarlarkValue infrastructure.

    override fun length(): Result<Int> {
        return Result.success(len())
    }

    override fun iterNext(index: Int, heap: Heap): Value? {
        return content.getOrNull(index)
    }

    override fun iterStop() {
        decIterCount()
    }

    override fun iterSizeHint(index: Int): Pair<Int, Int?> {
        check(index <= len())
        val rem = len() - index
        return Pair(rem, rem)
    }
}

/**
 * `Array` is not `Sync`, so wrap it into this struct to store it in static variable.
 * Empty `Array` is logically `Sync`.
 */
internal object ValueEmptyArray {
    private val inner: AllocStaticSimple<Array> by lazy {
        AllocStaticSimple.alloc(Array.new(0, 0))
    }

    fun unpack(): FrozenValueTyped<Array> = inner.unpack()
}

// Tests are in commonTest, not here.
