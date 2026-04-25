// port-lint: source src/values/thin_box_slice_frozen_value/thin_box.rs
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

/**
 * This type is a copy-paste of `buck2_util::thin_box::ThinBoxSlice`, with some mild adjustments.
 *
 * Specifically:
 *  1. This type guarantees that it's always a pointer with the bottom bit zero.
 *  2. This type is not implicitly dropped - `run_drop` must be called explicitly.
 *
 * In Kotlin, this is simplified to a List wrapper since the JVM/Kotlin handles memory management.
 */

/**
 * `Box<[T]>` but thin pointer to FrozenValue(s)
 *
 * In the Kotlin port, this is simplified to a wrapper around a MutableList
 * since Kotlin has garbage collection and does not need the low-level
 * memory layout optimizations of the Rust version.
 */
// pub(super) struct AllocatedThinBoxSlice<T>
internal class AllocatedThinBoxSlice<T>(
    private val items: MutableList<T> = mutableListOf(),
) : AbstractList<T>() {

    companion object {
        // pub(super) const fn empty() -> AllocatedThinBoxSlice<T>
        fun <T> empty(): AllocatedThinBoxSlice<T> = AllocatedThinBoxSlice(mutableListOf())

        // pub(super) fn new_uninit(len: usize) -> AllocatedThinBoxSlice<MaybeUninit<T>>
        fun <T> newUninit(len: Int): AllocatedThinBoxSlice<T?> {
            return AllocatedThinBoxSlice(MutableList(len) { null })
        }

        // impl FromIterator<T> for AllocatedThinBoxSlice<T>
        fun <T> fromIter(iter: Iterable<T>): AllocatedThinBoxSlice<T> {
            return AllocatedThinBoxSlice(iter.toMutableList())
        }
    }

    // fn read_len(&self) -> usize
    fun readLen(): Int = items.size

    override val size: Int get() = items.size

    override fun get(index: Int): T = items[index]

    operator fun set(index: Int, value: T) {
        items[index] = value
    }

    // pub(super) fn run_drop(self)
    fun runDrop() {
        items.clear()
    }

    // pub const unsafe fn into_inner(self) -> usize
    fun intoInner(): List<T> = items.toList()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AllocatedThinBoxSlice<*>) return false
        return items == other.items
    }

    override fun hashCode(): Int = items.hashCode()

    override fun toString(): String = items.toString()
}

// #[cfg(test)]
// mod tests

// #[test]
// fn test_empty()
internal fun testEmpty() {
    val thin = AllocatedThinBoxSlice.empty<String>()
    check(thin.size == 0)
    thin.runDrop()
}

// #[test]
// fn test_from_iter_sized()
internal fun testFromIterSized() {
    val thin = AllocatedThinBoxSlice.fromIter(listOf("a", "bb", "ccc"))
    check(thin.toList() == listOf("a", "bb", "ccc"))
    thin.runDrop()
}

// #[test]
// fn test_from_iter_unknown_size()
internal fun testFromIterUnknownSize() {
    val thin = AllocatedThinBoxSlice.fromIter(
        listOf("a", "b", "c").filter { true }
    )
    check(thin.toList() == listOf("a", "b", "c"))
    thin.runDrop()
}

/** If there are obvious memory violations, this test will catch them. */
// #[test]
// fn test_stress()
internal fun testStress() {
    for (i in 0 until 1000) {
        val thin = AllocatedThinBoxSlice.fromIter((0 until i).map { j -> j.toString() })
        check(thin.size == i)
        check(thin.toList() == (0 until i).map { j -> j.toString() })
        thin.runDrop()
    }
}
