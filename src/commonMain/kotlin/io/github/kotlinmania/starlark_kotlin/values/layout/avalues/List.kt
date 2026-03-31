// port-lint: source src/values/layout/avalues/list.rs
package io.github.kotlinmania.starlark_kotlin.values.layout.avalues

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

import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.Tracer
import io.github.kotlinmania.starlark_kotlin.values.layout.Freezer
import io.github.kotlinmania.starlark_kotlin.values.layout.AValue
import io.github.kotlinmania.starlark_kotlin.values.layout.AValueImpl
import io.github.kotlinmania.starlark_kotlin.values.layout.ValueTyped
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.types.list.FrozenListData
import io.github.kotlinmania.starlark_kotlin.values.types.list.ListData
import io.github.kotlinmania.starlark_kotlin.values.types.list.ListGen
import io.github.kotlinmania.starlark_kotlin.values.types.list.VALUE_EMPTY_FROZEN_LIST
import io.github.kotlinmania.starlark_kotlin.values.types.array.Array
import io.github.kotlinmania.starlark_kotlin.values.freeze_error.FreezeResult

// fn list_avalue<'v>(content: ValueTyped<'v, Array<'v>>) -> AValueImpl<'v, impl AValue<'v, ...>>
internal fun listAvalue(
    content: ValueTyped<Array>,
): AValueImpl<AValueList> {
    val listData = ListData.new(content.asRef().content().toMutableList())
    return AValueImpl.new(ListGen(listData))
}

// fn frozen_list_avalue<'fv>(len: usize) -> AValueImpl<'fv, AValueFrozenList>
internal fun frozenListAvalue(content: List<FrozenValue>): AValueImpl<AValueFrozenList> {
    return AValueImpl.new(ListGen(FrozenListData.new(content)))
}

/** AValue implementation for mutable lists. */
// struct AValueList;
// impl<'v> AValue<'v> for AValueList
internal object AValueList : AValue {

    // fn extra_len(_value: &ListGen<ListData<'v>>) -> usize
    override fun extraLen(value: StarlarkValue): Int = 0

    // fn offset_of_extra() -> usize
    override fun offsetOfExtra(): Int = 0

    // unsafe fn heap_freeze(me: ..., freezer: &Freezer) -> FreezeResult<FrozenValue>
    override fun heapFreeze(_freezer: Freezer): FreezeResult<FrozenValue> {
        // In the full implementation, this is called via vtable dispatch
        // with the actual StarlarkValue. The object form uses unpack() as placeholder.
        error("heapFreeze should be dispatched via vtable with actual value")
    }

    // unsafe fn heap_copy(me: ..., tracer: &Tracer<'v>) -> Value<'v>
    override fun heapCopy(_tracer: Tracer): Value {
        error("heapCopy should be dispatched via vtable with actual value")
    }

    override fun unpack(): StarlarkValue = ListGen(ListData())
}

/** AValue implementation for frozen lists. */
// pub(crate) struct AValueFrozenList;
// impl<'v> AValue<'v> for AValueFrozenList
internal object AValueFrozenList : AValue {

    // fn extra_len(value: &ListGen<FrozenListData>) -> usize
    override fun extraLen(value: StarlarkValue): Int {
        @Suppress("UNCHECKED_CAST")
        val list = value as ListGen<FrozenListData>
        return list.data.len()
    }

    // fn offset_of_extra() -> usize
    override fun offsetOfExtra(): Int = 0

    // unsafe fn heap_freeze(...) -> FreezeResult<FrozenValue>
    override fun heapFreeze(_freezer: Freezer): FreezeResult<FrozenValue> {
        error("already frozen")
    }

    // unsafe fn heap_copy(...) -> Value<'v>
    override fun heapCopy(_tracer: Tracer): Value {
        error("shouldn't be copying frozen values")
    }

    override fun unpack(): StarlarkValue = ListGen(FrozenListData.empty())
}

// --- FrozenHeap list allocation extensions ---

// impl FrozenHeap

/** Allocate a list with the given elements on this heap. */
// pub(crate) fn alloc_list(&self, elems: &[FrozenValue]) -> FrozenValue
fun FrozenHeap.allocList(elems: List<FrozenValue>): FrozenValue {
    if (elems.isEmpty()) {
        return VALUE_EMPTY_FROZEN_LIST.toFrozenValue()
    }

    return allocRaw(frozenListAvalue(elems)).toFrozenValue()
}

// pub(crate) fn alloc_list_iter(&self, elems: impl IntoIterator<Item = FrozenValue>) -> FrozenValue
fun FrozenHeap.allocListIter(elems: Iterable<FrozenValue>): FrozenValue {
    val list = elems.toList()
    return allocList(list)
}

// --- Heap list allocation extensions ---

// impl<'v> Heap<'v>

/** Allocate a list with the given elements (from a slice/array). */
// pub(crate) fn alloc_list(self, elems: &[Value<'v>]) -> Value<'v>
fun Heap.allocListFromSlice(elems: kotlin.Array<Value>): Value {
    val array = allocArray(elems.size)
    array.asRef().extendFromSlice(elems.toList())
    return allocRaw(listAvalue(array)).toValue()
}

/** Allocate a list with the given elements. */
// pub(crate) fn alloc_list_iter(self, elems: impl IntoIterator<Item = Value<'v>>) -> Value<'v>
fun Heap.allocListIter(elems: Iterable<Value>): Value {
    val result = tryAllocListIter(elems.map { Result.success(it) })
    return result.getOrThrow()
}

/** Allocate a list with the given elements. */
// pub(crate) fn try_alloc_list_iter<E>(self, elems: impl IntoIterator<Item = Result<Value<'v>, E>>) -> Result<Value<'v>, E>
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
    return Result.success(allocRaw(AValueImpl.new<AValueList>(listGen)).toValue())
}

/** Allocate a list by concatenating two slices. */
// pub(crate) fn alloc_list_concat(self, a: &[Value<'v>], b: &[Value<'v>]) -> Value<'v>
fun Heap.allocListConcatSlices(a: kotlin.Array<Value>, b: kotlin.Array<Value>): Value {
    val array = allocArray(a.size + b.size)
    array.asRef().extendFromSlice(a.toList())
    array.asRef().extendFromSlice(b.toList())
    return allocRaw(listAvalue(array)).toValue()
}
