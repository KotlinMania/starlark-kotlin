// port-lint: source src/values/value_of_unchecked.rs
package io.github.kotlinmania.starlark.values

/*
 * Copyright 2018 The Starlark in Rust Authors.
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

import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.layout.Freezer
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.ValueLike
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.heap.Tracer

/**
 * Store value annotated with type, but do not check the type.
 *
 * In Rust this is a generic struct parameterized by `V: ValueLifetimeless` and `T: StarlarkTypeRepr`,
 * where `T` is tracked via `PhantomData<fn() -> T>` (type-level only, no runtime storage).
 *
 * In Kotlin, `T` is a phantom type parameter used only for type-level annotation.
 * The `V` parameter represents the underlying value type ([Value] or [FrozenValue]).
 */
internal class ValueOfUncheckedGeneric<V : ValueLike, T : StarlarkTypeRepr> private constructor(
    private val value: V,
) {
    /**
     * Cast to a different Rust type for the same Starlark type.
     */
    fun <U : StarlarkTypeRepr> cast(): ValueOfUncheckedGeneric<V, U> = new(this.value)

    /** Get the value. */
    fun get(): V = value

    /**
     * Unpack the value.
     *
     * In Rust, this uses `V: ValueLike<'v>` and `T: UnpackValue<'v>` bounds.
     * In Kotlin, due to type erasure, an explicit [UnpackValue] instance is required.
     */
    fun <R> unpack(unpacker: UnpackValue<R>): R = unpacker.unpackValueErr(value.toValue())

    /** Debug representation, formatted as `ValueOfUnchecked(value)`. */
    fun toDebugString(): String = "ValueOfUnchecked(${get()})"

    override fun toString(): String = value.toString()

    /**
     * The representation of the starlark type.
     *
     * In Rust, this delegates to `T::Canonical::starlark_type_repr()`.
     * In Kotlin, due to type erasure, the concrete [StarlarkTypeRepr] for `T`
     * must be provided externally.
     */
    fun starlarkTypeRepr(typeRepr: StarlarkTypeRepr): Ty = typeRepr.starlarkTypeRepr()

    /**
     * Allocate the wrapped value on the heap. Returns the underlying value.
     *
     * The heap parameter is unused because the value is already allocated;
     * this simply extracts the [Value] from the wrapper.
     */
    fun allocValue(
        @Suppress("UNUSED_PARAMETER") heap: Heap,
    ): Value = value.toValue()

    /** Trace the inner value for garbage collection. */
    fun trace(tracer: Tracer) {
        val v = value
        if (v is Trace) {
            v.trace(tracer)
        }
    }

    /** Freeze this value, producing a frozen equivalent. */
    fun freeze(freezer: Freezer): Result<ValueOfUncheckedGeneric<FrozenValue, T>> {
        val frozen: FrozenValue =
            when (val v = value) {
                is Value -> v.freeze(freezer).getOrThrow()
                is FrozenValue -> v
                else -> v.toValue().freeze(freezer).getOrThrow()
            }
        return Result.success(new(frozen))
    }

    /** Convert to a [ValueOfUnchecked] wrapping a [Value]. */
    fun toValue(): ValueOfUncheckedGeneric<Value, T> = new(value.toValue())

    companion object {
        /** Wrap a value with a phantom type annotation. */
        fun <V : ValueLike, T : StarlarkTypeRepr> new(value: V): ValueOfUncheckedGeneric<V, T> = ValueOfUncheckedGeneric(value)
    }
}

/**
 * Starlark value with type annotation.
 *
 * Can be used in function signatures to provide types to the type checker.
 * Note this type does not actually check the type of the value.
 * Providing incorrect type annotation will result
 * in incorrect error reporting by the type checker.
 */
internal typealias ValueOfUnchecked<T> = ValueOfUncheckedGeneric<Value, T>

/** Frozen starlark value with type annotation. */
internal typealias FrozenValueOfUnchecked<T> = ValueOfUncheckedGeneric<FrozenValue, T>

/**
 * Allocate frozen value. Returns the underlying frozen value.
 */
internal fun <T : StarlarkTypeRepr> FrozenValueOfUnchecked<T>.allocFrozenValue(
    @Suppress("UNUSED_PARAMETER") heap: FrozenHeap,
): FrozenValue = get()

/**
 * Construct after checking the type.
 *
 * In Rust, the `T: UnpackValue<'v>` bound ensures the type check can be
 * performed at compile time. In Kotlin, an explicit [UnpackValue] instance
 * is required to perform the runtime check.
 */
internal fun <T : StarlarkTypeRepr, R> ValueOfUncheckedGeneric.Companion.newChecked(
    value: Value,
    unpacker: UnpackValue<R>,
): ValueOfUnchecked<T> {
    unpacker.unpackValueErr(value)
    return ValueOfUncheckedGeneric.new(value)
}

/** Construct after checking the type (convenience overload that skips type checking). */
internal fun <T : StarlarkTypeRepr> ValueOfUncheckedGeneric.Companion.newChecked(
    value: Value,
): ValueOfUnchecked<T> = ValueOfUncheckedGeneric.new(value)

/**
 * [UnpackValue] implementation for [ValueOfUnchecked].
 *
 * This always succeeds since [ValueOfUnchecked] wraps any value without checking the type.
 * The error type is effectively [Nothing] (Rust `Infallible`).
 *
 * In Rust:
 * ```
 * impl<'v, T: StarlarkTypeRepr> UnpackValue<'v> for ValueOfUnchecked<'v, T> {
 *     type Error = Infallible;
 *     fn unpack_value_impl(value: Value<'v>) -> Result<Option<Self>, Self::Error> {
 *         Ok(Some(Self::new(value)))
 *     }
 * }
 * ```
 */
internal class ValueOfUncheckedUnpackValue<T : StarlarkTypeRepr>(
    private val typeRepr: StarlarkTypeRepr,
) : UnpackValue<ValueOfUnchecked<T>> {
    override fun starlarkTypeRepr(): Ty = typeRepr.starlarkTypeRepr()

    override fun unpackValueImpl(value: Value): Result<ValueOfUnchecked<T>?> = Result.success(ValueOfUncheckedGeneric.new(value))
}

/**
 * Unpack a [Value] into a [ValueOfUnchecked].
 *
 * This always succeeds since [ValueOfUnchecked] wraps any value without checking the type.
 */
internal fun <T : StarlarkTypeRepr> unpackValueOfUnchecked(value: Value): ValueOfUnchecked<T> = ValueOfUncheckedGeneric.new(value)
