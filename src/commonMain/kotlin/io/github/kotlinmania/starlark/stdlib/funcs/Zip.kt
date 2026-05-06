// port-lint: source stdlib/funcs/zip.rs
package io.github.kotlinmania.starlark.stdlib.funcs.zip

/*
 * Copyright 2018 The Starlark in Rust Authors.
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

import io.github.kotlinmania.starlarksyntax.codemap.Span as Span
import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.eval.runtime.positionalAll
import io.github.kotlinmania.starlark.typing.ParamSpec
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.typing.TyCallable
import io.github.kotlinmania.starlark.typing.TyCallArgs
import io.github.kotlinmania.starlark.typing.TyCustomFunctionImpl
import io.github.kotlinmania.starlark.typing.TypingOrInternalError
import io.github.kotlinmania.starlark.typing.oracle.TypingOracleCtx
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.allocTuple
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.types.list.allocList

class ZipType : TyCustomFunctionImpl {
    override fun asCallable(): TyCallable {
        return TyCallable.new(ParamSpec.args(Ty.iter(Ty.any())), Ty.list(Ty.any()))
    }

    override fun validateCall(
        _span: Span,
        args: TyCallArgs,
        oracle: TypingOracleCtx,
    ): Result<Ty> {
        val iterItemTypes = mutableListOf<Ty>()
        for (pos in args.pos) {
            val itemTy = oracle.iterItem(pos).getOrThrow()
            iterItemTypes.add(itemTy)
        }
        return if (args.args != null) {
            Result.success(Ty.list(Ty.any()))
        } else {
            Result.success(Ty.list(Ty.tuple(iterItemTypes)))
        }
    }

    override fun equals(other: Any?): Boolean = other is ZipType
    override fun hashCode(): Int = this::class.hashCode()
    override fun toString(): String = "ZipType"
}

/**
 * [zip](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#zip
 * ): zip several iterables together
 *
 * `zip()` returns a new list of n-tuples formed from corresponding
 * elements of each of the n iterable sequences provided as arguments to
 * `zip`.  That is, the first tuple contains the first element of each of
 * the sequences, the second element contains the second element of each
 * of the sequences, and so on.  The result list is only as long as the
 * shortest of the input sequences.
 *
 * ```
 * zip()                           == []
 * zip(range(5))                   == [(0,), (1,), (2,), (3,), (4,)]
 * zip(range(5), "abc".elems())    == [(0, "a"), (1, "b"), (2, "c")]
 * ```
 */
fun zip(
    args: List<Value>,
    heap: Heap,
): Result<List<Value>> {
    val v = mutableListOf<Value>()
    var first = true
    for (arg in args) {
        var idx = 0
        val iter = arg.iterate(heap).getOrThrow()
        for (e in iter) {
            if (first) {
                v.add(heap.allocTuple(listOf(e)))
                idx += 1
            } else if (idx < v.size) {
                v[idx] = v[idx].add(heap.allocTuple(listOf(e)), heap).getOrThrow()
                idx += 1
            }
        }
        if (v.size > idx) {
            v.subList(idx, v.size).clear()
        }
        first = false
    }
    return Result.success(v)
}

fun registerZip(globals: GlobalsBuilder) {
    globals.setFunction("zip") { args, eval ->
        val positional = args.positionalAll()
        eval.heap().allocList(zip(positional, eval.heap()).getOrThrow())
    }
}
