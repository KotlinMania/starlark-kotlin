// port-lint: source src/eval/bc/native_function.rs
package io.github.kotlinmania.starlark.eval.bc

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

import io.github.kotlinmania.starlark.eval.runtime.Arguments
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.values.layout.FrozenValueTyped
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.types.NativeFunc
import io.github.kotlinmania.starlark.values.types.NativeFunction

/** Pointer to a native function optimized for bytecode execution. */
internal class BcNativeFunction private constructor(
    private val func: FrozenValueTyped<NativeFunction>,
    /** Copy function here from [func] to avoid extra dereference when calling. */
    private val imp: NativeFunc,
) {
    companion object {
        internal fun new(func: FrozenValueTyped<NativeFunction>): BcNativeFunction =
            BcNativeFunction(
                func = func,
                imp = func.asRef().function,
            )
    }

    internal fun func(): FrozenValueTyped<NativeFunction> = func

    fun toValue(): Value = func.toValue()

    fun invoke(args: Arguments, eval: Evaluator): Result<Value> = imp.invoke(eval, args)
}
