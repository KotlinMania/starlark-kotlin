// port-lint: source src/values/types/list/unpack.rs
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
import io.github.kotlinmania.starlark_kotlin.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark_kotlin.values.UnpackValue
import io.github.kotlinmania.starlark_kotlin.values.layout.Value

/**
 * Unpack a value of type `list<T>` into a list of items.
 *
 * @param T The expected element type, which must implement [StarlarkTypeRepr].
 * @property items The unpacked list items.
 */
data class UnpackList<T>(
    /** Unpacked items. */
    val items: MutableList<T>,
) : Iterable<T> {

    /** Create an empty [UnpackList]. */
    constructor() : this(mutableListOf())

    /** Returns an iterator over the items. */
    override fun iterator(): Iterator<T> = items.iterator()

    /** Returns a mutable iterator over the items. */
    fun iterMut(): MutableIterator<T> = items.iterator()

    companion object {
        /** Creates a default empty [UnpackList]. */
        fun <T> default(): UnpackList<T> = UnpackList()
    }
}

/**
 * [UnpackValue] implementation for [UnpackList].
 *
 * Attempts to unpack a [Value] as a list, then unpacks each element
 * using the provided element unpacker.
 *
 * @param T The target type for each list element.
 * @property elementUnpacker The [UnpackValue] used to unpack individual elements.
 */
class UnpackListUnpackValue<T>(
    private val elementUnpacker: UnpackValue<T>,
) : UnpackValue<UnpackList<T>> {

    override fun starlarkTypeRepr(): Ty {
        return Ty.list(elementUnpacker.starlarkTypeRepr())
    }

    override fun unpackValueImpl(value: Value): Result<UnpackList<T>?> {
        val listRef = ListRef.fromValue(value) ?: return Result.success(null)
        val items = mutableListOf<T>()
        for (v in listRef.iter()) {
            val unpacked = elementUnpacker.unpackValueImpl(v).getOrElse {
                return Result.failure(it)
            }
            if (unpacked == null) {
                return Result.success(null)
            }
            items.add(unpacked)
        }
        return Result.success(UnpackList(items))
    }
}

/**
 * [StarlarkTypeRepr] implementation for [UnpackList].
 *
 * Delegates to [ListType]'s type representation.
 */
class UnpackListStarlarkTypeRepr<T : StarlarkTypeRepr>(
    private val elementRepr: T,
) : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty {
        return Ty.list(elementRepr.starlarkTypeRepr())
    }
}
