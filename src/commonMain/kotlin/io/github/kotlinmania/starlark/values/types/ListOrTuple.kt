// port-lint: source src/values/types/list_or_tuple.rs
package io.github.kotlinmania.starlark.values.types

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

/**
 * Utility for unpacking a value of type `list[T]` or `tuple[T, ...]` into a list.
 *
 * Corresponds to Rust's `src/values/types/list_or_tuple.rs`.
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
// #[derive(Debug, Clone, Eq, PartialEq, Hash, Ord, PartialOrd)]
// pub struct UnpackListOrTuple<T> { pub items: Vec<T> }
data class UnpackListOrTuple<T>(
    /** Unpacked items of the list or tuple. */
    val items: MutableList<T>,
) : Iterable<T> {

    // impl Default for UnpackListOrTuple<T>
    constructor() : this(mutableListOf())

    // impl IntoIterator for UnpackListOrTuple<T>
    // fn into_iter(self) -> Self::IntoIter
    override fun iterator(): Iterator<T> = items.iterator()

    // impl IntoIterator for &'a mut UnpackListOrTuple<T>
    fun iterMut(): MutableIterator<T> = items.iterator()

    companion object {
        fun <T> default(): UnpackListOrTuple<T> = UnpackListOrTuple()
    }
}

/**
 * [UnpackValue] implementation for [UnpackListOrTuple].
 *
 * In Rust:
 * ```
 * impl<'v, T: UnpackValue<'v>> UnpackValue<'v> for UnpackListOrTuple<T> {
 *   type Error = <T as UnpackValue<'v>>::Error;
 *   fn unpack_value_impl(value: Value<'v>) -> Result<Option<Self>, Self::Error> { ... }
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
        // Rust: Either::<UnpackList<T>, UnpackTuple<T>>::unpack_value_impl(value)
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

// Rust: impl<'a, T> IntoIterator for &'a UnpackListOrTuple<T>
fun <T> UnpackListOrTuple<T>.iterRef(): Iterator<T> = items.iterator()
