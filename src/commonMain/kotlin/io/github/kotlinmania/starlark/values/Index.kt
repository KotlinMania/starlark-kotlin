// port-lint: source src/values/index.rs
package io.github.kotlinmania.starlark.values

import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.types.bigint.unpackInt


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

/** Index conversion utilities for Starlark at/set_at and slicing operations. */

/** Unpack an Int from a Value, raising an error if it is not an integer. */
// i32::unpack_value_err(v)
private fun unpackIntErr(v: Value): Result<Int> {
    val result = v.unpackInt()
    val i = result.getOrElse { return Result.failure(it) }
    return if (i != null) {
        Result.success(i)
    } else {
        Result.failure(
            ValueError.IncorrectParameterType
        )
    }
}

// Helper for convert_slice_indices
// fn convert_index_aux(len: i32, v1: Option<Value>, default: i32, min: i32, max: i32) -> crate::Result<i32>
private fun convertIndexAux(
    len: Int,
    v1: Value?,
    default: Int,
    min: Int,
    max: Int,
): Result<Int> {
    if (v1 != null) {
        if (v1.isNone()) {
            return Result.success(default)
        } else {
            val x = unpackIntErr(v1).getOrElse { return Result.failure(it) }
            val i = if (x < 0) len + x else x
            return if (i < min) {
                Result.success(min)
            } else if (i > max) {
                Result.success(max)
            } else {
                Result.success(i)
            }
        }
    } else {
        return Result.success(default)
    }
}

/**
 * Function to parse the index for at/set_at methods.
 *
 * Return an `Int` from the value corresponding to the index recentered between 0
 * and len. Raise the correct errors if the value is not numeric or the
 * index is out of bound.
 */
// pub(crate) fn convert_index(v: Value, len: i32) -> crate::Result<i32>
internal fun convertIndex(v: Value, len: Int): Result<Int> {
    val x = unpackIntErr(v).getOrElse { return Result.failure(it) }
    val i = if (x < 0) {
        val added = len.toLong() + x.toLong()
        if (added < Int.MIN_VALUE || added > Int.MAX_VALUE) {
            return Result.failure(ValueError.IntegerOverflow)
        }
        added.toInt()
    } else {
        x
    }
    return if (i < 0 || i >= len) {
        Result.failure(ValueError.IndexOutOfBound(i))
    } else {
        Result.success(i)
    }
}

/**
 * Parse indices for slicing.
 *
 * Takes the object length and 3 optional values and returns `(Int, Int, Int)`
 * with those index correctly converted in range of length.
 * Return the correct errors if the values are not numeric or the stride is 0.
 */
// pub(crate) fn convert_slice_indices(len: i32, start: Option<Value>, stop: Option<Value>, stride: Option<Value>) -> crate::Result<(i32, i32, i32)>
internal fun convertSliceIndices(
    len: Int,
    start: Value?,
    stop: Value?,
    stride: Value?,
): Result<Triple<Int, Int, Int>> {
    val strideVal = when {
        stride == null -> 1
        stride.isNone() -> 1
        else -> unpackIntErr(stride).getOrElse { return Result.failure(it) }
    }
    return when (strideVal) {
        0 -> Result.failure(ValueError.IndexOutOfBound(0))
        else -> {
            val defStart = if (strideVal < 0) len - 1 else 0
            val defEnd = if (strideVal < 0) -1 else len
            val clamp = if (strideVal < 0) -1 else 0
            val s1 = convertIndexAux(len, start, defStart, clamp, len + clamp)
                .getOrElse { return Result.failure(it) }
            val s2 = convertIndexAux(len, stop, defEnd, clamp, len + clamp)
                .getOrElse { return Result.failure(it) }
            Result.success(Triple(s1, s2, strideVal))
        }
    }
}

// pub(crate) fn apply_slice<T: Copy>(xs: &[T], start: Option<Value>, stop: Option<Value>, stride: Option<Value>) -> crate::Result<Vec<T>>
internal fun <T> applySlice(
    xs: List<T>,
    start: Value?,
    stop: Value?,
    stride: Value?,
): Result<List<T>> {
    val (startIdx, stopIdx, strideVal) = convertSliceIndices(xs.size, start, stop, stride)
        .getOrElse { return Result.failure(it) }
    if (strideVal == 1) {
        return if (startIdx >= stopIdx) {
            Result.success(emptyList())
        } else {
            Result.success(xs.subList(startIdx, stopIdx))
        }
    }

    val (adjStart, adjStop) = if (strideVal < 0) {
        Pair(stopIdx + 1, startIdx + 1)
    } else {
        Pair(startIdx, stopIdx)
    }
    if (adjStart >= adjStop) {
        return Result.success(emptyList())
    }
    val sub = xs.subList(adjStart, adjStop).toMutableList()
    if (strideVal == -1) {
        sub.reverse()
        return Result.success(sub)
    }
    if (strideVal < 0) {
        sub.reverse()
    }
    val astride = kotlin.math.abs(strideVal)
    val res = sub.filterIndexed { index, _ ->
        index % astride == 0
    }
    return Result.success(res)
}

// #[cfg(test)] mod tests
// Tests are in commonTest, not here.
