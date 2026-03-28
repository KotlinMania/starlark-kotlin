// port-lint: source src/small_set.rs
package starlark_map.small_set

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
import starlark_map.Hashed

/**
 * A set with deterministic iteration order.
 */
class SmallSet<T> private constructor(
    private val entries: ArrayList<Hashed<T>>,
) {
    constructor() : this(ArrayList())

    companion object {
        fun <T> withCapacity(n: Int): SmallSet<T> = SmallSet(ArrayList(n))
    }

    fun isEmpty(): Boolean = entries.isEmpty()

    fun len(): Int = entries.size

    fun clear() {
        entries.clear()
    }

    fun iter(): Sequence<T> = entries.asSequence().map { it.key() }

    fun iterHashed(): Sequence<Hashed<T>> = entries.asSequence()

    fun intoIterHashed(): Sequence<Hashed<T>> = iterHashed()

    fun addAll(values: Iterable<Hashed<T>>) {
        for (v in values) {
            insertHashed(v)
        }
    }

    fun containsHashed(key: Hashed<T>): Boolean {
        return entries.any { it == key }
    }

    fun <Q> containsHashed(key: Hashed<Q>): Boolean where Q : Equivalent<T> {
        val q = key.key()
        return entries.any { q.equivalent(it.key()) }
    }

    fun insertHashed(value: Hashed<T>): Boolean {
        if (containsHashed(value)) return false
        entries.add(value)
        return true
    }

    fun insertHashedUniqueUnchecked(value: Hashed<T>) {
        entries.add(value)
    }

    fun shiftRemoveHashed(value: Hashed<T>): Boolean {
        val index = entries.indexOfFirst { it == value }
        if (index < 0) return false
        entries.removeAt(index)
        return true
    }

    fun <Q> shiftRemoveHashed(value: Hashed<Q>): Boolean where Q : Equivalent<T> {
        val q = value.key()
        val index = entries.indexOfFirst { q.equivalent(it.key()) }
        if (index < 0) return false
        entries.removeAt(index)
        return true
    }
}

