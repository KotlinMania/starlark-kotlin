<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/values/types/string/DotFormat.kt
// port-lint: source values/types/string/dot_format.rs
package io.github.kotlinmania.starlark.values.types.string
=======
// port-lint: source src/values/types/string/dot_format.rs
package io.github.kotlinmania.starlark_kotlin.values.types.string
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/values/types/string/DotFormat.kt

import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.StringValue
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.str_.allocStrConcat3
import io.github.kotlinmania.starlark_kotlin.values.ValueError
import io.github.kotlinmania.starlark_kotlin.values.types.dict.Dict
import io.github.kotlinmania.starlark_kotlin.collections.StringPool

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

// Real types should be imported from their respective packages

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
internal fun formatOne(
    before: String,
    arg: Value,
    after: String,
    heap: Heap
): StringValue {
    return when (val argStr = StringValue.new(arg)) {
        null -> {
            val result = StringBuilder(before.length + after.length + 10)
            result.append(before)
            arg.collectRepr(result)
            result.append(after)
            StringValue.newUnchecked(heap.allocStr(result.toString()))
        }
        else -> heap.allocStrConcat3(before, argStr.toString(), after)
    }
}

/**
 * The format string can either have explicit indices,
 * or grab things sequentially, but not both.
 * FormatArgs knows which we are doing and keeps them in mind.
 */
private class FormatArgs<T : Iterator<Value>>(
    // Initially we have the iterator set and the args empty.
    // If we ever ask by index, we decant the iterator into args.
    private var iterator: T,
    private var args: MutableList<Value> = mutableListOf(),
    private var byIndex: Boolean = false,
    private var byOrder: Boolean = false
) {
    fun nextOrdered(): Result<Value> {
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

    fun byIndex(index: Int): Result<Value> {
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

internal fun format(
    thisString: String,
    args: Iterator<Value>,
    kwargs: Dict,
    stringPool: StringPool,
    heap: Heap
): Result<StringValue> = runCatching {
    val parser = FormatParser(thisString)
    val result = stringPool.alloc()
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

    val r = StringValue.newUnchecked(heap.allocStr(result.toString()))
    stringPool.release(result)
    r
}

private fun <T : Iterator<Value>> formatCapture(
    field: String,
    conv: FormatConv,
    args: FormatArgs<T>,
    kwargs: Dict,
    result: StringBuilder
): Result<Unit> = runCatching {
    val convS: (Value, StringBuilder) -> Unit = { x, r -> x.collectStr(r) }
    val convR: (Value, StringBuilder) -> Unit = { x, r -> x.collectRepr(r) }
    val convFn: (Value, StringBuilder) -> Unit = when (conv) {
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
