// port-lint: source src/values/types/list/unpack.rs
package io.github.kotlinmania.starlark_kotlin.values.types.list

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
 * Unpack a value of type `list<T>` into a vec.
 */
data class UnpackList<T>(
    /** Unpacked items. */
    val items: MutableList<T>
) : Iterable<T> {

    /**
     * Default constructor creating an empty UnpackList.
     */
    constructor() : this(mutableListOf())

    /**
     * Returns an iterator over the items.
     * Implements IntoIterator for UnpackList<T>.
     */
    override fun iterator(): Iterator<T> = items.iterator()

    /**
     * Returns a mutable iterator over the items.
     * Implements IntoIterator for &'a mut UnpackList<T>.
     */
    fun iterMut(): MutableIterator<T> = items.iterator()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as UnpackList<*>
        return items == other.items
    }

    override fun hashCode(): Int = items.hashCode()

    companion object {
        /**
         * Creates a default empty UnpackList.
         * Corresponds to Default::default() in Rust.
         */
        fun <T> default(): UnpackList<T> = UnpackList()
    }
}

/*
 * Pending trait implementations (to be added when base infrastructure is ported):
 *
 * 1. StarlarkTypeRepr for UnpackList<T>:
 *    - Canonical type maps to ListType<T>.Canonical
 *    - starlarkTypeRepr() delegates to ListType.starlarkTypeRepr()
 *
 * 2. UnpackValue for UnpackList<T>:
 *    - Error type: T.Error
 *    - unpackValueImpl: Unpacks ListRef and iterates to unpack each element
 *    - Returns None if value is not a list or any element fails to unpack
 *    - Note: Contains TODO(nga) about avoiding allocation on first element type mismatch
 *
 * 3. Tests:
 *    - test_unpack: Verifies unpacking list of strings, type mismatch, and non-list values
 */
