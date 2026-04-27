// port-lint: source src/values/types/float/globals.rs
package io.github.kotlinmania.starlark.values.types.float
import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.eval.runtime.positionalAll
import io.github.kotlinmania.starlark.typing.Ty

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

import io.github.kotlinmania.starlark.values.types.num.NumRef
import io.github.kotlinmania.starlark.values.types.string.stringRepr

/**
 * The accepted argument shape for `float()`: a numeric value, a boolean, or a string.
 */
sealed class FloatParam {
    data class Num(val value: NumRef) : FloatParam()
    data class Bool(val value: Boolean) : FloatParam()
    data class Str(val value: String) : FloatParam()
}

/**
 * Register float-related global functions.
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
     * # starlark::assert::allTrue(r#"
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
    globals.setFunction(
        name = "float",
        asType = Ty.float(),
        speculativeExecSafe = true,
    ) { args, eval ->
        // Get the first positional argument, or return 0.0 if absent.
        val positional = args.positionalAll()
        if (positional.isEmpty()) {
            return@setFunction StarlarkFloat(0.0).allocValue(eval.heap())
        }

        val v = positional.first()

        // Try to unpack as NumRef, Bool, or String in order.
        val asBool = v.unpackBool()
        if (asBool != null) {
            return@setFunction StarlarkFloat(if (asBool) 1.0 else 0.0).allocValue(eval.heap())
        }

        val asStr = v.unpackStr()
        if (asStr != null) {
            val s = asStr
            val f: Double = try {
                val f = s.toDouble()
                if (f.isInfinite() && !s.lowercase().contains("inf")) {
                    throw IllegalArgumentException(
                        "float() floating-point number too large: $s"
                    )
                } else {
                    f
                }
            } catch (x: NumberFormatException) {
                val repr = StringBuilder()
                stringRepr(s, repr)
                throw IllegalArgumentException(
                    "$repr is not a valid number: $x"
                )
            }
            return@setFunction StarlarkFloat(f).allocValue(eval.heap())
        }

        // Otherwise try as numeric (int or float)
        val asNum = NumRef.unpackValueImpl(v)
        if (asNum != null) {
            return@setFunction StarlarkFloat(asNum.asFloat()).allocValue(eval.heap())
        }

        throw IllegalArgumentException(
            "float() argument doesn't match, expected int, float, bool or string"
        )
    }
}
