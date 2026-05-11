<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/collections/symbol/Map.kt
// port-lint: source collections/symbol/map.rs
package io.github.kotlinmania.starlark.collections.symbol.map
=======
// port-lint: source src/collections/symbol/map.rs
package io.github.kotlinmania.starlark_kotlin.collections.symbol
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/collections/symbol/Map.kt

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
import io.github.kotlinmania.starlark_kotlin.collections.Hashed

/**
 * A symbol map backed by a hash table of [Symbol] keys.
 *
 * We use a flat hash table so we can look up efficiently and easily
 * by [Symbol] and string, without being limited by Kotlin's standard
 * map key constraints.
 */
class SymbolMap<T> private constructor(
    private val table: HashMap<ULong, MutableList<Pair<Symbol, T>>>,
    private var size: Int,
) {

    /** Create a new empty [SymbolMap]. */
    constructor() : this(HashMap(), 0)

    /** Create a new [SymbolMap] with the given [capacity]. */
    constructor(capacity: Int) : this(HashMap(capacity), 0)

    /** Debug formatting: produces `{key: value, ...}` representation. */
    override fun toString(): String {
        val entries = entries()
        return entries.joinToString(prefix = "{", postfix = "}") { (k, v) -> "$k: $v" }
    }

    /** Insert a key-value pair, returning the old value if the key was already present. */
    fun insert(key: String, value: T): T? {
        val s = Symbol.new(key)
        val hash = s.hash()
        val bucket = table.getOrPut(hash) { mutableListOf() }
        val idx = bucket.indexOfFirst { s == it.first }
        return if (idx >= 0) {
            val old = bucket[idx].second
            bucket[idx] = Pair(s, value)
            old
        } else {
            // This insert doesn't remove old values, so do that manually first
            bucket.add(Pair(s, value))
            size++
            null
        }
    }

    /** Look up a value by [Symbol] key. */
    fun get(key: Symbol): T? {
        val bucket = table[key.hash()] ?: return null
        return bucket.firstOrNull { key == it.first }?.second
    }

    /** Look up a value by string key. */
    fun getStr(key: String): T? {
        return getHashedStr(Hashed.new(key))
    }

    /** Look up a value by pre-hashed string key. */
    fun getHashedStr(key: Hashed<String>): T? {
        val hash = key.hash().promote()
        val bucket = table[hash] ?: return null
        return bucket.firstOrNull { it.first.asStr() == key.key() }?.second
    }

    /** Look up a value by pre-hashed string value, using aligned padded string comparison. */
    fun getHashedStringValue(key: Hashed<String>): T? {
        val hash = key.hash().promote()
        val bucket = table[hash] ?: return null
        val keyAligned = Symbol.new(key.key()).asAlignedPaddedStr()
        return bucket.firstOrNull { it.first.asAlignedPaddedStr() == keyAligned }?.second
    }

    /** Number of entries in the map. */
    fun len(): Int = size

    /** Iterate over all (Symbol, T) pairs. */
    fun iter(): List<Pair<Symbol, T>> = entries()

    /** Iterate over all keys. */
    fun keys(): List<Symbol> = entries().map { it.first }

    /** Iterate over all values. */
    fun values(): List<T> = entries().map { it.second }

    private fun entries(): List<Pair<Symbol, T>> = table.values.flatMap { it }
}
