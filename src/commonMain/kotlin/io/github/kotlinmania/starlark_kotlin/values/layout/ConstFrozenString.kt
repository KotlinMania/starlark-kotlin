// port-lint: source src/values/layout/const_frozen_string.rs
package io.github.kotlinmania.starlark_kotlin.values.layout

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

import io.github.kotlinmania.starlark_kotlin.values.FrozenStringValue

/// Create a [`FrozenStringValue`].
// #[macro_export]
// macro_rules! const_frozen_string { ... }
// Kotlin: No macro system. Translated as a function that creates frozen string values.
// In Rust, this macro creates compile-time static frozen strings using `StarlarkStrNRepr`.
// In Kotlin, we simply intern/cache the string.

// Cache for frequently used frozen strings.
private val frozenStringCache = HashMap<String, FrozenStringValue>()

/**
 * Create a [FrozenStringValue] from a string literal.
 *
 * This is the Kotlin equivalent of Rust's `const_frozen_string!` macro.
 * In Rust, this creates compile-time static frozen string values.
 * In Kotlin, we cache the values for identity-based comparison.
 */
fun constFrozenString(s: String): FrozenStringValue {
    return frozenStringCache.getOrPut(s) {
        constantString(s) ?: FrozenStringValue.fromString(s)
    }
}

/**
 * Try to get a pre-allocated constant string value.
 * Returns null if the string is not a known constant.
 */
internal fun constantString(s: String): FrozenStringValue? {
    // Rust: handles empty string and single-char strings as pre-allocated constants.
    return when {
        s.isEmpty() -> FrozenStringValue.emptyString()
        s.length == 1 -> FrozenStringValue.singleChar(s[0])
        else -> null
    }
}

// #[cfg(test)] mod tests
// Tests are in commonTest, not here.
