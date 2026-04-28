// port-lint: source values/types/dict/dict_type.rs
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
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark.values.UnpackValue
import io.github.kotlinmania.starlark.values.layout.Value

/**
 * A dict type marker.
 *
 * [StarlarkTypeRepr] provides `dict[K, V]`.
 * [UnpackValue] implementation verifies the types of entries and discards them.
 */
class DictType<K, V>(
    private val keyUnpacker: UnpackValue<K>,
    private val valueUnpacker: UnpackValue<V>,
) : UnpackValue<DictType<K, V>> {

    /**
     *
     * `type Canonical = DictType<K::Canonical, V::Canonical>`.
     * Returns `Ty::dict(K::starlarkTypeRepr(), V::starlarkTypeRepr())`.
     */
    override fun starlarkTypeRepr(): Ty {
        return Ty.dict(keyUnpacker.starlarkTypeRepr(), valueUnpacker.starlarkTypeRepr())
    }

    /**
     *
     * `type Error = Either<K::Error, V::Error>`.
     *
     * Walks the dict with `UnpackDictEntries<UnpackAndDiscard<K>, UnpackAndDiscard<V>>` and
     * returns the marker value if every entry parses, propagating either the key or value
     * unpacker error otherwise.
     */
    override fun unpackValueImpl(value: Value): Result<DictType<K, V>?> {
        val entriesUnpacker = UnpackDictEntriesUnpackValue(keyUnpacker, valueUnpacker)
        return entriesUnpacker.unpackValueImpl(value).fold(
            onSuccess = { entries ->
                if (entries != null) Result.success(this) else Result.success(null)
            },
            onFailure = { Result.failure(it) },
        )
    }

    companion object {
        /**
         * Convenience over [starlarkTypeRepr] when callers only have the type representations
         * for the key and value, without an [UnpackValue] for either side.
         */
        fun starlarkTypeRepr(keyRepr: StarlarkTypeRepr, valueRepr: StarlarkTypeRepr): Ty {
            return Ty.dict(keyRepr.starlarkTypeRepr(), valueRepr.starlarkTypeRepr())
        }

        /** Reified-generic entry point for the `DictType<K, V>::starlarkTypeRepr()` form. */
        inline fun <reified K : StarlarkTypeRepr, reified V : StarlarkTypeRepr> starlarkTypeRepr(): Ty {
            return Ty.dict(Ty.any(), Ty.any())
        }
    }
}
