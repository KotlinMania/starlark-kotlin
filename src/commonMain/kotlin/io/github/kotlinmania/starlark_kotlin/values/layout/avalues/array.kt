// port-lint: source src/values/layout/avalues/array.rs
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

import io.github.kotlinmania.starlark_kotlin.values.FreezeResult
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenRef
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.Heap
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.Trace
import io.github.kotlinmania.starlark_kotlin.values.Tracer
import io.github.kotlinmania.starlark_kotlin.values.Value
import io.github.kotlinmania.starlark_kotlin.values.ValueTyped
import io.github.kotlinmania.starlark_kotlin.values.layout.Freezer
import io.github.kotlinmania.starlark_kotlin.values.layout.avalue.AValue
import io.github.kotlinmania.starlark_kotlin.values.layout.avalue.AValueImpl
import io.github.kotlinmania.starlark_kotlin.values.layout.value_alloc_size.ValueAllocSize
import io.github.kotlinmania.starlark_kotlin.values.types.any_array.AnyArray
import io.github.kotlinmania.starlark_kotlin.values.types.array.Array
import io.github.kotlinmania.starlark_kotlin.values.types.array.ValueEmptyArray

// fn array_avalue<'v>(cap: u32) -> AValueImpl<...>
private fun arrayAvalue(cap: UInt): AValueImpl<AValueArray> {
    return AValueImpl.new(Array.new(0, cap))
}

// fn any_array_avalue<T: Debug + 'static>(cap: usize) -> AValueImpl<...>
private fun <T> anyArrayAvalue(cap: Int): AValueImpl<AValueAnyArray<T>> {
    return AValueImpl.new(AnyArray.new<T>(cap))
}

/// AValue implementation for Array (mutable, variable-length content backed by capacity).
// struct AValueArray;
// impl<'v> AValue<'v> for AValueArray
internal object AValueArray : AValue {
    // type StarlarkValue = Array<'v>;
    // type ExtraElem = Value<'v>;

    // fn extra_len(value: &Array<'v>) -> usize
    override fun extraLen(value: StarlarkValue): Int {
        // Note we return capacity, not length here.
        return (value as Array).capacity()
    }

    // fn offset_of_extra() -> usize
    override fun offsetOfExtra(): Int {
        return Array.offsetOfContent()
    }

    // fn alloc_size_for_extra_len(extra_len: usize) -> ValueAllocSize
    override fun allocSizeForExtraLen(extraLen: Int): ValueAllocSize = ValueAllocSize(0)

    // unsafe fn heap_freeze(...) -> FreezeResult<FrozenValue>
    override fun heapFreeze(freezer: Freezer): FreezeResult<FrozenValue> {
        error("arrays should not be frozen")
    }

    // unsafe fn heap_copy(me: *mut AValueRepr<Self::StarlarkValue>, tracer: &Tracer<'v>) -> Value<'v>
    override fun heapCopy(tracer: Tracer): Value {
        val array = unpack() as Array
        check(array.capacity() != 0) { "empty array is allocated statically" }

        if (array.len() == 0) {
            return ValueEmptyArray.unpack().toValue()
        }

        val content = array.contentMut()

        // Trace all values in the content.
        (content as Trace).trace(tracer)

        // Note when copying we are dropping extra capacity.
        val newArray = Array.new(content.size.toUInt(), content.size.toUInt())
        for (i in content.indices) {
            newArray.contentMut()[i] = content[i]
        }
        return newArray.toValue()
    }

    override fun unpack(): StarlarkValue = Array.new(0, 0u)
}

/// AValue implementation for AnyArray (typed frozen-heap-only array).
// pub(crate) struct AValueAnyArray<T>(PhantomData<T>);
// impl<'v, T: Debug + 'static> AValue<'v> for AValueAnyArray<T>
internal class AValueAnyArray<T> : AValue {
    // type StarlarkValue = AnyArray<T>;
    // type ExtraElem = T;

    // fn extra_len(value: &AnyArray<T>) -> usize
    override fun extraLen(value: StarlarkValue): Int {
        return (value as AnyArray<*>).len
    }

    // fn offset_of_extra() -> usize
    override fun offsetOfExtra(): Int {
        return AnyArray.offsetOfContent<Any>()
    }

    // fn alloc_size_for_extra_len(extra_len: usize) -> ValueAllocSize
    override fun allocSizeForExtraLen(extraLen: Int): ValueAllocSize = ValueAllocSize(0)

    // unsafe fn heap_freeze(...) -> FreezeResult<FrozenValue>
    override fun heapFreeze(freezer: Freezer): FreezeResult<FrozenValue> {
        error("AnyArray for now can only be allocated in FrozenHeap")
    }

    // unsafe fn heap_copy(...) -> Value<'v>
    override fun heapCopy(tracer: Tracer): Value {
        error("AnyArray for now can only be allocated in FrozenHeap")
    }

    override fun unpack(): StarlarkValue = AnyArray.new<Any>(0)
}

// impl FrozenHeap

// fn do_alloc_any_slice<T: Debug + Send + Sync + Clone>(&self, values: &[T]) -> FrozenRef<'static, [T]>
private fun <T> FrozenHeap.doAllocAnySlice(values: List<T>): FrozenRef<List<T>> {
    val anyArray = AnyArray.new<T>(values.size)
    for ((i, v) in values.withIndex()) {
        anyArray.content()[i] = v
    }
    return FrozenRef(anyArray.content().toList())
}

/// Allocate a slice in the frozen heap.
// pub(crate) fn alloc_any_slice<T: Debug + Send + Sync + Clone>(&self, values: &[T]) -> FrozenRef<'static, [T]>
fun <T> FrozenHeap.allocAnySlice(values: List<T>): FrozenRef<List<T>> {
    if (values.isEmpty()) {
        return FrozenRef(emptyList())
    } else if (values.size == 1) {
        val single = allocAny(values[0])
        return single.map { listOf(it) }
    } else {
        return doAllocAnySlice(values)
    }
}

// impl<'v> Heap<'v>

// pub(crate) fn alloc_array(self, cap: usize) -> ValueTyped<'v, Array<'v>>
fun Heap.allocArray(cap: Int): ValueTyped<Array> {
    if (cap == 0) {
        return ValueEmptyArray.unpack().toValueTyped()
    }

    val capU32: UInt = cap.toUInt()

    return allocRawExtra(arrayAvalue(capU32)).first
}
