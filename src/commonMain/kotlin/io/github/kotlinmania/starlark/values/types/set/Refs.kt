// port-lint: source src/values/types/set/refs.rs
package io.github.kotlinmania.starlark.values.types.set

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

import io.github.kotlinmania.starlark.collections.Hashed
import io.github.kotlinmania.starlark.collections.smallset.SmallSet
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.SetType
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark.values.UnpackValue
import io.github.kotlinmania.starlark.values.ValueError
import io.github.kotlinmania.starlark.values.layout.FrozenValueStarlarkTypeRepr
import io.github.kotlinmania.starlark.values.layout.Value

/**
 * Define the set type.
 */
class SetRef internal constructor(
    internal val aref: Either<BorrowedSetData, SetData>,
) {
    companion object {
        /**
         * Unpack a [Value] into a [SetRef], or return null if not a set.
         * Matches the Rust `UnpackValue::unpack_value_opt` trait method.
         */
        fun unpackValueOpt(value: Value): SetRef? =
            SetRefUnpackValue.unpackValueImpl(value).getOrThrow()
    }
}

/**
 * Clone implementation for SetRef.
 * Corresponds to Rust's Clone impl which uses Ref::clone for Left case.
 */
fun SetRef.clone(): SetRef =
    when (val ref = this.aref) {
        is Either.Left -> SetRef(Either.Left(ref.value.clone()))
        is Either.Right -> SetRef(Either.Right(ref.value))
    }

/**
 * Access the underlying content (SmallSet).
 * Extension property that mimics Rust's Deref to access `aref.content`.
 */
val SetRef.content: SmallSet<Value>
    get() =
        when (val ref = aref) {
            is Either.Left -> ref.value.data.content
            is Either.Right -> ref.value.content
        }

/**
 * Iterate through the values in the set, retaining their hashes.
 * Corresponds to accessing methods through Deref in Rust.
 */
fun SetRef.iterHashed(): Sequence<Hashed<Value>> =
    when (val ref = aref) {
        is Either.Left -> ref.value.data.iterHashed()
        is Either.Right -> ref.value.iterHashed()
    }

/**
 * Check if the set contains a hashed element.
 * Corresponds to accessing methods through Deref in Rust.
 */
fun SetRef.containsHashed(key: Hashed<Value>): Boolean =
    when (val ref = aref) {
        is Either.Left -> ref.value.data.containsHashed(key)
        is Either.Right -> ref.value.containsHashed(key)
    }

/**
 * Mutably borrowed `Set`.
 */
class SetMut internal constructor(
    internal val aref: BorrowedMutSetData,
) {
    companion object {
        /**
         * Error class for non-set values.
         */
        internal class NotSetError(
            typeName: String,
        ) : Exception("Value is not set, value type: `$typeName`")

        /**
         * Cold/inline(never) error path.
         */
        internal fun error(x: Value): Throwable =
            if (x.downcastRef<SetGen<FrozenSetData>>() != null) {
                ValueError.CannotMutateImmutableValue
            } else {
                NotSetError(x.getType())
            }

        /**
         * Downcast the value to a mutable set reference.
         */
        internal fun fromValue(x: Value): Result<SetMut> =
            when (val ptr = x.downcastRef<SetGen<RefCell>>()) {
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

/**
 * StarlarkTypeRepr implementation for SetRef.
 */
object SetRefStarlarkTypeRepr : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty = SetType(FrozenValueStarlarkTypeRepr).starlarkTypeRepr()
}

/**
 * UnpackValue implementation for SetRef.
 */
object SetRefUnpackValue : UnpackValue<SetRef> {
    override fun starlarkTypeRepr(): Ty = SetRefStarlarkTypeRepr.starlarkTypeRepr()

    override fun unpackValueImpl(value: Value): Result<SetRef?> {
        val result =
            if (value.unpackFrozen() != null) {
                value
                    .unpackFrozen()!!
                    .downcastRef<SetGen<FrozenSetData>>()
                    ?.let { SetRef(Either.Right(coerceSetData(it.inner))) }
            } else {
                value
                    .downcastRef<SetGen<RefCell>>()
                    ?.let { ptr -> SetRef(Either.Left(ptr.inner.borrow())) }
            }
        return Result.success(result)
    }
}

private fun coerceSetData(data: FrozenSetData): SetData =
    SetData(data.valueContent())

/**
 * Either type for representing one of two possible values.
 * Corresponds to Rust's `either::Either`.
 */
sealed class Either<out L, out R> {
    data class Left<out L>(
        val value: L,
    ) : Either<L, Nothing>()

    data class Right<out R>(
        val value: R,
    ) : Either<Nothing, R>()
}

/**
 * RefCell type for interior mutability.
 * Corresponds to Rust's `RefCell<T>`.
 */
class RefCell(
    private var value: SetData,
) : SetLike {
    private var borrowCount = 0
    private var mutBorrowCount = 0

    fun borrow(): BorrowedSetData {
        if (mutBorrowCount > 0) {
            throw IllegalStateException("Already mutably borrowed")
        }
        borrowCount++
        return Borrowed(value)
    }

    /**
     * Release (unleak) a previously leaked borrow.
     * Corresponds to Rust's `unleak_borrow` which undoes `mem::forget(self.borrow())`.
     */
    fun releaseBorrow() {
        check(borrowCount > 0) { "No borrow to release" }
        borrowCount--
    }

    fun tryBorrowMut(): BorrowedMutSetData? {
        if (borrowCount > 0 || mutBorrowCount > 0) {
            return null
        }
        mutBorrowCount++
        return BorrowedMut(value)
    }

    override fun content(): SmallSet<Value> = borrow().data.content

    override fun iterStart() {
        borrow()
    }

    override fun contentUnchecked(): SmallSet<Value> = borrow().data.content

    override fun iterStop() {
        releaseBorrow()
    }
}

/**
 * Borrowed reference.
 */
class Borrowed<T>(
    val data: T,
) {
    fun clone(): Borrowed<T> = Borrowed(data)
}

/**
 * Mutably borrowed reference.
 */
class BorrowedMut<T>(
    val data: T,
)

typealias BorrowedSetData = Borrowed<SetData>

typealias BorrowedMutSetData = BorrowedMut<SetData>
