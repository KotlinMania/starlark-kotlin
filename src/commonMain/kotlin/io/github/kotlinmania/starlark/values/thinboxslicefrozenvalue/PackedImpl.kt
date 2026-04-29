// port-lint: source src/values/thinBoxSliceFrozenValue/packedImpl.rs
package io.github.kotlinmania.starlark.values.thinboxslicefrozenvalue

/*
 * Copyright 2018 The Starlark in Rust Authors.
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

import io.github.kotlinmania.starlark.Either
import io.github.kotlinmania.starlark.values.layout.FrozenValue

/**
 * Wrapper to handle the packing and most of the unsafety.
 *
 * The representation is as follows:
 *  - In the case of a length 1 slice, the `FrozenValue` is stored in the `NonNull` pointer.
 *  - In all other cases, the `NonNull` is a `AllocatedThinBoxSlice<FrozenValue>` with the bottom
 *    bit set to 1.
 */
internal class PackedImpl private constructor(
    private val inline: FrozenValue?,
    private val allocated: AllocatedThinBoxSlice<FrozenValue>?,
) {
    companion object {
        fun newAllocated(allocated: AllocatedThinBoxSlice<FrozenValue>): PackedImpl {
            return PackedImpl(null, allocated)
        }

        fun new(iter: Iterable<FrozenValue>): PackedImpl {
            val it = iter.iterator()
            if (!it.hasNext()) {
                return newAllocated(AllocatedThinBoxSlice.empty())
            }
            val first = it.next()
            if (!it.hasNext()) {
                return PackedImpl(first, null)
            }
            val second = it.next()
            val rest = mutableListOf(first, second)
            while (it.hasNext()) rest.add(it.next())
            return newAllocated(AllocatedThinBoxSlice.fromIter(rest))
        }
    }

    fun unpack(): Either<FrozenValue, AllocatedThinBoxSlice<FrozenValue>> {
        return if (allocated != null) {
            Either.Right(allocated)
        } else {
            Either.Left(inline!!)
        }
    }

    fun asSlice(): List<FrozenValue> {
        return when (val u = unpack()) {
            is Either.Left -> listOf(u.value)
            is Either.Right -> u.value.toList()
        }
    }

    fun drop() {
        when (val u = unpack()) {
            is Either.Left -> {}
            is Either.Right -> u.value.runDrop()
        }
    }
}

/**
 * Optimized version of a boxed FrozenValue slice.
 *
 * Specifically, this type uses bit packing and other tricks so that it is only
 * 8 bytes in size, while being allocation free for lengths zero and one. It
 * depends on the lower bit of a FrozenPointer always being unset.
 */
class ThinBoxSliceFrozenValue private constructor(
    private val packed: PackedImpl,
) : AbstractList<FrozenValue>() {

    companion object {
        /** Produces an empty list */
        fun empty(): ThinBoxSliceFrozenValue =
            ThinBoxSliceFrozenValue(PackedImpl.newAllocated(AllocatedThinBoxSlice.empty()))

        fun fromIter(iter: Iterable<FrozenValue>): ThinBoxSliceFrozenValue {
            return ThinBoxSliceFrozenValue(PackedImpl.new(iter))
        }

        fun default(): ThinBoxSliceFrozenValue = fromIter(emptyList())
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
