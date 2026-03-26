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
import io.github.kotlinmania.starlark_kotlin.values.Tracer
import io.github.kotlinmania.starlark_kotlin.values.freeze_error.FreezeResult
import io.github.kotlinmania.starlark_kotlin.values.layout.Freezer
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.aligned_size.AlignedSize
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.AValueHeader
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.ForwardPtr
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.AValueRepr
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.arena.MIN_ALLOC
import io.github.kotlinmania.starlark_kotlin.values.layout.value_alloc_size.ValueAllocSize

/**
 * Extended vtable methods (those not covered by [StarlarkValue]).
 *
 * In Rust this is `pub(crate) trait AValue<'v>: Sized + 'v`.
 * The lifetime parameter `'v` is not needed in Kotlin because GC handles lifetimes.
 */
// pub(crate) trait AValue<'v>: Sized + 'v
internal interface AValue {

    /**
     * Certain types like `Tuple` or `StarlarkStr` have payload array
     * placed in a heap after `Self`. This is the type of an element of that array.
     *
     * In Rust: `type ExtraElem: 'v`
     *
     * In Kotlin, the extra element size is represented by [extraElemSize].
     */
    val extraElemSize: Int get() = 0

    /** Payload array length. */
    // fn extra_len(value: &Self::StarlarkValue) -> usize
    fun extraLen(value: StarlarkValue): Int

    /**
     * Offset of field holding content, in bytes.
     *
     * Return size of self if there's no extra content.
     */
    // fn offset_of_extra() -> usize
    fun offsetOfExtra(): Int

    /** Type is `StarlarkStr`. */
    // const IS_STR: bool = false
    val isStr: Boolean get() = false

    /**
     * Memory size of starlark value including `AValueHeader`.
     *
     * Computes the allocation size as the maximum of:
     * - The aligned size of the `AValueRepr` for the starlark value
     * - The minimum allocation size ([MIN_ALLOC])
     * - The aligned size of the extra content area
     */
    // fn alloc_size_for_extra_len(extra_len: usize) -> ValueAllocSize
    fun allocSizeForExtraLen(extraLen: Int): ValueAllocSize {
        val elemSize = extraElemSize
        require(elemSize == 0 || offsetOfExtra() % elemSize == 0) {
            "extra must be aligned"
        }
        // cmp::max(cmp::max(AlignedSize::of::<AValueRepr<Self::StarlarkValue>>(), MIN_ALLOC),
        //          AlignedSize::align_up(AValueRepr::<Self>::offset_of_extra()
        //              + (mem::size_of::<Self::ExtraElem>() * extra_len)))
        val baseSize = AlignedSize.alignUp(offsetOfExtra())
        val minAllocSize = MIN_ALLOC
        val extraSize = AlignedSize.alignUp(
            offsetOfExtra() + (elemSize * extraLen)
        )
        return ValueAllocSize.new(
            maxOf(baseSize, minAllocSize, extraSize)
        )
    }

    /**
     * The memory that should be charged to this value in a profile.
     *
     * Both the size of the value itself and anything it references.
     *
     * This existing is a bit of a hack to let statically allocated values set this to zero.
     */
    // fn total_memory_for_profile(value: &Self::StarlarkValue) -> usize
    fun totalMemoryForProfile(value: StarlarkValue): Int {
        return allocSizeForExtraLen(extraLen(value)).bytes().toInt()
    }

    /**
     * Freeze this value on the heap.
     *
     * In Rust: `unsafe fn heap_freeze<'fv>(me: *mut AValueRepr<Self::StarlarkValue>, freezer: &Freezer<'fv>) -> FreezeResult<FrozenValue>`
     */
    // unsafe fn heap_freeze(me, freezer) -> FreezeResult<FrozenValue>
    fun heapFreeze(freezer: Freezer): FreezeResult<FrozenValue>

    /**
     * Copy this value on the heap during GC.
     *
     * In Rust: `unsafe fn heap_copy(me: *mut AValueRepr<Self::StarlarkValue>, tracer: &Tracer<'v>) -> Value<'v>`
     */
    // unsafe fn heap_copy(me, tracer) -> Value<'v>
    fun heapCopy(tracer: Tracer): Value

    /** Get the underlying [StarlarkValue]. */
    fun unpack(): StarlarkValue
}

/**
 * A value with extended ([AValue]) vtable methods.
 *
 * In Rust: `#[repr(C)] pub(crate) struct AValueImpl<'v, T: AValue<'v>>(PhantomData<T>, pub(crate) T::StarlarkValue)`
 *
 * The `PhantomData<T>` is not needed in Kotlin because the type parameter is erased at runtime
 * but retained at compile time.
 */
// #[repr(C)]
// pub(crate) struct AValueImpl<'v, T: AValue<'v>>(PhantomData<T>, pub(crate) T::StarlarkValue)
internal class AValueImpl<T : AValue>(
    internal val value: StarlarkValue,
) {
    companion object {
        // pub(crate) const fn new(value: T::StarlarkValue) -> Self
        fun <T : AValue> new(value: StarlarkValue): AValueImpl<T> {
            return AValueImpl(value)
        }
    }
}

/**
 * If `A` provides a statically allocated frozen value,
 * replace object with the forward to that frozen value instead of using default freeze.
 *
 * In Rust this overwrites the object header with a forward pointer via
 * `AValueHeader::overwrite_with_forward`. In Kotlin, GC manages references
 * so no forwarding pointer needs to be written; we simply return the frozen value.
 *
 * @return `null` if the value does not support direct freezing,
 *   otherwise the [FreezeResult] of the frozen value.
 */
// pub(super) unsafe fn try_freeze_directly<'v, A>(
//     me: *mut AValueRepr<A::StarlarkValue>,
//     freezer: &Freezer<'_>,
// ) -> Option<FreezeResult<FrozenValue>>
// where A: AValue<'v>
internal fun tryFreezeDirectly(
    payload: StarlarkValue,
    freezer: Freezer,
): FreezeResult<FrozenValue>? {
    val f = payload.tryFreezeDirectly(freezer) ?: return null
    return when {
        f.isSuccess -> {
            val frozenValue = f.getOrThrow()
            // Rust: drop(AValueHeader::overwrite_with_forward::<A::StarlarkValue>(
            //     me, ForwardPtr::new_frozen(f)))
            // In Kotlin, GC manages references; no forwarding pointer overwrite needed.
            Result.success(frozenValue)
        }
        else -> f
    }
}

/**
 * `heap_freeze` implementation for simple [StarlarkValue] and `StarlarkFloat`.
 *
 * (`StarlarkFloat` is logically a simple type, but it is not considered simple type.)
 *
 * In Rust, this reserves space on the frozen heap, overwrites the original with a forward
 * pointer, and fills the reservation with the moved value. In Kotlin, the GC handles
 * object relocation, so we simply reserve, fill, and return the frozen value.
 */
// pub(super) unsafe fn heap_freeze_simple_impl<'v, A>(
//     me: *mut AValueRepr<A::StarlarkValue>,
//     freezer: &Freezer,
// ) -> FreezeResult<FrozenValue>
// where
//     A: AValue<'v, ExtraElem = ()>,
//     A::StarlarkValue: HeapSendable<'v> + HeapSyncable<'v>,
internal fun heapFreezeSimpleImpl(
    value: StarlarkValue,
    freezer: Freezer,
): FreezeResult<FrozenValue> {
    // let (fv, r) = freezer.reserve::<A>();
    val (fv, r) = freezer.reserve<AValue>()
    // let x = AValueHeader::overwrite_with_forward::<A::StarlarkValue>(
    //     me, ForwardPtr::new_frozen(fv));
    // r.fill(x);
    r.fill(value)
    return Result.success(fv)
}

/**
 * Common `heap_copy` implementation for types without extra.
 *
 * In Rust, this reserves space on the new heap, overwrites the original with a forward
 * pointer (to handle cycles), traces the value, and fills the reservation. In Kotlin,
 * GC handles cycles, but we preserve the trace-before-fill ordering for correctness.
 */
// pub(super) unsafe fn heap_copy_impl<'v, A>(
//     me: *mut AValueRepr<A::StarlarkValue>,
//     tracer: &Tracer<'v>,
//     trace: impl FnOnce(&mut A::StarlarkValue, &Tracer<'v>),
// ) -> Value<'v>
// where
//     A: AValue<'v, ExtraElem = ()>,
internal fun heapCopyImpl(
    value: StarlarkValue,
    tracer: Tracer,
    trace: (StarlarkValue, Tracer) -> Unit,
): Value {
    // let (v, r) = tracer.reserve::<A>();
    val (v, r) = tracer.reserve<AValue>()
    // let mut x = AValueHeader::overwrite_with_forward::<A::StarlarkValue>(
    //     me, ForwardPtr::new_unfrozen(v));
    // We have to put the forwarding node in _before_ we trace in case there are cycles
    trace(value, tracer)
    r.fill(value)
    return v
}

/**
 * Placeholder used during GC to fill space vacated by a moved object.
 *
 * When an object is moved during garbage collection, the original memory is
 * overwritten with a `BlackHole` that records the allocation size so the
 * arena can still iterate past it.
 */
// #[derive(Debug, Display, ProvidesStaticType, Allocative)]
// #[display("BlackHole")]
// pub(crate) struct BlackHole(pub(crate) ValueAllocSize)
internal class BlackHole(
    internal val size: ValueAllocSize,
) {
    override fun toString(): String = "BlackHole"
}

// ---- Extension functions for dependents ----

/**
 * Compute the total memory charged to this header's value for profiling purposes.
 *
 * Delegates to the vtable's memory size function, which corresponds to
 * `AValue::total_memory_for_profile` in Rust.
 */
internal fun AValueHeader.totalMemoryForProfile(): Long {
    return allocSize().bytes().toLong()
}

/**
 * Copy the value behind this header using the given tracer.
 *
 * Delegates to the vtable's heap copy function, which corresponds to
 * `AValue::heap_copy` in Rust.
 */
internal fun AValueHeader.heapCopy(tracer: Tracer): Value {
    return unpack().heapCopy(tracer)
}

/**
 * Return the length (number of elements) of a list.
 *
 * Corresponds to uses of `.len()` on Rust slices/vectors.
 * In Kotlin, [List.size] already provides this, but this top-level function
 * exists as an explicit mapping target for the Rust `len()` calls.
 */
internal fun <T> size(list: List<T>): Int = list.size

/**
 * Return the length (number of elements) of an array.
 */
internal fun <T> size(array: Array<T>): Int = array.size
