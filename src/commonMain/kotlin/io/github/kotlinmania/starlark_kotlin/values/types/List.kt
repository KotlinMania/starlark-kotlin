// port-lint: source src/values/types/list.rs
package io.github.kotlinmania.starlark_kotlin.values.types.list

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

/**
 * The list type, a mutable sequence of values.
 *
 * Submodules:
 * - [Alloc.kt] — [AllocList] allocation helper
 * - [Globals.kt] — global list constructor functions
 * - [ListType.kt] — [ListType] type descriptor
 * - [Methods.kt] — list methods (append, extend, etc.)
 * - [Refs.kt] — [ListRef] reference type
 * - [Unpack.kt] — [UnpackList] list unpacking
 * - [Value.kt] — list value implementation
 *
 * Public types (re-exported from submodules in Rust):
 * - [AllocList] — allocation helper for lists
 * - [ListType] — type descriptor for list
 * - [ListRef] — immutable list reference
 * - [UnpackList] — helper for unpacking list values
 */

// Re-exports from submodules (Kotlin: all public types in this package are inherently accessible)
// Rust: pub use crate::values::types::list::alloc::AllocList;
// Rust: pub use crate::values::types::list::list_type::ListType;
// Rust: pub use crate::values::types::list::refs::ListRef;
// Rust: pub use crate::values::types::list::unpack::UnpackList;
