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

/**
 * Wrapper for `Arc<str>`.
 *
 * In Kotlin, [String] is already immutable and managed by the platform GC,
 * so `ArcStr` is a thin wrapper preserving the Rust API surface.
 */
// #[derive(Clone, Dupe, Eq, PartialEq, Hash, Ord, PartialOrd, Debug, derive_more::Display, Allocative)]
// #[display("{}", &**self)]
// pub struct ArcStr(ArcOrStatic<str>)
class ArcStr private constructor(
    private val inner: String,
) : Comparable<ArcStr> {

    // impl ArcStr

    // pub fn as_str(&self) -> &str
    /** Get the `str`. */
    fun asStr(): String {
        return inner
    }

    // impl Deref for ArcStr
    // Kotlin: no Deref. Use `asStr()` or `inner` directly.

    // impl Borrow<str> for ArcStr
    // Kotlin: no Borrow trait. Use `asStr()`.

    // impl Display for ArcStr
    // #[display("{}", &**self)]
    override fun toString(): String {
        return inner
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

    companion object {
        // pub fn new_static(s: &'static str) -> ArcStr
        /** Create from static `str` without allocation. */
        fun newStatic(s: String): ArcStr {
            return ArcStr(s)
        }

        // impl<'a> From<&'a str> for ArcStr
        fun from(s: String): ArcStr {
            // In Rust: if s.is_empty() => use static, else Arc::from(s)
            // In Kotlin: String is always managed, no distinction needed.
            return ArcStr(s)
        }
    }
}
