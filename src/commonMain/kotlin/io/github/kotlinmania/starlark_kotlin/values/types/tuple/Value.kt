// port-lint: source src/values/types/tuple/value.rs
package io.github.kotlinmania.starlark_kotlin.values.types.tuple

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

import io.github.kotlinmania.starlark_kotlin.collections.StarlarkHasher
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.ValueError
import io.github.kotlinmania.starlark_kotlin.values.layout.ValueLike
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.AllocStaticSimple
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.allocTuple
import io.github.kotlinmania.starlark_kotlin.values.equalsSlice
import io.github.kotlinmania.starlark_kotlin.values.convertIndex
import io.github.kotlinmania.starlark_kotlin.values.compareSlice
import io.github.kotlinmania.starlark_kotlin.values.applySlice

/** Define the tuple type. See [Tuple] and [FrozenTuple] as the two aliases. */
class TupleGen<V>(
    /** The data stored by the tuple. */
    private val content: List<V>,
) : io.github.kotlinmania.starlark_kotlin.values.StarlarkValue {
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
    fun contentMut(): MutableList<V> {
        @Suppress("UNCHECKED_CAST")
        return content as MutableList<V>
    }

    /** Iterate over the elements of the tuple. */
    fun iter(): Iterator<Value> {
        return content.map { (it as ValueLike).toValue() }.iterator()
    }

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
        val otherTuple = TupleGen.fromValue(other) ?: return Result.success(false)
        return equalsSlice<Exception, V, Value>(content(), otherTuple.content()) { x, y -> (x as ValueLike).equals(y) }
    }

    override fun compare(other: Value): Result<Int> {
        val otherTuple = TupleGen.fromValue(other)
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
        val sliced = applySlice(content(), start, stop, stride).getOrElse {
            return Result.failure(it)
        }
        return Result.success(heap.allocTuple(sliced.map { (it as ValueLike).toValue() }))
    }

    override fun iterate(me: Value, heap: Heap): Result<Value> = Result.success(me)

    override fun iterSizeHint(index: Int): Pair<Int, Int?> {
        val rem = len() - index
        return Pair(rem, rem)
    }

    override fun iterNext(index: Int, heap: Heap): Value? {
        return content().getOrNull(index)?.let { (it as ValueLike).toValue() }
    }

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

/** Runtime type of unfrozen tuple. */
typealias Tuple = TupleGen<Value>

/** Runtime type of frozen tuple. */
typealias FrozenTuple = TupleGen<FrozenValue>

/** The empty tuple, statically allocated. */
val VALUE_EMPTY_TUPLE: AllocStaticSimple<FrozenTuple> =
    AllocStaticSimple.alloc(TupleGen(emptyList()))

/** Downcast a value to a tuple. */
fun TupleGen.Companion.fromValue(value: Value): Tuple? {
    return value.downcastRef<Tuple>()
}

// Serialize support for TupleGen
fun <V> TupleGen<V>.serialize(): List<V> = content()
