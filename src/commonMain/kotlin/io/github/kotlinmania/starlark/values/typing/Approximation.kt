// port-lint: source src/typing/ty.rs
package io.github.kotlinmania.starlark_kotlin.values.typing

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
 * A typing operation wasn't able to produce a precise result,
 * so made some kind of approximation.
 */
data class Approximation(
    /** The category of the approximation, e.g. `"Unknown type"`. */
    val category: String,
    /** The precise details of this approximation, e.g. which type was unknown. */
    val message: String,
) : Comparable<Approximation> {
    companion object {
        /** Create a new [Approximation]. */
        fun new(category: String, message: Any): Approximation {
            return Approximation(
                category = category,
                message = "$message",
            )
        }
    }

    override fun compareTo(other: Approximation): Int {
        val cmp = category.compareTo(other.category)
        if (cmp != 0) return cmp
        return message.compareTo(other.message)
    }

    override fun toString(): String {
        return "Approximation: $category = \"$message\""
    }
}
