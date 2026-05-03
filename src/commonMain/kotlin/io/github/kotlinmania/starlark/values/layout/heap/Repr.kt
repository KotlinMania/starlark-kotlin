// port-lint: source src/values/layout/heap/repr.rs
package io.github.kotlinmania.starlark.values.layout.heap

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

import io.github.kotlinmania.starlark.ReentrantLock
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.AValue
import io.github.kotlinmania.starlark.values.layout.AValueDyn
import io.github.kotlinmania.starlark.values.layout.AValueVTable
import io.github.kotlinmania.starlark.values.layout.StarlarkValueRawPtr
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.ValueAllocSize
import io.github.kotlinmania.starlark.withLock

class AValueHeader(
    var vtable: AValueVTable,
) {
    /**
     * Simulated pointer index for this header, used by the pointer-tagging system.
     * Assigned at construction time via the global registry.
     * The index is guaranteed to be 8-byte aligned (lower 3 bits are zero)
     * so that tag bits can be stored in the low bits.
     */
    val index: Long = nextIndex()

    init {
        // Register this header in the global lookup table so fromIndex can find it.
        headerRegistry[index] = this
    }

    override fun hashCode(): Int = index.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AValueHeader) return false
        return index == other.index
    }

    companion object {
        /** Alignment of objects in Starlark heap (8 bytes for tag bits). */
        const val ALIGN: Int = 8

        /** Global counter for assigning aligned indices. */
        private var counter: Long = ALIGN.toLong()

        /** Global registry mapping index -> AValueHeader. */
        private val headerRegistry: MutableMap<Long, AValueHeader> = mutableMapOf()

        /** Lock for thread-safe index allocation. */
        private val lock = ReentrantLock()

        /** Allocate the next aligned index. */
        private fun nextIndex(): Long = lock.withLock {
            val idx = counter
            counter += ALIGN
            idx
        }

        /** Look up an AValueHeader by its index. */
        fun fromIndex(index: Long): AValueHeader {
            return headerRegistry[index]
                ?: throw IllegalArgumentException("No AValueHeader registered for index $index")
        }

        fun new(vtable: AValueVTable): AValueHeader {
            val header = newConst(vtable)

            val vtableIndex = header.index
            // Check that the LSB is not set, as we reuse that for overwrite
            check(vtableIndex and 1L == 0L)

            return header
        }

        fun newConst(vtable: AValueVTable): AValueHeader {
            return AValueHeader(vtable)
        }

        //     me: *mut AValueRepr<T>,
        internal fun <T> overwriteWithForward(
            me: AValueRepr<T>,
            forwardPtr: ForwardPtr,
        ): T {
            val sz = me.header.unpack().memorySize()
            val payload = me.payload
            me.overwritten = AValueForward.new(forwardPtr, sz)
            return payload
        }
    }

    fun payloadPtr(): StarlarkValueRawPtr {
        return StarlarkValueRawPtr(vtable.starlarkValue)
    }

    fun <T : StarlarkValue> payload(): T {
        return payloadPtr().ptr as T
    }

    internal fun unpackValue(heapKind: HeapKind): Value {
        return when (heapKind) {
            HeapKind.Unfrozen -> Value.newPtrQueryIsStr(this)
            HeapKind.Frozen -> FrozenValue.newPtrQueryIsStr(this).toValue()
        }
    }

    internal fun unpack(): AValueDyn {
        return AValueDyn(payloadPtr(), vtable)
    }

    internal fun <T> asRepr(): AValueRepr<T> {
        val repr = reprRegistry[index]
        check(repr != null) { "asRepr: header index $index" }
        return repr as AValueRepr<T>
    }

    private fun asAvalueOrForward(): AValueOrForward {
        return AValueOrForward.Header(this)
    }

    fun allocSize(): ValueAllocSize {
        return asAvalueOrForward().allocSize()
    }
}

/** Registry mapping header index to its owning AValueRepr, for asRepr() lookups. */
internal val reprRegistry: MutableMap<Long, AValueRepr<*>> = mutableMapOf()

/** How object is represented in arena. */
class AValueRepr<T>(
    val header: AValueHeader,
    /** Payload of the object, i.e. the StarlarkValue. */
    val payload: T,
) {
    /**
     * When this repr is overwritten during GC, the forward information is stored here.
     */
    internal var overwritten: AValueForward? = null

    init {
        // Register this repr so AValueHeader.asRepr() can find it.
        reprRegistry[header.index] = this
    }

    companion object {
        // (Alignment is managed by the JVM; no assertion needed.)

        inline fun <reified T> paddingAfterHeader(): Int {
            // Rust computes type-dependent padding between header and payload.
            // Kotlin objects do not expose a stable layout, so keep this as zero.
            return 0
        }

        fun <T> fromPayloadPtrMut(payload: T): AValueRepr<T> {
            return reprRegistry.values.firstOrNull { it.payload === payload } as? AValueRepr<T>
                ?: error("fromPayloadPtrMut: payload not registered")
        }

        fun <T> withMetadata(
            metadata: AValueVTable,
            payload: T,
        ): AValueRepr<T> {
            return AValueRepr(
                header = AValueHeader(metadata),
                payload = payload,
            )
        }
    }

    fun offsetOfPayload(): Int {
        return AValueHeader.ALIGN
    }

    fun paddingAfterHeader(): Int {
        return offsetOfPayload() - AValueHeader.ALIGN
    }

    fun offsetOfExtra(avalue: AValue): Int {
        return offsetOfPayload() + avalue.offsetOfExtra()
    }

    // Pointer arithmetic for payload-to-repr conversion is handled by the registry.
}

/**
 * "Forward" pointer (pointer to another heap during GC).
 *
 * This pointer has `TAG_STR` bit set if it points to a string.
 *
 * Lower bit (which is the same bit as `TAG_UNFROZEN`) is always unset
 * regardless of whether it points to frozen or unfrozen value.
 * User of this struct must set this bit explicitly if needed.
 */
internal class ForwardPtr private constructor(
    private val rawValue: Long,
) {
    companion object {
        private fun new(ptr: Long): ForwardPtr {
            check(ptr and 1L == 0L)
            return ForwardPtr(ptr)
        }

        fun newFrozen(value: FrozenValue): ForwardPtr {
            return new(value.ptr.raw().ptrValue())
        }

        fun newUnfrozen(value: Value): ForwardPtr {
            check(value.unpackFrozen() == null)
            return new(value.ptr.raw().ptrValue() and 1L.inv())
        }
    }

    fun unpackFrozenValue(): FrozenValue {
        return FrozenValue.newPtrUsizeWithStrTag(rawValue)
    }

    fun unpackUnfrozenValue(): Value {
        return Value.newPtrUsizeWithStrTag(rawValue)
    }

    fun unpackValue(heapKind: HeapKind): Value {
        return when (heapKind) {
            HeapKind.Unfrozen -> unpackUnfrozenValue()
            HeapKind.Frozen -> unpackFrozenValue().toValue()
        }
    }
}

/** This is object written over [`AValueRepr`] during GC. */
internal class AValueForward(
    /** The forward pointer to the moved object. */
    private val forward: ForwardPtr,
    /** Size of `AValueRepr<T>` including extra. */
    val objectSize: ValueAllocSize,
) {
    companion object {
        fun new(forwardPtr: ForwardPtr, objectSize: ValueAllocSize): AValueForward {
            return AValueForward(forwardPtr, objectSize)
        }
    }

    fun forwardPtr(): ForwardPtr = forward

    fun assertDoesNotOverwriteExtra(reprOffsetOfExtra: Int) {
        // AValueForward conceptual size is 2 words (forward ptr + size).
        val forwardSize = 2 * AValueHeader.ALIGN
        check(forwardSize <= reprOffsetOfExtra)
    }

    override fun toString(): String = "AValueForward(objectSize=$objectSize)"
}

/** Object on the heap, either a real object or a forward. */
internal sealed class AValueOrForward {
    class Header(val header: AValueHeader) : AValueOrForward()
    class Forward(val forward: AValueForward) : AValueOrForward()

    private fun isForward(): Boolean = this is Forward

    fun unpack(): AValueOrForwardUnpack {
        return when (this) {
            is Header -> {
                val overwritten = reprRegistry[header.index]?.overwritten
                if (overwritten != null) {
                    AValueOrForwardUnpack.Forward(overwritten)
                } else {
                    AValueOrForwardUnpack.Header(header)
                }
            }
            is Forward -> AValueOrForwardUnpack.Forward(forward)
        }
    }

    fun unpackHeaderUnchecked(): AValueHeader {
        check(!isForward())
        return (this as Header).header
    }

    fun unpackHeader(): AValueHeader? {
        return when (this) {
            is Header -> header
            is Forward -> null
        }
    }

    fun unpackForward(): AValueForward? {
        return when (this) {
            is Header -> null
            is Forward -> forward
        }
    }

    /**
     * Size of allocation for this object:
     * following object is allocated at `self + allocSize + align up`.
     */
    fun allocSize(): ValueAllocSize {
        return when (val u = unpack()) {
            is AValueOrForwardUnpack.Header -> u.header.unpack().memorySize()
            is AValueOrForwardUnpack.Forward -> {
                // Overwritten, so the next word will be the size of the memory
                u.forward.objectSize
            }
        }
    }
}

/** `AValueOrForward` as enum. */
internal sealed class AValueOrForwardUnpack {
    class Header(val header: AValueHeader) : AValueOrForwardUnpack()
    class Forward(val forward: AValueForward) : AValueOrForwardUnpack()
}
