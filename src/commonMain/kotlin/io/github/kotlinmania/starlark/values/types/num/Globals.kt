// port-lint: source src/values/types/num/globals.rs
package io.github.kotlinmania.starlark.values.types.num

/*
 * Copyright 2018 The Starlark in Rust Authors.
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

import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.eval.runtime.Arguments
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.values.layout.Value

/**
 * Register numerical global functions.
 *
 * ```
 * abs(0)   == 0
 * abs(-10) == 10
 * abs(10)  == 10
 * abs(10.0) == 10.0
 * abs(-12.34) == 12.34
 * ```
 */
internal fun registerNum(globals: GlobalsBuilder) {
    /** Take the absolute value of an int. */
    globals.setFunction("abs") { args: Arguments, eval: Evaluator ->
        val v = args.positional<Value>(0)
        val x = NumRef.unpackValue(v).getOrThrow()
            ?: throw IllegalArgumentException("abs() requires a numeric argument")
        val result: Num = when (x) {
            is NumRef.Int -> Num.Int(x.value.abs())
            is NumRef.Float -> Num.Float(kotlin.math.abs(x.value.value))
        }
        result.allocValue(eval.heap())
    }
}
