// port-lint: source src/values/layout/avalue.rs
package io.github.kotlinmania.starlark_kotlin.values.layout.avalue

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

import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.Tracer
import io.github.kotlinmania.starlark_kotlin.values.freeze_error.FreezeResult
import io.github.kotlinmania.starlark_kotlin.values.layout.Freezer
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.aligned_size.AlignedSize
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.AValueHeader
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.arena.MIN_ALLOC
import io.github.kotlinmania.starlark_kotlin.values.layout.value_alloc_size.ValueAllocSize

/** Extended vtable methods (those not covered by [StarlarkValue]). */
// Rust: pub(crate) trait AValue<'v>: Sized + 'v
internal interface AValue {

    /** Payload array length. */
    fun extraLen(value: StarlarkValue): Int

    /** Offset of field holding content, in bytes. Return size of self if there's no extra content. */
    fun offsetOfExtra(): Int

    /** Type is `StarlarkStr`. */
    val isStr: Boolean get() = false

    /** Memory size of starlark value including `AValueHeader`. */
    fun allocSizeForExtraLen(extraLen: Int): ValueAllocSize {
        require(offsetOfExtra() % AValueHeader.ALIGN == 0) {
            "extra must be aligned"
        }
        val baseSize = AlignedSize.alignUp(offsetOfExtra())
        val minAllocSize = MIN_ALLOC
        val extraSize = AlignedSize.alignUp(
            offsetOfExtra() + (extraLen * AValueHeader.ALIGN)
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
        val allocSize = allocSizeForExtraLen(extraLen(value))
        val allocBytes = allocSize.bytes().toInt()
        return allocBytes
    }

    /** Freeze this value on the heap. */
    fun heapFreeze(freezer: Freezer): FreezeResult<FrozenValue>

    /** Copy this value on the heap during GC. */
    fun heapCopy(tracer: Tracer): Value

    /** Get the underlying [StarlarkValue]. */
    fun unpack(): StarlarkValue
}

/** A value with extended ([AValue]) vtable methods. */
// Rust: #[repr(C)] pub(crate) struct AValueImpl<'v, T: AValue<'v>>
internal class AValueImpl<T : AValue>(
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
 *
 * @return `null` if the value does not support direct freezing,
 *   otherwise the [FreezeResult] of the frozen value.
 */
internal fun tryFreezeDirectly(
    payload: StarlarkValue,
    freezer: Freezer,
): FreezeResult<FrozenValue>? {
    val f = payload.tryFreezeDirectly(freezer) ?: return null
    return when {
        f.isSuccess -> {
            val frozenValue = f.getOrThrow()
            // Rust: drop(AValueHeader::overwrite_with_forward(me, ForwardPtr::new_frozen(f)))
            // Kotlin GC manages references; no forwarding needed.
            Result.success(frozenValue)
        }
        else -> f
    }
}

/**
 * `heap_freeze` implementation for simple [StarlarkValue] and `StarlarkFloat`.
 *
 * (`StarlarkFloat` is logically a simple type, but it is not considered simple type.)
 */
internal fun heapFreezeSimpleImpl(
    value: StarlarkValue,
    freezer: Freezer,
): FreezeResult<FrozenValue> {
    val (fv, r) = freezer.reserve<AValue>()
    r.fill(value)
    return Result.success(fv)
}

/** Common `heap_copy` implementation for types without extra. */
internal fun heapCopyImpl(
    value: StarlarkValue,
    tracer: Tracer,
    trace: (StarlarkValue, Tracer) -> Unit,
): Value {
    val (v, r) = tracer.reserve<AValue>()
    // We have to put the forwarding node in _before_ we trace in case there are cycles
    trace(value, tracer)
    r.fill(value)
    return v
}

/** Placeholder used during GC to fill space vacated by a moved object. */
// Rust: #[derive(Debug, Display, ProvidesStaticType, Allocative)]
// Rust: #[display("BlackHole")]
internal class BlackHole(
    internal val size: ValueAllocSize,
) {
    override fun toString(): String = "BlackHole"
}
