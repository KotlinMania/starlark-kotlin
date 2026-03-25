// port-lint: source src/values/typing/type_type.rs
package io.github.kotlinmania.starlark_kotlin.values.typing.type_type

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

import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.typing.TyStarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.UnpackValue
import io.github.kotlinmania.starlark_kotlin.values.typing.AbstractType
import io.github.kotlinmania.starlark_kotlin.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.tests.derive.starlarkTypeRepr

/// Represent a type of type. (For example, an expression `int` is valid for this type.)
// pub struct TypeType(());
class TypeType private constructor() : StarlarkTypeRepr {

    // impl StarlarkTypeRepr for TypeType
    // type Canonical = AbstractType;
    // fn starlark_type_repr() -> Ty
    override fun starlarkTypeRepr(): Ty {
        return AbstractType.starlarkTypeRepr()
    }

    companion object {
        // static starlark_type_repr
        fun starlarkTypeRepr(): Ty {
            return AbstractType.starlarkTypeRepr()
        }

        /// Validate the value is type.
        // impl UnpackValue for TypeType
        // fn unpack_value_impl(value: Value) -> Result<Option<Self>, Self::Error>
        fun unpackValueImpl(value: Value): TypeType? {
            return if (TyStarlarkValue.isTypeFromVtable(value)) {
                TypeType()
            } else {
                null
            }
        }
    }
}

// #[cfg(test)] mod tests
// Tests are in commonTest, not here.
