// port-lint: source src/__derive_refs/parse_args.rs
package io.github.kotlinmania.starlark.__derive_refs

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

import io.github.kotlinmania.starlark_kotlin.eval.runtime.Arguments
import io.github.kotlinmania.starlark_kotlin.eval.runtime.params.spec.ParametersSpec
import io.github.kotlinmania.starlark_kotlin.values.UnpackValue
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.types.dict.Dict
import io.github.kotlinmania.starlark_kotlin.values.types.dict.allocValue
import io.github.kotlinmania.starlark_kotlin.values.ValueError
import io.github.kotlinmania.starlark_kotlin.collections.SmallMap

/**
 * Collect `N` arguments.
 *
 * This function is called by generated code.
 */
fun parseSignature(
    parser: ParametersSpec<FrozenValue>,
    args: Arguments,
    heap: Heap,
): Result<List<Value?>> {
    return Result.success(parser.collectInto(parser.len(), args, heap))
}

/** Parse positional-only arguments, required and optional. */
fun parsePositional(
    args: Arguments,
    heap: Heap,
    requiredCount: Int,
    optionalCount: Int,
): Result<Pair<List<Value>, List<Value?>>> {
    args.noNamedArgs().getOrElse { return Result.failure(it) }
    return args.optional(requiredCount, optionalCount, heap)
}

fun parsePositionalKwargsAlloc(
    args: Arguments,
    heap: Heap,
    requiredCount: Int,
    optionalCount: Int,
): Result<Triple<List<Value>, List<Value?>, Value>> {
    val (required, optional) = args.optional(requiredCount, optionalCount, heap)
        .getOrElse { return Result.failure(it) }
    val namesMap = args.namesMap().getOrElse { return Result.failure(it) }
    val kwargs = Dict.new(namesMap as SmallMap<Value, Value>).allocValue(heap)
    return Result.success(Triple(required, optional, kwargs))
}

/** Utility for checking a `this` parameter matches what you expect. */
fun <T> checkThis(unpack: UnpackValue<T>, thisValue: Value): Result<T> {
    return Result.success(unpack.unpackNamedParam(thisValue, "this"))
}

/** Utility for checking a required parameter matches what you expect. */
fun <T> checkRequired(unpack: UnpackValue<T>, name: String, x: Value?): Result<T> {
    val value = x ?: return Result.failure(ValueError.MissingRequired(name))
    return Result.success(unpack.unpackNamedParam(value, name))
}

/** Utility for checking an optional parameter matches what you expect. */
fun <T> checkOptional(unpack: UnpackValue<T>, name: String, x: Value?): Result<T?> {
    if (x == null) return Result.success(null)
    return Result.success(unpack.unpackNamedParam(x, name))
}

fun <T> checkDefaulted(
    unpack: UnpackValue<T>,
    name: String,
    x: Value?,
    default: () -> T,
): Result<T> {
    val optional = checkOptional(unpack, name, x).getOrElse { return Result.failure(it) }
    return Result.success(optional ?: default())
}

/** We already know the parameter is set, so we just unpack it. */
fun <T> checkUnpack(unpack: UnpackValue<T>, name: String, x: Value): Result<T> {
    return Result.success(unpack.unpackNamedParam(x, name))
}
