// port-lint: source values/types/list/list_type.rs
package io.github.kotlinmania.starlark.values.types.list

/*
 * Copyright 2018 The Starlark in Rust Authors.
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

import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark.values.UnpackValue
import io.github.kotlinmania.starlark.values.layout.Value

/**
 * A list type marker.
 *
 * [StarlarkTypeRepr] provides `list[T]`.
 * [UnpackValue] implementation verifies the types of items.
 *
 * This type is primarily used for type checking purposes: it validates
 * that a given [Value] is a list whose elements all unpack to [T].
 *
 * @param T The expected type of list elements, which must implement [StarlarkTypeRepr].
 */
class ListType<T : StarlarkTypeRepr> private constructor(
    private val elementRepr: Ty,
) : StarlarkTypeRepr {

    override fun starlarkTypeRepr(): Ty = Ty.list(elementRepr)

    companion object {
        /**
         * Create a [ListType] for the given element type representation.
         */
        fun <T : StarlarkTypeRepr> of(elementRepr: Ty): ListType<T> =
            ListType(elementRepr)

        /**
         * Create a [ListType] from a [StarlarkTypeRepr] instance for the element type.
         */
        fun <T : StarlarkTypeRepr> of(elementRepr: T): ListType<T> =
            ListType(elementRepr.starlarkTypeRepr())

        /**
         * Starlark type representation for `list[T]`.
         */
        fun starlarkTypeRepr(elementTy: Ty): Ty = Ty.list(elementTy)

        /**
         * Try to unpack a [Value] as a [ListType], verifying that all elements
         * match the expected type.
         *
         * Returns `null` if the value is not a list or any element does not
         * match the expected element type.
         */
        fun <T : StarlarkTypeRepr> unpackValue(
            value: Value,
            unpackElement: UnpackValue<T>,
        ): ListType<T>? {
            val list = ListRef.fromValue(value) ?: return null
            // Verify all elements unpack to the expected type
            for (v in list.iter()) {
                val unpacked = unpackElement.unpackValueImpl(v).getOrElse { return null }
                if (unpacked == null) return null
            }
            return ListType(unpackElement.starlarkTypeRepr())
        }
    }
}
