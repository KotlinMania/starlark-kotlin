// port-lint: source src/values/types/list/alloc.rs
package io.github.kotlinmania.starlark_kotlin.values.types.list

/*
 * Copyright 2018 The Starlark in Rust Authors.
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

import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.AllocFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.AllocValue
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.allocListIter

/**
 * Utility to allocate a list from iterator.
 *
 * # Example
 *
 * ```
 * use starlark::values::list::AllocList;
 *
 * # use starlark::values::{FrozenHeap, Heap};
 * # fn alloc(heap: Heap<'_>, frozen_heap: &FrozenHeap) {
 * let l = heap.alloc(AllocList([1, 2, 3]));
 * let ls = frozen_heap.alloc(AllocList([1, 2, 3]));
 * # }
 * ```
 */
data class AllocList<L>(val value: L) {
    companion object {
        /**
         * Allocate an empty list.
         */
        val EMPTY: AllocList<Sequence<FrozenValue>> = AllocList(emptySequence())
    }
}

/**
 * Implementation of StarlarkTypeRepr for AllocList<L>
 * where L: IntoIterator, L::Item: StarlarkTypeRepr.
 */
inline fun <reified L, reified Item> allocListStarlarkTypeRepr(): Ty
    where Item : StarlarkTypeRepr {
    return listStarlarkTypeRepr<Item>()
}

/**
 * Implementation of AllocValue for AllocList<L>
 * where L: IntoIterator, L::Item: AllocValue<V_>.
 */
fun <V, L, Item> AllocList<L>.allocValue(heap: Heap<V>): Value<V>
    where L : Iterable<Item>,
          Item : AllocValue<V> {
    return heap.allocListIter(value.asSequence().map { x -> x.allocValue(heap) })
}

/**
 * Implementation of AllocFrozenValue for AllocList<L>
 * where L: IntoIterator, L::Item: AllocFrozenValue.
 */
fun <L, Item> AllocList<L>.allocFrozenValue(heap: FrozenHeap): FrozenValue
    where L : Iterable<Item>,
          Item : AllocFrozenValue {
    return heap.allocListIter(value.asSequence().map { x -> x.allocFrozenValue(heap) })
}
