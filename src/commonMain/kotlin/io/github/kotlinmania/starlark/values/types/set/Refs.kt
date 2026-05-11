// port-lint: source values/types/set/refs.rs
package io.github.kotlinmania.starlark.values.types.set

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

import io.github.kotlinmania.starlark.Either
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.util.refcell.Ref
import io.github.kotlinmania.starlark.util.refcell.RefCell
import io.github.kotlinmania.starlark.util.refcell.RefMut
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.UnpackValue
import io.github.kotlinmania.starlark.values.ValueError
import io.github.kotlinmania.starlarkmap.Hashed
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark.values.SetType
import io.github.kotlinmania.starlarkmap.smallset.SmallSet
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.FrozenValueStarlarkTypeRepr

/**
 * Define the set type.
 */
class SetRef internal constructor(
    internal val aref: Either<Ref<SetData>, SetData>
) {
    companion object {
        /**
         * Unpack a [Value] into a [SetRef], or return null if not a set.
         */
        fun unpackValueOpt(value: Value): SetRef? =
            SetRefUnpackValue.unpackValueImpl(value).getOrThrow()
    }
}

/**
 * Clone implementation for SetRef.
 */
fun SetRef.clone(): SetRef {
    return when (val ref = this.aref) {
        is Either.Left -> SetRef(Either.Left(ref.value.ptrRead()))
        is Either.Right -> SetRef(Either.Right(ref.value))
    }
}

/** Access the underlying content of the borrowed set. */
val SetRef.content: SmallSet<Value>
    get() = when (val ref = aref) {
        is Either.Left -> ref.value.value.content
        is Either.Right -> ref.value.content
    }

/** Iterate through the values in the set, retaining their hashes. */
fun SetRef.iterHashed(): Sequence<Hashed<Value>> = when (val ref = aref) {
    is Either.Left -> ref.value.value.iterHashed()
    is Either.Right -> ref.value.iterHashed()
}

/** Check if the set contains a hashed element. */
fun SetRef.containsHashed(key: Hashed<Value>): Boolean = when (val ref = aref) {
    is Either.Left -> ref.value.value.containsHashed(key)
    is Either.Right -> ref.value.containsHashed(key)
}

/**
 * Mutably borrowed `Set`.
 */
class SetMut internal constructor(
    internal val aref: RefMut<SetData>
) {
    companion object {
        /**
         * Error class for non-set values.
         */
        internal class NotSetError(typeName: String) :
            Exception("Value is not set, value type: `$typeName`")

        /**
         * Cold/inline(never) error path.
         */
        internal fun error(x: Value): Throwable {
            return if (x.downcastRef<SetGen<FrozenSetData>>() != null) {
                ValueError.CannotMutateImmutableValue
            } else {
                NotSetError(x.getType())
            }
        }

        /**
         * Downcast the value to a mutable set reference.
         */
        internal fun fromValue(x: Value): Result<SetMut> {
            val ptr = x.downcastRef<SetGen<RefCell<SetData>>>()
            return when (ptr) {
                null -> Result.failure(error(x))
                else -> {
                    val borrowed = ptr.inner.tryBorrowMut()
                    if (borrowed != null) {
                        Result.success(SetMut(borrowed))
                    } else {
                        Result.failure(ValueError.MutationDuringIteration)
                    }
                }
            }
        }
    }
}

/**
 * StarlarkTypeRepr implementation for SetRef.
 */
object SetRefStarlarkTypeRepr : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty {
        return SetType(FrozenValueStarlarkTypeRepr).starlarkTypeRepr()
    }
}

/**
 * UnpackValue implementation for SetRef.
 */
object SetRefUnpackValue : UnpackValue<SetRef> {
    override fun starlarkTypeRepr(): Ty = SetRefStarlarkTypeRepr.starlarkTypeRepr()

    override fun unpackValueImpl(value: Value): Result<SetRef?> {
        val result = if (value.unpackFrozen() != null) {
            value.unpackFrozen()!!
                .downcastRef<SetGen<FrozenSetData>>()
                ?.let { SetRef(Either.Right(coerceSetData(it.inner))) }
        } else {
            value.downcastRef<SetGen<RefCell<SetData>>>()
                ?.let { ptr -> SetRef(Either.Left(ptr.inner.borrow())) }
        }
        return Result.success(result)
    }
}

/**
 * Coerce a [FrozenSetData] to a [SetData] view.
 * because FrozenValue can be treated as Value.
 */
private fun coerceSetData(data: FrozenSetData): SetData =
    SetData(data.content as SmallSet<Value>)


