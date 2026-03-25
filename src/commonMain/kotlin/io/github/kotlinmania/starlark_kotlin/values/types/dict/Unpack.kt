// port-lint: source src/values/types/dict/unpack.rs
package io.github.kotlinmania.starlark_kotlin.values.types.dict

import io.github.kotlinmania.starlark_kotlin.values.unpackValueOpt
import io.github.kotlinmania.starlark_kotlin.tests.derive.starlarkTypeRepr


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
 * Unpack `dict`.
 *
 * There's `impl` [UnpackValue] for [SmallMap](starlark_map::small_map::SmallMap)
 * but this can be used when hashing of unpacked keys is not needed.
 */
data class UnpackDictEntries<K, V>(
    /** Entries of the dictionary. */
    val entries: MutableList<Pair<K, V>> = mutableListOf()
)

// StarlarkTypeRepr implementation for UnpackDictEntries<K, V>
internal fun <K, V> starlarkTypeReprForUnpackDictEntries(): StarlarkTypeRepr<UnpackDictEntries<K, V>> {
    return object : StarlarkTypeRepr<UnpackDictEntries<K, V>> {
        override fun starlarkTypeRepr(): Ty {
            return DictType.starlarkTypeRepr<K, V>()
        }
    }
}

// UnpackValue implementation for UnpackDictEntries<K, V>
internal fun <K, V> unpackValueImplForUnpackDictEntries(
    value: Value<*>,
    kUnpack: (Value<*>) -> Result<K?>,
    vUnpack: (Value<*>) -> Result<V?>
): Result<UnpackDictEntries<K, V>?> {
    val dict = DictRef.unpackValueOpt(value) ?: return Result.success(null)

    val entries = mutableListOf<Pair<K, V>>()

    for ((k, v) in dict.iter()) {
        val unpackedK = kUnpack(k).mapCatching { it }.getOrElse { error ->
            return Result.failure(Either.Left<Any, Any>(error))
        } ?: return Result.success(null)

        val unpackedV = vUnpack(v).mapCatching { it }.getOrElse { error ->
            return Result.failure(Either.Right<Any, Any>(error))
        } ?: return Result.success(null)

        entries.add(Pair(unpackedK, unpackedV))
    }

    return Result.success(UnpackDictEntries(entries))
}

// Placeholder types for dependencies that will be ported later

internal interface StarlarkTypeRepr<T> {
    fun starlarkTypeRepr(): Ty
}

internal class Ty private constructor() {
    companion object {
        fun dict(keyTy: Ty, valueTy: Ty): Ty = Ty()
    }
}

internal class Value<V> private constructor()

internal class DictRef<V> private constructor() {
    fun iter(): List<Pair<Value<*>, Value<*>>> = emptyList()

    companion object {
        fun <V> unpackValueOpt(value: Value<V>): DictRef<V>? = null
    }
}

internal object DictType {
    fun <K, V> starlarkTypeRepr(): Ty = Ty()
}

internal sealed class Either<out L, out R> {
    data class Left<out L>(val value: L) : Either<L, Nothing>()
    data class Right<out R>(val value: R) : Either<Nothing, R>()
}
