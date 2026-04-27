// port-lint: source src/util/arcOrStatic.rs
package io.github.kotlinmania.starlark.util.arcorstatic

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

internal sealed interface Inner<T : Any> {
    data class Arc<T : Any>(val value: T) : Inner<T>

    data class Static<T : Any>(val value: T) : Inner<T>
}

internal class ArcOrStatic<T> private constructor(
    private val inner: Inner<T>,
) : Comparable<ArcOrStatic<T>> where T : Any, T : Comparable<T> {

    fun deref(): T {
        return when (val inner = inner) {
            is Inner.Arc -> inner.value
            is Inner.Static -> inner.value
        }
    }

    fun clone(): ArcOrStatic<T> {
        return when (val inner = inner) {
            is Inner.Arc -> ArcOrStatic(Inner.Arc(inner.value))
            is Inner.Static -> ArcOrStatic(Inner.Static(inner.value))
        }
    }

    fun dupe(): ArcOrStatic<T> = clone()

    override fun toString(): String = deref().toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ArcOrStatic<*>) return false
        return deref() == other.deref()
    }

    override fun hashCode(): Int = deref().hashCode()

    override fun compareTo(other: ArcOrStatic<T>): Int {
        return deref().compareTo(other.deref())
    }

    companion object {
        fun <T> newStatic(a: T): ArcOrStatic<T> where T : Any, T : Comparable<T> {
            return ArcOrStatic(Inner.Static(a))
        }

        fun <T> newArc(a: T): ArcOrStatic<T> where T : Any, T : Comparable<T> {
            return ArcOrStatic(Inner.Arc(a))
        }

        fun <T> new(a: T): ArcOrStatic<T> where T : Any, T : Comparable<T> {
            return newArc(a)
        }
    }
}
