// port-lint: source src/values/types/tuple/value.rs
package io.github.kotlinmania.starlark.values.types.tuple

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

import io.github.kotlinmania.starlark.collections.StarlarkHasher
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.ValueError
import io.github.kotlinmania.starlark.values.applySlice
import io.github.kotlinmania.starlark.values.compareSlice
import io.github.kotlinmania.starlark.values.convertIndex
import io.github.kotlinmania.starlark.values.equalsSlice
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.ValueLike
import io.github.kotlinmania.starlark.values.layout.avalues.AllocStaticSimple
import io.github.kotlinmania.starlark.values.layout.avalues.allocTuple
import io.github.kotlinmania.starlark.values.layout.heap.Heap

/** Define the tuple type. See [Tuple] and [FrozenTuple] as the two aliases. */
internal class TupleGen<V>(
    /** The data stored by the tuple. */
    private var content: List<V>,
) : io.github.kotlinmania.starlark.values.StarlarkValue {
    override val TYPE: String get() = Companion.TYPE
    override val HAS_iterate: Boolean get() = true
    override val HAS_equals: Boolean get() = true

    companion object {
        /** `type(())`. */
        const val TYPE: String = "tuple"
    }

    /** Get the length of the tuple. */
    fun len(): Int = content.size

    /** Tuple elements. */
    fun content(): List<V> = content

    /** Mutable access to tuple elements (used during construction). */
    internal fun contentMut(): MutableList<V> {
        @Suppress("UNCHECKED_CAST")
        return content as MutableList<V>
    }

    /** Iterate over the elements of the tuple. */
    fun iter(): Iterator<Value> = content.map { (it as ValueLike).toValue() }.iterator()

    override fun toString(): String {
        // For single-item tuples we need to add a trailing ','
        return if (content.size == 1) {
            "(${content[0]},)"
        } else {
            content.joinToString(", ", "(", ")")
        }
    }

    // --- StarlarkValue overrides ---
    // These must be interface overrides, not extension functions, so that vtable
    // dispatch through AValueDyn.starlarkValue() reaches them.

    override fun isSpecial(): Boolean = true

    override fun toBool(): Boolean = len() != 0

    override fun writeHash(hasher: StarlarkHasher): Result<Unit> {
        for (v in content()) {
            (v as ValueLike).writeHash(hasher).getOrElse { return Result.failure(it) }
        }
        return Result.success(Unit)
    }

    override fun equals(other: Value): Result<Boolean> {
        val otherTuple = Tuple.fromValue(other) ?: return Result.success(false)
        return equalsSlice<Exception, V, Value>(content(), otherTuple.content()) { x, y -> (x as ValueLike).equals(y) }
    }

    override fun compare(other: Value): Result<Int> {
        val otherTuple =
            TupleGen.fromValue(other)
                ?: return ValueError.unsupportedWith(TupleGen.TYPE, "cmp()", other)
        return compareSlice<Exception, V, Value>(content(), otherTuple.content()) { x, y -> (x as ValueLike).compare(y) }
    }

    override fun at(index: Value, heap: Heap): Result<Value> {
        val i = convertIndex(index, len()).getOrElse { return Result.failure(it) }
        return Result.success((content()[i] as ValueLike).toValue())
    }

    override fun length(): Result<Int> = Result.success(len())

    override fun isIn(other: Value): Result<Boolean> {
        for (x in content()) {
            if ((x as ValueLike).equals(other).getOrThrow()) {
                return Result.success(true)
            }
        }
        return Result.success(false)
    }

    override fun slice(start: Value?, stop: Value?, stride: Value?, heap: Heap): Result<Value> {
        val sliced =
            applySlice(content(), start, stop, stride).getOrElse {
                return Result.failure(it)
            }
        return Result.success(heap.allocTuple(sliced.map { (it as ValueLike).toValue() }))
    }

    override fun iterate(me: Value, heap: Heap): Result<Value> = Result.success(me)

    override fun iterSizeHint(index: Int): Pair<Int, Int?> {
        val rem = len() - index
        return Pair(rem, rem)
    }

    override fun iterNext(index: Int, heap: Heap): Value? = content().getOrNull(index)?.let { (it as ValueLike).toValue() }

    override fun iterStop() {}

    override fun add(rhs: Value, heap: Heap): Result<Value>? {
        val otherTuple = TupleGen.fromValue(rhs) ?: return null
        val result = mutableListOf<Value>()
        for (x in content()) {
            result.add((x as ValueLike).toValue())
        }
        for (x in otherTuple.content()) {
            result.add(x)
        }
        return Result.success(heap.allocTuple(result))
    }

    override fun mul(rhs: Value, heap: Heap): Result<Value>? {
        val l = rhs.unpackI32() ?: return null
        val result = mutableListOf<Value>()
        for (i in 0 until l) {
            result.addAll(content().map { (it as ValueLike).toValue() })
        }
        return Result.success(heap.allocTuple(result))
    }

    override fun rmul(lhs: Value, heap: Heap): Result<Value>? = mul(lhs, heap)

    override fun collectReprCycle(collector: StringBuilder) {
        collector.append("(...)")
    }

    override fun getTypeStarlarkRepr(): Ty = Ty.anyTuple()
}

class Tuple internal constructor(
    internal val delegate: TupleGen<Value>,
) : io.github.kotlinmania.starlark.values.StarlarkValue by delegate {
    fun len(): Int = delegate.len()

    fun content(): List<Value> = delegate.content()

    internal fun contentMut(): MutableList<Value> = delegate.contentMut()

    fun iter(): Iterator<Value> = delegate.iter()

    override fun toString(): String = delegate.toString()

    companion object {
        fun fromValue(value: Value): Tuple? {
            val raw = tupleGenFromValue(value) ?: return null
            val mapped = raw.content().map { (it as ValueLike).toValue() }
            return Tuple(TupleGen(mapped))
        }
    }
}

class FrozenTuple internal constructor(
    internal val delegate: TupleGen<FrozenValue>,
) : io.github.kotlinmania.starlark.values.StarlarkValue by delegate {
    fun len(): Int = delegate.len()

    fun content(): List<FrozenValue> = delegate.content()

    fun iter(): Iterator<Value> = delegate.iter()

    override fun toString(): String = delegate.toString()

    companion object {
        fun fromValue(value: Value): FrozenTuple? {
            val raw = tupleGenFromValue(value) ?: return null
            @Suppress("UNCHECKED_CAST")
            return FrozenTuple(raw as TupleGen<FrozenValue>)
        }
    }
}

internal fun tupleGenFromValue(value: Value): TupleGen<*>? =
    value.downcastRef<Tuple>()?.delegate
        ?: value.downcastRef<FrozenTuple>()?.delegate

/** The empty tuple, statically allocated. */
internal val VALUE_EMPTY_TUPLE: AllocStaticSimple<FrozenTuple> =
    AllocStaticSimple.alloc(FrozenTuple(TupleGen(emptyList())))

/** Downcast a value to a tuple. */
internal fun TupleGen.Companion.fromValue(value: Value): Tuple? = Tuple.fromValue(value)

// Serialize support for TupleGen
internal fun <V> TupleGen<V>.serialize(): List<V> = content()
