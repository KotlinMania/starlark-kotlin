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

//! Typechecker-related types.

// Rust module declarations — in Kotlin these are packages under values/typing/
// pub(crate) mod any;
// pub(crate) mod callable;
// pub(crate) mod globals;
// pub(crate) mod iter;
// pub mod macro_refs;
// pub(crate) mod never;
// pub(crate) mod ty;
// pub(crate) mod type_compiled;
// pub(crate) mod type_type;

// Re-exports — in Kotlin these are available via their respective packages:
// pub use TypeInstanceId           -> values.types.type_instance_id.TypeInstanceId
// pub use FrozenStarlarkCallable   -> values.typing.callable.FrozenStarlarkCallable
// pub use StarlarkCallable         -> values.typing.callable.StarlarkCallable
// pub use StarlarkCallableChecked  -> values.typing.callable.StarlarkCallableChecked
// pub use StarlarkCallableParamAny -> values.typing.callable.param.StarlarkCallableParamAny
// pub use StarlarkCallableParamSpec -> values.typing.callable.param.StarlarkCallableParamSpec
// pub use StarlarkIter             -> values.typing.iter.StarlarkIter
// pub use StarlarkNever            -> values.typing.never.StarlarkNever
// pub use TypeCompiled             -> values.typing.type_compiled.compiled.TypeCompiled
// pub use TypeCompiledImplAsStarlarkValue -> values.typing.type_compiled.compiled.TypeCompiledImplAsStarlarkValue
// pub use TypeMatcher              -> values.typing.type_compiled.matcher.TypeMatcher
// pub use TypeMatcherRegistered    -> values.typing.type_compiled.matcher.TypeMatcherRegistered
// pub use TypeMatcherFactory       -> values.typing.type_compiled.type_matcher_factory.TypeMatcherFactory
// pub use TypeType                 -> values.typing.type_type.TypeType
