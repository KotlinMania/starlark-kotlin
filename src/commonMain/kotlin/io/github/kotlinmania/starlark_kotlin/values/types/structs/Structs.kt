// port-lint: source src/values/types/structs/structs.rs
package io.github.kotlinmania.starlark_kotlin.values.types.structs

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

/**
 * Implementation of `struct` function.
 */

import io.github.kotlinmania.starlark_kotlin.environment.GlobalsBuilder
import io.github.kotlinmania.starlark_kotlin.typing.ParamSpec
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.typing.TyStruct
import io.github.kotlinmania.starlark_kotlin.typing.callable.TyCallable
import io.github.kotlinmania.starlark_kotlin.typing.error.TypingOrInternalError
import io.github.kotlinmania.starlark_kotlin.typing.function.TyCustomFunctionImpl
import io.github.kotlinmania.starlark_kotlin.typing.oracle.ctx.TypingOracleCtx
import io.github.kotlinmania.starlark_kotlin.values.types.namespace.Arguments
import io.github.kotlinmania.starlark_kotlin.util.ArcStr
import io.github.kotlinmania.starlark_kotlin.typing.TyCallArgs
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.types.function
import io.github.kotlinmania.starlark_kotlin.stdlib.new
import io.github.kotlinmania.starlark_kotlin.typing.ctx.named
import io.github.kotlinmania.starlark_kotlin.docs.kwargs
import io.github.kotlinmania.starlark_kotlin.analysis.unused_loads.pos
import io.github.kotlinmania.starlark_kotlin.analysis.node
import io.github.kotlinmania.starlark_kotlin.analysis.span
import io.github.kotlinmania.starlark_kotlin.codemap.Span

/**
 * Type implementation for the struct type.
 */
internal object StructType : TyCustomFunctionImpl {
    override fun asCallable(): TyCallable {
        // Note: this should be obtained from function signature from function definition.
        return TyCallable(ParamSpec.kwargs(Ty.any()), Ty.anyStruct())
    }

    override fun validateCall(
        span: Span,
        args: TyCallArgs,
        oracle: TypingOracleCtx
    ): Result<Ty> {
        if (args.pos.isNotEmpty()) {
            val pos = args.pos.first()
            return Result.failure(
                TypingOrInternalError.fromMessage(
                    oracle,
                    pos.span,
                    "Positional arguments not allowed"
                )
            )
        }

        val fields = mutableMapOf<ArcStr, Ty>()
        for (named in args.named) {
            val (name, ty) = named.node
            fields[ArcStr.from(name)] = ty
        }

        val extra = args.kwargs != null

        return Result.success(
            Ty.custom(
                TyStruct(
                    fields = fields.toSortedMap(),
                    extra = extra
                )
            )
        )
    }
}

/**
 * Register `struct` builtin.
 *
 * This function is the Kotlin equivalent of the Rust `#[starlark_module]` annotated function.
 * In Rust, the macro generates the necessary registration code; in Kotlin, we implement
 * this explicitly.
 */
internal fun registerStruct(builder: GlobalsBuilder) {
    builder.function(
        name = "struct",
        tyCustomFunction = StructType,
        asType = FrozenStruct::class
    ) { args: Arguments<*>, heap: Heap<*> ->
        args.noPositionalArgs(heap)

        // Note: missing optimization: practically most `struct` invocations are
        // performed with fixed named arguments, e.g. `struct(a = 1, b = 2)`.
        // In this case we can avoid allocating the map, but instead
        // allocate field index once at compilation time and store field values in a vector.

        Result.success(Struct.new(args.namesMap()))
    }
}
