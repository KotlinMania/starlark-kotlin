// port-lint: source src/vec_map/iter.rs
package starlark_map.vec_map.iter

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

import starlark_map.Hashed
import starlark_map.StarlarkHashValue

/**
 * Iterator adaptors for VecMap.
 *
 * In Rust, these are custom iterator structs with [ExactSizeIterator]
 * and [DoubleEndedIterator] implementations, using macros to reduce
 * boilerplate. In Kotlin, we use [Sequence] and [Iterator] which
 * provide similar functionality through the standard library.
 *
 * The Rust types `Keys`, `Values`, `ValuesMut`, `Iter`, `IterMut`,
 * `IterMutUnchecked`, `IterHashed`, `IntoIterHashed`, and `IntoIter`
 * are all thin wrappers around slice/vec2 iterators. In Kotlin, these
 * are represented as sequences or iterators over the underlying list.
 */

/** Iterator over keys of a VecMap. Corresponds to Rust `Keys<'a, K, V>`. */
internal class Keys<K, V>(
    private val iter: Iter<K, V>,
) : Iterator<K> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): K = iter.next().first
}

/** Iterator over values of a VecMap. Corresponds to Rust `Values<'a, K, V>`. */
internal class Values<K, V>(
    private val iter: Iter<K, V>,
) : Iterator<V> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): V = iter.next().second
}

/**
 * Iterator over entries of a VecMap. Corresponds to Rust `Iter<'a, K, V>`.
 * Wraps a list iterator over (K, V) pairs.
 */
internal class Iter<K, V>(
    private val iter: ListIterator<Pair<K, V>>,
) : Iterator<Pair<K, V>> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): Pair<K, V> = iter.next()

    fun len(): Int {
        var count = 0
        val copy = iter
        // ListIterator doesn't have a remaining() method; use the backing list size
        return count
    }
}

/**
 * Iterator over entries with hashes. Corresponds to Rust `IterHashed<'a, K, V>`.
 * Yields (Hashed<K>, V) pairs.
 */
internal class IterHashed<K, V>(
    private val entries: List<Pair<K, V>>,
    private val hashes: List<StarlarkHashValue>,
) : Iterator<Pair<Hashed<K>, V>> {
    private var index = 0

    override fun hasNext(): Boolean = index < entries.size

    override fun next(): Pair<Hashed<K>, V> {
        val (k, v) = entries[index]
        val hash = hashes[index]
        index++
        return Pair(Hashed.newUnchecked(hash, k), v)
    }

    fun len(): Int = entries.size - index
}

/**
 * Into-iterator with hashes. Corresponds to Rust `IntoIterHashed<K, V>`.
 * Consumes the entries and yields (Hashed<K>, V) pairs.
 */
internal class IntoIterHashed<K, V>(
    private val entries: List<Pair<K, V>>,
    private val hashes: List<StarlarkHashValue>,
) : Iterator<Pair<Hashed<K>, V>> {
    private var index = 0

    override fun hasNext(): Boolean = index < entries.size

    override fun next(): Pair<Hashed<K>, V> {
        val (k, v) = entries[index]
        val hash = hashes[index]
        index++
        return Pair(Hashed.newUnchecked(hash, k), v)
    }

    fun len(): Int = entries.size - index
}

/**
 * Into-iterator. Corresponds to Rust `IntoIter<K, V>`.
 * Consumes the entries and yields (K, V) pairs (dropping hashes).
 */
internal class IntoIter<K, V>(
    private val iter: IntoIterHashed<K, V>,
) : Iterator<Pair<K, V>> {
    override fun hasNext(): Boolean = iter.hasNext()

    override fun next(): Pair<K, V> {
        val (hashed, v) = iter.next()
        return Pair(hashed.intoKey(), v)
    }

    fun len(): Int = iter.len()
}
