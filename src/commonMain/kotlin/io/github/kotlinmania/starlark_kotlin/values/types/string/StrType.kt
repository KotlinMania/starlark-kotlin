// port-lint: source src/values/types/string/str_type.rs
package io.github.kotlinmania.starlark_kotlin.values.types.string

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

import kotlin.math.max
import io.github.kotlinmania.starlark_kotlin.values.types.list.None
import io.github.kotlinmania.starlark_kotlin.values.owned.unpackStr
import io.github.kotlinmania.starlark_kotlin.analysis.Other
import io.github.kotlinmania.starlark_kotlin.values.unsupportedWith
import io.github.kotlinmania.starlark_kotlin.values.unsupported
import io.github.kotlinmania.starlark_kotlin.values.types.tuple.it
import io.github.kotlinmania.starlark_kotlin.values.types.list.haystack
import io.github.kotlinmania.starlark_kotlin.values.owned.unpackI32
import io.github.kotlinmania.starlark_kotlin.values.hash
import io.github.kotlinmania.starlark_kotlin.tests.str
import io.github.kotlinmania.starlark_kotlin.tests.derive.module.repr
import io.github.kotlinmania.starlark_kotlin.analysis.body

/**
 * The result of calling `type()` on strings.
 */
const val STRING_TYPE: String = "string"

/**
 * Internal structure for StarlarkStr with generic size parameter.
 *
 * Holds a lazily-initialized cached hash code, the length in bytes,
 * and the body data in a word-aligned array.
 */
internal class StarlarkStrN<N>(
    val n: Int
) {
    // Lazily-initialized cached hash code.
    val hash = atomic(0)

    // Length in bytes.
    var len: Int = 0

    // Followed by an unsized block, meaning this type is unsized.
    // But we can't mark it as such since we really want &StarlarkStr to
    // take up only one word.
    val body: Array<Long> = Array(n) { 0L }
}

/**
 * A pointer to this type represents a Starlark string.
 * Use of this type is discouraged and not considered stable.
 */
class StarlarkStr internal constructor() {
    internal val str: StarlarkStrN<0> = StarlarkStrN(0)

    private var actualBody: ByteArray? = null

    companion object {
        /**
         * Hash value when hash field is not initialized.
         */
        const val UNINIT_HASH: Int = 0

        /**
         * Used in `const_frozen_string!` macro, so it is public.
         */
        fun payloadLenForLen(len: Int): Int {
            return (len + Long.SIZE_BYTES - 1) / Long.SIZE_BYTES
        }

        /**
         * Unsafe because if you do `unpack` on this it will blow up.
         */
        @Suppress("FunctionName")
        internal fun new(len: Int, hash: Int): StarlarkStr {
            require(len.toLong() and 0xFFFFFFFF00000000L == 0L) { "len overflow" }
            val result = StarlarkStr()
            result.str.hash.set(hash)
            result.str.len = len
            return result
        }

        /**
         * Format a Rust string like `repr(s)`.
         */
        fun repr(s: String): String {
            val buffer = StringBuilder()
            stringRepr(s, buffer)
            return buffer.toString()
        }
    }
}

/**
 * Freeze implementation for StarlarkStrN.
 */
internal fun <N> freezeStarlarkStrN(value: StarlarkStrN<N>, freezer: Freezer): Result<StarlarkStrN<N>> {
    return Result.success(value)
}

/**
 * Freeze implementation for StarlarkStr.
 */
internal fun freezeStarlarkStr(value: StarlarkStr, freezer: Freezer): Result<StarlarkStr> {
    return Result.success(value)
}

/**
 * Deref implementation - returns the string content as a Kotlin String.
 */
internal fun StarlarkStr.asStr(): String {
    if (actualBody == null) {
        return ""
    }
    return actualBody!!.decodeToString(0, len())
}

/**
 * PartialEq implementation.
 */
internal fun StarlarkStr.equals(other: StarlarkStr): Boolean {
    return this.asAlignedPaddedStr() == other.asAlignedPaddedStr()
}

/**
 * Ord implementation.
 */
internal fun StarlarkStr.compareTo(other: StarlarkStr): Int {
    return this.asStr().compareTo(other.asStr())
}

/**
 * Debug implementation.
 */
internal fun StarlarkStr.debugFormat(): String {
    return this.asStr()
}

/**
 * Get a Rust string reference from this Starlark string.
 */
fun StarlarkStr.asStr(): String {
    if (actualBody == null) {
        return ""
    }
    return String(actualBody!!, 0, len())
}

internal fun StarlarkStr.asAlignedPaddedStr(): AlignedPaddedStr {
    return AlignedPaddedStr.new(len(), str.body)
}

/**
 * Get cached hash value or compute if it is not cached yet.
 */
fun StarlarkStr.getHash(): StarlarkHashValue {
    // Note relaxed load and store are practically non-locking memory operations.
    val hash = str.hash.get()
    if (hash != 0) {
        return StarlarkHashValue.newUnchecked(hash)
    } else {
        val s = StarlarkHasher()
        hashStringValue(asStr(), s)
        val newHash = s.finishSmall()
        // If hash is zero, we are unlucky, but it is highly improbable.
        str.hash.set(newHash.get())
        return newHash
    }
}

/**
 * Rust string reference along with its hash value.
 */
fun StarlarkStr.asStrHashed(): Hashed<String> {
    return Hashed.newUnchecked(getHash(), asStr())
}

/**
 * String length, in bytes.
 */
fun StarlarkStr.len(): Int {
    return str.len
}

/**
 * Is this string empty?
 */
fun StarlarkStr.isEmpty(): Boolean {
    return str.len == 0
}

internal fun StarlarkStr.offsetOfContent(): Int {
    // In Kotlin, we approximate the memory offset concept
    // This would be the offset of the body field in the layout
    return 8 // hash (4 bytes) + len (4 bytes)
}

/**
 * How to hash a string in a way that is compatible with Value.
 */
internal fun hashStringValue(x: String, state: StarlarkHasher) {
    state.write(x.toByteArray())
}

/**
 * Display implementation for StarlarkStr.
 */
internal fun StarlarkStr.display(): String {
    // We could either accumulate straight into the buffer (can't preallocate, virtual call on each character)
    // or accumulate into a String buffer first. Not sure which is faster, but string buffer lets us
    // share code with collect_repr more easily.
    return StarlarkStr.repr(asStr())
}

internal fun strMethods(): Methods? {
    return MethodsStatic.methods { stringMethods(it) }
}

/**
 * StarlarkValue implementation for StarlarkStr.
 */
internal interface StarlarkStrValue {
    fun isSpecial(private: Private): Boolean {
        return true
    }

    fun getMethods(): Methods? {
        return strMethods()
    }

    fun collectRepr(self: StarlarkStr, buffer: StringBuilder) {
        // String repr() is quite hot, so optimise it
        stringRepr(self.asStr(), buffer)
    }

    fun toBool(self: StarlarkStr): Boolean {
        return !self.isEmpty()
    }

    fun writeHash(self: StarlarkStr, hasher: StarlarkHasher): Result<Unit> {
        // Don't defer to str because we cache the Hash in StarlarkStr
        hasher.writeU32(self.getHash().get())
        return Result.success(Unit)
    }

    fun getHash(self: StarlarkStr, private: Private): Result<StarlarkHashValue> {
        return Result.success(self.getHash())
    }

    fun equals(self: StarlarkStr, other: Value): Result<Boolean> {
        val otherStr = other.unpackStarlarkStr()
        return if (otherStr != null) {
            Result.success(self == otherStr)
        } else {
            Result.success(false)
        }
    }

    fun compare(self: StarlarkStr, other: Value): Result<Int> {
        val otherStr = other.unpackStr()
        return if (otherStr != null) {
            Result.success(self.asStr().compareTo(otherStr))
        } else {
            ValueError.unsupportedWith(self, "cmp()", other)
        }
    }

    fun at(self: StarlarkStr, index: Value, heap: Heap): Result<Value> {
        // This method is disturbingly hot. Use the logic from `convert_index`,
        // but modified to be UTF8 string friendly.
        val i = index.unpackI32() ?: return ValueError.invalidIndex(index)

        if (i >= 0) {
            val c = fastString.at(self.asStr(), CharIndex(i))
            return if (c != null) {
                Result.success(c.toString().allocStringValue(heap))
            } else {
                Result.failure(ValueError.IndexOutOfBound(i))
            }
        } else {
            val lenChars = fastString.len(self.asStr())
            val ind = CharIndex(-i) // Index from the end, minimum of 1
            if (ind > lenChars) {
                Result.failure(ValueError.IndexOutOfBound(i))
            } else if (lenChars.value == self.len()) {
                // We are a 7bit ASCII string, so take the fast-path
                val bytes = self.asStr().toByteArray()
                Result.success((bytes[(lenChars - ind).value].toInt().toChar()).toString().allocStringValue(heap))
            } else {
                val c = fastString.at(self.asStr(), lenChars - ind)
                Result.success(c.toString().allocStringValue(heap))
            }
        }
    }

    fun length(self: StarlarkStr): Result<Int> {
        return Result.success(fastString.len(self.asStr()).value)
    }

    fun isIn(self: StarlarkStr, other: Value): Result<Boolean> {
        val s = other.unpackStr() ?: return ValueError.unsupported("in", other)
        return Result.success(fastString.contains(self.asStr(), s))
    }

    fun slice(
        self: StarlarkStr,
        start: Value?,
        stop: Value?,
        stride: Value?,
        heap: Heap
    ): Result<Value> {
        val s = self.asStr()

        if (stride != null && stride.unpackI32() != 1) {
            // The stride case is super rare and super complex, so let's do something inefficient but safe
            val xs = s.map { it }.toList()
            val result = applySlice(xs, start, stop, stride)
            return result.map { it.joinToString("").allocStringValue(heap) }
        }

        fun startStopToNoneOr(v: Value?): Result<NoneOr<Int>> {
            return if (v == null) {
                Result.success(NoneOr.None())
            } else {
                val i = v.unpackI32()
                if (i != null) {
                    Result.success(NoneOr.Other(i))
                } else {
                    Result.failure(ValueError.InvalidType(v, "int"))
                }
            }
        }

        val startResult = startStopToNoneOr(start)
        val stopResult = startStopToNoneOr(stop)

        if (startResult.isFailure) return startResult.map { null!! }
        if (stopResult.isFailure) return stopResult.map { null!! }

        val startNone = startResult.getOrThrow()
        val stopNone = stopResult.getOrThrow()

        val indices = fastString.convertStrIndices(s, startNone.intoOption(), stopNone.intoOption())
        return if (indices != null) {
            Result.success(heap.allocStr(indices.haystack).toValue())
        } else {
            Result.success(heap.allocStr("").toValue())
        }
    }

    fun add(self: StarlarkStr, other: Value, heap: Heap): Result<Value>? {
        val otherStr = other.unpackStr()
        return if (otherStr != null) {
            if (self.isEmpty()) {
                Result.success(other)
            } else {
                Result.success(heap.allocStrConcat(self.asStr(), otherStr).toValue())
            }
        } else {
            null
        }
    }

    fun mul(self: StarlarkStr, other: Value, heap: Heap): Result<Value>? {
        val l = other.unpackI32()
        if (l == null) {
            return null
        }

        val result = StringBuilder(self.len() * max(0, l))
        repeat(l) {
            result.append(self.asStr())
        }
        return Result.success(result.toString().allocStringValue(heap))
    }

    fun rmul(self: StarlarkStr, lhs: Value, heap: Heap): Result<Value>? {
        return mul(self, lhs, heap)
    }

    fun percent(self: StarlarkStr, other: Value, heap: Heap): Result<Value> {
        return interpolation.percent(self.asStr(), other).map { it.allocStringValue(heap) }
    }

    fun typecheckerTy(self: StarlarkStr): Ty? {
        return Ty.starlarkValue<StarlarkStr>()
    }
}

/**
 * Serialize implementation for StarlarkStr.
 */
internal fun StarlarkStr.serialize(serializer: Serializer): Result<Unit> {
    return serializer.serializeStr(asStr())
}

// Real types should be imported from their respective packages
