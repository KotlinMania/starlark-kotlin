// port-lint: source src/typing.rs
@file:Suppress("unused", "ObjectPropertyName")
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

/// Types required to support the [`typecheck`](crate::syntax::AstModule::typecheck) function.

// pub(crate) mod arc_ty;
internal val arc_ty = "arc_ty"
// pub(crate) mod basic;
internal val basic = "basic"
// pub(crate) mod bindings;
internal val bindings = "bindings"
// pub(crate) mod call_args;
internal val call_args = "call_args"
// pub(crate) mod callable;
internal val callable = "callable"
// pub(crate) mod callable_param;
internal val callable_param = "callable_param"
// pub(crate) mod ctx;
internal val ctx = "ctx"
// pub(crate) mod custom;
internal val custom = "custom"
// pub(crate) mod error;
internal val error = "error"
// pub(crate) mod fill_types_for_lint;
internal val fill_types_for_lint = "fill_types_for_lint"
// pub(crate) mod function;
internal val function = "function"
// pub(crate) mod interface;
internal val interface_ = "interface"
// pub(crate) mod mode;
internal val mode = "mode"
// pub(crate) mod oracle;
internal val oracle = "oracle"
// pub(crate) mod small_arc_vec;
internal val small_arc_vec = "small_arc_vec"
// pub(crate) mod small_arc_vec_or_static;
internal val small_arc_vec_or_static = "small_arc_vec_or_static"
// pub(crate) mod starlark_value;
internal val starlark_value = "starlark_value"
// pub(crate) mod structs;
internal val structs = "structs"
// pub(crate) mod tuple;
internal val tuple = "tuple"
// pub(crate) mod ty;
internal val ty = "ty"
// pub(crate) mod typecheck;
internal val typecheck = "typecheck"
// pub(crate) mod user;
internal val user = "user"

// pub mod macro_support;
internal val macro_support = "macro_support"

// #[cfg(test)]
// mod tests;
internal val tests = "tests"

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
