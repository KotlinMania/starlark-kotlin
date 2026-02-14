// port-lint: source src/values/types/list_or_tuple.rs
package io.github.kotlinmania.starlark_kotlin.values.types.list_or_tuple

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

/// Utility for unpacking a value of type `list[T]` or `tuple[T, ...]` into a list.

/// Unpack a value of type `list[T]` or `tuple[T, ...]` into a list.
// #[derive(Debug, Clone, Eq, PartialEq, Hash, Ord, PartialOrd)]
// pub struct UnpackListOrTuple<T> { pub items: Vec<T> }
class UnpackListOrTuple<T>(
    /// Unpacked items of the list or tuple.
    val items: MutableList<T>,
) : Iterable<T> {

    // impl Default for UnpackListOrTuple<T>
    constructor() : this(mutableListOf())

    companion object {
        // impl StarlarkTypeRepr for UnpackListOrTuple<T>
        // fn starlark_type_repr() -> Ty
        // Kotlin: type representation delegates to Either<UnpackList<T>, UnpackTuple<T>>
        // Actual type repr will be resolved when UnpackList/UnpackTuple are ported.

        // impl UnpackValue for UnpackListOrTuple<T>
        // fn unpack_value_impl(value: Value<'v>) -> Result<Option<Self>, Self::Error>
        fun <T> unpackValueImpl(
            value: Any,
            unpackList: (Any) -> List<T>?,
            unpackTuple: (Any) -> List<T>?,
        ): UnpackListOrTuple<T>? {
            val listResult = unpackList(value)
            if (listResult != null) {
                return UnpackListOrTuple(listResult.toMutableList())
            }
            val tupleResult = unpackTuple(value)
            if (tupleResult != null) {
                return UnpackListOrTuple(tupleResult.toMutableList())
            }
            return null
        }
    }

    // impl IntoIterator for UnpackListOrTuple<T>
    // fn into_iter(self) -> Self::IntoIter
    override fun iterator(): Iterator<T> = items.iterator()

    // impl PartialEq for UnpackListOrTuple<T>
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UnpackListOrTuple<*>) return false
        return items == other.items
    }

    // impl Hash for UnpackListOrTuple<T>
    override fun hashCode(): Int = items.hashCode()

    // impl Debug for UnpackListOrTuple<T>
    override fun toString(): String = "UnpackListOrTuple(items=$items)"
}

// #[cfg(test)] mod tests
// Tests are in commonTest, not here.
