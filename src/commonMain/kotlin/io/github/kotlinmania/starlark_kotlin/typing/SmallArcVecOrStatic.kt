// port-lint: source src/typing/small_arc_vec_or_static.rs
package io.github.kotlinmania.starlark_kotlin.typing.small_arc_vec_or_static

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
 * A small vector that is either arc-reference-counted or a static slice.
 *
 * In Rust, this optimizes memory by storing small vectors inline (via `SmallArcVec1`)
 * or as a static reference. In Kotlin, we use a simple [List] wrapper since
 * the JVM GC handles reference counting.
 */
// #[derive(Debug, Clone, Dupe, Allocative)]
// pub(crate) struct SmallArcVec1OrStatic<T: 'static>(SmallArcVec1OrStaticImpl<T>)
internal class SmallArcVec1OrStatic<T> private constructor(
    private val items: List<T>,
) : Iterable<T> {
    // impl SmallArcVec1OrStatic

    companion object {
        /** Create from a static slice. */
        // pub(crate) fn new_static(x: &'static [T]) -> Self
        fun <T> newStatic(x: List<T>): SmallArcVec1OrStatic<T> {
            return SmallArcVec1OrStatic(x)
        }

        /** Create from a slice, cloning the elements. */
        // pub(crate) fn clone_from_slice(x: &[T]) -> Self
        fun <T> cloneFromSlice(x: List<T>): SmallArcVec1OrStatic<T> {
            return if (x.isEmpty()) {
                newStatic(emptyList())
            } else {
                SmallArcVec1OrStatic(x.toList())
            }
        }
    }

    /** Get the underlying slice. */
    // pub(crate) fn as_slice(&self) -> &[T]
    fun asSlice(): List<T> = items

    // impl Default for SmallArcVec1OrStatic<T>
    // Use SmallArcVec1OrStatic.newStatic(emptyList()) for the default.

    // impl Deref for SmallArcVec1OrStatic<T> { type Target = [T]; }
    val size: Int get() = items.size
    operator fun get(index: Int): T = items[index]
    fun isEmpty(): Boolean = items.isEmpty()

    // impl IntoIterator for &SmallArcVec1OrStatic<T>
    override fun iterator(): Iterator<T> = items.iterator()

    // impl PartialEq for SmallArcVec1OrStatic<T>
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SmallArcVec1OrStatic<*>) return false
        return items == other.items
    }

    // impl Hash for SmallArcVec1OrStatic<T>
    override fun hashCode(): Int = items.hashCode()

    override fun toString(): String = items.toString()
}
