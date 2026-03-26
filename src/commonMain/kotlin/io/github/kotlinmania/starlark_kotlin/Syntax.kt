// port-lint: source src/syntax.rs
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

/**
 * Public API for parser.
 *
 * This module mirrors `src/syntax.rs` which re-exports parser types from `starlark_syntax`.
 *
 * ## Public re-exports (`pub use`)
 *
 * ```rust
 * pub use starlark_syntax::dialect::Dialect;
 * pub use starlark_syntax::dialect::DialectTypes;
 * pub use starlark_syntax::syntax::AstLoad;
 * pub use starlark_syntax::syntax::AstModule;
 * pub use starlark_syntax::syntax::ast;
 * ```
 */

// Re-exports matching Rust `pub use` declarations.
// In Kotlin these types live in their own packages.

// pub use starlark_syntax::dialect::Dialect
typealias Dialect = io.github.kotlinmania.starlark_kotlin.syntax.Dialect

// pub use starlark_syntax::dialect::DialectTypes
typealias DialectTypes = io.github.kotlinmania.starlark_kotlin.syntax.DialectTypes

// pub use starlark_syntax::syntax::AstLoad
typealias AstLoad = io.github.kotlinmania.starlark_kotlin.syntax.AstLoad

// pub use starlark_syntax::syntax::AstModule
typealias AstModule = io.github.kotlinmania.starlark_kotlin.syntax.AstModule
