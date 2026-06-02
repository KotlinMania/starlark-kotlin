// port-lint: source src/values/types/float/unpack.rs
package io.github.kotlinmania.starlark.values.types.float

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

// use crate::typing::Ty;
// use crate::values::UnpackValue;
// use crate::values::Value;
// use crate::values::type_repr::StarlarkTypeRepr;
// use crate::values::types::num::value::Num;
// use crate::values::types::num::value::NumRef;

import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark.values.UnpackValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.types.num.NumRef

// / Unpack `int` or `float` into `f64`.
// pub struct UnpackFloat(pub f64);
class UnpackFloat(
    val value: Double,
) : StarlarkTypeRepr {
    // impl StarlarkTypeRepr for UnpackFloat
    //     type Canonical = <Num as StarlarkTypeRepr>::Canonical;
    //     fn starlark_type_repr() -> Ty
    override fun starlarkTypeRepr(): Ty = Ty.union2(Ty.int(), Ty.float())

    companion object : UnpackValue<UnpackFloat> {
        override fun starlarkTypeRepr(): Ty = Ty.union2(Ty.int(), Ty.float())

        // impl<'v> UnpackValue<'v> for UnpackFloat {
        //     type Error = <NumRef<'v> as UnpackValue<'v>>::Error;
        //     fn unpack_value_impl(value: Value<'v>) -> Result<Option<Self>, Self::Error>
        override fun unpackValueImpl(value: Value): Result<UnpackFloat?> {
            val num = NumRef.unpackValueImpl(value) ?: return Result.success(null)
            return Result.success(UnpackFloat(num.asFloat()))
        }
    }
}

// #[cfg(test)]
// mod tests {
//     fn test_unpack_float()
// }
