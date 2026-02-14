// port-lint: source src/util/non_static_type_id.rs
package io.github.kotlinmania.starlark_kotlin.util.non_static_type_id

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
// Rust: this is a test-only utility for getting TypeId of non-'static types.
// Kotlin: no lifetime system, so type identity is always available via KClass.

// pub(crate) fn non_static_type_id<T: ?Sized>() -> TypeId
// Kotlin: KClass serves as type identity; no lifetime constraints.
internal inline fun <reified T : Any> nonStaticTypeId(): KClass<T> {
    return T::class
}
