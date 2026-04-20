// port-lint: source src/values/layout/static_string.rs
package io.github.kotlinmania.starlark_kotlin.values.layout

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

import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark_kotlin.values.types.string.StarlarkStr
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.AValueHeader
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.AValueRepr
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.str_.VALUE_STR_A_VALUE_PTR
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.str_.allocStrIntern
import io.github.kotlinmania.starlark_kotlin.values.starlark_type_id.StarlarkTypeId

/**
 * Internal string representation with packed body bytes.
 * In Rust, this is `StarlarkStrN<const N: usize>` with const generics for the body size.
 * In Kotlin, we use a dynamic LongArray.
 */
internal class StarlarkStrN(
    val len: Int,
    val hash: UInt,
    val body: LongArray,
)

/**
 * A constant string that can be converted to a [FrozenValue].
 *
 * In the Rust original, `N` is the length in words and `#[repr(C)]` ensures
 * the struct layout matches the heap representation. In Kotlin, we hold the
 * `AValueRepr<StarlarkStrN>` directly and rely on the runtime representation.
 */
class StarlarkStrNRepr internal constructor(
    private val repr: AValueRepr<StarlarkStrN>,
) {
    companion object {
        /** Create a new [StarlarkStrNRepr] given a string of length greater than 1. */
        fun new(s: String): StarlarkStrNRepr {
            require(s.length > 1) {
                "static strings of length <= 1 cannot be created from outside of the crate"
            }
            return newUnchecked(s)
        }

        internal fun newUnchecked(s: String): StarlarkStrNRepr {
            val payloadLen = StarlarkStr.payloadLenForLen(s.length)
            // Pack the string bytes into word-sized payload array.
            // This mirrors the Rust compile-time byte packing into usize words.
            val wordSize = ULong.SIZE_BYTES // sizeof(usize) equivalent
            val payload = LongArray(payloadLen)
            for (i in s.indices) {
                val wordIdx = i / wordSize
                val shift = 8 * (i % wordSize)
                payload[wordIdx] = payload[wordIdx] or
                    ((s[i].code.toLong() and 0xFF) shl shift)
            }

            // In Rust, VALUE_STR_A_VALUE_PTR is a static vtable; the StarlarkStr lives
            // in the arena bytes after the header.  In Kotlin there is no raw memory so
            // we need the AValueHeader's vtable to carry the actual StarlarkStr so that
            // `Value.unpackStarlarkStr()` can find it via `getRef().downcastRef<StarlarkStr>()`.
            val str = StarlarkStr(s)
            val typeId = ConstTypeId.of<StarlarkStr>()
            val header = AValueHeader(
                AValueVTable(
                    staticTypeOfValue = typeId,
                    starlarkTypeId = StarlarkTypeId.fromTypeId(typeId),
                    typeName = "string",
                    isStr = true,
                    memorySizeFn = { _ ->
                        val byteLen = str.len()
                        ValueAllocSize.new(
                            AlignedSize.alignUp(StarlarkStr.offsetOfContent() + byteLen)
                        )
                    },
                    heapFreezeFn = { _, freezer ->
                        // Static constant strings: re-intern on the frozen heap.
                        val fv = freezer.frozenHeap().allocStrIntern(str.asStr())
                        Result.success(fv.toFrozenValue())
                    },
                    heapCopyFn = { _, tracer ->
                        tracer.allocStr(str.asStr())
                    },
                    starlarkValue = str,
                )
            )

            return StarlarkStrNRepr(
                repr = AValueRepr(
                    header = header,
                    payload = StarlarkStrN(
                        len = s.length,
                        hash = 0u,
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

/**
 * Returns a cached [FrozenStringValue] for strings of length <= 1,
 * or null for longer strings.
 */
fun constantString(x: String): FrozenStringValue? {
    return if (x.length > 1) {
        null
    } else if (x.isEmpty()) {
        VALUE_EMPTY_STRING.erase()
    } else {
        // If the string is 1 byte long there can only be up to the first 128 characters present
        // therefore this index will be total
        val b = x[0].code
        if (b < 128) {
            VALUE_BYTE_STRINGS[b].erase()
        } else {
            null
        }
    }
}

internal val VALUE_BYTE_STRINGS: Array<StarlarkStrNRepr> = Array(128) { i ->
    StarlarkStrNRepr.newUnchecked(i.toChar().toString())
}
