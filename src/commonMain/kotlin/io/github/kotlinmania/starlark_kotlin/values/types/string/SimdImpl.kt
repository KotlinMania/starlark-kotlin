// port-lint: source src/values/types/string/simd.rs
package io.github.kotlinmania.starlark_kotlin.values.types.string

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
 * Platform-specific SIMD implementation.
 *
 * Note: The Rust implementation has conditional SIMD support for x86/x86_64 with SSE2.
 * In Kotlin Multiplatform, we currently don't have portable SIMD support across all platforms,
 * so this implementation always falls back to the non-SIMD path.
 *
 * This matches the Rust code's behavior on platforms without SSE2 support and maintains
 * semantic parity while allowing for future platform-specific SIMD optimizations.
 */
internal actual fun <R> SwitchHaveSimd<R>.switchImpl(): R {
    // Currently, Kotlin Multiplatform doesn't have stable SIMD support across all platforms.
    // The Rust code also falls back to non-SIMD on non-SSE2 platforms.
    // This implementation always uses the non-SIMD path, maintaining correctness
    // while allowing for future platform-specific optimizations via expect/actual.
    return noSimd()
}
