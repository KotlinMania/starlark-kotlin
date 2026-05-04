// port-lint: source values/types/string/allocUnpack.rs
package io.github.kotlinmania.starlark.values.types.string

/*
 * Copyright 2019 The Starlark in Rust Authors.
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

/** Implementations of alloc and unpack traits for string. */

import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark.values.StringTypeRepr
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark.values.layout.typed.StringValue

fun String.allocFrozenValue(heap: FrozenHeap): FrozenValue {
    return this.allocFrozenStringValue(heap).toFrozenValue()
}

fun String.allocFrozenStringValue(heap: FrozenHeap): FrozenStringValue {
    return heap.allocStrIntern(this)
}

fun String.allocValue(heap: Heap): Value {
    return this.allocStringValue(heap).toValue()
}

fun String.allocStringValue(heap: Heap): StringValue {
    return heap.allocStr(this)
}

// Char uses the same type representation as String in Starlark because individual
// characters are represented as single-character strings. See StringTypeRepr in TypeRepr.kt.
object CharTypeRepr : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty = StringTypeRepr.starlarkTypeRepr()
}

fun Char.allocValue(heap: Heap): Value {
    return this.allocStringValue(heap).toValue()
}

fun Char.allocStringValue(heap: Heap): StringValue {
    return this.toString().allocStringValue(heap)
}

fun unpackValueImplBorrowedString(value: Value): Result<String?> {
    return Result.success(value.unpackStr())
}

fun unpackValueImplOwnedString(value: Value): Result<String?> {
    return Result.success(value.unpackStr())
}
