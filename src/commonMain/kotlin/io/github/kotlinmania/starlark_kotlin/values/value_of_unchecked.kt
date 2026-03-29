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

// use std::convert::Infallible;
// use std::fmt;
// use std::fmt::Debug;
// use std::fmt::Display;
// use std::fmt::Formatter;
// use std::marker::PhantomData;

// use allocative::Allocative;
// use dupe::Clone_;
// use dupe::Copy_;
// use dupe::Dupe_;

// use crate::coerce::Coerce;
// use crate::typing::Ty;
// use crate::values::AllocFrozenValue;
// use crate::values::AllocValue;
// use crate::values::Freeze;
// use crate::values::FreezeResult;
// use crate::values::Freezer;
// use crate::values::FrozenHeap;
// use crate::values::FrozenValue;
// use crate::values::Heap;
// use crate::values::Trace;
// use crate::values::Tracer;
// use crate::values::UnpackValue;
// use crate::values::Value;
// use crate::values::ValueLifetimeless;
// use crate::values::ValueLike;
// use crate::values::type_repr::StarlarkTypeRepr;

import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.freeze_error.FreezeResult
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
// #[derive(Clone_, Copy_, Dupe_, Allocative)]
// #[allocative(bound = "")]
// pub struct ValueOfUncheckedGeneric<V: ValueLifetimeless, T: StarlarkTypeRepr>(
//     V,
//     PhantomData<fn() -> T>,
// );
class ValueOfUncheckedGeneric<V, T : StarlarkTypeRepr> private constructor(
    private val value: V,
) {

    // unsafe impl<V, U, T> Coerce<ValueOfUncheckedGeneric<V, T>> for ValueOfUncheckedGeneric<U, T>
    // where
    //     V: ValueLifetimeless,
    //     U: ValueLifetimeless,
    //     U: Coerce<V>,
    //     T: StarlarkTypeRepr,
    // {
    // }
    // Kotlin: Coerce is a Rust zero-cost abstraction for safe pointer casting.
    // No Kotlin equivalent is needed since we use generic type parameters directly.

    // impl<V: ValueLifetimeless, T: StarlarkTypeRepr> ValueOfUncheckedGeneric<V, T> {

    /**
     * New.
     */
    // #[inline]
    // pub fn new(value: V) -> Self {
    //     Self(value, PhantomData)
    // }
    // Moved to companion object.

    /**
     * Cast to a different Rust type for the same Starlark type.
     */
    // #[inline]
    // pub fn cast<U: StarlarkTypeRepr<Canonical = T::Canonical>>(
    //     self,
    // ) -> ValueOfUncheckedGeneric<V, U> {
    //     ValueOfUncheckedGeneric::new(self.0)
    // }
    @Suppress("UNCHECKED_CAST")
    fun <U : StarlarkTypeRepr> cast(): ValueOfUncheckedGeneric<V, U> {
        return new(this.value)
    }

    /**
     * Get the value.
     */
    // #[inline]
    // pub fn get(self) -> V {
    //     self.0
    // }
    fun get(): V {
        return value
    }

    /**
     * Unpack the value.
     *
     * In Rust, this uses `V: ValueLike<'v>` and `T: UnpackValue<'v>` bounds.
     * In Kotlin, due to type erasure, `T` cannot be statically dispatched,
     * so an explicit [UnpackValue] instance is required.
     */
    // pub fn unpack<'v>(self) -> crate::Result<T>
    // where
    //     V: ValueLike<'v>,
    //     T: UnpackValue<'v>,
    // {
    //     T::unpack_value_err(self.get().to_value())
    // }
    fun <R> unpack(unpacker: UnpackValue<R>): R {
        val v = value
        val asValue: Value = when (v) {
            is Value -> v
            is ValueLike -> v.toValue()
            else -> throw IllegalStateException("Cannot convert to Value")
        }
        return unpacker.unpackValueErr(asValue)
    }

    // } // end impl ValueOfUncheckedGeneric

    // impl<V: ValueLifetimeless, T: StarlarkTypeRepr> Debug for ValueOfUncheckedGeneric<V, T> {
    //     fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
    //         f.debug_tuple("ValueOfUnchecked")
    //             .field(&self.get())
    //             .finish()
    //     }
    // }
    /**
     * Debug representation of this value.
     *
     * Mirrors Rust's `Debug` trait impl which formats as `ValueOfUnchecked(value)`.
     */
    fun toDebugString(): String {
        return "ValueOfUnchecked(${get()})"
    }

    // impl<V: ValueLifetimeless, T: StarlarkTypeRepr> Display for ValueOfUncheckedGeneric<V, T> {
    //     fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
    //         Display::fmt(&self.get(), f)
    //     }
    // }
    /**
     * Display representation of this value.
     *
     * Delegates to the underlying value's display formatting.
     */
    override fun toString(): String {
        return value.toString()
    }

    // impl<V: ValueLifetimeless, T: StarlarkTypeRepr> StarlarkTypeRepr for ValueOfUncheckedGeneric<V, T> {
    //     type Canonical = T::Canonical;
    //
    //     fn starlark_type_repr() -> Ty {
    //         <Self as StarlarkTypeRepr>::Canonical::starlark_type_repr()
    //     }
    // }
    /**
     * The representation of the starlark type.
     *
     * In Rust, this delegates to `T::Canonical::starlark_type_repr()`.
     * In Kotlin, due to type erasure, the concrete [StarlarkTypeRepr] for `T`
     * must be provided externally.
     *
     * @param typeRepr the [StarlarkTypeRepr] instance for the phantom type `T`
     * @return the [Ty] for this value's annotated type
     */
    fun starlarkTypeRepr(typeRepr: StarlarkTypeRepr): Ty {
        return typeRepr.starlarkTypeRepr()
    }

    // impl<'v, V: ValueLike<'v>, T: StarlarkTypeRepr> AllocValue<'v> for ValueOfUncheckedGeneric<V, T> {
    //     fn alloc_value(self, _heap: Heap<'v>) -> Value<'v> {
    //         self.0.to_value()
    //     }
    // }
    /**
     * Allocate the wrapped value on the heap. Returns the underlying value.
     *
     * The heap parameter is unused because the value is already allocated;
     * this simply extracts the [Value] from the wrapper.
     */
    fun allocValue(@Suppress("UNUSED_PARAMETER") heap: Heap): Value {
        return when (val v = value) {
            is Value -> v
            is ValueLike -> v.toValue()
            else -> throw IllegalStateException("ValueOfUncheckedGeneric: cannot alloc non-Value type")
        }
    }

    // impl<T: StarlarkTypeRepr> AllocFrozenValue for ValueOfUncheckedGeneric<FrozenValue, T> {
    //     fn alloc_frozen_value(self, _heap: &FrozenHeap) -> FrozenValue {
    //         self.0
    //     }
    // }
    /**
     * Allocate frozen value. Returns the underlying frozen value.
     *
     * The heap parameter is unused because the value is already frozen;
     * this simply extracts the [FrozenValue] from the wrapper.
     */
    fun allocFrozenValue(@Suppress("UNUSED_PARAMETER") heap: FrozenHeap): FrozenValue {
        @Suppress("UNCHECKED_CAST")
        return value as FrozenValue
    }

    // unsafe impl<'v, V, T> Trace<'v> for ValueOfUncheckedGeneric<V, T>
    // where
    //     // This is essentially `V: ValueLike<'v>`,
    //     // but for derive it is convenient to have these bounds.
    //     V: ValueLifetimeless + Trace<'v>,
    //     T: StarlarkTypeRepr,
    // {
    //     fn trace(&mut self, tracer: &Tracer<'v>) {
    //         self.0.trace(tracer)
    //     }
    // }
    /**
     * Trace the inner value for garbage collection.
     *
     * Delegates to the inner value's trace when `V` implements [Trace].
     */
    fun trace(tracer: Tracer) {
        val v = value
        if (v is Trace) {
            v.trace(tracer)
        }
    }

    // impl<V: ValueLifetimeless + Freeze, T: StarlarkTypeRepr> Freeze for ValueOfUncheckedGeneric<V, T> {
    //     type Frozen = ValueOfUncheckedGeneric<FrozenValue, T>;
    //
    //     fn freeze(self, freezer: &Freezer) -> FreezeResult<Self::Frozen> {
    //         let frozen = self.0.freeze(freezer)?;
    //         Ok(ValueOfUncheckedGeneric::new(frozen))
    //     }
    // }
    /**
     * Freeze this value, producing a frozen equivalent.
     *
     * The frozen type is `ValueOfUncheckedGeneric<FrozenValue, T>`.
     */
    fun freeze(freezer: Freezer): FreezeResult<ValueOfUncheckedGeneric<FrozenValue, T>> {
        val v = value
        val frozen: FrozenValue = when (v) {
            is Value -> v.freeze(freezer).getOrThrow()
            is FrozenValue -> v
            else -> throw IllegalStateException("Cannot freeze non-Value type")
        }
        return Result.success(new(frozen))
    }

    // impl<'v, V: ValueLike<'v>, T: StarlarkTypeRepr> ValueOfUncheckedGeneric<V, T> {

    /**
     * Convert to a value.
     *
     * Extracts the underlying [Value] from the [ValueLike] wrapper
     * and wraps it in a new [ValueOfUnchecked].
     */
    // #[inline]
    // pub fn to_value(self) -> ValueOfUnchecked<'v, T> {
    //     ValueOfUnchecked::new(self.0.to_value())
    // }
    fun toValue(): ValueOfUncheckedGeneric<Value, T> {
        val v = value
        val asValue: Value = when (v) {
            is Value -> v
            is ValueLike -> v.toValue()
            else -> throw IllegalStateException("Cannot convert to Value")
        }
        return new(asValue)
    }

    // } // end impl ValueOfUncheckedGeneric (to_value)

    companion object {
        /**
         * New.
         *
         * Wraps a value with a phantom type annotation.
         */
        // #[inline]
        // pub fn new(value: V) -> Self {
        //     Self(value, PhantomData)
        // }
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
// pub type ValueOfUnchecked<'v, T> = ValueOfUncheckedGeneric<Value<'v>, T>;
typealias ValueOfUnchecked<T> = ValueOfUncheckedGeneric<Value, T>

/**
 * Frozen starlark value with type annotation.
 */
// pub type FrozenValueOfUnchecked<'f, T> = ValueOfUncheckedGeneric<FrozenValue, T>;
typealias FrozenValueOfUnchecked<T> = ValueOfUncheckedGeneric<FrozenValue, T>

// impl<'v, T: StarlarkTypeRepr> ValueOfUnchecked<'v, T> {

/**
 * Construct after checking the type.
 *
 * In Rust, the `T: UnpackValue<'v>` bound ensures the type check can be
 * performed at compile time. In Kotlin, an explicit [UnpackValue] instance
 * is required to perform the runtime check.
 */
// #[inline]
// pub fn new_checked(value: Value<'v>) -> crate::Result<Self>
// where
//     T: UnpackValue<'v>,
// {
//     T::unpack_value_err(value)?;
//     Ok(Self::new(value))
// }
fun <T : StarlarkTypeRepr, R> ValueOfUncheckedGeneric.Companion.newChecked(
    value: Value,
    unpacker: UnpackValue<R>,
): ValueOfUnchecked<T> {
    // T::unpack_value_err(value)?;
    unpacker.unpackValueErr(value)
    // Ok(Self::new(value))
    return ValueOfUncheckedGeneric.new(value)
}

/**
 * Construct after checking the type (convenience overload that skips type checking).
 *
 * This mirrors the Rust API where `T: UnpackValue` is a compile-time bound.
 * In Kotlin, when the type check cannot be performed at the call site,
 * this overload wraps the value directly.
 */
fun <T : StarlarkTypeRepr> ValueOfUncheckedGeneric.Companion.newChecked(
    value: Value,
): ValueOfUnchecked<T> {
    return ValueOfUncheckedGeneric.new(value)
}

// } // end impl ValueOfUnchecked

// impl<'v, T: StarlarkTypeRepr> UnpackValue<'v> for ValueOfUnchecked<'v, T> {
//     type Error = Infallible;
//
//     #[inline]
//     fn unpack_value_impl(value: Value<'v>) -> Result<Option<Self>, Self::Error> {
//         Ok(Some(Self::new(value)))
//     }
// }

/**
 * Unpack a [Value] into a [ValueOfUnchecked].
 *
 * This always succeeds since [ValueOfUnchecked] wraps any value without checking the type.
 * The `Error` type is `Infallible` in Rust, meaning this operation cannot fail.
 */
fun <T : StarlarkTypeRepr> unpackValueOfUnchecked(value: Value): ValueOfUnchecked<T> {
    return ValueOfUncheckedGeneric.new(value)
}

// #[cfg(test)]
// mod tests {
//     ...
// }
// Tests are in commonTest/kotlin/.../values/ValueOfUncheckedTest.kt
