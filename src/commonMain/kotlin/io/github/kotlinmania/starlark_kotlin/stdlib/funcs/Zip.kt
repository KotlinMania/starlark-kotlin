// port-lint: source src/stdlib/funcs/zip.rs
package io.github.kotlinmania.starlark_kotlin.stdlib.funcs.zip

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

import io.github.kotlinmania.starlark_kotlin.codemap.Span
import io.github.kotlinmania.starlark_kotlin.environment.GlobalsBuilder
import io.github.kotlinmania.starlark_kotlin.typing.ParamSpec
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.typing.TypingOracleCtx
import io.github.kotlinmania.starlark_kotlin.typing.call_args.TyCallArgs
import io.github.kotlinmania.starlark_kotlin.typing.callable.TyCallable
import io.github.kotlinmania.starlark_kotlin.typing.error.TypingOrInternalError
import io.github.kotlinmania.starlark_kotlin.typing.function.TyCustomFunctionImpl
import io.github.kotlinmania.starlark_kotlin.values.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value

// #[derive(Clone, Debug, Eq, PartialEq, Hash, Ord, PartialOrd, Allocative)]
// struct ZipType;
private object ZipType : TyCustomFunctionImpl {

    // impl TyCustomFunctionImpl for ZipType

    // fn as_callable(&self) -> TyCallable
    override fun asCallable(): TyCallable {
        return TyCallable(ParamSpec.args(Ty.iter(Ty.any())), Ty.list(Ty.any()))
    }

    // fn validate_call(...)
    override fun validateCall(
        span: Span,
        args: TyCallArgs,
        oracle: TypingOracleCtx,
    ): Result<Ty> {
        val iterItemTypes = mutableListOf<Ty>()
        for (pos in args.pos) {
            val itemTy = oracle.iterItem(pos)
            iterItemTypes.add(itemTy)
        }
        return if (args.args != null) {
            Result.success(Ty.list(Ty.any()))
        } else {
            Result.success(Ty.list(Ty.tuple(iterItemTypes)))
        }
    }
}

/**
 * [zip](https://github.com/bazelbuild/starlark/blob/master/spec.md#zip): zip several iterables together.
 *
 * `zip()` returns a new list of n-tuples formed from corresponding
 * elements of each of the n iterable sequences provided as arguments to
 * `zip`. That is, the first tuple contains the first element of each of
 * the sequences, the second element contains the second element of each
 * of the sequences, and so on. The result list is only as long as the
 * shortest of the input sequences.
 *
 * ```
 * zip()                           == []
 * zip(range(5))                   == [(0,), (1,), (2,), (3,), (4,)]
 * zip(range(5), "abc".elems())    == [(0, "a"), (1, "b"), (2, "c")]
 * ```
 */
// #[starlark_module]
// pub(crate) fn register_zip(globals: &mut GlobalsBuilder)
internal fun registerZip(globals: GlobalsBuilder) {
    // #[starlark(speculative_exec_safe, ty_custom_function = ZipType)]
    // fn zip<'v>(args: UnpackTuple<...>, heap: Heap<'v>) -> starlark::Result<Vec<Value<'v>>>
    globals.setFunction("zip", speculativeExecSafe = true, tyCustomFunction = ZipType) { eval, args ->
        val argsList = args.positionalAll()
        val heap = eval.heap()
        val v = mutableListOf<Value>()
        var first = true
        for (arg in argsList) {
            var idx = 0
            for (e in arg.iterate(heap)) {
                if (first) {
                    v.add(heap.alloc(listOf(e)))
                    idx += 1
                } else if (idx < v.size) {
                    v[idx] = v[idx].add(heap.alloc(listOf(e)), heap)
                    idx += 1
                }
            }
            // Truncate to shortest
            while (v.size > idx) {
                v.removeAt(v.size - 1)
            }
            first = false
        }
        heap.allocList(v)
    }
}
