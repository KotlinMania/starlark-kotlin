// port-lint: source src/values/types/dict/unpack.rs
package io.github.kotlinmania.starlark_kotlin.values.types.dict

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
 * Unpack `dict`.
 *
 * There's an [UnpackValue] implementation for [SmallMap][io.github.kotlinmania.starlark_kotlin.collections.SmallMap]
 * but this can be used when hashing of unpacked keys is not needed.
 */
class UnpackDictEntries<K, V>(
    /** Entries of the dictionary. */
    val entries: MutableList<Pair<K, V>> = mutableListOf()
) {
    companion object {
        /** Default constructor matching Rust's `Default` trait. */
        fun <K, V> default(): UnpackDictEntries<K, V> {
            return UnpackDictEntries(mutableListOf())
        }

        /**
         * StarlarkTypeRepr implementation for UnpackDictEntries<K, V>.
         */
        inline fun <reified K : StarlarkTypeRepr, reified V : StarlarkTypeRepr> starlarkTypeRepr(): Ty {
            return DictType.starlarkTypeRepr<K, V>()
        }

        /**
         * UnpackValue implementation for UnpackDictEntries<K, V> where K: UnpackValue, V: UnpackValue.
         *
         * Returns null if the value is not a dict. Returns the [UnpackDictEntries] with
         * all key/value pairs unpacked, or null if any key or value fails type checking.
         */
        fun <K : Any, V : Any> unpackValue(value: Value<*>): Result<UnpackDictEntries<K, V>?>? {
            val dict = dictRefFromValue(value) ?: return Result.success(null)
            val entries = mutableListOf<Pair<K, V>>()
            for ((k, v) in dict.iter()) {
                @Suppress("UNCHECKED_CAST")
                val unpackedK = (k as? K) ?: return Result.success(null)
                @Suppress("UNCHECKED_CAST")
                val unpackedV = (v as? V) ?: return Result.success(null)
                entries.add(Pair(unpackedK, unpackedV))
            }
            return Result.success(UnpackDictEntries(entries))
        }
    }
}

private fun <V_> dictRefFromValue(value: Value<V_>): DictRef<V_>? {
    return io.github.kotlinmania.starlark_kotlin.values.types.dict.dictRefFromValue(value)
}

private fun <V_> DictRef<V_>.iter(): Sequence<Pair<Value<V_>, Value<V_>>> {
    val dict = when (val ref = aref) {
        is Either.Left -> ref.value.value
        is Either.Right -> ref.value
    }
    return dict.iter()
}
