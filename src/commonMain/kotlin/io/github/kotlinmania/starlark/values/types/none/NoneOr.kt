// port-lint: source src/values/types/none/noneOr.rs
package io.github.kotlinmania.starlark.values.types.none

/*
 * Copyright 2019 The Starlark in Rust Authors.
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
import io.github.kotlinmania.starlark.values.AllocFrozenValue
import io.github.kotlinmania.starlark.values.AllocValue
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark.values.UnpackValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.Heap

/**
 * Equivalent of a Kotlin nullable type, where `null`
 * is encoded as [NoneType].
 * Useful for its [UnpackValue] instance.
 */
sealed class NoneOr<out T> : StarlarkTypeRepr {
    /** Starlark `None`. */
    data object None : NoneOr<kotlin.Nothing>() {
        override fun starlarkTypeRepr(): Ty = Ty.none()
    }

    /** Not `None`. */
    data class Other<T>(val value: T) : NoneOr<T>() {
        override fun starlarkTypeRepr(): Ty {
            return if (value is StarlarkTypeRepr) {
                Ty.union2(Ty.none(), value.starlarkTypeRepr())
            } else {
                Ty.none()
            }
        }
    }

    /** Convert the [NoneOr] to a nullable type. */
    fun intoOption(): T? {
        return when (this) {
            is None -> null
            is Other -> this.value
        }
    }

    /** Is the value a [NoneOr.None]. */
    fun isNone(): Boolean {
        return this is None
    }

    companion object {
        /** Convert a nullable type to a [NoneOr]. */
        fun <T> fromOption(option: T?): NoneOr<T> {
            return when (option) {
                null -> None
                else -> Other(option)
            }
        }
    }
}

/** [UnpackValue] implementation for [NoneOr]. */
class NoneOrUnpackValue<T>(
    private val inner: UnpackValue<T>,
) : UnpackValue<NoneOr<T>> {
    override fun starlarkTypeRepr(): Ty {
        return Ty.union2(Ty.none(), inner.starlarkTypeRepr())
    }

    override fun unpackValueImpl(value: Value): Result<NoneOr<T>?> {
        if (value.isNone()) {
            return Result.success(NoneOr.None)
        }
        return inner.unpackValueImpl(value).map { it?.let { NoneOr.Other(it) } }
    }
}

/** [AllocValue] implementation for [NoneOr] where [T] implements [AllocValue]. */
fun <T : AllocValue> NoneOr<T>.allocValue(heap: Heap): Value {
    return when (this) {
        is NoneOr.None -> Value.newNone()
        is NoneOr.Other -> value.allocValue(heap)
    }
}

/** [AllocFrozenValue] implementation for [NoneOr] where [T] implements [AllocFrozenValue]. */
fun <T : AllocFrozenValue> NoneOr<T>.allocFrozenValue(heap: FrozenHeap): FrozenValue {
    return when (this) {
        is NoneOr.None -> FrozenValue.newNone()
        is NoneOr.Other -> value.allocFrozenValue(heap)
    }
}
