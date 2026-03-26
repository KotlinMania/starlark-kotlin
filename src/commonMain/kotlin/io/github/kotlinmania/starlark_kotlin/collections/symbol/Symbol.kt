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
import starlark_map.Hashed
import starlark_map.StarlarkHashValue

/** A pre-hashed string used for efficient dictionary lookup. */
internal class Symbol private constructor(
    private val hash: Long,
    private val len: Int,
    private val payload: LongArray,
    private val smallHash: StarlarkHashValue,
) {

    companion object {
        fun new(x: String): Symbol {
            return newHashed(Hashed.new(x))
        }

        fun newHashed(x: Hashed<String>): Symbol {
            val smallHash = x.hash()
            val hash = smallHash.promote()
            val len = x.key().length
            val lenWords = (len + Long.SIZE_BYTES - 1) / Long.SIZE_BYTES
            val payload = LongArray(lenWords) // 0 pad it at the end
            val bytes = x.key().encodeToByteArray()
            for (i in bytes.indices) {
                payload[i / Long.SIZE_BYTES] = payload[i / Long.SIZE_BYTES] or
                    (bytes[i].toLong() and 0xFF shl (i % Long.SIZE_BYTES * 8))
            }
            return Symbol(
                hash = hash.toLong(),
                len = len,
                payload = payload,
                smallHash = smallHash,
            )
        }
    }

    fun hash(): Long = hash

    fun asStr(): String {
        val s = ByteArray(len)
        for (i in 0 until len) {
            s[i] = (payload[i / Long.SIZE_BYTES] shr (i % Long.SIZE_BYTES * 8) and 0xFF).toByte()
        }
        return s.decodeToString()
    }

    fun asAlignedPaddedStr(): AlignedPaddedStr {
        return AlignedPaddedStr.new(len, payload)
    }

    fun asStrHashed(): Hashed<String> =
        Hashed.newUnchecked(smallHash, asStr())

    fun smallHash(): StarlarkHashValue = smallHash

    override fun toString(): String = asStr()

    override fun equals(other: Any?): Boolean {
        if (other !is Symbol) return false
        if (len != other.len) return false

        val p1 = payload
        val p2 = other.payload
        // Important to use the payload len, which is in Long units, rather than len which is in u8
        for (i in payload.indices) {
            if (p1[i] != p2[i]) return false
        }
        return true
    }

    override fun hashCode(): Int = hash.toInt()
}
