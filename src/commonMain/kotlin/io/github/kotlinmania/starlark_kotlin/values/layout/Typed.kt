// port-lint: source src/values/layout/typed.rs
package io.github.kotlinmania.starlark_kotlin.values.layout

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

/// Submodules:
///  - typed/String.kt (string)

import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.AllocFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.AllocValue
import io.github.kotlinmania.starlark_kotlin.values.Freeze
import io.github.kotlinmania.starlark_kotlin.values.Freezer
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenRef
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.FrozenValueOfUnchecked
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.ValueOfUnchecked
import io.github.kotlinmania.starlark_kotlin.values.layout.avalue.AValue
import io.github.kotlinmania.starlark_kotlin.values.layout.avalue.AValueImpl
import io.github.kotlinmania.starlark_kotlin.values.starlark_type_id.StarlarkTypeId
import io.github.kotlinmania.starlark_kotlin.values.types.string.intern.FrozenStringValue
import io.github.kotlinmania.starlark_kotlin.values.types.string.StringValue
import io.github.kotlinmania.starlark_kotlin.values.types.string.StarlarkStr
import io.github.kotlinmania.starlark_kotlin.values.owned.FrozenValueTyped
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.AValueRepr
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.types.string.unpackStarlarkStr
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.toStringValue
import io.github.kotlinmania.starlark_kotlin.any.downcastRef

/// [Value] wrapper which asserts contained value is of type `<T>`.
// pub struct ValueTyped<'v, T: StarlarkValue<'v>>(Value<'v>, marker::PhantomData<T>)
class ValueTyped<T : StarlarkValue>(
    private val value: Value,
) {
    companion object {
        /// Downcast.
        // pub fn new(value: Value<'v>) -> Option<ValueTyped<'v, T>>
        inline fun <reified T : StarlarkValue> new(value: Value): ValueTyped<T>? {
            value.downcastRef<T>() ?: return null
            return ValueTyped(value)
        }

        /// Downcast.
        // pub fn new_err(value: Value<'v>) -> crate::Result<ValueTyped<'v, T>>
        inline fun <reified T : StarlarkValue> newErr(value: Value): ValueTyped<T> {
            value.downcastRefErr<T>()
            return ValueTyped(value)
        }

        /// Construct typed value without checking the value is of type `<T>`.
        // pub unsafe fn new_unchecked(value: Value<'v>) -> ValueTyped<'v, T>
        fun <T : StarlarkValue> newUnchecked(value: Value): ValueTyped<T> = ValueTyped(value)

        // pub(crate) fn new_repr<A: AValue<'v, StarlarkValue = T>>(repr: &'v AValueRepr<AValueImpl<'v, A>>) -> ValueTyped<'v, T>
        internal fun <T : StarlarkValue> newRepr(repr: AValueRepr<AValueImpl<*, *>>): ValueTyped<T> =
            ValueTyped(Value.newRepr(repr))
    }

    /// Erase the type.
    // pub fn to_value(self) -> Value<'v>
    fun toValue(): Value = value

    /// Get the reference to the pointed value.
    // pub fn as_ref(self) -> &'v T
    @Suppress("UNCHECKED_CAST")
    fun asRef(): T = value.downcastRefUnchecked() as T

    /// Compute the hash value.
    // pub fn hashed(self) -> crate::Result<Hashed<Self>>
    fun hashed(): Int {
        val s = toValue().unpackStarlarkStr()
        return s?.getHash() ?: toValue().getHash()
    }

    /// Convert to another Value wrapper.
    // pub fn to_value_of_unchecked(self) -> ValueOfUnchecked<'v, T>
    fun toValueOfUnchecked(): ValueOfUnchecked<T> = ValueOfUnchecked.new(toValue())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ValueTyped<*>) return false
        return value == other.value
    }

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = value.toString()
}

/// [FrozenValue] wrapper which asserts contained value is of type `<T>`.
// pub struct FrozenValueTyped<'v, T: StarlarkValue<'v>>(FrozenValue, marker::PhantomData<&'v T>)
class FrozenValueTyped<T : StarlarkValue>(
    private val frozenValue: FrozenValue,
) {
    companion object {
        // pub(crate) fn is_str() -> bool
        inline fun <reified T : StarlarkValue> isStr(): Boolean =
            T::class == StarlarkStr::class

        /// Construct without checking type.
        // pub unsafe fn new_unchecked(value: FrozenValue) -> FrozenValueTyped<'v, T>
        fun <T : StarlarkValue> newUnchecked(value: FrozenValue): FrozenValueTyped<T> =
            FrozenValueTyped(value)

        /// Downcast.
        // pub fn new(value: FrozenValue) -> Option<FrozenValueTyped<'v, T>>
        inline fun <reified T : StarlarkValue> new(value: FrozenValue): FrozenValueTyped<T>? {
            value.downcastRef<T>() ?: return null
            return FrozenValueTyped(value)
        }

        /// Downcast.
        // pub fn new_err(value: FrozenValue) -> crate::Result<FrozenValueTyped<'v, T>>
        inline fun <reified T : StarlarkValue> newErr(value: FrozenValue): FrozenValueTyped<T> {
            value.downcastRefErr<T>()
            return FrozenValueTyped(value)
        }

        // pub(crate) fn new_repr<A: AValue<'v, StarlarkValue = T>>(repr: &'v AValueRepr<AValueImpl<'v, A>>) -> FrozenValueTyped<'v, T>
        internal fun <T : StarlarkValue> newRepr(repr: AValueRepr<AValueImpl<*, *>>): FrozenValueTyped<T> =
            FrozenValueTyped(FrozenValue.newRepr(repr))
    }

    /// Erase the type.
    // pub fn to_frozen_value(self) -> FrozenValue
    fun toFrozenValue(): FrozenValue = frozenValue

    /// Convert to the value.
    // pub fn to_value(self) -> Value<'v>
    fun toValue(): Value = frozenValue.toValue()

    /// Convert to the value.
    // pub fn to_value_typed(self) -> ValueTyped<'v, T>
    fun toValueTyped(): ValueTyped<T> = ValueTyped.newUnchecked(frozenValue.toValue())

    /// Get the reference to the pointed value.
    // pub fn as_ref(self) -> &'v T
    @Suppress("UNCHECKED_CAST")
    fun asRef(): T = frozenValue.toValue().downcastRefUnchecked() as T

    // pub(crate) fn as_frozen_ref(self) -> FrozenRef<'v, T>
    internal fun asFrozenRef(): FrozenRef<T> = FrozenRef.new(asRef())

    /// Convert to another FrozenValue wrapper.
    // pub fn to_value_of_unchecked(self) -> FrozenValueOfUnchecked<'v, T>
    fun toValueOfUnchecked(): FrozenValueOfUnchecked<T> =
        FrozenValueOfUnchecked.new(toFrozenValue())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FrozenValueTyped<*>) return false
        return toValueTyped() == other.toValueTyped()
    }

    override fun hashCode(): Int = frozenValue.hashCode()

    override fun toString(): String = frozenValue.toString()
}

/// Extension for [ValueTyped] wrapping [StarlarkStr].
// impl<'v> ValueTyped<'v, StarlarkStr>
fun ValueTyped<StarlarkStr>.asStr(): String = asRef().asStr()

/// Extension for [FrozenValueTyped] wrapping [StarlarkStr].
// impl<'v> FrozenValueTyped<'v, StarlarkStr>
fun FrozenValueTyped<StarlarkStr>.asStr(): String = asRef().asStr()

/// [StarlarkTypeRepr] impl for [ValueTyped].
// impl<'v, T: StarlarkValue<'v>> StarlarkTypeRepr for ValueTyped<'v, T>
fun <T : StarlarkValue> ValueTyped<T>.starlarkTypeRepr(): Ty = TODO("T::starlarkTypeRepr()")

/// [AllocValue] impl for [ValueTyped].
// impl<'v, T: StarlarkValue<'v>> AllocValue<'v> for ValueTyped<'v, T>
fun <T : StarlarkValue> ValueTyped<T>.allocValue(heap: Heap): Value = toValue()

/// [StarlarkTypeRepr] impl for [FrozenValueTyped].
// impl<'v, T: StarlarkValue<'v>> StarlarkTypeRepr for FrozenValueTyped<'v, T>
fun <T : StarlarkValue> FrozenValueTyped<T>.starlarkTypeRepr(): Ty = TODO("T::starlarkTypeRepr()")

/// [AllocValue] impl for [FrozenValueTyped].
// impl<'v, 'f, T: StarlarkValue<'f>> AllocValue<'v> for FrozenValueTyped<'f, T>
fun <T : StarlarkValue> FrozenValueTyped<T>.allocValue(heap: Heap): Value = toFrozenValue().toValue()

/// [AllocFrozenValue] impl for [FrozenValueTyped].
// impl<'v, T: StarlarkValue<'v>> AllocFrozenValue for FrozenValueTyped<'v, T>
fun <T : StarlarkValue> FrozenValueTyped<T>.allocFrozenValue(heap: FrozenHeap): FrozenValue =
    toFrozenValue()

/// [AllocStringValue] impl for [StringValue].
// impl<'v> AllocStringValue<'v> for StringValue<'v>
fun StringValue.allocStringValue(heap: Heap): StringValue = this

/// [AllocStringValue] impl for [FrozenStringValue].
// impl<'v> AllocStringValue<'v> for FrozenStringValue
fun FrozenStringValue.allocStringValue(heap: Heap): StringValue = toStringValue()

/// [AllocFrozenStringValue] impl for [FrozenStringValue].
// impl AllocFrozenStringValue for FrozenStringValue
fun FrozenStringValue.allocFrozenStringValue(heap: FrozenHeap): FrozenStringValue = this
