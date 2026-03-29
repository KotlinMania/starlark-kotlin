// port-lint: source src/eval.rs
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
 * Evaluate some code, typically done by creating an
 * [Evaluator][io.github.kotlinmania.starlark_kotlin.eval.runtime.Evaluator], then calling
 * [evalModule][io.github.kotlinmania.starlark_kotlin.eval.evalModule].
 *
 * Submodules (Kotlin packages under eval):
 * - pub(crate) mod bc       -> eval.bc
 * - pub(crate) mod compiler -> eval.compiler
 * - mod params              -> eval.params (private)
 * - pub(crate) mod runtime  -> eval.runtime
 * - pub(crate) mod soft_error -> eval.soft_error
 *
 * Re-exports:
 * - pub use runtime::arguments::Arguments
 * - pub use runtime::before_stmt::BeforeStmtFuncDyn
 * - pub use runtime::evaluator::Evaluator
 * - pub use runtime::file_loader::FileLoader
 * - pub use runtime::file_loader::ReturnFileLoader
 * - pub use runtime::params::parser::ParametersParser
 * - pub use runtime::params::spec::ParametersSpec
 * - pub use runtime::params::spec::ParametersSpecParam
 * - pub use runtime::profile::data::ProfileData
 * - pub use runtime::profile::mode::ProfileMode
 * - pub use soft_error::SoftErrorHandler
 * - pub use starlark_syntax::call_stack::CallStack
 *
 * Code (Evaluator extension functions) ported in eval/Eval.kt.
 */
