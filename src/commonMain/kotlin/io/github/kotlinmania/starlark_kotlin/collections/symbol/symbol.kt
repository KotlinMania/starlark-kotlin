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

// Placeholder types referenced from other modules
// These will be replaced with real imports as the port progresses
class StarlarkHashValue(val value: Int)

class Hashed<T>(val hash: StarlarkHashValue, val key: T) {
    companion object {
        fun <T> new(value: T): Hashed<T> {
            return Hashed(StarlarkHashValue(value.hashCode()), value)
        }
        fun <T> newUnchecked(hash: StarlarkHashValue, value: T): Hashed<T> {
            return Hashed(hash, value)
        }
    }
}

class AlignedPaddedStr(val str: String)

/// A pre-hashed string used for efficient dictionary lookup.
internal class Symbol private constructor(
    private val hash: Long,
    private val len: Int,
    // In Rust this is Box<[usize]> for word-aligned payload.
    // In Kotlin we use the string directly.
    private val payload: String,
    private val smallHash: StarlarkHashValue,
) {

    companion object {
        fun new(x: String): Symbol {
            return newHashed(Hashed.new(x))
        }

        fun newHashed(x: Hashed<String>): Symbol {
            val smallHash = x.hash
            val hash = smallHash.value.toLong()
            val len = x.key.length
            return Symbol(
                hash = hash,
                len = len,
                payload = x.key,
                smallHash = smallHash,
            )
        }
    }

    fun hash(): Long = hash

    fun asStr(): String = payload

    fun asAlignedPaddedStr(): AlignedPaddedStr {
        return AlignedPaddedStr(payload)
    }

    fun asStrHashed(): Hashed<String> {
        return Hashed.newUnchecked(smallHash, asStr())
    }

    fun smallHash(): StarlarkHashValue = smallHash

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Symbol) return false
        if (len != other.len) return false
        return payload == other.payload
    }

    override fun hashCode(): Int = hash.toInt()

    override fun toString(): String = asStr()
}
