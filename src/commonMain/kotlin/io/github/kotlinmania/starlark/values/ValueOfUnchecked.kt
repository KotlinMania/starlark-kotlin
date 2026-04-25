// port-lint: source src/values/value_of_unchecked.rs
package io.github.kotlinmania.starlark_kotlin.values

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

import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.layout.Freezer
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.layout.ValueLike
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Tracer

/**
 * Store value annotated with type, but do not check the type.
 *
 * In Rust this is a generic struct parameterized by `V: ValueLifetimeless` and `T: StarlarkTypeRepr`,
 * where `T` is tracked via `PhantomData<fn() -> T>` (type-level only, no runtime storage).
 *
 * In Kotlin, `T` is a phantom type parameter used only for type-level annotation.
 * The `V` parameter represents the underlying value type ([Value] or [FrozenValue]).
 */
open class ValueOfUncheckedGeneric<V, T : StarlarkTypeRepr> protected constructor(
    private val value: V,
) {

    /**
     * Cast to a different Rust type for the same Starlark type.
     */
    open fun <U : StarlarkTypeRepr> cast(): ValueOfUncheckedGeneric<V, U> {
        return new(this.value)
    }

    /** Get the value. */
    fun get(): V {
        return value
    }

    /**
     * Unpack the value.
     *
     * In Rust, this uses `V: ValueLike<'v>` and `T: UnpackValue<'v>` bounds.
     * In Kotlin, due to type erasure, an explicit [UnpackValue] instance is required.
     */
    fun <R> unpack(unpacker: UnpackValue<R>): R {
        val v = value
        val asValue: Value = when (v) {
            is Value -> v
            is ValueLike -> v.toValue()
            else -> throw IllegalStateException("Cannot convert to Value")
        }
        return unpacker.unpackValueErr(asValue)
    }

    /** Debug representation, formatted as `ValueOfUnchecked(value)`. */
    fun toDebugString(): String {
        return "ValueOfUnchecked(${get()})"
    }

    override fun toString(): String {
        return value.toString()
    }

    /**
     * The representation of the starlark type.
     *
     * In Rust, this delegates to `T::Canonical::starlark_type_repr()`.
     * In Kotlin, due to type erasure, the concrete [StarlarkTypeRepr] for `T`
     * must be provided externally.
     */
    fun starlarkTypeRepr(typeRepr: StarlarkTypeRepr): Ty {
        return typeRepr.starlarkTypeRepr()
    }

    /**
     * Allocate the wrapped value on the heap. Returns the underlying value.
     *
     * The heap parameter is unused because the value is already allocated;
     * this simply extracts the [Value] from the wrapper.
     */
    fun allocValue(heap: Heap): Value {
        return when (val v = value) {
            is Value -> v
            is ValueLike -> v.toValue()
            else -> throw IllegalStateException("ValueOfUncheckedGeneric: cannot alloc non-Value type")
        }
    }

    /**
     * Allocate frozen value. Returns the underlying frozen value.
     *
     * The heap parameter is unused because the value is already frozen;
     * this simply extracts the [FrozenValue] from the wrapper.
     */
    fun allocFrozenValue(heap: FrozenHeap): FrozenValue {
        return when (val v = value) {
            is FrozenValue -> v
            else -> throw IllegalStateException("ValueOfUncheckedGeneric: cannot allocFrozenValue non-FrozenValue type")
        }
    }

    /** Trace the inner value for garbage collection. */
    fun trace(tracer: Tracer) {
        val v = value
        if (v is Trace) {
            v.trace(tracer)
        }
    }

    /** Freeze this value, producing a frozen equivalent. */
    fun freeze(freezer: Freezer): Result<ValueOfUncheckedGeneric<FrozenValue, T>> {
        val v = value
        val frozen: FrozenValue = when (v) {
            is Value -> v.freeze(freezer).getOrThrow()
            is FrozenValue -> v
            else -> throw IllegalStateException("Cannot freeze non-Value type")
        }
        return Result.success(new(frozen))
    }

    /** Convert to a [ValueOfUnchecked] wrapping a [Value]. */
    open fun toValue(): ValueOfUncheckedGeneric<Value, T> {
        val v = value
        val asValue: Value = when (v) {
            is Value -> v
            is ValueLike -> v.toValue()
            else -> throw IllegalStateException("Cannot convert to Value")
        }
        return new(asValue)
    }

    companion object {
        /** Wrap a value with a phantom type annotation. */
        fun <V, T : StarlarkTypeRepr> new(value: V): ValueOfUncheckedGeneric<V, T> {
            return ValueOfUncheckedGeneric(value)
        }
    }
}

/**
 * Starlark value with type annotation.
 *
 * Can be used in function signatures to provide types to the type checker.
 *
 * Note this type does not actually check the type of the value.
 * Providing incorrect type annotation will result
 * in incorrect error reporting by the type checker.
 */
class ValueOfUnchecked<T : StarlarkTypeRepr> private constructor(value: Value) : ValueOfUncheckedGeneric<Value, T>(value) {
    override fun <U : StarlarkTypeRepr> cast(): ValueOfUnchecked<U> {
        return new(get())
    }

    override fun toValue(): ValueOfUnchecked<T> {
        return this
    }

    companion object {
        /** New. */
        fun <T : StarlarkTypeRepr> new(value: Value): ValueOfUnchecked<T> {
            return ValueOfUnchecked(value)
        }

        /** Construct after checking the type. */
        fun <T : StarlarkTypeRepr, R> newChecked(
            value: Value,
            unpacker: UnpackValue<R>,
        ): ValueOfUnchecked<T> {
            unpacker.unpackValueErr(value)
            return new(value)
        }

        /** Construct after checking the type (convenience overload that skips type checking). */
        fun <T : StarlarkTypeRepr> newChecked(value: Value): ValueOfUnchecked<T> {
            return new(value)
        }
    }
}

/** Frozen starlark value with type annotation. */
class FrozenValueOfUnchecked<T : StarlarkTypeRepr> private constructor(value: FrozenValue) :
    ValueOfUncheckedGeneric<FrozenValue, T>(value) {
    override fun <U : StarlarkTypeRepr> cast(): FrozenValueOfUnchecked<U> {
        return new(get())
    }

    override fun toValue(): ValueOfUnchecked<T> {
        return ValueOfUnchecked.new(super.toValue().get())
    }

    companion object {
        fun <T : StarlarkTypeRepr> new(value: FrozenValue): FrozenValueOfUnchecked<T> {
            return FrozenValueOfUnchecked(value)
        }
    }
}

/**
 * Unpack a [Value] into a [ValueOfUnchecked].
 *
 * This always succeeds since [ValueOfUnchecked] wraps any value without checking the type.
 */
fun <T : StarlarkTypeRepr> unpackValueOfUnchecked(value: Value): ValueOfUnchecked<T> {
    return ValueOfUnchecked.new(value)
}
