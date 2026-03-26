// port-lint: source src/assert.rs
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

// Utilities to test Starlark code execution, using the [Assert] type and top-level functions.
//
// There are two general approaches. You can either use the functions in this module directly, e.g.:

typealias AssertEquals = io.github.kotlinmania.starlark_kotlin.assert.AssertEquals

// ```
// import io.github.kotlinmania.starlark_kotlin.assert
// assert.eq("1+2", "3")
// ```

typealias AssertDifferent = io.github.kotlinmania.starlark_kotlin.assert.AssertDifferent

// Or create an [Assert] object, which supports the same assertions, but also lets you modify the
// environment in which the tests are run, e.g.:
// ```
// import io.github.kotlinmania.starlark_kotlin.assert.Assert
// import io.github.kotlinmania.starlark_kotlin.syntax.Dialect

typealias AssertLessThan = io.github.kotlinmania.starlark_kotlin.assert.AssertLessThan

// val a = Assert()
// a.dialect(Dialect.Standard) // Use standard Starlark
// a.eq("1+2", "3")
// ```
// The tests in question may be run multiple times, in different modes, to maximise test coverage.
// For example, execution tests are run at different garbage collection settings. Parsing tests are run
// with both Unix and Windows newlines.

// mod assert -> assert/Assert.kt
// mod conformance -> assert/Conformance.kt

// pub use assert::*
typealias Assert = io.github.kotlinmania.starlark_kotlin.assert.Assert
typealias GcStrategy = io.github.kotlinmania.starlark_kotlin.assert.GcStrategy
typealias AssertsStar = io.github.kotlinmania.starlark_kotlin.assert.AssertsStar
