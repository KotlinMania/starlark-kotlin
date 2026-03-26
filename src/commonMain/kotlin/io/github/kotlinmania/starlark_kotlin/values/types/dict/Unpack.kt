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

import io.github.kotlinmania.starlark_kotlin.values.UnpackValue
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.type_repr.StarlarkTypeRepr

/**
 * Unpack `dict`.
 *
 * This can be used when hashing of unpacked keys is not needed.
 */
class UnpackDictEntries<K, V>(
    /** Entries of the dictionary. */
    val entries: MutableList<Pair<K, V>> = mutableListOf(),
) {
    companion object {
        fun <K : Any, V : Any> unpackValue(value: Value): UnpackDictEntries<K, V>? {
            val dict = DictRef.unpackValueOpt(value) ?: return null
            val entries = mutableListOf<Pair<K, V>>()
            for ((k, v) in dict) {
                @Suppress("UNCHECKED_CAST")
                val key = UnpackValue.unpackValueImpl<K>(k) ?: return null
                @Suppress("UNCHECKED_CAST")
                val value = UnpackValue.unpackValueImpl<V>(v) ?: return null
                entries.add(Pair(key, value))
            }
            return UnpackDictEntries(entries)
        }
    }
}
