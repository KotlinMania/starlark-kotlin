// port-lint: source src/values/comparison.rs
package io.github.kotlinmania.starlark_kotlin.values

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

import io.github.kotlinmania.starlark_kotlin.collections.SmallMap
import io.github.kotlinmania.starlark_kotlin.values.layout.avalue.size
import io.github.kotlinmania.starlark_kotlin.values.layout.size

// pub(crate) fn equals_slice<E, X1, X2>(xs, ys, f) -> Result<bool, E>
internal fun <E : Exception, X1, X2> equalsSlice(
    xs: List<X1>,
    ys: List<X2>,
    f: (X1, X2) -> Result<Boolean>,
): Result<Boolean> {
    if (xs.size != ys.size) {
        return Result.success(false)
    }
    for (i in xs.indices) {
        val eq = f(xs[i], ys[i]).getOrElse { return Result.failure(it) }
        if (!eq) {
            return Result.success(false)
        }
    }
    return Result.success(true)
}

// pub(crate) fn equals_small_map<E, K1, K2, V1, V2>(x, y, f) -> Result<bool, E>
internal fun <E : Exception, K, V1, V2> equalsSmallMap(
    x: SmallMap<K, V1>,
    y: SmallMap<K, V2>,
    f: (V1, V2) -> Result<Boolean>,
): Result<Boolean> {
    if (x.size != y.size) {
        return Result.success(false)
    }
    for ((xk, xv) in x) {
        val yv = y[xk] ?: return Result.success(false)
        val eq = f(xv, yv).getOrElse { return Result.failure(it) }
        if (!eq) {
            return Result.success(false)
        }
    }
    return Result.success(true)
}

// pub(crate) fn equals_small_set<K1, K2>(xs, ys) -> bool
internal fun <K> equalsSmallSet(xs: Set<K>, ys: Set<K>): Boolean {
    if (xs.size != ys.size) {
        return false
    }
    for (x in xs) {
        if (x !in ys) {
            return false
        }
    }
    return true
}

// pub(crate) fn compare_slice<E, X1, X2>(xs, ys, f) -> Result<Ordering, E>
internal fun <E : Exception, X1, X2> compareSlice(
    xs: List<X1>,
    ys: List<X2>,
    f: (X1, X2) -> Result<Int>,
): Result<Int> {
    for (i in 0 until minOf(xs.size, ys.size)) {
        val cmp = f(xs[i], ys[i]).getOrElse { return Result.failure(it) }
        if (cmp != 0) {
            return Result.success(cmp)
        }
    }
    return Result.success(xs.size.compareTo(ys.size))
}

// pub(crate) fn compare_small_map<E, K, K2: Ord + Hash, V1, V2>(x, y, key, f) -> Result<Ordering, E>
internal fun <E : Exception, K, K2 : Comparable<K2>, V1, V2> compareSmallMap(
    x: SmallMap<K, V1>,
    y: SmallMap<K, V2>,
    key: (K) -> K2,
    f: (V1, V2) -> Result<Int>,
): Result<Int> {
    val cmp = x.size.compareTo(y.size)
    if (cmp != 0) {
        return Result.success(cmp)
    }
    val xSorted = x.entries.sortedBy { (k, _) -> key(k) }
    val ySorted = y.entries.sortedBy { (k, _) -> key(k) }
    for (i in xSorted.indices) {
        val (xk, xv) = xSorted[i]
        val (yk, yv) = ySorted[i]
        val keyCmp = key(xk).compareTo(key(yk))
        if (keyCmp != 0) {
            return Result.success(keyCmp)
        }
        val valueCmp = f(xv, yv).getOrElse { return Result.failure(it) }
        if (valueCmp != 0) {
            return Result.success(valueCmp)
        }
    }
    return Result.success(0)
}
