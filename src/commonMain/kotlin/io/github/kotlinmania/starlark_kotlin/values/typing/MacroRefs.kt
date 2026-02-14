// port-lint: source src/values/typing/macro_refs.rs
package io.github.kotlinmania.starlark_kotlin.values.typing

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

import io.github.kotlinmania.starlark_kotlin.values.Heap
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.Value
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.compiled.TypeCompiled

// #[derive(Debug, thiserror::Error)]
// enum TypingMacroRefsError
private sealed class TypingMacroRefsError(message: String) : Exception(message) {
    // #[error("LHS is not a type: `{0}`")]
    // LhsNotType(String)
    class LhsNotType(repr: String) : TypingMacroRefsError("LHS is not a type: `$repr`")
}

/// Implementation of `bit_or` for `StarlarkValue` implementations which are types.
// pub fn starlark_value_bit_or_for_type<'v, S: StarlarkValue<'v>>(
//     this: &S,
//     other: Value<'v>,
//     heap: Heap<'v>,
// ) -> crate::Result<Value<'v>>
fun starlarkValueBitOrForType(
    thisValue: StarlarkValue,
    other: Value,
    heap: Heap,
): Result<Value> {
    val thisType = thisValue.evalType()
    if (thisType == null) {
        val repr = buildString { thisValue.collectRepr(this) }
        return Result.failure(TypingMacroRefsError.LhsNotType(repr))
    }
    val thisCompiled = TypeCompiled.fromTy(thisType, heap)
    val otherCompiled = TypeCompiled.new(other, heap).getOrElse { return Result.failure(it) }
    return Result.success(TypeCompiled.typeAnyOfTwo(thisCompiled, otherCompiled, heap).toInner())
}
