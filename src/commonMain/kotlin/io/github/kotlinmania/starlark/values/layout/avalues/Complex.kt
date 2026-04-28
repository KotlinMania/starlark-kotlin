// port-lint: source src/values/layout/avalues/complex.rs
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

import io.github.kotlinmania.starlark.eval.compiler.DefGen
import io.github.kotlinmania.starlark.values.ComplexValue
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.Trace
import io.github.kotlinmania.starlark.values.layout.Freezer
import io.github.kotlinmania.starlark.values.layout.AValue
import io.github.kotlinmania.starlark.values.layout.AValueImpl
import io.github.kotlinmania.starlark.values.layout.AlignedSize
import io.github.kotlinmania.starlark.values.layout.ValueAllocSize
import io.github.kotlinmania.starlark.values.freezeerror.FreezeError
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.Freeze
import io.github.kotlinmania.starlark.values.FrozenRef
import io.github.kotlinmania.starlark.values.layout.tryFreezeDirectly
import io.github.kotlinmania.starlark.values.layout.heapCopyImpl

private sealed class AValueError : Exception() {
    class CannotBeFrozen(val typeName: String) : AValueError() {
        override val message: String get() = "Value of type `$typeName` cannot be frozen"
    }
}

/** AValue implementation for ComplexValue types that support freezing. */
// where
internal class AValueComplex(
    private val value: ComplexValue,
) : AValue {

    override fun extraLen(value: StarlarkValue): Int = 0

    override fun offsetOfExtra(): Int = 0

    override fun allocSizeForExtraLen(extraLen: Int): ValueAllocSize = ValueAllocSize(AlignedSize(0u))

    override fun heapFreeze(freezer: Freezer): Result<FrozenValue> {
        val direct = tryFreezeDirectly(value, freezer)
        if (direct != null) {
            return direct
        }

        // r.fill(res);
        val freezable = value as Freeze<StarlarkValue>
        val result = freezable.freeze(freezer)
        val frozen = result.getOrElse { return Result.failure(it) }

        val (fv, r) = freezer.reserve<AValue>()
        r.fill(frozen)

        if (frozen is DefGen<*>) {
            freezer.frozenDefs.add(FrozenRef(frozen as DefGen<FrozenValue>))
        }

        return Result.success(fv)
    }

    override fun heapCopy(tracer: Tracer): Value {
        return heapCopyImpl(value, tracer) { v, t -> (v as Trace).trace(t) }
    }

    override fun unpack(): StarlarkValue = value
}

/** AValue implementation for types that can be traced but cannot be frozen. */
// where
internal class AValueComplexNoFreeze(
    private val value: StarlarkValue,
) : AValue {

    override fun extraLen(value: StarlarkValue): Int = 0

    override fun offsetOfExtra(): Int = 0

    override fun allocSizeForExtraLen(extraLen: Int): ValueAllocSize = ValueAllocSize(AlignedSize(0u))

    override fun heapFreeze(_freezer: Freezer): Result<FrozenValue> {
        return Result.failure(
            FreezeError(AValueError.CannotBeFrozen(value::class.simpleName ?: "unknown").message)
        )
    }

    override fun heapCopy(tracer: Tracer): Value {
        return heapCopyImpl(value, tracer) { v, t -> (v as Trace).trace(t) }
    }

    override fun unpack(): StarlarkValue = value
}

/** Allocate a [ComplexValue] on the [Heap]. */
fun Heap.allocComplex(x: ComplexValue): Value {
    check(!x.isSpecial())
    return allocRaw(AValueImpl.new<AValueComplex>(x)).toValue()
}

/** Allocate a value which can be traced (garbage collected), but cannot be frozen. */
fun Heap.allocComplexNoFreeze(x: StarlarkValue): Value {
    check(x is Trace)
    check(!x.isSpecial())
    // When specializations are stable, we can have single `allocComplex` function,
    // which enables or not enables freezing depending on whether `T` implements `Freeze`.
    return allocRaw(AValueImpl.new<AValueComplexNoFreeze>(x)).toValue()
}
