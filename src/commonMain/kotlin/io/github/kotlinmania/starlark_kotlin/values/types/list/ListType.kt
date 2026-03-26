// port-lint: source src/values/types/list/list_type.rs
package io.github.kotlinmania.starlark_kotlin.values.types.list

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

import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.UnpackValue
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.type_repr.StarlarkTypeRepr

/**
 * A list type marker.
 *
 * [StarlarkTypeRepr] provides `list[T]`.
 * [UnpackValue] implementation verifies the types of items.
 */
class ListType<T : StarlarkTypeRepr> private constructor() : StarlarkTypeRepr {
    // impl StarlarkTypeRepr for ListType
    override fun starlarkTypeRepr(): Ty {
        return Ty.list(T::class) // Kotlin: simplified, actual impl uses reified type
    }

    companion object {
        fun <T : StarlarkTypeRepr> unpackValue(value: Value): ListType<T>? {
            val list = UnpackList.unpackValue<T>(value) ?: return null
            return ListType()
        }
    }
}
