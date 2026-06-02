// port-lint: source src/slice_vec_ext.rs
@file:Suppress("unused")

package io.github.kotlinmania.starlark.syntax.slice_vec_ext

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
 * Optimised collect iterator into `List`, which might be a [Result].
 *
 * If we do a standard `.map(f)` on the iterator it will never have a good size hint,
 * as the lower bound will always be zero, so might reallocate several times.
 * We know the `List` will either be thrown away, or exactly `len`, so aim if we do allocate,
 * make sure it is at `len`. However, if the first element throws an error, we don't need
 * to allocate at all, so special case that.
 */
private fun <T> collectResult(items: List<Result<T>>): Result<List<T>> {
    if (items.isEmpty()) return Result.success(emptyList())

    val first = items[0]
    if (first.isFailure) return Result.failure(first.exceptionOrNull()!!)

    val res = ArrayList<T>(items.size)
    res.add(first.getOrThrow())
    for (i in 1 until items.size) {
        val x = items[i]
        if (x.isFailure) return Result.failure(x.exceptionOrNull()!!)
        res.add(x.getOrThrow())
    }
    return Result.success(res)
}

/**
 * Extension traits on slices/[List].
 *
 * Kotlin doesn't have slice traits, so these are provided as extension functions.
 */
fun <T, B> List<T>.mapExt(f: (T) -> B): List<B> {
    return this.map(f)
}

/**
 * A shorthand for `iter().map(f).collect::<Result<Vec<_>, _>>()`.
 */
fun <T, B> List<T>.tryMap(f: (T) -> Result<B>): Result<List<B>> {
    val mapped = ArrayList<Result<B>>(this.size)
    for (x in this) {
        mapped.add(f(x))
    }
    return collectResult(mapped)
}

/**
 * Extension traits on [List] (standing in for Rust's `Vec`).
 *
 * Kotlin doesn't have ownership, so `into_*` variants are expressed as normal extensions.
 */
fun <T, B> List<T>.intoMap(f: (T) -> B): List<B> {
    return this.map(f)
}

/**
 * A shorthand for `into_iter().map(f).collect::<Result<Vec<_>, _>>()`.
 */
fun <T, B> List<T>.intoTryMap(f: (T) -> Result<B>): Result<List<B>> {
    if (this.isEmpty()) return Result.success(emptyList())

    val first = f(this[0])
    if (first.isFailure) return Result.failure(first.exceptionOrNull()!!)

    val res = ArrayList<B>(this.size)
    res.add(first.getOrThrow())
    for (i in 1 until this.size) {
        val x = f(this[i])
        if (x.isFailure) return Result.failure(x.exceptionOrNull()!!)
        res.add(x.getOrThrow())
    }
    return Result.success(res)
}

