// port-lint: source src/values/typing.rs
@file:Suppress("unused", "ObjectPropertyName")
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

/// Typechecker-related types.

// pub(crate) mod any;
// internal val any = "any" // conflicts with Types.kt declaration
// pub(crate) mod callable;
internal val callable = "callable"
// pub(crate) mod globals;
internal val globals = "globals"
// pub(crate) mod iter;
internal val iter = "iter"
// pub mod macro_refs;
internal val macro_refs = "macro_refs"
// pub(crate) mod never;
internal val never = "never"
// pub(crate) mod ty;
internal val ty = "ty"
// pub(crate) mod type_compiled;
internal val type_compiled = "type_compiled"
// pub(crate) mod type_type;
internal val type_type = "type_type"

// pub use crate::values::types::type_instance_id::TypeInstanceId;
// pub use crate::values::typing::callable::FrozenStarlarkCallable;
// pub use crate::values::typing::callable::StarlarkCallable;
// pub use crate::values::typing::callable::StarlarkCallableChecked;
// pub use crate::values::typing::callable::param::StarlarkCallableParamAny;
// pub use crate::values::typing::callable::param::StarlarkCallableParamSpec;
// pub use crate::values::typing::iter::StarlarkIter;
// pub use crate::values::typing::never::StarlarkNever;
// pub use crate::values::typing::type_compiled::compiled::TypeCompiled;
// pub use crate::values::typing::type_compiled::compiled::TypeCompiledImplAsStarlarkValue;
// pub use crate::values::typing::type_compiled::matcher::TypeMatcher;
// pub use crate::values::typing::type_compiled::matcher::TypeMatcherRegistered;
// pub use crate::values::typing::type_compiled::type_matcher_factory::TypeMatcherFactory;
// pub use crate::values::typing::type_type::TypeType;
