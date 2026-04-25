// port-lint: source src/values/types/tuple/unpack.rs
package io.github.kotlinmania.starlark_kotlin.values.types.tuple

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

/** Unpack a value of type `tuple[T, ...]` into a list. */
// #[derive(Debug, Clone, Eq, PartialEq, Hash, Ord, PartialOrd)]
// pub struct UnpackTuple<T> { pub items: Vec<T> }
data class UnpackTuple<T>(
    /** Unpacked items. */
    val items: MutableList<T>,
) : Iterable<T> {

    // impl Default for UnpackTuple<T>
    constructor() : this(mutableListOf())

    // impl IntoIterator for UnpackTuple<T>
    // fn into_iter(self) -> Self::IntoIter
    override fun iterator(): Iterator<T> = items.iterator()

    // impl IntoIterator for &'a mut UnpackTuple<T>
    fun iterMut(): MutableIterator<T> = items.iterator()

    companion object {
        fun <T> default(): UnpackTuple<T> = UnpackTuple()
    }
}

/**
 * [UnpackValue] implementation for [UnpackTuple].
 *
 * Corresponds to Rust's `impl<'v, T: UnpackValue<'v>> UnpackValue<'v> for UnpackTuple<T>`.
 */
class UnpackTupleUnpackValue<T>(
    private val elementUnpacker: UnpackValue<T>,
) : UnpackValue<UnpackTuple<T>> {
    override fun starlarkTypeRepr(): Ty {
        return Ty.tupleOf(elementUnpacker.starlarkTypeRepr())
    }

    override fun unpackValueImpl(value: Value): Result<UnpackTuple<T>?> {
        val tuple = TupleRef.fromValue(value) ?: return Result.success(null)
        val items = ArrayList<T>(tuple.len())
        for (v in tuple.iter()) {
            val unpacked = elementUnpacker.unpackValueImpl(v).getOrElse {
                return Result.failure(it)
            }
            if (unpacked == null) {
                return Result.success(null)
            }
            items.add(unpacked)
        }
        return Result.success(UnpackTuple(items))
    }
}

/**
 * [StarlarkTypeRepr] implementation for [UnpackTuple].
 *
 * Corresponds to Rust's `impl<T: StarlarkTypeRepr> StarlarkTypeRepr for UnpackTuple<T>`.
 */
class UnpackTupleStarlarkTypeRepr<T : StarlarkTypeRepr>(
    private val elementRepr: T,
) : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty {
        return Ty.tupleOf(elementRepr.starlarkTypeRepr())
    }
}

// Rust: impl<'a, T> IntoIterator for &'a UnpackTuple<T>
fun <T> UnpackTuple<T>.iterRef(): Iterator<T> = items.iterator()
