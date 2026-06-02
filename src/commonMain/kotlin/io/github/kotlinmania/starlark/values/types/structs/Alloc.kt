
// port-lint: source src/values/types/structs/alloc.rs
package io.github.kotlinmania.starlark.values.types.structs

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

import io.github.kotlinmania.starlark.collections.SmallMap
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.AllocFrozenStringValue
import io.github.kotlinmania.starlark.values.AllocFrozenValue
import io.github.kotlinmania.starlark.values.AllocStringValue
import io.github.kotlinmania.starlark.values.AllocValue
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.allocComplex
import io.github.kotlinmania.starlark.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.heap.Heap

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
internal data class AllocStruct<S>(
    val value: S,
) {
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
    return Ty.anyStruct()
}

/**
 * Implementation of AllocValue for AllocStruct<S>
 * where S: IntoIterator, S::Item = (K, V), K: AllocStringValue, V: AllocValue.
 */
internal fun <K, V, S> AllocStruct<S>.allocValue(heap: Heap): Value
    where S : Iterable<Pair<K, V>>,
          K : AllocStringValue,
          V : AllocValue {
    val iter = value.iterator()
    // size_hint().0 in Rust returns the lower bound of the iterator's size hint
    val sizeHint = if (value is Collection<*>) value.size else 0
    val fields = SmallMap.withCapacity<String, Value>(sizeHint)

    for ((k, v) in iter) {
        val allocatedKey = k.allocStringValue(heap).asStr()
        val allocatedVal = v.allocValue(heap)
        val prev = fields.insert(allocatedKey, allocatedVal)
        check(prev == null) { "non-unique key: $allocatedKey" }
    }

    return heap.allocComplex(StructGen.mutable(fields))
}

/**
 * Implementation of AllocFrozenValue for AllocStruct<S>
 * where S: IntoIterator, S::Item = (K, V), K: AllocFrozenStringValue, V: AllocFrozenValue.
 */
internal fun <K, V, S> AllocStruct<S>.allocFrozenValue(heap: FrozenHeap): FrozenValue
    where S : Iterable<Pair<K, V>>,
          K : AllocFrozenStringValue,
          V : AllocFrozenValue {
    val iter = value.iterator()
    // size_hint().0 in Rust returns the lower bound of the iterator's size hint
    val sizeHint = if (value is Collection<*>) value.size else 0
    val fields = SmallMap.withCapacity<String, FrozenValue>(sizeHint)

    for ((k, v) in iter) {
        val allocatedKey = k.allocFrozenStringValue(heap).asStr()
        val allocatedVal = v.allocFrozenValue(heap)
        val prev = fields.insert(allocatedKey, allocatedVal)
        check(prev == null) { "non-unique key: $allocatedKey" }
    }

    return heap.allocSimple(StructGen.frozen(fields))
}
