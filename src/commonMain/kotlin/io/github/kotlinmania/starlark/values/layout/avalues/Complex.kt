// port-lint: source src/values/layout/avalues/complex.rs
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

import io.github.kotlinmania.starlark.eval.compiler.FrozenDefPostFreeze
import io.github.kotlinmania.starlark.values.ComplexValue
import io.github.kotlinmania.starlark.values.Freeze
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.Trace
import io.github.kotlinmania.starlark.values.freezeerror.FreezeError
import io.github.kotlinmania.starlark.values.layout.AValue
import io.github.kotlinmania.starlark.values.layout.AValueImpl
import io.github.kotlinmania.starlark.values.layout.AlignedSize
import io.github.kotlinmania.starlark.values.layout.Freezer
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.ValueAllocSize
import io.github.kotlinmania.starlark.values.layout.heap.AValueHeader
import io.github.kotlinmania.starlark.values.layout.heap.AValueRepr
import io.github.kotlinmania.starlark.values.layout.heap.ForwardPtr
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.layout.heapCopyImpl
import io.github.kotlinmania.starlark.values.layout.tryFreezeDirectly

private sealed class AValueError : Exception() {
    class CannotBeFrozen(
        val typeName: String,
    ) : AValueError() {
        override val message: String get() = "Value of type `$typeName` cannot be frozen"
    }
}

/** AValue implementation for ComplexValue types that support freezing. */
internal class AValueComplex(
    private val value: StarlarkValue,
) : AValue {
    override fun extraLen(value: StarlarkValue): Int = 0

    override fun offsetOfExtra(): Int = 0

    override fun allocSizeForExtraLen(extraLen: Int): ValueAllocSize = ValueAllocSize(AlignedSize(0u))

    override fun heapFreeze(freezer: Freezer): Result<FrozenValue> {
        val direct = tryFreezeDirectly(value, freezer)
        if (direct != null) {
            return direct
        }

        @Suppress("UNCHECKED_CAST")
        val freezeValue = value as Freeze<StarlarkValue>
        val result = freezeValue.freeze(freezer)
        val frozen = result.getOrElse { return Result.failure(it) }

        val (fv, r) = freezer.reserve<AValue>()
        r.fill(frozen)

        if (frozen is FrozenDefPostFreeze) {
            freezer.frozenDefs.add(
                io.github.kotlinmania.starlark.values
                    .FrozenRef(frozen),
            )
        }

        return Result.success(fv)
    }

    override fun heapFreeze(
        repr: AValueRepr<*>,
        freezer: Freezer,
    ): Result<FrozenValue> {
        val direct = tryFreezeDirectly(value, freezer)
        if (direct != null) {
            if (direct.isSuccess) {
                AValueHeader.overwriteWithForward(repr, ForwardPtr.newFrozen(direct.getOrThrow()))
            }
            return direct
        }

        val (fv, r) = freezer.reserve<AValue>()
        AValueHeader.overwriteWithForward(repr, ForwardPtr.newFrozen(fv))

        @Suppress("UNCHECKED_CAST")
        val freezeValue = value as Freeze<StarlarkValue>
        val result = freezeValue.freeze(freezer)
        val frozen = result.getOrElse { return Result.failure(it) }
        r.fill(frozen)

        if (frozen is FrozenDefPostFreeze) {
            freezer.frozenDefs.add(
                io.github.kotlinmania.starlark.values
                    .FrozenRef(frozen),
            )
        }

        return Result.success(fv)
    }

    override fun heapCopy(repr: AValueRepr<*>, tracer: Tracer): Value = heapCopyImpl(repr, value, tracer) { v, t -> (v as Trace).trace(t) }

    override fun unpack(): StarlarkValue = value
}

/** AValue implementation for types that can be traced but cannot be frozen. */
internal class AValueComplexNoFreeze(
    private val value: StarlarkValue,
) : AValue {
    override fun extraLen(value: StarlarkValue): Int = 0

    override fun offsetOfExtra(): Int = 0

    override fun allocSizeForExtraLen(extraLen: Int): ValueAllocSize = ValueAllocSize(AlignedSize(0u))

    override fun heapFreeze(freezer: Freezer): Result<FrozenValue> =
        Result.failure(
            FreezeError(AValueError.CannotBeFrozen(value::class.simpleName ?: "unknown").message),
        )

    override fun heapCopy(repr: AValueRepr<*>, tracer: Tracer): Value = heapCopyImpl(repr, value, tracer) { v, t -> (v as Trace).trace(t) }

    override fun unpack(): StarlarkValue = value
}

/** Allocate a [ComplexValue] on the [Heap]. */
fun <T> Heap.allocComplex(x: T): Value where T : ComplexValue, T : Freeze<out StarlarkValue> {
    check(!x.isSpecial())
    return allocRaw(AValueImpl.new(x, AValueComplex(x))).toValue()
}

/** Allocate a [ComplexValue] of unknown static type on the [Heap]. */
fun Heap.allocComplexAny(x: Any): Value {
    check(x is ComplexValue)
    check(x is Freeze<*>)
    @Suppress("UNCHECKED_CAST")
    fun <T> Heap.allocComplexHelper(v: Any): Value where T : ComplexValue, T : Freeze<out StarlarkValue> =
        this.allocComplex(v as T)
    return allocComplexHelper<io.github.kotlinmania.starlark.values.types.anycomplex.StarlarkAnyComplex<Any>>(x)
}


/** Allocate a value which can be traced (garbage collected), but cannot be frozen. */
fun Heap.allocComplexNoFreeze(x: StarlarkValue): Value {
    check(x is Trace)
    check(!x.isSpecial())
    // When specializations are stable, we can have single `alloc_complex` function,
    // which enables or not enables freezing depending on whether `T` implements `Freeze`.
    return allocRaw(AValueImpl.new(x, AValueComplexNoFreeze(x))).toValue()
}
