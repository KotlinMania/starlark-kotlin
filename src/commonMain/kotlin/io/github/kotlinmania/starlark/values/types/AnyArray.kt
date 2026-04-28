// port-lint: source src/values/types/anyArray.rs
package io.github.kotlinmania.starlark.values.types.anyarray

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

//! Utility to heap allocate arrays of values.

import io.github.kotlinmania.starlark.values.StarlarkValue

// content holds the heap-allocated elements; the JVM manages layout, so
// this is a plain mutable list rather than a fixed-size inline array.
internal class AnyArray<T>(
    private val content: MutableList<T>,
) : StarlarkValue {

    val len: Int get() = content.size

    companion object {
        /**
         * Creates a new array with capacity for [len] elements.
         */
        fun <T> new(len: Int): AnyArray<T> {
            return AnyArray(ArrayList(len))
        }
    }

    fun asSlice(): List<T> = content

    /**
     * Returns the byte offset of the content array inside the host object.
     * On the JVM there is no C-style memory layout, so this returns 0 — the
     * runtime owns the object's representation.
     */
    fun offsetOfContent(): Int = 0

    operator fun get(index: Int): T = content[index]

    operator fun set(index: Int, value: T) {
        content[index] = value
    }

    fun add(value: T) {
        content.add(value)
    }

    override fun toString(): String {
        return "AnyArray(${asSlice()})"
    }

    // No explicit drop needed; the runtime collects the array when
    // the wrapper is no longer reachable.

    override val TYPE: String get() = "AnyArray"
}

// Tests are in commonTest.
