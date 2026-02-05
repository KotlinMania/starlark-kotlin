// port-lint: source src/values/types/structs/alloc.rs
package io.github.kotlinmania.starlark_kotlin.values.types.structs

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
import io.github.kotlinmania.starlark_kotlin.values.Heap
import io.github.kotlinmania.starlark_kotlin.values.Value
import io.github.kotlinmania.starlark_kotlin.values.allocValue.AllocFrozenStringValue
import io.github.kotlinmania.starlark_kotlin.values.allocValue.AllocStringValue
import io.github.kotlinmania.starlark_kotlin.values.typeRepr.StarlarkTypeRepr

/**
 * Utility to allocate a struct on a heap.
 *
 * ## Panics
 *
 * Panics if:
 * * keys are not strings
 * * keys are not unique
 *
 * ## Example
 *
 * ```
 * use starlark::values::structs::AllocStruct;
 *
 * # use starlark::values::{FrozenHeap, Heap};
 * # fn alloc(heap: Heap<'_>, frozen_heap: &FrozenHeap) {
 * let s = heap.alloc(AllocStruct([("a", 1), ("b", 2)]));
 * let fs = frozen_heap.alloc(AllocStruct([("a", 1), ("b", 2)]));
 * # }
 * ```
 */
data class AllocStruct<S>(val value: S) {
    companion object {
        /**
         * Allocate an empty struct.
         */
        val EMPTY: AllocStruct<Sequence<Pair<String, String>>> = AllocStruct(emptySequence())
    }
}

/**
 * Implementation of StarlarkTypeRepr for AllocStruct<S>
 * where S: IntoIterator, S::Item = (K, V), V: StarlarkTypeRepr.
 *
 * In Kotlin, we provide this as an extension function on the companion object.
 */
inline fun <reified K, reified V, reified S> allocStructStarlarkTypeRepr(): Ty
    where S : Iterable<Pair<K, V>>,
          V : StarlarkTypeRepr {
    // Return the canonical type for StructRef
    return Struct.starlarkTypeRepr()
}

/**
 * Implementation of AllocValue for AllocStruct<S>
 * where S: IntoIterator, S::Item = (K, V), K: AllocStringValue<V_>, V: AllocValue<V_>.
 */
fun <V_, K, V, S> AllocStruct<S>.allocValue(heap: Heap<V_>): Value<V_>
    where S : Iterable<Pair<K, V>>,
          K : AllocStringValue<V_>,
          V : AllocValue<V_> {
    val iter = value.iterator()
    // size_hint().0 in Rust returns the lower bound of the iterator's size hint
    val sizeHint = if (value is Collection<*>) value.size else 0
    val fields = SmallMap.withCapacity<String, Value<V_>>(sizeHint)

    for ((k, v) in iter) {
        val k = k.allocStringValue(heap)
        val v = v.allocValue(heap)
        val prev = fields.insert(k, v)
        assert(prev == null) { "non-unique key: $k" }
    }

    return heap.alloc(Struct.new(fields))
}

/**
 * Implementation of AllocFrozenValue for AllocStruct<S>
 * where S: IntoIterator, S::Item = (K, V), K: AllocFrozenStringValue, V: AllocFrozenValue.
 */
fun <K, V, S> AllocStruct<S>.allocFrozenValue(heap: FrozenHeap): FrozenValue
    where S : Iterable<Pair<K, V>>,
          K : AllocFrozenStringValue,
          V : AllocFrozenValue {
    val iter = value.iterator()
    // size_hint().0 in Rust returns the lower bound of the iterator's size hint
    val sizeHint = if (value is Collection<*>) value.size else 0
    val fields = SmallMap.withCapacity<String, FrozenValue>(sizeHint)

    for ((k, v) in iter) {
        val k = k.allocFrozenStringValue(heap)
        val v = v.allocFrozenValue(heap)
        val prev = fields.insert(k, v)
        assert(prev == null) { "non-unique key: $k" }
    }

    return heap.alloc(FrozenStruct.new(fields))
}
