// port-lint: source src/ordered_map.rs
package io.github.kotlinmania.starlark_kotlin.collections.ordered_map

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

import io.github.kotlinmania.starlark_kotlin.collections.Equivalent
import io.github.kotlinmania.starlark_kotlin.collections.Hashed
import io.github.kotlinmania.starlark_kotlin.collections.SmallMap

/**
 * Wrapper for [SmallMap] which considers map equal if iteration order is equal.
 *
 * Unlike [SmallMap], two [OrderedMap]s are equal only when they contain the same
 * key-value pairs in the same iteration order.
 */
class OrderedMap<K, V> internal constructor(
    internal val inner: SmallMap<K, V>,
) : Iterable<Pair<K, V>>, Comparable<OrderedMap<K, V>> {

    companion object {
        /** Create a new empty map. */
        fun <K, V> new(): OrderedMap<K, V> = OrderedMap(SmallMap.new())

        /** Create a new empty map with the specified capacity. */
        fun <K, V> withCapacity(capacity: Int): OrderedMap<K, V> =
            OrderedMap(SmallMap.withCapacity(capacity))

        /** Create a default (empty) [OrderedMap]. Corresponds to Rust `Default` impl. */
        fun <K, V> default(): OrderedMap<K, V> = OrderedMap(SmallMap.new())

        /**
         * Create an [OrderedMap] from an iterable of key-value pairs.
         * Corresponds to Rust `FromIterator` impl.
         */
        fun <K, V> fromIterator(iter: Iterable<Pair<K, V>>): OrderedMap<K, V> {
            val map = SmallMap.new<K, V>()
            for ((k, v) in iter) {
                map.insert(k, v)
            }
            return OrderedMap(map)
        }

        /**
         * Create an [OrderedMap] from a [SmallMap].
         * Corresponds to Rust `From<SmallMap<K, V>>` impl.
         */
        fun <K, V> from(map: SmallMap<K, V>): OrderedMap<K, V> = OrderedMap(map)
    }

    /** Get the number of elements in the map. */
    fun len(): Int = inner.len()

    /** Check if the map is empty. */
    fun isEmpty(): Boolean = inner.isEmpty()

    /** Iterate over the entries as (key, value) pairs. */
    fun iter(): Sequence<Pair<K, V>> = inner.iter()

    /** Iterate over the entries with hashed keys. */
    fun iterHashed(): Sequence<Pair<Hashed<K>, V>> = inner.iterHashed()

    /** Iterate over the keys. */
    fun keys(): Sequence<K> = inner.keys()

    /** Iterate over the values. */
    fun values(): Sequence<V> = inner.values()

    /**
     * Get a reference to the value associated with the given key.
     * Corresponds to Rust `get<Q>(&self, k: &Q) -> Option<&V>`.
     */
    fun get(key: K): V? = inner.get(key)

    /**
     * Get a reference to the value associated with the given key using [Equivalent].
     * Corresponds to Rust `get<Q>(&self, k: &Q) -> Option<&V>` with `Q: Equivalent<K>`.
     */
    fun <Q> get(key: Q): V? where Q : Equivalent<K> = inner.get(key)

    /** Find an entry by an index. */
    fun getIndex(index: Int): Pair<K, V>? = inner.getIndex(index)

    /**
     * Find an entry index for a given key.
     * Corresponds to Rust `get_index_of<Q>(&self, key: &Q) -> Option<usize>`.
     */
    fun getIndexOf(key: K): Int? = inner.getIndexOf(key)

    /** Find an entry index for a given key using [Equivalent]. */
    fun <Q> getIndexOf(key: Q): Int? where Q : Equivalent<K> = inner.getIndexOf(key)

    /**
     * Check if the map contains the given key.
     * Corresponds to Rust `contains_key<Q>(&self, k: &Q) -> bool`.
     */
    fun containsKey(key: K): Boolean = inner.getIndexOf(key) != null

    /** Check if the map contains the given key using [Equivalent]. */
    fun <Q> containsKey(key: Q): Boolean where Q : Equivalent<K> = inner.getIndexOf(key) != null

    /**
     * Insert an entry into the map. Returns the previous value if the key existed.
     * Corresponds to Rust `insert(&mut self, k: K, v: V) -> Option<V>`.
     */
    fun insert(key: K, value: V): V? = inner.insert(key, value)

    /**
     * Remove an entry by key. Uses shift-remove to preserve iteration order.
     * Corresponds to Rust `remove<Q>(&mut self, k: &Q) -> Option<V>`.
     */
    fun remove(key: K): V? = inner.shiftRemove(key)

    /** Remove an entry by key using [Equivalent]. Uses shift-remove to preserve iteration order. */
    fun <Q> remove(key: Q): V? where Q : Equivalent<K> = inner.shiftRemove(key)

    /** Clear the map. */
    fun clear() = inner.clear()

    /**
     * Sort the map by keys.
     * Corresponds to Rust `sort_keys(&mut self) where K: Ord`.
     */
    @Suppress("UNCHECKED_CAST")
    fun sortKeys() {
        inner.entries.sortWith(compareBy { it.key.key() as Comparable<Any> })
    }

    /**
     * Extend the map with entries from an iterable.
     * Corresponds to Rust `Extend<(K, V)>` impl.
     */
    fun extend(iter: Iterable<Pair<K, V>>) {
        for ((k, v) in iter) {
            inner.insert(k, v)
        }
    }

    /** Corresponds to Rust `IntoIterator` impl. */
    override fun iterator(): Iterator<Pair<K, V>> = inner.iterator()

    /**
     * Ordered equality: two [OrderedMap]s are equal iff they contain the same entries
     * in the same iteration order.
     * Corresponds to Rust `PartialEq` impl using `eq_ordered`.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OrderedMap<*, *>) return false
        if (len() != other.len()) return false
        val thisIter = iter().iterator()
        val otherIter = other.iter().iterator()
        while (thisIter.hasNext()) {
            if (!otherIter.hasNext()) return false
            val (k, v) = thisIter.next()
            val (ok, ov) = otherIter.next()
            if (k != ok || v != ov) return false
        }
        return true
    }

    /**
     * Hash based on ordered iteration of entries.
     * Corresponds to Rust `Hash` impl using `hash_ordered`.
     *
     * In Rust, `hash_ordered` hashes only the values (the keys are already
     * incorporated via the Hashed wrapper's hash). In Kotlin we hash both
     * key and value in iteration order to remain consistent with [equals].
     */
    override fun hashCode(): Int {
        var result = 1
        for ((k, v) in iter()) {
            result = 31 * result + (k?.hashCode() ?: 0)
            result = 31 * result + (v?.hashCode() ?: 0)
        }
        return result
    }

    /**
     * Compare two [OrderedMap]s lexicographically by their iteration order.
     * Corresponds to Rust `PartialOrd` / `Ord` impls.
     */
    override fun compareTo(other: OrderedMap<K, V>): Int {
        val thisIter = iter().iterator()
        val otherIter = other.iter().iterator()
        while (thisIter.hasNext() && otherIter.hasNext()) {
            val (tk, tv) = thisIter.next()
            val (ok, ov) = otherIter.next()
            @Suppress("UNCHECKED_CAST")
            val keyCmp = (tk as Comparable<K>).compareTo(ok)
            if (keyCmp != 0) return keyCmp
            @Suppress("UNCHECKED_CAST")
            val valCmp = (tv as Comparable<V>).compareTo(ov)
            if (valCmp != 0) return valCmp
        }
        return when {
            thisIter.hasNext() -> 1
            otherIter.hasNext() -> -1
            else -> 0
        }
    }

    override fun toString(): String {
        return iter().joinToString(", ", "{", "}") { (k, v) -> "$k=$v" }
    }
}
