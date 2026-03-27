// port-lint: source src/values/types/int/globals.rs
package io.github.kotlinmania.starlark_kotlin.values.types.int

import io.github.kotlinmania.starlark_kotlin.values.types.tuple.it



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

// Placeholder types until the actual implementations are ported
expect class GlobalsBuilder

expect class Heap<V_> {
    fun <T> alloc(value: T): Value<V_>
}

expect class Value<V_>

expect class ValueOfUnchecked<V_, T> {
    companion object {
        fun <V_, T> new(value: Value<V_>): ValueOfUnchecked<V_, T>
    }
}

expect class ValueOf<V_, T> {
    val typed: T
    val value: Value<V_>
}


expect class StarlarkFloat {
    val value: Double

    companion object {
        fun trunc(value: Double): Double
    }
}

/**
 * Either type for representing one of two possible values.
 * Corresponds to Rust's `either::Either`.
 */
sealed class Either<out L, out R> {
    data class Left<out L>(val value: L) : Either<L, Nothing>()
    data class Right<out R>(val value: R) : Either<Nothing, R>()
}

/**
 * Sealed class representing NumRef enum values.
 * This mimics Rust's `NumRef<V_>` enum.
 */
sealed class NumRefValue<V_> {
    // TODO: stub - Int needs real import
    data class Int<V_>(val value: Value<V_>) : NumRefValue<V_>()
    // TODO: stub - Float needs real import
    data class Float<V_>(val value: StarlarkFloat) : NumRefValue<V_>()
}

// Extension functions for GlobalsBuilder to register functions
expect fun <V_> GlobalsBuilder.function(
    name: String,
    asType: kotlin.reflect.KClass<*>,
    speculativeExecSafe: Boolean = false,
    requirePos: Boolean = false,
    impl: (a: ValueOf<V_, Either<Either<NumRefValue<V_>, Boolean>, String>>?, base: Int?, heap: Heap<V_>) -> Result<ValueOfUnchecked<V_, StarlarkInt>>
)

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
    globals.function<Nothing>(
        name = "int",
        asType = PointerI32::class,
        speculativeExecSafe = true,
        requirePos = true
    ) { a, base, heap ->
        if (a == null) {
            return@function Result.success(ValueOfUnchecked.new(heap.alloc(0)))
        }

        val numOrBool = when (val typed = a.typed) {
            is Either.Left -> typed.value
            is Either.Right -> {
                val s = typed.value
                val baseValue = base ?: 0
                if (baseValue == 1 || baseValue < 0 || baseValue > 36) {
                    return@function Result.failure(
                        IllegalArgumentException(
                            "$baseValue is not a valid base, int() base must be >= 2 and <= 36"
                        )
                    )
                }

                val (negate, strippedSign) = when (s.firstOrNull()) {
                    '+' -> false to s.substring(1)
                    '-' -> true to s.substring(1)
                    else -> false to s
                }

                val actualBase = if (baseValue == 0) {
                    when (strippedSign.take(2)) {
                        "0b", "0B" -> 2
                        "0o", "0O" -> 8
                        "0x", "0X" -> 16
                        else -> 10
                    }
                } else {
                    baseValue
                }

                val strippedPrefix = when (actualBase) {
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
                    return@function Result.failure(
                        IllegalArgumentException("Cannot parse `$strippedPrefix` as an integer")
                    )
                }

                return@function StarlarkInt.fromStrRadix(strippedPrefix, actualBase).mapCatching { x ->
                    val result = if (negate) -x else x
                    ValueOfUnchecked.new(heap.alloc(result))
                }
            }
        }

        if (base != null) {
            return@function Result.failure(
                IllegalArgumentException(
                    "int() cannot convert non-string with explicit base '$base'"
                )
            )
        }

        return@function when (numOrBool) {
            is Either.Left -> {
                when (val numRef = numOrBool.value) {
                    is NumRefValue.Int -> {
                        Result.success(ValueOfUnchecked.new(a.value))
                    }
                    is NumRefValue.Float -> {
                        val f = numRef.value
                        StarlarkInt.fromF64Exact(StarlarkFloat.trunc(f.value)).map {
                            ValueOfUnchecked.new(heap.alloc(it))
                        }
                    }
                }
            }
            is Either.Right -> {
                val b = numOrBool.value
                Result.success(ValueOfUnchecked.new(heap.alloc(b as Int)))
            }
        }
    }
}
