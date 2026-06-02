// port-lint: source src/values/types/list_or_tuple.rs
package io.github.kotlinmania.starlark.values.types.listortuple

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

/** Utility for unpacking a value of type `list[T]` or `tuple[T, ...]` into a list. */

/** Unpack a value of type `list[T]` or `tuple[T, ...]` into a list. */
class UnpackListOrTuple<T>(
    /** Unpacked items of the list or tuple. */
    val items: MutableList<T>,
) : Iterable<T> {
    constructor() : this(mutableListOf())

    companion object {
        // Kotlin: type representation delegates to Either<UnpackList<T>, UnpackTuple<T>>
        // Actual type repr will be resolved when UnpackList/UnpackTuple are ported.

        fun <T> unpackValueImpl(
            value: Any,
            unpackList: (Any) -> List<T>?,
            unpackTuple: (Any) -> List<T>?,
        ): UnpackListOrTuple<T>? {
            val listResult = unpackList(value)
            if (listResult != null) {
                return UnpackListOrTuple(listResult.toMutableList())
            }
            val tupleResult = unpackTuple(value)
            if (tupleResult != null) {
                return UnpackListOrTuple(tupleResult.toMutableList())
            }
            return null
        }
    }

    override fun iterator(): Iterator<T> = items.iterator()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UnpackListOrTuple<*>) return false
        return items == other.items
    }

    override fun hashCode(): Int = items.hashCode()

    override fun toString(): String = "UnpackListOrTuple(items=$items)"
}

// Tests are in commonTest, not here.
