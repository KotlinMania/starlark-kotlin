// port-lint: source src/lib.rs
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

/// A Starlark interpreter in Kotlin (ported from starlark-rust).
/// Starlark is a deterministic version of Python, with a specification,
/// used by (amongst others) the Buck and Bazel build systems.
///
/// ## Usage
///
/// To evaluate a simple file:
///
/// ```kotlin
/// val content = """
/// def hello():
///    return "hello"
/// hello() + " world!"
/// """
///
/// // We first parse the content, giving a filename and the Starlark
/// // Dialect we'd like to use (we pick standard).
/// val ast = AstModule.parse("hello_world.star", content, Dialect.Standard)
///
/// // We create a Globals, defining the standard library functions available.
/// val globals = Globals.standard()
///
/// // We create a Module, which stores the global variables for our calculation.
/// val module = Module()
///
/// // We create an evaluator, which controls how evaluation occurs.
/// val eval = Evaluator(module)
///
/// // And finally we evaluate the code using the evaluator.
/// val res = eval.evalModule(ast, globals)
/// assert(res.unpackStr() == "hello world!")
/// ```
///
/// Modules:
///  - Macros.kt (macros)
///  - analysis/ (analysis)
///  - any/ (any)
///  - assert/ (assert)
///  - collections/ (collections)
///  - debug/ (debug)
///  - docs/ (docs)
///  - environment/ (environment)
///  - errors/ (errors)
///  - eval/ (eval)
///  - private/ (private)
///  - read_line/ (read_line)
///  - sealed/ (sealed)
///  - syntax/ (syntax)
///  - typing/ (typing)
///  - cast/ (cast)
///  - hint/ (hint)
///  - stdlib/ (stdlib)
///  - util/ (util)
///  - values/ (values)
///  - wasm/ (wasm)
///  - pagable/ (pagable)
///  - coerce/ (coerce)
///  - tests/ (tests)
///  - __macro_refs/ (__macro_refs)
///  - __derive_refs/ (__derive_refs)

// Re-exports (equivalent to `pub use` in Rust lib.rs)
// pub use starlark_derive::starlark_module -> annotation-based in Kotlin
// pub use starlark_syntax::Error -> io.github.kotlinmania.starlark_kotlin.Error
// pub use starlark_syntax::ErrorKind -> io.github.kotlinmania.starlark_kotlin.ErrorKind
// pub use starlark_syntax::Result -> kotlin.Result
// pub use stdlib::PrintHandler -> io.github.kotlinmania.starlark_kotlin.stdlib.PrintHandler
