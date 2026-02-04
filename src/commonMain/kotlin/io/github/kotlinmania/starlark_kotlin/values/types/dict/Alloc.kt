// port-lint: source src/values/types/dict/alloc.rs
package io.github.kotlinmania.starlark_kotlin.values.types.dict

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

import io.github.kotlinmania.starlark_kotlin.collections.SmallMap
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.AllocFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.AllocValue
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.Heap
import io.github.kotlinmania.starlark_kotlin.values.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.value.ValueLike
import io.github.kotlinmania.starlark_kotlin.values.typeRepr.StarlarkTypeRepr

/**
 * Utility to allocate a dict from iterator.
 *
 * Iterator must be a list of pairs (key, value).
 * Duplicate keys are allowed, last key wins.
 *
 * # Panics
 *
 * Panics if a key is not hashable.
 *
 * # Example
 *
 * ```
 * use starlark::values::dict::AllocDict;
 *
 * # use starlark::values::{FrozenHeap, Heap};
 * # fn alloc(heap: Heap<'_>, frozen_heap: &FrozenHeap) {
 * let l = heap.alloc(AllocDict([("a", 1), ("b", 2), ("c", 3)]));
 * let ls = frozen_heap.alloc(AllocDict([("a", 1), ("b", 2), ("c", 3)]));
 * # }
 * ```
 */
data class AllocDict<D>(val d: D)

/**
 * Implementation for AllocDict<iter::Empty<(FrozenValue, FrozenValue)>>
 */
object AllocDictEmpty {
    /**
     * Allocate an empty dict.
     */
    val EMPTY: AllocDict<Sequence<Pair<FrozenValue, FrozenValue>>> = AllocDict(emptySequence())
}

/**
 * Implementation of StarlarkTypeRepr for AllocDict<D>
 * where D: IntoIterator<Item = (K, V)>, K: StarlarkTypeRepr, V: StarlarkTypeRepr
 */
inline fun <reified D, reified K, reified V> allocDictStarlarkTypeRepr(): Ty
    where D : Iterable<Pair<K, V>>,
          K : StarlarkTypeRepr,
          V : StarlarkTypeRepr {
    return DictType.starlarkTypeRepr<K, V>()
}

/**
 * Implementation of AllocValue<'v> for AllocDict<D>
 * where D: IntoIterator<Item = (K, V)>, K: AllocValue<'v>, V: AllocValue<'v>
 */
fun <'v, D, K, V> AllocDict<D>.allocValue(heap: Heap<'v>): Value<'v>
    where D : Iterable<Pair<K, V>>,
          K : AllocValue<'v>,
          V : AllocValue<'v> {
    val iter = this.d.iterator()
    val map = SmallMap.withCapacity<Value<'v>, Value<'v>>((this.d as? Collection<*>)?.size ?: 0)
    for ((k, v) in iter) {
        map.insertHashed(
            k.allocValue(heap).getHashed()!!,
            v.allocValue(heap)
        )
    }
    return heap.alloc(Dict.new(map))
}

/**
 * Implementation of AllocFrozenValue for AllocDict<D>
 * where D: IntoIterator<Item = (K, V)>, K: AllocFrozenValue, V: AllocFrozenValue
 */
fun <D, K, V> AllocDict<D>.allocFrozenValue(heap: FrozenHeap): FrozenValue
    where D : Iterable<Pair<K, V>>,
          K : AllocFrozenValue,
          V : AllocFrozenValue {
    val iter = this.d.iterator()
    val map = SmallMap.withCapacity<FrozenValue, FrozenValue>((this.d as? Collection<*>)?.size ?: 0)
    for ((k, v) in iter) {
        map.insertHashed(
            k.allocFrozenValue(heap).getHashed()!!,
            v.allocFrozenValue(heap)
        )
    }
    return heap.alloc(FrozenDictData(content = map))
}
