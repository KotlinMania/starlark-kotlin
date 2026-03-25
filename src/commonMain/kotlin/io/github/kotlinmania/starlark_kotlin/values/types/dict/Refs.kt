// port-lint: source src/values/types/dict/refs.rs
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
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.UnpackValue
import io.github.kotlinmania.starlark_kotlin.values.ValueError
import io.github.kotlinmania.starlark_kotlin.values.layout.value
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.getType
import io.github.kotlinmania.starlark_kotlin.tests.derive.starlarkTypeRepr
import io.github.kotlinmania.starlark_kotlin.values.types.tuple.unpackFrozen
import io.github.kotlinmania.starlark_kotlin.inner
import io.github.kotlinmania.starlark_kotlin.coerce
import io.github.kotlinmania.starlark_kotlin.any.downcastRef

sealed class Either<out L, out R> {
    data class Left<out L>(val value: L) : Either<L, Nothing>()
    data class Right<out R>(val value: R) : Either<Nothing, R>()
}

/** Borrowed `Dict`. */
class DictRef<V_> internal constructor(
    internal val aref: Either<Ref<Dict<V_>>, Dict<V_>>
)

fun <V_> DictRef<V_>.clone(): DictRef<V_> {
    return when (val ref = this.aref) {
        is Either.Left -> DictRef(Either.Left(ref.value.clone()))
        is Either.Right -> DictRef(Either.Right(ref.value))
    }
}

/** Downcast the value to a dict. */
fun <V_> dictRefFromValue(x: Value<V_>): DictRef<V_>? {
    return if (x.unpackFrozen() != null) {
        x.downcastRef<DictGen<FrozenDictData>>()
            ?.let { DictRef(Either.Right(coerce(it.inner))) }
    } else {
        val ptr = x.downcastRef<DictGen<AtomicRef<Dict<V_>>>>() ?: return null
        DictRef(Either.Left(ptr.inner.borrow()))
    }
}

operator fun <V_> DictRef<V_>.getValue(thisRef: Any?, property: Any?): Dict<V_> {
    return when (val ref = aref) {
        is Either.Left -> ref.value.value
        is Either.Right -> ref.value
    }
}

/** Mutably borrowed `Dict`. */
class DictMut<V_>(
    val aref: RefMut<Dict<V_>>
)

/** Downcast the value to a mutable dict reference. */
inline fun <V_> dictMutFromValue(x: Value<V_>): Result<DictMut<V_>> {
    class NotDictError(val typeName: String) : Exception("Value is not dict, value type: `$typeName`")

    fun <V_> error(x: Value<V_>): Throwable {
        return if (x.downcastRef<DictGen<FrozenDictData>>() != null) {
            ValueError.CannotMutateImmutableValue()
        } else {
            NotDictError(x.getType())
        }
    }

    val ptr = x.downcastRef<DictGen<AtomicRef<Dict<V_>>>>()
    return when (ptr) {
        null -> Result.failure(error(x))
        else -> when (val borrowed = ptr.inner.tryBorrowMut()) {
            null -> Result.failure(ValueError.MutationDuringIteration())
            else -> Result.success(DictMut(borrowed))
        }
    }
}

/** Reference to frozen `Dict`. */
class FrozenDictRef internal constructor(
    private val dict: FrozenDictData
)

/** Downcast to frozen dict. */
fun frozenDictRefFromFrozenValue(x: FrozenValue): FrozenDictRef? {
    return x.downcastRef<DictGen<FrozenDictData>>()
        ?.let { FrozenDictRef(it.inner) }
}

/** Get value by a string key. */
fun FrozenDictRef.getStr(key: String): FrozenValue? {
    return this.dict.getStr(key)
}

/** Iterate over dict entries. */
fun FrozenDictRef.iter(): Sequence<Pair<FrozenValue, FrozenValue>> {
    return this.dict.iter()
}

object DictRefStarlarkTypeRepr : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty {
        return DictType.starlarkTypeRepr<FrozenValue, FrozenValue>()
    }
}

object DictRefUnpackValue : UnpackValue<Nothing> {
    override fun <V_> unpackValueImpl(value: Value<V_>): Result<DictRef<V_>?> {
        return Result.success(dictRefFromValue(value))
    }
}

class Ref<T>(val value: T) {
    fun clone(): Ref<T> = Ref(value)
}

class RefMut<T>(val value: T)

fun <V_> AtomicRef<Dict<V_>>.borrow(): Ref<Dict<V_>> {
    return Ref(this.value)
}

fun <V_> AtomicRef<Dict<V_>>.tryBorrowMut(): RefMut<Dict<V_>>? {
    return RefMut(this.value)
}
