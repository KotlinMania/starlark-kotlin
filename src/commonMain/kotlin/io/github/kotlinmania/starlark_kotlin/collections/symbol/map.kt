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

/// An optimised string HashMap which goes even faster when the keys can
/// be pre-hashed or otherwise precomputed.
///
/// The two bottlenecks in our use of these hash tables are computing the hashes and comparing
/// the resulting keys for equality. We precompute the hashes. We also use `[usize]` to do faster
/// comparison when possible. We use the Starlark SmallHash hashes, promoted by IdHasher,
/// so we can reuse a SmallMap hash.

// We use a RawTable (the thing that underlies HashMap) so we can look up efficiently
// and easily by Symbol and str, without being limited by `Borrow` traits.
// #[derive(Clone, Trace, Allocative)]
// pub(crate) struct SymbolMap<T>(HashTable<(Symbol, T)>);
// Kotlin: backed by a LinkedHashMap<Symbol, T>. HashMap already handles hashing.
internal class SymbolMap<T> private constructor(
    private val table: LinkedHashMap<Symbol, T>,
) : Iterable<Pair<Symbol, T>> {

    // pub(crate) fn new() -> Self
    constructor() : this(LinkedHashMap())

    // pub(crate) fn with_capacity(capacity: usize) -> Self
    constructor(capacity: Int) : this(LinkedHashMap(capacity))

    // impl Debug for SymbolMap<T>
    override fun toString(): String {
        return table.entries.joinToString(prefix = "{", postfix = "}") { (k, v) -> "$k: $v" }
    }

    // pub(crate) fn insert(&mut self, key: &str, value: T) -> Option<T>
    fun insert(key: String, value: T): T? {
        val s = Symbol.new(key)
        return table.put(s, value)
    }

    // pub(crate) fn get(&self, key: &Symbol) -> Option<&T>
    fun get(key: Symbol): T? {
        return table[key]
    }

    // pub(crate) fn get_str(&self, key: &str) -> Option<&T>
    fun getStr(key: String): T? {
        return get(Symbol.new(key))
    }

    // pub(crate) fn get_hashed_str(&self, key: Hashed<&str>) -> Option<&T>
    fun getHashedStr(key: String): T? {
        return getStr(key)
    }

    // pub(crate) fn get_hashed_string_value(&self, key: Hashed<StringValue>) -> Option<&T>
    fun getHashedStringValue(key: String): T? {
        return getStr(key)
    }

    // pub(crate) fn len(&self) -> usize
    fun len(): Int = table.size

    // pub(crate) fn iter(&self) -> impl ExactSizeIterator<Item = &(Symbol, T)>
    override fun iterator(): Iterator<Pair<Symbol, T>> {
        return table.entries.map { (k, v) -> Pair(k, v) }.iterator()
    }

    // pub(crate) fn keys(&self) -> impl ExactSizeIterator<Item = &Symbol>
    fun keys(): Collection<Symbol> = table.keys

    // pub(crate) fn values(&self) -> impl ExactSizeIterator<Item = &T>
    fun values(): Collection<T> = table.values
}

// Placeholder for Symbol until symbol.rs is fully ported
internal class Symbol private constructor(
    private val name: String,
    private val hash: Int,
) {
    companion object {
        fun new(key: String): Symbol {
            return Symbol(key, key.hashCode())
        }
    }

    fun asStr(): String = name

    fun hash(): Int = hash

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Symbol) return false
        return hash == other.hash && name == other.name
    }

    override fun hashCode(): Int = hash

    override fun toString(): String = name
}
