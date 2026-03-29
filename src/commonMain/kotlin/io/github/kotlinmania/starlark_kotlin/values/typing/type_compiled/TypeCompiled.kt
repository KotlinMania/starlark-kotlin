// port-lint: source src/values/typing/type_compiled.rs
package io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled

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
 * Compiled type expressions for efficient runtime type checking.
 *
 * Submodules:
 * - [alloc][io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.Alloc] - Allocation helpers for type matchers
 * - [compiled][io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.Compiled] - Compiled type representation
 * - [factory][io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.Factory] - Type matcher factory
 * - [globals][io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.Globals] - Global type matchers
 * - [matcher][io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.Matcher] - Type matcher interface
 * - [matchers][io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.Matchers] - Concrete type matcher implementations
 * - [tests][io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.Tests] - Tests for compiled types
 * - [typeMatcherFactory][io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.TypeMatcherFactory] - Factory interface
 */
