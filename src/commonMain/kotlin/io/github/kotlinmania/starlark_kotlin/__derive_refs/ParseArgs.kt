// port-lint: source src/__derive_refs/parse_args.rs
package io.github.kotlinmania.starlark_kotlin.__derive_refs

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
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.UnpackValue
import io.github.kotlinmania.starlark_kotlin.values.ValueError
import io.github.kotlinmania.starlark_kotlin.eval.bc.ParametersSpec
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.unpackNamedParam
import io.github.kotlinmania.starlark_kotlin.values.types.tuple.it
import io.github.kotlinmania.starlark_kotlin.eval.runtime.params.spec.collectInto

/// Collect `N` arguments.
///
/// This function is called by generated code.
fun parseSignature(
    parser: ParametersSpec<FrozenValue>,
    args: Arguments,
    heap: Heap,
): Result<Array<Value?>> {
    return parser.collectInto(args, heap)
}

/// Parse positional-only arguments, required and optional.
fun parsePositional(
    args: Arguments,
    heap: Heap,
    requiredCount: Int,
    optionalCount: Int,
): Result<Pair<Array<Value>, Array<Value?>>> {
    args.noNamedArgs().getOrElse { return Result.failure(it) }
    return args.optional(heap, requiredCount, optionalCount)
}

fun parsePositionalKwargsAlloc(
    args: Arguments,
    heap: Heap,
    requiredCount: Int,
    optionalCount: Int,
): Result<Triple<Array<Value>, Array<Value?>, Value>> {
    val (required, optional) = args.optional(heap, requiredCount, optionalCount)
        .getOrElse { return Result.failure(it) }
    val namesMap = args.namesMap().getOrElse { return Result.failure(it) }
    val kwargs = heap.alloc(namesMap)
    return Result.success(Triple(required, optional, kwargs))
}

/// Utility for checking a `this` parameter matches what you expect.
inline fun <reified T> checkThis(thisValue: Value): Result<T>
    where T : UnpackValue
{
    return T.unpackNamedParam(thisValue, "this")
}

/// Utility for checking a required parameter matches what you expect.
inline fun <reified T> checkRequired(name: String, x: Value?): Result<T>
    where T : UnpackValue
{
    val value = x ?: return Result.failure(ValueError.MissingRequired(name))
    return T.unpackNamedParam(value, name)
}

/// Utility for checking an optional parameter matches what you expect.
inline fun <reified T> checkOptional(name: String, x: Value?): Result<T?>
    where T : UnpackValue
{
    if (x == null) return Result.success(null)
    return T.unpackNamedParam(x, name).map { it }
}

inline fun <reified T> checkDefaulted(
    name: String,
    x: Value?,
    default: () -> T,
): Result<T>
    where T : UnpackValue
{
    val optional = checkOptional<T>(name, x).getOrElse { return Result.failure(it) }
    return Result.success(optional ?: default())
}

/// We already know the parameter is set, so we just unpack it.
inline fun <reified T> checkUnpack(name: String, x: Value): Result<T>
    where T : UnpackValue
{
    return T.unpackNamedParam(x, name)
}
