// port-lint: source src/values/types/list/globals.rs
package io.github.kotlinmania.starlark_kotlin.values.types.list

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
import io.github.kotlinmania.starlark_kotlin.typing.TyFunction
import io.github.kotlinmania.starlark_kotlin.typing.callable.TyCallable
import io.github.kotlinmania.starlark_kotlin.typing.error.TypingOrInternalError
import io.github.kotlinmania.starlark_kotlin.typing.function.TyCustomFunctionImpl
import io.github.kotlinmania.starlark_kotlin.values.ValueOfUnchecked
import io.github.kotlinmania.starlark_kotlin.values.typing.StarlarkIter
import io.github.kotlinmania.starlark_kotlin.values.types.SpecialBuiltinFunction
import io.github.kotlinmania.starlark_kotlin.syntax.ast.TypingOracleCtx
import io.github.kotlinmania.starlark_kotlin.typing.TyCallArgs
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.types.tuple.it
import io.github.kotlinmania.starlark_kotlin.values.types.string.registerFunction
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.allocList
import io.github.kotlinmania.starlark_kotlin.typing.iterItem
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.TypingOracleCtx
import io.github.kotlinmania.starlark_kotlin.typing.callable.validateFnCall
import io.github.kotlinmania.starlark_kotlin.analysis.unused_loads.pos
import io.github.kotlinmania.starlark_kotlin.analysis.node
import io.github.kotlinmania.starlark_kotlin.codemap.Spanned
import io.github.kotlinmania.starlark_kotlin.codemap.Span

object ListType : TyCustomFunctionImpl {
    override fun isType(): Boolean {
        return true
    }

    override fun asCallable(): TyCallable {
        return LIST.callable
    }

    override fun validateCall(
        span: Span,
        args: TyCallArgs,
        oracle: TypingOracleCtx
    ): Result<Ty> {
        oracle.validateFnCall(span, LIST.callable, args).getOrElse { return Result.failure(it) }

        if (args.pos.firstOrNull() != null) {
            val arg = args.pos.first()
            // This is infallible after the check above.
            val item = oracle.iterItem(Spanned(span, arg.node)).getOrElse { return Result.failure(it) }
            return Result.success(Ty.list(item))
        }

        return Result.success(Ty.anyList())
    }
}

private val LIST: TyFunction by lazy {
    TyFunction.newWithTypeAttr(
        ParamSpec.posOnly(emptyList(), listOf(Ty.iter(Ty.any()))),
        Ty.anyList(),
        Ty.anyList()
    )
}

internal fun registerList(globals: GlobalsBuilder) {
    /**
     * [list](
     * https://github.com/bazelbuild/starlark/blob/master/spec.md#list
     * ): construct a list.
     *
     * `list(x)` returns a new list containing the elements of the
     * iterable sequence x.
     *
     * With no argument, `list()` returns a new empty list.
     *
     * ```
     * # starlark::assert::all_true(r#"
     * list()        == []
     * list((1,2,3)) == [1, 2, 3]
     * # "#);
     * # starlark::assert::fail(r#"
     * list("strings are not iterable") # error: not supported
     * # "#, r#"not supported on type"#);
     * ```
     */
    globals.registerFunction(
        name = "list",
        asType = FrozenList::class,
        speculativeExecSafe = true,
        specialBuiltinFunction = SpecialBuiltinFunction.List,
        tyCustomFunction = ListType
    ) { a: ValueOfUnchecked<StarlarkIter<Value<*>>>?, heap: Heap<*> ->
        Result.success(ValueOfUnchecked.new(if (a != null) {
            val xs = ListRefImpl.fromValue(a.get())
            if (xs != null) {
                heap.allocList(xs.content())
            } else {
                val it = a.get().iterate(heap).getOrElse { return@registerFunction Result.failure(it) }
                AllocList(it).allocValue(heap)
            }
        } else {
            AllocList.EMPTY.allocValue(heap)
        }))
    }
}
