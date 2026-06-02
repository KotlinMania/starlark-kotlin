// port-lint: source src/__derive_refs.rs
@file:Suppress("unused")

package io.github.kotlinmania.starlark.__derive_refs

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

/**
 * __derive_refs allows us to reference other crates in starlark_derive without users needing to be
 *  aware of those dependencies. We make them public here and then can reference them like
 *  `starlark::__derive_refs::foo`.
 */

// Submodules:
// pub mod components    -> __derive_refs.components (Components.kt)
// pub mod invoke_macro_error -> __derive_refs.invoke_macro_error (InvokeMacroError.kt)
// pub mod param_spec    -> __derive_refs.param_spec (ParamSpec.kt)
// pub mod parse_args    -> __derive_refs.parse_args (ParseArgs.kt)
// pub mod sig           -> __derive_refs.sig (Sig.kt)

// Re-exports for vtable registration macro.
// pub use crate::values::layout::avalues::simple::AValueSimple;
// pub use crate::values::layout::vtable::AValueVTable;

/**
 * Re-exports for derive macro references.
 *
 * In Rust, this module re-exports types from other crates so that
 * starlark_derive can reference them without users being aware of
 * those transitive dependencies.
 */

/**
 * Serde re-exports for derive macro use.
 *
 * In Rust: `pub mod serde { pub use serde::{Serialize, Serializer, ser::Error}; }`
 * In Kotlin: these would be available via kotlinx.serialization imports.
 */
internal object DeriveRefsSerde {
    // pub use serde::Serialize;
    // pub use serde::Serializer;
    // pub use serde::ser::Error;
}
