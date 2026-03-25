// port-lint: source src/stdlib/funcs/zip.rs
package io.github.kotlinmania.starlark_kotlin.stdlib.funcs

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
import io.github.kotlinmania.starlark_kotlin.typing.ParamSpec
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.typing.call_args.TyCallArgs
import io.github.kotlinmania.starlark_kotlin.typing.callable.TyCallable
import io.github.kotlinmania.starlark_kotlin.typing.error.TypingOrInternalError
import io.github.kotlinmania.starlark_kotlin.typing.function.TyCustomFunctionImpl
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.ValueOfUnchecked
import io.github.kotlinmania.starlark_kotlin.values.typing.StarlarkIter
import io.github.kotlinmania.starlark_kotlin.values.types.tuple.unpack.UnpackTuple
import io.github.kotlinmania.starlark_kotlin.syntax.ast.TypingOracleCtx
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.owned.asRef
import io.github.kotlinmania.starlark_kotlin.values.iterate
import io.github.kotlinmania.starlark_kotlin.typing.iterItem
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.TypingOracleCtx
import io.github.kotlinmania.starlark_kotlin.stdlib.add
import io.github.kotlinmania.starlark_kotlin.codemap.Span
import io.github.kotlinmania.starlark_kotlin.values.owned_frozen_ref.asRef

class ZipType : TyCustomFunctionImpl {
    override fun asCallable(): TyCallable {
        return TyCallable.new(ParamSpec.args(Ty.iter(Ty.any())), Ty.list(Ty.any()))
    }

    override fun validateCall(
        span: Span,
        args: TyCallArgs,
        oracle: TypingOracleCtx,
    ): Result<Ty> {
        val iterItemTypes = mutableListOf<Ty>()
        for (pos in args.pos) {
            val itemTy = oracle.iterItem(pos.asRef())
            iterItemTypes.add(itemTy)
        }
        return if (args.args != null) {
            Result.success(Ty.list(Ty.any()))
        } else {
            Result.success(Ty.list(Ty.tuple(iterItemTypes)))
        }
    }

    override fun equals(other: Any?): Boolean = other is ZipType
    override fun hashCode(): Int = javaClass.hashCode()
    override fun toString(): String = "ZipType"
}

/// [zip](
/// https://github.com/bazelbuild/starlark/blob/master/spec.md#zip
/// ): zip several iterables together
///
/// `zip()` returns a new list of n-tuples formed from corresponding
/// elements of each of the n iterable sequences provided as arguments to
/// `zip`.  That is, the first tuple contains the first element of each of
/// the sequences, the second element contains the second element of each
/// of the sequences, and so on.  The result list is only as long as the
/// shortest of the input sequences.
///
/// ```
/// zip()                           == []
/// zip(range(5))                   == [(0,), (1,), (2,), (3,), (4,)]
/// zip(range(5), "abc".elems())    == [(0, "a"), (1, "b"), (2, "c")]
/// ```
fun zip(
    args: UnpackTuple<ValueOfUnchecked<StarlarkIter<FrozenValue>>>,
    heap: Heap,
): Result<List<Value>> {
    val v = mutableListOf<Value>()
    var first = true
    for (arg in args.items) {
        var idx = 0
        for (e in arg.get().iterate(heap)) {
            if (first) {
                v.add(heap.alloc(listOf(e)))
                idx += 1
            } else if (idx < v.size) {
                v[idx] = v[idx].add(heap.alloc(listOf(e)), heap)
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
    globals.set("zip", ::zip)
}
