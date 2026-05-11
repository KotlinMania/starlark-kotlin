// port-lint: source values/types/list/alloc.rs
package io.github.kotlinmania.starlark.values.types.list

/*
 * Copyright 2018 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not import this file except in compliance with the License.
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

import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.AllocFrozenValue
import io.github.kotlinmania.starlark.values.AllocValue
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.allocListIter
import io.github.kotlinmania.starlark.values.layout.heap.Heap

/**
 * Utility to allocate a list from an iterable.
 *
 * # Example
 *
 * ```kotlin
 * // Allocate on the mutable heap:
 * val l = heap.alloc(AllocList(listOf(1, 2, 3)))
 *
 * // Allocate on the frozen heap:
 * val ls = frozenHeap.alloc(AllocList(listOf(1, 2, 3)))
 * ```
 *
 * @param L The type of the backing iterable.
 * @property items The items to allocate into the new list.
 */
class AllocList<L>(val items: L) {
    /**
     * Companion holding the [EMPTY] constant.
     *
     * separate implementation block for the specific `Empty` iterator specialization.
     */
    companion object {
        /**
         * Allocate an empty list.
         *
         */
        val EMPTY: AllocList<List<FrozenValue>> = AllocList(emptyList())
    }
}


/** [StarlarkTypeRepr] for [AllocList]. */
fun <L, Item : StarlarkTypeRepr> AllocList<L>.starlarkTypeRepr(): Ty
    where L : Iterable<Item> {
    return Ty.anyList()
}


/**
 * Allocate this [AllocList] as a mutable [Value] on the given [heap].
 *
 * Each item is allocated individually via [AllocValue.allocValue], then
 * the resulting values are collected into a new list.
 *
 * where L: IntoIterator, L::Item: AllocValue`.
 */
fun <L, Item : AllocValue> AllocList<L>.allocValue(heap: Heap): Value
    where L : Iterable<Item> {
    // Map each item through AllocValue::allocValue, then collect into a list.
    val allocated = items.map { x -> x.allocValue(heap) }
    return heap.allocListIter(allocated)
}


/**
 * Allocate this [AllocList] as a [FrozenValue] on the given frozen [heap].
 *
 * Each item is allocated individually via [AllocFrozenValue.allocFrozenValue],
 * then the resulting values are collected into a new frozen list.
 *
 * where L: IntoIterator, L::Item: AllocFrozenValue`.
 */
fun <L, Item : AllocFrozenValue> AllocList<L>.allocFrozenValue(heap: FrozenHeap): FrozenValue
    where L : Iterable<Item> {
    // Map each item through AllocFrozenValue::allocFrozenValue, then collect.
    val allocated = items.map { x -> x.allocFrozenValue(heap) }
    return heap.allocListIter(allocated)
}
