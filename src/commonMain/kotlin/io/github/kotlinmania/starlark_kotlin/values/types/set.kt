// port-lint: source src/values/types/set.rs
package io.github.kotlinmania.starlark_kotlin.values.types

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
 * The set type.
 *
 * Submodules:
 * - [methods][io.github.kotlinmania.starlark_kotlin.values.types.set.Methods] - set methods
 * - [refs][io.github.kotlinmania.starlark_kotlin.values.types.set.Refs] - set references (SetRef, SetMut)
 * - [set][io.github.kotlinmania.starlark_kotlin.values.types.set.Set] - set globals
 * - [value][io.github.kotlinmania.starlark_kotlin.values.types.set.Value] - set value type
 */

// Re-exports (mirrors Rust's pub use declarations)
internal typealias SetMutExport = io.github.kotlinmania.starlark_kotlin.values.types.set.SetMut
internal typealias SetRefExport = io.github.kotlinmania.starlark_kotlin.values.types.set.SetRef
