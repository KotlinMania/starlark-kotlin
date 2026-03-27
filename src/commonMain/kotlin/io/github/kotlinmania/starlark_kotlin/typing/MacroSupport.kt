// port-lint: source src/typing/macro_support.rs
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

// doc(hidden)

// Placeholder types referenced from other modules
// These will be replaced with real imports as the port progresses
// TODO: stub - Ty needs real import
class Ty {
    companion object {
        fun any(): Ty = Ty()
        fun unions(tys: List<Ty>): Ty = Ty()
    }

    fun iterUnion(): List<TyBasic> = emptyList()
}

// TODO: stub - TyBasic needs real import
sealed class TyBasic {
    // TODO: stub - Tuple needs real import
    class Tuple(val item: TupleItem) : TyBasic()
    // TODO: stub - Dict needs real import
    class Dict(val key: Any, val value: DictValue) : TyBasic()
    // TODO: stub - Other needs real import
    class Other : TyBasic()
}

class TupleItem {
    fun itemTy(): Ty = Ty()
}

class DictValue {
    fun toTy(): Ty = Ty()
}

internal fun unpackArgsItemTy(ty: Ty): Ty {
    return Ty.unions(
        ty.iterUnion().map { basic ->
            when (basic) {
                is TyBasic.Tuple -> basic.item.itemTy()
                else -> Ty.any()
            }
        }
    )
}

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
