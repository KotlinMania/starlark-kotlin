// port-lint: source src/values/types/tuple/value.rs
package io.github.kotlinmania.starlark.values.types.tuple

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

import starlarkmap.StarlarkHasher
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.ValueError
import io.github.kotlinmania.starlark.values.layout.ValueLike
import io.github.kotlinmania.starlark.values.layout.avalues.AllocStaticSimple
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.toValue
import io.github.kotlinmania.starlark.values.layout.avalues.allocTuple
import io.github.kotlinmania.starlark.values.equalsSlice
import io.github.kotlinmania.starlark.values.convertIndex
import io.github.kotlinmania.starlark.values.compareSlice
import io.github.kotlinmania.starlark.values.applySlice

/** Define the tuple type. See [Tuple] and [FrozenTuple] as the two aliases. */
class TupleGen<V>(
    /** The data stored by the tuple. */
    private val content: List<V>,
) : io.github.kotlinmania.starlark.values.StarlarkValue {
    override val TYPE: String get() = Companion.TYPE

    companion object {
        /** `type(())`. */
        const val TYPE: String = "tuple"

        /** Downcast a value to a tuple. */
        fun fromValue(value: Value): TupleGen<Value>? {
            return value.downcastRef<TupleGen<Value>>()
        }
    }

    /** Get the length of the tuple. */
    fun len(): Int = content.size

    /** Tuple elements. */
    fun content(): List<V> = content

    /** Mutable access to tuple elements (used during construction). */
    fun contentMut(): MutableList<V> {
        return content as MutableList<V>
    }

    /** Iterate over the elements of the tuple. */
    fun iter(): Iterator<Value> {
        return content.map { (it as ValueLike<*>).toValue() }.iterator()
    }

    override fun toString(): String {
        // For single-item tuples we need to add a trailing ','
        return if (content.size == 1) {
            "(${content[0]},)"
        } else {
            content.joinToString(", ", "(", ")")
        }
    }

    override fun isSpecial(): Boolean = true

    override fun toBool(): Boolean = len() != 0

    override fun writeHash(hasher: StarlarkHasher): Result<Unit> {
        for (v in content()) {
            (v as ValueLike<*>).writeHash(hasher).getOrElse { return Result.failure(it) }
        }
        return Result.success(Unit)
    }

    override fun equals(other: Value): Result<Boolean> {
        val otherTuple = TupleGen.fromValue(other) ?: return Result.success(false)
        // `fromValue` returns TupleGen<Value> via an unchecked cast, but the underlying tuple may
        // hold FrozenValue elements (FrozenTuple). Read both lists through ValueLike<*> — the common
        // supertype — so element access does not generate a checkcast to Value that would fail
        // for FrozenValue instances.
        val xs = content() as List<ValueLike<*>>
        val ys = otherTuple.content() as List<ValueLike<*>>
        return equalsSlice<Exception, ValueLike<*>, ValueLike<*>>(xs, ys) { x, y -> x.equals(y.toValue()) }
    }

    override fun compare(other: Value): Result<Int> {
        val otherTuple = TupleGen.fromValue(other)
            ?: return ValueError.unsupportedWith(TupleGen.TYPE, "cmp()", other)
        val xs = content() as List<ValueLike<*>>
        val ys = otherTuple.content() as List<ValueLike<*>>
        return compareSlice<Exception, ValueLike<*>, ValueLike<*>>(xs, ys) { x, y -> x.compare(y.toValue()) }
    }

    override fun at(index: Value, _heap: Heap): Result<Value> {
        val i = convertIndex(index, len()).getOrElse { return Result.failure(it) }
        return Result.success((content()[i] as ValueLike<*>).toValue())
    }

    override fun length(): Result<Int> = Result.success(len())

    override fun isIn(other: Value): Result<Boolean> {
        for (x in content()) {
            if ((x as ValueLike<*>).equals(other).getOrThrow()) {
                return Result.success(true)
            }
        }
        return Result.success(false)
    }

    override fun slice(start: Value?, stop: Value?, stride: Value?, heap: Heap): Result<Value> {
        val sliced = applySlice(content(), start, stop, stride).getOrElse {
            return Result.failure(it)
        }
        return Result.success(heap.allocTuple(sliced.map { (it as ValueLike<*>).toValue() }))
    }

    override fun iterate(me: Value, heap: Heap): Result<Value> = Result.success(me)

    override fun iterSizeHint(index: Int): Pair<Int, Int?> {
        val rem = len() - index
        return Pair(rem, rem)
    }

    override fun iterNext(index: Int, heap: Heap): Value? {
        return content().getOrNull(index)?.let { (it as ValueLike<*>).toValue() }
    }

    override fun iterStop() {}

    override fun add(other: Value, heap: Heap): Result<Value>? {
        val otherTuple = TupleGen.fromValue(other) ?: return null
        val result = mutableListOf<Value>()
        for (x in content()) {
            result.add((x as ValueLike<*>).toValue())
        }
        // `otherTuple` may actually be TupleGen<FrozenValue>; iterate via ValueLike<*> to avoid
        // the implicit Value checkcast that would fail for FrozenValue.
        val ys = otherTuple.content() as List<ValueLike<*>>
        for (x in ys) {
            result.add(x.toValue())
        }
        return Result.success(heap.allocTuple(result))
    }

    override fun mul(other: Value, heap: Heap): Result<Value>? {
        val l = other.unpackI32() ?: return null
        val result = mutableListOf<Value>()
        for (i in 0 until l) {
            result.addAll(content().map { (it as ValueLike<*>).toValue() })
        }
        return Result.success(heap.allocTuple(result))
    }

    override fun rmul(lhs: Value, heap: Heap): Result<Value>? = mul(lhs, heap)

    override fun collectReprCycle(collector: StringBuilder) {
        collector.append("(...)")
    }

    override fun getTypeStarlarkRepr(): Ty = Ty.anyTuple()
}

// Frozen and unfrozen tuples share `TupleGen`; the inner value type distinguishes them.

/** The empty tuple, statically allocated. */
val VALUE_EMPTY_TUPLE: AllocStaticSimple<TupleGen<FrozenValue>> =
    AllocStaticSimple.alloc(TupleGen(emptyList()))

// Serialize support for TupleGen
fun <V> TupleGen<V>.serialize(): List<V> = content()
