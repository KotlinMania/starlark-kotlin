// port-lint: source src/values/types/string/dot_format.rs
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

// Placeholder types until dependencies are ported
expect enum class FormatConv { Str, Repr }
expect enum class EscapeCurlyBrace { Open, Close; fun asStr(): String }
expect sealed class FormatToken {
    class Text(text: String) : FormatToken
    class Capture(capture: String, pos: Int, conv: FormatConv) : FormatToken
    class Escape(escape: EscapeCurlyBrace) : FormatToken
}
expect class FormatParser { fun next(): Result<FormatToken?> }
expect class StringPool { fun alloc(): String; fun release(s: String) }
expect class Heap<V> {
    fun allocStr(s: String): StringValue<V>
    fun allocStrConcat3(before: String, middle: String, after: String): StringValue<V>
}
expect class Value<V> {
    fun collectRepr(result: StringBuilder)
    fun collectStr(result: StringBuilder)
}
expect class StringValue<V> {
    companion object { fun <V> new(value: Value<V>): StringValue<V>? }
}
expect sealed class ValueError : Throwable {
    class IndexOutOfBound(index: Int) : ValueError
    class KeyNotFound(key: String) : ValueError
}
expect class Dict<V> { fun getStr(field: String): Value<V>? }

/**
 * Try parse `"aaa{}bbb"` and return `("aaa", "bbb")`.
 */
internal fun parseFormatOne(s: String): Pair<String, String>? {
    val parser = FormatParser(s)
    val before = StringBuilder(s.length)
    while (true) {
        val token = parser.next().getOrNull() ?: return null
        when (token) {
            null -> return null
            is FormatToken.Text -> before.append(token.text)
            is FormatToken.Escape -> before.append(token.escape.asStr())
            is FormatToken.Capture -> {
                if (token.capture == "" && token.conv == FormatConv.Str) {
                    break
                } else {
                    return null
                }
            }
        }
    }

    val after = StringBuilder(s.length - before.length)
    while (true) {
        val token = parser.next().getOrNull() ?: break
        when (token) {
            null -> break
            is FormatToken.Text -> after.append(token.text)
            is FormatToken.Escape -> after.append(token.escape.asStr())
            is FormatToken.Capture -> return null
        }
    }

    return Pair(before.toString(), after.toString())
}

/**
 * Evaluate `"<before>{}<after>".format(arg)`.
 */
internal fun <V> formatOne(
    before: String,
    arg: Value<V>,
    after: String,
    heap: Heap<V>
): StringValue<V> {
    return when (val argStr = StringValue.new(arg)) {
        null -> {
            val result = StringBuilder(before.length + after.length + 10)
            result.append(before)
            arg.collectRepr(result)
            result.append(after)
            heap.allocStr(result.toString())
        }
        else -> heap.allocStrConcat3(before, argStr.toString(), after)
    }
}

/**
 * The format string can either have explicit indices,
 * or grab things sequentially, but not both.
 * FormatArgs knows which we are doing and keeps them in mind.
 */
private class FormatArgs<V, T : Iterator<Value<V>>>(
    // Initially we have the iterator set and the args empty.
    // If we ever ask by index, we decant the iterator into args.
    private var iterator: T,
    private var args: MutableList<Value<V>> = mutableListOf(),
    private var byIndex: Boolean = false,
    private var byOrder: Boolean = false
) {
    fun nextOrdered(): Result<Value<V>> {
        return if (byIndex) {
            Result.failure(IllegalArgumentException(
                "Cannot mix manual field specification and automatic field numbering in format string"
            ))
        } else {
            byOrder = true
            if (iterator.hasNext()) {
                Result.success(iterator.next())
            } else {
                Result.failure(IllegalArgumentException("Not enough parameters in format string"))
            }
        }
    }

    fun byIndex(index: Int): Result<Value<V>> {
        return if (byOrder) {
            Result.failure(IllegalArgumentException(
                "Cannot mix manual field specification and automatic field numbering in format string"
            ))
        } else {
            if (!byIndex) {
                args.addAll(iterator.asSequence())
                byIndex = true
            }
            if (index < args.size) {
                Result.success(args[index])
            } else {
                Result.failure(ValueError.IndexOutOfBound(index))
            }
        }
    }
}

internal fun <V> format(
    thisString: String,
    args: Iterator<Value<V>>,
    kwargs: Dict<V>,
    stringPool: StringPool,
    heap: Heap<V>
): Result<StringValue<V>> = runCatching {
    val parser = FormatParser(thisString)
    val result = stringPool.alloc().let { StringBuilder() }
    val formatArgs = FormatArgs(args)

    while (true) {
        val token = parser.next().getOrThrow() ?: break
        when (token) {
            is FormatToken.Text -> result.append(token.text)
            is FormatToken.Escape -> result.append(token.escape.asStr())
            is FormatToken.Capture -> {
                formatCapture(token.capture, token.conv, formatArgs, kwargs, result).getOrThrow()
            }
        }
    }

    val r = heap.allocStr(result.toString())
    stringPool.release(result.toString())
    r
}

private fun <V, T : Iterator<Value<V>>> formatCapture(
    field: String,
    conv: FormatConv,
    args: FormatArgs<V, T>,
    kwargs: Dict<V>,
    result: StringBuilder
): Result<Unit> = runCatching {
    val convS: (Value<V>, StringBuilder) -> Unit = { x, r -> x.collectStr(r) }
    val convR: (Value<V>, StringBuilder) -> Unit = { x, r -> x.collectRepr(r) }
    val convFn: (Value<V>, StringBuilder) -> Unit = when (conv) {
        FormatConv.Str -> convS
        FormatConv.Repr -> convR
    }

    when {
        field.isEmpty() -> {
            convFn(args.nextOrdered().getOrThrow(), result)
        }
        field.all { it.isDigit() } -> {
            val i = field.toIntOrNull() ?: throw IllegalArgumentException(
                "Error parsing `$field` as a format string index"
            )
            convFn(args.byIndex(i).getOrThrow(), result)
        }
        else -> {
            val invalidChar = field.firstOrNull { c ->
                c in listOf('.', ',', '[', ']')
            }
            if (invalidChar != null) {
                throw IllegalArgumentException(
                    "Invalid character '$invalidChar' inside replacement field"
                )
            }
            val v = kwargs.getStr(field) ?: throw ValueError.KeyNotFound(field)
            convFn(v, result)
        }
    }
}
