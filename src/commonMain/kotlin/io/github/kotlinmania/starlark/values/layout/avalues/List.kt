// port-lint: source src/values/layout/avalues/list.rs
package io.github.kotlinmania.starlark.values.layout.avalues

/*
 * Copyright 2019 The Starlark in Rust Authors.
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

import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.AValue
import io.github.kotlinmania.starlark.values.layout.AValueImpl
import io.github.kotlinmania.starlark.values.layout.Freezer
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.ValueTyped
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.types.array.Array
import io.github.kotlinmania.starlark.values.types.list.FrozenListData
import io.github.kotlinmania.starlark.values.types.list.ListData
import io.github.kotlinmania.starlark.values.types.list.ListGen
import io.github.kotlinmania.starlark.values.types.list.VALUE_EMPTY_FROZEN_LIST

internal fun listAvalue(
    content: ValueTyped<Array>,
): AValueImpl<AValueList> {
    val listData = ListData.new(content.asRef().content().toMutableList())
    return AValueImpl.new(ListGen(listData), AValueList)
}

internal fun frozenListAvalue(content: List<FrozenValue>): AValueImpl<AValueFrozenList> = AValueImpl.new(ListGen(FrozenListData.new(content)), AValueFrozenList)

/** AValue implementation for mutable lists. */
internal object AValueList : AValue {
    override fun extraLen(value: StarlarkValue): Int = 0

    override fun offsetOfExtra(): Int = 0

    override fun heapFreeze(freezer: Freezer): Result<FrozenValue> {
        // In the full implementation, this is called via vtable dispatch
        // with the actual StarlarkValue. The object form uses unpack() as placeholder.
        error("heapFreeze should be dispatched via vtable with actual value")
    }

    override fun heapCopy(tracer: Tracer): Value {
        error("heapCopy should be dispatched via vtable with actual value")
    }

    override fun unpack(): StarlarkValue = ListGen(ListData())
}

/** AValue implementation for frozen lists. */
internal object AValueFrozenList : AValue {
    override fun extraLen(value: StarlarkValue): Int {
        @Suppress("UNCHECKED_CAST")
        val list = value as ListGen<FrozenListData>
        return list.data.len()
    }

    override fun offsetOfExtra(): Int = 0

    override fun heapFreeze(freezer: Freezer): Result<FrozenValue> {
        error("already frozen")
    }

    override fun heapCopy(tracer: Tracer): Value {
        error("shouldn't be copying frozen values")
    }

    override fun unpack(): StarlarkValue = ListGen(FrozenListData.empty())
}

// --- FrozenHeap list allocation extensions ---


/** Allocate a list with the given elements on this heap. */
fun FrozenHeap.allocList(elems: List<FrozenValue>): FrozenValue {
    if (elems.isEmpty()) {
        return VALUE_EMPTY_FROZEN_LIST.toFrozenValue()
    }

    return allocRaw(frozenListAvalue(elems)).toFrozenValue()
}

fun FrozenHeap.allocListIter(elems: Iterable<FrozenValue>): FrozenValue {
    val list = elems.toList()
    return allocList(list)
}

// --- Heap list allocation extensions ---


/** Allocate a list with the given elements (from a slice/array). */
fun Heap.allocListFromSlice(elems: kotlin.Array<Value>): Value {
    val array = allocArray(elems.size)
    array.asRef().extendFromSlice(elems.toList())
    return allocRaw(listAvalue(array)).toValue()
}

/** Allocate a list with the given elements. */
fun Heap.allocListIter(elems: Iterable<Value>): Value {
    val result = tryAllocListIter(elems.map { Result.success(it) })
    return result.getOrThrow()
}

/** Allocate a list with the given elements. */
fun Heap.tryAllocListIter(
    elems: Iterable<Result<Value>>,
): Result<Value> {
    val array = allocArray(0)
    val listData = ListData.new(array.asRef().content().toMutableList())
    val listGen = ListGen(listData)
    for (elem in elems) {
        val v = elem.getOrElse { return Result.failure(it) }
        listData.push(v, this)
    }
    return Result.success(allocRaw(AValueImpl.new(listGen, AValueList)).toValue())
}

/** Allocate a list by concatenating two slices. */
fun Heap.allocListConcatSlices(a: kotlin.Array<Value>, b: kotlin.Array<Value>): Value {
    val array = allocArray(a.size + b.size)
    array.asRef().extendFromSlice(a.toList())
    array.asRef().extendFromSlice(b.toList())
    return allocRaw(listAvalue(array)).toValue()
}
