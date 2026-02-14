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

// #[derive(Debug, Allocative)]
// enum Inner<T: ?Sized + 'static> {
//     Arc(Arc<T>),
//     Static(&'static T),
// }
// Kotlin: GC handles all references; no need for Arc/static distinction.

// #[derive(Debug, Allocative)]
// pub(crate) struct ArcOrStatic<T: ?Sized + 'static>(Inner<T>);
// Kotlin: simple wrapper around a reference. GC handles lifetimes.
internal class ArcOrStatic<T : Any> private constructor(
    private val value: T,
) : Comparable<ArcOrStatic<T>> {

    companion object {
        // pub(crate) fn new_static(a: &'static T) -> Self
        fun <T : Any> newStatic(a: T): ArcOrStatic<T> {
            return ArcOrStatic(a)
        }

        // pub(crate) fn new_arc(a: Arc<T>) -> Self
        fun <T : Any> newArc(a: T): ArcOrStatic<T> {
            return ArcOrStatic(a)
        }

        // pub(crate) fn new(a: T) -> Self
        fun <T : Any> new(a: T): ArcOrStatic<T> {
            return ArcOrStatic(a)
        }
    }

    // impl Deref for ArcOrStatic
    // fn deref(&self) -> &T
    fun deref(): T = value

    // impl Display for ArcOrStatic
    override fun toString(): String = value.toString()

    // impl PartialEq for ArcOrStatic
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ArcOrStatic<*>) return false
        return value == other.value
    }

    // impl Hash for ArcOrStatic
    override fun hashCode(): Int = value.hashCode()

    // impl Ord for ArcOrStatic
    @Suppress("UNCHECKED_CAST")
    override fun compareTo(other: ArcOrStatic<T>): Int {
        return (value as Comparable<T>).compareTo(other.value)
    }
}
