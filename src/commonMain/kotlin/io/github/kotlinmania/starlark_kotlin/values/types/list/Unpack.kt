<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/values/types/list/Unpack.kt
// port-lint: source values/types/list/unpack.rs
package io.github.kotlinmania.starlark.values.types.list
=======
// port-lint: source src/values/types/list/unpack.rs
package io.github.kotlinmania.starlark_kotlin.values.types.list
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/values/types/list/Unpack.kt

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
 * Corresponds to Rust's `UnpackList<T>` struct with `#[derive(Debug, Clone, Eq, PartialEq, Hash, Ord, PartialOrd)]`.
 *
 * @param T The expected element type, which must implement [StarlarkTypeRepr].
 * @property items The unpacked list items.
 */
data class UnpackList<T>(
    /** Unpacked items. */
    val items: MutableList<T>,
) : Iterable<T> {

    /** Create an empty [UnpackList]. Corresponds to Rust's `impl Default`. */
    constructor() : this(mutableListOf())

    /**
     * Returns an iterator over the items.
     *
     * Corresponds to Rust's `impl IntoIterator for UnpackList<T>`.
     */
    override fun iterator(): Iterator<T> = items.iterator()

    /**
     * Returns a mutable iterator over the items.
     *
     * Corresponds to Rust's `impl IntoIterator for &mut UnpackList<T>`.
     */
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
<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/values/types/list/Unpack.kt
=======
 * Corresponds to Rust's `impl UnpackValue<'v> for UnpackList<T>`.
 *
 * The error type in Rust is `<T as UnpackValue>::Error`, i.e. the
 * element unpacker's error type is propagated. In Kotlin we use
 * `Result<UnpackList<T>?>` with standard `Throwable` errors.
 *
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/values/types/list/Unpack.kt
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
        // In Rust: let Some(list) = <&ListRef>::unpack_value_opt(value) else { return Ok(None) };
        val listRef = ListRef.fromValue(value) ?: return Result.success(null)
        // Pre-allocate with capacity matching the list length.
        // In Rust: Vec::with_capacity(list.len())
        val capacity = listRef.len()
        val items = ArrayList<T>(capacity)
        for (v in listRef.iter()) {
            // In Rust: let Some(v) = T::unpack_value_impl(v)? else { return Ok(None) };
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
 *
 * Corresponds to Rust's `impl StarlarkTypeRepr for UnpackList<T>` where
 * `type Canonical = <ListType<T> as StarlarkTypeRepr>::Canonical`.
 */
class UnpackListStarlarkTypeRepr<T : StarlarkTypeRepr>(
    private val elementRepr: T,
) : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty {
        return Ty.list(elementRepr.starlarkTypeRepr())
    }
}

/**
 * Extension for consuming an [UnpackList] into its underlying list.
 *
 * Corresponds to Rust's `impl IntoIterator for UnpackList<T>` where
 * `type Item = T` and `type IntoIter = vec::IntoIter<T>`.
 */
fun <T> UnpackList<T>.intoList(): MutableList<T> = items

/**
 * Extension for iterating an [UnpackList] by reference.
 *
 * Corresponds to Rust's `impl IntoIterator for &'a UnpackList<T>` where
 * `type Item = &'a T` and `type IntoIter = slice::Iter<'a, T>`.
 */
fun <T> UnpackList<T>.iterRef(): Iterator<T> = items.iterator()
