// port-lint: source src/values/types/int/i32.rs
package io.github.kotlinmania.starlark.values.types.int

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

// use std::any;

// use crate::typing::Ty;
// use crate::values::AllocFrozenValue;
// use crate::values::AllocValue;
// use crate::values::FrozenHeap;
// use crate::values::FrozenValue;
// use crate::values::Heap;
// use crate::values::UnpackValue;
// use crate::values::Value;
// use crate::values::int::pointer_i32::PointerI32;
// use crate::values::layout::value::IntegerTooBigError;
// use crate::values::type_repr::StarlarkTypeRepr;
// use crate::values::types::int::int_or_big::StarlarkInt;
// use crate::values::types::int::int_or_big::StarlarkIntRef;

import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.layout.IntegerTooBigError
import io.github.kotlinmania.starlark.values.layout.Value

// impl<'v> AllocValue<'v> for i32
// impl AllocFrozenValue for i32

/**
 * Allocate an [Int] (Rust `i32`) on the Starlark heap.
 *
 * Delegates to [StarlarkInt.from] for the conversion.
 */
fun allocValueI32(value: Int): StarlarkInt = StarlarkInt.from(value)

// impl StarlarkTypeRepr for i32
//     fn starlark_type_repr() -> Ty { PointerI32::starlark_type_repr() }
fun i32StarlarkTypeRepr(): Ty = Ty.int()

// impl UnpackValue<'_> for i32 {
//     type Error = crate::Error;

/**
 * Unpack an [Int] (Rust `i32`) from a Starlark [Value].
 *
 * Note this does not use `Value.unpackInteger()` because unlike other call sites,
 * we know that `i32` is `InlineInt` on 64-bit platforms and never `BigInt`,
 * so this is faster.
 */
// fn unpack_value_impl(value: Value) -> crate::Result<Option<Self>>
fun unpackValueI32(value: Value): Result<Int?> {
    // Fast path: try to unpack as inline i32
    val v = value.unpackI32()
    if (v != null) {
        return Result.success(v)
    }
    // Slow path: check if it's a bigger int (error) or not an int at all (null)
    val int = StarlarkIntRef.unpack(value)
    if (int != null) {
        return Result.failure(
            IntegerTooBigError(
                value = int.toString(),
                integerType = "Int",
            ),
        )
    }
    return Result.success(null)
}
