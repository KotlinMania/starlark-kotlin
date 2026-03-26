// port-lint: source src/errors.rs
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
 * Error types used by Starlark.
 *
 * This module mirrors `src/errors.rs` which re-exports public error types
 * and declares internal submodules.
 *
 * ## Public re-exports (`pub use`)
 *
 * ```rust
 * pub use starlark_syntax::frame::Frame;
 * pub use crate::analysis::EvalMessage;
 * pub use crate::analysis::EvalSeverity;
 * pub use crate::analysis::Lint;
 * ```
 *
 * ## Submodules
 *
 * | Rust submodule    | Kotlin package                   |
 * |-------------------|----------------------------------|
 * | `did_you_mean`    | `errors.did_you_mean`            |
 */

// pub use starlark_syntax::frame::Frame
typealias Frame = io.github.kotlinmania.starlark_kotlin.syntax.frame.Frame

// pub use crate::analysis::EvalMessage
typealias EvalMessage = io.github.kotlinmania.starlark_kotlin.analysis.EvalMessage

// pub use crate::analysis::EvalSeverity
typealias EvalSeverity = io.github.kotlinmania.starlark_kotlin.analysis.EvalSeverity

// pub use crate::analysis::Lint
typealias Lint = io.github.kotlinmania.starlark_kotlin.analysis.Lint
