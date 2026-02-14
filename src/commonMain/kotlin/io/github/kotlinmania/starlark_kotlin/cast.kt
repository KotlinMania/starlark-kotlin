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

// #[inline(always)]
// pub(crate) fn ptr_to_usize<T: ?Sized>(x: &T) -> usize
/** Convert a reference to its identity (analogous to pointer-to-usize in Rust). */
internal fun ptrToUsize(x: Any): Int {
    return x.hashCode()
}

// pub(crate) unsafe fn usize_to_ptr<'a, T>(x: usize) -> &'a T
// Kotlin: No equivalent — Rust raw pointer cast from integer.
// Not transliterable; Kotlin does not support pointer arithmetic.

// pub(crate) unsafe fn ptr_lifetime<'a, 'b, T: ?Sized>(x: &'a T) -> &'b T
// Kotlin: No lifetime system; references are managed by GC.
// Not transliterable; Kotlin has no lifetime annotations.

// macro_rules! transmute { ... }
// pub(crate) use transmute;
/** Unsafe reinterpret cast. In Kotlin, use `as` or `@Suppress("UNCHECKED_CAST")`. */
@Suppress("UNCHECKED_CAST")
internal fun <From, To> transmute(value: From): To {
    return value as To
}
