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

import io.github.kotlinmania.starlark_kotlin.coerce.coerce
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.UnpackValue
import io.github.kotlinmania.starlark_kotlin.values.Value
import io.github.kotlinmania.starlark_kotlin.values.ValueError
import io.github.kotlinmania.starlark_kotlin.values.typeRepr.StarlarkTypeRepr
import kotlinx.atomicfu.AtomicRef

sealed class Either<out L, out R> {
    data class Left<out L>(val value: L) : Either<L, Nothing>()
    data class Right<out R>(val value: R) : Either<Nothing, R>()
}

/** Borrowed `Dict`. */
class DictRef<'v> internal constructor(
    internal val aref: Either<Ref<Dict<'v>>, Dict<'v>>
)

fun <'v> DictRef<'v>.clone(): DictRef<'v> {
    return when (val ref = this.aref) {
        is Either.Left -> DictRef(Either.Left(ref.value.clone()))
        is Either.Right -> DictRef(Either.Right(ref.value))
    }
}

/** Downcast the value to a dict. */
fun <'v> dictRefFromValue(x: Value<'v>): DictRef<'v>? {
    return if (x.unpackFrozen() != null) {
        x.downcastRef<DictGen<FrozenDictData>>()
            ?.let { DictRef(Either.Right(coerce(it.inner))) }
    } else {
        val ptr = x.downcastRef<DictGen<AtomicRef<Dict<'v>>>>() ?: return null
        DictRef(Either.Left(ptr.inner.borrow()))
    }
}

operator fun <'v> DictRef<'v>.getValue(thisRef: Any?, property: Any?): Dict<'v> {
    return when (val ref = aref) {
        is Either.Left -> ref.value.value
        is Either.Right -> ref.value
    }
}

/** Mutably borrowed `Dict`. */
class DictMut<'v>(
    val aref: RefMut<Dict<'v>>
)

/** Downcast the value to a mutable dict reference. */
inline fun <'v> dictMutFromValue(x: Value<'v>): Result<DictMut<'v>> {
    class NotDictError(val typeName: String) : Exception("Value is not dict, value type: `$typeName`")

    fun <'v> error(x: Value<'v>): Throwable {
        return if (x.downcastRef<DictGen<FrozenDictData>>() != null) {
            ValueError.CannotMutateImmutableValue()
        } else {
            NotDictError(x.getType())
        }
    }

    val ptr = x.downcastRef<DictGen<AtomicRef<Dict<'v>>>>()
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
    override fun <'v> unpackValueImpl(value: Value<'v>): Result<DictRef<'v>?> {
        return Result.success(dictRefFromValue(value))
    }
}

class Ref<T>(val value: T) {
    fun clone(): Ref<T> = Ref(value)
}

class RefMut<T>(val value: T)

fun <'v> AtomicRef<Dict<'v>>.borrow(): Ref<Dict<'v>> {
    return Ref(this.value)
}

fun <'v> AtomicRef<Dict<'v>>.tryBorrowMut(): RefMut<Dict<'v>>? {
    return RefMut(this.value)
}
