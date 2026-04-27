// port-lint: source src/values/layout/staticString.rs
package io.github.kotlinmania.starlark.values.layout

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

/** Statically allocated strings. */

import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.avalues.str.VALUE_STR_A_VALUE_PTR
import io.github.kotlinmania.starlark.values.layout.heap.AValueRepr
import io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark.values.types.string.StarlarkStr
import kotlin.concurrent.atomics.AtomicInt

/**
 * Internal string representation with packed body words.
 */
internal class StarlarkStrN(
    val len: UInt,
    val hash: AtomicInt,
    val body: LongArray,
)

/**
 * A constant string that can be converted to a [FrozenValue].
 *
 * **Note** the body length is in words, not bytes.
 */
class StarlarkStrNRepr internal constructor(
    private val repr: AValueRepr<StarlarkStrN>,
) {
    companion object {
        /**
         * Create a new [StarlarkStrNRepr] given a string of length greater than 1.
         *
         * This function is used internally by [constantString] and friends to statically
         * allocate strings.
         */
        fun new(s: String): StarlarkStrNRepr {
            require(s.length > 1) {
                "static strings of length <= 1 cannot be created from outside of the crate"
            }
            return newUnchecked(s)
        }

        internal fun newUnchecked(s: String): StarlarkStrNRepr {
            val payloadLen = StarlarkStr.payloadLenForLen(s.length)
            require(s.length.toUInt().toInt() == s.length)
            val payload = LongArray(payloadLen)

            val wordSize = ULong.SIZE_BYTES
            var i = 0
            while (i != s.length) {
                payload[i / wordSize] = payload[i / wordSize] or
                    ((s[i].code.toLong() and 0xFF) shl (8 * (i % wordSize)))
                i += 1
            }

            return StarlarkStrNRepr(
                repr = AValueRepr(
                    header = VALUE_STR_A_VALUE_PTR,
                    payload = StarlarkStrN(
                        len = s.length.toUInt(),
                        hash = AtomicInt(0),
                        body = payload,
                    ),
                ),
            )
        }
    }

    /** Obtain the [FrozenValue] for a [StarlarkStrNRepr]. */
    fun unpack(): FrozenValue {
        return FrozenValue.newPtr(repr.header, true)
    }

    /** Erase the type parameter, giving a slightly nicer user experience. */
    fun erase(): FrozenStringValue {
        return FrozenStringValue.newUnchecked(unpack())
    }
}

internal val VALUE_EMPTY_STRING: StarlarkStrNRepr = StarlarkStrNRepr.newUnchecked("")

/** For static strings of length > 1, allocate via [StarlarkStrNRepr] directly. */
fun constantString(x: String): FrozenStringValue? {
    return if (x.length > 1) {
        null
    } else if (x.isEmpty()) {
        VALUE_EMPTY_STRING.erase()
    } else {
        // If the string is 1 byte long there can only be up to the first 128 characters present
        // therefore this index will be total
        VALUE_BYTE_STRINGS[x[0].code].erase()
    }
}

internal val VALUE_BYTE_STRINGS: Array<StarlarkStrNRepr> = Array(128) { i ->
    StarlarkStrNRepr.newUnchecked(i.toChar().toString())
}
