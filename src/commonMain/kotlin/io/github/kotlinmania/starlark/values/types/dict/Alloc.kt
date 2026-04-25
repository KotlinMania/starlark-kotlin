// port-lint: source src/values/types/dict/alloc.rs
package io.github.kotlinmania.starlark.values.types.dict

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

// use std::iter;

// use starlarkmap::small_map::SmallMap;

// use crate::typing::Ty;
// use crate::values::AllocFrozenValue;
// use crate::values::AllocValue;
// use crate::values::FrozenHeap;
// use crate::values::FrozenValue;
// use crate::values::Heap;
// use crate::values::Value;
// use crate::values::dict::Dict;
// use crate::values::dict::value::FrozenDictData;
// use crate::values::layout::value::ValueLike;
// use crate::values::type_repr::StarlarkTypeRepr;
// use crate::values::types::dict::dict_type::DictType;

import starlarkmap.smallmap.SmallMap
import starlarkmap.Hashed
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.AllocFrozenValue
import io.github.kotlinmania.starlark.values.AllocValue
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr

/// Utility to allocate a dict from iterator.
///
/// Iterator must be a list of pairs (key, value).
/// Duplicate keys are allowed, last key wins.
///
/// # Panics
///
/// Panics if a key is not hashable.
///
/// # Example
///
/// ```
/// use starlark::values::dict::AllocDict;
///
/// # use starlark::values::{FrozenHeap, Heap};
/// # fn alloc(heap: Heap<'_>, frozen_heap: &FrozenHeap) {
/// let l = heap.alloc(AllocDict([("a", 1), ("b", 2), ("c", 3)]));
/// let ls = frozen_heap.alloc(AllocDict([("a", 1), ("b", 2), ("c", 3)]));
/// # }
/// ```
// pub struct AllocDict<D>(pub D);
data class AllocDict<D>(val d: D) {
    // impl AllocDict<iter::Empty<(FrozenValue, FrozenValue)>>
    companion object {
        /// Allocate an empty dict.
        // pub const EMPTY: AllocDict<iter::Empty<(FrozenValue, FrozenValue)>> = AllocDict(iter::empty());
        val EMPTY: AllocDict<Sequence<Pair<FrozenValue, FrozenValue>>> = AllocDict(emptySequence())
    }
}

// impl<D, K, V> StarlarkTypeRepr for AllocDict<D>
// where D: IntoIterator<Item = (K, V)>, K: StarlarkTypeRepr, V: StarlarkTypeRepr
//     fn starlark_type_repr() -> Ty
inline fun <reified K : StarlarkTypeRepr, reified V : StarlarkTypeRepr> allocDictStarlarkTypeRepr(): Ty =
    DictType.starlarkTypeRepr<K, V>()

// impl<'v, D, K, V> AllocValue<'v> for AllocDict<D>
// where D: IntoIterator<Item = (K, V)>, K: AllocValue<'v>, V: AllocValue<'v>
//     fn alloc_value(self, heap: Heap<'v>) -> Value<'v>
fun <D, K, V> AllocDict<D>.allocValue(heap: Heap): Value
    where D : Iterable<Pair<K, V>>,
          K : AllocValue,
          V : AllocValue {
    val iter = this.d.iterator()
    // let mut map = SmallMap::with_capacity(iter.size_hint().0);
    val map = SmallMap.withCapacity<Value, Value>((this.d as? Collection<*>)?.size ?: 0)
    for ((k, v) in iter) {
        // map.insert_hashed(k.alloc_value(heap).get_hashed().unwrap(), v.alloc_value(heap));
        map.insertHashed(
            k.allocValue(heap).getHashed().getOrThrow(),
            v.allocValue(heap)
        )
    }
    // heap.alloc(Dict::new(map))
    return Dict.new(map).allocValue(heap)
}

// impl<D, K, V> AllocFrozenValue for AllocDict<D>
// where D: IntoIterator<Item = (K, V)>, K: AllocFrozenValue, V: AllocFrozenValue
//     fn alloc_frozen_value(self, heap: &FrozenHeap) -> FrozenValue
fun <D, K, V> AllocDict<D>.allocFrozenValue(heap: FrozenHeap): FrozenValue
    where D : Iterable<Pair<K, V>>,
          K : AllocFrozenValue,
          V : AllocFrozenValue {
    val iter = this.d.iterator()
    // let mut map = SmallMap::with_capacity(iter.size_hint().0);
    val map = SmallMap.withCapacity<FrozenValue, FrozenValue>((this.d as? Collection<*>)?.size ?: 0)
    for ((k, v) in iter) {
        // map.insert_hashed(k.alloc_frozen_value(heap).get_hashed().unwrap(), v.alloc_frozen_value(heap));
        val frozenKey = k.allocFrozenValue(heap)
        val hashedValue = frozenKey.toValue().getHashed().getOrThrow()
        map.insertHashed(
            Hashed.newUnchecked(hashedValue.hash(), frozenKey),
            v.allocFrozenValue(heap)
        )
    }
    // heap.alloc(FrozenDictData { content: map })
    return FrozenDictData(content = map).allocFrozenValue(heap)
}
