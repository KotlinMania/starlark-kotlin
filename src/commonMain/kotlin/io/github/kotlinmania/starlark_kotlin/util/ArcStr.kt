// port-lint: source src/util/arc_str.rs
package io.github.kotlinmania.starlark_kotlin.util

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

import io.github.kotlinmania.starlark_kotlin.util.arc_or_static.ArcOrStatic

/** Wrapper for `Arc<str>`. */
// #[derive(Clone, Dupe, Eq, PartialEq, Hash, Ord, PartialOrd, Debug, derive_more::Display, Allocative)]
// #[display("{}", &**self)]
// pub struct ArcStr(ArcOrStatic<str>)
class ArcStr private constructor(
    private val inner: ArcOrStatic<String>,
) : Comparable<ArcStr> {
    // impl ArcStr

    // pub fn new_static(s: &'static str) -> ArcStr
    /** Create from static `str` without allocation. */
    companion object {
        fun newStatic(s: String): ArcStr {
            return ArcStr(ArcOrStatic.newStatic(s))
        }

        // impl<'a> From<&'a str> for ArcStr
        fun from(s: String): ArcStr {
            return if (s.isEmpty()) {
                ArcStr(ArcOrStatic.newStatic(""))
            } else {
                ArcStr(ArcOrStatic.newArc(s))
            }
        }
    }

    // pub fn as_str(&self) -> &str
    /** Get the `str`. */
    fun asStr(): String {
        return deref()
    }

    // impl Deref for ArcStr
    fun deref(): String {
        return inner.deref()
    }

    // impl Borrow<str> for ArcStr
    fun borrow(): String {
        return deref()
    }

    // impl Display for ArcStr
    // #[display("{}", &**self)]
    override fun toString(): String {
        return deref()
    }

    // impl PartialEq for ArcStr (via ArcOrStatic)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ArcStr) return false
        return inner == other.inner
    }

    // impl Hash for ArcStr (via ArcOrStatic)
    override fun hashCode(): Int {
        return inner.hashCode()
    }

    // impl Ord for ArcStr (via ArcOrStatic)
    override fun compareTo(other: ArcStr): Int {
        return inner.compareTo(other.inner)
    }
}
