// port-lint: source src/docs.rs
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
 * Types supporting documentation for code written in or for Starlark.
 *
 * Submodules (Kotlin packages under docs):
 * - pub mod code      -> docs.code
 * - pub mod markdown  -> docs.markdown
 * - pub mod multipage -> docs.multipage
 * - mod parse         -> docs.parse (private)
 * - mod tests         -> docs.tests (test-only)
 *
 * Re-exports:
 * - pub use parse::DocStringKind
 * - pub use crate::eval::runtime::params::display::FmtParam
 *
 * Code (DocString, DocModule, DocFunction, DocParams, DocParam, DocReturn,
 * DocProperty, DocMember, DocType, DocItem) ported in docs/Docs.kt.
 */
