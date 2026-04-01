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

import io.github.kotlinmania.starlark_kotlin.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.layout.Freezer
import io.github.kotlinmania.starlark_kotlin.values.layout.AValue
import io.github.kotlinmania.starlark_kotlin.values.layout.AValueImpl
import io.github.kotlinmania.starlark_kotlin.values.layout.heapCopyImpl
import io.github.kotlinmania.starlark_kotlin.values.layout.heapFreezeSimpleImpl
import io.github.kotlinmania.starlark_kotlin.values.layout.tryFreezeDirectly
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Tracer
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValueTyped

// pub(crate) fn simple<'v, T: StarlarkValue<'v>>(x: T) -> AValueImpl<'v, AValueSimple<T>>
internal fun <T : StarlarkValue> simple(x: T): AValueImpl<AValueSimple<T>> {
    return AValueImpl.new(x)
}

/** AValue implementation for simple Starlark values. */
// pub struct AValueSimple<T>(PhantomData<T>);
// Kotlin: GC handles memory. AValueSimple is a marker class wrapping a StarlarkValue.
class AValueSimple<T : StarlarkValue>(
    private val inner: T,
) : AValue {

    // fn extra_len(_value: &T) -> usize
    override fun extraLen(value: StarlarkValue): Int = 0

    // fn offset_of_extra() -> usize
    override fun offsetOfExtra(): Int = 0

    // unsafe fn heap_freeze(me, freezer) -> Result<FrozenValue>
    override fun heapFreeze(freezer: Freezer): Result<FrozenValue> {
        val direct = tryFreezeDirectly(inner, freezer)
        if (direct != null) return direct
        return heapFreezeSimpleImpl(inner, freezer)
    }

    // unsafe fn heap_copy(me, tracer) -> Value<'v>
    override fun heapCopy(tracer: Tracer): Value {
        return heapCopyImpl(inner, tracer) { _, _ -> }
    }

    override fun unpack(): StarlarkValue = inner
}

/** Extension function on FrozenHeap for simple typed static allocation. */
// impl FrozenHeap
// pub(crate) fn alloc_simple_typed_static<T>(&self, val: T) -> FrozenValueTyped<'static, T>
@Suppress("UNCHECKED_CAST")
fun <T : StarlarkValue> FrozenHeap.allocSimpleTypedStatic(value: T): FrozenValueTyped<T> {
    return allocRaw(simple(value)) as FrozenValueTyped<T>
}

/** Allocate a value on the heap. */
// pub fn alloc_simple_typed<'fv, T>(&'fv self, val: T) -> FrozenValueTyped<'fv, T>
@Suppress("UNCHECKED_CAST")
fun <T : StarlarkValue> FrozenHeap.allocSimpleTyped(value: T): FrozenValueTyped<T> {
    return allocRaw(simple(value)) as FrozenValueTyped<T>
}

/**
 * Allocate a simple [`StarlarkValue`] on this heap.
 *
 * Simple value is any starlark value which:
 * * bound by `'static` lifetime (in particular, it cannot contain references to other `Value`s)
 * * is not special builtin (e.g. `None`)
 */
// pub fn alloc_simple<T>(&self, val: T) -> FrozenValue
fun <T : StarlarkValue> FrozenHeap.allocSimple(value: T): FrozenValue {
    return allocSimpleTypedStatic(value).toFrozenValue()
}

/** Allocate a simple [`StarlarkValue`] on this heap. */
// impl Heap { pub fn alloc_simple<T>(&self, x: T) -> Value<'v> }
fun <T : StarlarkValue> Heap.allocSimple(x: T): Value {
    return allocRaw(simple(x)).toValue()
}
