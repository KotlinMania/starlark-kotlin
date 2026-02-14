// port-lint: source src/eval/bc/if_debug.rs
package io.github.kotlinmania.starlark_kotlin.eval.bc.if_debug

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

/// Utility to make stateful debug assertions easier.

/// Store `T` if debug assertions enabled, ZST otherwise.
///
/// This object is easier to work with than explicitly marking code with
/// `#[cfg(debug_assertions)]`: cfg attributes are not easy to write
/// without mistakes where code works, and no warnings in both debug and release.
///
/// In other words, this type converts cfg to types.
///
/// This type implements `equals` which always returns `true`,
/// and `compareTo` which always returns 0 (Equal), so
/// * this type is easy to include in structs which do derives
/// * behavior of this object does not depend on `<T>`
/// * behavior does not depend on whether debugging assertions enabled or not
// #[derive(Debug, Default, Copy, Clone, Dupe)]
// pub(crate) struct IfDebug<T> {
//     #[cfg(debug_assertions)] value: T,
//     _marker: marker::PhantomData<T>,
// }
// Kotlin: no cfg-based conditional compilation; always store the value.
internal class IfDebug<T> private constructor(
    private val value: T?,
) : Comparable<IfDebug<T>> {

    companion object {
        // Kotlin: DEBUG flag simulates #[cfg(debug_assertions)].
        // Always true in Kotlin (no zero-cost release stripping).
        private const val DEBUG: Boolean = true
    }

    // pub(crate) fn new(value: T) -> IfDebug<T>
    /// Store a value if debug assertions enabled, drop otherwise.
    constructor(value: T) : this(if (DEBUG) value else null)

    // pub(crate) fn new_if_debug(init: impl FnOnce() -> T) -> IfDebug<T>
    /// Store a value if debug assertions enabled, drop otherwise.
    constructor(init: () -> T) : this(if (DEBUG) init() else null)

    // pub(crate) fn get_ref(&self) -> Option<&T>
    /// Get a reference to stored value if assertions enabled, `null` otherwise.
    fun getRef(): T? {
        return value
    }

    // pub(crate) fn get_ref_if_debug(&self) -> &T
    /// Get a reference to stored value if assertions enabled, panic otherwise.
    fun getRefIfDebug(): T {
        return getRef() ?: error("assertions disabled")
    }

    // pub(crate) fn if_debug(&self, f: impl FnOnce(&T))
    /// Invoke a function if debug enabled.
    fun ifDebug(f: (T) -> Unit) {
        val v = getRef()
        if (v != null) {
            f(v)
        }
    }

    // impl PartialEq for IfDebug<T>
    // fn eq(&self, _other: &Self) -> bool { true }
    override fun equals(other: Any?): Boolean {
        if (other !is IfDebug<*>) return false
        return true
    }

    // impl Hash — consistent with equals
    override fun hashCode(): Int = 0

    // impl Ord for IfDebug<T>
    // fn cmp(&self, _other: &Self) -> Ordering { Ordering::Equal }
    override fun compareTo(other: IfDebug<T>): Int = 0

    // impl Debug for IfDebug<T>
    override fun toString(): String = "IfDebug($value)"
}
