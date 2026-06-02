// port-lint: source src/unordered_set.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)
package io.github.kotlinmania.starlark.collections.unorderedset

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

import io.github.kotlinmania.starlark.collections.Equivalent
import io.github.kotlinmania.starlark.collections.Hashed
import io.github.kotlinmania.starlark.collections.StarlarkHashValue
import io.github.kotlinmania.starlark.collections.unorderedmap.UnorderedMap
import kotlin.native.HiddenFromObjC

/**
 * `HashSet` that does not expose insertion order.
 *
 * Corresponds to Rust `UnorderedSet<T>` which wraps `UnorderedMap<T, ()>`.
 * In Kotlin, we wrap [UnorderedMap]<T, [Unit]> to maintain the same structure.
 */
@HiddenFromObjC
internal class UnorderedSet<T> internal constructor(
    private val map: UnorderedMap<T, Unit>,
) {
    companion object {
        /** Create a new empty set. */
        fun <T> new(): UnorderedSet<T> = UnorderedSet(UnorderedMap.new())

        /** Create a new empty set with the specified capacity. */
        fun <T> withCapacity(n: Int): UnorderedSet<T> = UnorderedSet(UnorderedMap.withCapacity(n))

        /** Create a default (empty) [UnorderedSet]. Corresponds to Rust `Default` impl. */
        fun <T> default(): UnorderedSet<T> = new()

        /**
         * Create an [UnorderedSet] from an iterable.
         * Corresponds to Rust `FromIterator` impl.
         */
        fun <T> fromIterator(iter: Iterable<T>): UnorderedSet<T> {
            val set = new<T>()
            for (v in iter) {
                set.insert(v)
            }
            return set
        }
    }

    /** Insert a value into the set. Returns `true` if the value was not already present. */
    fun insert(k: T): Boolean = map.insert(k, Unit) == null

    /** Clear the set, removing all values. */
    fun clear() = map.clear()

    /** Is the set empty? */
    fun isEmpty(): Boolean = map.isEmpty()

    /** Get the number of elements in the set. */
    fun len(): Int = map.len()

    /**
     * Does the set contain the specified value?
     * Corresponds to Rust `contains<Q>(&self, value: &Q) -> bool`.
     */
    fun contains(value: T): Boolean = map.containsKey(value)

    /**
     * Does the set contain the specified value using [Equivalent]?
     */
    fun <Q> contains(value: Q): Boolean where Q : Equivalent<T> = map.containsKey(value)

    /**
     * Does the set contain the specified value (pre-hashed)?
     * Corresponds to Rust `contains_hashed<Q>(&self, value: Hashed<&Q>) -> bool`.
     */
    fun containsHashed(value: T): Boolean = map.containsKeyHashed(value)

    /**
     * Lower-level access to the underlying map.
     * Corresponds to Rust `raw_entry_mut(&mut self) -> RawEntryBuilderMut<'_, T>`.
     */
    fun rawEntryMut(): RawEntryBuilderMut<T> = RawEntryBuilderMut(map.rawEntryMut())

    /** Iterate over the values in the set (private). */
    fun iter(): Sequence<T> = map.keysUnordered()

    /**
     * Unordered equality: two sets are equal iff they have the same elements,
     * regardless of iteration order.
     * Corresponds to Rust `PartialEq` impl.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UnorderedSet<*>) return false
        return map == other.map
    }

    override fun hashCode(): Int = map.hashCode()

    override fun toString(): String = map.toString()
}

/**
 * Get the entries in the set, sorted by [comparator].
 * Corresponds to Rust `entries_sorted(&self) -> Vec<&T>`.
 */
internal fun <T> UnorderedSet<T>.entriesSortedWith(comparator: Comparator<in T>): List<T> =
    iter().sortedWith(comparator).toList()

/** Get the entries in the set, sorted by natural order. */
internal fun <T : Comparable<T>> UnorderedSet<T>.entriesSorted(): List<T> =
    entriesSortedWith(naturalOrder())

/**
 * Builder for [RawEntryMut].
 * Corresponds to Rust `RawEntryBuilderMut<'a, T>`.
 */
@HiddenFromObjC
internal class RawEntryBuilderMut<T>(
    private val entry: io.github.kotlinmania.starlark.collections.unorderedmap.RawEntryBuilderMut<T, Unit>,
) {
    /**
     * Find the entry for a key.
     * Corresponds to Rust `from_entry<Q>(&self, entry: &Q) -> RawEntryMut<'a, T>`.
     */
    fun fromEntry(value: T): RawEntryMut<T> =
        when (val raw = entry.fromKey(value)) {
            is io.github.kotlinmania.starlark.collections.unorderedmap.RawEntryMut.Occupied ->
                RawEntryMut.Occupied(RawOccupiedEntryMut(raw.entry))
            is io.github.kotlinmania.starlark.collections.unorderedmap.RawEntryMut.Vacant ->
                RawEntryMut.Vacant(RawVacantEntryMut(raw.entry))
        }

    /**
     * Find the entry for a pre-hashed key.
     * Corresponds to Rust `from_entry_hashed<Q>(&self, entry: Hashed<&Q>) -> RawEntryMut<'a, T>`.
     */
    fun fromEntryHashed(value: Hashed<T>): RawEntryMut<T> = fromEntry(value.key())

    /**
     * Find the entry by hash and equality function.
     * Corresponds to Rust `from_hash<F>(&self, hash: StarlarkHashValue, is_match: F) -> RawEntryMut<'a, T>`.
     */
    fun fromHash(hash: StarlarkHashValue, isMatch: (T) -> Boolean): RawEntryMut<T> =
        when (val raw = entry.fromHash(hash, isMatch)) {
            is io.github.kotlinmania.starlark.collections.unorderedmap.RawEntryMut.Occupied ->
                RawEntryMut.Occupied(RawOccupiedEntryMut(raw.entry))
            is io.github.kotlinmania.starlark.collections.unorderedmap.RawEntryMut.Vacant ->
                RawEntryMut.Vacant(RawVacantEntryMut(raw.entry))
        }
}

/**
 * Reference to an entry in a [UnorderedSet].
 * Corresponds to Rust `RawEntryMut<'a, T>`.
 */
@HiddenFromObjC
internal sealed class RawEntryMut<T> {
    /** Occupied entry. */
    class Occupied<T>(
        val entry: RawOccupiedEntryMut<T>,
    ) : RawEntryMut<T>()

    /** Vacant entry. */
    class Vacant<T>(
        val entry: RawVacantEntryMut<T>,
    ) : RawEntryMut<T>()
}

/**
 * Reference to an occupied entry in a [UnorderedSet].
 * Corresponds to Rust `RawOccupiedEntryMut<'a, T>`.
 */
@HiddenFromObjC
internal class RawOccupiedEntryMut<T>(
    private val entry: io.github.kotlinmania.starlark.collections.unorderedmap.RawOccupiedEntryMut<T, Unit>,
) {
    /** Remove the entry. */
    fun remove(): T = entry.removeEntry().first

    /** Replace the entry. Returns the old value. */
    fun insert(value: T): T = entry.insertKey(value)
}

/**
 * Reference to a vacant entry in a [UnorderedSet].
 * Corresponds to Rust `RawVacantEntryMut<'a, T>`.
 */
@HiddenFromObjC
internal class RawVacantEntryMut<T>(
    private val entry: io.github.kotlinmania.starlark.collections.unorderedmap.RawVacantEntryMut<T, Unit>,
) {
    /** Insert an entry to the set. Computes the hash of the key. */
    fun insert(value: T) {
        entry.insert(value, Unit)
    }

    /** Insert an entry to the set with a pre-computed hash. */
    fun insertHashed(value: Hashed<T>) {
        entry.insertHashed(value, Unit)
    }
}
