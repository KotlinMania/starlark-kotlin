// port-lint: source src/values/typing/type_type.rs
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

import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark.values.UnpackValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.typing.ty.AbstractType

/** Represent a type of type. (For example, an expression `int` is valid for this type.) */
class TypeType private constructor() :
    StarlarkTypeRepr,
    UnpackValue<TypeType> {

        override fun starlarkTypeRepr(): Ty = AbstractType.starlarkTypeRepr()

        override fun unpackValueImpl(value: Value): Result<TypeType?> =
            if (value.vtable().hasEvalType) {
                Result.success(TypeType())
            } else {
                Result.success(null)
            }

        companion object {
            /** Validate the value is a type. */
            fun unpackValue(value: Value): TypeType? =
                if (value.vtable().hasEvalType) {
                    TypeType()
                } else {
                    null
                }
        }
    }

// Tests are in commonTest, not here.
