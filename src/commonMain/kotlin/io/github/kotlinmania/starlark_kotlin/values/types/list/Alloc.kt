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
import io.github.kotlinmania.starlark_kotlin.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap

/**
 * Utility to allocate a list from iterator.
 *
 * # Example
 *
 * ```kotlin
 * val l = heap.alloc(AllocList(listOf(1, 2, 3)))
 * val ls = frozenHeap.alloc(AllocList(listOf(1, 2, 3)))
 * ```
 */
class AllocList<L>(val items: L) {
    companion object {
        /** Allocate an empty list. */
        val EMPTY: AllocList<List<FrozenValue>> = AllocList(emptyList())
    }
}

// impl StarlarkTypeRepr for AllocList<L>
// where L: IntoIterator, L::Item: StarlarkTypeRepr
fun <L, Item : StarlarkTypeRepr> AllocList<L>.starlarkTypeRepr(): Ty
    where L : Iterable<Item> {
    return Ty.anyList()
}

// impl AllocValue for AllocList<L>
// where L: IntoIterator, L::Item: AllocValue
fun <L, Item : AllocValue> AllocList<L>.allocValue(heap: Heap): Value
    where L : Iterable<Item> {
    return heap.alloc(AllocList(items.map { x -> x.allocValue(heap) }))
}

// impl AllocFrozenValue for AllocList<L>
// where L: IntoIterator, L::Item: AllocFrozenValue
fun <L, Item : AllocFrozenValue> AllocList<L>.allocFrozenValue(heap: FrozenHeap): FrozenValue
    where L : Iterable<Item> {
    return heap.alloc(AllocList(items.map { x -> x.allocFrozenValue(heap) }))
}
