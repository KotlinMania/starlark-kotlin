// port-lint: source src/cast.rs
package io.github.kotlinmania.starlark_kotlin.cast

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

// Rust: fn ptr_to_usize<T: ?Sized>(x: &T) -> usize
/** Convert a reference to its identity hash (analogous to pointer-to-usize in Rust). */
internal fun ptrToUsize(x: Any): Int {
    return x.hashCode()
}

// Rust: unsafe fn usize_to_ptr — not transliterable (raw pointer cast from integer)
// Rust: unsafe fn ptr_lifetime — not transliterable (lifetime re-borrowing)

// Rust: macro_rules! transmute
/**
 * Unsafe reinterpret cast, analogous to `transmute!(from, to, value)` in Rust.
 *
 * In Kotlin, uses an unchecked cast.
 */
@Suppress("UNCHECKED_CAST")
internal fun <From, To> transmute(value: From): To {
    return value as To
}
