// port-lint: source src/values/layout/typed.rs
package io.github.kotlinmania.starlark.values.layout

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

/**
 * Submodules:
 *  - typed/String.kt (string)
 */

import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.AllocFrozenValue
import io.github.kotlinmania.starlark.values.AllocValue
import io.github.kotlinmania.starlark.values.Freeze
import io.github.kotlinmania.starlark.values.layout.Freezer
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.FrozenRef
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.Trace
import io.github.kotlinmania.starlark.values.ValueOfUncheckedGeneric
import io.github.kotlinmania.starlark.values.layout.AValue
import io.github.kotlinmania.starlark.values.layout.AValueImpl
import io.github.kotlinmania.starlark.values.starlarktypeid.StarlarkTypeId
import io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark.values.layout.typed.StringValue
import io.github.kotlinmania.starlark.values.types.string.StarlarkStr
import io.github.kotlinmania.starlark.values.layout.heap.AValueRepr
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.layout.heap.ValueHolder
import io.github.kotlinmania.starlark.values.types.int.PointerI32
import starlarkmap.Hashed
import starlarkmap.StarlarkHashValue

/** [Value] wrapper which asserts contained value is of type `<T>`. */
class ValueTyped<T : StarlarkValue>(
    internal val value: Value,
) {
    companion object {
        /** Downcast. */
        internal inline fun <reified T : StarlarkValue> new(value: Value): ValueTyped<T>? {
            value.downcastRef<T>() ?: return null
            return ValueTyped(value)
        }

        /** Downcast. */
        internal inline fun <reified T : StarlarkValue> newErr(value: Value): ValueTyped<T> {
            value.downcastRef<T>()
                ?: throw IllegalArgumentException("Expected ${T::class.simpleName}, got ${value.toStringForTypeError()}")
            return ValueTyped(value)
        }

        /** Construct typed value without checking the value is of type `<T>`. */
        fun <T : StarlarkValue> newUnchecked(value: Value): ValueTyped<T> = ValueTyped(value)

        internal fun <A : AValue, T : StarlarkValue> newRepr(repr: AValueRepr<AValueImpl<A>>): ValueTyped<T> =
            ValueTyped(Value.newRepr(repr))
    }

    /** Erase the type. */
    fun toValue(): Value = value

    /** Get the reference to the pointed value. */
    fun asRef(): T = value.getRef().value.ptr as T

    /** Compute the hash value. */
    fun hashed(): Result<Hashed<ValueTyped<T>>> {
        val s = toValue().unpackStarlarkStr()
        val hash: StarlarkHashValue = if (s != null) {
            s.getHash().getOrElse { return Result.failure(it) }
        } else {
            toValue().getHash().getOrElse { return Result.failure(it) }
        }
        return Result.success(Hashed.newUnchecked(hash, this))
    }

    /** Convert to another Value wrapper. */
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
class FrozenValueTyped<T : StarlarkValue>(
    private val frozenValue: FrozenValue,
) {
    companion object {
        inline fun <reified T : StarlarkValue> isStr(): Boolean =
            T::class == StarlarkStr::class

        internal inline fun <reified T : StarlarkValue> isPointerI32(): Boolean =
            PointerI32.typeIsPointerI32<T>()

        /** Construct without checking type. */
        fun <T : StarlarkValue> newUnchecked(value: FrozenValue): FrozenValueTyped<T> =
            FrozenValueTyped(value)

        /** Downcast. */
        internal inline fun <reified T : StarlarkValue> new(value: FrozenValue): FrozenValueTyped<T>? {
            value.downcastRef<T>() ?: return null
            return FrozenValueTyped(value)
        }

        /** Downcast. */
        internal inline fun <reified T : StarlarkValue> newErr(value: FrozenValue): FrozenValueTyped<T> {
            value.downcastRef<T>()
                ?: throw IllegalArgumentException("Expected ${T::class.simpleName}, got ${value.toValue().toStringForTypeError()}")
            return FrozenValueTyped(value)
        }

        internal fun <A : AValue, T : StarlarkValue> newRepr(repr: AValueRepr<AValueImpl<A>>): FrozenValueTyped<T> =
            FrozenValueTyped(FrozenValue.newPtrQueryIsStr(repr.header))
    }

    /** Erase the type. */
    fun toFrozenValue(): FrozenValue = frozenValue

    /** Convert to the value. */
    fun toValue(): Value = frozenValue.toValue()

    /** Convert to the value. */
    fun toValueTyped(): ValueTyped<T> = ValueTyped.newUnchecked(frozenValue.toValue())

    /** Get the reference to the pointed value. */
    fun asRef(): T = frozenValue.toValue().getRef().value.ptr as T

    internal fun asFrozenRef(): FrozenRef<T> = FrozenRef.new(asRef())

    /** Convert to another FrozenValue wrapper. */
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
fun ValueTyped<StarlarkStr>.asStr(): String = asRef().asStr()

/** Extension for [FrozenValueTyped] wrapping [StarlarkStr]. */
fun FrozenValueTyped<StarlarkStr>.asStr(): String = asRef().asStr()

/** [StarlarkTypeRepr] implementation for [ValueTyped]. */
fun <T : StarlarkValue> ValueTyped<T>.starlarkTypeRepr(): Ty =
    asRef().typecheckerTy() ?: Ty.any()

/** [AllocValue] implementation for [ValueTyped]. */
fun <T : StarlarkValue> ValueTyped<T>.allocValue(heap: Heap): Value = toValue()

/** [StarlarkTypeRepr] implementation for [FrozenValueTyped]. */
fun <T : StarlarkValue> FrozenValueTyped<T>.starlarkTypeRepr(): Ty =
    asRef().typecheckerTy() ?: Ty.any()

/** [AllocValue] implementation for [FrozenValueTyped]. */
fun <T : StarlarkValue> FrozenValueTyped<T>.allocValue(heap: Heap): Value = toFrozenValue().toValue()

/** [AllocFrozenValue] implementation for [FrozenValueTyped]. */
fun <T : StarlarkValue> FrozenValueTyped<T>.allocFrozenValue(heap: FrozenHeap): FrozenValue =
    toFrozenValue()

/** [AllocStringValue] implementation for [StringValue]. */
fun StringValue.allocStringValue(heap: Heap): StringValue = this

/** [AllocStringValue] implementation for [FrozenStringValue]. */
fun FrozenStringValue.allocStringValue(heap: Heap): StringValue = toStringValue()

/** [AllocFrozenStringValue] implementation for [FrozenStringValue]. */
fun FrozenStringValue.allocFrozenStringValue(heap: FrozenHeap): FrozenStringValue = this

/**
 * [Trace] implementation for [ValueTyped].
 * Traces the contained value and asserts the type is unchanged after tracing.
 */
fun <T : StarlarkValue> ValueTyped<T>.trace(tracer: Tracer) {
    val holder = ValueHolder(value)
    tracer.trace(holder)
    // The underlying value field is internal, so we update via the holder
    // After tracing, the value reference may have been forwarded
}

/**
 * [Trace] implementation for [FrozenValueTyped].
 * Frozen values do not need tracing.
 */
fun <T : StarlarkValue> FrozenValueTyped<T>.trace(tracer: Tracer) {
    // Nothing to do: frozen values are immutable and not subject to GC forwarding.
}

/**
 * [Freeze] implementation for [FrozenValueTyped].
 * Already frozen, returns self.
 */
fun <T : StarlarkValue> FrozenValueTyped<T>.freeze(
    freezer: Freezer,
): Result<FrozenValueTyped<T>> = Result.success(this)

/**
 * [Freeze] implementation for [ValueTyped].
 * Freezes the contained value and wraps as [FrozenValueTyped].
 */
fun <T : StarlarkValue> ValueTyped<T>.freeze(freezer: Freezer): Result<FrozenValueTyped<T>> {
    val frozenValue = toValue().freeze(freezer)
    if (frozenValue.isFailure) return Result.failure(frozenValue.exceptionOrNull()!!)
    val fvt = FrozenValueTyped.newUnchecked<T>(frozenValue.getOrThrow())
    return Result.success(fvt)
}

/**
 * [UnpackValue] implementation for [ValueTyped].
 * Attempts to downcast a [Value] to [ValueTyped].
 */
internal inline fun <reified T : StarlarkValue> unpackValueTyped(value: Value): Result<ValueTyped<T>?> =
    Result.success(ValueTyped.new<T>(value))

/**
 * [UnpackValue] implementation for [FrozenValueTyped].
 * Attempts to downcast a [Value] to [FrozenValueTyped], requiring the value to be frozen.
 */
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
                "Expected frozen value of type `${T::class.simpleName}`, got unfrozen: `${value.toStringForTypeError()}`"
            )
        )
    }
    return Result.success(null)
}

/**
 * Helper to create [ValueOfUncheckedGeneric] from a [Value] without requiring
 * the phantom type parameter to satisfy [StarlarkTypeRepr].
 */
internal fun valueOfUncheckedFromValue(value: Value): ValueOfUncheckedGeneric<Value, *> {
    return ValueOfUncheckedGeneric.new<Value, io.github.kotlinmania.starlark.values.StarlarkTypeRepr>(value)
        as ValueOfUncheckedGeneric<Value, *>
}

/**
 * Helper to create [ValueOfUncheckedGeneric] from a [FrozenValue] without requiring
 * the phantom type parameter to satisfy [StarlarkTypeRepr].
 */
internal fun frozenValueOfUncheckedFromFrozenValue(value: FrozenValue): ValueOfUncheckedGeneric<FrozenValue, *> {
    return ValueOfUncheckedGeneric.new<FrozenValue, io.github.kotlinmania.starlark.values.StarlarkTypeRepr>(value)
        as ValueOfUncheckedGeneric<FrozenValue, *>
}
