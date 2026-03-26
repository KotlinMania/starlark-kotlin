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

/** Convert a reference to a usize-like integer identity. */
@Suppress("NOTHING_TO_INLINE")
internal inline fun ptrToUsize(x: Any): Int {
    val ref = x as Any
    val ptr = ref.hashCode()
    val usize = ptr and Int.MAX_VALUE
    return usize
}

/**
 * Undefined behaviour if the argument is zero, or does not satisfy the alignment
 * of type `T`.
 */
@Suppress("UNCHECKED_CAST")
internal fun <T : Any> usizeToPtr(x: Int, lookup: (Int) -> Any): T {
    require(x != 0) { "Zero is not a valid pointer" }
    require(x > 0) { "Pointer is not aligned" }
    return lookup(x) as T
}

/** Re-interpret the lifetime of a reference (identity in Kotlin). */
@Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")
internal inline fun <T> ptrLifetime(x: T): T {
    return (x as Any) as T
}

/**
 * `transmute!(from-type, to-type, value)` will do a [transmute][kotlin.Any],
 * but the original and result types must be specified.
 */
@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
internal inline fun <From, To> transmute(value: From): To {
    return value as To
}
