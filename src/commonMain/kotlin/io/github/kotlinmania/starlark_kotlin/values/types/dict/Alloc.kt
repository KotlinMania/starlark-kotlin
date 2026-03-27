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
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.StarlarkTypeRepr

/**
 * Utility to allocate a dict from iterator.
 *
 * Iterator must be a list of pairs (key, value).
 * Duplicate keys are allowed, last key wins.
 *
 * Panics if a key is not hashable.
 *
 * Example:
 * ```
 * val l = heap.alloc(AllocDict(listOf("a" to 1, "b" to 2, "c" to 3)))
 * val ls = frozenHeap.alloc(AllocDict(listOf("a" to 1, "b" to 2, "c" to 3)))
 * ```
 */
data class AllocDict<D>(val d: D) {
    companion object {
        /** Allocate an empty dict. */
        val EMPTY: AllocDict<Sequence<Pair<FrozenValue, FrozenValue>>> = AllocDict(emptySequence())
    }
}

/** StarlarkTypeRepr for AllocDict. */
inline fun <reified K : StarlarkTypeRepr, reified V : StarlarkTypeRepr> allocDictStarlarkTypeRepr(): Ty =
    DictType.starlarkTypeRepr<K, V>()

/** AllocValue for AllocDict where D: Iterable<Pair<K, V>>, K: AllocValue, V: AllocValue. */
fun <D, K, V> AllocDict<D>.allocValue(heap: Heap): Value
    where D : Iterable<Pair<K, V>>,
          K : AllocValue,
          V : AllocValue {
    val iter = this.d.iterator()
    val map = SmallMap.withCapacity<Value, Value>((this.d as? Collection<*>)?.size ?: 0)
    for ((k, v) in iter) {
        map.insertHashed(
            k.allocValue(heap).getHashed()!!,
            v.allocValue(heap)
        )
    }
    return Dict.new(map).allocValue(heap)
}

/** AllocFrozenValue for AllocDict where D: Iterable<Pair<K, V>>, K: AllocFrozenValue, V: AllocFrozenValue. */
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
    return FrozenDictData(content = map).allocFrozenValue(heap)
}
