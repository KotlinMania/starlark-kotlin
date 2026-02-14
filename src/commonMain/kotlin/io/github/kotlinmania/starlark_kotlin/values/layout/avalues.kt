// port-lint: source src/values/layout/avalues.rs
package io.github.kotlinmania.starlark_kotlin.values.layout.avalues

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

/// AValue implementations for various Starlark types.
///
/// Submodules (Kotlin files in this package):
/// - array: AValue for Array and AnyArray types
/// - complex: AValue for ComplexValue types
/// - list: AValue for List type
/// - simple: AValue for simple (non-complex) types
/// - static_: AValue for statically allocated values
/// - str_: AValue for StarlarkStr type
/// - tuple: AValue for Tuple type
