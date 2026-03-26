// port-lint: source src/collections/symbol/map.rs
package io.github.kotlinmania.starlark_kotlin.collections.symbol.map

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
 * An optimised string HashMap which goes even faster when the keys can
 * be pre-hashed or otherwise precomputed.
 *
 * The two bottlenecks in our use of these hash tables are computing the hashes and comparing
 * the resulting keys for equality. We precompute the hashes. We also use word-aligned comparison
 * when possible. We use the Starlark SmallHash hashes, promoted by IdHasher,
 * so we can reuse a SmallMap hash.
 *
 * Benchmarks on which the word-aligned choice was made (mac/linux, all in ns):
 * ```
 *                            8 bytes       32 bytes      64 bytes
 *  slice equality (memcmp)   3.5/3.8       3.5/ 3.0      4.5/ 4.7
 *  usize equality loop       1.0/1.4       2.7/ 3.5      3.5/ 6.0
 *  u8 equality loop          3.4/5.7      13.7/19.7     22.6/44.8
 * ```
 *
 * Measuring some sample strings, the P50 = 21 bytes, P75 = 27, P95 = 35,
 * so we can reasonably expect to hit the smaller cases most often.
 */

import io.github.kotlinmania.starlark_kotlin.collections.symbol.Symbol

/**
 * A map from [Symbol] to values, using pre-computed hashes for fast lookup.
 *
 * In Rust, this wraps `HashTable<(Symbol, T)>` for raw hash table access.
 * In Kotlin, we use a [LinkedHashMap] for insertion-order iteration.
 */
// #[derive(Clone, Trace, Allocative)]
// pub(crate) struct SymbolMap<T>(HashTable<(Symbol, T)>)
internal class SymbolMap<T> private constructor(
    private val table: LinkedHashMap<String, Pair<Symbol, T>>,
) {
    // impl SymbolMap

    constructor() : this(LinkedHashMap())

    constructor(capacity: Int) : this(LinkedHashMap(capacity))

    // pub(crate) fn insert(&mut self, key: &str, value: T) -> Option<T>
    fun insert(key: String, value: T): T? {
        val symbol = Symbol(key)
        val old = table.put(key, Pair(symbol, value))
        return old?.second
    }

    /** Look up by [Symbol]. */
    // pub(crate) fn get(&self, key: &Symbol) -> Option<&T>
    fun get(key: Symbol): T? {
        return table[key.asStr()]?.second
    }

    /** Look up by string. */
    // pub(crate) fn get_str(&self, key: &str) -> Option<&T>
    fun getStr(key: String): T? {
        return table[key]?.second
    }

    /** Number of entries. */
    // pub(crate) fn len(&self) -> usize
    fun len(): Int = table.size

    /** Iterate over (Symbol, T) pairs. */
    // pub(crate) fn iter(&self) -> impl ExactSizeIterator<Item = &(Symbol, T)>
    fun iter(): List<Pair<Symbol, T>> = table.values.toList()

    /** Iterate over keys. */
    // pub(crate) fn keys(&self) -> impl ExactSizeIterator<Item = &Symbol>
    fun keys(): List<Symbol> = table.values.map { it.first }

    /** Iterate over values. */
    // pub(crate) fn values(&self) -> impl ExactSizeIterator<Item = &T>
    fun values(): List<T> = table.values.map { it.second }

    // impl Debug for SymbolMap<T>
    override fun toString(): String {
        return table.values.joinToString(prefix = "{", postfix = "}") { (k, v) ->
            "$k: $v"
        }
    }
}
