// port-lint: source src/values/typing/type_compiled/globals.rs
package io.github.kotlinmania.starlark.values.typing.typecompiled

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

import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.eval.runtime.Arguments
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.values.layout.Value

// #[starlark_module]
// pub(crate) fn register_eval_type(globals: &mut GlobalsBuilder)
internal fun registerEvalType(globals: GlobalsBuilder) {
    /** Create a runtime type object which can be used to check if a value matches the given type. */
    // fn eval_type<'v>(#[starlark(require = pos)] ty: ValueOfUnchecked<'v, AbstractType>, eval: &mut Evaluator) -> anyhow::Result<TypeCompiled<Value<'v>>>
    globals.setFunction("eval_type") { args: Arguments, eval: Evaluator ->
        val ty = args.positional1(eval.heap()).getOrThrow()
        TypeCompiled.new(ty, eval.heap()).toInner()
    }

    /**
     * Check if a value matches the given type.
     *
     * This operation can be very fast or very slow depending on how it is used.
     *
     * `isinstance(x, list)` is very fast,
     * because it is compiled to a special bytecode instruction.
     *
     * `isinstance(x, list[str])` is `O(N)` operation
     * because it checks every element in this list.
     *
     * `L = list; [isinstance(x, L) for x in y]` is slow when `L` is not a constant:
     * `isinstance()` first converts `list` to a type in a loop, which is slow.
     *
     * But last operation can be optimized like this:
     * `L = eval_type(list); [isinstance(x, L) for x in y]`:
     * `eval_type()` converts `list` value into prepared type matcher.
     */
    // fn isinstance<'v>(#[starlark(require = pos)] value: Value<'v>, #[starlark(require = pos)] ty: ValueOfUnchecked<'v, AbstractType>, eval: &mut Evaluator) -> anyhow::Result<bool>
    globals.setFunction("isinstance") { args: Arguments, eval: Evaluator ->
        val positional = args.positional(2, eval.heap()).getOrThrow()
        val value = positional[0]
        val ty = positional[1]
        val compiled =
            runCatching { TypeCompiled.new(ty, eval.heap()) }
                .getOrElse { return@setFunction Result.failure<Value>(it) }
        Value.newBool(compiled.matches(value))
    }
}

// #[cfg(test)] mod tests
// Tests are in commonTest, not here.
