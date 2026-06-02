// port-lint: source src/eval/compiler/small_vec_1.rs
package io.github.kotlinmania.starlark.eval.compiler

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

/** Small `Vec`. */

/** A small vector. */
//     One(T),
//     Vec(Vec<T>),
// }
internal sealed class SmallVec1<T> :
    Iterable<T>,
    Comparable<SmallVec1<T>> {
    class One<T>(
        val value: T,
    ) : SmallVec1<T>()

    class Vec<T>(
        val values: MutableList<T>,
    ) : SmallVec1<T>()

    companion object {
        fun <T> new(): SmallVec1<T> = Vec(mutableListOf())
    }

    fun asSlice(): List<T> =
        when (this) {
            is One -> listOf(value)
            is Vec -> values
        }

    // impl Deref for SmallVec1
    // Kotlin: access via asSlice()

    // impl IntoIterator for SmallVec1
    override fun iterator(): Iterator<T> =
        when (this) {
            is One -> iterator { yield(value) }
            is Vec -> values.iterator()
        }

    // Note: returns a new SmallVec1 since sealed classes are immutable references.
    // Caller must reassign: `self = self.extend(that)`
    fun extend(that: SmallVec1<T>): SmallVec1<T> =
        when {
            this is Vec && this.values.isEmpty() -> that
            that is Vec && that.values.isEmpty() -> this
            this is One && that is One -> Vec(mutableListOf(this.value, that.value))
            this is One && that is Vec -> {
                that.values.add(0, this.value)
                Vec(that.values)
            }
            this is Vec && that is One -> {
                this.values.add(that.value)
                Vec(this.values)
            }
            this is Vec && that is Vec -> {
                this.values.addAll(that.values)
                Vec(this.values)
            }
            else -> error("unreachable")
        }

    fun push(value: T): SmallVec1<T> = extend(One(value))

    // impl Debug for SmallVec1
    override fun toString(): String = asSlice().toString()

    // impl PartialEq for SmallVec1
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SmallVec1<*>) return false
        return asSlice() == other.asSlice()
    }

    // impl Hash for SmallVec1
    override fun hashCode(): Int = asSlice().hashCode()

    // impl PartialOrd + Ord for SmallVec1
    @Suppress("UNCHECKED_CAST")
    override fun compareTo(other: SmallVec1<T>): Int {
        val left = asSlice()
        val right = other.asSlice()
        val minLen = minOf(left.size, right.size)
        for (i in 0 until minLen) {
            val cmp = (left[i] as Comparable<T>).compareTo(right[i])
            if (cmp != 0) return cmp
        }
        return left.size.compareTo(right.size)
    }
}
