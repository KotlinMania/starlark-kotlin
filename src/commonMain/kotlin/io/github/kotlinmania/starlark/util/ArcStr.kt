// port-lint: source src/util/arcStr.rs
package io.github.kotlinmania.starlark.util

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

import io.github.kotlinmania.starlark.util.arcorstatic.ArcOrStatic

/** Wrapper for `Arc<str>`. */
class ArcStr private constructor(
    private val inner: ArcOrStatic<String>,
) : Comparable<ArcStr> {

    companion object {
        /** Create from static `str` without allocation. */
        fun newStatic(s: String): ArcStr {
            return ArcStr(ArcOrStatic.newStatic(s))
        }

        fun from(s: String): ArcStr {
            return if (s.isEmpty()) {
                ArcStr(ArcOrStatic.newStatic(""))
            } else {
                ArcStr(ArcOrStatic.newArc(s))
            }
        }
    }

    /** Get the `str`. */
    fun asStr(): String {
        return deref()
    }

    fun deref(): String {
        return inner.deref()
    }

    fun borrow(): String {
        return deref()
    }

    override fun toString(): String {
        return deref()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ArcStr) return false
        return inner == other.inner
    }

    override fun hashCode(): Int {
        return inner.hashCode()
    }

    override fun compareTo(other: ArcStr): Int {
        return inner.compareTo(other.inner)
    }
}
