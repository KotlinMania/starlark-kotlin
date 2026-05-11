// port-lint: source src/small_set/iter.rs
package io.github.kotlinmania.starlark_kotlin.collections.small_set.iter

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

import io.github.kotlinmania.starlark_kotlin.collections.Hashed
import io.github.kotlinmania.starlark_kotlin.collections.small_map.iter.Iter as SmallMapIter
import io.github.kotlinmania.starlark_kotlin.collections.small_map.iter.IterMutUnchecked as SmallMapIterMutUnchecked
import io.github.kotlinmania.starlark_kotlin.collections.small_map.iter.IterHashed as SmallMapIterHashed
import io.github.kotlinmania.starlark_kotlin.collections.small_map.iter.IntoIter as SmallMapIntoIter
import io.github.kotlinmania.starlark_kotlin.collections.small_map.iter.IntoIterHashed as SmallMapIntoIterHashed

/**
 * Iterator types for [SmallSet][io.github.kotlinmania.starlark_kotlin.collections.small_set.SmallSet].
 *
 * In Rust, these are thin wrappers around [small_map] iterators that
 * discard the `()` value component. In Kotlin, they wrap the
 * corresponding small_map iterators and extract just the key.
 */

/** Iterator over entries of [SmallSet]. Corresponds to Rust `Iter<'a, T>`. */
internal class Iter<T>(
    internal val iter: SmallMapIter<T, Unit>,
) : Iterator<T> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): T = iter.next().first
    fun len(): Int = iter.len()
}

/**
 * Iterator over mutable entries of [SmallSet]. Corresponds to Rust `IterMutUnchecked<'a, T>`.
 *
 * Kotlin does not have mutable references, so this is functionally equivalent to [Iter].
 */
internal class IterMutUnchecked<T>(
    internal val iter: SmallMapIterMutUnchecked<T, Unit>,
) : Iterator<T> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): T = iter.next().first
    fun len(): Int = iter.len()
}

/** Iterator over hashed entries of [SmallSet]. Corresponds to Rust `IterHashed<'a, T>`. */
internal class IterHashed<T>(
    internal val iter: SmallMapIterHashed<T, Unit>,
) : Iterator<Hashed<T>> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): Hashed<T> = iter.next().first
    fun len(): Int = iter.len()
}

/** Iterator that moves entries out of [SmallSet]. Corresponds to Rust `IntoIter<T>`. */
internal class IntoIter<T>(
    internal val iter: SmallMapIntoIter<T, Unit>,
) : Iterator<T> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): T = iter.next().first
    fun len(): Int = iter.len()
}

/** Iterator that moves hashed entries out of [SmallSet]. Corresponds to Rust `IntoIterHashed<T>`. */
internal class IntoIterHashed<T>(
    internal val iter: SmallMapIntoIterHashed<T, Unit>,
) : Iterator<Hashed<T>> {
    override fun hasNext(): Boolean = iter.hasNext()
    override fun next(): Hashed<T> = iter.next().first
    fun len(): Int = iter.len()
}
