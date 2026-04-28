// port-lint: source src/values/types/dict/unpack.rs
package io.github.kotlinmania.starlark.values.types.dict

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
import io.github.kotlinmania.starlark.values.UnpackValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr

/**
 * Unpack `dict`.
 *
 * There's an [UnpackValue] implementation for [SmallMap][starlarkmap.smallmap.SmallMap]
 * but this can be used when hashing of unpacked keys is not needed.
 */
class UnpackDictEntries<K, V>(
    /** Entries of the dictionary. */
    val entries: MutableList<Pair<K, V>> = mutableListOf()
) {
    companion object {
        /** Default empty entries. */
        fun <K, V> default(): UnpackDictEntries<K, V> {
            return UnpackDictEntries(mutableListOf())
        }

        /** Starlark type representation for `UnpackDictEntries<K, V>`. */
        inline fun <reified K : StarlarkTypeRepr, reified V : StarlarkTypeRepr> starlarkTypeRepr(): Ty {
            return DictType.starlarkTypeRepr<K, V>()
        }

        /**
         * Unpack a value into [UnpackDictEntries].
         *
         * Returns `null` if the value is not a dict, or if any key or value fails type checking.
         */
        fun <K : Any, V : Any> unpackValue(value: Value): Result<UnpackDictEntries<K, V>?>? {
            val dict = dictRefFromValue(value) ?: return Result.success(null)
            val entries = mutableListOf<Pair<K, V>>()
            for ((k, v) in dict.iter()) {
                val unpackedK = (k as? K) ?: return Result.success(null)
                val unpackedV = (v as? V) ?: return Result.success(null)
                entries.add(Pair(unpackedK, unpackedV))
            }
            return Result.success(UnpackDictEntries(entries))
        }
    }
}

/** [UnpackValue] implementation for [UnpackDictEntries]. */
class UnpackDictEntriesUnpackValue<K, V>(
    private val keyUnpacker: UnpackValue<K>,
    private val valueUnpacker: UnpackValue<V>,
) : UnpackValue<UnpackDictEntries<K, V>> {
    override fun unpackValueImpl(value: Value): Result<UnpackDictEntries<K, V>?> {
        val dict = dictRefFromValue(value) ?: return Result.success(null)
        val entries = mutableListOf<Pair<K, V>>()
        for ((k, v) in dict.iter()) {
            val unpackedK = keyUnpacker.unpackValueImpl(k).getOrElse { return Result.failure(it) } ?: return Result.success(null)
            val unpackedV = valueUnpacker.unpackValueImpl(v).getOrElse { return Result.failure(it) } ?: return Result.success(null)
            entries.add(Pair(unpackedK, unpackedV))
        }
        return Result.success(UnpackDictEntries(entries))
    }

    override fun starlarkTypeRepr(): Ty = Ty.dict(keyUnpacker.starlarkTypeRepr(), valueUnpacker.starlarkTypeRepr())
}
