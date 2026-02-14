// port-lint: source src/values/types/num/globals.rs
package io.github.kotlinmania.starlark_kotlin.values.types.num

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

import io.github.kotlinmania.starlark_kotlin.environment.GlobalsBuilder
import kotlin.math.abs

/// #[starlark_module]
/// pub(crate) fn register_num(globals: &mut GlobalsBuilder)
internal fun registerNum(globals: GlobalsBuilder) {
    /// Take the absolute value of an int.
    ///
    /// ```
    /// abs(0)   == 0
    /// abs(-10) == 10
    /// abs(10)  == 10
    /// abs(10.0) == 10.0
    /// abs(-12.34) == 12.34
    /// ```
    ///
    /// fn abs(#[starlark(require = pos)] x: NumRef) -> anyhow::Result<Num>
    fun abs(x: NumRef): Num {
        return when (x) {
            is NumRef.Int -> Num.Int(x.value.abs())
            is NumRef.Float -> Num.Float(abs(x.value.value))
        }
    }

    globals.setFunction(
        name = "abs",
        positional = 1,
    ) { args, _ ->
        val x = args.first() as NumRef
        Result.success(abs(x))
    }
}
