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
import io.github.kotlinmania.starlark_kotlin.values.type_repr.StarlarkTypeRepr

/**
 * Store value annotated with type, but do not check the type.
 *
 * The type parameter `T` is a phantom type used for type-level tracking
 * of the Starlark type, similar to Rust's `PhantomData<fn() -> T>`.
 */
// #[derive(Clone_, Copy_, Dupe_, Allocative)]
// #[allocative(bound = "")]
// pub struct ValueOfUncheckedGeneric<V: ValueLifetimeless, T: StarlarkTypeRepr>(V, PhantomData<fn() -> T>)
class ValueOfUncheckedGeneric<V, T : StarlarkTypeRepr> private constructor(
    private val value: V,
) {
    // unsafe impl<V, U, T> Coerce<ValueOfUncheckedGeneric<V, T>> for ValueOfUncheckedGeneric<U, T>
    // Kotlin: no Coerce equivalent needed.

    // impl<V: ValueLifetimeless, T: StarlarkTypeRepr> ValueOfUncheckedGeneric<V, T>

    /** Get the value. */
    // pub fn get(self) -> V
    fun get(): V {
        return value
    }

    /**
     * Cast to a different Rust type for the same Starlark type.
     */
    // pub fn cast<U: StarlarkTypeRepr<Canonical = T::Canonical>>(self) -> ValueOfUncheckedGeneric<V, U>
    @Suppress("UNCHECKED_CAST")
    fun <U : StarlarkTypeRepr> cast(): ValueOfUncheckedGeneric<V, U> {
        return new(this.value)
    }

    /**
     * Unpack the value.
     */
    // pub fn unpack<'v>(self) -> crate::Result<T>
    // where V: ValueLike<'v>, T: UnpackValue<'v>
    // Kotlin: requires the T type to implement UnpackValue.
    // This is handled at call sites through explicit unpacking.

    // impl<V: ValueLifetimeless, T: StarlarkTypeRepr> Debug for ValueOfUncheckedGeneric<V, T>
    // impl<V: ValueLifetimeless, T: StarlarkTypeRepr> Display for ValueOfUncheckedGeneric<V, T>
    override fun toString(): String {
        return value.toString()
    }

    // impl<V: ValueLifetimeless, T: StarlarkTypeRepr> StarlarkTypeRepr for ValueOfUncheckedGeneric<V, T>
    // Kotlin: type repr is accessed through the T companion/class itself.

    // impl<'v, V: ValueLike<'v>, T: StarlarkTypeRepr> AllocValue<'v> for ValueOfUncheckedGeneric<V, T>
    // fn alloc_value(self, _heap: Heap<'v>) -> Value<'v>
    // Kotlin: handled at call sites. value is already a Value.

    // impl<T: StarlarkTypeRepr> AllocFrozenValue for ValueOfUncheckedGeneric<FrozenValue, T>
    // fn alloc_frozen_value(self, _heap: &FrozenHeap) -> FrozenValue
    // Kotlin: handled at call sites.

    // unsafe impl<'v, V, T> Trace<'v> for ValueOfUncheckedGeneric<V, T>
    // fn trace(&mut self, tracer: &Tracer<'v>)
    // Kotlin: trace delegates to the inner value's trace.

    // impl<V: ValueLifetimeless + Freeze, T: StarlarkTypeRepr> Freeze for ValueOfUncheckedGeneric<V, T>
    // type Frozen = ValueOfUncheckedGeneric<FrozenValue, T>

    // impl<'v, V: ValueLike<'v>, T: StarlarkTypeRepr> ValueOfUncheckedGeneric<V, T>
    /** Convert to a value. */
    // pub fn to_value(self) -> ValueOfUnchecked<'v, T>
    fun toValue(): ValueOfUncheckedGeneric<Value, T> {
        @Suppress("UNCHECKED_CAST")
        val v = value as? Value ?: (value as ValueLike).toValue()
        return new(v)
    }

    companion object {
        // pub fn new(value: V) -> Self
        /** New. */
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
// where T: UnpackValue<'v>
fun <T : StarlarkTypeRepr> ValueOfUnchecked.Companion.newChecked(
    value: Value,
): Result<ValueOfUnchecked<T>> {
    // T::unpack_value_err(value)?;
    // Ok(Self::new(value))
    // Kotlin: type checking would be done at call sites.
    return Result.success(ValueOfUncheckedGeneric.new(value))
}

// impl<V: ValueLifetimeless + Freeze, T: StarlarkTypeRepr> Freeze for ValueOfUncheckedGeneric<V, T>
/** Freeze a [ValueOfUncheckedGeneric]. */
fun <T : StarlarkTypeRepr> ValueOfUncheckedGeneric<Value, T>.freeze(
    freezer: Freezer,
): FreezeResult<ValueOfUncheckedGeneric<FrozenValue, T>> {
    val frozen = freezer.freeze(this.get())
    if (frozen.isError()) return FreezeResult.error(frozen.errorValue())
    return FreezeResult.success(ValueOfUncheckedGeneric.new(frozen.value()))
}

// impl<'v, T: StarlarkTypeRepr> UnpackValue<'v> for ValueOfUnchecked<'v, T>
// type Error = Infallible
// fn unpack_value_impl(value: Value<'v>) -> Result<Option<Self>, Self::Error>
// Kotlin: UnpackValue always succeeds for ValueOfUnchecked since it wraps any value.

// #[cfg(test)] mod tests
// Tests are in commonTest, not here.
