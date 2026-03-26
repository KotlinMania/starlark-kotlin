// port-lint: source src/assert.rs
@file:Suppress("MatchingDeclarationName")

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

import io.github.kotlinmania.starlark_kotlin.assert.conformance as assertConformance
import io.github.kotlinmania.starlark_kotlin.assert.conformanceExcept as assertConformanceExcept
import io.github.kotlinmania.starlark_kotlin.environment.FrozenModule
import io.github.kotlinmania.starlark_kotlin.starlark_error.Error as StarlarkError
import io.github.kotlinmania.starlark_kotlin.values.owned.OwnedFrozenValue

/**
 * Utilities to test Starlark code execution, using the [Assert] type and top-level functions.
 *
 * There are two general approaches. You can either use the functions in this module directly, e.g.:
 *
 * ```
 * import io.github.kotlinmania.starlark_kotlin.assert
 * assert.eq("1+2", "3")
 * ```
 *
 * Or create an [Assert] object, which supports the same assertions, but also lets you modify the
 * environment in which the tests are run, e.g.:
 *
 * ```
 * import io.github.kotlinmania.starlark_kotlin.assert.Assert
 * import io.github.kotlinmania.starlark_kotlin.syntax.Dialect
 *
 * val a = Assert()
 * a.dialect(Dialect.Standard) // Use standard Starlark
 * a.eq("1+2", "3")
 * ```
 *
 * The tests in question may be run multiple times, in different modes, to maximise test coverage.
 * For example, execution tests are run at different garbage collection settings. Parsing tests are run
 * with both Unix and Windows newlines.
 */

// mod assert -> assert/Assert.kt
// mod conformance -> assert/Conformance.kt

// pub use assert::*
// Re-export the Assert class from the assert subpackage.
typealias Assert = io.github.kotlinmania.starlark_kotlin.assert.Assert

// Re-export top-level convenience functions that mirror Assert companion methods.
// In the Rust source these are free functions re-exported via `pub use assert::*`.

/** See [Assert.eq]. */
fun eq(lhs: String, rhs: String) {
    Assert.eq(lhs, rhs)
}

/** See [Assert.fail]. */
fun fail(program: String, msg: String): StarlarkError {
    return Assert.fail(program, msg)
}

/** See [Assert.fails]. */
fun fails(program: String, msgs: List<String>): StarlarkError {
    return Assert.fails(program, msgs)
}

/** See [Assert.isTrue]. */
fun isTrue(program: String) {
    Assert.isTrue(program)
}

/** See [Assert.isFalse]. */
fun isFalse(program: String) {
    Assert.isFalse(program)
}

/** See [Assert.allTrue]. */
fun allTrue(expressions: String) {
    Assert.allTrue(expressions)
}

/** See [Assert.pass]. */
fun pass(program: String): OwnedFrozenValue {
    return Assert.pass(program)
}

/** See [Assert.passModule]. */
fun passModule(program: String): FrozenModule {
    return Assert.passModule(program)
}

// Re-export conformance extension functions from assert/Conformance.kt
// so they are available when Assert is imported from this package.

/** Run a conformance test, e.g. the Go Starlark tests. */
fun Assert.conformance(code: String) {
    assertConformance(code)
}

/** Run a conformance test, but where some test cases are allowed to fail. */
fun Assert.conformanceExcept(code: String, except: List<String>) {
    assertConformanceExcept(code, except)
}
