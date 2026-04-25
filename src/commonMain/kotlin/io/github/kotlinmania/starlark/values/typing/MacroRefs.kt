// port-lint: source src/values/typing/macro_refs.rs
package io.github.kotlinmania.starlark.values.typing

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

import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.typing.typecompiled.TypeCompiled

private class TypingMacroRefsError(repr: String) : Exception("LHS is not a type: `$repr`")

/** Implementation of `bitOr` for [StarlarkValue] implementations which are types. */
fun starlarkValueBitOrForType(
    thisValue: StarlarkValue,
    other: Value,
    heap: Heap,
): Result<Value> {
    val evalType = thisValue.evalType()
        ?: run {
            val repr = buildString { thisValue.collectRepr(this) }
            return Result.failure(TypingMacroRefsError(repr))
        }
    val thisCompiled = TypeCompiled.fromTy(evalType, heap)
    val otherCompiled = TypeCompiled.new(other, heap)
    return Result.success(TypeCompiled.typeAnyOfTwo(thisCompiled, otherCompiled, heap).toInner())
}
