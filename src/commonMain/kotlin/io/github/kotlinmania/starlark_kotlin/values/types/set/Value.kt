// port-lint: source src/values/types/set/value.rs
package io.github.kotlinmania.starlark_kotlin.values.types.set

import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.types.string.Serializer
import starlark_map.Hashed
import io.github.kotlinmania.starlark_kotlin.values.types.enumeration.value.MethodsStatic
import io.github.kotlinmania.starlark_kotlin.values.types.enumeration.value.Methods
import io.github.kotlinmania.starlark_kotlin.values.ValueError
import io.github.kotlinmania.starlark_kotlin.values.SetType
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.Freezer
import io.github.kotlinmania.starlark_kotlin.collections.SmallSet
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.types.dict.getHashed
import io.github.kotlinmania.starlark_kotlin.tests.derive.starlarkTypeRepr
import io.github.kotlinmania.starlark_kotlin.values.typing.anySet
import io.github.kotlinmania.starlark_kotlin.values.types.dict.insertHashed
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.intoInner
import io.github.kotlinmania.starlark_kotlin.values.equalsSmallSet
import io.github.kotlinmania.starlark_kotlin.util.unleakBorrow
import io.github.kotlinmania.starlark_kotlin.util.refcell.borrow
import io.github.kotlinmania.starlark_kotlin.values.freeze_error.FreezeResult

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

// Note: This is a skeletal port. Many dependencies (SmallSet, Hashed, Cell, etc.) are not yet fully ported.
// This file captures the structure and will be completed once dependencies are available.

/**
 * Generic set wrapper.
 *
 * Transparent wrapper around the inner set implementation.
 * Corresponds to Rust's `SetGen<T>` with `#[repr(transparent)]`.
 */
@JvmInline
value class SetGen<T>(val inner: T)

/**
 * Define the mutable set type.
 *
 * Corresponds to Rust's `SetData`.
 */
class SetData {
    /** The data stored by the set. */
    val content: SmallSet<Value> = SmallSet()

    fun clear() {
        content.clear()
    }

    /**
     * Iterate through the values in the set.
     */
    fun iter(): Sequence<Value> {
        return content.iter()
    }

    /**
     * Iterate through the values in the set, but retaining the hash of the values.
     */
    fun iterHashed(): Sequence<Hashed<Value>> {
        return content.iterHashed().map { it.copied() }
    }

    /**
     * Check if the set contains a hashed element.
     */
    fun containsHashed(key: Hashed<Value>): Boolean {
        return content.containsHashed(key.asRef())
    }

    fun addHashed(value: Hashed<Value>): Boolean {
        return content.insertHashed(value)
    }

    fun addHashedUniqueUnchecked(value: Hashed<Value>) {
        content.insertHashedUniqueUnchecked(value)
    }

    fun removeHashed(value: Hashed<Value>): Boolean {
        return content.shiftRemoveHashed(value)
    }
}

/**
 * Define the frozen set type.
 *
 * Corresponds to Rust's `FrozenSetData`.
 */
class FrozenSetData {
    /** The data stored by the set. The values must all be hashable values. */
    val content: SmallSet<FrozenValue> = SmallSet()
}

/** Mutable set type alias. */
typealias MutableSet = SetGen<Cell<SetData>>

/** Frozen set type alias. */
typealias FrozenSet = SetGen<FrozenSetData>

/**
 * AllocValue implementation for SetData.
 */
fun SetData.allocValue(heap: Heap): Value {
    return heap.allocComplex(SetGen(Cell.new(this)))
}

/**
 * StarlarkTypeRepr implementation for SetData.
 */
fun SetData.starlarkTypeRepr(): Ty {
    return SetType.starlarkTypeRepr<Value>()
}

/**
 * Freeze implementation for MutableSet.
 */
fun MutableSet.freeze(freezer: Freezer): FreezeResult<FrozenSet> {
    val content = this.inner.intoInner().content.freeze(freezer)
    return when (content) {
        is Result.Success -> FreezeResult.Ok(SetGen(FrozenSetData().apply {
            this.content.addAll(content.value)
        }))
        is Result.Failure -> FreezeResult.Err(content.exception)
        else -> throw IllegalStateException("Unexpected result: $content")
    }
}

/**
 * Get set methods.
 */
fun setMethods(): Methods? {
    return RES.methods { setMethodsImpl() }
}

private val RES = MethodsStatic.new()

/**
 * Trait for set-like operations.
 *
 * Corresponds to Rust's `SetLike` trait.
 */
interface SetLike {
    fun content(): SmallSet<Value>

    // These functions are unsafe for the same reason
    // StarlarkValue iterator functions are unsafe.
    fun iterStart()
    fun contentUnchecked(): SmallSet<Value>
    fun iterStop()
}

/**
 * SetLike implementation for Cell<SetData>.
 */
class CellSetDataSetLike(private val cell: Cell<SetData>) : SetLike {
    override fun content(): SmallSet<Value> {
        return cell.borrow().content
    }

    override fun iterStart() {
        cell.forgetBorrow()
    }

    override fun iterStop() {
        cell.unleakBorrow()
    }

    override fun contentUnchecked(): SmallSet<Value> {
        return cell.tryBorrowUnguarded()!!.content
    }
}

/**
 * SetLike implementation for FrozenSetData.
 */
class FrozenSetDataSetLike(private val data: FrozenSetData) : SetLike {
    override fun content(): SmallSet<Value> {
        return coerce(data.content)
    }

    override fun iterStart() {
        // No-op for frozen data
    }

    override fun iterStop() {
        // No-op for frozen data
    }

    override fun contentUnchecked(): SmallSet<Value> {
        return coerce(data.content)
    }
}

// Register vtable for FrozenSet (special type not handled by #[starlark_value] macro, because V is not ValueLike).
// Note: registerAvalueSimpleFrozen!(FrozenSet) - to be implemented in registration system

/**
 * StarlarkValue implementation for SetGen<T> where T: SetLike.
 *
 * Corresponds to the #[starlark_value(type = "set")] macro expansion.
 */
// Note: The actual implementation would use a macro or code generation system.
// For now, we define extension functions that would be part of the trait impl.

/**
 * Returns the length of the set.
 */
fun <T> SetGen<T>.length(): Result<Int> where T : SetLike {
    val setLike = inner as SetLike
    return Result.success(setLike.content().len())
}

/**
 * Check if a value is in the set.
 */
fun <T> SetGen<T>.isIn(other: Value): Result<Boolean> where T : SetLike {
    val setLike = inner as SetLike
    return try {
        val hashed = other.getHashed()
        Result.success(setLike.content().containsHashed(hashed.asRef()))
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

/**
 * Check equality with another value.
 */
fun <T> SetGen<T>.equals(other: Value): Result<Boolean> where T : SetLike {
    val setLike = inner as SetLike
    return try {
        val otherSet = SetRef.unpackValueOpt(other)
        if (otherSet == null) {
            Result.success(false)
        } else {
            Result.success(equalsSmallSet(setLike.content(), otherSet.aref.content))
        }
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

/**
 * Get methods for set type.
 */
fun <T> SetGen<T>.getMethods(): Methods? where T : SetLike {
    return setMethods()
}

/**
 * Start iteration over the set.
 */
fun <T> SetGen<T>.iterate(me: Value, heap: Heap): Result<Value> where T : SetLike {
    val setLike = inner as SetLike
    setLike.iterStart()
    return Result.success(me)
}

/**
 * Get size hint for iteration.
 */
fun <T> SetGen<T>.iterSizeHint(index: Int): Pair<Int, Int?> where T : SetLike {
    val setLike = inner as SetLike
    check(index <= setLike.content().len())
    val rem = setLike.content().len() - index
    return Pair(rem, rem)
}

/**
 * Get next item in iteration.
 */
fun <T> SetGen<T>.iterNext(index: Int, heap: Heap): Value? where T : SetLike {
    val setLike = inner as SetLike
    return setLike.contentUnchecked().iter().drop(index).firstOrNull()
}

/**
 * Stop iteration.
 */
fun <T> SetGen<T>.iterStop() where T : SetLike {
    val setLike = inner as SetLike
    setLike.iterStop()
}

/**
 * Convert to boolean.
 */
fun <T> SetGen<T>.toBool(): Boolean where T : SetLike {
    val setLike = inner as SetLike
    return !setLike.content().isEmpty()
}

/**
 * Set union (bitwise OR operator).
 */
fun <T> SetGen<T>.bitOr(rhs: Value, heap: Heap): Result<Value> where T : SetLike {
    val setLike = inner as SetLike
    return try {
        // Unlike in `union` it is not possible to `|` `set` and iterable. This is due python semantics.
        val rhsSet = SetRef.unpackValueOpt(rhs)
            ?: return ValueError.unsupportedWith(this, "|", rhs)

        if (setLike.content().isEmpty()) {
            return Result.success(rhsSet.aref.clone().allocValue(heap))
        }

        val items = setLike.content().clone()
        for (h in rhsSet.aref.iterHashed()) {
            items.insertHashed(h)
        }
        Result.success(SetData().apply { content.addAll(items) }.allocValue(heap))
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

/**
 * Set intersection (bitwise AND operator).
 */
fun <T> SetGen<T>.bitAnd(rhs: Value, heap: Heap): Result<Value> where T : SetLike {
    val setLike = inner as SetLike
    return try {
        val rhsSet = SetRef.unpackValueOpt(rhs)
            ?: return ValueError.unsupportedWith(this, "&", rhs)

        val items = SmallSet<Value>()
        if (setLike.content().isEmpty()) {
            return Result.success(SetData().allocValue(heap))
        }

        for (h in rhsSet.aref.iterHashed()) {
            if (setLike.content().containsHashed(h.asRef())) {
                items.insertHashedUniqueUnchecked(h)
            }
        }

        Result.success(SetData().apply { content.addAll(items) }.allocValue(heap))
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

/**
 * Set symmetric difference (bitwise XOR operator).
 */
fun <T> SetGen<T>.bitXor(rhs: Value, heap: Heap): Result<Value> where T : SetLike {
    val setLike = inner as SetLike
    return try {
        val rhsSet = SetRef.unpackValueOpt(rhs)
            ?: return ValueError.unsupportedWith(this, "^", rhs)

        if (rhsSet.aref.content.isEmpty()) {
            return Result.success(SetData().apply {
                content.addAll(setLike.content().clone())
            }.allocValue(heap))
        }

        val data = SetData()
        for (elem in setLike.content().iterHashed()) {
            if (!rhsSet.aref.containsHashed(elem.copied())) {
                data.addHashedUniqueUnchecked(elem.copied())
            }
        }

        for (hashed in rhsSet.aref.iterHashed()) {
            if (!setLike.content().containsHashed(hashed.asRef())) {
                data.addHashed(hashed)
            }
        }
        Result.success(data.allocValue(heap))
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

/**
 * Set difference (subtraction operator).
 */
fun <T> SetGen<T>.sub(rhs: Value, heap: Heap): Result<Value> where T : SetLike {
    val setLike = inner as SetLike
    return try {
        val rhsSet = SetRef.unpackValueOpt(rhs)
            ?: return ValueError.unsupportedWith(this, "-", rhs)

        if (setLike.content().isEmpty()) {
            return Result.success(SetData().allocValue(heap))
        }

        if (rhsSet.aref.content.isEmpty()) {
            return Result.success(SetData().apply {
                content.addAll(setLike.content().clone())
            }.allocValue(heap))
        }

        val data = SetData()

        for (elem in setLike.content().iterHashed()) {
            if (!rhsSet.aref.containsHashed(elem.copied())) {
                data.addHashed(elem.copied())
            }
        }
        Result.success(data.allocValue(heap))
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

/**
 * Get typechecker type.
 */
fun <T> SetGen<T>.typecheckerTy(): Ty? where T : SetLike {
    return Ty.anySet()
}

/**
 * Get type representation for Starlark.
 */
fun getTypeStarlarkRepr(): Ty {
    return Ty.anySet()
}

/**
 * Serialize a SetGen to a serializer.
 */
fun <T> SetGen<T>.serialize(serializer: Serializer): Result<Unit, SerializerError> where T : SetLike {
    val setLike = inner as SetLike
    return try {
        serializer.collectSeq(setLike.content().iter())
        Result.success(Unit)
    } catch (e: SerializerError) {
        Result.failure(e)
    }
}

/**
 * Display a SetGen as a string.
 */
fun <T> SetGen<T>.display(): String where T : SetLike {
    val setLike = inner as SetLike
    return fmtContainer("set([", "])", setLike.content().iter())
}
