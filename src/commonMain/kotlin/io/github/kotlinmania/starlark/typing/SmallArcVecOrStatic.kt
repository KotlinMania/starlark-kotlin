// port-lint: source src/typing/small_arc_vec_or_static.rs
package io.github.kotlinmania.starlark.typing.smallarcvecorstatic
import io.github.kotlinmania.starlark.typing.SmallArcVec1

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

private sealed class SmallArcVec1OrStaticImpl<out T> {
    class Arc<T : Comparable<T>>(
        val inner: SmallArcVec1<T>,
    ) : SmallArcVec1OrStaticImpl<T>()

    class Static<T>(
        val inner: List<T>,
    ) : SmallArcVec1OrStaticImpl<T>()
}

internal class SmallArcVec1OrStatic<T> private constructor(
    private val impl: SmallArcVec1OrStaticImpl<T>,
) : Comparable<SmallArcVec1OrStatic<T>>,
    Iterable<T> where T : Comparable<T> {
    companion object {
        fun <T : Comparable<T>> newStatic(x: List<T>): SmallArcVec1OrStatic<T> = SmallArcVec1OrStatic(SmallArcVec1OrStaticImpl.Static(x))

        fun <T : Comparable<T>> cloneFromSlice(x: List<T>): SmallArcVec1OrStatic<T> =
            if (x.isEmpty()) {
                newStatic(emptyList())
            } else {
                SmallArcVec1OrStatic(
                    SmallArcVec1OrStaticImpl.Arc(
                        SmallArcVec1.cloneFromSlice(x),
                    ),
                )
            }
    }

    fun asSlice(): List<T> =
        when (val i = impl) {
            is SmallArcVec1OrStaticImpl.Arc -> i.inner.asSlice()
            is SmallArcVec1OrStaticImpl.Static -> i.inner
        }

    val size: Int get() = asSlice().size

    fun isEmpty(): Boolean = asSlice().isEmpty()

    operator fun get(index: Int): T = asSlice()[index]

    override fun iterator(): Iterator<T> = asSlice().iterator()

    override fun compareTo(other: SmallArcVec1OrStatic<T>): Int {
        val a = this.asSlice()
        val b = other.asSlice()
        val minLen = minOf(a.size, b.size)
        for (i in 0 until minLen) {
            val cmp = a[i].compareTo(b[i])
            if (cmp != 0) return cmp
        }
        return a.size.compareTo(b.size)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SmallArcVec1OrStatic<*>) return false
        return asSlice() == other.asSlice()
    }

    override fun hashCode(): Int = asSlice().hashCode()

    override fun toString(): String = asSlice().toString()
}
