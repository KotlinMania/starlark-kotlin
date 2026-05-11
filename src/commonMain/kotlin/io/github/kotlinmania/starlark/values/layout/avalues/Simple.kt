// port-lint: source values/layout/avalues/simple.rs
package io.github.kotlinmania.starlark.values.layout.avalues.simple

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
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.Freezer
import io.github.kotlinmania.starlark.values.layout.AValue
import io.github.kotlinmania.starlark.values.layout.AValueImpl
import io.github.kotlinmania.starlark.values.layout.heapCopyImpl
import io.github.kotlinmania.starlark.values.layout.heapFreezeSimpleImpl
import io.github.kotlinmania.starlark.values.layout.tryFreezeDirectly
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.layout.FrozenValueTyped

internal fun <T : StarlarkValue> simple(x: T): AValueImpl<AValueSimple<T>> {
    return AValueImpl.new(x)
}

/** AValue implementation for simple Starlark values. */
class AValueSimple<T : StarlarkValue>(
    private val inner: T,
) : AValue {

    override fun extraLen(value: StarlarkValue): Int = 0

    override fun offsetOfExtra(): Int = 0

    override fun heapFreeze(freezer: Freezer): Result<FrozenValue> {
        val direct = tryFreezeDirectly(inner, freezer)
        if (direct != null) return direct
        return heapFreezeSimpleImpl(inner, freezer)
    }

    override fun heapCopy(tracer: Tracer): Value {
        return heapCopyImpl(inner, tracer) { _, _ -> }
    }

    override fun unpack(): StarlarkValue = inner
}

/** Extension function on FrozenHeap for simple typed static allocation. */
fun <T : StarlarkValue> FrozenHeap.allocSimpleTypedStatic(val_: T): FrozenValueTyped<T> {
    return allocRaw(simple(val_)) as FrozenValueTyped<T>
}

/** Allocate a value on the heap. */
fun <T : StarlarkValue> FrozenHeap.allocSimpleTyped(val_: T): FrozenValueTyped<T> {
    return allocRaw(simple(val_)) as FrozenValueTyped<T>
}

/**
 * Allocate a simple [`StarlarkValue`] on this heap.
 *
 * Simple value is any starlark value which:
 * * bound to global scope (in particular, it cannot contain references to other `Value`s)
 * * is not special builtin (e.g. `None`)
 */
fun <T : StarlarkValue> FrozenHeap.allocSimple(val_: T): FrozenValue {
    return allocSimpleTypedStatic(val_).toFrozenValue()
}

/** Allocate a simple [`StarlarkValue`] on this heap. */
fun <T : StarlarkValue> Heap.allocSimple(x: T): Value {
    return allocRaw(simple(x)).toValue()
}
