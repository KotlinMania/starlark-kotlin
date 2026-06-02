// port-lint: source src/values/types/tuple/globals.rs
package io.github.kotlinmania.starlark.values.types.tuple

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
import io.github.kotlinmania.starlark.values.layout.avalues.allocTuple
import io.github.kotlinmania.starlark.values.layout.avalues.allocTupleIter

/**
 * Register the `tuple` builtin function.
 *
 * [tuple](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#tuple
 * ): returns a tuple containing the elements of the iterable x.
 *
 * With no arguments, `tuple()` returns the empty tuple.
 *
 * ```
 * tuple() == ()
 * tuple([1,2,3]) == (1, 2, 3)
 * ```
 */
internal fun registerTuple(globals: GlobalsBuilder) {
    globals.setFunction("tuple", asType = io.github.kotlinmania.starlark.typing.Ty.anyTuple()) { args: Arguments, eval: Evaluator ->
        val heap = eval.heap()
        val a = args.optionalPositional<Value>(0)
        if (a == null) {
            heap.allocTuple(emptyList())
        } else {
            val tupleRef = TupleRef.fromValue(a)
            if (tupleRef != null) {
                a
            } else {
                val it = a.iterate(heap).getOrThrow()
                heap.allocTupleIter(it.asSequence().asIterable())
            }
        }
    }
}
