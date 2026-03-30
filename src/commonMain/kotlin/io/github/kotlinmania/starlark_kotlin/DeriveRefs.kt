// port-lint: source src/__derive_refs.rs
@file:Suppress("unused", "ObjectPropertyName")
package io.github.kotlinmania.starlark_kotlin

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

// #![doc(hidden)]

/// __derive_refs allows us to reference other crates in starlark_derive without users needing to be
///  aware of those dependencies. We make them public here and then can reference them like
///  `starlark::__derive_refs::foo`.

// pub mod serde { ... }
internal val serde = "serde"
// pub use inventory;
internal val inventory = "inventory"
// pub mod components;
internal val components = "components"
// pub mod invoke_macro_error;
internal val invoke_macro_error = "invoke_macro_error"
// pub mod param_spec;
internal val param_spec = "param_spec"
// pub mod parse_args;
internal val parse_args = "parse_args"
// pub mod sig;
internal val sig = "sig"

// pub use crate::values::layout::avalues::simple::AValueSimple;
// pub use crate::values::layout::vtable::AValueVTable;
