<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/values/types/set/Refs.kt
// port-lint: source values/types/set/refs.rs
package io.github.kotlinmania.starlark.values.types.set
=======
// port-lint: source src/values/types/set/refs.rs
package io.github.kotlinmania.starlark_kotlin.values.types.set
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/values/types/set/Refs.kt

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

<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/values/types/set/Refs.kt
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
=======
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.UnpackValue
import io.github.kotlinmania.starlark_kotlin.values.ValueError
import io.github.kotlinmania.starlark_kotlin.collections.Hashed
import io.github.kotlinmania.starlark_kotlin.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark_kotlin.values.SetType
import io.github.kotlinmania.starlark_kotlin.collections.small_set.SmallSet
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValueStarlarkTypeRepr
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/values/types/set/Refs.kt

/**
 * Define the set type.
 */
class SetRef internal constructor(
    internal val aref: Either<Ref<SetData>, SetData>
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
fun SetRef.clone(): SetRef {
    return when (val ref = this.aref) {
        is Either.Left -> SetRef(Either.Left(ref.value.ptrRead()))
        is Either.Right -> SetRef(Either.Right(ref.value))
    }
}

/**
 * Access the underlying content (SmallSet).
 * Extension property that mimics Rust's Deref to access `aref.content`.
 */
val SetRef.content: SmallSet<Value>
    get() = when (val ref = aref) {
        is Either.Left -> ref.value.value.content
        is Either.Right -> ref.value.content
    }

/**
 * Iterate through the values in the set, retaining their hashes.
 * Corresponds to accessing methods through Deref in Rust.
 */
fun SetRef.iterHashed(): Sequence<Hashed<Value>> = when (val ref = aref) {
    is Either.Left -> ref.value.value.iterHashed()
    is Either.Right -> ref.value.iterHashed()
}

/**
 * Check if the set contains a hashed element.
 * Corresponds to accessing methods through Deref in Rust.
 */
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
        internal inline fun fromValue(x: Value): Result<SetMut> {
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
 * Corresponds to Rust's `coerce(&x.0)` which zero-cost converts FrozenSetData to SetData
 * because FrozenValue can be treated as Value.
 */
@Suppress("UNCHECKED_CAST")
private fun coerceSetData(data: FrozenSetData): SetData =
    SetData(data.content as SmallSet<Value>)

<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/values/types/set/Refs.kt

=======
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

    fun borrow(): BorrowedSetData {
        if (mutBorrowCount > 0) {
            throw IllegalStateException("Already mutably borrowed")
        }
        borrowCount++
        @Suppress("UNCHECKED_CAST")
        return BorrowedSetData(value as SetData)
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
        @Suppress("UNCHECKED_CAST")
        return BorrowedMutSetData(value as SetData)
    }
}

/**
 * Borrowed reference to SetData (immutable).
 * Corresponds to Rust's `Ref<SetData>`.
 */
class BorrowedSetData(val data: SetData) {
    fun clone(): BorrowedSetData = BorrowedSetData(data)
}

/**
 * Mutably borrowed reference to SetData.
 * Corresponds to Rust's `RefMut<SetData>`.
 */
class BorrowedMutSetData(val data: SetData)
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/values/types/set/Refs.kt
