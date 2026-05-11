<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/values/UnpackAndDiscard.kt
// port-lint: source values/unpack_and_discard.rs
package io.github.kotlinmania.starlark.values.unpackanddiscard
=======
// port-lint: source src/values/unpack_and_discard.rs
package io.github.kotlinmania.starlark_kotlin.values.unpack_and_discard
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/values/UnpackAndDiscard.kt

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
 * Unpack the value of type [T], but do not store result.
 *
 * This can be used when type needs to be checked, but the unpacked value is not needed.
 */
class UnpackAndDiscard<T : Any> internal constructor(
    // Kotlin: stored Ty replaces Rust's static trait dispatch on PhantomData.
    private val ty: Ty,
) : StarlarkTypeRepr {

    // impl StarlarkTypeRepr for UnpackAndDiscard
    // fn starlark_type_repr() -> Ty
    override fun starlarkTypeRepr(): Ty {
        return ty
    }

    companion object {
        // impl UnpackValue for UnpackAndDiscard
        // fn unpack_value_impl(value: Value<'v>) -> Result<Option<Self>, Self::Error>
        //
        // In Rust, `T::unpack_value_impl(value)` calls the trait statically.
        // In Kotlin, we require the UnpackValue<T> instance to be passed in,
        // since Kotlin cannot dispatch interface methods via KClass.
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
