// port-lint: source src/values/thin_box_slice_frozen_value/packed_impl.rs
package io.github.kotlinmania.starlark_kotlin.values.thin_box_slice_frozen_value

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

import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.types.int.InlineInt

/// Wrapper to handle the packing and most of the unsafety.
///
/// In Kotlin, this is simplified to a wrapper around a list since there is no
/// need for low-level bit packing. The Rust version uses pointer tricks to
/// store a single FrozenValue inline vs. a heap-allocated slice.
// struct PackedImpl(NonNull<()>)
private class PackedImpl(
    private val items: List<FrozenValue>,
) {
    companion object {
        fun new(iter: Iterable<FrozenValue>): PackedImpl {
            return PackedImpl(iter.toList())
        }
    }

    fun asSlice(): List<FrozenValue> = items
}

/// Optimized version of a `Box<[FrozenValue]>`.
///
/// In Kotlin, this is simplified to a wrapper around a List<FrozenValue>.
/// The Rust version uses bit packing and other tricks so that it is only
/// 8 bytes in size, while being allocation free for lengths zero and one.
// pub struct ThinBoxSliceFrozenValue<'v>(PackedImpl, PhantomData<&'v ()>)
class ThinBoxSliceFrozenValue(
    private val packed: PackedImpl,
) : AbstractList<FrozenValue>() {

    companion object {
        /// Produces an empty list
        // pub const fn empty() -> Self
        fun empty(): ThinBoxSliceFrozenValue = ThinBoxSliceFrozenValue(PackedImpl(emptyList()))

        // impl FromIterator<FrozenValue> for ThinBoxSliceFrozenValue
        fun fromIter(iter: Iterable<FrozenValue>): ThinBoxSliceFrozenValue {
            return ThinBoxSliceFrozenValue(PackedImpl.new(iter))
        }
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

// #[cfg(test)]
// mod tests

private fun acrossLengths(a: List<FrozenValue>) {
    for (len in 0..a.size) {
        val value = ThinBoxSliceFrozenValue.fromIter(a.take(len))
        check(value.size == len)
        check(value.toList() == a.take(len))
    }
}

// #[test]
// fn test_strings()
internal fun testStrings() {
    val h = FrozenHeap()
    val strs = listOf("", "abc", "def", "ghijkl")
    val s = (strs + strs + strs + strs).map { s -> h.allocStr(s).toFrozenValue() }
    acrossLengths(s)
}

// #[test]
// fn test_ints()
internal fun testInts() {
    val ints = listOf(0, 1, 2, 3, 4, 5, 1000, 1 shl 20)
    val i = (ints + ints).map { i -> FrozenValue.newInt(InlineInt.testingNew(i)) }
    acrossLengths(i)
}

// #[test]
// fn test_mixed_types()
internal fun testMixedTypes() {
    val items = listOf(
        FrozenValue.newNone(),
        FrozenValue.newInt(InlineInt.testingNew(0)),
        FrozenValue.newEmptyList(),
        FrozenValue.newBool(true),
    )
    val a = items + items + items + items
    acrossLengths(a)
}

// #[test]
// fn test_default()
internal fun testDefault() {
    val value = ThinBoxSliceFrozenValue.fromIter(emptyList())
    check(value.size == 0)
}

// #[test]
// fn test_empty()
internal fun testEmptyPacked() {
    val valA = ThinBoxSliceFrozenValue.empty()
    val valB = ThinBoxSliceFrozenValue.empty()
    check(valA == valB)
}
