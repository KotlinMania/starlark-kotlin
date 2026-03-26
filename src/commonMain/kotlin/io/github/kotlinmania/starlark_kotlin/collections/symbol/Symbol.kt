// port-lint: source src/collections/symbol/symbol.rs
package io.github.kotlinmania.starlark_kotlin.collections.symbol

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

import io.github.kotlinmania.starlark_kotlin.collections.aligned_padded_str.AlignedPaddedStr

/**
 * A pre-hashed string used for efficient dictionary lookup.
 *
 * In Rust, this stores the string as `Box<[usize]>` for word-aligned comparison.
 * In Kotlin, we delegate to the standard [String] implementation since the JVM
 * already optimizes string hashing and comparison.
 */
// #[derive(Clone, Trace, Allocative)]
// pub(crate) struct Symbol
internal class Symbol private constructor(
    /** Pre-computed hash. */
    private val hash: Long,
    /** The string value. */
    private val str: String,
    /** Small hash for SmallMap compatibility. */
    private val smallHashValue: Int,
) {
    // impl Symbol

    constructor(x: String) : this(
        hash = x.hashCode().toLong(),
        str = x,
        smallHashValue = x.hashCode(),
    )

    // pub(crate) fn new_hashed(x: Hashed<&str>) -> Self
    constructor(hashedStr: String, precomputedHash: Int) : this(
        hash = precomputedHash.toLong(),
        str = hashedStr,
        smallHashValue = precomputedHash,
    )

    /** Get the pre-computed hash. */
    // pub(crate) fn hash(&self) -> u64
    fun hash(): Long = hash

    /** Get the string value. */
    // pub(crate) fn as_str(&self) -> &str
    fun asStr(): String = str

    /** Get as aligned padded string for fast comparison. */
    // pub(crate) fn as_aligned_padded_str(&self) -> AlignedPaddedStr<'_>
    fun asAlignedPaddedStr(): AlignedPaddedStr = AlignedPaddedStr(str)

    /** Get the small hash value. */
    // pub(crate) fn small_hash(&self) -> StarlarkHashValue
    fun smallHash(): Int = smallHashValue

    // impl Debug for Symbol
    override fun toString(): String = str

    // impl PartialEq for Symbol
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Symbol) return false
        if (hash != other.hash) return false
        return str == other.str
    }

    // impl Eq for Symbol
    override fun hashCode(): Int = hash.toInt()
}
