// port-lint: source src/values/types/tuple/alloc.rs
package io.github.kotlinmania.starlark.values.types.tuple

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

import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.AllocFrozenValue
import io.github.kotlinmania.starlark.values.AllocValue
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.allocTupleIter
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.heap.Heap

/**
 * Utility to allocate a tuple.
 *
 * Note, for tuples of fixed sizes there are implementations for `(A,)` or `(A, B)`.
 *
 * # Example
 *
 * ```
 * val l = heap.alloc(AllocTuple(listOf(1, 2, 3)))
 * val ls = frozenHeap.alloc(AllocTuple(listOf(1, 2, 3)))
 * ```
 */
class AllocTuple<T>(
    val items: Iterable<T>,
) : StarlarkTypeRepr,
    AllocValue,
    AllocFrozenValue
    where T : StarlarkTypeRepr, T : AllocValue, T : AllocFrozenValue {
    companion object {
        /** Allocate an empty tuple. */
        val EMPTY: AllocTuple<Nothing> = AllocTuple(emptyList())
    }

    override fun starlarkTypeRepr(): Ty = Ty.tupleOf(items.firstOrNull()?.starlarkTypeRepr() ?: Ty.any())

    override fun allocValue(heap: Heap): Value =
        heap.allocTupleIter(
            items.map { x ->
                (x as AllocValue).allocValue(heap)
            },
        )

    override fun allocFrozenValue(heap: FrozenHeap): FrozenValue =
        heap.allocTupleIter(
            items.map { x ->
                (x as AllocFrozenValue).allocFrozenValue(heap)
            },
        )
}
