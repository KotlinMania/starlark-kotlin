// port-lint: source src/values/types/set/value.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)
package io.github.kotlinmania.starlark.values.types.set

import kotlin.native.HiddenFromObjC

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

import io.github.kotlinmania.starlark.collections.Hashed
import io.github.kotlinmania.starlark.collections.smallset.SmallSet
import io.github.kotlinmania.starlark.environment.Methods
import io.github.kotlinmania.starlark.environment.MethodsStatic
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.ComplexValue
import io.github.kotlinmania.starlark.values.Freeze
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.Trace
import io.github.kotlinmania.starlark.values.ValueError
import io.github.kotlinmania.starlark.values.freezeSmallSet
import io.github.kotlinmania.starlark.values.layout.Freezer
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.allocComplex
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.layout.heap.ValueHolder

/**
 * Generic set wrapper.
 *
 * Transparent wrapper around the inner set implementation.
 * Corresponds to Rust's `SetGen<T>` with `#[repr(transparent)]`.
 */
internal data class SetGen<T : SetLike>(
    val inner: T,
) : ComplexValue,
    Trace,
    Freeze<StarlarkValue> {
    override fun freeze(freezer: Freezer): Result<StarlarkValue> {
        val mutableInner =
            inner as? RefCell
                ?: return Result.failure(ValueError.CannotMutateImmutableValue)
        return SetGen(mutableInner).freezeToFrozenSet(freezer).map { it as StarlarkValue }
    }

    override val TYPE: String get() = SET_TYPE
    override val HAS_iterate: Boolean get() = true
    override val HAS_equals: Boolean get() = true

    override fun trace(tracer: Tracer) {
        val innerVal = inner
        if (innerVal is Trace) {
            innerVal.trace(tracer)
        }
    }

    override fun length(): Result<Int> =
        Result.success(inner.content().len())

    override fun isIn(other: Value): Result<Boolean> =
        try {
            val hashed = other.getHashed().getOrThrow()
            Result.success(inner.content().containsHashed(hashed.asRef()))
        } catch (e: Throwable) {
            Result.failure(e)
        }

    override fun equals(other: Value): Result<Boolean> {
        val otherSet =
            SetRef.unpackValueOpt(other)
                ?: return Result.success(false)
        return Result.success(equalsSmallSet(inner.content(), otherSet.content))
    }

    override fun getMethods(): Methods? = setMethods()

    override fun iterate(me: Value, heap: Heap): Result<Value> {
        inner.iterStart()
        return Result.success(me)
    }

    override fun iterSizeHint(index: Int): Pair<Int, Int?> {
        check(index <= inner.content().len())
        val rem = inner.content().len() - index
        return Pair(rem, rem)
    }

    override fun iterNext(index: Int, heap: Heap): Value? =
        inner
            .contentUnchecked()
            .iter()
            .drop(index)
            .firstOrNull()

    override fun iterStop() {
        inner.iterStop()
    }

    override fun toBool(): Boolean =
        !inner.content().isEmpty()

    override fun bitOr(other: Value, heap: Heap): Result<Value> {
        return try {
            // Unlike in `union` it is not possible to `|` `set` and iterable. This is due python semantics.
            val rhsSet =
                SetRef.unpackValueOpt(other)
                    ?: return ValueError.unsupportedWith(SET_TYPE, "|", other)

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

    override fun bitAnd(other: Value, heap: Heap): Result<Value> {
        return try {
            val rhsSet =
                SetRef.unpackValueOpt(other)
                    ?: return ValueError.unsupportedWith(SET_TYPE, "&", other)

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

    override fun bitXor(other: Value, heap: Heap): Result<Value> {
        return try {
            val rhsSet =
                SetRef.unpackValueOpt(other)
                    ?: return ValueError.unsupportedWith(SET_TYPE, "^", other)

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

    override fun sub(other: Value, heap: Heap): Result<Value> {
        return try {
            val rhsSet =
                SetRef.unpackValueOpt(other)
                    ?: return ValueError.unsupportedWith(SET_TYPE, "-", other)

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

    override fun typecheckerTy(): Ty = Ty.anySet()

    override fun getTypeStarlarkRepr(): Ty = Ty.anySet()

    override fun toString(): String = fmtContainer("set([", "])", inner.content().iter())
}

private const val SET_TYPE: String = "set"

/**
 * Define the mutable set type.
 *
 * Corresponds to Rust's `SetData`.
 */
@HiddenFromObjC
class SetData internal constructor(
    /** The data stored by the set. */
    internal val content: SmallSet<Value>,
) : Trace {
    override fun trace(tracer: Tracer) {
        for (i in content.entries.indices) {
            val entry = content.entries[i]
            val key = entry.key()
            val holder = ValueHolder(key)
            tracer.trace(holder)
            content.entries[i] = Hashed.newUnchecked(entry.hash(), holder.value)
        }
    }
    constructor() : this(SmallSet())

    fun clear() {
        content.clear()
    }

    /**
     * Iterate through the values in the set.
     */
    fun iter(): Sequence<Value> = content.iter()

    /**
     * Iterate through the values in the set, but retaining the hash of the values.
     */
    fun iterHashed(): Sequence<Hashed<Value>> = content.iterHashed().map { it.copied() }

    /**
     * Check if the set contains a hashed element.
     */
    fun containsHashed(key: Hashed<Value>): Boolean = content.containsHashed(key.asRef())

    fun addHashed(value: Hashed<Value>): Boolean = content.insertHashed(value)

    fun addHashedUniqueUnchecked(value: Hashed<Value>) {
        content.insertHashedUniqueUnchecked(value)
    }

    fun removeHashed(value: Hashed<Value>): Boolean = content.shiftRemoveHashed(value)
}

/**
 * Define the frozen set type.
 *
 * Corresponds to Rust's `FrozenSetData`.
 */
@HiddenFromObjC
class FrozenSetData(
    /** The data stored by the set. The values must all be hashable values. */
    internal val content: SmallSet<FrozenValue> = SmallSet(),
) : SetLike {
    override fun content(): SmallSet<Value> = valueContent()

    override fun iterStart() {
    }

    override fun iterStop() {
    }

    override fun contentUnchecked(): SmallSet<Value> = valueContent()
}

internal fun FrozenSetData.valueContent(): SmallSet<Value> {
    val values = SmallSet.withCapacity<Value>(content.len())
    for (entry in content.iterHashed()) {
        values.insertHashedUniqueUnchecked(
            Hashed.newUnchecked(entry.hash(), entry.key().toValue()),
        )
    }
    return values
}

@HiddenFromObjC
class MutableSet internal constructor(
    internal val delegate: SetGen<RefCell>,
) : StarlarkValue by delegate,
    ComplexValue,
    Trace,
    Freeze<FrozenSet> {
    internal val inner: RefCell get() = delegate.inner

    override fun toString(): String = delegate.toString()

    override fun trace(tracer: Tracer) {
        delegate.trace(tracer)
    }

    override fun freeze(freezer: Freezer): Result<FrozenSet> = delegate.freezeToFrozenSet(freezer)
}

@HiddenFromObjC
class FrozenSet internal constructor(
    internal val delegate: SetGen<FrozenSetData>,
) : StarlarkValue by delegate,
    ComplexValue,
    Trace {
    val inner: FrozenSetData get() = delegate.inner

    override fun toString(): String = delegate.toString()

    override fun trace(tracer: Tracer) {
        delegate.trace(tracer)
    }
}

internal fun setGenFromValue(value: Value): SetGen<*>? =
    value.downcastRef<MutableSet>()?.delegate
        ?: value.downcastRef<FrozenSet>()?.delegate

/**
 * AllocValue implementation for SetData.
 */
internal fun SetData.allocValue(heap: Heap): Value = heap.allocComplex(MutableSet(SetGen(RefCell(this))))

/**
 * StarlarkTypeRepr implementation for SetData.
 */
internal fun SetData.starlarkTypeRepr(): Ty = Ty.anySet()

/**
 * Freeze implementation for MutableSet.
 */
internal fun SetGen<RefCell>.freezeToFrozenSet(freezer: Freezer): Result<FrozenSet> {
    val contentResult =
        freezeSmallSet(
            this.inner
                .borrow()
                .data.content,
            freezer,
        ) { v, f -> v.freeze(f) }
    if (contentResult.isFailure) return Result.failure(contentResult.exceptionOrNull()!!)
    return Result.success(FrozenSet(SetGen(FrozenSetData(contentResult.getOrThrow()))))
}

/**
 * Get set methods.
 */
internal fun setMethods(): Methods = RES.methods(::setMethodsImpl)

private val RES = MethodsStatic()

/** Delegate to the set methods registration in Methods.kt. */
private fun setMethodsImpl(builder: io.github.kotlinmania.starlark.environment.MethodsBuilder) {
    setMethods(builder)
}

/**
 * Trait for set-like operations.
 *
 * Corresponds to Rust's `SetLike` trait.
 */
@HiddenFromObjC
interface SetLike {
    fun content(): SmallSet<Value>

    // These functions are unsafe for the same reason
    // StarlarkValue iterator functions are unsafe.
    fun iterStart()

    fun contentUnchecked(): SmallSet<Value>

    fun iterStop()
}

internal fun SetGen<out SetLike>.serialize(): List<Value> = inner.content().iter().toList()

// Register vtable for FrozenSet (special type not handled by #[starlark_value] macro, because V is not ValueLike).
// Note: registerAvalueSimpleFrozen!(FrozenSet) - to be implemented in registration system

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
