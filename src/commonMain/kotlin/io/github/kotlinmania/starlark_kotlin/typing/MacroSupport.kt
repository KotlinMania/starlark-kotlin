// port-lint: source src/typing/macro_support.rs
@file:Suppress("unused")

package io.github.kotlinmania.starlark_kotlin.typing

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

// #![doc(hidden)]

// pub(crate) fn unpack_args_item_ty(ty: Ty) -> Ty
/**
 * Given a type representing `*args`, extract the item type.
 *
 * For tuple types, returns the item type; otherwise returns [Ty.any].
 */
internal fun unpackArgsItemTy(ty: Ty): Ty {
    return Ty.unions(
        ty.iterUnion().map { basic ->
            when (basic) {
                is TyBasic.Tuple -> basic.itemTy()
                else -> Ty.any()
            }
        }
    )
}

// pub(crate) fn unpack_kwargs_value_ty(ty: Ty) -> Ty
/**
 * Given a type representing `**kwargs`, extract the value type.
 *
 * For dict types, returns the value type; otherwise returns [Ty.any].
 */
internal fun unpackKwargsValueTy(ty: Ty): Ty {
    return Ty.unions(
        ty.iterUnion().map { basic ->
            when (basic) {
                is TyBasic.Dict -> basic.value.toTy()
                else -> Ty.any()
            }
        }
    )
}
