<<<<<<<< HEAD:src/commonTest/kotlin/io/github/kotlinmania/starlark_kotlin/tests/TestsModule.kt
// port-lint: source tests.rs
package io.github.kotlinmania.starlark.tests
========
// port-lint: source src/cast.rs
package io.github.kotlinmania.starlark_kotlin.cast
>>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/Cast.kt

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

<<<<<<<< HEAD:src/commonTest/kotlin/io/github/kotlinmania/starlark_kotlin/tests/TestsModule.kt
/**
 * Test module entrypoint in the upstream Rust crate.
 *
 * In Kotlin, the individual tests are mapped one-to-one from `tests/*.rs` into this package.
 */

========
/** Convert a reference to a usize-like integer (identity hash). */
@Suppress("NOTHING_TO_INLINE")
internal inline fun ptrToUsize(x: Any): Int {
    return x.hashCode() and Int.MAX_VALUE
}

/** Convert a usize-like integer back to a reference via lookup. */
@Suppress("UNCHECKED_CAST")
internal fun <T : Any> usizeToPtr(x: Int, lookup: (Int) -> Any): T {
    require(x != 0) { "Zero is not a valid pointer" }
    require(x > 0) { "Pointer is not aligned" }
    return lookup(x) as T
}

/** Lifetime re-interpretation (identity in Kotlin — no lifetime system). */
@Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")
internal inline fun <T> ptrLifetime(x: T): T {
    return (x as Any) as T
}

/** Transmute between types (unchecked cast in Kotlin). */
@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
internal inline fun <From, To> transmute(value: From): To {
    return value as To
}
>>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/Cast.kt
