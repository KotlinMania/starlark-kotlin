// port-lint: source src/values/layout/avalue.rs
package io.github.kotlinmania.starlark_kotlin.values.layout

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

import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Tracer
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.AValueHeader
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.arena.MIN_ALLOC

/** Extended vtable methods (those not covered by [StarlarkValue]). */
interface AValue {

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
 * `heap_freeze` implementation for simple [StarlarkValue] and `StarlarkFloat`
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

/** Common `heap_copy` implementation for types without extra. */
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
) {
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
