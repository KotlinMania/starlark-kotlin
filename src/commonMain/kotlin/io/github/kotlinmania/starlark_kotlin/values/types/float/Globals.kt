// port-lint: source src/values/types/float/globals.rs
package io.github.kotlinmania.starlark_kotlin.values.types.float

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

expect class StarlarkFloat

expect class NumRef {
    fun asFloat(): Double
}

expect class StringRepr {
    companion object {
        fun stringRepr(s: String, buffer: StringBuilder)
    }
}

/**
 * Sealed class representing the type hierarchy for float() parameter.
 * This mimics Rust's `Either<Either<NumRef, bool>, &str>` type.
 */
sealed class FloatParam {
    // TODO: stub - Num needs real import
    data class Num(val value: NumRef) : FloatParam()
    // TODO: stub - Bool needs real import
    data class Bool(val value: Boolean) : FloatParam()
    data class Str(val value: String) : FloatParam()
}

// Extension functions for GlobalsBuilder to register functions
expect fun GlobalsBuilder.function(
    name: String,
    asType: kotlin.reflect.KClass<*>,
    speculativeExecSafe: Boolean = false,
    requirePos: Boolean = false,
    impl: (FloatParam?) -> Result<Double>
)

/**
 * Register float-related global functions.
 *
 * This is the Kotlin port of the Rust `#[starlark_module]` annotated function.
 * The macro in Rust generates code to register these globals; in Kotlin, we
 * implement this explicitly as a regular function.
 */
internal fun registerFloat(globals: GlobalsBuilder) {
    /**
     * [float](
     * https://github.com/bazelbuild/starlark/blob/master/spec.md#float
     * ): interprets its argument as a floating-point number.
     *
     * If x is a `float`, the result is x.
     * if x is an `int`, the result is the nearest floating point value to x.
     * If x is a string, the string is interpreted as a floating-point literal.
     * With no arguments, `float()` returns `0.0`.
     *
     * ```
     * # starlark::assert::all_true(r#"
     * float() == 0.0
     * float(1) == 1.0
     * float('1') == 1.0
     * float('1.0') == 1.0
     * float('.25') == 0.25
     * float('1e2') == 100.0
     * float(False) == 0.0
     * float(True) == 1.0
     * # "#);
     * # starlark::assert::fail(r#"
     * float("hello")   # error: not a valid number
     * # "#, "not a valid number");
     * # starlark::assert::fail(r#"
     * float([])   # error
     * # "#, "doesn't match, expected");
     * ```
     */
    globals.function(
        name = "float",
        asType = StarlarkFloat::class,
        speculativeExecSafe = true,
        requirePos = true
    ) { a: FloatParam? ->
        if (a == null) {
            return@function Result.success(0.0)
        }

        when (a) {
            is FloatParam.Num -> {
                Result.success(a.value.asFloat())
            }
            is FloatParam.Bool -> {
                Result.success(if (a.value) 1.0 else 0.0)
            }
            is FloatParam.Str -> {
                val s = a.value
                try {
                    val f = s.toDouble()
                    if (f.isInfinite() && !s.lowercase().contains("inf")) {
                        // if a resulting float is infinite but the parsed string is not explicitly infinity then we should fail with an error
                        Result.failure(
                            IllegalArgumentException(
                                "float() floating-point number too large: $s"
                            )
                        )
                    } else {
                        Result.success(f)
                    }
                } catch (x: NumberFormatException) {
                    val repr = StringBuilder()
                    StringRepr.stringRepr(s, repr)
                    Result.failure(
                        IllegalArgumentException(
                            "$repr is not a valid number: $x"
                        )
                    )
                }
            }
        }
    }
}
