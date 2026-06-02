// port-lint: source src/values/types/int/globals.rs
package io.github.kotlinmania.starlark.values.types.int

import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.types.num.NumRef
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
 * Register int-related global functions.
 *
 * This is the Kotlin port of the Rust `#[starlark_module]` annotated function.
 * The macro in Rust generates code to register these globals; in Kotlin, we
 * implement this explicitly as a regular function.
 */
internal fun registerInt(globals: GlobalsBuilder) {
    /**
     * [int](
     * https://github.com/bazelbuild/starlark/blob/master/spec.md#int
     * ): convert a value to integer.
     *
     * `int(x[, base])` interprets its argument as an integer.
     *
     * If x is an `int`, the result is x.
     * If x is a `float`, the result is the integer value nearest to x,
     * truncating towards zero; it is an error if x is not finite (`NaN`,
     * `+Inf`, `-Inf`).
     * If x is a `bool`, the result is 0 for `False` or 1 for `True`.
     *
     * If x is a string, it is interpreted like a string literal;
     * an optional base prefix (`0`, `0b`, `0B`, `0x`, `0X`) determines which
     * base to use. The string may specify an arbitrarily large integer,
     * whereas true integer literals are restricted to 64 bits.
     * If a non-zero `base` argument is provided, the string is interpreted
     * in that base and no base prefix is permitted; the base argument may
     * specified by name.
     *
     * `int()` with no arguments returns 0.
     *
     * ```
     * # starlark::assert::all_true(r#"
     * int() == 0
     * int(1) == 1
     * int(False) == 0
     * int(True) == 1
     * int('1') == 1
     * int('16') == 16
     * int('16', 10) == 16
     * int('16', 8) == 14
     * int('16', 16) == 22
     * int(0.0) == 0
     * int(3.14) == 3
     * int(-12345.6789) == -12345
     * int(2e9) == 2000000000
     * # "#);
     * # starlark::assert::fail(r#"
     * int("hello")   # error: Cannot parse
     * # "#, "Cannot parse");
     * # starlark::assert::fail(r#"
     * int(float("nan"))   # error: cannot be represented as exact integer
     * # "#, "cannot be represented as exact integer");
     * # starlark::assert::fail(r#"
     * int(float("inf"))   # error: cannot be represented as exact integer
     * # "#, "cannot be represented as exact integer");
     * ```
     */
    // #[starlark(as_type = PointerI32, speculative_exec_safe)]
    // fn int<'v>(
    //     #[starlark(require = pos)] a: Option<ValueOf<'v, Either<Either<NumRef<'v>, bool>, &'v str>>>,
    //     base: Option<i32>,
    //     heap: Heap<'v>,
    // ) -> starlark::Result<ValueOfUnchecked<'v, StarlarkInt>>
    globals.setFunction("int", speculativeExecSafe = true, asType = Ty.int()) { callArgs, eval ->
        val heap = eval.heap()
        val a: Value? = callArgs.optionalPositional(0)
        val base: Int? = callArgs.optionalNamed("base")

        if (a == null) {
            // int() with no args returns 0
            return@setFunction Value.newInt(InlineInt.ZERO)
        }

        // Try to interpret as string first
        val str = a.unpackStr()
        if (str != null) {
            val baseValue = base ?: 0
            if (baseValue == 1 || baseValue < 0 || baseValue > 36) {
                throw IllegalArgumentException(
                    "$baseValue is not a valid base, int() base must be >= 2 and <= 36",
                )
            }

            val (negate, strippedSign) =
                when (str.firstOrNull()) {
                    '+' -> false to str.substring(1)
                    '-' -> true to str.substring(1)
                    else -> false to str
                }

            val actualBase =
                if (baseValue == 0) {
                    when (strippedSign.take(2)) {
                        "0b", "0B" -> 2
                        "0o", "0O" -> 8
                        "0x", "0X" -> 16
                        else -> 10
                    }
                } else {
                    baseValue
                }

            val strippedPrefix =
                when (actualBase) {
                    16 -> {
                        if (strippedSign.startsWith("0x") || strippedSign.startsWith("0X")) {
                            strippedSign.substring(2)
                        } else {
                            strippedSign
                        }
                    }
                    8 -> {
                        if (strippedSign.startsWith("0o") || strippedSign.startsWith("0O")) {
                            strippedSign.substring(2)
                        } else {
                            strippedSign
                        }
                    }
                    2 -> {
                        if (strippedSign.startsWith("0b") || strippedSign.startsWith("0B")) {
                            strippedSign.substring(2)
                        } else {
                            strippedSign
                        }
                    }
                    else -> strippedSign
                }

            // We already handled the sign above, so we are not trying to parse another sign.
            if (strippedPrefix.startsWith('-') || strippedPrefix.startsWith('+')) {
                throw IllegalArgumentException("Cannot parse `$strippedPrefix` as an integer")
            }

            val x = StarlarkInt.fromStrRadix(strippedPrefix, actualBase).getOrThrow()
            val result = if (negate) -x else x
            return@setFunction allocStarlarkInt(result, heap)
        }

        // Not a string - try numeric or bool
        if (base != null) {
            throw IllegalArgumentException(
                "int() cannot convert non-string with explicit base '$base'",
            )
        }

        // Try to unpack as a numeric value
        val numRef = a.unpackNum()
        if (numRef != null) {
            return@setFunction when (numRef) {
                is NumRef.Int -> {
                    // Already an int, return the original value
                    a
                }
                is NumRef.Float -> {
                    val f = numRef.value
                    val truncated = truncate(f.value)
                    val starlarkInt = StarlarkInt.fromF64Exact(truncated).getOrThrow()
                    allocStarlarkInt(starlarkInt, heap)
                }
            }
        }

        // Try to unpack as a bool
        val boolVal = a.unpackBool()
        if (boolVal != null) {
            return@setFunction Value.newInt(if (boolVal) InlineInt.newUnchecked(1) else InlineInt.ZERO)
        }

        throw IllegalArgumentException(
            "int() argument must be a string, a number, or a bool, not '${a.getType()}'",
        )
    }
}

/**
 * Allocate a [StarlarkInt] on the heap and return a [Value].
 *
 * For small ints that fit in an [InlineInt], no heap allocation is needed.
 * For big ints, the value is allocated on the heap via [StarlarkBigInt.allocValue].
 */
private fun allocStarlarkInt(
    starlarkInt: StarlarkInt,
    heap: io.github.kotlinmania.starlark.values.layout.heap.Heap,
): Value =
    when (starlarkInt) {
        is StarlarkInt.Small -> Value.newInt(starlarkInt.value)
        is StarlarkInt.Big -> starlarkInt.value.allocValue(heap)
    }
