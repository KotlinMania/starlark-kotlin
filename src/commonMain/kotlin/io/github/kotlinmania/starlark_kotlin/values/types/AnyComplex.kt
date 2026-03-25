// port-lint: source src/values/types/any_complex.rs
package io.github.kotlinmania.starlark_kotlin.values.types.any_complex

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

/// A type `StarlarkAnyComplex` which can wrap any value into a `Value`.

/// Allocate arbitrary value on the starlark heap without implementing full `StarlarkValue`.
///
/// This is useful for data not directly visible to starlark code.
///
/// This type is for "complex" values (with tracing during GC). For no GC version check
/// `StarlarkAny`.
// #[derive(Trace, Freeze, Allocative, ProvidesStaticType, NoSerialize)]
// pub struct StarlarkAnyComplex<T> { pub value: T }
class StarlarkAnyComplex<T : Any>(
    /// The value.
    val value: T,
) {
    companion object {
        // pub fn new(value: T) -> StarlarkAnyComplex<T>
        /// Construct a new `StarlarkAnyComplex` value, which can be allocated on the heap.
        fun <T : Any> new(value: T): StarlarkAnyComplex<T> {
            return StarlarkAnyComplex(value)
        }

        // pub fn get(value: Value<'v>) -> Option<&'v T>
        /// Obtain the value from a `Value`, if it is a `StarlarkAnyComplex<T>`.
        @Suppress("UNCHECKED_CAST")
        inline fun <reified T : Any> get(value: Any): T? {
            val complex = value as? StarlarkAnyComplex<*> ?: return null
            return complex.value as? T
        }

        // pub fn get_err(value: Value<'v>) -> crate::Result<&'v T>
        /// Obtain the value from a `Value`, if it is a `StarlarkAnyComplex<T>`.
        @Suppress("UNCHECKED_CAST")
        inline fun <reified T : Any> getErr(value: Any): T {
            val complex = value as? StarlarkAnyComplex<*>
                ?: throw IllegalArgumentException("Value is not StarlarkAnyComplex")
            return complex.value as? T
                ?: throw IllegalArgumentException(
                    "StarlarkAnyComplex value is not of expected type ${T::class}"
                )
        }
    }

    // Proper `Debug` is hard to require from users because of `Freeze` and `ProvidesStaticType`.
    // impl Debug for StarlarkAnyComplex<T>
    // fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result
    override fun toString(): String {
        return "${value!!::class.simpleName ?: "StarlarkAnyComplex"} { .. }"
    }

    // #[starlark_value(type = "any_complex")]
    // impl StarlarkValue for StarlarkAnyComplex<T>
    // Kotlin: StarlarkValue trait methods would be implemented here
    // when the StarlarkValue interface is fully ported.

    // impl AllocValue for StarlarkAnyComplex<T>
    // fn alloc_value(self, heap: Heap<'v>) -> Value<'v>
    // Kotlin: allocation is handled by the heap directly.
}

// #[cfg(test)] mod tests
// Tests are in commonTest, not here.
