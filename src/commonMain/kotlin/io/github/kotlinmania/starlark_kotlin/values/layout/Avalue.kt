// port-lint: source src/values/layout/avalue.rs
package io.github.kotlinmania.starlark_kotlin.values.layout.avalue

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

import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.layout.Freezer
import io.github.kotlinmania.starlark_kotlin.values.layout.value_alloc_size.ValueAllocSize
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.toValue
import io.github.kotlinmania.starlark_kotlin.values.Tracer
import io.github.kotlinmania.starlark_kotlin.tests.freeze
import io.github.kotlinmania.starlark_kotlin.values.freeze_error.FreezeResult

/// Extended vtable methods (those not covered by `StarlarkValue`).
// pub(crate) trait AValue<'v>: Sized + 'v
internal interface AValue {
    // type StarlarkValue: StarlarkValue<'v>;
    // Kotlin: associated type represented as a generic bound on implementations.

    // type ExtraElem: 'v;
    // Kotlin: no extra element type needed; no raw memory layout.

    // fn extra_len(value: &Self::StarlarkValue) -> usize;
    /** Payload array length. */
    fun extraLen(value: StarlarkValue): Int

    // fn offset_of_extra() -> usize;
    /** Offset of field holding content, in bytes. */
    fun offsetOfExtra(): Int

    // const IS_STR: bool = false;
    /** Type is `StarlarkStr`. */
    val isStr: Boolean get() = false

    // fn alloc_size_for_extra_len(extra_len: usize) -> ValueAllocSize
    /** Memory size of starlark value including `AValueHeader`. */
    fun allocSizeForExtraLen(extraLen: Int): ValueAllocSize

    // fn total_memory_for_profile(value: &Self::StarlarkValue) -> usize
    /** The memory that should be charged to this value in a profile. */
    fun totalMemoryForProfile(value: StarlarkValue): Int {
        return allocSizeForExtraLen(extraLen(value)).bytes()
    }

    // unsafe fn heap_freeze<'fv>(me: *mut AValueRepr<Self::StarlarkValue>, freezer: &Freezer<'fv>) -> FreezeResult<FrozenValue>
    /** Freeze this value on the heap. */
    fun heapFreeze(freezer: Freezer): FreezeResult<FrozenValue>

    // unsafe fn heap_copy(me: *mut AValueRepr<Self::StarlarkValue>, tracer: &Tracer<'v>) -> Value<'v>
    /** Copy this value on the heap during GC. */
    fun heapCopy(tracer: Tracer): Value

    /** Get the underlying [StarlarkValue]. */
    fun unpack(): StarlarkValue
}

/// A value with extended (`AValue`) vtable methods.
// #[repr(C)]
// pub(crate) struct AValueImpl<'v, T: AValue<'v>>(PhantomData<T>, pub(crate) T::StarlarkValue);
internal class AValueImpl<T : AValue>(
    internal val value: StarlarkValue,
) {
    // impl<'v, T: AValue<'v>> AValueImpl<'v, T>

    companion object {
        // pub(crate) const fn new(value: T::StarlarkValue) -> Self
        fun <T : AValue> new(value: StarlarkValue): AValueImpl<T> {
            return AValueImpl(value)
        }
    }
}

/// If `A` provides a statically allocated frozen value,
/// replace object with the forward to that frozen value instead of using default freeze.
// pub(super) unsafe fn try_freeze_directly<'v, A>(...)
internal fun tryFreezeDirectly(
    payload: StarlarkValue,
    freezer: Freezer,
): FreezeResult<FrozenValue>? {
    // unsafe {
    //     let f = match (*me).payload.try_freeze_directly(freezer)? {
    //         Ok(x) => x,
    //         Err(e) => return Some(Err(e)),
    //     };
    //     drop(AValueHeader::overwrite_with_forward::<A::StarlarkValue>(
    //         me, ForwardPtr::new_frozen(f),
    //     ));
    //     Some(Ok(f))
    // }
    val result = payload.tryFreezeDirectly(freezer) ?: return null
    return result.map { f ->
        // In Rust: overwrite with forward pointer.
        // In Kotlin: GC manages references; no forwarding needed.
        f
    }
}

/// `heap_freeze` implementation for simple `StarlarkValue` and `StarlarkFloat`.
// pub(super) unsafe fn heap_freeze_simple_impl<'v, A>(...)
internal fun heapFreezeSimpleImpl(
    value: StarlarkValue,
    freezer: Freezer,
): FreezeResult<FrozenValue> {
    // unsafe {
    //     let (fv, r) = freezer.reserve::<A>();
    //     let x = AValueHeader::overwrite_with_forward::<A::StarlarkValue>(
    //         me, ForwardPtr::new_frozen(fv),
    //     );
    //     r.fill(x);
    //     Ok(fv)
    // }
    // Kotlin: No raw memory manipulation.
    // Simple freeze: allocate on frozen heap and return the frozen value.
    return value.freeze(freezer)
}

/// Common `heap_copy` implementation for types without extra.
// pub(super) unsafe fn heap_copy_impl<'v, A>(...)
internal fun heapCopyImpl(
    value: StarlarkValue,
    tracer: Tracer,
    trace: (StarlarkValue, Tracer) -> Unit,
): Value {
    // unsafe {
    //     let (v, r) = tracer.reserve::<A>();
    //     let mut x = AValueHeader::overwrite_with_forward::<A::StarlarkValue>(
    //         me, ForwardPtr::new_unfrozen(v),
    //     );
    //     trace(&mut x, tracer);
    //     r.fill(x);
    //     v
    // }
    // Kotlin: No raw memory manipulation.
    // Copy: trace the value and return the traced copy.
    trace(value, tracer)
    return value.toValue()
}

// #[derive(Debug, Display, ProvidesStaticType, Allocative)]
// #[display("BlackHole")]
// pub(crate) struct BlackHole(pub(crate) ValueAllocSize);
internal class BlackHole(
    internal val size: ValueAllocSize,
) {
    override fun toString(): String = "BlackHole"
}

// #[cfg(test)] mod tests
// Tests are in commonTest, not here.
