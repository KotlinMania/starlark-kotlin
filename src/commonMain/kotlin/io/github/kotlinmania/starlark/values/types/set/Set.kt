// port-lint: source src/values/types/set/set.rs
package io.github.kotlinmania.starlark.values.types.set

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
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.typing.TyStarlarkValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.Heap

/**
 * Register the `set` builtin function.
 *
 * This function is generated from the `#[starlark_module]` macro in Rust.
 * It registers the `set` constructor as a global builtin function.
 */
internal fun registerSet(globals: GlobalsBuilder) {
    /**
     * [set](https://github.com/bazelbuild/starlark/blob/master/spec.md#set):
     * construct a set.
     *
     * `set(x)` returns a new set containing the unique elements of the
     * iterable sequence x.
     *
     * With no argument, `set()` returns a new empty set.
     *
     * ```
     * # starlark::assert::all_true(r#"
     * set()           == set([])
     * set([1, 2, 3])  == set([3, 2, 1])
     * set([1, 2, 1])  == set([1, 2])
     * # "#);
     * ```
     */
    // #[starlark(as_type = FrozenSet, speculative_exec_safe,
    //   special_builtin_function = SpecialBuiltinFunction::Set)]
    // fn set<'v>(arg: Option<ValueOfUnchecked<'v, StarlarkIter<Value<'v>>>>, heap: Heap<'v>)
    //   -> starlark::Result<SetData<'v>>
    globals.setFunction(
        name = "set",
        asType = Ty.starlarkValue(TyStarlarkValue.set()),
        speculativeExecSafe = true,
    ) { callArgs, eval ->
        val heap: Heap = eval.heap()
        val arg: Value? = callArgs.optionalPositional(0)
        val set =
            when (arg) {
                null -> SetData()
                else -> {
                    val pos = arg
                    when (val setRef = SetRef.unpackValueOpt(pos)) {
                        null -> {
                            val it = pos.iterate(heap).getOrThrow()
                            val data = SetData()
                            for (el in it) {
                                val hashedEl = el.getHashed().getOrThrow()
                                data.content.insertHashed(hashedEl)
                            }
                            data
                        }
                        else -> {
                            // (set.aref).clone() -- clone the SetData from the SetRef
                            val data = SetData()
                            for (el in setRef.content.iterHashed()) {
                                data.content.insertHashed(el)
                            }
                            data
                        }
                    }
                }
            }
        set.allocValue(heap)
    }
}
