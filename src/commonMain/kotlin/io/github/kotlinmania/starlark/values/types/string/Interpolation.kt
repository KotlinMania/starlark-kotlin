// port-lint: source src/values/types/string/interpolation.rs
package io.github.kotlinmania.starlark.values.types.string

import io.github.kotlinmania.starlark.values.ValueError
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.str.allocStrConcat3
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.typed.StringValue
import io.github.kotlinmania.starlark.values.types.float.StarlarkFloat
import io.github.kotlinmania.starlark.values.types.float.writeCompact
import io.github.kotlinmania.starlark.values.types.float.writeDecimal
import io.github.kotlinmania.starlark.values.types.float.writeScientific
import io.github.kotlinmania.starlark.values.types.int.StarlarkIntRef
import io.github.kotlinmania.starlark.values.types.num.NumRef
import io.github.kotlinmania.starlark.values.types.tuple.Tuple
import io.github.kotlinmania.starlark.values.types.tuple.fromValue
import kotlin.math.truncate

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

/**
 * String interpolation-related code.
 * Based on https://docs.python.org/3/library/stdtypes.html#printf-style-string-formatting
 */

// `i32::abs(i32::MIN)` panics as `i32::MIN` has no corresponding
// positive value that fits inside `i32`. For this edge case,
// let's just hardcode the results.
private const val I32_MIN_OCTAL: String = "-20000000000"
private const val I32_MIN_HEX: String = "-80000000"

/**
 * Operator `%` format or evaluation errors
 */
sealed class StringInterpolationError(
    message: String,
) : Exception(message) {
    class TooManyParameters : StringInterpolationError("Too many arguments for format string")

    class NotEnoughParameters : StringInterpolationError("Not enough arguments for format string")

    class IncompleteFormat : StringInterpolationError("Incomplete format")

    class UnsupportedFormatCharacter(
        char: Char,
    ) : StringInterpolationError("Unsupported format character: '$char'")

    class ExpectingFormatCharacter : StringInterpolationError("Expecting format character (internal error)")
}

sealed class PercentSFormat {
    /** `%s`. */
    object Str : PercentSFormat()

    /** `%r`. */
    object Repr : PercentSFormat()

    /** `%d`. */
    object Dec : PercentSFormat()

    /** `%o`. */
    object Oct : PercentSFormat()

    /** `%x`. */
    object Hex : PercentSFormat()

    /** `%X`. */
    object HexUpper : PercentSFormat()

    /** `%e`. */
    object Exp : PercentSFormat()

    /** `%E`. */
    object ExpUpper : PercentSFormat()

    /** `%f` or `%F`. */
    object Float : PercentSFormat()

    /** `%g`. */
    object FloatCompact : PercentSFormat()

    /** `%G`. */
    object FloatCompactUpper : PercentSFormat()
}

private data class Item(
    val literal: String,
    val format: PercentSFormat?,
)

private class PercentFormatParser(
    private var rem: String,
) : Iterator<Result<Item>> {
    override fun hasNext(): Boolean = rem.isNotEmpty()

    @Suppress("ReturnCount")
    override fun next(): Result<Item> {
        val indexOfPercent = rem.indexOfFirst { it == '%' }
        if (indexOfPercent >= 0) {
            val prevRem = rem
            val literal = rem.substring(0, indexOfPercent)
            val remAfterPercent = rem.substring(indexOfPercent)

            if (remAfterPercent.length < 2) {
                return Result.failure(StringInterpolationError.IncompleteFormat())
            }

            val f = remAfterPercent[1]
            val res: Item =
                when (f) {
                    '%' -> {
                        // Include the percent in the literal.
                        val literalWithPercent = prevRem.substring(0, indexOfPercent + 1)
                        Item(literalWithPercent, null)
                    }
                    's' -> Item(literal, PercentSFormat.Str)
                    'r' -> Item(literal, PercentSFormat.Repr)
                    'd' -> Item(literal, PercentSFormat.Dec)
                    'o' -> Item(literal, PercentSFormat.Oct)
                    'x' -> Item(literal, PercentSFormat.Hex)
                    'X' -> Item(literal, PercentSFormat.HexUpper)
                    'e' -> Item(literal, PercentSFormat.Exp)
                    'E' -> Item(literal, PercentSFormat.ExpUpper)
                    'f', 'F' -> Item(literal, PercentSFormat.Float)
                    'g' -> Item(literal, PercentSFormat.FloatCompact)
                    'G' -> Item(literal, PercentSFormat.FloatCompactUpper)
                    else -> {
                        // Note we need to find the second character, not the second byte.
                        val chars = remAfterPercent.iterator()
                        chars.next() // skip '%'
                        val c =
                            if (chars.hasNext()) {
                                chars.next()
                            } else {
                                return Result.failure(StringInterpolationError.ExpectingFormatCharacter())
                            }
                        return Result.failure(StringInterpolationError.UnsupportedFormatCharacter(c))
                    }
                }
            // We reach here only if format character is ASCII,
            // so we can safely skip 2 bytes.
            rem = remAfterPercent.substring(2)
            return Result.success(res)
        } else {
            if (rem.isEmpty()) {
                throw NoSuchElementException()
            } else {
                val literal = rem
                rem = ""
                return Result.success(Item(literal, null))
            }
        }
    }
}

fun percent(format: String, value: Value): Result<String> {
    // NOTE(nga): user could reuse `Evaluator::string_pool` here, but
    //   * we don't have access to `Evaluator` in `StarlarkValue::percent`
    //   * after single %s made intrinsic, this code is not that hot now

    // random guess as a baseline capacity
    val res = StringBuilder(format.length + 20)

    val tuple = Tuple.fromValue(value)
    val one = listOf(value)
    val values: List<Value> =
        when (tuple) {
            null -> one
            else -> tuple.content()
        }
    var valueIndex = 0

    fun nextValue(): Result<Value> =
        if (valueIndex < values.size) {
            Result.success(values[valueIndex++])
        } else {
            Result.failure(StringInterpolationError.NotEnoughParameters())
        }

    // because of the way format is defined, we can deal with it as bytes
    val parser = PercentFormatParser(format)
    while (parser.hasNext()) {
        val itemResult = parser.next()
        val item = itemResult.getOrElse { return Result.failure(it) }
        res.append(item.literal)
        when (item.format) {
            null -> {}
            PercentSFormat.Str -> {
                val arg = nextValue().getOrElse { return Result.failure(it) }
                val str = arg.unpackStr()
                if (str == null) {
                    arg.collectRepr(res)
                } else {
                    res.append(str)
                }
            }
            PercentSFormat.Repr -> {
                val arg = nextValue().getOrElse { return Result.failure(it) }
                arg.collectRepr(res)
            }
            PercentSFormat.Dec -> {
                val v = nextValue().getOrElse { return Result.failure(it) }
                when (val num = v.unpackNum()) {
                    is NumRef.Int -> {
                        when (val intRef = num.value) {
                            is StarlarkIntRef.Small -> {
                                res.append(intRef.value.toI32())
                            }
                            is StarlarkIntRef.Big -> {
                                res.append(intRef.value.get())
                            }
                        }
                    }
                    is NumRef.Float -> {
                        val truncated = NumRef.Float(StarlarkFloat(truncate(num.value.value)))
                        val asInt = truncated.asInt()
                        if (asInt != null) {
                            res.append(asInt)
                        } else {
                            return ValueError.unsupportedType(v, "format(%d)")
                        }
                    }
                    null -> {
                        return ValueError.unsupportedType(v, "format(%d)")
                    }
                }
            }
            PercentSFormat.Oct -> {
                val v = nextValue().getOrElse { return Result.failure(it) }
                when (val num = v.unpackNum()) {
                    is NumRef.Int -> {
                        when (val intRef = num.value) {
                            is StarlarkIntRef.Small -> {
                                val vi = intRef.value.toI32()
                                val vp = kotlin.math.abs(vi)
                                if (vi == Int.MIN_VALUE) {
                                    res.append(I32_MIN_OCTAL)
                                } else {
                                    if (vi < 0) res.append("-")
                                    res.append(vp.toString(8))
                                }
                            }
                            is StarlarkIntRef.Big -> {
                                val bigInt = intRef.value.get()
                                if (bigInt.signum() < 0) res.append("-")
                                res.append(bigInt.abs().toString(8))
                            }
                        }
                    }
                    is NumRef.Float, null -> {
                        return ValueError.unsupportedType(v, "format(%o)")
                    }
                }
            }
            PercentSFormat.Hex -> {
                val v = nextValue().getOrElse { return Result.failure(it) }
                when (val num = v.unpackNum()) {
                    is NumRef.Int -> {
                        when (val intRef = num.value) {
                            is StarlarkIntRef.Small -> {
                                val vi = intRef.value.toI32()
                                val vp = kotlin.math.abs(vi)
                                if (vi == Int.MIN_VALUE) {
                                    res.append(I32_MIN_HEX)
                                } else {
                                    if (vi < 0) res.append("-")
                                    res.append(vp.toString(16))
                                }
                            }
                            is StarlarkIntRef.Big -> {
                                val bigInt = intRef.value.get()
                                if (bigInt.signum() < 0) res.append("-")
                                res.append(bigInt.abs().toString(16))
                            }
                        }
                    }
                    is NumRef.Float, null -> {
                        return ValueError.unsupportedType(v, "format(%x)")
                    }
                }
            }
            PercentSFormat.HexUpper -> {
                val v = nextValue().getOrElse { return Result.failure(it) }
                when (val num = v.unpackNum()) {
                    is NumRef.Int -> {
                        when (val intRef = num.value) {
                            is StarlarkIntRef.Small -> {
                                val vi = intRef.value.toI32()
                                val vp = kotlin.math.abs(vi)
                                if (vi == Int.MIN_VALUE) {
                                    res.append(I32_MIN_HEX.uppercase())
                                } else {
                                    if (vi < 0) res.append("-")
                                    res.append(vp.toString(16).uppercase())
                                }
                            }
                            is StarlarkIntRef.Big -> {
                                val bigInt = intRef.value.get()
                                if (bigInt.signum() < 0) res.append("-")
                                res.append(bigInt.abs().toString(16).uppercase())
                            }
                        }
                    }
                    is NumRef.Float, null -> {
                        return ValueError.unsupportedType(v, "format(%X)")
                    }
                }
            }
            PercentSFormat.Exp -> {
                val v = nextValue().getOrElse { return Result.failure(it) }
                val numRef = NumRef.unpackParam(v).getOrElse { return Result.failure(it) }
                writeScientific(res, numRef.asFloat(), 'e', false)
            }
            PercentSFormat.ExpUpper -> {
                val v = nextValue().getOrElse { return Result.failure(it) }
                val numRef = NumRef.unpackParam(v).getOrElse { return Result.failure(it) }
                writeScientific(res, numRef.asFloat(), 'E', false)
            }
            PercentSFormat.Float -> {
                val v = nextValue().getOrElse { return Result.failure(it) }
                val numRef = NumRef.unpackParam(v).getOrElse { return Result.failure(it) }
                writeDecimal(res, numRef.asFloat())
            }
            PercentSFormat.FloatCompact -> {
                val v = nextValue().getOrElse { return Result.failure(it) }
                val numRef = NumRef.unpackParam(v).getOrElse { return Result.failure(it) }
                writeCompact(res, numRef.asFloat(), 'e')
            }
            PercentSFormat.FloatCompactUpper -> {
                val v = nextValue().getOrElse { return Result.failure(it) }
                val numRef = NumRef.unpackParam(v).getOrElse { return Result.failure(it) }
                writeCompact(res, numRef.asFloat(), 'E')
            }
        }
    }

    return if (valueIndex < values.size) {
        Result.failure(StringInterpolationError.TooManyParameters())
    } else {
        Result.success(res.toString())
    }
}

/**
 * Try parse `"aaa%sbbb"` and return `("aaa", "bbb")`.
 */
fun parsePercentSOne(format: String): Pair<String, String>? {
    val before = StringBuilder(format.length)
    val chars = format.iterator()

    while (chars.hasNext()) {
        when (val c = chars.next()) {
            '%' -> {
                if (!chars.hasNext()) return null
                when (chars.next()) {
                    '%' -> before.append('%')
                    's' -> break
                    else -> return null
                }
            }
            else -> before.append(c)
        }
    }

    val after = StringBuilder(format.length - before.length)
    while (chars.hasNext()) {
        when (val c = chars.next()) {
            '%' -> {
                if (!chars.hasNext()) return null
                when (chars.next()) {
                    '%' -> after.append('%')
                    else -> return null
                }
            }
            else -> after.append(c)
        }
    }

    return Pair(before.toString(), after.toString())
}

/**
 * Evaluate `"<before>%s<after>" % arg`.
 */
fun percentSOne(
    before: String,
    arg: Value,
    after: String,
    heap: Heap,
): Result<StringValue> {
    val strValue = StringValue.new(arg)
    return if (strValue != null) {
        Result.success(heap.allocStrConcat3(before, strValue.toString(), after))
    } else {
        val one =
            when (val tuple = Tuple.fromValue(arg)) {
                null -> arg
                else -> {
                    val content = tuple.content()
                    when {
                        content.isEmpty() -> {
                            return Result.failure(StringInterpolationError.NotEnoughParameters())
                        }
                        content.size == 1 -> content[0]
                        else -> {
                            return Result.failure(StringInterpolationError.TooManyParameters())
                        }
                    }
                }
            }
        Result.success(formatOne(before, one, after, heap))
    }
}
