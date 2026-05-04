// port-lint: source eval/compiler/smallVec1.rs
package io.github.kotlinmania.starlark.eval.compiler

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

/** Small `Vec`. */
internal sealed class SmallVec1<T> : Iterable<T>, Comparable<SmallVec1<T>> {
    class One<T>(val value: T) : SmallVec1<T>()
    class Vec<T>(val values: MutableList<T>) : SmallVec1<T>()

    fun asSlice(): List<T> = when (this) {
        is One -> listOf(value)
        is Vec -> values
    }

    fun fmt(): String = asSlice().toString()

    fun eq(other: SmallVec1<T>): Boolean = asSlice() == other.asSlice()

    fun hash(): Int = asSlice().hashCode()

    fun partialCmp(other: SmallVec1<T>): Int {
        val left = asSlice()
        val right = other.asSlice()
        val minLen = minOf(left.size, right.size)
        for (i in 0 until minLen) {
            val cmp = (left[i] as Comparable<T>).compareTo(right[i])
            if (cmp != 0) return cmp
        }
        return left.size.compareTo(right.size)
    }

    fun cmp(other: SmallVec1<T>): Int = partialCmp(other)

    fun deref(): List<T> = asSlice()

    fun intoIter(): Iterator<T> = when (this) {
        is One -> iterator { yield(value) }
        is Vec -> values.iterator()
    }

    override fun iterator(): Iterator<T> = intoIter()

    fun extend(that: SmallVec1<T>): SmallVec1<T> {
        val left = this
        val right = that
        return when {
            left is Vec && left.values.isEmpty() -> right
            right is Vec && right.values.isEmpty() -> left
            left is One && right is One -> Vec(mutableListOf(left.value, right.value))
            left is One && right is Vec -> {
                right.values.add(0, left.value)
                Vec(right.values)
            }
            left is Vec && right is One -> {
                left.values.add(right.value)
                Vec(left.values)
            }
            left is Vec && right is Vec -> {
                left.values.addAll(right.values)
                Vec(left.values)
            }
            else -> error("unreachable")
        }
    }

    fun push(value: T): SmallVec1<T> = extend(One(value))

    override fun toString(): String = fmt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SmallVec1<*>) return false
        return eq(other as SmallVec1<T>)
    }

    override fun hashCode(): Int = hash()

    override fun compareTo(other: SmallVec1<T>): Int = cmp(other)

    companion object {
        fun <T> new(): SmallVec1<T> = Vec(mutableListOf())
    }
}
