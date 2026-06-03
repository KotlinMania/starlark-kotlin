// port-lint: source src/collections/alloca.rs
package io.github.kotlinmania.starlark.collections

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

/** Initial temporary allocation capacity hint, in bytes. */
private const val INITIAL_SIZE: Int = 1000000

/**
 * A reusable allocator-style helper for callback-scoped temporary slices.
 */
internal class Alloca {
    companion object {
        /** Default initial capacity in bytes. */
        private const val DEFAULT_SIZE: Int = INITIAL_SIZE
    }

    /**
     * Create a new [Alloca] with the default capacity.
     */
    constructor() : this(DEFAULT_SIZE)

    /**
     * Create a new [Alloca] with the given capacity hint.
     */
    constructor(
        @Suppress("UNUSED_PARAMETER") sizeBytes: Int,
    ) {
    }

    /**
     * Allocate an uninitialized array of [len] elements and pass it to [k].
     *
     * @param len Number of elements to allocate.
     * @param k Callback that receives a nullable temporary array and produces a result.
     * @return The result produced by [k].
     */
    fun <T, R> allocaUninit(len: Int, k: (Array<Any?>) -> R): R {
        val data = arrayOfNulls<Any>(len)
        return k(data)
    }

    /**
     * Allocate and initialize an array of [len] elements, then pass it to [k].
     *
     * Each element is initialized by calling [init]. The resulting mutable list
     * is passed to the callback.
     *
     * @param len Number of elements to allocate.
     * @param init Factory function called once per element to produce its initial value.
     * @param k Callback that receives the initialized mutable list and produces a result.
     * @return The result produced by [k].
     */
    fun <T, R> allocaInit(len: Int, init: () -> T, k: (MutableList<T>) -> R): R {
        val data = MutableList(len) { init() }
        return k(data)
    }

    /**
     * Allocate an array of [len] elements, all set to [fill], then pass it to [k].
     *
     * This is a convenience wrapper around [allocaInit] for copyable values.
     *
     * @param len Number of elements to allocate.
     * @param fill The value to fill every element with.
     * @param k Callback that receives the filled mutable list and produces a result.
     * @return The result produced by [k].
     */
    fun <T, R> allocaFill(len: Int, fill: T, k: (MutableList<T>) -> R): R = allocaInit(len, { fill }, k)

    /**
     * Concatenate two lists and invoke [k] with the result.
     *
     * If either list is empty, the other is passed directly to [k] without copying.
     * Otherwise, the elements are cloned into a new temporary list.
     *
     * @param x First list.
     * @param y Second list.
     * @param k Callback that receives the concatenated list and produces a result.
     * @return The result produced by [k].
     */
    fun <T, R> allocaConcat(x: List<T>, y: List<T>, k: (List<T>) -> R): R =
        if (x.isEmpty()) {
            k(y)
        } else if (y.isEmpty()) {
            k(x)
        } else {
            allocaConcatSlow(x, y, k)
        }

    /**
     * Slow path for [allocaConcat]: both lists are non-empty, so we must
     * clone elements into a new combined list.
     */
    private fun <T, R> allocaConcatSlow(x: List<T>, y: List<T>, k: (List<T>) -> R): R {
        val xy = ArrayList<T>(x.size + y.size)
        xy.addAll(x)
        xy.addAll(y)
        return k(xy)
    }
}
