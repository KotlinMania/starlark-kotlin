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
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.UnpackValue
import io.github.kotlinmania.starlark_kotlin.values.ValueError
import io.github.kotlinmania.starlark_kotlin.values.types.string.ValueLike
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.AllocStaticSimple
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.toValue
import io.github.kotlinmania.starlark_kotlin.values.types.bigint.unpackInt
import io.github.kotlinmania.starlark_kotlin.values.equalsSlice
import io.github.kotlinmania.starlark_kotlin.values.convertIndex
import io.github.kotlinmania.starlark_kotlin.values.compareSlice
import io.github.kotlinmania.starlark_kotlin.values.applySlice
import io.github.kotlinmania.starlark_kotlin.any.downcastRef

/// Define the tuple type. See [Tuple] and [FrozenTuple] as the two aliases.
class TupleGen<V>(
    /// The data stored by the tuple.
    private val content: List<V>,
) {
    companion object {
        /// `type(())`.
        const val TYPE: String = "tuple"
    }

    /// Get the length of the tuple.
    fun len(): Int = content.size

    /// Tuple elements.
    fun content(): List<V> = content

    /// Mutable access to tuple elements (used during construction).
    fun contentMut(): MutableList<V> {
        @Suppress("UNCHECKED_CAST")
        return content as MutableList<V>
    }

    /// Iterate over the elements of the tuple.
    fun iter(): Iterator<Value> where V : ValueLike {
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
}

/// Runtime type of unfrozen tuple.
typealias Tuple = TupleGen<Value>

/// Runtime type of frozen tuple.
typealias FrozenTuple = TupleGen<FrozenValue>

/// The empty tuple, statically allocated.
val VALUE_EMPTY_TUPLE: AllocStaticSimple<FrozenTuple> =
    AllocStaticSimple.alloc(TupleGen(emptyList()))

/// Downcast a value to a tuple.
fun Tuple.Companion.fromValue(value: Value): Tuple? {
    return value.downcastRef<Tuple>()
}

// StarlarkValue implementation for TupleGen

fun TupleGen<*>.isSpecial(): Boolean = true

fun TupleGen<*>.toBool(): Boolean = len() != 0

fun <V : ValueLike> TupleGen<V>.writeHash(hasher: StarlarkHasher): Result<Unit> {
    for (v in content()) {
        v.writeHash(hasher).getOrElse { return Result.failure(it) }
    }
    return Result.success(Unit)
}

fun <V : ValueLike> TupleGen<V>.equals(other: Value): Result<Boolean> {
    val otherTuple = Tuple.fromValue(other) ?: return Result.success(false)
    return equalsSlice(content(), otherTuple.content()) { x, y -> x.equals(y) }
}

fun <V : ValueLike> TupleGen<V>.compare(other: Value): Result<Int> {
    val otherTuple = Tuple.fromValue(other)
        ?: return ValueError.unsupportedWith(this, "cmp()", other)
    return compareSlice(content(), otherTuple.content()) { x, y -> x.compare(y) }
}

fun <V : ValueLike> TupleGen<V>.at(index: Value, heap: Heap): Result<Value> {
    val i = convertIndex(index, len())
    return Result.success((content()[i] as ValueLike).toValue())
}

fun TupleGen<*>.length(): Result<Int> = Result.success(len())

fun <V : ValueLike> TupleGen<V>.isIn(other: Value): Result<Boolean> {
    for (x in content()) {
        if (x.equals(other).getOrThrow()) {
            return Result.success(true)
        }
    }
    return Result.success(false)
}

fun <V : ValueLike> TupleGen<V>.slice(
    start: Value?,
    stop: Value?,
    stride: Value?,
    heap: Heap,
): Result<Value> {
    val sliced = applySlice(content(), start, stop, stride).getOrElse {
        return Result.failure(it)
    }
    return Result.success(heap.allocTuple(sliced))
}

fun <V : ValueLike> TupleGen<V>.iterate(me: Value, heap: Heap): Result<Value> = Result.success(me)

fun <V : ValueLike> TupleGen<V>.iterSizeHint(index: Int): Pair<Int, Int?> {
    val rem = len() - index
    return Pair(rem, rem)
}

fun <V : ValueLike> TupleGen<V>.iterNext(index: Int, heap: Heap): Value? {
    return content().getOrNull(index)?.let { (it as ValueLike).toValue() }
}

fun <V> TupleGen<V>.iterStop() {}

fun <V : ValueLike> TupleGen<V>.add(other: Value, heap: Heap): Result<Value>? {
    val otherTuple = Tuple.fromValue(other) ?: return null
    val result = mutableListOf<Value>()
    for (x in content()) {
        result.add((x as ValueLike).toValue())
    }
    for (x in otherTuple.content()) {
        result.add(x)
    }
    return Result.success(heap.allocTuple(result))
}

fun <V : ValueLike> TupleGen<V>.mul(other: Value, heap: Heap): Result<Value>? {
    val l = UnpackValue.unpackInt(other) ?: return null
    val result = mutableListOf<Value>()
    for (i in 0 until l) {
        result.addAll(content().map { (it as ValueLike).toValue() })
    }
    return Result.success(heap.allocTuple(result))
}

fun <V : ValueLike> TupleGen<V>.rmul(lhs: Value, heap: Heap): Result<Value>? = mul(lhs, heap)

fun TupleGen<*>.collectReprCycle(collector: StringBuilder) {
    collector.append("(...)")
}

fun TupleGen<*>.typecheckerTy(): Ty? = Ty.anyTuple()

fun TupleGen<*>.getTypeStarlarkRepr(): Ty = Ty.anyTuple()

// Serialize support for TupleGen
fun <V> TupleGen<V>.serialize(): List<V> = content()
