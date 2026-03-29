// port-lint: source src/values/typing.rs
package io.github.kotlinmania.starlark_kotlin.values

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
 * Typechecker-related types.
 *
 * Submodules:
 * - [any][io.github.kotlinmania.starlark_kotlin.values.typing.Any] - any type
 * - [callable][io.github.kotlinmania.starlark_kotlin.values.typing.Callable] - callable types
 * - [globals][io.github.kotlinmania.starlark_kotlin.values.typing.Globals] - typing globals
 * - [iter][io.github.kotlinmania.starlark_kotlin.values.typing.Iter] - iterator types
 * - [macroRefs][io.github.kotlinmania.starlark_kotlin.values.typing.MacroRefs] - macro references
 * - [never][io.github.kotlinmania.starlark_kotlin.values.typing.Never] - never type
 * - [ty][io.github.kotlinmania.starlark_kotlin.values.typing.Ty] - type representation
 * - [typeCompiled][io.github.kotlinmania.starlark_kotlin.values.typing.TypeCompiled] - compiled types
 * - [typeType][io.github.kotlinmania.starlark_kotlin.values.typing.TypeType] - type type
 */

// Re-exports (mirrors Rust's pub use declarations)
internal typealias TypeInstanceIdExport = io.github.kotlinmania.starlark_kotlin.values.types.TypeInstanceId
internal typealias StarlarkCallableExport<P, R> = io.github.kotlinmania.starlark_kotlin.values.typing.StarlarkCallable<P, R>
internal typealias StarlarkIterExport<T> = io.github.kotlinmania.starlark_kotlin.values.typing.StarlarkIter<T>
internal typealias StarlarkNeverExport = io.github.kotlinmania.starlark_kotlin.values.typing.StarlarkNever
internal typealias TypeCompiledExport = io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.TypeCompiled
internal typealias TypeMatcherExport = io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.TypeMatcher
internal typealias TypeMatcherFactoryExport = io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.TypeMatcherFactory
internal typealias TypeTypeExport = io.github.kotlinmania.starlark_kotlin.values.typing.TypeType
