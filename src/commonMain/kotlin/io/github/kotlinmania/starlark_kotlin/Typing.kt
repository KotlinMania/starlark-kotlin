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

/**
 * Types required to support the [typecheck][io.github.kotlinmania.starlark_kotlin.syntax.AstModule.typecheck] function.
 *
 * This module mirrors `src/typing.rs` which declares submodules and public re-exports.
 *
 * ## Submodules
 *
 * | Rust submodule                  | Kotlin package                                          |
 * |---------------------------------|---------------------------------------------------------|
 * | `arc_ty`                        | `typing.arc_ty`                                         |
 * | `basic`                         | `typing.basic`                                          |
 * | `bindings`                      | `typing.bindings`                                       |
 * | `call_args`                     | `typing.call_args`                                      |
 * | `callable`                      | `typing.callable`                                       |
 * | `callable_param`                | `typing.callable_param`                                 |
 * | `ctx`                           | `typing.ctx`                                            |
 * | `custom`                        | `typing.custom`                                         |
 * | `error`                         | `typing.error`                                          |
 * | `fill_types_for_lint`           | `typing.fill_types_for_lint`                            |
 * | `function`                      | `typing.function`                                       |
 * | `interface`                     | `typing.interface`                                      |
 * | `mode`                          | `typing.mode`                                           |
 * | `oracle`                        | `typing.oracle`                                         |
 * | `small_arc_vec`                 | `typing.small_arc_vec`                                  |
 * | `small_arc_vec_or_static`       | `typing.small_arc_vec_or_static`                        |
 * | `starlark_value`                | `typing.starlark_value`                                 |
 * | `structs`                       | `typing.structs`                                        |
 * | `tuple`                         | `typing.tuple`                                          |
 * | `ty`                            | `typing.ty`                                             |
 * | `typecheck`                     | `typing.typecheck`                                      |
 * | `user`                          | `typing.user`                                           |
 * | `macro_support`                 | `typing.macro_support`                                  |
 *
 * ## Public re-exports (`pub use`)
 *
 * ```rust
 * pub use basic::TyBasic;
 * pub use callable::TyCallable;
 * pub use callable_param::ParamIsRequired;
 * pub use callable_param::ParamSpec;
 * pub use function::TyFunction;
 * pub use interface::Interface;
 * pub use oracle::ctx::TypingOracleCtx;
 * pub use oracle::traits::TypingBinOp;
 * pub use oracle::traits::TypingUnOp;
 * pub use starlark_value::TyStarlarkValue;
 * pub use structs::TyStruct;
 * pub use ty::Approximation;
 * pub use ty::Ty;
 * pub use ty::TypeRenderConfig;
 * pub use typecheck::AstModuleTypecheck;
 * pub use typecheck::TypeMap;
 * pub use user::TyUser;
 * pub use user::TyUserFields;
 * pub use user::TyUserIndex;
 * pub use user::TyUserParams;
 * ```
 */

// Re-exports matching Rust `pub use` declarations.
// In Kotlin these types are accessed via their own packages;
// type aliases are provided here for crate-level convenience.

// pub use basic::TyBasic
typealias TyBasic = io.github.kotlinmania.starlark_kotlin.typing.TyBasic

// pub use callable::TyCallable
typealias TyCallable = io.github.kotlinmania.starlark_kotlin.typing.TyCallable

// pub use function::TyFunction
typealias TyFunction = io.github.kotlinmania.starlark_kotlin.typing.TyFunction

// pub use interface::Interface
typealias TypingInterface = io.github.kotlinmania.starlark_kotlin.typing.Interface

// pub use starlark_value::TyStarlarkValue
typealias TyStarlarkValue = io.github.kotlinmania.starlark_kotlin.typing.TyStarlarkValue

// pub use structs::TyStruct
typealias TyStruct = io.github.kotlinmania.starlark_kotlin.typing.TyStruct

// pub use ty::Ty
typealias Ty = io.github.kotlinmania.starlark_kotlin.typing.Ty

// pub use ty::Approximation
typealias Approximation = io.github.kotlinmania.starlark_kotlin.typing.Approximation

// pub use ty::TypeRenderConfig
typealias TypeRenderConfig = io.github.kotlinmania.starlark_kotlin.typing.TypeRenderConfig

// pub use typecheck::AstModuleTypecheck
typealias AstModuleTypecheck = io.github.kotlinmania.starlark_kotlin.typing.AstModuleTypecheck

// pub use typecheck::TypeMap
typealias TypeMap = io.github.kotlinmania.starlark_kotlin.typing.TypeMap

// pub use user::TyUser
typealias TyUser = io.github.kotlinmania.starlark_kotlin.typing.TyUser

// pub use user::TyUserFields
typealias TyUserFields = io.github.kotlinmania.starlark_kotlin.typing.TyUserFields

// pub use user::TyUserIndex
typealias TyUserIndex = io.github.kotlinmania.starlark_kotlin.typing.TyUserIndex

// pub use user::TyUserParams
typealias TyUserParams = io.github.kotlinmania.starlark_kotlin.typing.TyUserParams
