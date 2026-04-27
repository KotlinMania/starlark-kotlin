// port-lint: source src/values/layout/avalue.rs
package io.github.kotlinmania.starlark.values.layout

/*
 * Copyright 2019 The Starlark in Rust Authors.
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

import io.github.kotlinmania.starlark.environment.Module
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.layout.Freezer
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.AlignedSize
import io.github.kotlinmania.starlark.values.layout.avalues.allocTuple
import io.github.kotlinmania.starlark.values.layout.heap.AValueHeader
import io.github.kotlinmania.starlark.values.layout.heap.AValueRepr
import io.github.kotlinmania.starlark.values.layout.heap.ForwardPtr
import io.github.kotlinmania.starlark.values.layout.heap.arena.MIN_ALLOC
import io.github.kotlinmania.starlark.values.layout.ValueAllocSize
import starlarkmap.smallmap.SmallMap
import io.github.kotlinmania.starlark.values.types.dict.Dict
import io.github.kotlinmania.starlark.values.types.dict.allocValue
import io.github.kotlinmania.starlark.values.types.list.ListData
import io.github.kotlinmania.starlark.values.types.list.allocList
import io.github.kotlinmania.starlark.values.types.tuple.unpackTuple2

/**
 * Extended vtable methods (those not covered by [StarlarkValue]).
 *
 * Sealed: the upstream `values/layout/avalues.rs` mod groups the closed set of
 * AValue implementations (array, complex, list, simple, static_, str_, tuple).
 * Sealing the contract here gives the Kotlin compiler the same closed-variant
 * guarantee that the Rust mod declaration provides; concrete implementations
 * live in the `avalues` subpackage.
 */
sealed interface AValue {

    /**
     * Certain types like `Tuple` or `StarlarkStr` have payload array
     * placed in a heap after `Self`. This is the type of an element of that array.
     */
    val extraElemSize: Int get() = 0

    /** Payload array length. */
    fun extraLen(value: StarlarkValue): Int

    /**
     * Offset of field holding content, in bytes.
     *
     * Return size of self if there's no extra content.
     */
    fun offsetOfExtra(): Int

    /** Type is `StarlarkStr`. */
    val isStr: Boolean get() = false

    /** Memory size of starlark value including `AValueHeader`. */
    fun allocSizeForExtraLen(extraLen: Int): ValueAllocSize {
        val elemSize = extraElemSize
        require(elemSize == 0 || offsetOfExtra() % elemSize == 0) {
            "extra must be aligned"
        }
        val baseSize = AlignedSize.alignUp(offsetOfExtra())
        val minAllocSize = MIN_ALLOC
        // Content is not necessarily aligned to end of `A`.
        val extraSize = AlignedSize.alignUp(
            offsetOfExtra() + (elemSize * extraLen)
        )
        return ValueAllocSize.new(
            maxOf(baseSize, minAllocSize, extraSize)
        )
    }

    /**
     * The memory that should be charged to this value in a profile.
     *
     * Both the size of the value itself and anything it references.
     *
     * This existing is a bit of a hack to let statically allocated values set this to zero.
     */
    fun totalMemoryForProfile(value: StarlarkValue): Int {
        return allocSizeForExtraLen(extraLen(value)).bytes().toInt()
    }

    /** Freeze this value on the heap. */
    fun heapFreeze(freezer: Freezer): Result<FrozenValue>

    /** Copy this value on the heap. */
    fun heapCopy(tracer: Tracer): Value

    /** Unwrapped type. */
    fun unpack(): StarlarkValue
}

/** A value with extended ([AValue]) vtable methods. */
class AValueImpl<T : AValue>(
    internal val value: StarlarkValue,
) {
    companion object {
        fun <T : AValue> new(value: StarlarkValue): AValueImpl<T> {
            return AValueImpl(value)
        }
    }
}

/**
 * If `A` provides a statically allocated frozen value,
 * replace object with the forward to that frozen value instead of using default freeze.
 */
internal fun tryFreezeDirectly(
    payload: StarlarkValue,
    freezer: Freezer,
): Result<FrozenValue>? {
    val f = payload.tryFreezeDirectly(freezer) ?: return null
    return when {
        f.isSuccess -> {
            val frozenValue = f.getOrThrow()
            Result.success(frozenValue)
        }
        else -> f
    }
}

/**
 * `heapFreeze` implementation for simple [StarlarkValue] and `StarlarkFloat`
 * (`StarlarkFloat` is logically a simple type, but it is not considered simple type).
 */
internal fun heapFreezeSimpleImpl(
    value: StarlarkValue,
    freezer: Freezer,
): Result<FrozenValue> {
    val (fv, r) = freezer.reserve<AValue>()
    val x = value
    r.fill(x)
    return Result.success(fv)
}

/** Common `heapCopy` implementation for types without extra. */
internal fun heapCopyImpl(
    value: StarlarkValue,
    tracer: Tracer,
    trace: (StarlarkValue, Tracer) -> Unit,
): Value {
    val (v, r) = tracer.reserve<AValue>()
    val x = value
    // We have to put the forwarding node in _before_ we trace in case there are cycles
    trace(x, tracer)
    r.fill(x)
    return v
}

/** Placeholder used during GC to fill space vacated by a moved object. */
internal class BlackHole(
    internal val size: ValueAllocSize,
) : StarlarkValue {
    override val TYPE: String get() = "BlackHole"

    override fun toString(): String = "BlackHole"
}

/** Total memory for profile. */
internal fun AValueHeader.totalMemoryForProfile(): Long =
    allocSize().bytes().toLong()

/** Copy value using the given tracer. */
internal fun AValueHeader.heapCopy(tracer: Tracer): Value =
    unpack().heapCopy(tracer)

/** Len of a collection. */
internal fun <T> size(list: List<T>): Int = list.size

internal object AValueTests {

    fun tupleCycleFreeze() {
        Module.withTempHeap { module ->
            val list = module.heap().allocList(emptyList())
            val tuple = module.heap().allocTuple(listOf(list))
            ListData.fromValueMut(list).getOrNull()
                ?.push(tuple)
            module.set("t", tuple)
            module.freeze()
            Result.success(Unit)
        }
    }

    fun testTryFreezeDirectly() {
        // `tryFreezeDirectly` is only implemented for `dict` at the moment of writing,
        // so import it for the test.

        Module.withTempHeap { module ->
            val d0 = Dict.new(SmallMap.new()).allocValue(module.heap())
            val d1 = Dict.new(SmallMap.new()).allocValue(module.heap())
            // Pointers are not equal.
            check(d0 !== d1)

            module.setExtraValue(module.heap().allocTuple(listOf(d0, d1)))

            val frozen = module.freeze().getOrThrow()
            val extra = frozen.extraValue()!!.toValue()
            val (fd0, fd1) = unpackTuple2<Value, Value>(extra, { it }, { it })
                ?: error("expected a 2-element tuple")
            // Pointers are equal.
            check(fd0 === fd1)
            Result.success(Unit)
        }
    }
}
