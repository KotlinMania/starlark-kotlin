// port-lint: source src/values/types/dict/dict_type.rs
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
import io.github.kotlinmania.starlark_kotlin.values.StarlarkTypeRepr

/**
 * A dict type marker.
 *
 * [StarlarkTypeRepr] provides `dict[K, V]`.
 * [UnpackValue] implementation verifies the types of entries and discards them.
 */
class DictType<K : StarlarkTypeRepr, V : StarlarkTypeRepr> private constructor() {

    companion object {
        /** Factory method to create a [DictType] instance. */
        fun <K : StarlarkTypeRepr, V : StarlarkTypeRepr> instance(): DictType<K, V> = DictType()

        /**
         * StarlarkTypeRepr implementation for DictType<K, V>.
         *
         * Returns the Starlark type representation: `Ty.dict(K.starlarkTypeRepr(), V.starlarkTypeRepr())`.
         */
        inline fun <reified K : StarlarkTypeRepr, reified V : StarlarkTypeRepr> starlarkTypeRepr(): Ty {
            return Ty.dict(K::class.starlarkTypeRepr(), V::class.starlarkTypeRepr())
        }
    }
}

/**
 * UnpackValue implementation for DictType<K, V> where K: UnpackValue, V: UnpackValue.
 *
 * Unpacks a value as a dict, verifying the types of entries and discarding them.
 * Returns a [DictType] marker if the value is a dict with matching key/value types,
 * or null if the value is not a dict.
 *
 * The error type is [Either]<K.Error, V.Error>.
 */
fun <V_, K : UnpackValue<V_>, V : UnpackValue<V_>> unpackDictType(
    value: Value<V_>
): Result<DictType<K, V>?> {
    return when (val result = UnpackDictEntries.unpackValue<K, V>(value)) {
        null -> Result.success(null)
        else -> result.map { entries ->
            if (entries != null) DictType.instance<K, V>() else null
        }
    }
}

private fun <T : StarlarkTypeRepr> kotlin.reflect.KClass<T>.starlarkTypeRepr(): Ty {
    return Ty.any()
}
