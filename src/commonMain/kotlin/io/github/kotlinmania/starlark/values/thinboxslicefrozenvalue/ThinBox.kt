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
internal class AllocatedThinBoxSlice<T>(
    private val items: MutableList<T> = mutableListOf(),
) : AbstractList<T>() {
    companion object {
        fun <T> empty(): AllocatedThinBoxSlice<T> = AllocatedThinBoxSlice(mutableListOf())

        fun <T> newUninit(len: Int): AllocatedThinBoxSlice<T?> = AllocatedThinBoxSlice(MutableList(len) { null })

        fun <T> fromIter(iter: Iterable<T>): AllocatedThinBoxSlice<T> = AllocatedThinBoxSlice(iter.toMutableList())
    }

    fun readLen(): Int = items.size

    override val size: Int get() = items.size

    override fun get(index: Int): T = items[index]

    operator fun set(index: Int, value: T) {
        items[index] = value
    }

    fun runDrop() {
        items.clear()
    }

    fun intoInner(): List<T> = items.toList()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AllocatedThinBoxSlice<*>) return false
        return items == other.items
    }

    override fun hashCode(): Int = items.hashCode()

    override fun toString(): String = items.toString()
}
