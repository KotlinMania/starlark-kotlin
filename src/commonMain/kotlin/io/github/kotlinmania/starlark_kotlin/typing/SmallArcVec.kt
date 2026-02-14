// port-lint: source src/typing/small_arc_vec.rs
package io.github.kotlinmania.starlark_kotlin.typing

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

/**
 * Internal representation for [SmallArcVec1].
 *
 * Optimized storage for 0, 1, or many elements, avoiding heap allocation
 * for the common single-element case.
 */
private sealed class SmallArcVec1Impl<out T> {
    data object Zero : SmallArcVec1Impl<Nothing>()
    data class One<T>(val value: T) : SmallArcVec1Impl<T>()
    data class Many<T>(val values: List<T>) : SmallArcVec1Impl<T>()
}

/**
 * A small vector optimized for 0 or 1 elements.
 *
 * When there are 0 or 1 elements, no heap allocation is performed.
 * For 2+ elements, a shared [List] is used (analogous to `Arc<[T]>` in Rust).
 */
class SmallArcVec1<T> internal constructor(
    private val impl: SmallArcVec1Impl<T>
) : Comparable<SmallArcVec1<T>> where T : Comparable<T> {

    companion object {
        /** Create an empty [SmallArcVec1]. */
        fun <T : Comparable<T>> empty(): SmallArcVec1<T> = SmallArcVec1(SmallArcVec1Impl.Zero)

        /** Create a [SmallArcVec1] with a single element. */
        fun <T : Comparable<T>> one(value: T): SmallArcVec1<T> = SmallArcVec1(SmallArcVec1Impl.One(value))

        /** Create a [SmallArcVec1] from a list, choosing optimal representation. */
        fun <T : Comparable<T>> cloneFromSlice(slice: List<T>): SmallArcVec1<T> = when {
            slice.isEmpty() -> empty()
            slice.size == 1 -> one(slice[0])
            else -> SmallArcVec1(SmallArcVec1Impl.Many(slice.toList()))
        }

        /** Collect from an iterator into the optimal representation. */
        fun <T : Comparable<T>> fromIterator(iter: Iterator<T>): SmallArcVec1<T> {
            if (!iter.hasNext()) return empty()
            val i0 = iter.next()
            if (!iter.hasNext()) return one(i0)
            val list = mutableListOf(i0, iter.next())
            iter.forEach { list.add(it) }
            return SmallArcVec1(SmallArcVec1Impl.Many(list))
        }
    }

    /** Get a view of the contained elements as a [List]. */
    fun asSlice(): List<T> = when (val i = impl) {
        is SmallArcVec1Impl.Zero -> emptyList()
        is SmallArcVec1Impl.One -> listOf(i.value)
        is SmallArcVec1Impl.Many -> {
            require(i.values.size >= 2) { "Many variant must have at least 2 elements" }
            i.values
        }
    }

    /** Check if the collection is empty. */
    fun isEmpty(): Boolean = impl is SmallArcVec1Impl.Zero

    /** Get the number of elements. */
    val size: Int get() = when (val i = impl) {
        is SmallArcVec1Impl.Zero -> 0
        is SmallArcVec1Impl.One -> 1
        is SmallArcVec1Impl.Many -> i.values.size
    }

    /** Get the number of elements (alias for [size]). */
    fun len(): Int = size

    /** Iterate over the elements. */
    operator fun iterator(): Iterator<T> = asSlice().iterator()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SmallArcVec1<*>) return false
        return asSlice() == other.asSlice()
    }

    override fun hashCode(): Int = asSlice().hashCode()

    override fun toString(): String = asSlice().toString()

    override fun compareTo(other: SmallArcVec1<T>): Int {
        val thisSlice = asSlice()
        val otherSlice = other.asSlice()
        val minSize = minOf(thisSlice.size, otherSlice.size)
        for (i in 0 until minSize) {
            val cmp = thisSlice[i].compareTo(otherSlice[i])
            if (cmp != 0) return cmp
        }
        return thisSlice.size.compareTo(otherSlice.size)
    }
}

/**
 * Collect an [Iterable] into a [SmallArcVec1].
 */
fun <T : Comparable<T>> Iterable<T>.toSmallArcVec1(): SmallArcVec1<T> =
    SmallArcVec1.fromIterator(this.iterator())
