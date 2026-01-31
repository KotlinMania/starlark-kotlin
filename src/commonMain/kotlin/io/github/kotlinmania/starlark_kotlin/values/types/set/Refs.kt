// port-lint: source src/values/types/set/refs.rs
package io.github.kotlinmania.starlark_kotlin.values.types.set

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
import io.github.kotlinmania.starlark_kotlin.values.typeRepr.SetType
import io.github.kotlinmania.starlark_kotlin.values.typeRepr.StarlarkTypeRepr

/**
 * Define the set type.
 */
class SetRef<'v> internal constructor(
    internal val aref: Either<BorrowedSetData<'v>, SetData<'v>>
)

/**
 * Clone implementation for SetRef.
 * Corresponds to Rust's Clone impl which uses Ref::clone for Left case.
 */
fun <'v> SetRef<'v>.clone(): SetRef<'v> {
    return when (val ref = this.aref) {
        is Either.Left -> SetRef(Either.Left(ref.value.clone()))
        is Either.Right -> SetRef(Either.Right(ref.value))
    }
}

/**
 * Access the underlying content (SmallSet).
 * Extension property that mimics Rust's Deref to access `aref.content`.
 */
val <'v> SetRef<'v>.content: SmallSet<Value<'v>>
    get() = when (val ref = aref) {
        is Either.Left -> ref.value.data.content
        is Either.Right -> ref.value.content
    }

/**
 * Iterate through the values in the set, retaining their hashes.
 * Corresponds to accessing methods through Deref in Rust.
 */
fun <'v> SetRef<'v>.iterHashed(): Sequence<Hashed<Value<'v>>> = when (val ref = aref) {
    is Either.Left -> ref.value.data.iterHashed()
    is Either.Right -> ref.value.iterHashed()
}

/**
 * Check if the set contains a hashed element.
 * Corresponds to accessing methods through Deref in Rust.
 */
fun <'v> SetRef<'v>.containsHashed(key: Hashed<Value<'v>>): Boolean = when (val ref = aref) {
    is Either.Left -> ref.value.data.containsHashed(key)
    is Either.Right -> ref.value.containsHashed(key)
}

/**
 * Mutably borrowed `Set`.
 */
class SetMut<'v> internal constructor(
    internal val aref: BorrowedMutSetData<'v>
) {
    companion object {
        /**
         * Error class for non-set values.
         */
        private class NotSetError(typeName: String) :
            Exception("Value is not set, value type: `$typeName`")

        /**
         * Cold/inline(never) error path.
         */
        private fun <'v> error(x: Value<'v>): Throwable {
            return if (x.downcastRef<SetGen<FrozenSetData>>() != null) {
                ValueError.CannotMutateImmutableValue
            } else {
                NotSetError(x.getType())
            }
        }

        /**
         * Downcast the value to a mutable set reference.
         */
        inline fun <'v> fromValue(x: Value<'v>): Result<SetMut<'v>> {
            val ptr = x.downcastRef<SetGen<RefCell<SetData<'v>>>>()
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
        return SetType.starlarkTypeRepr<FrozenValue>()
    }
}

/**
 * UnpackValue implementation for SetRef.
 */
object SetRefUnpackValue : UnpackValue<Nothing> {
    override fun <'v> unpackValueImpl(value: Value<'v>): Result<SetRef<'v>?> {
        val result = if (value.unpackFrozen() != null) {
            value.unpackFrozen()!!
                .downcastRef<SetGen<FrozenSetData>>()
                ?.let { SetRef(Either.Right(coerce(it.inner))) }
        } else {
            value.downcastRef<SetGen<RefCell<SetData<'v>>>>()
                ?.let { ptr -> SetRef(Either.Left(ptr.inner.borrow())) }
        }
        return Result.success(result)
    }
}

/**
 * Either type for representing one of two possible values.
 * Corresponds to Rust's `either::Either`.
 */
sealed class Either<out L, out R> {
    data class Left<out L>(val value: L) : Either<L, Nothing>()
    data class Right<out R>(val value: R) : Either<Nothing, R>()
}

/**
 * RefCell type for interior mutability.
 * Corresponds to Rust's `RefCell<T>`.
 */
class RefCell<T>(private var value: T) {
    private var borrowCount = 0
    private var mutBorrowCount = 0

    fun borrow(): BorrowedSetData<*> {
        if (mutBorrowCount > 0) {
            throw IllegalStateException("Already mutably borrowed")
        }
        borrowCount++
        @Suppress("UNCHECKED_CAST")
        return BorrowedSetData(value as SetData<*>)
    }

    fun tryBorrowMut(): BorrowedMutSetData<*>? {
        if (borrowCount > 0 || mutBorrowCount > 0) {
            return null
        }
        mutBorrowCount++
        @Suppress("UNCHECKED_CAST")
        return BorrowedMutSetData(value as SetData<*>)
    }
}

/**
 * Borrowed reference to SetData (immutable).
 * Corresponds to Rust's `Ref<'v, SetData<'v>>`.
 */
class BorrowedSetData<'v>(val data: SetData<'v>) {
    fun clone(): BorrowedSetData<'v> = BorrowedSetData(data)
}

/**
 * Mutably borrowed reference to SetData.
 * Corresponds to Rust's `RefMut<'v, SetData<'v>>`.
 */
class BorrowedMutSetData<'v>(val data: SetData<'v>)
