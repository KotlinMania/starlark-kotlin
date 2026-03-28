// port-lint: source src/sorted_map.rs
package starlark_map.sorted_map

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

import starlark_map.Equivalent
import starlark_map.ordered_map.OrderedMap
import starlark_map.small_map.SmallMap

/**
 * [OrderedMap] but with keys sorted.
 *
 * Corresponds to Rust `SortedMap<K, V>`.
 */
class SortedMap<K, V> internal constructor(
    private val map: OrderedMap<K, V>,
) : Iterable<Pair<K, V>> {

    companion object {
        /** Construct an empty [SortedMap]. */
        fun <K, V> new(): SortedMap<K, V> where K : Comparable<K> =
            SortedMap(OrderedMap.new())

        /** Create a default (empty) [SortedMap]. Corresponds to Rust `Default` impl. */
        fun <K, V> default(): SortedMap<K, V> where K : Comparable<K> = new()

        /**
         * Create a [SortedMap] from an iterable of key-value pairs.
         * Corresponds to Rust `FromIterator` impl.
         */
        fun <K, V> fromIterator(iter: Iterable<Pair<K, V>>): SortedMap<K, V> where K : Comparable<K> {
            val map = OrderedMap.fromIterator(iter)
            return from(map)
        }

        /** Create a [SortedMap] from an [OrderedMap]. Corresponds to Rust `From<OrderedMap<K, V>>`. */
        fun <K, V> from(map: OrderedMap<K, V>): SortedMap<K, V> where K : Comparable<K> {
            map.sortKeys()
            return SortedMap(map)
        }

        /** Create a [SortedMap] from a [SmallMap]. Corresponds to Rust `From<SmallMap<K, V>>`. */
        fun <K, V> from(map: SmallMap<K, V>): SortedMap<K, V> where K : Comparable<K> =
            from(OrderedMap.from(map))
    }

    /** Iterate over the entries. */
    fun iter(): Sequence<Pair<K, V>> = map.iter()

    /** Iterate over the keys. */
    fun keys(): Sequence<K> = map.keys()

    /** Iterate over the values. */
    fun values(): Sequence<V> = map.values()

    /** Return the number of elements in the map. */
    fun len(): Int = map.len()

    /** Check if the map is empty. */
    fun isEmpty(): Boolean = map.isEmpty()

    /**
     * Get a reference to the value associated with the given key.
     * Corresponds to Rust `get<Q>(&self, key: &Q) -> Option<&V>`.
     */
    fun get(key: K): V? = map.get(key)

    /** Get a reference to the value using [Equivalent]. */
    fun <Q> get(key: Q): V? where Q : Equivalent<K> = map.get(key)

    /**
     * Check if the map contains the given key.
     * Corresponds to Rust `contains_key<Q>(&self, k: &Q) -> bool`.
     */
    fun containsKey(key: K): Boolean = map.containsKey(key)

    /** Check if the map contains the given key using [Equivalent]. */
    fun <Q> containsKey(key: Q): Boolean where Q : Equivalent<K> = map.containsKey(key)

    /** Iterate over the map with hashes. */
    fun iterHashed() = map.iterHashed()

    override fun iterator(): Iterator<Pair<K, V>> = map.iterator()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SortedMap<*, *>) return false
        return map == other.map
    }

    override fun hashCode(): Int = map.hashCode()

    override fun toString(): String = map.toString()
}
