// port-lint: source src/values/types/string/globals.rs
package io.github.kotlinmania.starlark.values.types.string
import io.github.kotlinmania.starlark.environment.GlobalsBuilder

import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.eval.runtime.optionalPositional
import io.github.kotlinmania.starlark.eval.runtime.positional
import io.github.kotlinmania.starlark.values.toValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.typed.StringValue
import io.github.kotlinmania.starlark.values.types.bigint.allocValue


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


/**
 * Register string-related global functions.
 *
 * The macro in Rust generates code to register these globals; in Kotlin, we
 * implement this explicitly as a regular function.
 */
internal fun registerStr(globals: GlobalsBuilder) {
    // chr(i): returns a string encoding a codepoint
    globals.setFunction("chr") { callArgs, eval ->
        val a: Value = callArgs.positional(0)
        val i = a.unpackI32() ?: throw IllegalArgumentException(
            "chr() argument must be an integer, got ${a.getType()}"
        )
        val c = chr(i).getOrThrow()
        eval.heap().allocStr(c.toString())
    }

    // ord(s): returns the codepoint of a character
    globals.setFunction("ord") { callArgs, eval ->
        val a: Value = callArgs.positional(0)
        val sv = StringValue.new(a)
            ?: throw IllegalArgumentException("ord: expected a string argument")
        val result = ord(sv).getOrThrow()
        result.allocValue(eval.heap())
    }

    // repr(a): formats its argument as a string
    globals.setFunction("repr") { callArgs, eval ->
        val a: Value = callArgs.positional(0)
        repr(a, eval).getOrThrow().toValue()
    }

    // str(a): formats its argument as a string (without quotation for strings)
    globals.setFunction("str") { callArgs, eval ->
        val a: Value? = callArgs.optionalPositional(0)
        if (a == null) {
            // str() with no args returns empty string
            eval.heap().allocStr("")
        } else {
            str(a, eval).getOrThrow().toValue()
        }
    }
}

/**
 * [chr](https://github.com/bazelbuild/starlark/blob/master/spec.md#bool):
 * returns a string encoding a codepoint.
 *
 * `chr(i)` returns a string that encodes the single Unicode code
 * point whose value is specified by the integer `i`. `chr` fails
 * unless `0 ≤ i ≤ 0x10FFFF`.
 *
 * ```
 * # starlark::assert::allTrue(r#"
 * chr(65) == 'A'
 * chr(1049) == 'Й'
 * chr(0x1F63F) == '😿'
 * # "#);
 * ```
 */
internal fun chr(i: Int): Result<Char> {
    if (i < 0) {
        return Result.failure(
            IllegalArgumentException("chr() parameter value negative integer $i")
        )
    }

    val cp = i.toUInt()

    return try {
        val codePoint = cp.toInt()
        if (codePoint in 0..0x10FFFF) {
            Result.success(Char(codePoint))
        } else {
            Result.failure(
                IllegalArgumentException(
                    "chr() parameter value is 0x${cp.toString(16)} which is not a valid UTF-8 codepoint"
                )
            )
        }
    } catch (e: IllegalArgumentException) {
        Result.failure(
            IllegalArgumentException(
                "chr() parameter value is 0x${cp.toString(16)} which is not a valid UTF-8 codepoint"
            )
        )
    }
}

/**
 * [ord](https://github.com/bazelbuild/starlark/blob/master/spec.md#ord):
 * returns the codepoint of a character
 *
 * `ord(s)` returns the integer value of the sole Unicode code point
 * encoded by the string `s`.
 *
 * If `s` does not encode exactly one Unicode code point, `ord` fails.
 * Each invalid code within the string is treated as if it encodes the
 * Unicode replacement character, U+FFFD.
 *
 * Example:
 *
 * ```
 * # starlark::assert::allTrue(r#"
 * ord("A")                                == 65
 * ord("Й")                                == 1049
 * ord("😿")                               == 0x1F63F
 * # "#);
 * ```
 */
internal fun ord(a: StringValue): Result<Int> {
    val chars = a.asStr().iterator()

    return if (chars.hasNext()) {
        val c = chars.next()
        if (!chars.hasNext()) {
            Result.success(c.code)
        } else {
            Result.failure(
                IllegalArgumentException(
                    "ord(): ${a.toValue().toRepr()} is not a single character string"
                )
            )
        }
    } else {
        Result.failure(
            IllegalArgumentException(
                "ord(): ${a.toValue().toRepr()} is not a single character string"
            )
        )
    }
}

/**
 * [repr](https://github.com/bazelbuild/starlark/blob/master/spec.md#repr):
 * formats its argument as a string.
 *
 * All strings in the result are double-quoted.
 *
 * ```
 * # starlark::assert::allTrue(r#"
 * repr(1)                 == '1'
 * repr("x")               == "\"x\""
 * repr([1, "x"])          == "[1, \"x\"]"
 * repr("test \"'")        == "\"test \\\"'\""
 * repr("x\"y😿 \\'")      == "\"x\\\"y\\U0001f63f \\\\'\""
 * # "#);
 * ```
 */
internal fun repr(a: Value, eval: Evaluator): Result<StringValue> {
    val s = eval.stringPool.alloc()
    a.collectRepr(s)
    val r = eval.heap().allocStr(s.toString())
    eval.stringPool.release(s)
    return Result.success(r)
}

/**
 * [str](https://github.com/bazelbuild/starlark/blob/master/spec.md#str):
 * formats its argument as a string.
 *
 * If x is a string, the result is x (without quotation).
 * All other strings, such as elements of a list of strings, are
 * double-quoted.
 *
 * ```
 * # starlark::assert::allTrue(r#"
 * str(1)                          == '1'
 * str("x")                        == 'x'
 * str([1, "x"])                   == "[1, \"x\"]"
 * # "#);
 * ```
 */
internal fun str(a: Value, eval: Evaluator): Result<StringValue> {
    // Special case that can avoid reallocating, but is equivalent
    StringValue.new(a)?.let { return Result.success(it) }

    val s = eval.stringPool.alloc()
    a.collectRepr(s)
    val r = eval.heap().allocStr(s.toString())
    eval.stringPool.release(s)
    return Result.success(r)
}
