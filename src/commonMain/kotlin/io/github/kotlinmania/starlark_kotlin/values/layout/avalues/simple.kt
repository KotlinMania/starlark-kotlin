// port-lint: source src/values/layout/avalues/simple.rs
package io.github.kotlinmania.starlark_kotlin.values.layout.avalues.simple

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
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.FrozenValueTyped
import io.github.kotlinmania.starlark_kotlin.values.Heap
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.Tracer
import io.github.kotlinmania.starlark_kotlin.values.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.Freezer
import io.github.kotlinmania.starlark_kotlin.values.layout.avalue.AValue
import io.github.kotlinmania.starlark_kotlin.values.layout.avalue.AValueImpl
import io.github.kotlinmania.starlark_kotlin.values.layout.value_alloc_size.ValueAllocSize

// pub(crate) fn simple<'v, T: StarlarkValue<'v>>(x: T) -> AValueImpl<'v, AValueSimple<T>>
internal fun <T : StarlarkValue> simple(x: T): AValueImpl<AValueSimple<T>> {
    return AValueImpl.new(x)
}

/// AValue implementation for simple Starlark values.
// pub struct AValueSimple<T>(PhantomData<T>);
// Kotlin: GC handles memory. AValueSimple is a marker class wrapping a StarlarkValue.
class AValueSimple<T : StarlarkValue>(
    private val inner: T,
) : AValue {

    // fn extra_len(_value: &T) -> usize
    override fun extraLen(value: StarlarkValue): Int = 0

    // fn offset_of_extra() -> usize
    override fun offsetOfExtra(): Int = 0

    // fn alloc_size_for_extra_len(extra_len: usize) -> ValueAllocSize
    override fun allocSizeForExtraLen(extraLen: Int): ValueAllocSize {
        return ValueAllocSize.ofBytes(0)
    }

    // unsafe fn heap_freeze(me, freezer) -> FreezeResult<FrozenValue>
    override fun heapFreeze(freezer: Freezer): FreezeResult<FrozenValue> {
        return freezer.freeze(inner)
    }

    // unsafe fn heap_copy(me, tracer) -> Value<'v>
    override fun heapCopy(tracer: Tracer): Value {
        return tracer.trace(inner)
    }

    override fun unpack(): StarlarkValue = inner
}

/// Extension function on FrozenHeap for simple typed static allocation.
// impl FrozenHeap
// pub(crate) fn alloc_simple_typed_static<T>(&self, val: T) -> FrozenValueTyped<'static, T>
fun <T : StarlarkValue> FrozenHeap.allocSimpleTypedStatic(val_: T): FrozenValueTyped<T> {
    return allocRaw(simple(val_))
}

/// Allocate a value on the heap.
// pub fn alloc_simple_typed<'fv, T>(&'fv self, val: T) -> FrozenValueTyped<'fv, T>
fun <T : StarlarkValue> FrozenHeap.allocSimpleTyped(val_: T): FrozenValueTyped<T> {
    return allocRaw(simple(val_))
}

/// Allocate a simple [`StarlarkValue`] on this heap.
///
/// Simple value is any starlark value which:
/// * bound by `'static` lifetime (in particular, it cannot contain references to other `Value`s)
/// * is not special builtin (e.g. `None`)
// pub fn alloc_simple<T>(&self, val: T) -> FrozenValue
fun <T : StarlarkValue> FrozenHeap.allocSimple(val_: T): FrozenValue {
    return allocSimpleTypedStatic(val_).toFrozenValue()
}

/// Allocate a simple [`StarlarkValue`] on this heap.
// impl Heap { pub fn alloc_simple<T>(&self, x: T) -> Value<'v> }
fun <T : StarlarkValue> Heap.allocSimple(x: T): Value {
    return allocRaw(simple(x)).toValue()
}
