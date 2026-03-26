// port-lint: source src/util/arc_or_static.rs
package io.github.kotlinmania.starlark_kotlin.util.arc_or_static

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
 * A value that is either reference-counted or a static reference.
 *
 * In Rust, this is `Arc<T>` or `&'static T`. In Kotlin, we don't need the distinction
 * since garbage collection handles reference counting. We keep the wrapper for
 * API parity and to document the ownership semantics.
 */
// #[derive(Debug, Allocative)]
// pub(crate) struct ArcOrStatic<T: ?Sized + 'static>(Inner<T>)
internal class ArcOrStatic<T> private constructor(
    private val value: T,
) {
    // impl ArcOrStatic

    // pub(crate) fn new_static(a: &'static T) -> Self
    // pub(crate) fn new_arc(a: Arc<T>) -> Self
    // pub(crate) fn new(a: T) -> Self

    /** Get the contained value. */
    fun get(): T = value

    companion object {
        /** Create from a static reference. */
        fun <T> newStatic(a: T): ArcOrStatic<T> {
            return ArcOrStatic(a)
        }

        /** Create from a new value (would be Arc-wrapped in Rust). */
        fun <T> new(a: T): ArcOrStatic<T> {
            return ArcOrStatic(a)
        }
    }

    // impl Deref for ArcOrStatic<T>
    // In Kotlin, use get() instead.

    // impl Display for ArcOrStatic<T>
    override fun toString(): String = value.toString()

    // impl Clone for ArcOrStatic<T>
    // In Kotlin, ArcOrStatic is effectively immutable and shareable.

    // impl PartialEq for ArcOrStatic<T>
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ArcOrStatic<*>) return false
        return value == other.value
    }

    // impl Hash for ArcOrStatic<T>
    override fun hashCode(): Int = value.hashCode()

    // impl PartialOrd + Ord are available via Comparable if T is Comparable.
}
