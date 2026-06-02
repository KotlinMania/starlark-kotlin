// port-lint: source src/collections/symbol/symbol.rs
package io.github.kotlinmania.starlark.collections.symbol

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

// use std::fmt;
// use std::fmt::Debug;
// use std::intrinsics::copy_nonoverlapping;
// use std::mem;
// use std::slice;
// use std::str;

// use allocative::Allocative;
// use starlark_derive::Trace;
// use io.github.kotlinmania.starlark.collections::Hashed;
// use io.github.kotlinmania.starlark.collections::StarlarkHashValue;

import io.github.kotlinmania.starlark.Coerce
import io.github.kotlinmania.starlark.collections.Hashed
import io.github.kotlinmania.starlark.collections.StarlarkHashValue
import io.github.kotlinmania.starlark.collections.alignedpaddedstr.AlignedPaddedStr
import io.github.kotlinmania.starlark.eval.runtime.ArgSymbol
import io.github.kotlinmania.starlark.eval.runtime.params.spec.ParametersSpec

// use crate as starlark;
// use crate::coerce::Coerce;
// use crate::collections::aligned_padded_str::AlignedPaddedStr;

/**
 * A pre-hashed string used for efficient dictionary lookup.
 */
// #[derive(Clone, Trace, Allocative)]
// pub(crate) struct Symbol {
//     hash: u64,
//     len: u32,
//     payload: Box<[usize]>,
//     small_hash: StarlarkHashValue,
// }
class Symbol private constructor(
    private val hash: ULong,
    private val len: UInt,
    private val payload: LongArray,
    private val smallHash: StarlarkHashValue,
) : Coerce<Symbol>,
    ArgSymbol {
    // unsafe impl Coerce<Symbol> for Symbol {}

    // impl Debug for Symbol {
    //     fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
    //         self.as_str().fmt(f)
    //     }
    // }
    override fun toString(): String = asStr()

    // impl PartialEq for Symbol {
    //     fn eq(&self, other: &Self) -> bool {
    override fun equals(other: Any?): Boolean {
        if (other !is Symbol) return false
        // if self.len != other.len {
        //     return false;
        // }
        if (this.len != other.len) {
            return false
        }

        val p1 = this.payload
        val p2 = other.payload
        // Important to use the payload len, which is in u64 units, rather than len which is in u8
        // for i in 0..self.payload.len() {
        for (i in 0 until this.payload.size) {
            // Safe because we checked the lengths at the start
            // if unsafe { p1.get_unchecked(i) != p2.get_unchecked(i) } {
            if (p1[i] != p2[i]) {
                return false
            }
        }
        // true
        return true
    }

    // impl Eq for Symbol {}
    override fun hashCode(): Int = hash.toInt()

    // pub(crate) fn new(x: &str) -> Self {
    //     Self::new_hashed(Hashed::new(x))
    // }

    // pub(crate) fn new_hashed(x: Hashed<&str>) -> Self {
    //     ...
    // }

    // #[inline]
    // pub(crate) fn hash(&self) -> u64 {
    //     self.hash
    // }
    internal fun hash(): ULong = hash

    // pub(crate) fn as_str(&self) -> &str {
    //     // All safe because we promise we started out with a str
    //     unsafe {
    //         let s = slice::from_raw_parts(self.payload.as_ptr() as *const u8, self.len as usize);
    //         str::from_utf8_unchecked(s)
    //     }
    // }
    internal fun asStr(): String {
        // All safe because we promise we started out with a str
        val bytes = ByteArray(len.toInt())
        for (i in 0 until len.toInt()) {
            bytes[i] = (payload[i / Long.SIZE_BYTES] shr (i % Long.SIZE_BYTES * 8)).toByte()
        }
        return bytes.decodeToString()
    }

    // #[inline]
    // pub(crate) fn as_aligned_padded_str(&self) -> AlignedPaddedStr<'_> {
    //     unsafe { AlignedPaddedStr::new(self.len as usize, self.payload.as_ptr()) }
    // }
    internal fun asAlignedPaddedStr(): AlignedPaddedStr =
        AlignedPaddedStr.new(len = len.toInt(), data = payload)

    // pub(crate) fn as_str_hashed(&self) -> Hashed<&str> {
    //     Hashed::new_unchecked(self.small_hash, self.as_str())
    // }
    internal fun asStrHashed(): Hashed<String> =
        Hashed.newUnchecked(smallHash, asStr())

    // pub(crate) fn small_hash(&self) -> StarlarkHashValue {
    //     self.small_hash
    // }
    // Also satisfies ArgSymbol::small_hash
    override fun smallHash(): StarlarkHashValue = smallHash

    // impl ArgSymbol for Symbol (from arguments.rs)
    // fn get_index_from_param_spec<'v, V: ValueLike<'v>>(&self, ps: &ParametersSpec<V>) -> Option<usize>
    override fun <V> getIndexFromParamSpec(ps: ParametersSpec<V>): Int? = ps.names.get(this)?.toInt()

    companion object {
        // pub(crate) fn new(x: &str) -> Self {
        //     Self::new_hashed(Hashed::new(x))
        // }
        internal fun new(x: String): Symbol = newHashed(Hashed.new(x))

        // pub(crate) fn new_hashed(x: Hashed<&str>) -> Self {
        internal fun newHashed(x: Hashed<String>): Symbol {
            // let small_hash = x.hash();
            val smallHash = x.hash()
            // let hash = small_hash.promote();
            val hash = smallHash.promote()
            // let len = x.key().len();
            val len = x.key().length
            // let len_words = len.div_ceil(mem::size_of::<usize>());
            val lenWords = (len + Long.SIZE_BYTES - 1) / Long.SIZE_BYTES
            // let mut payload = vec![0; len_words]; // 0 pad it at the end
            val payload = LongArray(lenWords) // 0 pad it at the end
            // unsafe {
            //     copy_nonoverlapping(x.key().as_ptr(), payload.as_mut_ptr() as *mut u8, len);
            // }
            val bytes = x.key().encodeToByteArray()
            for (i in 0 until len) {
                payload[i / Long.SIZE_BYTES] = payload[i / Long.SIZE_BYTES] or
                    (bytes[i].toLong() and 0xFF shl (i % Long.SIZE_BYTES * 8))
            }
            // Self { hash, len: len.try_into().unwrap(), payload: payload.into_boxed_slice(), small_hash }
            return Symbol(
                hash = hash,
                len = len.toUInt(),
                payload = payload,
                smallHash = smallHash,
            )
        }
    }
}
