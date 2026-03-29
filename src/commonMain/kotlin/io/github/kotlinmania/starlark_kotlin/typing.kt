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
//   We consider "non-sensicle" operations like list.remove and == to have implied types that make them meaningful
//       even if they don't fail when doing something silly

/**
 * Types required to support the [typecheck][io.github.kotlinmania.starlark_kotlin.typing.Typecheck] function.
 *
 * Re-exports:
 * - [TyBasic][io.github.kotlinmania.starlark_kotlin.typing.Basic]
 * - [TyCallable][io.github.kotlinmania.starlark_kotlin.typing.Callable]
 * - [ParamIsRequired][io.github.kotlinmania.starlark_kotlin.typing.CallableParam]
 * - [ParamSpec][io.github.kotlinmania.starlark_kotlin.typing.CallableParam]
 * - [TyFunction][io.github.kotlinmania.starlark_kotlin.typing.Function]
 * - [Interface][io.github.kotlinmania.starlark_kotlin.typing.Interface]
 * - [TypingOracleCtx][io.github.kotlinmania.starlark_kotlin.typing.Oracle]
 * - [TypingBinOp][io.github.kotlinmania.starlark_kotlin.typing.Oracle]
 * - [TypingUnOp][io.github.kotlinmania.starlark_kotlin.typing.Oracle]
 * - [TyStarlarkValue][io.github.kotlinmania.starlark_kotlin.typing.StarlarkValue]
 * - [TyStruct][io.github.kotlinmania.starlark_kotlin.typing.Structs]
 * - [Approximation][io.github.kotlinmania.starlark_kotlin.typing.Ty]
 * - [Ty][io.github.kotlinmania.starlark_kotlin.typing.Ty]
 * - [TypeRenderConfig][io.github.kotlinmania.starlark_kotlin.typing.Ty]
 * - [AstModuleTypecheck][io.github.kotlinmania.starlark_kotlin.typing.Typecheck]
 * - [TypeMap][io.github.kotlinmania.starlark_kotlin.typing.Typecheck]
 * - [TyUser][io.github.kotlinmania.starlark_kotlin.typing.User]
 * - [TyUserFields][io.github.kotlinmania.starlark_kotlin.typing.User]
 * - [TyUserIndex][io.github.kotlinmania.starlark_kotlin.typing.User]
 * - [TyUserParams][io.github.kotlinmania.starlark_kotlin.typing.User]
 *
 * Submodules:
 * - [arcTy][io.github.kotlinmania.starlark_kotlin.typing.ArcTy] - arc-wrapped type
 * - [basic][io.github.kotlinmania.starlark_kotlin.typing.Basic] - basic types
 * - [bindings][io.github.kotlinmania.starlark_kotlin.typing.Bindings] - type bindings
 * - [callArgs][io.github.kotlinmania.starlark_kotlin.typing.CallArgs] - call argument types
 * - [callable][io.github.kotlinmania.starlark_kotlin.typing.Callable] - callable types
 * - [callableParam][io.github.kotlinmania.starlark_kotlin.typing.CallableParam] - callable parameters
 * - [ctx][io.github.kotlinmania.starlark_kotlin.typing.Ctx] - typing context
 * - [custom][io.github.kotlinmania.starlark_kotlin.typing.Custom] - custom types
 * - [error][io.github.kotlinmania.starlark_kotlin.typing.Error] - typing errors
 * - [fillTypesForLint][io.github.kotlinmania.starlark_kotlin.typing.FillTypesForLint] - lint type filling
 * - [function][io.github.kotlinmania.starlark_kotlin.typing.Function] - function types
 * - [interface_][io.github.kotlinmania.starlark_kotlin.typing.Interface] - typing interface
 * - [mode][io.github.kotlinmania.starlark_kotlin.typing.Mode] - typing mode
 * - [oracle][io.github.kotlinmania.starlark_kotlin.typing.Oracle] - typing oracle
 * - [smallArcVec][io.github.kotlinmania.starlark_kotlin.typing.SmallArcVec] - small arc vector
 * - [smallArcVecOrStatic][io.github.kotlinmania.starlark_kotlin.typing.SmallArcVecOrStatic] - small arc vector or static
 * - [starlarkValue][io.github.kotlinmania.starlark_kotlin.typing.StarlarkValue] - starlark value typing
 * - [structs][io.github.kotlinmania.starlark_kotlin.typing.Structs] - struct types
 * - [tuple][io.github.kotlinmania.starlark_kotlin.typing.Tuple] - tuple types
 * - [ty][io.github.kotlinmania.starlark_kotlin.typing.Ty] - core type representation
 * - [typecheck][io.github.kotlinmania.starlark_kotlin.typing.Typecheck] - type checking
 * - [user][io.github.kotlinmania.starlark_kotlin.typing.User] - user-defined types
 * - [macroSupport][io.github.kotlinmania.starlark_kotlin.typing.MacroSupport] - macro support utilities
 */
