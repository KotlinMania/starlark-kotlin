// port-lint: source src/eval/bc/ifDebug.rs
package io.github.kotlinmania.starlark.eval.bc

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

/** Utility to make stateful debug assertions easier. */

/**
 * Store `T` if debug assertions enabled, ZST otherwise.
 *
 * This object is easier to work with than explicitly marking code with
 * without mistakes where code works, and no warnings in both debug and release.
 *
 * In other words, this type converts cfg to types.
 *
 * This type implements [equals] which always returns `true`,
 * and [compareTo] which always returns 0 (Equal), so
 * * this type is easy to include in structs which do derives
 * * behavior of this object does not depend on `<T>`
 * * behavior does not depend on whether debugging assertions enabled or not
 */
// In release build this structure is DST,
// so gazebo suggests implementing `Dupe` for any `<T>`. T102920913.
//     _marker: marker::PhantomData<T>,
// Kotlin: no cfg-based conditional compilation; always store the value.
internal class IfDebug<T> private constructor(
    private val value: T?,
) : Comparable<IfDebug<T>> {

    companion object {
        // Always true in Kotlin (no zero-cost release stripping).
        private const val DEBUG: Boolean = true

        /** Store a value if debug assertions enabled, drop otherwise. */
        fun <T> new(value: T): IfDebug<T> {
            return newIfDebug { value }
        }

        /** Store a value if debug assertions enabled, drop otherwise. */
        fun <T> newIfDebug(init: () -> T): IfDebug<T> {
            return IfDebug(
                value = if (DEBUG) init() else null,
            )
        }
    }

    /** Get a reference to stored value if assertions enabled, `null` otherwise. */
    fun getRef(): T? {
        return value
    }

    /** Get a reference to stored value if assertions enabled, panic otherwise. */
    fun getRefIfDebug(): T {
        return getRef() ?: error("assertions disabled")
    }

    /** Invoke a function if debug enabled. */
    fun ifDebug(f: (T) -> Unit) {
        val v = getRef()
        if (v != null) {
            f(v)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is IfDebug<*>) return false
        return true
    }

    override fun hashCode(): Int = 0

    override fun compareTo(other: IfDebug<T>): Int = 0

    override fun toString(): String = "IfDebug($value)"
}
