// port-lint: source src/values/types/none/none_type.rs
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
import io.github.kotlinmania.starlark_kotlin.collections.StarlarkHashValue
import io.github.kotlinmania.starlark_kotlin.collections.StarlarkHasher
import io.github.kotlinmania.starlark_kotlin.values.AllocFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.AllocValue
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.UnpackValue
import io.github.kotlinmania.starlark_kotlin.values.types.string.Serializer
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.AllocStaticSimple
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.value_of.unpackValueImpl
import io.github.kotlinmania.starlark_kotlin.values.getTypeStarlarkRepr
import io.github.kotlinmania.starlark_kotlin.values.unpack_and_discard.unpackValueImpl

/** Define the None type, use [NoneType] in Rust. */
object NoneType : StarlarkValue, AllocValue, AllocFrozenValue, UnpackValue<NoneType> {
    /** The result of `type(None)`. */
    override val TYPE: String = "NoneType"

    override fun toString(): String = "None"

    override fun isSpecial(): Boolean = true

    override fun toBool(): Boolean = false

    override fun writeHash(hasher: StarlarkHasher): Result<Unit> {
        // just took the result of hash(None) in macos python 2.7.10 interpreter.
        hasher.writeU64(9_223_380_832_852_120_682UL)
        return Result.success(Unit)
    }

    override fun getHash(): Result<StarlarkHashValue> {
        // Just a random number.
        return Result.success(StarlarkHashValue.newUnchecked(0xf9c2263dU))
    }

    override fun starlarkTypeRepr(): Ty {
        return Ty.none()
    }

    override fun getTypeStarlarkRepr(): Ty {
        return Ty.none()
    }

    override fun typecheckerTy(): Ty? {
        return Ty.none()
    }

    override fun evalType(): Ty? {
        return Ty.none()
    }

    override fun allocValue(heap: Heap): Value {
        return Value.newNone()
    }

    fun serialize(serializer: Serializer): Result<Unit> {
        return serializer.serializeNone()
    }

    override fun allocFrozenValue(heap: FrozenHeap): FrozenValue {
        return FrozenValue.newNone()
    }

    override fun unpackValueImpl(value: Value): Result<NoneType?> {
        return if (value.isNone()) {
            Result.success(NoneType)
        } else {
            Result.success(null)
        }
    }
}

internal val VALUE_NONE: AllocStaticSimple<NoneType> = AllocStaticSimple.alloc(NoneType)

fun getTypeStarlarkRepr(): Ty {
    return NoneType.getTypeStarlarkRepr()
}

fun unpackValueImpl(value: Value): Result<NoneType?> {
    return NoneType.unpackValueImpl(value)
}
