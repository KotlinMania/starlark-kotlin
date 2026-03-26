// port-lint: source src/typing.rs
package io.github.kotlinmania.starlark_kotlin

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

// Notes:
//   We deal with list.append/list.extend/list.insert, which mutate their list argument
//   We ignore dict.setdefault/dict.update, as these are pretty complex functions
//   We consider "non-sensical" operations like list.remove and == to have implied types
//   that make them meaningful even if they don't fail when doing something silly

/**
 * Types required to support the [typecheck][io.github.kotlinmania.starlark_kotlin.syntax.AstModule] function.
 *
 * Submodules:
 * - arcTy - arc-wrapped type references
 * - basic - [TyBasic][io.github.kotlinmania.starlark_kotlin.typing.TyBasic]
 * - bindings - type bindings
 * - callArgs - call argument types
 * - callable - [TyCallable][io.github.kotlinmania.starlark_kotlin.typing.TyCallable]
 * - callableParam - [ParamSpec][io.github.kotlinmania.starlark_kotlin.typing.ParamSpec]
 * - ctx - typing context
 * - custom - custom type support
 * - error - typing errors
 * - fillTypesForLint - lint type filling
 * - function - [TyFunction][io.github.kotlinmania.starlark_kotlin.typing.TyFunction]
 * - interface - [Interface][io.github.kotlinmania.starlark_kotlin.typing.Interface]
 * - mode - typing mode
 * - oracle - typing oracle
 * - smallArcVec - small arc vector
 * - smallArcVecOrStatic - small arc vector or static
 * - starlarkValue - [TyStarlarkValue][io.github.kotlinmania.starlark_kotlin.typing.TyStarlarkValue]
 * - structs - [TyStruct][io.github.kotlinmania.starlark_kotlin.typing.TyStruct]
 * - tuple - tuple typing
 * - ty - [Ty][io.github.kotlinmania.starlark_kotlin.typing.Ty]
 * - typecheck - [AstModuleTypecheck][io.github.kotlinmania.starlark_kotlin.typing.AstModuleTypecheck]
 * - user - [TyUser][io.github.kotlinmania.starlark_kotlin.typing.TyUser]
 * - macroSupport - macro support utilities
 */

// Re-exports (mirrors Rust's pub use declarations)
internal typealias TyBasicExport = io.github.kotlinmania.starlark_kotlin.typing.TyBasic
internal typealias TyCallableExport = io.github.kotlinmania.starlark_kotlin.typing.TyCallable
internal typealias ParamSpecExport = io.github.kotlinmania.starlark_kotlin.typing.ParamSpec
internal typealias TyFunctionExport = io.github.kotlinmania.starlark_kotlin.typing.TyFunction
internal typealias InterfaceExport = io.github.kotlinmania.starlark_kotlin.typing.Interface
internal typealias TypingBinOpExport = io.github.kotlinmania.starlark_kotlin.typing.TypingBinOp
internal typealias TypingUnOpExport = io.github.kotlinmania.starlark_kotlin.typing.TypingUnOp
internal typealias TyStarlarkValueExport = io.github.kotlinmania.starlark_kotlin.typing.TyStarlarkValue
internal typealias TyStructExport = io.github.kotlinmania.starlark_kotlin.typing.TyStruct
internal typealias TyExport = io.github.kotlinmania.starlark_kotlin.typing.Ty
internal typealias TyUserExport = io.github.kotlinmania.starlark_kotlin.typing.TyUser
internal typealias TyUserFieldsExport = io.github.kotlinmania.starlark_kotlin.typing.TyUserFields
internal typealias TyUserIndexExport = io.github.kotlinmania.starlark_kotlin.typing.TyUserIndex
internal typealias TyUserParamsExport = io.github.kotlinmania.starlark_kotlin.typing.TyUserParams
