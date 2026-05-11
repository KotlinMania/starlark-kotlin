// port-lint: source collections/string_pool.rs
package io.github.kotlinmania.starlark.collections

/*
 * Copyright 2019 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not import this file except in compliance with the License.
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

/** Reuse string allocation. */

/** Pool of strings. */
internal class StringPool {
    private val builders = mutableListOf<StringBuilder>()

    /**
     * Fetch a string from the pool or create an empty one.
     *
     * It is OK to not return a string to the pool.
     */
    fun alloc(): StringBuilder {
        val builder = if (builders.isNotEmpty()) builders.removeLast() else StringBuilder()
        check(builder.isEmpty()) { "Pooled builder should be empty" }
        return builder
    }

    /**
     * Return the string back to the pool.
     *
     * Only strings previously allocated with this pool should be returned,
     * otherwise pool may grow too much.
     */
    fun release(s: StringBuilder) {
        s.clear()
        builders.add(s)
    }
}
