// port-lint: source src/__derive_refs/parse_args.rs
package io.github.kotlinmania.starlark_kotlin.__derive_refs.parse_args

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

import io.github.kotlinmania.starlark_kotlin.eval.Arguments
import io.github.kotlinmania.starlark_kotlin.eval.ParametersSpec
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.Heap
import io.github.kotlinmania.starlark_kotlin.values.UnpackValue
import io.github.kotlinmania.starlark_kotlin.values.ValueError
import io.github.kotlinmania.starlark_kotlin.values.layout.Value

/**
 * Collect `N` arguments.
 *
 * This function is called by generated code.
 */
// pub fn parse_signature<'v, const N: usize>(...) -> crate::Result<[Option<Value<'v>>; N]>
fun parseSignature(
    parser: ParametersSpec<FrozenValue>,
    args: Arguments,
    heap: Heap,
): List<Value?> {
    return parser.collectInto(args, heap)
}

/**
 * Parse positional-only arguments, required and optional.
 */
// pub fn parse_positional<'v, const R: usize, const O: usize>(...) -> Result<(...)>
fun parsePositional(
    args: Arguments,
    heap: Heap,
    requiredCount: Int,
    optionalCount: Int,
): Pair<List<Value>, List<Value?>> {
    args.noNamedArgs()
    return args.optional(heap, requiredCount, optionalCount)
}

// pub fn parse_positional_kwargs_alloc<'v, 'a, const R: usize, const O: usize>(...) -> Result<(...)>
fun parsePositionalKwargsAlloc(
    args: Arguments,
    heap: Heap,
    requiredCount: Int,
    optionalCount: Int,
): Triple<List<Value>, List<Value?>, Value> {
    val (required, optional) = args.optional(heap, requiredCount, optionalCount)
    val kwargs = args.namesMap()
    val kwargsValue = heap.alloc(kwargs)
    return Triple(required, optional, kwargsValue)
}

/** Utility for checking a `this` parameter matches what you expect. */
// pub fn check_this<'v, T: UnpackValue<'v>>(this: Value<'v>) -> crate::Result<T>
inline fun <reified T> checkThis(thisValue: Value): T {
    return UnpackValue.unpackNamedParam<T>(thisValue, "this")
}

/** Utility for checking a required parameter matches what you expect. */
// pub fn check_required<'v, T: UnpackValue<'v>>(name: &str, x: Option<Value<'v>>) -> crate::Result<T>
inline fun <reified T> checkRequired(name: String, x: Value?): T {
    val value = x ?: throw ValueError.MissingRequired(name)
    return UnpackValue.unpackNamedParam<T>(value, name)
}

/** Utility for checking an optional parameter matches what you expect. */
// pub fn check_optional<'v, T: UnpackValue<'v>>(name: &str, x: Option<Value<'v>>) -> crate::Result<Option<T>>
inline fun <reified T> checkOptional(name: String, x: Value?): T? {
    return when (x) {
        null -> null
        else -> UnpackValue.unpackNamedParam<T>(x, name)
    }
}

// pub fn check_defaulted<'v, T: UnpackValue<'v>>(name: &str, x: Option<Value<'v>>, default: ...) -> crate::Result<T>
inline fun <reified T> checkDefaulted(name: String, x: Value?, default: () -> T): T {
    return checkOptional<T>(name, x) ?: default()
}

/** We already know the parameter is set, so we just unpack it. */
// pub fn check_unpack<'v, T: UnpackValue<'v>>(name: &str, x: Value<'v>) -> crate::Result<T>
inline fun <reified T> checkUnpack(name: String, x: Value): T {
    return UnpackValue.unpackNamedParam<T>(x, name)
}
