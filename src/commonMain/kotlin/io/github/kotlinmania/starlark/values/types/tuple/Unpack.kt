// port-lint: source src/values/types/tuple/unpack.rs
package io.github.kotlinmania.starlark.values.types.tuple.unpack

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

/** Unpack a value of type `tuple[T, ...]` into a list. */
class UnpackTuple<T>(
    /** Unpacked items. */
    val items: MutableList<T>,
) : Iterable<T> {
    constructor() : this(mutableListOf())

    companion object {
        // Kotlin: type representation deferred to when Ty is fully ported.

        fun <T> unpackValueImpl(
            value: Any,
            tupleFromValue: (Any) -> List<Any>?,
            unpackItem: (Any) -> T?,
        ): UnpackTuple<T>? {
            val tuple = tupleFromValue(value) ?: return null
            val items = ArrayList<T>(tuple.size)
            for (v in tuple) {
                val item = unpackItem(v) ?: return null
                items.add(item)
            }
            return UnpackTuple(items)
        }
    }

    override fun iterator(): Iterator<T> = items.iterator()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UnpackTuple<*>) return false
        return items == other.items
    }

    override fun hashCode(): Int = items.hashCode()

    override fun toString(): String = "UnpackTuple(items=$items)"
}

// Tests are in commonTest, not here.
