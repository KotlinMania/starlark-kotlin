// port-lint: source src/values/types/set/value.rs
package io.github.kotlinmania.starlark_kotlin.values.types.set

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

import io.github.kotlinmania.starlark_kotlin.collections.Hashed
import io.github.kotlinmania.starlark_kotlin.collections.SmallSet
import io.github.kotlinmania.starlark_kotlin.environment.Methods
import io.github.kotlinmania.starlark_kotlin.environment.MethodsStatic
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.ComplexValue
import io.github.kotlinmania.starlark_kotlin.values.Freezer
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.Trace
import io.github.kotlinmania.starlark_kotlin.values.Tracer
import io.github.kotlinmania.starlark_kotlin.values.ValueError
import io.github.kotlinmania.starlark_kotlin.values.freeze_error.FreezeResult
import io.github.kotlinmania.starlark_kotlin.values.freezeSmallSet
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.allocComplex
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap

/**
 * Generic set wrapper.
 *
 * Transparent wrapper around the inner set implementation.
 * Corresponds to Rust's `SetGen<T>` with `#[repr(transparent)]`.
 */
// #[derive(Clone, Default, Trace, Debug, ProvidesStaticType, Allocative)]
// pub(crate) struct SetGen<T>(pub(crate) T);
data class SetGen<T>(val inner: T) : ComplexValue, Trace {
    override val TYPE: String get() = SET_TYPE

    override fun trace(tracer: Tracer) {
        val innerVal = inner
        if (innerVal is Trace) {
            innerVal.trace(tracer)
        }
    }
}

private const val SET_TYPE: String = "set"

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
class FrozenSetData(
    /** The data stored by the set. The values must all be hashable values. */
    val content: SmallSet<FrozenValue> = SmallSet()
)

/** Mutable set type alias. */
typealias MutableSet = SetGen<RefCell<SetData>>

/** Frozen set type alias. */
typealias FrozenSet = SetGen<FrozenSetData>

/**
 * AllocValue implementation for SetData.
 */
fun SetData.allocValue(heap: Heap): Value {
    return heap.allocComplex(SetGen(RefCell(this)))
}

/**
 * StarlarkTypeRepr implementation for SetData.
 */
fun SetData.starlarkTypeRepr(): Ty {
    return Ty.anySet()
}

/**
 * Freeze implementation for MutableSet.
 */
fun MutableSet.freeze(freezer: Freezer): FreezeResult<FrozenSet> {
    val contentResult = freezeSmallSet(
        this.inner.borrow().data.content,
        freezer,
    ) { v, f -> v.freeze(f) }
    if (contentResult.isFailure) return Result.failure(contentResult.exceptionOrNull()!!)
    return Result.success(SetGen(FrozenSetData(contentResult.getOrThrow())))
}

/**
 * Get set methods.
 */
fun setMethods(): Methods? {
    return RES.methods(::setMethodsImpl)
}

private val RES = MethodsStatic()

/** Delegate to the set methods registration in Methods.kt. */
private fun setMethodsImpl(builder: io.github.kotlinmania.starlark_kotlin.environment.MethodsBuilder) {
    setMethods(builder)
}

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
 * SetLike implementation for RefCell<SetData>.
 */
class RefCellSetDataSetLike(private val cell: RefCell<SetData>) : SetLike {
    override fun content(): SmallSet<Value> {
        return cell.borrow().data.content
    }

    override fun iterStart() {
        // In Rust, mem::forget(self.borrow()) leaks a borrow to prevent mutation during iteration.
        // In Kotlin, the RefCell tracks borrow count; we increment it without releasing.
        cell.borrow()
    }

    override fun iterStop() {
        cell.releaseBorrow()
    }

    override fun contentUnchecked(): SmallSet<Value> {
        return cell.borrow().data.content
    }
}

/**
 * SetLike implementation for FrozenSetData.
 */
class FrozenSetDataSetLike(private val data: FrozenSetData) : SetLike {
    @Suppress("UNCHECKED_CAST")
    override fun content(): SmallSet<Value> {
        return data.content as SmallSet<Value>
    }

    override fun iterStart() {
        // No-op for frozen data
    }

    override fun iterStop() {
        // No-op for frozen data
    }

    @Suppress("UNCHECKED_CAST")
    override fun contentUnchecked(): SmallSet<Value> {
        return data.content as SmallSet<Value>
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
fun <T : SetLike> SetGen<T>.length(): Result<Int> {
    return Result.success(inner.content().len())
}

/**
 * Check if a value is in the set.
 */
fun <T : SetLike> SetGen<T>.isIn(other: Value): Result<Boolean> {
    return try {
        val hashed = other.getHashed().getOrThrow()
        Result.success(inner.content().containsHashed(hashed.asRef()))
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

/**
 * Check equality with another value.
 */
fun <T : SetLike> SetGen<T>.setEquals(other: Value): Result<Boolean> {
    return try {
        val otherSet = SetRef.unpackValueOpt(other)
        if (otherSet == null) {
            Result.success(false)
        } else {
            Result.success(equalsSmallSet(inner.content(), otherSet.content))
        }
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

/**
 * Get methods for set type.
 */
fun <T : SetLike> SetGen<T>.getMethods(): Methods? {
    return setMethods()
}

/**
 * Start iteration over the set.
 */
fun <T : SetLike> SetGen<T>.iterate(me: Value, heap: Heap): Result<Value> {
    inner.iterStart()
    return Result.success(me)
}

/**
 * Get size hint for iteration.
 */
fun <T : SetLike> SetGen<T>.iterSizeHint(index: Int): Pair<Int, Int?> {
    check(index <= inner.content().len())
    val rem = inner.content().len() - index
    return Pair(rem, rem)
}

/**
 * Get next item in iteration.
 */
fun <T : SetLike> SetGen<T>.iterNext(index: Int, heap: Heap): Value? {
    return inner.contentUnchecked().iter().drop(index).firstOrNull()
}

/**
 * Stop iteration.
 */
fun <T : SetLike> SetGen<T>.iterStop() {
    inner.iterStop()
}

/**
 * Convert to boolean.
 */
fun <T : SetLike> SetGen<T>.toBool(): Boolean {
    return !inner.content().isEmpty()
}

/**
 * Set union (bitwise OR operator).
 */
fun <T : SetLike> SetGen<T>.bitOr(rhs: Value, heap: Heap): Result<Value> {
    return try {
        // Unlike in `union` it is not possible to `|` `set` and iterable. This is due python semantics.
        val rhsSet = SetRef.unpackValueOpt(rhs)
            ?: return ValueError.unsupportedWith(SET_TYPE, "|", rhs)

        if (inner.content().isEmpty()) {
            return Result.success(copySetData(rhsSet.content).allocValue(heap))
        }

        val items = copySmallSet(inner.content())
        for (h in rhsSet.iterHashed()) {
            items.insertHashed(h)
        }
        Result.success(SetData().apply { content.addAll(items.iterHashed().asIterable()) }.allocValue(heap))
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

/**
 * Set intersection (bitwise AND operator).
 */
fun <T : SetLike> SetGen<T>.bitAnd(rhs: Value, heap: Heap): Result<Value> {
    return try {
        val rhsSet = SetRef.unpackValueOpt(rhs)
            ?: return ValueError.unsupportedWith(SET_TYPE, "&", rhs)

        if (inner.content().isEmpty()) {
            return Result.success(SetData().allocValue(heap))
        }

        val items = SmallSet<Value>()
        for (h in rhsSet.iterHashed()) {
            if (inner.content().containsHashed(h.asRef())) {
                items.insertHashedUniqueUnchecked(h)
            }
        }

        Result.success(SetData().apply { content.addAll(items.iterHashed().asIterable()) }.allocValue(heap))
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

/**
 * Set symmetric difference (bitwise XOR operator).
 */
fun <T : SetLike> SetGen<T>.bitXor(rhs: Value, heap: Heap): Result<Value> {
    return try {
        val rhsSet = SetRef.unpackValueOpt(rhs)
            ?: return ValueError.unsupportedWith(SET_TYPE, "^", rhs)

        if (rhsSet.content.isEmpty()) {
            return Result.success(copySetData(inner.content()).allocValue(heap))
        }

        val data = SetData()
        for (elem in inner.content().iterHashed()) {
            if (!rhsSet.containsHashed(elem.copied())) {
                data.addHashedUniqueUnchecked(elem.copied())
            }
        }

        for (hashed in rhsSet.iterHashed()) {
            if (!inner.content().containsHashed(hashed.asRef())) {
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
fun <T : SetLike> SetGen<T>.sub(rhs: Value, heap: Heap): Result<Value> {
    return try {
        val rhsSet = SetRef.unpackValueOpt(rhs)
            ?: return ValueError.unsupportedWith(SET_TYPE, "-", rhs)

        if (inner.content().isEmpty()) {
            return Result.success(SetData().allocValue(heap))
        }

        if (rhsSet.content.isEmpty()) {
            return Result.success(copySetData(inner.content()).allocValue(heap))
        }

        val data = SetData()

        for (elem in inner.content().iterHashed()) {
            if (!rhsSet.containsHashed(elem.copied())) {
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
fun <T : SetLike> SetGen<T>.typecheckerTy(): Ty? {
    return Ty.anySet()
}

/**
 * Get type representation for Starlark.
 */
fun getTypeStarlarkRepr(): Ty {
    return Ty.anySet()
}

/**
 * Display a SetGen as a string.
 */
fun <T : SetLike> SetGen<T>.display(): String {
    return fmtContainer("set([", "])", inner.content().iter())
}

/**
 * Format a container with start/end delimiters and comma-separated items.
 * Corresponds to Rust's `display_container::fmt_container`.
 */
private fun <T> fmtContainer(
    start: String,
    end: String,
    iter: Sequence<T>,
): String {
    val builder = StringBuilder()
    builder.append(start)
    var first = true
    for (item in iter) {
        if (!first) builder.append(", ")
        builder.append(item.toString())
        first = false
    }
    builder.append(end)
    return builder.toString()
}

/**
 * Compare two SmallSets for equality by checking containment in both directions.
 * Corresponds to Rust's `equals_small_set`.
 */
private fun <K> equalsSmallSet(xs: SmallSet<K>, ys: SmallSet<K>): Boolean {
    if (xs.len() != ys.len()) {
        return false
    }
    for (x in xs.iter()) {
        if (!ys.contains(x)) {
            return false
        }
    }
    return true
}

/**
 * Create a new SetData as a copy of an existing SmallSet<Value>.
 */
private fun copySetData(source: SmallSet<Value>): SetData {
    val data = SetData()
    data.content.addAll(source.iterHashed().asIterable())
    return data
}

/**
 * Create a shallow copy of a SmallSet by reinserting all hashed entries.
 */
private fun <T> copySmallSet(source: SmallSet<T>): SmallSet<T> {
    val copy = SmallSet<T>()
    copy.addAll(source.iterHashed().asIterable())
    return copy
}
