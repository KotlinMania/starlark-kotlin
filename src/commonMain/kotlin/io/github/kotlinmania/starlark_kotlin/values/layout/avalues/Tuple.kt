// port-lint: source src/values/layout/avalues/tuple.rs
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
import io.github.kotlinmania.starlark_kotlin.values.layout.Freezer
import io.github.kotlinmania.starlark_kotlin.values.layout.avalue.AValue
import io.github.kotlinmania.starlark_kotlin.values.layout.avalue.AValueImpl
import io.github.kotlinmania.starlark_kotlin.values.layout.value_alloc_size.ValueAllocSize
import io.github.kotlinmania.starlark_kotlin.values.types.tuple.FrozenTuple
import io.github.kotlinmania.starlark_kotlin.values.types.tuple.Tuple
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.trace
import io.github.kotlinmania.starlark_kotlin.values.toValue
import io.github.kotlinmania.starlark_kotlin.values.Tracer
import io.github.kotlinmania.starlark_kotlin.values.types.any_array.offsetOfContent
import io.github.kotlinmania.starlark_kotlin.values.types.array.len
import io.github.kotlinmania.starlark_kotlin.values.types.array.contentMut
import io.github.kotlinmania.starlark_kotlin.values.freeze_error.FreezeResult

// fn tuple_avalue<'v>(len: usize) -> AValueImpl<'v, AValueTuple>
internal fun tupleAvalue(len: Int): AValueImpl<AValueTuple> {
    return AValueImpl.new(Tuple.new(len))
}

// fn frozen_tuple_avalue<'fv>(len: usize) -> AValueImpl<'fv, AValueFrozenTuple>
internal fun frozenTupleAvalue(len: Int): AValueImpl<AValueFrozenTuple> {
    return AValueImpl.new(FrozenTuple.new(len))
}

/// AValue implementation for mutable tuples.
// struct AValueTuple;
// impl<'v> AValue<'v> for AValueTuple
internal class AValueTuple(
    private val tuple: Tuple,
) : AValue {

    // type StarlarkValue = Tuple<'v>;
    // type ExtraElem = Value<'v>;

    // fn extra_len(value: &Tuple<'v>) -> usize
    override fun extraLen(value: StarlarkValue): Int {
        return (value as Tuple).len()
    }

    // fn offset_of_extra() -> usize
    override fun offsetOfExtra(): Int = Tuple.offsetOfContent()

    // fn alloc_size_for_extra_len(extra_len: usize) -> ValueAllocSize
    override fun allocSizeForExtraLen(extraLen: Int): ValueAllocSize {
        return ValueAllocSize.ofBytes(extraLen)
    }

    // unsafe fn heap_freeze(me, freezer) -> FreezeResult<FrozenValue>
    override fun heapFreeze(freezer: Freezer): FreezeResult<FrozenValue> {
        // debug_assert!((*me).payload.len() != 0, "empty tuple is allocated statically");
        val content = tuple.content()
        check(content.isNotEmpty()) { "empty tuple is allocated statically" }

        // Freeze each element.
        // TODO: this allocation is unnecessary
        val frozenValues = mutableListOf<FrozenValue>()
        for (v in content) {
            val frozen = freezer.freeze(v)
            if (frozen.isError()) return FreezeResult.error(frozen.errorValue())
            frozenValues.add(frozen.get())
        }

        // Allocate frozen tuple on frozen heap.
        val frozenTuple = FrozenTuple.new(content.size)
        frozenTuple.setContent(frozenValues)
        return freezer.frozenHeap().allocTupleFromFrozen(frozenTuple)
    }

    // unsafe fn heap_copy(me, tracer) -> Value<'v>
    override fun heapCopy(tracer: Tracer): Value {
        // debug_assert!((*me).payload.len() != 0, "empty tuple is allocated statically");
        val content = tuple.contentMut()
        check(content.isNotEmpty()) { "empty tuple is allocated statically" }

        // Trace each element in place.
        for (i in content.indices) {
            content[i] = tracer.trace(content[i])
        }

        return tuple.toValue()
    }

    override fun unpack(): StarlarkValue = tuple
}

/// AValue implementation for frozen tuples.
// struct AValueFrozenTuple;
// impl<'v> AValue<'v> for AValueFrozenTuple
internal class AValueFrozenTuple(
    private val frozenTuple: FrozenTuple,
) : AValue {

    // type StarlarkValue = FrozenTuple;
    // type ExtraElem = FrozenValue;

    // fn extra_len(value: &FrozenTuple) -> usize
    override fun extraLen(value: StarlarkValue): Int {
        return (value as FrozenTuple).len()
    }

    // fn offset_of_extra() -> usize
    override fun offsetOfExtra(): Int = FrozenTuple.offsetOfContent()

    // fn alloc_size_for_extra_len(extra_len: usize) -> ValueAllocSize
    override fun allocSizeForExtraLen(extraLen: Int): ValueAllocSize {
        return ValueAllocSize.ofBytes(extraLen)
    }

    // unsafe fn heap_freeze(_me, _freezer) -> FreezeResult<FrozenValue>
    override fun heapFreeze(freezer: Freezer): FreezeResult<FrozenValue> {
        error("already frozen")
    }

    // unsafe fn heap_copy(_me, _tracer) -> Value<'v>
    override fun heapCopy(tracer: Tracer): Value {
        error("shouldn't be copying frozen values")
    }

    override fun unpack(): StarlarkValue = frozenTuple
}

// impl FrozenHeap

/// Allocate a tuple with the given elements on this heap.
// pub(crate) fn alloc_tuple(&self, elems: &[FrozenValue]) -> FrozenValue
fun FrozenHeap.allocTuple(elems: List<FrozenValue>): FrozenValue {
    if (elems.isEmpty()) {
        return FrozenValue.newEmptyTuple()
    }
    val frozenTuple = FrozenTuple.new(elems.size)
    frozenTuple.setContent(elems)
    return allocRaw(frozenTupleAvalue(elems.size)).toFrozenValue()
}

/// Allocate a tuple from an iterator of elements.
// pub(crate) fn alloc_tuple_iter(&self, elems: impl IntoIterator<Item = FrozenValue>) -> FrozenValue
fun FrozenHeap.allocTupleIter(elems: Iterable<FrozenValue>): FrozenValue {
    val list = elems.toList()
    return allocTuple(list)
}

/// Internal helper: allocate a pre-built frozen tuple.
internal fun FrozenHeap.allocTupleFromFrozen(frozenTuple: FrozenTuple): FreezeResult<FrozenValue> {
    return FreezeResult.success(allocRaw(frozenTupleAvalue(frozenTuple.len())).toFrozenValue())
}

// impl Heap

/// Allocate a tuple with the given elements.
// pub(crate) fn alloc_tuple(self, elems: &[Value<'v>]) -> Value<'v>
fun Heap.allocTuple(elems: List<Value>): Value {
    if (elems.isEmpty()) {
        return Value.newEmptyTuple()
    }
    val tuple = Tuple.new(elems.size)
    tuple.setContent(elems)
    return allocRaw(tupleAvalue(elems.size)).toValue()
}

/// Allocate a tuple from an iterator of elements.
// pub(crate) fn alloc_tuple_iter(self, elems: impl IntoIterator<Item = Value<'v>>) -> Value<'v>
fun Heap.allocTupleIter(elems: Iterable<Value>): Value {
    val list = elems.toList()
    return allocTuple(list)
}

// register_special_avalue_frozen!(FrozenTuple, AValueFrozenTuple);
// Kotlin: vtable registration is handled differently; no macro expansion needed.
