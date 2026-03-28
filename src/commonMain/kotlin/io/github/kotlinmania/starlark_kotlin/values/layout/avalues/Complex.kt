// port-lint: source src/values/layout/avalues/complex.rs
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

import io.github.kotlinmania.starlark_kotlin.eval.compiler.FrozenDef
import io.github.kotlinmania.starlark_kotlin.values.ComplexValue
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.Trace
import io.github.kotlinmania.starlark_kotlin.values.layout.Freezer
import io.github.kotlinmania.starlark_kotlin.values.layout.AValue
import io.github.kotlinmania.starlark_kotlin.values.layout.AValueImpl
import io.github.kotlinmania.starlark_kotlin.values.layout.AlignedSize
import io.github.kotlinmania.starlark_kotlin.values.layout.ValueAllocSize
import io.github.kotlinmania.starlark_kotlin.values.freeze_error.FreezeError
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.Tracer
import io.github.kotlinmania.starlark_kotlin.values.tryFreezeDirectly
import io.github.kotlinmania.starlark_kotlin.values.layout.heapCopyImpl
import io.github.kotlinmania.starlark_kotlin.values.freeze_error.FreezeResult

// #[derive(Debug, thiserror::Error)]
// enum AValueError
private sealed class AValueError : Exception() {
    // #[error("Value of type `{0}` cannot be frozen")]
    // CannotBeFrozen(&'static str),
    class CannotBeFrozen(val typeName: String) : AValueError() {
        override val message: String get() = "Value of type `$typeName` cannot be frozen"
    }
}

/// AValue implementation for ComplexValue types that support freezing.
// struct AValueComplex<T>(PhantomData<T>);
// impl<'v, T> AValue<'v> for AValueComplex<T>
// where
//     T: ComplexValue<'v>,
//     T::Frozen: StarlarkValue<'static>,
internal class AValueComplex(
    private val value: ComplexValue,
) : AValue {
    // type StarlarkValue = T;
    // type ExtraElem = ();

    // fn extra_len(_value: &T) -> usize
    override fun extraLen(value: StarlarkValue): Int = 0

    // fn offset_of_extra() -> usize
    override fun offsetOfExtra(): Int = 0

    // fn alloc_size_for_extra_len(extra_len: usize) -> ValueAllocSize
    override fun allocSizeForExtraLen(extraLen: Int): ValueAllocSize = ValueAllocSize(AlignedSize(0u))

    // unsafe fn heap_freeze(me: *mut AValueRepr<Self::StarlarkValue>, freezer: &Freezer) -> FreezeResult<FrozenValue>
    override fun heapFreeze(freezer: Freezer): FreezeResult<FrozenValue> {
        // if let Some(f) = try_freeze_directly::<Self>(me, freezer)
        val direct = tryFreezeDirectly(value, freezer)
        if (direct != null) {
            return direct
        }

        // let (fv, r) = freezer.reserve::<AValueSimple<T::Frozen>>();
        // let x = AValueHeader::overwrite_with_forward(...);
        // let res = x.freeze(freezer)?;
        // r.fill(res);
        val result = value.freeze(freezer)
        val fv = result.getOrElse { return FreezeResult.failure(it) }

        // if TypeId::of::<T::Frozen>() == TypeId::of::<FrozenDef>()
        if (fv is FrozenDef) {
            freezer.frozenDefs.add(fv)
        }

        return FreezeResult.success(fv)
    }

    // unsafe fn heap_copy(me: *mut AValueRepr<Self::StarlarkValue>, tracer: &Tracer<'v>) -> Value<'v>
    override fun heapCopy(tracer: Tracer): Value {
        return heapCopyImpl(value, tracer) { v, t -> (v as Trace).trace(t) }
    }

    override fun unpack(): StarlarkValue = value
}

/// AValue implementation for types that can be traced but cannot be frozen.
// pub(crate) struct AValueComplexNoFreeze<T>(PhantomData<T>);
// impl<'v, T> AValue<'v> for AValueComplexNoFreeze<T>
// where
//     T: StarlarkValue<'v> + Trace<'v>,
internal class AValueComplexNoFreeze(
    private val value: StarlarkValue,
) : AValue {
    // type StarlarkValue = T;
    // type ExtraElem = ();

    // fn extra_len(_value: &T) -> usize
    override fun extraLen(value: StarlarkValue): Int = 0

    // fn offset_of_extra() -> usize
    override fun offsetOfExtra(): Int = 0

    // fn alloc_size_for_extra_len(extra_len: usize) -> ValueAllocSize
    override fun allocSizeForExtraLen(extraLen: Int): ValueAllocSize = ValueAllocSize(AlignedSize(0u))

    // unsafe fn heap_freeze(...) -> FreezeResult<FrozenValue>
    override fun heapFreeze(freezer: Freezer): FreezeResult<FrozenValue> {
        return FreezeResult.failure(
            FreezeError(AValueError.CannotBeFrozen(value::class.simpleName ?: "unknown").message!!)
        )
    }

    // unsafe fn heap_copy(me: *mut AValueRepr<Self::StarlarkValue>, tracer: &Tracer<'v>) -> Value<'v>
    override fun heapCopy(tracer: Tracer): Value {
        return heapCopyImpl(value, tracer) { v, t -> (v as Trace).trace(t) }
    }

    override fun unpack(): StarlarkValue = value
}

// impl<'v> Heap<'v>

/// Allocate a [ComplexValue] on the [Heap].
// pub fn alloc_complex<T>(self, x: T) -> Value<'v>
fun Heap.allocComplex(x: ComplexValue): Value {
    check(!x.isSpecial())
    return allocRaw(AValueImpl.new<AValueComplex>(x)).toValue()
}

/// Allocate a value which can be traced (garbage collected), but cannot be frozen.
// pub fn alloc_complex_no_freeze<T>(self, x: T) -> Value<'v>
fun Heap.allocComplexNoFreeze(x: StarlarkValue): Value {
    check(x is Trace)
    check(!x.isSpecial())
    // When specializations are stable, we can have single `alloc_complex` function,
    // which enables or not enables freezing depending on whether `T` implements `Freeze`.
    return allocRaw(AValueImpl.new<AValueComplexNoFreeze>(x)).toValue()
}
