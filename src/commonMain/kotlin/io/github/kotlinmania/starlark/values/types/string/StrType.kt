// port-lint: source src/values/types/string/strType.rs
package io.github.kotlinmania.starlark.values.types.string

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

import kotlin.math.max
import starlarkmap.Hashed
import starlarkmap.StarlarkHashValue
import starlarkmap.StarlarkHasher
import io.github.kotlinmania.starlark.collections.alignedpaddedstr.AlignedPaddedStr
import io.github.kotlinmania.starlark.environment.Methods
import io.github.kotlinmania.starlark.environment.MethodsStatic
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.ValueError
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.toValue
import io.github.kotlinmania.starlark.values.types.none.NoneOr
import io.github.kotlinmania.starlark.values.StarlarkValue
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * The result of calling `type()` on strings.
 */
const val STRING_TYPE: String = "string"

/**
 * A pointer to this type represents a Starlark string.
 *
 * cached hash and a byte length field. Kotlin cannot model the unsized layout directly in common
 * code, so this is a value wrapper with equivalent semantics exposed through [StarlarkValue].
 */
class StarlarkStr(
    private val s: String,
) : Comparable<StarlarkStr>, StarlarkValue {

    @OptIn(ExperimentalAtomicApi::class)
    private val cachedHash: AtomicInt = AtomicInt(0)

    fun asStr(): String = s

    fun len(): Int = s.encodeToByteArray().size

    fun isEmpty(): Boolean = s.isEmpty()

    internal fun asAlignedPaddedStr(): AlignedPaddedStr {
        val bytes = s.encodeToByteArray()
        val lenBytes = bytes.size
        val words = payloadLenForLen(lenBytes)
        val data = LongArray(words)
        var i = 0
        var wordIndex = 0
        while (i < lenBytes) {
            var w = 0L
            var shift = 0
            var j = 0
            while (j < 8 && i < lenBytes) {
                w = w or ((bytes[i].toLong() and 0xffL) shl shift)
                shift += 8
                j += 1
                i += 1
            }
            data[wordIndex] = w
            wordIndex += 1
        }
        return AlignedPaddedStr.new(lenBytes, data)
    }

    /**
     *
     * Kotlin: this is an internal, non-`Result` accessor used to implement [StarlarkValue] hashing.
     */
    @OptIn(ExperimentalAtomicApi::class)
    fun getHashValue(): StarlarkHashValue {
        val h = cachedHash.load()
        if (h != 0) {
            return StarlarkHashValue.newUnchecked(h.toUInt())
        }

        val hasher = StarlarkHasher()
        hashStringValue(s, hasher)
        val hash = hasher.finishSmall()
        cachedHash.store(hash.get().toInt())
        return hash
    }

    fun asStrHashed(): Hashed<String> {
        return Hashed.newUnchecked(getHashValue(), s)
    }

    override fun compareTo(other: StarlarkStr): Int {
        return s.compareTo(other.s)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StarlarkStr) return false
        return asAlignedPaddedStr() == other.asAlignedPaddedStr()
    }

    override fun hashCode(): Int = s.hashCode()

    override val TYPE: String get() = STRING_TYPE

    override fun isSpecial(): Boolean = starlarkStrIsSpecial()

    override fun getMethods(): Methods? = starlarkStrGetMethods()

    override fun collectRepr(collector: StringBuilder) {
        starlarkStrCollectRepr(this, collector)
    }

    override fun toBool(): Boolean = starlarkStrToBool(this)

    override fun writeHash(hasher: StarlarkHasher): Result<Unit> = starlarkStrWriteHash(this, hasher)

    override fun getHash(): Result<StarlarkHashValue> = starlarkStrGetHash(this)

    override fun equals(other: Value): Result<Boolean> = starlarkStrEquals(this, other)

    override fun compare(other: Value): Result<Int> = starlarkStrCompare(this, other)

    override fun at(index: Value, heap: Heap): Result<Value> = starlarkStrAt(this, index, heap)

    override fun length(): Result<Int> = starlarkStrLength(this)

    override fun isIn(other: Value): Result<Boolean> = starlarkStrIsIn(this, other)

    override fun slice(start: Value?, stop: Value?, stride: Value?, heap: Heap): Result<Value> =
        starlarkStrSlice(this, start, stop, stride, heap)

    override fun add(other: Value, heap: Heap): Result<Value>? = starlarkStrAdd(this, other, heap)

    override fun mul(other: Value, heap: Heap): Result<Value>? = starlarkStrMul(this, other, heap)

    override fun rmul(lhs: Value, heap: Heap): Result<Value>? = starlarkStrRmul(this, lhs, heap)

    override fun percent(other: Value, heap: Heap): Result<Value> = starlarkStrPercent(this, other, heap)

    override fun typecheckerTy(): Ty? = starlarkStrTypecheckerTy(this)

    override fun toString(): String = starlarkStrRepr(s)

    companion object {
        val UNINIT_HASH: StarlarkHashValue get() = StarlarkHashValue.newUnchecked(0u)

        fun payloadLenForLen(len: Int): Int = (len + 7) / 8

        fun offsetOfContent(): Int = 8

        fun repr(s: String): String = starlarkStrRepr(s)
    }
}

/**
 * How to hash a string in a way that is compatible with Value.
 */
internal fun hashStringValue(x: String, state: StarlarkHasher) {
    state.write(x.encodeToByteArray())
}

internal fun strMethods(): Methods? {
    return STR_METHODS_STATIC.methods(::stringMethods)
}

private val STR_METHODS_STATIC = MethodsStatic()

/**
 * StarlarkValue-like operations on StarlarkStr.
 *
 * These are the implementations of Starlark operations for string values,
 */

internal fun starlarkStrCollectRepr(self: StarlarkStr, buffer: StringBuilder) {
    // String repr() is quite hot, so optimise it
    stringRepr(self.asStr(), buffer)
}

internal fun starlarkStrToBool(self: StarlarkStr): Boolean {
    return self.asStr().isNotEmpty()
}

internal fun starlarkStrWriteHash(self: StarlarkStr, hasher: StarlarkHasher): Result<Unit> {
    // Don't defer to str because we cache the Hash in StarlarkStr
    hasher.writeU32(self.getHashValue().get())
    return Result.success(Unit)
}

internal fun starlarkStrGetHash(self: StarlarkStr): Result<StarlarkHashValue> {
    return Result.success(self.getHashValue())
}

internal fun starlarkStrEquals(self: StarlarkStr, other: Value): Result<Boolean> {
    val otherStr = other.unpackStarlarkStr()
    return if (otherStr != null) {
        Result.success(self == otherStr)
    } else {
        Result.success(false)
    }
}

internal fun starlarkStrCompare(self: StarlarkStr, other: Value): Result<Int> {
    val otherStr = other.unpackStr()
    return if (otherStr != null) {
        Result.success(self.asStr().compareTo(otherStr))
    } else {
        ValueError.unsupportedWith(self.asStr(), "cmp()", other)
    }
}

internal fun starlarkStrAt(self: StarlarkStr, index: Value, heap: Heap): Result<Value> {
    // This method is disturbingly hot. Use the logic from `convertIndex`,
    // but modified to be UTF8 string friendly.
    val i = index.unpackI32() ?: return Result.failure(
        ValueError.IncorrectParameterType
    )

    val s = self.asStr()
    val lenChars = codePointCount(s)

    if (i >= 0) {
        val cpIndex = i
        if (cpIndex >= lenChars) {
            return Result.failure(ValueError.IndexOutOfBound(i))
        }
        val cp = codePointSubstringAt(s, cpIndex)!!
        return Result.success(heap.allocStr(cp).toValue())
    }

    val ind = -i // Index from the end, minimum of 1
    if (ind > lenChars) {
        return Result.failure(ValueError.IndexOutOfBound(i))
    }
    val cpIndex = lenChars - ind
    val cp = codePointSubstringAt(s, cpIndex)!!
    return Result.success(heap.allocStr(cp).toValue())
}

internal fun starlarkStrLength(self: StarlarkStr): Result<Int> {
    // In Starlark, len() returns the number of Unicode codepoints, not bytes
    return Result.success(codePointCount(self.asStr()))
}

internal fun starlarkStrIsIn(self: StarlarkStr, other: Value): Result<Boolean> {
    val needle = other.unpackStr() ?: return ValueError.unsupportedWith(STRING_TYPE, "in", other)
    return Result.success(self.asStr().contains(needle))
}

internal fun starlarkStrSlice(
    self: StarlarkStr,
    start: Value?,
    stop: Value?,
    stride: Value?,
    heap: Heap,
): Result<Value> {
    val s = self.asStr()
    val len = codePointCount(s)

    // Handle stride case
    if (stride != null && stride.unpackI32() != 1) {
        val strideVal = stride.unpackI32()
            ?: return Result.failure(ValueError.IncorrectParameterType)
        val startVal = start?.unpackI32()
        val stopVal = stop?.unpackI32()

        val indices = sliceIndices(len, startVal, stopVal, strideVal)
        val result = StringBuilder()
        for (i in indices) {
            result.append(codePointSubstringAt(s, i)!!)
        }
        return Result.success(heap.allocStr(result.toString()).toValue())
    }

    // No stride (or stride == 1)
    fun startStopToNoneOr(v: Value?): Result<NoneOr<Int>> {
        return when (v) {
            null -> Result.success(NoneOr.None)
            else -> {
                val i = v.unpackI32() ?: return Result.failure(ValueError.IncorrectParameterType)
                Result.success(NoneOr.Other(i))
            }
        }
    }

    val startNoneOr = startStopToNoneOr(start).getOrElse { return Result.failure(it) }
    val stopNoneOr = startStopToNoneOr(stop).getOrElse { return Result.failure(it) }

    val (startCp, stopCp) = convertStrIndices(
        s = s,
        start = startNoneOr.intoOption(),
        stop = stopNoneOr.intoOption(),
    ) ?: return Result.success(heap.allocStr("").toValue())

    val startUtf16 = utf16IndexForCodePointIndex(s, startCp)
    val stopUtf16 = utf16IndexForCodePointIndex(s, stopCp)
    return Result.success(heap.allocStr(s.substring(startUtf16, stopUtf16)).toValue())
}

internal fun starlarkStrAdd(self: StarlarkStr, other: Value, heap: Heap): Result<Value>? {
    val otherStr = other.unpackStr()
    return if (otherStr != null) {
        if (self.asStr().isEmpty()) {
            Result.success(other)
        } else {
            Result.success(heap.allocStr(self.asStr() + otherStr).toValue())
        }
    } else {
        null
    }
}

internal fun starlarkStrMul(self: StarlarkStr, other: Value, heap: Heap): Result<Value>? {
    val l = other.unpackI32() ?: return null

    val result = StringBuilder(self.asStr().length * max(0, l))
    repeat(max(0, l)) {
        result.append(self.asStr())
    }
    return Result.success(heap.allocStr(result.toString()).toValue())
}

internal fun starlarkStrRmul(self: StarlarkStr, lhs: Value, heap: Heap): Result<Value>? {
    return starlarkStrMul(self, lhs, heap)
}

internal fun starlarkStrPercent(self: StarlarkStr, other: Value, heap: Heap): Result<Value> {
    return percent(self.asStr(), other).map { heap.allocStr(it).toValue() }
}

internal fun starlarkStrIsSpecial(): Boolean {
    return true
}

internal fun starlarkStrGetMethods(): Methods? {
    return strMethods()
}

internal fun starlarkStrTypecheckerTy(self: StarlarkStr): Ty? {
    return Ty.string()
}

internal fun starlarkStrSerialize(self: StarlarkStr): String {
    return self.asStr()
}

/**
 * Format a string like `repr(s)`.
 */
fun starlarkStrRepr(s: String): String {
    val buffer = StringBuilder()
    stringRepr(s, buffer)
    return buffer.toString()
}

// ---- Helpers ----

private fun codePointCount(s: String): Int {
    var count = 0
    var i = 0
    while (i < s.length) {
        val c = s[i]
        if (c.isHighSurrogate() && i + 1 < s.length && s[i + 1].isLowSurrogate()) {
            i += 2
        } else {
            i += 1
        }
        count += 1
    }
    return count
}

/** Return a substring which contains exactly one Unicode code point at [codePointIndex]. */
private fun codePointSubstringAt(s: String, codePointIndex: Int): String? {
    if (codePointIndex < 0) return null
    var cp = 0
    var i = 0
    while (i < s.length) {
        if (cp == codePointIndex) {
            val c = s[i]
            return if (c.isHighSurrogate() && i + 1 < s.length && s[i + 1].isLowSurrogate()) {
                s.substring(i, i + 2)
            } else {
                s.substring(i, i + 1)
            }
        }
        val c = s[i]
        i += if (c.isHighSurrogate() && i + 1 < s.length && s[i + 1].isLowSurrogate()) 2 else 1
        cp += 1
    }
    return null
}

private fun utf16IndexForCodePointIndex(s: String, codePointIndex: Int): Int {
    var cp = 0
    var i = 0
    while (i < s.length && cp < codePointIndex) {
        val c = s[i]
        i += if (c.isHighSurrogate() && i + 1 < s.length && s[i + 1].isLowSurrogate()) 2 else 1
        cp += 1
    }
    return i
}

private fun convertStrIndices(s: String, start: Int?, stop: Int?): Pair<Int, Int>? {
    val len = codePointCount(s)
    val startCp = clampIndex(start ?: 0, len)
    val stopCp = clampIndex(stop ?: len, len)
    return if (startCp < stopCp) Pair(startCp, stopCp) else null
}

/** Clamp an index into range [0, len]. Negative values count from end. */
private fun clampIndex(index: Int, len: Int): Int {
    val adjusted = if (index < 0) len + index else index
    return adjusted.coerceIn(0, len)
}

/** Compute the indices for a slice with stride. */
private fun sliceIndices(len: Int, start: Int?, stop: Int?, stride: Int): List<Int> {
    if (stride == 0) return emptyList()

    val defStart = if (stride > 0) 0 else len - 1
    val defStop = if (stride > 0) len else -(len + 1)

    val s = clampSliceIndex(start ?: defStart, len, stride > 0)
    val e = clampSliceIndex(stop ?: defStop, len, stride > 0)

    val result = mutableListOf<Int>()
    var i = s
    if (stride > 0) {
        while (i < e) {
            result.add(i)
            i += stride
        }
    } else {
        while (i > e) {
            result.add(i)
            i += stride
        }
    }
    return result
}

/** Clamp a slice index, handling negative values. */
private fun clampSliceIndex(index: Int, len: Int, positive: Boolean): Int {
    val adjusted = if (index < 0) len + index else index
    return if (positive) {
        adjusted.coerceIn(0, len)
    } else {
        adjusted.coerceIn(-1, len - 1)
    }
}
