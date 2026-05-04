// port-lint: source values/thinBoxSliceFrozenValue/thinBox.rs
package io.github.kotlinmania.starlark.values.thinboxslicefrozenvalue

/*
 * Copyright 2018 The Starlark in Rust Authors.
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
 * This type is a copy-paste of `buck2Util::thinBox::ThinBoxSlice`, with some mild adjustments.
 *
 * Specifically:
 *  1. This type guarantees that it's always a pointer with the bottom bit zero.
 *  2. This type is not implicitly dropped - `runDrop` must be called explicitly.
 */

internal class ThinBoxSliceLayout<T>(
    var len: Int,
    var data: MutableList<T>,
) {
    companion object {
        fun offsetOfData(): Int = 0
    }
}

/**
 * Boxed slice of T, but as a thin pointer to FrozenValue(s)
 *
 * Similar to `ThinBoxSlice`, but it ignores the lowest bit, allowing
 * PackedImpl to import that to store a single FrozenValue in place of this
 * object. Like `ThinBoxSlice`, the remaining unused pointer bits are used to
 * store an embedded length. If these bits are zero, the `ptr` points to the
 * `.data` of a ThinBoxSliceLayout, which stores the `.len`. Otherwise, ptr
 * points at the T[].
 *
 * The current implementation returns what amounts to a null pointer for an
 * empty list. An alternative would be to return a valid pointer to a
 * statically allocated "long"-lengthed object with a length of 0. This would
 * reduce the number of representations, but testing at the time of this
 * writing shows that empty lists are common, and the pointer dereference in
 * reading the length causes a small performance hit. Changes in the future may
 * make this the preferred implementation.
 */
internal class AllocatedThinBoxSlice<T> private constructor(
    private var data: MutableList<T>,
    private var ptr: Long,
) : AbstractList<T>() {

    companion object {
        fun <T> empty(): AllocatedThinBoxSlice<T> =
            AllocatedThinBoxSlice(mutableListOf(), 0L)

        fun getReservedTagBitCount(): Int = 1

        fun getUnshiftedTagBitMask(): Int {
            val align = 8
            check((align and (align - 1)) == 0) { "alignment must be a power of two" }
            return align - 1
        }

        fun getTagBitMask(): Int {
            val mask = getUnshiftedTagBitMask() ushr getReservedTagBitCount()
            check(mask != 0)
            return mask
        }

        fun getMaxShortLen(): Int = getTagBitMask() + 1

        /** Allocation layout for a slice of length `len`. */
        fun <T> layoutForLen(len: Int): Pair<Boolean, Int> {
            return if (len != 0 && len != 1 && len <= getMaxShortLen()) {
                Pair(true, len)
            } else {
                Pair(false, len)
            }
        }

        /** Allocate uninitialized memory for a slice of length `len`. */
        fun <T> newUninit(len: Int): AllocatedThinBoxSlice<T?> {
            if (len == 0) {
                return empty()
            }
            val (isShort, _layout) = layoutForLen<T>(len)
            val data: MutableList<T?> = MutableList(len) { null }
            val tag = if (isShort) ((len - 1).toLong() shl getReservedTagBitCount()) else 0L
            return AllocatedThinBoxSlice(data, tag)
        }

        fun <T> fromInner(ptr: Long, data: MutableList<T>): AllocatedThinBoxSlice<T> {
            return AllocatedThinBoxSlice(data, ptr)
        }

        fun <T> fromIter(iter: Iterable<T>): AllocatedThinBoxSlice<T> {
            val list = iter.toMutableList()
            val lower = list.size
            if (lower == 0) {
                return empty()
            }
            val (isShort, _layout) = layoutForLen<T>(lower)
            val tag = if (isShort) ((lower - 1).toLong() shl getReservedTagBitCount()) else 0L
            return AllocatedThinBoxSlice(list, tag)
        }

        fun <T> default(): AllocatedThinBoxSlice<T> = empty()
    }

    private fun getTagBits(): Int {
        return ((ptr.toInt() and getUnshiftedTagBitMask()) ushr getReservedTagBitCount())
    }

    private fun asPtr(): MutableList<T>? {
        return if (data.isEmpty() && ptr == 0L) null else data
    }

    private fun asNonnullPtr(): MutableList<T> = data

    /** Length of the slice. */
    fun readLen(): Int {
        if (asPtr() == null) {
            return 0
        }
        val bits = getTagBits()
        return if (bits != 0) {
            bits + 1
        } else {
            data.size
        }
    }

    override val size: Int get() = readLen()

    override fun get(index: Int): T = data[index]

    internal fun setUnchecked(index: Int, value: T) {
        data[index] = value
    }

    operator fun set(index: Int, value: T) {
        data[index] = value
    }

    fun assumeInit(): AllocatedThinBoxSlice<T> = this as AllocatedThinBoxSlice<T>

    fun intoInner(): Long = ptr

    fun runDrop() {
        val len = readLen()
        if (len != 0) {
            val (isShort, _layout) = layoutForLen<T>(len)
            if (!isShort) {
                // Layout includes ThinBoxSliceLayout header; nothing to free explicitly.
            }
            data.clear()
            ptr = 0L
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AllocatedThinBoxSlice<*>) return false
        return data == other.data
    }

    override fun hashCode(): Int = data.hashCode()

    override fun toString(): String = data.toString()
}
