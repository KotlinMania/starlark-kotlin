// port-lint: source src/values/types/listOrTuple.rs
package io.github.kotlinmania.starlark.values.types

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

/**
 * Utility for unpacking a value of type `list[T]` or `tuple[T, ...]` into a list.
 *
 */

import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.Either
import io.github.kotlinmania.starlark.values.EitherUnpackValue
import io.github.kotlinmania.starlark.values.EitherTypeRepr
import io.github.kotlinmania.starlark.values.UnpackValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.types.list.UnpackListStarlarkTypeRepr
import io.github.kotlinmania.starlark.values.types.list.UnpackListUnpackValue
import io.github.kotlinmania.starlark.values.types.tuple.UnpackTupleStarlarkTypeRepr
import io.github.kotlinmania.starlark.values.types.tuple.UnpackTupleUnpackValue

/** Unpack a value of type `list[T]` or `tuple[T, ...]` into a list. */
data class UnpackListOrTuple<T>(
    /** Unpacked items of the list or tuple. */
    val items: MutableList<T>,
) : Iterable<T> {

    constructor() : this(mutableListOf())

    override fun iterator(): Iterator<T> = items.iterator()

    fun iterMut(): MutableIterator<T> = items.iterator()

    companion object {
        fun <T> default(): UnpackListOrTuple<T> = UnpackListOrTuple()
    }
}

/**
 * [UnpackValue] implementation for [UnpackListOrTuple].
 *
 * ```
 * }
 * ```
 *
 * Kotlin takes a value-level element unpacker instance of [UnpackValue].
 */
class UnpackListOrTupleUnpackValue<T>(
    private val elementUnpacker: UnpackValue<T>,
) : UnpackValue<UnpackListOrTuple<T>> {

    override fun starlarkTypeRepr(): Ty {
        val eitherTypeRepr = EitherTypeRepr(
            left = UnpackListStarlarkTypeRepr(elementUnpacker),
            right = UnpackTupleStarlarkTypeRepr(elementUnpacker),
        )
        return eitherTypeRepr.starlarkTypeRepr()
    }

    override fun unpackValueImpl(value: Value): Result<UnpackListOrTuple<T>?> {
        val eitherUnpacker = EitherUnpackValue(
            left = UnpackListUnpackValue(elementUnpacker),
            right = UnpackTupleUnpackValue(elementUnpacker),
        )
        val unpacked = eitherUnpacker.unpackValueImpl(value).getOrElse {
            return Result.failure(it)
        } ?: return Result.success(null)

        return when (unpacked) {
            is Either.Left -> Result.success(UnpackListOrTuple(unpacked.value.items))
            is Either.Right -> Result.success(UnpackListOrTuple(unpacked.value.items))
        }
    }
}

fun <T> UnpackListOrTuple<T>.iterRef(): Iterator<T> = items.iterator()
