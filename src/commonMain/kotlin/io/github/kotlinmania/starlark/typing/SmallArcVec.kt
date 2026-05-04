// port-lint: source typing/smallArcVec.rs
package io.github.kotlinmania.starlark.typing

/*
 * Copyright 2019 The Starlark in Rust Authors.
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

private sealed class SmallArcVec1Impl<out T> {
    data object Zero : SmallArcVec1Impl<Nothing>()
    class One<T>(val value: T) : SmallArcVec1Impl<T>() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is One<*>) return false
            return value == other.value
        }
        override fun hashCode(): Int = value.hashCode()
    }
    class Many<T>(val values: List<T>) : SmallArcVec1Impl<T>() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Many<*>) return false
            return values == other.values
        }
        override fun hashCode(): Int = values.hashCode()
    }
}

internal class SmallArcVec1<T> private constructor(
    private val impl: SmallArcVec1Impl<T>,
) : Comparable<SmallArcVec1<T>> where T : Comparable<T> {

    companion object {
        fun <T : Comparable<T>> empty(): SmallArcVec1<T> {
            return SmallArcVec1(SmallArcVec1Impl.Zero)
        }

        fun <T : Comparable<T>> one(x: T): SmallArcVec1<T> {
            return SmallArcVec1(SmallArcVec1Impl.One(x))
        }

        fun <T : Comparable<T>> cloneFromSlice(slice: List<T>): SmallArcVec1<T> {
            return when (slice.size) {
                0 -> empty()
                1 -> one(slice[0])
                else -> SmallArcVec1(SmallArcVec1Impl.Many(slice.toList()))
            }
        }

        fun <T : Comparable<T>> fromIterator(iter: Iterator<T>): SmallArcVec1<T> {
            if (!iter.hasNext()) {
                return empty()
            }
            val i0 = iter.next()
            if (!iter.hasNext()) {
                return SmallArcVec1(SmallArcVec1Impl.One(i0))
            }
            val i1 = iter.next()
            val vec = mutableListOf(i0, i1)
            while (iter.hasNext()) {
                vec.add(iter.next())
            }
            return SmallArcVec1(SmallArcVec1Impl.Many(vec))
        }
    }

    fun asSlice(): List<T> {
        return when (val i = impl) {
            is SmallArcVec1Impl.Zero -> emptyList()
            is SmallArcVec1Impl.One -> listOf(i.value)
            is SmallArcVec1Impl.Many -> {
                require(i.values.size >= 2)
                i.values
            }
        }
    }

    val size: Int get() = asSlice().size

    fun isEmpty(): Boolean = impl is SmallArcVec1Impl.Zero

    operator fun get(index: Int): T = asSlice()[index]

    operator fun iterator(): Iterator<T> = asSlice().iterator()

    override fun compareTo(other: SmallArcVec1<T>): Int {
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
        if (other !is SmallArcVec1<*>) return false
        return asSlice() == other.asSlice()
    }

    override fun hashCode(): Int = asSlice().hashCode()

    override fun toString(): String = asSlice().toString()
}
