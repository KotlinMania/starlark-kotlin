// port-lint: source src/values/types/dict/refs.rs
package io.github.kotlinmania.starlark.values.types.dict

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

import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark.values.UnpackValue
import io.github.kotlinmania.starlark.values.ValueError
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value

internal sealed class Either<out L, out R> {
    data class Left<out L>(
        val value: L,
    ) : Either<L, Nothing>()

    data class Right<out R>(
        val value: R,
    ) : Either<Nothing, R>()
}

internal class DictRef internal constructor(
    internal val aref: Either<Ref<Dict>, Dict>,
)

internal fun DictRef.clone(): DictRef =
    when (val ref = this.aref) {
        is Either.Left -> DictRef(Either.Left(ref.value.clone()))
        is Either.Right -> DictRef(Either.Right(ref.value))
    }

internal fun dictRefFromValue(x: Value): DictRef? {
    val gen = dictGenFromValue(x) ?: return null
    val inner = gen.inner
    return if (inner is FrozenDictData) {
        DictRef(Either.Right(Dict(inner.toValueMap())))
    } else if (inner is AtomicRef<*>) {
        @Suppress("UNCHECKED_CAST")
        DictRef(Either.Left((inner as AtomicRef<Dict>).borrow()))
    } else {
        null
    }
}

internal operator fun DictRef.getValue(thisRef: Any?, property: Any?): Dict =
    when (val ref = aref) {
        is Either.Left -> ref.value.value
        is Either.Right -> ref.value
    }

/** Iterate over key/value pairs, mirroring Rust's `Deref<Target = Dict>` on DictRef. */
internal fun DictRef.iter(): Sequence<Pair<Value, Value>> =
    when (val ref = aref) {
        is Either.Left -> ref.value.value.iter()
        is Either.Right -> ref.value.iter()
    }

internal class DictMut internal constructor(
    internal val aref: RefMut<Dict>,
)

internal fun dictMutFromValue(x: Value): Result<DictMut> {
    class NotDictError(
        typeName: String,
    ) : Exception("Value is not dict, value type: `$typeName`")

    val dictGen = dictGenFromValue(x)

    fun error(x: Value): Throwable =
        if (dictGen?.inner is FrozenDictData) {
            ValueError.CannotMutateImmutableValue
        } else {
            NotDictError(x.getType())
        }

    val dict = when (val inner = dictGen?.inner) {
        is AtomicRef<*> -> inner.value as? Dict
        else -> null
    }

    if (dict == null) return Result.failure(error(x))
    val borrowed = RefMut(dict)
    return Result.success(DictMut(borrowed))
}

internal class FrozenDictRef internal constructor(
    private val dict: FrozenDictData,
) {
    companion object {
        // / Downcast to frozen dict.
        internal fun fromFrozenValue(x: FrozenValue): FrozenDictRef? {
            val gen = dictGenFromValue(x.toValue()) ?: return null
            val inner = gen.inner as? FrozenDictData ?: return null
            return FrozenDictRef(inner)
        }
    }

    // / Get value by a string key.
    fun getStr(key: String): FrozenValue? = dict.getStr(key)

    // / Iterate over dict entries.
    fun iter(): Sequence<Pair<FrozenValue, FrozenValue>> = dict.iter()
}

internal object DictRefStarlarkTypeRepr : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty =
        Ty.dict(Ty.any(), Ty.any())
}

internal object DictRefUnpackValue : UnpackValue<DictRef> {
    override fun starlarkTypeRepr(): Ty = DictRefStarlarkTypeRepr.starlarkTypeRepr()

    override fun unpackValueImpl(value: Value): Result<DictRef?> =
        Result.success(dictRefFromValue(value))
}

internal class Ref<T>(
    val value: T,
) {
    fun clone(): Ref<T> = Ref(value)
}

internal class RefMut<T>(
    val value: T,
)
