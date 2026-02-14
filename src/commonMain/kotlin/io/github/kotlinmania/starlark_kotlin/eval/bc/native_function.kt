// port-lint: source src/eval/bc/native_function.rs
package io.github.kotlinmania.starlark_kotlin.eval.bc.native_function

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

import io.github.kotlinmania.starlark_kotlin.eval.Arguments
import io.github.kotlinmania.starlark_kotlin.eval.Evaluator
import io.github.kotlinmania.starlark_kotlin.values.FrozenValueTyped
import io.github.kotlinmania.starlark_kotlin.values.Value
import io.github.kotlinmania.starlark_kotlin.values.function.NativeFunc
import io.github.kotlinmania.starlark_kotlin.values.function.NativeFunction

/// Pointer to a native function optimized for bytecode execution.
// #[derive(Copy, Clone, Dupe, Allocative)]
// pub(crate) struct BcNativeFunction {
//     fun: FrozenValueTyped<'static, NativeFunction>,
//     imp: &'static NativeFunc,
// }
internal class BcNativeFunction private constructor(
    private val func: FrozenValueTyped<NativeFunction>,
    /// Copy function here from `fun` to avoid extra dereference when calling.
    private val imp: NativeFunc,
) {
    // impl BcNativeFunction

    companion object {
        // pub(crate) fn new(fun: FrozenValueTyped<'static, NativeFunction>) -> BcNativeFunction
        fun new(func: FrozenValueTyped<NativeFunction>): BcNativeFunction {
            return BcNativeFunction(
                func = func,
                imp = func.asRef().function,
            )
        }
    }

    // #[inline]
    // pub(crate) fn fun(&self) -> FrozenValueTyped<'static, NativeFunction>
    fun func(): FrozenValueTyped<NativeFunction> = func

    // #[inline]
    // pub(crate) fn to_value<'v>(&self) -> Value<'v>
    fun toValue(): Value = func.toValue()

    // #[inline]
    // pub(crate) fn invoke<'v>(&self, args: &Arguments<'v, '_>, eval: &mut Evaluator<'v, '_, '_>) -> crate::Result<Value<'v>>
    fun invoke(args: Arguments, eval: Evaluator): Value {
        return imp.invoke(eval, args)
    }
}
