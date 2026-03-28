// port-lint: source src/values/layout/complex.rs
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

import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.AllocValue
import io.github.kotlinmania.starlark_kotlin.values.ComplexValue
import io.github.kotlinmania.starlark_kotlin.values.Freeze
import io.github.kotlinmania.starlark_kotlin.values.Freezer
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.Trace
import io.github.kotlinmania.starlark_kotlin.values.UnpackValue
import io.github.kotlinmania.starlark_kotlin.values.freeze_error.FreezeError
import io.github.kotlinmania.starlark_kotlin.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.ValueHolder
import io.github.kotlinmania.starlark_kotlin.values.trace
import io.github.kotlinmania.starlark_kotlin.values.Tracer
import io.github.kotlinmania.starlark_kotlin.values.freeze_error.FreezeResult
import kotlin.reflect.KClass

/// Value which is either a complex mutable value or a frozen value.
///
/// In Rust this is `ValueTypedComplex<'v, T>` with `PhantomData<T>`, parameterised
/// over the complex value type T (where T: ComplexValue) and its frozen counterpart
/// T::Frozen (where T::Frozen: StarlarkValue<'static>).
///
/// In Kotlin, the class is parameterised by [T] (the mutable ComplexValue type) and
/// [F] (its frozen StarlarkValue counterpart). Both type parameters are reified where
/// needed via inline factory methods.
// #[derive(Copy_, Clone_, Dupe_, Allocative)]
// pub struct ValueTypedComplex<'v, T>(Value<'v>, PhantomData<T>)
// where T: ComplexValue<'v>, T::Frozen: StarlarkValue<'static>;
class ValueTypedComplex<T : ComplexValue, F : StarlarkValue> @PublishedApi internal constructor(
    // Mutable: tracer.trace(&mut self.0) may update the pointer during GC.
    @PublishedApi internal var value: Value,
    @PublishedApi internal val mutableClass: KClass<T>,
    @PublishedApi internal val frozenClass: KClass<F>,
) : StarlarkTypeRepr, Trace {

    // impl ValueTypedComplex

    companion object {
        /// Internal non-inline helper used by public inline [new]: checks if the raw pointer
        /// held by [value] is an instance of [mutableClass] or [frozenClass].
        @PublishedApi
        @Suppress("UNCHECKED_CAST")
        internal fun <T : ComplexValue, F : StarlarkValue> newImpl(
            value: Value,
            mutableClass: KClass<T>,
            frozenClass: KClass<F>,
        ): ValueTypedComplex<T, F>? {
            val raw = value.getRef().value.ptr
            return if (mutableClass.isInstance(raw) || frozenClass.isInstance(raw)) {
                ValueTypedComplex(value, mutableClass, frozenClass)
            } else {
                null
            }
        }

        /// Downcast.
        // pub fn new(value: Value<'v>) -> Option<Self>
        inline fun <reified T : ComplexValue, reified F : StarlarkValue> new(
            value: Value,
        ): ValueTypedComplex<T, F>? = newImpl(value, T::class, F::class)

        /// Downcast.
        // pub fn new_err(value: Value<'v>) -> crate::Result<Self>
        inline fun <reified T : ComplexValue, reified F : StarlarkValue> newErr(
            value: Value,
        ): Result<ValueTypedComplex<T, F>> {
            val result = new<T, F>(value)
            return if (result != null) {
                Result.success(result)
            } else {
                Result.failure(
                    IllegalArgumentException(
                        "Expected value of type `${T::class.simpleName}`, got: `${value.toStringForTypeError()}`"
                    )
                )
            }
        }

        /// Convert from a ValueTyped to a ValueTypedComplex.
        // impl From<ValueTyped<'v, T>> for ValueTypedComplex<'v, T>
        inline fun <reified T : ComplexValue, reified F : StarlarkValue> from(
            typed: ValueTyped<T>,
        ): ValueTypedComplex<T, F> {
            return ValueTypedComplex(typed.toValue(), T::class, F::class)
        }
    }

    /// Get the value back.
    // pub fn to_value(self) -> Value<'v>
    fun toValue(): Value = value

    /// Downcast a Value to T using its stored KClass, via the AValueDyn raw pointer.
    @Suppress("UNCHECKED_CAST")
    private fun downcastMutable(): T? {
        val raw = value.getRef().value.ptr
        return if (mutableClass.isInstance(raw)) raw as T else null
    }

    /// Downcast a Value to F using its stored KClass, via the AValueDyn raw pointer.
    @Suppress("UNCHECKED_CAST")
    private fun downcastFrozen(): F? {
        val raw = value.getRef().value.ptr
        return if (frozenClass.isInstance(raw)) raw as F else null
    }

    /// Unpack the mutable or frozen value.
    ///
    /// Returns either the mutable value (Left) or the frozen value (Right).
    // pub fn unpack(self) -> Either<&'v T, &'v T::Frozen>
    fun unpack(): Any {
        val mutable = downcastMutable()
        if (mutable != null) return mutable
        val frozen = downcastFrozen()
        if (frozen != null) return frozen
        error("unreachable: validated at construction")
    }

    /// Unpack the mutable value, or null if frozen.
    fun unpackMutable(): T? = downcastMutable()

    /// Unpack the frozen value, or null if mutable.
    fun unpackFrozen(): F? = downcastFrozen()

    // impl StarlarkTypeRepr for ValueTypedComplex
    // type Canonical = <T as StarlarkTypeRepr>::Canonical;
    // fn starlark_type_repr() -> Ty { T::starlark_type_repr() }
    override fun starlarkTypeRepr(): Ty {
        // Delegate to the mutable type's repr.
        val instance = downcastMutable()
        if (instance is StarlarkTypeRepr) {
            return instance.starlarkTypeRepr()
        }
        return Ty.any()
    }

    // impl AllocValue for ValueTypedComplex
    // fn alloc_value(self, _heap: &'v Heap) -> Value<'v> { self.0 }
    fun allocValue(@Suppress("UNUSED_PARAMETER") heap: Heap): Value = value

    // impl UnpackValue for ValueTypedComplex
    // fn unpack_value_impl(value: Value<'v>) -> Result<Option<Self>, Self::Error> { Ok(Self::new(value)) }
    // Kotlin: static unpack is handled via the `new` companion method.

    // impl Debug for ValueTypedComplex
    // fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result
    override fun toString(): String = "ValueTypedComplex($value)"

    // impl Trace for ValueTypedComplex
    // fn trace(&mut self, tracer: &Tracer<'v>)
    override fun trace(tracer: Tracer) {
        val holder = ValueHolder(value)
        tracer.trace(holder)
        value = holder.value
        // If type of value changed, dereference will produce the wrong object type.
        // debug_assert!(Self::new(self.0).is_some());
    }

    // impl Freeze for ValueTypedComplex
    // fn freeze(self, freezer: &Freezer) -> FreezeResult<FrozenValueTyped<'static, T::Frozen>>
    fun freeze(freezer: Freezer): FreezeResult<FrozenValueTyped<F>> {
        val frozenResult = value.freeze(freezer)
        if (frozenResult.isFailure) {
            return Result.failure(frozenResult.exceptionOrNull()!!)
        }
        val frozenFv = frozenResult.getOrThrow()
        // Verify the frozen value has the expected type (mirrors FrozenValueTyped::new_err).
        val raw = frozenFv.toValue().getRef().value.ptr
        if (!frozenClass.isInstance(raw)) {
            return Result.failure(
                FreezeError.new(
                    "Expected value of type `${frozenClass.simpleName}`, got: `${frozenFv.toValue().toStringForTypeError()}`"
                )
            )
        }
        return Result.success(FrozenValueTyped.newUnchecked(frozenFv))
    }
}

// #[cfg(test)] mod tests { ... }
// Tests are in commonTest, not here.
