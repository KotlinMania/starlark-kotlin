// port-lint: source src/values/types/dict/refs.rs
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

import io.github.kotlinmania.starlarkmap.smallmap.SmallMap
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.UnpackValue
import io.github.kotlinmania.starlark.values.ValueError
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr

sealed class Either<out L, out R> {
    data class Left<out L>(val value: L) : Either<L, Nothing>()
    data class Right<out R>(val value: R) : Either<Nothing, R>()
}

/** Borrowed `Dict`. */
class DictRef internal constructor(
    internal val aref: Either<Ref<Dict>, Dict>
)

fun DictRef.clone(): DictRef = when (val ref = this.aref) {
    is Either.Left -> DictRef(Either.Left(ref.value.clone()))
    is Either.Right -> DictRef(Either.Right(ref.value))
}

/** Downcast the value to a dict. */
fun dictRefFromValue(x: Value): DictRef? =
    if (x.unpackFrozen() != null) {
        x.downcastRef<DictGen<FrozenDictData>>()
            ?.let { DictRef(Either.Right(coerce(it.inner))) }
    } else {
        val ptr = x.downcastRef<DictGen<AtomicRef<Dict>>>() ?: return null
        DictRef(Either.Left(ptr.inner.borrow()))
    }

operator fun DictRef.getValue(thisRef: Any?, property: Any?): Dict = when (val ref = aref) {
    is Either.Left -> ref.value.value
    is Either.Right -> ref.value
}

/** Iterate over key/value pairs of the borrowed dict. */
fun DictRef.iter(): Sequence<Pair<Value, Value>> = when (val ref = aref) {
    is Either.Left -> ref.value.value.iter()
    is Either.Right -> ref.value.iter()
}

/** Mutably borrowed `Dict`. */
class DictMut(
    /** Mutable reference to the dict */
    val aref: RefMut<Dict>
)

/** Downcast the value to a mutable dict reference. */
fun dictMutFromValue(x: Value): Result<DictMut> {
    class NotDictError(typeName: String) : Exception("Value is not dict, value type: `$typeName`")

    fun error(x: Value): Throwable =
        if (x.downcastRef<DictGen<FrozenDictData>>() != null) ValueError.CannotMutateImmutableValue
        else NotDictError(x.getType())

    val ptr = x.downcastRef<DictGen<AtomicRef<Dict>>>() ?: return Result.failure(error(x))
    return when (val borrowed = ptr.inner.tryBorrowMut()) {
        null -> Result.failure(ValueError.MutationDuringIteration)
        else -> Result.success(DictMut(borrowed))
    }
}

/** Reference to frozen `Dict`. */
class FrozenDictRef internal constructor(
    private val dict: FrozenDictData
) {
    companion object {
        /** Downcast to frozen dict. */
        fun fromFrozenValue(x: FrozenValue): FrozenDictRef? =
            x.downcastRef<DictGen<FrozenDictData>>()?.let { FrozenDictRef(it.inner) }
    }

    /** Get value by a string key. */
    fun getStr(key: String): FrozenValue? = dict.getStr(key)

    /** Iterate over dict entries. */
    fun iter(): Sequence<Pair<FrozenValue, FrozenValue>> = dict.iter()
}

object DictRefStarlarkTypeRepr : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty =
        Ty.dict(Ty.any(), Ty.any())
}

object DictRefUnpackValue : UnpackValue<DictRef> {
    override fun starlarkTypeRepr(): Ty = DictRefStarlarkTypeRepr.starlarkTypeRepr()

    override fun unpackValueImpl(value: Value): Result<DictRef?> =
        Result.success(dictRefFromValue(value))
}

class Ref<T>(val value: T) {
    fun clone(): Ref<T> = Ref(value)
}

class RefMut<T>(val value: T)

private fun coerce(data: FrozenDictData): Dict =
    Dict(data.content as SmallMap<Value, Value>)
