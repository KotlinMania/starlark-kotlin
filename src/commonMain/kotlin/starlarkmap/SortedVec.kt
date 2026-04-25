// port-lint: source src/sorted_vec.rs
package starlarkmap.sortedvec

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
 * Type which enforces that its elements are sorted. That's it.
 *
 * Corresponds to Rust `SortedVec<T>`.
 */
class SortedVec<T> private constructor(
    private val vec: MutableList<T>,
) : Iterable<T>, Comparable<SortedVec<T>> {

    companion object {
        /** Construct an empty [SortedVec]. */
        fun <T> new(): SortedVec<T> = SortedVec(mutableListOf())

        /**
         * Construct without checking that the elements are sorted.
         *
         * In debug builds, asserts that the list is sorted.
         */
        fun <T : Comparable<T>> newUnchecked(vec: List<T>): SortedVec<T> {
            check(vec.zipWithNext().all { (a, b) -> a <= b }) {
                "SortedVec::new_unchecked called with unsorted elements"
            }
            return SortedVec(vec.toMutableList())
        }

        /** Create a default (empty) [SortedVec]. Corresponds to Rust `Default` impl. */
        fun <T> default(): SortedVec<T> = new()

        /**
         * Create a [SortedVec] from a list, sorting it first.
         * Corresponds to Rust `From<Vec<T>>` impl.
         */
        fun <T : Comparable<T>> from(vec: List<T>): SortedVec<T> {
            val sorted = vec.toMutableList()
            sorted.sort()
            return SortedVec(sorted)
        }

        /**
         * Create a [SortedVec] from an iterable, sorting the collected elements.
         * Corresponds to Rust `FromIterator` impl.
         */
        fun <T : Comparable<T>> fromIterator(iter: Iterable<T>): SortedVec<T> {
            val vec = iter.toMutableList()
            vec.sort()
            return SortedVec(vec)
        }
    }

    /** Iterate over the elements. */
    fun iter(): Sequence<T> = vec.asSequence()

    /** Corresponds to Rust `IntoIterator` impl. */
    override fun iterator(): Iterator<T> = vec.iterator()

    /**
     * Ordered equality: two [SortedVec]s are equal iff they contain the same elements
     * in the same order.
     * Corresponds to Rust `PartialEq` / `Eq` derives.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SortedVec<*>) return false
        return vec == other.vec
    }

    /**
     * Hash based on the sorted elements.
     * Corresponds to Rust `Hash` derive.
     */
    override fun hashCode(): Int = vec.hashCode()

    /**
     * Lexicographic comparison.
     * Corresponds to Rust `PartialOrd` / `Ord` derives.
     */
    override fun compareTo(other: SortedVec<T>): Int {
        val thisIter = vec.iterator()
        val otherIter = other.vec.iterator()
        while (thisIter.hasNext() && otherIter.hasNext()) {
            val t = thisIter.next()
            val o = otherIter.next()
            @Suppress("UNCHECKED_CAST")
            val cmp = (t as Comparable<T>).compareTo(o)
            if (cmp != 0) return cmp
        }
        return when {
            thisIter.hasNext() -> 1
            otherIter.hasNext() -> -1
            else -> 0
        }
    }

    override fun toString(): String = vec.toString()
}
