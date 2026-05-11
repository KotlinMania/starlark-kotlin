// port-lint: source values/layout/avalues/array.rs
package io.github.kotlinmania.starlark.values.layout.avalues

/*
 * Copyright 2019 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not import this file except in compliance with the License.
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

import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.FrozenRef
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.Trace
import io.github.kotlinmania.starlark.values.layout.Freezer
import io.github.kotlinmania.starlark.values.layout.AValue
import io.github.kotlinmania.starlark.values.layout.AValueImpl
import io.github.kotlinmania.starlark.values.layout.ValueAllocSize
import io.github.kotlinmania.starlark.values.types.anyarray.AnyArray
import io.github.kotlinmania.starlark.values.types.array.Array
import io.github.kotlinmania.starlark.values.types.array.ValueEmptyArray
import io.github.kotlinmania.starlark.values.layout.ValueTyped
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.toValue
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.layout.AlignedSize
import io.github.kotlinmania.starlark.values.types.allocAny

private fun arrayAvalue(cap: UInt): AValueImpl<AValueArray> {
    return AValueImpl.new(Array.new(0, cap.toInt()))
}

private fun <T> anyArrayAvalue(cap: Int): AValueImpl<AValueAnyArray<T>> {
    return AValueImpl.new(AnyArray.new<T>(cap))
}

/** AValue implementation for Array (mutable, variable-length content backed by capacity). */
internal object AValueArray : AValue {

    override fun extraLen(value: StarlarkValue): Int {
        // Note we return capacity, not length here.
        return (value as Array).capacity()
    }

    override fun offsetOfExtra(): Int = 0

    override fun allocSizeForExtraLen(extraLen: Int): ValueAllocSize = ValueAllocSize(AlignedSize(0u))

    override fun heapFreeze(_freezer: Freezer): Result<FrozenValue> {
        error("arrays should not be frozen")
    }

    override fun heapCopy(tracer: Tracer): Value {
        val array = unpack() as Array
        check(array.capacity() != 0) { "empty array is allocated statically" }

        if (array.len() == 0) {
            return ValueEmptyArray.unpack().toValue()
        }

        val content = array.contentMut()

        val (v, r, _) = tracer.reserveWithExtra<AValueArray>(content.size)

        // Trace all values in the content.
        (content as Trace).trace(tracer)

        // Note when copying we are dropping extra capacity.
        val newArray = Array.new(content.size, content.size)
        for (i in content.indices) {
            newArray.contentMut()[i] = content[i]
        }
        r.fill(newArray)
        return v
    }

    override fun unpack(): StarlarkValue = Array.new(0, 0)
}

/** AValue implementation for AnyArray (typed frozen-heap-only array). */
internal class AValueAnyArray<T> : AValue {

    override fun extraLen(value: StarlarkValue): Int {
        return (value as AnyArray<*>).len
    }

    override fun offsetOfExtra(): Int = 0

    override fun allocSizeForExtraLen(extraLen: Int): ValueAllocSize = ValueAllocSize(AlignedSize(0u))

    override fun heapFreeze(_freezer: Freezer): Result<FrozenValue> {
        error("AnyArray for now can only be allocated in FrozenHeap")
    }

    override fun heapCopy(_tracer: Tracer): Value {
        error("AnyArray for now can only be allocated in FrozenHeap")
    }

    override fun unpack(): StarlarkValue = AnyArray.new<Any>(0)
}

private fun <T> FrozenHeap.doAllocAnySlice(values: List<T>): FrozenRef<List<T>> {
    val anyArray = AnyArray.new<T>(values.size)
    for (v in values) {
        anyArray.add(v)
    }
    return FrozenRef(anyArray.asSlice().toList())
}

/** Allocate a slice in the frozen heap. */
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

internal fun Heap.allocArray(cap: Int): ValueTyped<Array> {
    if (cap == 0) {
        return ValueEmptyArray.unpack().toValueTyped()
    }

    val capU32: UInt = cap.toUInt()

    return allocRawExtra(arrayAvalue(capU32)).first as ValueTyped<Array>
}
