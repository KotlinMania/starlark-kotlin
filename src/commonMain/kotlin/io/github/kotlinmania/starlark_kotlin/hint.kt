// port-lint: source src/hint.rs
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
 * Hint to the compiler that this branch is likely to be taken.
 *
 * This is a port of Rust's `likely` intrinsic. In Rust nightly builds,
 * this uses the intrinsic for optimization. In stable Rust and Kotlin,
 * it's a no-op that returns the value unchanged.
 *
 * The Kotlin compiler may optimize this in the future, but currently
 * this function exists for semantic parity with the Rust implementation.
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun likely(b: Boolean): Boolean = b

/**
 * Hint to the compiler that this branch is unlikely to be taken.
 *
 * This is a port of Rust's `unlikely` intrinsic. In Rust nightly builds,
 * this uses the intrinsic for optimization. In stable Rust and Kotlin,
 * it's a no-op that returns the value unchanged.
 *
 * The Kotlin compiler may optimize this in the future, but currently
 * this function exists for semantic parity with the Rust implementation.
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun unlikely(b: Boolean): Boolean = b
