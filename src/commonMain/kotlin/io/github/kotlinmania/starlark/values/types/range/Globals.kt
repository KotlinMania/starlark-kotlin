// port-lint: source src/values/types/range/globals.rs
package io.github.kotlinmania.starlark.values.types.range
import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.eval.runtime.Arguments
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.eval.runtime.optionalPositional
import io.github.kotlinmania.starlark.eval.runtime.positional


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

// Real types should be imported from their respective packages

/**
 * Register range-related global functions.
 *
 * The macro in Rust generates code to register these globals; in Kotlin, we
 * implement this explicitly as a regular function.
 */
internal fun registerRange(globals: GlobalsBuilder) {
    /**
     * [range](
     * https://github.com/bazelbuild/starlark/blob/master/spec.md#range
     * ): return a range of integers
     *
     * `range` returns a tuple of integers defined by the specified interval
     * and stride.
     *
     * ```python
     * range(stop)                             # equivalent to range(0, stop)
     * range(start, stop)                      # equivalent to range(start, stop, 1)
     * range(start, stop, step)
     * ```
     *
     * `range` requires between one and three integer arguments.
     * With one argument, `range(stop)` returns the ascending sequence of
     * non-negative integers less than `stop`.
     * With two arguments, `range(start, stop)` returns only integers not less
     * than `start`.
     *
     * With three arguments, `range(start, stop, step)` returns integers
     * formed by successively adding `step` to `start` until the value meets or
     * passes `stop`. A call to `range` fails if the value of `step` is
     * zero.
     *
     * ```
     * # starlark::assert::allTrue(r#"
     * list(range(10))                         == [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
     * list(range(3, 10))                      == [3, 4, 5, 6, 7, 8, 9]
     * list(range(3, 10, 2))                   == [3, 5, 7, 9]
     * list(range(10, 3, -2))                  == [10, 8, 6, 4]
     * # "#);
     * ```
     */
    globals.setFunction("range") { args: Arguments, eval: Evaluator ->
        val a1 = args.positional<Int>(0)
        val a2 = args.optionalPositional<Int>(1)
        val step = args.optionalPositional<Int>(2)

        val actualStep = step ?: 1
        val start = if (a2 != null) a1 else 0
        val stop = a2 ?: a1

        if (actualStep == 0) {
            Result.failure<Range>(
                IllegalArgumentException("Third argument of range (step) cannot be zero")
            )
        } else {
            Result.success(Range.new(start, stop, NonZeroI32.new(actualStep)!!))
        }
    }
}
