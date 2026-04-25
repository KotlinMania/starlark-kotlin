// port-lint: source src/util/non_static_type_id.rs
package io.github.kotlinmania.starlark.util.nonstatictypeid

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

import kotlin.reflect.KClass

// #![cfg(test)]
// Rust: This entire module is test-only.
// In Kotlin, type erasure means there's no direct equivalent to Rust's TypeId for non-static lifetimes.
// We provide a KClass-based approximation for test utilities.

// pub(crate) fn non_static_type_id<T: ?Sized>() -> TypeId
/**
 * Get the runtime type identifier for a type.
 *
 * In Rust, this uses unsafe transmute to get TypeId for types with non-'static lifetimes.
 * In Kotlin, KClass serves as the type identifier since there are no lifetime parameters.
 */
internal inline fun <reified T : Any> nonStaticTypeId(): KClass<T> {
    return T::class
}

// #[cfg(test)] mod tests
// Tests are in commonTest, not here.
