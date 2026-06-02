// port-lint: source src/values/layout/avalues/tuple.rs
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
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.types.tuple.FrozenTuple
import io.github.kotlinmania.starlark.values.types.tuple.Tuple
import io.github.kotlinmania.starlark.values.types.tuple.TupleGen

internal fun tupleAvalue(len: Int): AValueImpl<AValueTuple> = AValueImpl.new(TupleGen<Value>(MutableList(len) { Value.newNone() }), AValueTuple)

internal fun frozenTupleAvalue(len: Int): AValueImpl<AValueFrozenTuple> = AValueImpl.new(TupleGen<FrozenValue>(MutableList(len) { FrozenValue.newNone() }), AValueFrozenTuple)

/** AValue implementation for mutable tuples. */
internal object AValueTuple : AValue {
    override fun extraLen(value: StarlarkValue): Int {
        @Suppress("UNCHECKED_CAST")
        return (value as Tuple).len()
    }

    override fun offsetOfExtra(): Int = 0

    override fun heapFreeze(freezer: Freezer): Result<FrozenValue> {
        error("heapFreeze should be dispatched via vtable with actual value")
    }

    override fun heapCopy(tracer: Tracer): Value {
        error("heapCopy should be dispatched via vtable with actual value")
    }

    override fun unpack(): StarlarkValue = TupleGen<Value>(emptyList())
}

/** AValue implementation for frozen tuples. */
internal object AValueFrozenTuple : AValue {
    override fun extraLen(value: StarlarkValue): Int {
        @Suppress("UNCHECKED_CAST")
        return (value as FrozenTuple).len()
    }

    override fun offsetOfExtra(): Int = 0

    override fun heapFreeze(freezer: Freezer): Result<FrozenValue> {
        error("already frozen")
    }

    override fun heapCopy(tracer: Tracer): Value {
        error("shouldn't be copying frozen values")
    }

    override fun unpack(): StarlarkValue = TupleGen<FrozenValue>(emptyList())
}


/** Allocate a tuple with the given elements on this heap. */
fun FrozenHeap.allocTuple(elems: List<FrozenValue>): FrozenValue {
    if (elems.isEmpty()) {
        return FrozenValue.newEmptyTuple()
    }
    val avalue = AValueImpl.new(TupleGen(elems), AValueFrozenTuple)
    return allocRaw(avalue).toFrozenValue()
}

/** Allocate a tuple from an iterator of elements. */
fun FrozenHeap.allocTupleIter(elems: Iterable<FrozenValue>): FrozenValue {
    val list = elems.toList()
    return allocTuple(list)
}


/** Allocate a tuple with the given elements. */
fun Heap.allocTuple(elems: List<Value>): Value {
    if (elems.isEmpty()) {
        return Value.newEmptyTuple()
    }
    val avalue = AValueImpl.new(TupleGen(elems), AValueTuple)
    return allocRaw(avalue).toValue()
}

/** Allocate a tuple from an iterator of elements. */
fun Heap.allocTupleIter(elems: Iterable<Value>): Value {
    val list = elems.toList()
    return allocTuple(list)
}

// register_special_avalue_frozen!(FrozenTuple, AValueFrozenTuple);
// Kotlin: vtable registration is handled differently; no macro expansion needed.
