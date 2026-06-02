// port-lint: source src/values/types/string/str_type.rs
package io.github.kotlinmania.starlark.values.types.string

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

import io.github.kotlinmania.starlark.collections.StarlarkHashValue
import io.github.kotlinmania.starlark.collections.StarlarkHasher
import io.github.kotlinmania.starlark.environment.Methods
import io.github.kotlinmania.starlark.environment.MethodsStatic
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.ValueError
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.typed.StarlarkStr
import kotlin.math.max

/**
 * The result of calling `type()` on strings.
 */
const val STRING_TYPE: String = "string"

/**
 * How to hash a string in a way that is compatible with Value.
 */
internal fun hashStringValue(x: String, state: StarlarkHasher) {
    state.write(x.encodeToByteArray())
}

internal fun strMethods(): Methods = STR_METHODS_STATIC.methods(::stringMethods)

private val STR_METHODS_STATIC = MethodsStatic()

/**
 * StarlarkValue-like operations on StarlarkStr.
 *
 * These are the implementations of Starlark operations for string values,
 * mirroring the `#[starlark_value]` impl block in the Rust source.
 */

internal fun starlarkStrCollectRepr(self: StarlarkStr, buffer: StringBuilder) {
    // String repr() is quite hot, so optimise it
    stringRepr(self.asStr(), buffer)
}

internal fun starlarkStrToBool(self: StarlarkStr): Boolean = self.asStr().isNotEmpty()

internal fun starlarkStrWriteHash(self: StarlarkStr, hasher: StarlarkHasher): Result<Unit> {
    // Don't defer to str because we cache the Hash in StarlarkStr
    val hashValue = StarlarkHashValue.new(self.asStr())
    hasher.writeU32(hashValue.get())
    return Result.success(Unit)
}

internal fun starlarkStrGetHash(self: StarlarkStr): Result<StarlarkHashValue> = Result.success(StarlarkHashValue.new(self.asStr()))

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
    // This method is disturbingly hot. Use the logic from `convert_index`,
    // but modified to be UTF8 string friendly.
    val i =
        index.unpackI32() ?: return Result.failure(
            ValueError.IncorrectParameterType,
        )

    val s = self.asStr()
    val chars = s.toList()
    val lenChars = chars.size

    if (i >= 0) {
        if (i >= lenChars) {
            return Result.failure(ValueError.IndexOutOfBound(i))
        }
        return Result.success(heap.allocStr(chars[i].toString()))
    } else {
        val ind = -i // Index from the end, minimum of 1
        if (ind > lenChars) {
            return Result.failure(ValueError.IndexOutOfBound(i))
        }
        return Result.success(heap.allocStr(chars[lenChars - ind].toString()))
    }
}

internal fun starlarkStrLength(self: StarlarkStr): Result<Int> {
    // In Starlark, len() returns the number of Unicode codepoints, not bytes
    return Result.success(self.asStr().length)
}

internal fun starlarkStrIsIn(self: StarlarkStr, other: Value): Result<Boolean> {
    val s = other.unpackStr() ?: return ValueError.unsupportedWith("string", "in", other)
    return Result.success(s.contains(self.asStr()))
}

internal fun starlarkStrSlice(
    self: StarlarkStr,
    start: Value?,
    stop: Value?,
    stride: Value?,
    heap: Heap,
): Result<Value> {
    val s = self.asStr()
    val chars = s.toList()
    val len = chars.size

    // Handle stride case
    if (stride != null && stride.unpackI32() != 1) {
        val strideVal =
            stride.unpackI32()
                ?: return Result.failure(ValueError.IncorrectParameterType)
        val startVal = start?.unpackI32()
        val stopVal = stop?.unpackI32()

        val indices = sliceIndices(len, startVal, stopVal, strideVal)
        val result = StringBuilder()
        for (i in indices) {
            result.append(chars[i])
        }
        return Result.success(heap.allocStr(result.toString()))
    }

    // No stride (or stride == 1)
    val startNone: Int? =
        start?.let {
            it.unpackI32() ?: return Result.failure(ValueError.IncorrectParameterType)
        }
    val stopNone: Int? =
        stop?.let {
            it.unpackI32() ?: return Result.failure(ValueError.IncorrectParameterType)
        }

    val startIdx = clampIndex(startNone ?: 0, len)
    val stopIdx = clampIndex(stopNone ?: len, len)

    return if (startIdx < stopIdx) {
        val slice = chars.subList(startIdx, stopIdx).joinToString("")
        Result.success(heap.allocStr(slice))
    } else {
        Result.success(heap.allocStr(""))
    }
}

internal fun starlarkStrAdd(self: StarlarkStr, other: Value, heap: Heap): Result<Value>? {
    val otherStr = other.unpackStr()
    return if (otherStr != null) {
        if (self.asStr().isEmpty()) {
            Result.success(other)
        } else {
            Result.success(heap.allocStr(self.asStr() + otherStr))
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
    return Result.success(heap.allocStr(result.toString()))
}

internal fun starlarkStrRmul(self: StarlarkStr, lhs: Value, heap: Heap): Result<Value>? = starlarkStrMul(self, lhs, heap)

internal fun starlarkStrPercent(self: StarlarkStr, other: Value, heap: Heap): Result<Value> = percent(self.asStr(), other).map { heap.allocStr(it) }

internal fun starlarkStrIsSpecial(): Boolean = true

internal fun starlarkStrGetMethods(): Methods? = strMethods()

internal fun starlarkStrTypecheckerTy(self: StarlarkStr): Ty = Ty.string()

internal fun starlarkStrSerialize(self: StarlarkStr): String = self.asStr()

/**
 * Format a string like `repr(s)`.
 */
fun starlarkStrRepr(s: String): String {
    val buffer = StringBuilder()
    stringRepr(s, buffer)
    return buffer.toString()
}

// ---- Helpers ----

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
