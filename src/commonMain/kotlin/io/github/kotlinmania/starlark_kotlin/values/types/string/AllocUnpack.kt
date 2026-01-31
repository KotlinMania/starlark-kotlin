// port-lint: source src/values/types/string/alloc_unpack.rs
package io.github.kotlinmania.starlark_kotlin.values.types.string

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
 * Implementations of alloc and unpack traits for string.
 *
 * This file provides extension functions for String and Char types to support
 * allocation on heaps and unpacking from values in the Starlark runtime.
 *
 * Note: The actual type definitions (FrozenHeap, Heap, Value, etc.) are declared
 * elsewhere and will be linked when the core value system is fully ported.
 *
 * Rust source implementations:
 * - impl AllocFrozenValue for String
 * - impl AllocFrozenStringValue for String
 * - impl<'a> AllocFrozenValue for &'a str
 * - impl<'a> AllocFrozenStringValue for &'a str
 * - impl<'v> AllocValue<'v> for String
 * - impl<'v> AllocStringValue<'v> for String
 * - impl StarlarkTypeRepr for char
 * - impl<'v> AllocValue<'v> for char
 * - impl<'v> AllocStringValue<'v> for char
 * - impl StarlarkTypeRepr for &'_ String
 * - impl<'v> AllocValue<'v> for &'_ String
 * - impl<'v> AllocStringValue<'v> for &'_ String
 * - impl<'v> AllocValue<'v> for &'_ str
 * - impl<'v> AllocStringValue<'v> for &'_ str
 * - impl<'v> UnpackValue<'v> for &'v str
 * - impl<'v> UnpackValue<'v> for String
 *
 * Kotlin adaptations:
 * - String is already a reference type in Kotlin, so we don't need separate
 *   implementations for borrowed (&str) vs owned (String) strings
 * - Extension functions replace trait implementations
 * - Generic type parameter V represents the lifetime parameter 'v from Rust
 */

// Placeholder type declarations for types not yet fully ported
// These match the interfaces from the Rust traits but are declared as expect
// to be provided by platform-specific implementations later

// Note: Many of these types are already declared in other files in this package
// (StrType.kt, Methods.kt, etc.) but with different signatures.
// We avoid redeclaring them here to prevent conflicts.

// The following extension functions implement the allocation and unpacking
// functionality without requiring the full type definitions.

// impl AllocFrozenValue for String
// impl AllocFrozenStringValue for String

/**
 * Allocates a [String] on a frozen heap.
 *
 * Corresponds to Rust:
 * ```rust
 * impl AllocFrozenValue for String {
 *     fn alloc_frozen_value(self, heap: &FrozenHeap) -> FrozenValue {
 *         self.alloc_frozen_string_value(heap).to_frozen_value()
 *     }
 * }
 * ```
 */
// Note: These functions are commented out to avoid conflicts with existing expect declarations
// They will be uncommented when the core types are properly defined
/*
fun String.allocFrozenValue(heap: FrozenHeap): FrozenValue {
    return this.allocFrozenStringValue(heap).toFrozenValue()
}

fun String.allocFrozenStringValue(heap: FrozenHeap): FrozenStringValue {
    return heap.allocStr(this)
}
*/

// impl<'a> AllocFrozenValue for &'a str
// impl<'a> AllocFrozenStringValue for &'a str
// Kotlin note: String is already a reference type, covered by the above implementations

// impl<'v> AllocValue<'v> for String
// impl<'v> AllocStringValue<'v> for String

/**
 * Allocates a [String] on a heap.
 *
 * Corresponds to Rust:
 * ```rust
 * impl<'v> AllocValue<'v> for String {
 *     fn alloc_value(self, heap: Heap<'v>) -> Value<'v> {
 *         self.alloc_string_value(heap).to_value()
 *     }
 * }
 * ```
 */
// Note: These functions are commented out to avoid conflicts with existing expect declarations
// They will be uncommented when the core types are properly defined
/*
fun <V> String.allocValue(heap: Heap<V>): Value<V> {
    return this.allocStringValue(heap).toValue()
}

fun <V> String.allocStringValue(heap: Heap<V>): StringValue<V> {
    return heap.allocStr(this)
}
*/

// impl StarlarkTypeRepr for char

/**
 * Type representation for [Char] in Starlark.
 *
 * Corresponds to Rust:
 * ```rust
 * impl StarlarkTypeRepr for char {
 *     type Canonical = <String as StarlarkTypeRepr>::Canonical;
 *
 *     fn starlark_type_repr() -> Ty {
 *         String::starlark_type_repr()
 *     }
 * }
 * ```
 *
 * Note: Char uses the same type representation as String in Starlark because
 * individual characters are represented as single-character strings.
 */
// Kotlin note: Associated types will be handled differently when StarlarkTypeRepr is fully ported

// impl<'v> AllocValue<'v> for char
// impl<'v> AllocStringValue<'v> for char

/**
 * Allocates a [Char] on a heap as a single-character string.
 *
 * Corresponds to Rust:
 * ```rust
 * impl<'v> AllocValue<'v> for char {
 *     fn alloc_value(self, heap: Heap<'v>) -> Value<'v> {
 *         self.alloc_string_value(heap).to_value()
 *     }
 * }
 * ```
 */
// Note: These functions are commented out to avoid conflicts with existing expect declarations
// They will be uncommented when the core types are properly defined
/*
fun <V> Char.allocValue(heap: Heap<V>): Value<V> {
    return this.allocStringValue(heap).toValue()
}

fun <V> Char.allocStringValue(heap: Heap<V>): StringValue<V> {
    return heap.allocChar(this)
}
*/

// impl StarlarkTypeRepr for &'_ String
// impl<'v> AllocValue<'v> for &'_ String
// impl<'v> AllocStringValue<'v> for &'_ String
// impl<'v> AllocValue<'v> for &'_ str
// impl<'v> AllocStringValue<'v> for &'_ str
// Kotlin note: String is already a reference type in Kotlin, so all string reference
// implementations are covered by the String extension functions above

// impl<'v> UnpackValue<'v> for &'v str

/**
 * Unpacks a borrowed string from a [Value].
 *
 * Corresponds to Rust:
 * ```rust
 * impl<'v> UnpackValue<'v> for &'v str {
 *     type Error = Infallible;
 *
 *     fn unpack_value_impl(value: Value<'v>) -> Result<Option<Self>, Self::Error> {
 *         Ok(value.unpack_str())
 *     }
 * }
 * ```
 *
 * Returns the string if the value is a string, null otherwise.
 * Error type is Nothing (Infallible in Rust), meaning this operation never fails.
 */
// Note: This function is commented out to avoid conflicts with existing expect declarations
// It will be uncommented when the core types are properly defined
/*
fun <V> unpackValueImplBorrowedString(value: Value<V>): Result<String?> {
    return Result.success(value.unpackStr())
}
*/

// impl<'v> UnpackValue<'v> for String

/**
 * Unpacks an owned string from a [Value].
 *
 * Corresponds to Rust:
 * ```rust
 * impl<'v> UnpackValue<'v> for String {
 *     type Error = Infallible;
 *
 *     fn unpack_value_impl(value: Value<'v>) -> Result<Option<Self>, Self::Error> {
 *         Ok(value.unpack_str().map(ToOwned::to_owned))
 *     }
 * }
 * ```
 *
 * Returns a new owned String if the value is a string, null otherwise.
 * Error type is Nothing (Infallible in Rust), meaning this operation never fails.
 *
 * Kotlin note: Since Kotlin strings are immutable and already behave like references,
 * we don't need to explicitly clone/own them. The `.map(ToOwned::to_owned)` operation
 * from Rust is effectively a no-op in Kotlin, but the semantic intent is preserved
 * (converting from borrowed to owned).
 */
// Note: This function is commented out to avoid conflicts with existing expect declarations
// It will be uncommented when the core types are properly defined
/*
fun <V> unpackValueImplOwnedString(value: Value<V>): Result<String?> {
    return Result.success(value.unpackStr()?.let { it })
}
*/
