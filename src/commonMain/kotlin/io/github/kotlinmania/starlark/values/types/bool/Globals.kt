// port-lint: source values/types/bool/globals.rs
package io.github.kotlinmania.starlark.values.types.bool

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

import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.eval.runtime.Arguments
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.eval.runtime.optionalPositional
import io.github.kotlinmania.starlark.values.layout.Value

internal fun registerBool(globals: GlobalsBuilder) {
    /** A boolean representing true. */
    globals.setConst("True", true)

    /** A boolean representing false. */
    globals.setConst("False", false)

    /**
     * [bool](
     * https://github.com/bazelbuild/starlark/blob/master/spec.md#bool
     * ): returns the truth value of any starlark value.
     *
     * ```
     * bool() == False
     * bool([]) == False
     * bool([1]) == True
     * bool(True) == True
     * bool(False) == False
     * bool(None) == False
     * bool(bool) == True
     * bool(1) == True
     * bool(0) == False
     * bool({}) == False
     * bool({1:2}) == True
     * bool(()) == False
     * bool((1,)) == True
     * bool("") == False
     * bool("1") == True
     * ```
     */
    globals.setFunction("bool") { args: Arguments, eval: Evaluator ->
        val x = args.optionalPositional<Value>(0)
        Value.newBool(x?.toBool() ?: false)
    }
}
