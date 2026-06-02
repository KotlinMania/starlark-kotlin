// port-lint: source src/values/layout/avalues/simple.rs
package io.github.kotlinmania.starlark.values.layout.avalues.simple

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
import io.github.kotlinmania.starlark.values.layout.FrozenValueTyped
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.AValueHeader
import io.github.kotlinmania.starlark.values.layout.heap.AValueRepr
import io.github.kotlinmania.starlark.values.layout.heap.ForwardPtr
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.layout.heapCopyImpl
import io.github.kotlinmania.starlark.values.layout.heapFreezeSimpleImpl
import io.github.kotlinmania.starlark.values.layout.tryFreezeDirectly

internal fun <T : StarlarkValue> simple(x: T): AValueImpl<AValueSimple<T>> = AValueImpl.new(x, AValueSimple(x))

/** AValue implementation for simple Starlark values. */
// Kotlin: GC handles memory. AValueSimple is a marker class wrapping a StarlarkValue.
internal class AValueSimple<T : StarlarkValue>(
    private val inner: T,
) : AValue {
    override fun extraLen(value: StarlarkValue): Int = 0

    override fun offsetOfExtra(): Int = 0

    override fun heapFreeze(freezer: Freezer): Result<FrozenValue> {
        val direct = tryFreezeDirectly(inner, freezer)
        if (direct != null) return direct
        return heapFreezeSimpleImpl(inner, freezer)
    }

    override fun heapFreeze(
        repr: AValueRepr<*>,
        freezer: Freezer,
    ): Result<FrozenValue> {
        val direct = tryFreezeDirectly(inner, freezer)
        if (direct != null) {
            if (direct.isSuccess) {
                AValueHeader.overwriteWithForward(repr, ForwardPtr.newFrozen(direct.getOrThrow()))
            }
            return direct
        }
        val (fv, r) = freezer.reserve<AValue>()
        val x = AValueHeader.overwriteWithForward(repr, ForwardPtr.newFrozen(fv))
        r.fill(x)
        return Result.success(fv)
    }

    override fun heapCopy(tracer: Tracer): Value = heapCopyImpl(inner, tracer) { _, _ -> }

    override fun unpack(): StarlarkValue = inner
}

/** Extension function on FrozenHeap for simple typed static allocation. */
@Suppress("UNCHECKED_CAST")
internal fun <T : StarlarkValue> FrozenHeap.allocSimpleTypedStatic(value: T): FrozenValueTyped<T> = allocRaw(simple(value)) as FrozenValueTyped<T>

/** Allocate a value on the heap. */
@Suppress("UNCHECKED_CAST")
internal fun <T : StarlarkValue> FrozenHeap.allocSimpleTyped(value: T): FrozenValueTyped<T> = allocRaw(simple(value)) as FrozenValueTyped<T>

/**
 * Allocate a simple [`StarlarkValue`] on this heap.
 *
 * Simple value is any starlark value which:
 * * bound by `'static` lifetime (in particular, it cannot contain references to other `Value`s)
 * * is not special builtin (e.g. `None`)
 */
internal fun <T : StarlarkValue> FrozenHeap.allocSimple(value: T): FrozenValue = allocSimpleTypedStatic(value).toFrozenValue()

/** Allocate a simple [`StarlarkValue`] on this heap. */
internal fun <T : StarlarkValue> Heap.allocSimple(x: T): Value = allocRaw(simple(x)).toValue()
