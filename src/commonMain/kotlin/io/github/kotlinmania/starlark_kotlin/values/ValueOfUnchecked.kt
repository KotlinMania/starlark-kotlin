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
import io.github.kotlinmania.starlark_kotlin.values.freeze_error.FreezeResult
import io.github.kotlinmania.starlark_kotlin.values.layout.Freezer
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.layout.ValueLike
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Tracer

/** Store value annotated with type, but do not check the type. */
// #[derive(Clone_, Copy_, Dupe_, Allocative)]
// #[allocative(bound = "")]
// pub struct ValueOfUncheckedGeneric<V: ValueLifetimeless, T: StarlarkTypeRepr>(
//     V,
//     PhantomData<fn() -> T>,
// );
class ValueOfUncheckedGeneric<V, T : StarlarkTypeRepr> private constructor(
    private val value: V,
) : StarlarkTypeRepr, Trace {

    // unsafe impl<V, U, T> Coerce<ValueOfUncheckedGeneric<V, T>> for ValueOfUncheckedGeneric<U, T>
    // where
    //     V: ValueLifetimeless,
    //     U: ValueLifetimeless,
    //     U: Coerce<V>,
    //     T: StarlarkTypeRepr,
    // Kotlin: Coerce is a Rust zero-cost abstraction with no Kotlin equivalent needed.

    // impl<V: ValueLifetimeless, T: StarlarkTypeRepr> ValueOfUncheckedGeneric<V, T>

    /** New. */
    // pub fn new(value: V) -> Self
    // Moved to companion object below.

    /** Cast to a different Rust type for the same Starlark type. */
    // pub fn cast<U: StarlarkTypeRepr<Canonical = T::Canonical>>(
    //     self,
    // ) -> ValueOfUncheckedGeneric<V, U>
    @Suppress("UNCHECKED_CAST")
    fun <U : StarlarkTypeRepr> cast(): ValueOfUncheckedGeneric<V, U> {
        return new(this.value)
    }

    /** Get the value. */
    // pub fn get(self) -> V
    fun get(): V {
        return value
    }

    /** Unpack the value. */
    // pub fn unpack<'v>(self) -> crate::Result<T>
    // where
    //     V: ValueLike<'v>,
    //     T: UnpackValue<'v>,
    //
    // Kotlin: Due to type erasure, T cannot be statically dispatched.
    // This overload accepts an explicit UnpackValue instance to perform the unpack.
    fun <R> unpack(unpacker: UnpackValue<R>): R {
        val v = value
        val asValue: Value = when (v) {
            is Value -> v
            is ValueLike -> v.toValue()
            else -> throw IllegalStateException("Cannot convert to Value")
        }
        return unpacker.unpackValueErr(asValue)
    }

    // impl<V: ValueLifetimeless, T: StarlarkTypeRepr> Debug for ValueOfUncheckedGeneric<V, T>
    // fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result
    /** Debug representation of this value. */
    fun toDebugString(): String {
        return "ValueOfUnchecked(${get()})"
    }

    // impl<V: ValueLifetimeless, T: StarlarkTypeRepr> Display for ValueOfUncheckedGeneric<V, T>
    // fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result
    /** Display representation of this value. */
    override fun toString(): String {
        return value.toString()
    }

    // impl<V: ValueLifetimeless, T: StarlarkTypeRepr> StarlarkTypeRepr for ValueOfUncheckedGeneric<V, T>
    //     type Canonical = T::Canonical;
    //
    //     fn starlark_type_repr() -> Ty {
    //         <Self as StarlarkTypeRepr>::Canonical::starlark_type_repr()
    //     }
    /**
     * The representation of the starlark type.
     *
     * Kotlin: The Canonical associated type is not directly representable.
     * In Rust, this calls `T::Canonical::starlark_type_repr()`.
     * Since Canonical types produce the same Ty as their source by design,
     * callers should provide the type repr at construction if needed,
     * because T is erased at runtime.
     */
    override fun starlarkTypeRepr(): Ty {
        throw UnsupportedOperationException(
            "StarlarkTypeRepr for ValueOfUncheckedGeneric requires a concrete T"
        )
    }

    // impl<'v, V: ValueLike<'v>, T: StarlarkTypeRepr> AllocValue<'v> for ValueOfUncheckedGeneric<V, T>
    // fn alloc_value(self, _heap: Heap<'v>) -> Value<'v> {
    //     self.0.to_value()
    // }
    /** Alloc the wrapped value on the heap. Returns the underlying value. */
    fun allocValue(@Suppress("UNUSED_PARAMETER") heap: Heap): Value {
        return when (val v = value) {
            is Value -> v
            is ValueLike -> v.toValue()
            else -> throw IllegalStateException("ValueOfUncheckedGeneric: cannot alloc non-Value type")
        }
    }

    // impl<T: StarlarkTypeRepr> AllocFrozenValue for ValueOfUncheckedGeneric<FrozenValue, T>
    // fn alloc_frozen_value(self, _heap: &FrozenHeap) -> FrozenValue {
    //     self.0
    // }
    /** Alloc frozen value. Returns the underlying frozen value. */
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
    /** Trace delegates to the inner value's trace when V is Trace. */
    override fun trace(tracer: Tracer) {
        val v = value
        if (v is Trace) {
            v.trace(tracer)
        }
    }

    // impl<V: ValueLifetimeless + Freeze, T: StarlarkTypeRepr> Freeze for ValueOfUncheckedGeneric<V, T>
    //     type Frozen = ValueOfUncheckedGeneric<FrozenValue, T>;
    //
    //     fn freeze(self, freezer: &Freezer) -> FreezeResult<Self::Frozen> {
    //         let frozen = self.0.freeze(freezer)?;
    //         Ok(ValueOfUncheckedGeneric::new(frozen))
    //     }
    /** Freeze this value, producing a frozen equivalent. */
    fun freeze(freezer: Freezer): FreezeResult<ValueOfUncheckedGeneric<FrozenValue, T>> {
        val v = value
        val frozen: FrozenValue = when (v) {
            is Value -> v.freeze(freezer).getOrThrow()
            is FrozenValue -> v
            else -> throw IllegalStateException("Cannot freeze non-Value type")
        }
        return Result.success(new(frozen))
    }

    // impl<'v, V: ValueLike<'v>, T: StarlarkTypeRepr> ValueOfUncheckedGeneric<V, T>

    /** Convert to a value. */
    // pub fn to_value(self) -> ValueOfUnchecked<'v, T>
    fun toValue(): ValueOfUncheckedGeneric<Value, T> {
        val v = value
        val asValue: Value = when (v) {
            is Value -> v
            is ValueLike -> v.toValue()
            else -> throw IllegalStateException("Cannot convert to Value")
        }
        return new(asValue)
    }

    companion object {
        /** New. */
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
// pub type ValueOfUnchecked<'v, T> = ValueOfUncheckedGeneric<Value<'v>, T>
typealias ValueOfUnchecked<T> = ValueOfUncheckedGeneric<Value, T>

/** Frozen starlark value with type annotation. */
// pub type FrozenValueOfUnchecked<'f, T> = ValueOfUncheckedGeneric<FrozenValue, T>
typealias FrozenValueOfUnchecked<T> = ValueOfUncheckedGeneric<FrozenValue, T>

// impl<'v, T: StarlarkTypeRepr> ValueOfUnchecked<'v, T>

/** Construct after checking the type. */
// pub fn new_checked(value: Value<'v>) -> crate::Result<Self>
// where
//     T: UnpackValue<'v>,
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

// impl<'v, T: StarlarkTypeRepr> UnpackValue<'v> for ValueOfUnchecked<'v, T>
//     type Error = Infallible;
//
//     fn unpack_value_impl(value: Value<'v>) -> Result<Option<Self>, Self::Error> {
//         Ok(Some(Self::new(value)))
//     }

/**
 * Unpack a [Value] into a [ValueOfUnchecked].
 *
 * This always succeeds since [ValueOfUnchecked] wraps any value without checking the type.
 * The `Error` type is `Infallible` in Rust, meaning this operation cannot fail.
 */
fun <T : StarlarkTypeRepr> unpackValueOfUnchecked(value: Value): ValueOfUnchecked<T> {
    return ValueOfUncheckedGeneric.new(value)
}

// #[cfg(test)] mod tests
// Tests are in commonTest/kotlin/.../values/ValueOfUncheckedTest.kt
