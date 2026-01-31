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

import io.github.kotlinmania.starlark_kotlin.Private
import io.github.kotlinmania.starlark_kotlin.collections.StarlarkHashValue
import io.github.kotlinmania.starlark_kotlin.collections.StarlarkHasher
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.AllocStaticSimple
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.Heap
import io.github.kotlinmania.starlark_kotlin.values.Value

/** Define the None type, use NoneType in Rust. */
object NoneType {
    /** The result of type(None). */
    const val TYPE: String = "NoneType"

    override fun toString(): String {
        return "None"
    }
}

/** Define the NoneType type */
fun isSpecial(private: Private): Boolean
    where NoneType : Any {
    return true
}

fun NoneType.toBool(): Boolean {
    return false
}

fun NoneType.writeHash(hasher: StarlarkHasher): Result<Unit> {
    // just took the result of hash(None) in macos python 2.7.10 interpreter.
    hasher.writeU64(9_223_380_832_852_120_682UL)
    return Result.success(Unit)
}

fun NoneType.getHash(private: Private): Result<StarlarkHashValue> {
    // Just a random number.
    return Result.success(StarlarkHashValue.newUnchecked(0xf9c2263dU))
}

fun getTypeStarlarkRepr(): Ty {
    return Ty.none()
}

fun NoneType.typecheckerTy(): Ty? {
    return Ty.none()
}

fun NoneType.evalType(): Ty? {
    return Ty.none()
}

fun <V> NoneType.allocValue(heap: Heap<V>): Value<V> {
    return Value.newNone()
}

fun <S> NoneType.serialize(serializer: S): Result<Any>
    where S : kotlinx.serialization.SerializationStrategy<*> {
    return Result.success(Unit)
}

internal val VALUE_NONE: AllocStaticSimple<NoneType> = AllocStaticSimple.alloc(NoneType)

fun NoneType.allocFrozenValue(heap: FrozenHeap): FrozenValue {
    return FrozenValue.newNone()
}

fun <V> unpackValueImpl(value: Value<V>): Result<NoneType?> {
    return if (value.isNone()) {
        Result.success(NoneType)
    } else {
        Result.success(null)
    }
}
