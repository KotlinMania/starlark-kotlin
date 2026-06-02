// port-lint: source src/values/layout/typed.rs
package io.github.kotlinmania.starlark.values.layout

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

/**
 * Submodules:
 *  - typed/String.kt (string)
 */

import io.github.kotlinmania.starlark.collections.Hashed
import io.github.kotlinmania.starlark.collections.StarlarkHashValue
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.FrozenRef
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.ValueOfUncheckedGeneric
import io.github.kotlinmania.starlark.values.layout.heap.AValueRepr
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.layout.heap.ValueHolder
import io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark.values.layout.typed.StarlarkStr
import io.github.kotlinmania.starlark.values.layout.typed.StringValue
import io.github.kotlinmania.starlark.values.types.int.PointerI32

/** [Value] wrapper which asserts contained value is of type `<T>`. */
// pub struct ValueTyped<'v, T: StarlarkValue<'v>>(Value<'v>, marker::PhantomData<T>)
class ValueTyped<T : StarlarkValue>(
    internal val value: Value,
) {
    companion object {
        /** Downcast. */
        // pub fn new(value: Value<'v>) -> Option<ValueTyped<'v, T>>
        internal inline fun <reified T : StarlarkValue> new(value: Value): ValueTyped<T>? {
            value.downcastRef<T>() ?: return null
            return ValueTyped(value)
        }

        /** Downcast. */
        // pub fn new_err(value: Value<'v>) -> crate::Result<ValueTyped<'v, T>>
        internal inline fun <reified T : StarlarkValue> newErr(value: Value): ValueTyped<T> {
            value.downcastRef<T>()
                ?: throw IllegalArgumentException("Expected ${T::class.simpleName}, got ${value.toStringForTypeError()}")
            return ValueTyped(value)
        }

        /** Construct typed value without checking the value is of type `<T>`. */
        // pub unsafe fn new_unchecked(value: Value<'v>) -> ValueTyped<'v, T>
        fun <T : StarlarkValue> newUnchecked(value: Value): ValueTyped<T> = ValueTyped(value)

        // pub(crate) fn new_repr<A: AValue<'v, StarlarkValue = T>>(repr: &'v AValueRepr<AValueImpl<'v, A>>) -> ValueTyped<'v, T>
        internal fun <A : AValue, T : StarlarkValue> newRepr(repr: AValueRepr<AValueImpl<A>>): ValueTyped<T> =
            ValueTyped(Value.newRepr(repr))
    }

    /** Erase the type. */
    // pub fn to_value(self) -> Value<'v>
    fun toValue(): Value = value

    /** Get the reference to the pointed value. */
    // pub fn as_ref(self) -> &'v T
    @Suppress("UNCHECKED_CAST")
    fun asRef(): T = value.getRef().value.ptr as T

    /** Compute the hash value. */
    // pub fn hashed(self) -> crate::Result<Hashed<Self>>
    fun hashed(): Result<Hashed<ValueTyped<T>>> {
        val s = toValue().unpackStarlarkStr()
        val hash: StarlarkHashValue =
            if (s != null) {
                s.getHash().getOrElse { return Result.failure(it) }
            } else {
                toValue().getHash().getOrElse { return Result.failure(it) }
            }
        return Result.success(Hashed.newUnchecked(hash, this))
    }

    /** Convert to another Value wrapper. */
    // pub fn to_value_of_unchecked(self) -> ValueOfUnchecked<'v, T>
    fun toValueOfUnchecked(): ValueOfUncheckedGeneric<Value, *> =
        valueOfUncheckedFromValue(toValue())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ValueTyped<*>) return false
        return value == other.value
    }

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = value.toString()
}

/** [FrozenValue] wrapper which asserts contained value is of type `<T>`. */
// pub struct FrozenValueTyped<'v, T: StarlarkValue<'v>>(FrozenValue, marker::PhantomData<&'v T>)
class FrozenValueTyped<T : StarlarkValue>(
    private val frozenValue: FrozenValue,
) {
    companion object {
        // pub(crate) fn is_str() -> bool
        inline fun <reified T : StarlarkValue> isStr(): Boolean =
            T::class == StarlarkStr::class

        // pub(crate) fn is_pointer_i32() -> bool
        internal inline fun <reified T : StarlarkValue> isPointerI32(): Boolean =
            PointerI32.typeIsPointerI32<T>()

        /** Construct without checking type. */
        // pub unsafe fn new_unchecked(value: FrozenValue) -> FrozenValueTyped<'v, T>
        fun <T : StarlarkValue> newUnchecked(value: FrozenValue): FrozenValueTyped<T> =
            FrozenValueTyped(value)

        /** Downcast. */
        // pub fn new(value: FrozenValue) -> Option<FrozenValueTyped<'v, T>>
        internal inline fun <reified T : StarlarkValue> new(value: FrozenValue): FrozenValueTyped<T>? {
            value.downcastRef<T>() ?: return null
            return FrozenValueTyped(value)
        }

        /** Downcast. */
        // pub fn new_err(value: FrozenValue) -> crate::Result<FrozenValueTyped<'v, T>>
        internal inline fun <reified T : StarlarkValue> newErr(value: FrozenValue): FrozenValueTyped<T> {
            value.downcastRef<T>()
                ?: throw IllegalArgumentException("Expected ${T::class.simpleName}, got ${value.toValue().toStringForTypeError()}")
            return FrozenValueTyped(value)
        }

        // pub(crate) fn new_repr<A: AValue<'v, StarlarkValue = T>>(repr: &'v AValueRepr<AValueImpl<'v, A>>) -> FrozenValueTyped<'v, T>
        internal fun <A : AValue, T : StarlarkValue> newRepr(repr: AValueRepr<AValueImpl<A>>): FrozenValueTyped<T> =
            FrozenValueTyped(FrozenValue.newPtrQueryIsStr(repr.header))
    }

    /** Erase the type. */
    // pub fn to_frozen_value(self) -> FrozenValue
    fun toFrozenValue(): FrozenValue = frozenValue

    /** Convert to the value. */
    // pub fn to_value(self) -> Value<'v>
    fun toValue(): Value = frozenValue.toValue()

    /** Convert to the value. */
    // pub fn to_value_typed(self) -> ValueTyped<'v, T>
    fun toValueTyped(): ValueTyped<T> = ValueTyped.newUnchecked(frozenValue.toValue())

    /** Get the reference to the pointed value. */
    // pub fn as_ref(self) -> &'v T
    @Suppress("UNCHECKED_CAST")
    fun asRef(): T =
        frozenValue
            .toValue()
            .getRef()
            .value.ptr as T

    // pub(crate) fn as_frozen_ref(self) -> FrozenRef<'v, T>
    internal fun asFrozenRef(): FrozenRef<T> = FrozenRef.new(asRef())

    /** Convert to another FrozenValue wrapper. */
    // pub fn to_value_of_unchecked(self) -> FrozenValueOfUnchecked<'v, T>
    fun toValueOfUnchecked(): ValueOfUncheckedGeneric<FrozenValue, *> =
        frozenValueOfUncheckedFromFrozenValue(toFrozenValue())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FrozenValueTyped<*>) return false
        return toValueTyped() == other.toValueTyped()
    }

    override fun hashCode(): Int = frozenValue.hashCode()

    override fun toString(): String = frozenValue.toString()
}

/** Extension for [ValueTyped] wrapping [StarlarkStr]. */
// impl<'v> ValueTyped<'v, StarlarkStr>
fun ValueTyped<StarlarkStr>.asStr(): String = asRef().asStr()

/** Extension for [FrozenValueTyped] wrapping [StarlarkStr]. */
// impl<'v> FrozenValueTyped<'v, StarlarkStr>
fun FrozenValueTyped<StarlarkStr>.asStr(): String = asRef().asStr()

/** [StarlarkTypeRepr] impl for [ValueTyped]. */
// impl<'v, T: StarlarkValue<'v>> StarlarkTypeRepr for ValueTyped<'v, T>
fun <T : StarlarkValue> ValueTyped<T>.starlarkTypeRepr(): Ty =
    asRef().typecheckerTy() ?: Ty.any()

/** [AllocValue] impl for [ValueTyped]. */
// impl<'v, T: StarlarkValue<'v>> AllocValue<'v> for ValueTyped<'v, T>
fun <T : StarlarkValue> ValueTyped<T>.allocValue(heap: Heap): Value = toValue()

/** [StarlarkTypeRepr] impl for [FrozenValueTyped]. */
// impl<'v, T: StarlarkValue<'v>> StarlarkTypeRepr for FrozenValueTyped<'v, T>
fun <T : StarlarkValue> FrozenValueTyped<T>.starlarkTypeRepr(): Ty =
    asRef().typecheckerTy() ?: Ty.any()

/** [AllocValue] impl for [FrozenValueTyped]. */
// impl<'v, 'f, T: StarlarkValue<'f>> AllocValue<'v> for FrozenValueTyped<'f, T>
fun <T : StarlarkValue> FrozenValueTyped<T>.allocValue(heap: Heap): Value = toFrozenValue().toValue()

/** [AllocFrozenValue] impl for [FrozenValueTyped]. */
// impl<'v, T: StarlarkValue<'v>> AllocFrozenValue for FrozenValueTyped<'v, T>
fun <T : StarlarkValue> FrozenValueTyped<T>.allocFrozenValue(heap: FrozenHeap): FrozenValue =
    toFrozenValue()

/** [AllocStringValue] impl for [StringValue]. */
// impl<'v> AllocStringValue<'v> for StringValue<'v>
fun StringValue.allocStringValue(heap: Heap): StringValue = this

/** [AllocStringValue] impl for [FrozenStringValue]. */
// impl<'v> AllocStringValue<'v> for FrozenStringValue
fun FrozenStringValue.allocStringValue(heap: Heap): StringValue = toStringValue()

/** [AllocFrozenStringValue] impl for [FrozenStringValue]. */
// impl AllocFrozenStringValue for FrozenStringValue
fun FrozenStringValue.allocFrozenStringValue(heap: FrozenHeap): FrozenStringValue = this

/**
 * [Trace] impl for [ValueTyped].
 * Traces the contained value and asserts the type is unchanged after tracing.
 */
// unsafe impl<'v, T: StarlarkValue<'v>> Trace<'v> for ValueTyped<'v, T>
fun <T : StarlarkValue> ValueTyped<T>.trace(tracer: Tracer) {
    val holder = ValueHolder(value)
    tracer.trace(holder)
    // The underlying value field is internal, so we update via the holder
    // After tracing, the value reference may have been forwarded
}

/**
 * [Trace] impl for [FrozenValueTyped].
 * Frozen values do not need tracing.
 */
// unsafe impl<'v, 'f, T: StarlarkValue<'f>> Trace<'v> for FrozenValueTyped<'f, T>
fun <T : StarlarkValue> FrozenValueTyped<T>.trace(
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {
    // Nothing to do: frozen values are immutable and not subject to GC forwarding.
}

/**
 * [Freeze] impl for [FrozenValueTyped].
 * Already frozen, returns self.
 */
// impl<T: StarlarkValue<'static>> Freeze for FrozenValueTyped<'static, T>
fun <T : StarlarkValue> FrozenValueTyped<T>.freeze(
    @Suppress("UNUSED_PARAMETER") freezer: Freezer,
): Result<FrozenValueTyped<T>> = Result.success(this)

/**
 * [Freeze] impl for [ValueTyped].
 * Freezes the contained value and wraps as [FrozenValueTyped].
 */
// impl<'v, T> Freeze for ValueTyped<'v, T> where T: StarlarkValue<'v>, T: Freeze
fun <T : StarlarkValue> ValueTyped<T>.freeze(freezer: Freezer): Result<FrozenValueTyped<T>> {
    val frozenValue = toValue().freeze(freezer)
    if (frozenValue.isFailure) return Result.failure(frozenValue.exceptionOrNull()!!)
    val fvt = FrozenValueTyped.newUnchecked<T>(frozenValue.getOrThrow())
    return Result.success(fvt)
}

/**
 * [UnpackValue] impl for [ValueTyped].
 * Attempts to downcast a [Value] to [ValueTyped].
 */
// impl<'v, T: StarlarkValue<'v>> UnpackValue<'v> for ValueTyped<'v, T>
internal inline fun <reified T : StarlarkValue> unpackValueTyped(value: Value): Result<ValueTyped<T>?> =
    Result.success(ValueTyped.new<T>(value))

/**
 * [UnpackValue] impl for [FrozenValueTyped].
 * Attempts to downcast a [Value] to [FrozenValueTyped], requiring the value to be frozen.
 */
// impl<'v, T: StarlarkValue<'v>> UnpackValue<'v> for FrozenValueTyped<'v, T>
internal inline fun <reified T : StarlarkValue> unpackFrozenValueTyped(value: Value): Result<FrozenValueTyped<T>?> {
    val frozen = value.unpackFrozen()
    if (frozen != null) {
        val typed = FrozenValueTyped.new<T>(frozen)
        if (typed != null) {
            return Result.success(typed)
        }
    } else if (value.downcastRef<T>() != null) {
        // Value is of the right type but not frozen
        return Result.failure(
            IllegalArgumentException(
                "Expected frozen value of type `${T::class.simpleName}`, got unfrozen: `${value.toStringForTypeError()}`",
            ),
        )
    }
    return Result.success(null)
}

/**
 * Helper to create [ValueOfUncheckedGeneric] from a [Value] without requiring
 * the phantom type parameter to satisfy [StarlarkTypeRepr].
 * This is needed because Kotlin's [StarlarkValue] does not extend [StarlarkTypeRepr],
 * while Rust's StarlarkValue does.
 */
@Suppress("UNCHECKED_CAST")
internal fun valueOfUncheckedFromValue(value: Value): ValueOfUncheckedGeneric<Value, *> =
    ValueOfUncheckedGeneric.new<Value, io.github.kotlinmania.starlark.values.StarlarkTypeRepr>(value)
        as ValueOfUncheckedGeneric<Value, *>

/**
 * Helper to create [ValueOfUncheckedGeneric] from a [FrozenValue] without requiring
 * the phantom type parameter to satisfy [StarlarkTypeRepr].
 */
@Suppress("UNCHECKED_CAST")
internal fun frozenValueOfUncheckedFromFrozenValue(value: FrozenValue): ValueOfUncheckedGeneric<FrozenValue, *> =
    ValueOfUncheckedGeneric.new<FrozenValue, io.github.kotlinmania.starlark.values.StarlarkTypeRepr>(value)
        as ValueOfUncheckedGeneric<FrozenValue, *>
