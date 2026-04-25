// port-lint: source src/values/types/none.rs
package io.github.kotlinmania.starlark_kotlin.values.types.none

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
 * The `None` type.
 *
 * Submodules:
 * - [Globals.kt] — global None functions
 * - [NoneOr.kt] — [NoneOr] optional type wrapper
 * - [NoneType.kt] — [NoneType] type definition
 *
 * Public types (re-exported from submodules in Rust):
 * - [NoneOr] — a value that is either None or a typed value
 * - [NoneType] — the None type descriptor
 */

// Re-exports from submodules (Kotlin: all public types in this package are inherently accessible)
// Rust: pub use none_or::NoneOr;
// Rust: pub use none_type::NoneType;
