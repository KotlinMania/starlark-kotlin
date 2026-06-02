// port-lint: source src/values/layout/heap/maybe_uninit_slice_util.rs
package io.github.kotlinmania.starlark.values.layout.heap.maybe_uninit_slice_util

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
 * Populate a slice (array) with values from iterator, handle exceptions by writing
 * provided fallback value.
 *
 * In Kotlin, arrays are always initialized, so MaybeUninit is modeled as Array<T?>.
 * The fallback write-on-panic semantics are preserved using try/finally.
 */
internal inline fun <reified T> maybeUninitWriteFromExactSizeIter(
    slice: Array<T?>,
    iter: Iterable<T>,
    writeOnPanic: T,
) {
    /** On drop (exception), populate the remaining elements with the provided value. */
    // WriteRemOnDrop equivalent: try/finally pattern
    var writeIndex = 0
    try {
        val iterator = iter.iterator()
        for (i in slice.indices) {
            check(iterator.hasNext()) { "iterator provided size_hint incorrectly" }
            slice[i] = iterator.next()
            writeIndex = i + 1
        }
        check(!iterator.hasNext()) { "iterator provided size_hint incorrectly" }
    } catch (e: Throwable) {
        // Fill remaining uninitialized slots with fallback value
        for (i in writeIndex until slice.size) {
            slice[i] = writeOnPanic
        }
        throw e
    }
}
