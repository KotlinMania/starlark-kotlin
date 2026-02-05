// port-lint: source src/values/types/none/none_or.rs
package io.github.kotlinmania.starlark_kotlin.values.types.none

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

import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.AllocFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.AllocValue
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.Heap
import io.github.kotlinmania.starlark_kotlin.values.UnpackValue
import io.github.kotlinmania.starlark_kotlin.values.Value
import io.github.kotlinmania.starlark_kotlin.values.typeRepr.StarlarkTypeRepr

/**
 * Equivalent of a Rust [Option], where `None`
 * is encoded as [NoneType].
 * Useful for its [UnpackValue] instance.
 */
sealed class NoneOr<out T> {
    /**
     * Starlark `None`.
     */
    data object None : NoneOr<kotlin.Nothing>()

    /**
     * Not `None`.
     */
    data class Other<T>(val value: T) : NoneOr<T>()

    /**
     * Convert the [NoneOr] to a real Kotlin [Option] (nullable type).
     */
    inline fun intoOption(): T? {
        return when (this) {
            is None -> null
            is Other -> this.value
        }
    }

    /**
     * Is the value a [NoneOr.None].
     */
    fun isNone(): Boolean {
        return this is None
    }

    companion object {
        /**
         * Convert a Kotlin [Option] (nullable type) to a [NoneOr].
         */
        inline fun <T> fromOption(option: T?): NoneOr<T> {
            return when (option) {
                null -> None
                else -> Other(option)
            }
        }
    }
}

// impl<T: StarlarkTypeRepr> StarlarkTypeRepr for NoneOr<T>
expect class NoneOrStarlarkTypeRepr<T : StarlarkTypeRepr> : StarlarkTypeRepr

// impl<V_, T: UnpackValue<V_>> UnpackValue<V_> for NoneOr<T>
expect class NoneOrUnpackValue<V_, T> : UnpackValue<V_>

// impl<V_, T: AllocValue<V_>> AllocValue<V_> for NoneOr<T>
expect fun <V_, T> NoneOr<T>.allocValue(heap: Heap<V_>): Value<V_>

// impl<T: AllocFrozenValue> AllocFrozenValue for NoneOr<T>
expect fun <T> NoneOr<T>.allocFrozenValue(heap: FrozenHeap): FrozenValue
