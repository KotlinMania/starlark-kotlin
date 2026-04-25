// port-lint: source src/values/layout/value_alloc_size.rs
package io.github.kotlinmania.starlark.values.layout

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

import io.github.kotlinmania.starlark.values.layout.AlignedSize
import io.github.kotlinmania.starlark.values.layout.heap.arena.MIN_ALLOC

/**
 * Size of `AValue` with `AValueHeader` added.
 * This is the size of the value as it is stored in the heap.
 */
data class ValueAllocSize(
    private val size: AlignedSize,
) : Comparable<ValueAllocSize> {

    override fun compareTo(other: ValueAllocSize): Int {
        return size.compareTo(other.size)
    }

    // impl ValueAllocSize

    companion object {
        // pub(crate) fn try_new(size: AlignedSize) -> Option<ValueAllocSize>
        fun tryNew(size: AlignedSize): ValueAllocSize? {
            return if (size < MIN_ALLOC) {
                null
            } else {
                ValueAllocSize(size)
            }
        }

        // pub(crate) fn new(size: AlignedSize) -> ValueAllocSize
        fun new(size: AlignedSize): ValueAllocSize {
            return tryNew(size)
                ?: error("$size is too small for a value (minimum is $MIN_ALLOC)")
        }
    }

    // pub(crate) fn layout(self) -> Layout
    // Kotlin: No `std::alloc::Layout`. Not transliterable.

    // pub(crate) fn size(self) -> AlignedSize
    fun size(): AlignedSize = size

    // pub(crate) const fn bytes(self) -> u32
    fun bytes(): UInt = size.bytes()
}
