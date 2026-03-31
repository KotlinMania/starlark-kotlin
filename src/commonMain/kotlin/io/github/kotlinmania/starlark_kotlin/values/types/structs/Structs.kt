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
import io.github.kotlinmania.starlark_kotlin.typing.TyStarlarkValue
import io.github.kotlinmania.starlark_kotlin.typing.TyStruct
import io.github.kotlinmania.starlark_kotlin.typing.TyCallable
import io.github.kotlinmania.starlark_kotlin.typing.TyCustomFunctionImpl
import io.github.kotlinmania.starlark_kotlin.typing.TyCallArgs
import io.github.kotlinmania.starlark_kotlin.typing.oracle.TypingOracleCtx
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Arguments
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.codemap.Span
import starlark_map.small_map.SmallMap

/**
 * Type implementation for the struct type.
 */
internal object StructType : TyCustomFunctionImpl {
    override fun asCallable(): TyCallable {
        // Note: this should be obtained from function signature from function definition.
        return TyCallable.new(ParamSpec.kwargs(Ty.any()), Ty.anyStruct())
    }

    override fun validateCall(
        span: Span,
        args: TyCallArgs,
        oracle: TypingOracleCtx
    ): Result<Ty> {
        if (args.pos.isNotEmpty()) {
            val pos = args.pos.first()
            return Result.failure(
                oracle.msgError(
                    pos.span,
                    "Positional arguments not allowed"
                )
            )
        }

        val fields = mutableMapOf<String, Ty>()
        for (named in args.named) {
            val (name, ty) = named.node
            fields[name] = ty
        }

        val extra = args.kwargs != null

        return Result.success(
            Ty.custom(
                TyStruct(
                    fields = fields.toList().sortedBy { it.first.toString() }.toMap(),
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
    builder.setFunction(
        name = "struct",
        asType = Ty.starlarkValue(TyStarlarkValue.new("struct"))
    ) { args: Arguments, eval ->
        val heap = eval.heap()
        val noPosResult = args.noPositionalArgs(heap)
        if (noPosResult.isFailure) return@setFunction noPosResult

        // Note: missing optimization: practically most `struct` invocations are
        // performed with fixed named arguments, e.g. `struct(a = 1, b = 2)`.
        // In this case we can avoid allocating the map, but instead
        // allocate field index once at compilation time and store field values in a vector.

        val namesResult = args.namesMap()
        if (namesResult.isFailure) return@setFunction Result.failure<Any?>(namesResult.exceptionOrNull()!!)
        val namesMap = namesResult.getOrThrow()

        // Convert SmallMap<StringValue, Value> to SmallMap<String, Value>
        val fields = SmallMap.withCapacity<String, Value>(namesMap.len())
        for ((k, v) in namesMap.iter()) {
            fields.insert(k.asStr(), v)
        }
        Result.success(Struct(fields))
    }
}
