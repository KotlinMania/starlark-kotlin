// port-lint: source src/values/thin_box_slice_frozen_value/packed_impl.rs
package io.github.kotlinmania.starlark.values.thinboxslicefrozenvalue

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

import io.github.kotlinmania.starlark.values.layout.FrozenValue

/**
 * Wrapper to handle the packing and most of the unsafety.
 *
 * In Kotlin, this is simplified to a wrapper around a list since there is no
 * need for low-level bit packing. The Rust version uses pointer tricks to
 * store a single FrozenValue inline vs. a heap-allocated slice.
 */
private class PackedImpl(
    private val items: List<FrozenValue>,
) {
    companion object {
        fun new(iter: Iterable<FrozenValue>): PackedImpl = PackedImpl(iter.toList())
    }

    fun asSlice(): List<FrozenValue> = items
}

/**
 * Optimized version of a `Box<[FrozenValue]>`.
 *
 * In Kotlin, this is simplified to a wrapper around a List<FrozenValue>.
 * The Rust version uses bit packing and other tricks so that it is only
 * 8 bytes in size, while being allocation free for lengths zero and one.
 */
class ThinBoxSliceFrozenValue private constructor(
    private val packed: PackedImpl,
) : AbstractList<FrozenValue>() {
    companion object {
        /** Produces an empty list */
        fun empty(): ThinBoxSliceFrozenValue = ThinBoxSliceFrozenValue(PackedImpl(emptyList()))

        // impl FromIterator<FrozenValue> for ThinBoxSliceFrozenValue
        fun fromIter(iter: Iterable<FrozenValue>): ThinBoxSliceFrozenValue = ThinBoxSliceFrozenValue(PackedImpl.new(iter))
    }

    override val size: Int get() = packed.asSlice().size

    override fun get(index: Int): FrozenValue = packed.asSlice()[index]

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ThinBoxSliceFrozenValue) return false
        return packed.asSlice() == other.packed.asSlice()
    }

    override fun hashCode(): Int = packed.asSlice().hashCode()

    override fun toString(): String = packed.asSlice().toString()
}
