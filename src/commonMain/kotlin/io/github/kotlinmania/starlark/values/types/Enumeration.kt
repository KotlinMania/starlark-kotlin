// port-lint: source src/values/types/enumeration.rs
package io.github.kotlinmania.starlark.values.types

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
 * Fixed set enumerations, with runtime checking of validity.
 *
 * Calling `enum()` produces an [EnumType]. Calling the [EnumType] creates an [EnumValue].
 *
 * The implementation ensures that each value of the enumeration is only stored once,
 * so they may also provide (modest) memory savings. Created in starlark with the
 * `enum` function:
 *
 * ```
 * Colors = enum("Red", "Green", "Blue")
 * val = Colors("Red")
 * assert_eq(val.value, "Red")
 * assert_eq(val.index, 0)
 * assert_eq(Colors[0], val)
 * assert_eq(Colors.type, "Colors")
 * assert_eq([v.value for v in Colors], ["Red", "Green", "Blue"])
 * ```
 *
 * Submodules:
 * - [enumType][io.github.kotlinmania.starlark.values.types.enumeration.EnumType] - enum type definition
 * - [globals][io.github.kotlinmania.starlark.values.types.enumeration.Globals] - global enum functions
 * - [matcher][io.github.kotlinmania.starlark.values.types.enumeration.Matcher] - enum matcher
 * - [tyEnumType][io.github.kotlinmania.starlark.values.types.enumeration.TyEnumType] - enum typing
 * - [value][io.github.kotlinmania.starlark.values.types.enumeration.Value] - enum value
 */

// Re-exports (mirrors Rust's pub use declarations) are done via direct imports at call sites.
