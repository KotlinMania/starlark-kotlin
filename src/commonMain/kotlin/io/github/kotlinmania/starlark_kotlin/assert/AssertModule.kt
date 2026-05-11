<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/assert/AssertModule.kt
// port-lint: source assert.rs
package io.github.kotlinmania.starlark.assert
=======
// port-lint: source src/assert.rs
package io.github.kotlinmania.starlark_kotlin.assert
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/assert/AssertModule.kt

/*
 * Copyright 2019 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/assert/AssertModule.kt
 * you may not import this file except in compliance with the License.
=======
 * you may not use this file except in compliance with the License.
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/assert/AssertModule.kt
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
 * Utilities to test Starlark code execution, using the [Assert] type and top-level functions.
 *
<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/assert/AssertModule.kt
 * There are two general approaches. You can either use the functions in this module directly,
 * e.g.:
 *
 * ```
 * import io.github.kotlinmania.starlark.assert.eq
 *
 * eq("1+2", "3")
=======
 * There are two general approaches. You can either use the functions in this module directly, e.g.:
 *
 * ```kotlin
 * import io.github.kotlinmania.starlark_kotlin.assert
 * assert.eq("1+2", "3")
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/assert/AssertModule.kt
 * ```
 *
 * Or create an [Assert] object, which supports the same assertions, but also lets you modify the
 * environment in which the tests are run, e.g.:
 *
<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/assert/AssertModule.kt
 * ```
 * import io.github.kotlinmania.starlark.assert.Assert
 * import io.github.kotlinmania.starlark.syntax.dialect.Dialect
=======
 * ```kotlin
 * import io.github.kotlinmania.starlark_kotlin.assert.Assert
 * import io.github.kotlinmania.starlark_kotlin.syntax.dialect.Dialect
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/assert/AssertModule.kt
 *
 * val a = Assert()
 * a.dialect(Dialect.Standard) // Use standard Starlark
 * a.eq("1+2", "3")
 * ```
 *
 * The tests in question may be run multiple times, in different modes, to maximise test coverage.
<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/assert/AssertModule.kt
 * For example, execution tests are run at different garbage collection settings. Parsing tests are
 * run with both Unix and Windows newlines.
 */

=======
 * For example, execution tests are run at different garbage collection settings. Parsing tests are run
 * with both Unix and Windows newlines.
 */

// Rust `mod assert; mod conformance; pub use assert::*;`
//
// Kotlin does not need module declarations: `Assert.kt` and `Conformance.kt` live in the same
// package and their public declarations are available as `io.github.kotlinmania.starlark_kotlin.assert.*`.

private val assert: () -> Assert = { Assert() }
private val conformance = Assert::conformance
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/assert/AssertModule.kt
