// port-lint: source src/eval/compiler/small_vec_1.rs
package io.github.kotlinmania.starlark_kotlin.eval.compiler

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

/** A small vector optimized for the single-element case. */
internal sealed class SmallVec1<T> : Iterable<T> {

    class One<T>(val value: T) : SmallVec1<T>()
    class Vec<T>(val values: MutableList<T>) : SmallVec1<T>()

    fun asSlice(): List<T> = when (this) {
        is One -> listOf(value)
        is Vec -> values
    }

    override fun iterator(): Iterator<T> = when (this) {
        is One -> sequenceOf(value).iterator()
        is Vec -> values.iterator()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is SmallVec1<*>) return false
        return asSlice() == other.asSlice()
    }

    override fun hashCode(): Int = asSlice().hashCode()

    override fun toString(): String = asSlice().toString()

    companion object {
        fun <T> new(): SmallVec1<T> = Vec(mutableListOf())
    }
}

internal fun <T> SmallVec1<T>.extend(that: SmallVec1<T>): SmallVec1<T> {
    return when {
        this is SmallVec1.Vec && this.values.isEmpty() -> that
        that is SmallVec1.Vec && that.values.isEmpty() -> this
        this is SmallVec1.One && that is SmallVec1.One ->
            SmallVec1.Vec(mutableListOf(this.value, that.value))
        this is SmallVec1.One && that is SmallVec1.Vec -> {
            that.values.add(0, this.value)
            that
        }
        this is SmallVec1.Vec && that is SmallVec1.One -> {
            this.values.add(that.value)
            this
        }
        this is SmallVec1.Vec && that is SmallVec1.Vec -> {
            this.values.addAll(that.values)
            this
        }
        else -> error("unreachable")
    }
}

internal fun <T> SmallVec1<T>.push(value: T): SmallVec1<T> {
    return this.extend(SmallVec1.One(value))
}
