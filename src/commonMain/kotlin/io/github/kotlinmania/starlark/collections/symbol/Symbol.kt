// port-lint: source src/collections/symbol/symbol.rs
package io.github.kotlinmania.starlark.collections.symbol

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

import io.github.kotlinmania.starlark.Coerce
import io.github.kotlinmania.starlark.collections.alignedpaddedstr.AlignedPaddedStr
import io.github.kotlinmania.starlark.eval.runtime.ArgSymbol
import io.github.kotlinmania.starlark.eval.runtime.params.spec.ParametersSpec
import starlarkmap.Hashed
import starlarkmap.StarlarkHashValue

/**
 * A pre-hashed string used for efficient dictionary lookup.
 */
class Symbol private constructor(
    private val hash: ULong,
    private val len: UInt,
    private val payload: LongArray,
    private val smallHash: StarlarkHashValue,
) : Coerce<Symbol>, ArgSymbol {

    override fun toString(): String = asStr()

    override fun equals(other: Any?): Boolean {
        if (other !is Symbol) return false
        if (this.len != other.len) {
            return false
        }

        val p1 = this.payload
        val p2 = other.payload
        // Important to import the payload len, which is in u64 units, rather than len which is in u8
        for (i in 0 until this.payload.size) {
            // Safe because we checked the lengths at the start
            if (p1[i] != p2[i]) {
                return false
            }
        }
        return true
    }

    override fun hashCode(): Int = hash.toInt()

    internal fun hash(): ULong = hash

    internal fun asStr(): String {
        // All safe because we promise we started out with a str
        val bytes = ByteArray(len.toInt())
        for (i in 0 until len.toInt()) {
            bytes[i] = (payload[i / Long.SIZE_BYTES] shr (i % Long.SIZE_BYTES * 8)).toByte()
        }
        return bytes.decodeToString()
    }

    internal fun asAlignedPaddedStr(): AlignedPaddedStr =
        AlignedPaddedStr.new(len = len.toInt(), data = payload)

    internal fun asStrHashed(): Hashed<String> =
        Hashed.newUnchecked(smallHash, asStr())

    // Also satisfies ArgSymbol::smallHash
    override fun smallHash(): StarlarkHashValue = smallHash

    override fun <V> getIndexFromParamSpec(ps: ParametersSpec<V>): Int? {
        return ps.names.get(this)?.toInt()
    }

    companion object {
        internal fun new(x: String): Symbol = newHashed(Hashed.new(x))

        internal fun newHashed(x: Hashed<String>): Symbol {
            val smallHash = x.hash()
            val hash = smallHash.promote()
            val len = x.key().length
            val lenWords = (len + Long.SIZE_BYTES - 1) / Long.SIZE_BYTES
            val payload = LongArray(lenWords) // 0 pad it at the end
            // Copy character bytes into the word-aligned payload.
            val bytes = x.key().encodeToByteArray()
            for (i in 0 until len) {
                payload[i / Long.SIZE_BYTES] = payload[i / Long.SIZE_BYTES] or
                    (bytes[i].toLong() and 0xFF shl (i % Long.SIZE_BYTES * 8))
            }
            return Symbol(
                hash = hash,
                len = len.toUInt(),
                payload = payload,
                smallHash = smallHash,
            )
        }
    }
}
