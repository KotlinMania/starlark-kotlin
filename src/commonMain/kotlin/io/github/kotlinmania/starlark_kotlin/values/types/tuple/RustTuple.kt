// port-lint: source src/values/types/tuple/rust_tuple.rs
package io.github.kotlinmania.starlark_kotlin.values.types.tuple

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

/** Bindings to/from tuple types. */

import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.allocTuple
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.FrozenHeap

/** AllocValue for 1-element tuple. */
fun <T1> allocTuple1(t1: T1, heap: Heap, alloc1: (T1, Heap) -> Value): Value {
    return heap.allocTuple(listOf(alloc1(t1, heap)))
}

/** AllocValue for 2-element tuple. */
fun <T1, T2> allocTuple2(
    t1: T1,
    t2: T2,
    heap: Heap,
    alloc1: (T1, Heap) -> Value,
    alloc2: (T2, Heap) -> Value,
): Value {
    return heap.allocTuple(listOf(alloc1(t1, heap), alloc2(t2, heap)))
}

/** AllocValue for 3-element tuple. */
fun <T1, T2, T3> allocTuple3(
    t1: T1,
    t2: T2,
    t3: T3,
    heap: Heap,
    alloc1: (T1, Heap) -> Value,
    alloc2: (T2, Heap) -> Value,
    alloc3: (T3, Heap) -> Value,
): Value {
    return heap.allocTuple(listOf(alloc1(t1, heap), alloc2(t2, heap), alloc3(t3, heap)))
}

/** AllocFrozenValue for 1-element tuple. */
fun <T1> allocFrozenTuple1(
    t1: T1,
    heap: FrozenHeap,
    alloc1: (T1, FrozenHeap) -> FrozenValue,
): FrozenValue {
    return heap.allocTuple(listOf(alloc1(t1, heap)))
}

/** AllocFrozenValue for 2-element tuple. */
fun <T1, T2> allocFrozenTuple2(
    t1: T1,
    t2: T2,
    heap: FrozenHeap,
    alloc1: (T1, FrozenHeap) -> FrozenValue,
    alloc2: (T2, FrozenHeap) -> FrozenValue,
): FrozenValue {
    return heap.allocTuple(listOf(alloc1(t1, heap), alloc2(t2, heap)))
}

/** AllocFrozenValue for 3-element tuple. */
fun <T1, T2, T3> allocFrozenTuple3(
    t1: T1,
    t2: T2,
    t3: T3,
    heap: FrozenHeap,
    alloc1: (T1, FrozenHeap) -> FrozenValue,
    alloc2: (T2, FrozenHeap) -> FrozenValue,
    alloc3: (T3, FrozenHeap) -> FrozenValue,
): FrozenValue {
    return heap.allocTuple(listOf(alloc1(t1, heap), alloc2(t2, heap), alloc3(t3, heap)))
}

/** StarlarkTypeRepr for 1-element tuple. */
fun starlarkTypeReprTuple1(repr1: () -> Ty): Ty {
    return Ty.tuple(listOf(repr1()))
}

/** StarlarkTypeRepr for 2-element tuple. */
fun starlarkTypeReprTuple2(repr1: () -> Ty, repr2: () -> Ty): Ty {
    return Ty.tuple2(repr1(), repr2())
}

/** StarlarkTypeRepr for 3-element tuple. */
fun starlarkTypeReprTuple3(repr1: () -> Ty, repr2: () -> Ty, repr3: () -> Ty): Ty {
    return Ty.tuple(listOf(repr1(), repr2(), repr3()))
}

/** UnpackValue for 2-element tuple. */
fun <T1, T2> unpackTuple2(
    value: Value,
    unpack1: (Value) -> T1?,
    unpack2: (Value) -> T2?,
): Pair<T1, T2>? {
    val t = TupleGen.fromValue(value) ?: return null
    val content = t.content()
    if (content.size != 2) return null
    val a = unpack1(content[0]) ?: return null
    val b = unpack2(content[1]) ?: return null
    return Pair(a, b)
}

/** UnpackValue for 3-element tuple. */
fun <T1, T2, T3> unpackTuple3(
    value: Value,
    unpack1: (Value) -> T1?,
    unpack2: (Value) -> T2?,
    unpack3: (Value) -> T3?,
): Triple<T1, T2, T3>? {
    val t = TupleGen.fromValue(value) ?: return null
    val content = t.content()
    if (content.size != 3) return null
    val a = unpack1(content[0]) ?: return null
    val b = unpack2(content[1]) ?: return null
    val c = unpack3(content[2]) ?: return null
    return Triple(a, b, c)
}
