// port-lint: source values/unpackAndDiscard.rs
package io.github.kotlinmania.starlark.values.unpackanddiscard

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
 * Unpack the value of type [T], but do not store the result.
 *
 * This can be used when the type needs to be checked, but the unpacked value is not needed.
 */
class UnpackAndDiscard<T : Any> internal constructor(
    private val ty: Ty,
) : StarlarkTypeRepr {

    override fun starlarkTypeRepr(): Ty {
        return ty
    }

    companion object {
        internal fun <T : Any> unpackValueImpl(
            value: Value,
            unpacker: UnpackValue<T>,
        ): UnpackAndDiscard<T>? {
            val result = unpacker.unpackValueImpl(value).getOrThrow()
            return if (result != null) {
                UnpackAndDiscard(unpacker.starlarkTypeRepr())
            } else {
                null
            }
        }
    }
}
