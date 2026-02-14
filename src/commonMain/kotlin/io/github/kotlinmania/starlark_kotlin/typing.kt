// port-lint: source src/typing.rs
package io.github.kotlinmania.starlark_kotlin.typing

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

//! Types required to support the [`typecheck`] function.

// pub(crate) mod arc_ty;
// pub(crate) mod basic;
// pub(crate) mod bindings;
// pub(crate) mod call_args;
// pub(crate) mod callable;
// pub(crate) mod callable_param;
// pub(crate) mod ctx;
// pub(crate) mod custom;
// pub(crate) mod error;
// pub(crate) mod fill_types_for_lint;
// pub(crate) mod function;
// pub(crate) mod interface;
// pub(crate) mod mode;
// pub(crate) mod oracle;
// pub(crate) mod small_arc_vec;
// pub(crate) mod small_arc_vec_or_static;
// pub(crate) mod starlark_value;
// pub(crate) mod structs;
// pub(crate) mod tuple;
// pub(crate) mod ty;
// pub(crate) mod typecheck;
// pub(crate) mod user;
// pub mod macro_support;
// Submodules: arc_ty, basic, bindings, call_args, callable, callable_param, ctx, custom,
//   error, fill_types_for_lint, function, interface, mode, oracle, small_arc_vec,
//   small_arc_vec_or_static, starlark_value, structs, tuple, ty, typecheck, user, macro_support

// Re-exports handled by Kotlin package visibility.
// pub use basic::TyBasic;
// pub use callable::TyCallable;
// pub use callable_param::ParamIsRequired;
// pub use callable_param::ParamSpec;
// pub use function::TyFunction;
// pub use interface::Interface;
// pub use oracle::ctx::TypingOracleCtx;
// pub use oracle::traits::TypingBinOp;
// pub use oracle::traits::TypingUnOp;
// pub use starlark_value::TyStarlarkValue;
// pub use structs::TyStruct;
// pub use ty::Approximation;
// pub use ty::Ty;
// pub use ty::TypeRenderConfig;
// pub use typecheck::AstModuleTypecheck;
// pub use typecheck::TypeMap;
// pub use user::TyUser;
// pub use user::TyUserFields;
// pub use user::TyUserIndex;
// pub use user::TyUserParams;
