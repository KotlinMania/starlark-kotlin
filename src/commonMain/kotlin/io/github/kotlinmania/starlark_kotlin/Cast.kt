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
//     x as *const T as *const () as usize
@Suppress("NOTHING_TO_INLINE")
internal inline fun ptrToUsize(x: Any): Int {
    // Kotlin: No raw pointer casts. Use hashCode as usize-like value.
    return x.hashCode() and Int.MAX_VALUE
}

/// Undefined behaviour if the argument is zero, or does not satisfy the alignment
/// of type `T`.
// #[inline(always)]
// pub(crate) unsafe fn usize_to_ptr<'a, T>(x: usize) -> &'a T
@Suppress("UNCHECKED_CAST")
internal fun <T : Any> usizeToPtr(x: Int, lookup: (Int) -> Any): T {
    // debug_assert!(x != 0, "Zero is not a valid pointer");
    require(x != 0) { "Zero is not a valid pointer" }
    // debug_assert!(x.is_multiple_of(std::mem::align_of::<T>()), "Pointer is not aligned");
    require(x > 0) { "Pointer is not aligned" }
    // unsafe { &*(x as *const T) }
    return lookup(x) as T
}

// #[inline(always)]
// pub(crate) unsafe fn ptr_lifetime<'a, 'b, T: ?Sized>(x: &'a T) -> &'b T
//     unsafe { &*(x as *const T) }
@Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")
internal inline fun <T> ptrLifetime(x: T): T {
    // Kotlin: Lifetime re-interpretation is identity - no lifetime system.
    return (x as Any) as T
}

/// `transmute!(from-type, to-type, value)` will do a [`transmute`](std::mem::transmute),
/// but the original and result types must be specified.
// macro_rules! transmute {
//     ($from:ty, $to:ty, $e:expr) => {
//         std::mem::transmute::<$from, $to>($e)
//     };
// }
@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
internal inline fun <From, To> transmute(value: From): To {
    return value as To
}

// pub(crate) use transmute;
