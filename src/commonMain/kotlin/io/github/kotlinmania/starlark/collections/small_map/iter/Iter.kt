// port-lint: source src/small_map/iter.rs
package io.github.kotlinmania.starlark.collections.small_map.iter

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

import io.github.kotlinmania.starlark.collections.Hashed
import io.github.kotlinmania.starlark.collections.vec_map.iter.IterHashed as VecMapIterHashed
import io.github.kotlinmania.starlark.collections.vec_map.iter.Iter as VecMapIter
import io.github.kotlinmania.starlark.collections.vec_map.iter.IntoIterHashed as VecMapIntoIterHashed
import io.github.kotlinmania.starlark.collections.vec_map.iter.IntoIter as VecMapIntoIter
import io.github.kotlinmania.starlark.collections.vec_map.iter.Keys as VecMapKeys
import io.github.kotlinmania.starlark.collections.vec_map.iter.Values as VecMapValues

/**
 * Iterator types for [SmallMap][io.github.kotlinmania.starlark.collections.SmallMap].
 *
 * In Rust, these are thin wrappers around vec_map iterators with
 * `def_iter!()` and `def_double_ended_iter!()` macro expansions.
 * In Kotlin, they delegate to the corresponding vec_map iterators.
 */

/** Iterator over hashed entries of [SmallMap]. Corresponds to Rust `IterHashed<'a, K, V>`. */
internal class IterHashed<K, V>(
    internal val iter: VecMapIterHashed<K, V>,
) : Iterator<Pair<Hashed<K>, V>> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): Pair<Hashed<K>, V> = iter.next()
    fun len(): Int = iter.len()
}

/** Iterator over entry references. Corresponds to Rust `Iter<'a, K, V>`. */
internal class Iter<K, V>(
    internal val iter: VecMapIter<K, V>,
) : Iterator<Pair<K, V>> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): Pair<K, V> = iter.next()
    fun len(): Int = iter.len()
}

/**
 * Iterator over mutable entry references. Corresponds to Rust `IterMut<'a, K, V>`.
 *
 * Kotlin does not have mutable references, so this is functionally equivalent to [Iter].
 */
internal class IterMut<K, V>(
    internal val iter: VecMapIter<K, V>,
) : Iterator<Pair<K, V>> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): Pair<K, V> = iter.next()
    fun len(): Int = iter.len()
}

/**
 * Iterator over mutable entry references (unchecked key mutation).
 * Corresponds to Rust `IterMutUnchecked<'a, K, V>`.
 *
 * Kotlin does not have mutable references, so this is functionally equivalent to [Iter].
 */
internal class IterMutUnchecked<K, V>(
    internal val iter: VecMapIter<K, V>,
) : Iterator<Pair<K, V>> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): Pair<K, V> = iter.next()
    fun len(): Int = iter.len()
}

/** Iterator that moves hashed entries out of [SmallMap]. Corresponds to Rust `IntoIterHashed<K, V>`. */
internal class IntoIterHashed<K, V>(
    internal val iter: VecMapIntoIterHashed<K, V>,
) : Iterator<Pair<Hashed<K>, V>> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): Pair<Hashed<K>, V> = iter.next()
    fun len(): Int = iter.len()
}

/** Iterator that moves entries out of [SmallMap]. Corresponds to Rust `IntoIter<K, V>`. */
internal class IntoIter<K, V>(
    internal val iter: VecMapIntoIter<K, V>,
) : Iterator<Pair<K, V>> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): Pair<K, V> = iter.next()
    fun len(): Int = iter.len()
}

/** Iterator over [SmallMap] keys. Corresponds to Rust `Keys<'a, K, V>`. */
internal class Keys<K, V>(
    internal val iter: VecMapKeys<K, V>,
) : Iterator<K> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): K = iter.next()
}

/** Iterator over [SmallMap] values. Corresponds to Rust `Values<'a, K, V>`. */
internal class Values<K, V>(
    internal val iter: VecMapValues<K, V>,
) : Iterator<V> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): V = iter.next()
}

/** Iterator that moves keys out of [SmallMap]. Corresponds to Rust `IntoKeys<K, V>`. */
internal class IntoKeys<K, V>(
    internal val iter: VecMapIntoIter<K, V>,
) : Iterator<K> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): K = iter.next().first
    fun len(): Int = iter.len()
}

/** Iterator that moves values out of [SmallMap]. Corresponds to Rust `IntoValues<K, V>`. */
internal class IntoValues<K, V>(
    internal val iter: VecMapIntoIter<K, V>,
) : Iterator<V> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): V = iter.next().second
    fun len(): Int = iter.len()
}

/**
 * Iterator over [SmallMap] mutable values. Corresponds to Rust `ValuesMut<'a, K, V>`.
 *
 * Kotlin does not have mutable references, so this is functionally equivalent to [Values].
 */
internal class ValuesMut<K, V>(
    internal val iter: VecMapValues<K, V>,
) : Iterator<V> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): V = iter.next()
}
