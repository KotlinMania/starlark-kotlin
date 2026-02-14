// port-lint: source src/eval/bc/compiler.rs
package io.github.kotlinmania.starlark_kotlin.eval.bc.compiler

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

/// Compile module or function to bytecode.
///
/// Submodules (Kotlin files in this package):
/// - assign: Assignment compilation
/// - assign_modify: Augmented assignment compilation
/// - call: Call expression compilation
/// - compr: Comprehension compilation
/// - def: Function definition compilation
/// - expr: Expression compilation
/// - if_compiler: If statement compilation
/// - stmt: Statement compilation
