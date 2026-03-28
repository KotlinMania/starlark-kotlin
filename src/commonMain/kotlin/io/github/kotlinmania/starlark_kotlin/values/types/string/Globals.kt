// port-lint: source src/values/types/string/globals.rs
package io.github.kotlinmania.starlark_kotlin.values.types.string
import io.github.kotlinmania.starlark_kotlin.environment.GlobalsBuilder

import io.github.kotlinmania.starlark_kotlin.values.toValue
import io.github.kotlinmania.starlark_kotlin.tests.collectRepr
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.StringValue


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

// Extension functions for GlobalsBuilder to register different function types
// These will be implemented when the actual GlobalsBuilder is ported
expect fun GlobalsBuilder.registerFunction(
    name: String,
    asType: kotlin.reflect.KClass<*>? = null,
    speculativeExecSafe: Boolean = false,
    impl: FunctionImpl
)

// Marker interface for function implementations
expect interface FunctionImpl

/**
 * Register string-related global functions.
 *
 * This is the Kotlin port of the Rust `#[starlark_module]` annotated function.
 * The macro in Rust generates code to register these globals; in Kotlin, we
 * implement this explicitly as a regular function.
 */
internal fun registerStr(globals: GlobalsBuilder) {
    // The implementations below would be registered through the GlobalsBuilder
    // when it's properly ported. For now, they serve as documentation of the API.

    // Note: In the full implementation, these would call globals.registerFunction()
    // with appropriate FunctionImpl instances that encapsulate the logic below.
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
 * # starlark::assert::all_true(r#"
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
        if (Character.isValidCodePoint(codePoint)) {
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
 * # starlark::assert::all_true(r#"
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
 * # starlark::assert::all_true(r#"
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
 * # starlark::assert::all_true(r#"
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
