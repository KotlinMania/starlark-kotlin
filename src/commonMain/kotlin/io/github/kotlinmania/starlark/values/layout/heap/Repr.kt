// port-lint: source src/values/layout/heap/repr.rs
package io.github.kotlinmania.starlark.values.layout.heap

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

import io.github.kotlinmania.starlark.ReentrantLock
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.AValue
import io.github.kotlinmania.starlark.values.layout.AValueDyn
import io.github.kotlinmania.starlark.values.layout.AValueVTable
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.StarlarkValueRawPtr
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.ValueAllocSize
import io.github.kotlinmania.starlark.withLock

internal class AValueHeader(
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
        val ALIGN: Int = 8

        /** Global counter for assigning aligned indices. */
        private var counter: Long = ALIGN.toLong()

        fun currentCounter(): Long = lock.withLock { counter }

        /** Global registry mapping index -> AValueHeader. */
        private val headerRegistry: MutableMap<Long, AValueHeader> = mutableMapOf()

        /** Lock for thread-safe index allocation. */
        private val lock = ReentrantLock()

        /** Allocate the next aligned index. */
        private fun nextIndex(): Long =
            lock.withLock {
                val idx = counter
                counter += ALIGN
                idx
            }

        /** Look up an AValueHeader by its index. */
        fun fromIndex(index: Long): AValueHeader =
            headerRegistry[index]
                ?: throw IllegalArgumentException("No AValueHeader registered for index $index")

        fun new(vtable: AValueVTable): AValueHeader {
            val header = newConst(vtable)

            val vtableIndex = header.index
            // Check that the LSB is not set, as we reuse that for overwrite
            check(vtableIndex and 1L == 0L)

            return header
        }

        fun newConst(vtable: AValueVTable): AValueHeader = AValueHeader(vtable)

        //     me: *mut AValueRepr<T>,
        //     forward_ptr: ForwardPtr,
        // ) -> T
        internal fun overwriteWithForward(
            me: AValueRepr<*>,
            forwardPtr: ForwardPtr,
        ): StarlarkValue {
            val sz = me.header.unpack().memorySize()
            val payload = me.payload
            me.overwritten = AValueForward.new(forwardPtr, sz)
            return payload as? StarlarkValue
                ?: error("Expected StarlarkValue payload")
        }
    }

    fun payloadPtr(): StarlarkValueRawPtr {
        // In Rust, this does pointer arithmetic from the header to the payload
        // area in contiguous arena memory. In Kotlin, the StarlarkValue is stored
        // in the vtable since there's no raw memory layout.
        return StarlarkValueRawPtr(vtable.starlarkValue)
    }

    fun payload(): StarlarkValue = payloadPtr().starlarkValue()

    internal fun unpackValue(heapKind: HeapKind): Value =
        when (heapKind) {
            HeapKind.Unfrozen -> Value.newPtrQueryIsStr(this)
            HeapKind.Frozen -> FrozenValue.newPtrQueryIsStr(this).toValue()
        }

    internal fun unpack(): AValueDyn = AValueDyn(payloadPtr(), vtable)

    internal fun asRepr(): AValueRepr<*> {
        val repr = reprRegistry[index]
        check(repr != null) { "asRepr: header index $index" }
        return repr
    }

    private fun asAvalueOrForward(): AValueOrForward = AValueOrForward.Header(this)

    fun allocSize(): ValueAllocSize = asAvalueOrForward().allocSize()
}

/** Registry mapping header index to its owning AValueRepr, for asRepr() lookups. */
internal val reprRegistry: MutableMap<Long, AValueRepr<*>> = mutableMapOf()

// / How object is represented in arena.
internal class AValueRepr<T>(
    val header: AValueHeader,
    /** Payload of the object, i.e. the StarlarkValue. */
    val payload: T,
) {
    /**
     * When this repr is overwritten during GC, the forward information is stored here.
     * In Rust this is done by overwriting memory in-place via a union.
     */
    internal var overwritten: AValueForward? = null

    init {
        // Register this repr so AValueHeader.asRepr() can find it.
        reprRegistry[header.index] = this
    }

    companion object {
        // };
        // (Alignment is managed by the JVM; no assertion needed.)

        //     payload: T,
        // ) -> AValueRepr<T>
        fun <T> withMetadata(
            metadata: AValueVTable,
            payload: T,
        ): AValueRepr<T> =
            AValueRepr(
                header = AValueHeader(metadata),
                payload = payload,
            )
    }

    fun offsetOfPayload(): Int {
        // In Rust, this is the byte offset of payload within the repr struct.
        // In Kotlin, we simulate with the header's conceptual size.
        return AValueHeader.ALIGN
    }

    fun paddingAfterHeader(): Int = offsetOfPayload() - AValueHeader.ALIGN

    fun offsetOfExtra(avalue: AValue): Int = offsetOfPayload() + avalue.offsetOfExtra()

    // Pointer arithmetic for payload-to-repr conversion is handled by the registry.
}

// / "Forward" pointer (pointer to another heap during GC).
// /
// / This pointer has `TAG_STR` bit set if it points to a string.
// /
// / Lower bit (which is the same bit as `TAG_UNFROZEN`) is always unset
// / regardless of whether it points to frozen or unfrozen value.
// / User of this struct must set this bit explicitly if needed.
internal class ForwardPtr private constructor(
    private val rawValue: Long,
) {
    companion object {
        private fun new(ptr: Long): ForwardPtr {
            check(ptr and 1L == 0L)
            return ForwardPtr(ptr)
        }

        fun newFrozen(value: FrozenValue): ForwardPtr = new(value.ptr.raw().ptrValue())

        fun newUnfrozen(value: Value): ForwardPtr {
            check(value.unpackFrozen() == null)
            return new(value.ptr.raw().ptrValue() and 1L.inv())
        }
    }

    fun unpackFrozenValue(): FrozenValue = FrozenValue.newPtrUsizeWithStrTag(rawValue)

    fun unpackUnfrozenValue(): Value = Value.newPtrUsizeWithStrTag(rawValue)

    fun unpackValue(heapKind: HeapKind): Value =
        when (heapKind) {
            HeapKind.Unfrozen -> unpackUnfrozenValue()
            HeapKind.Frozen -> unpackFrozenValue().toValue()
        }
}

// / This is object written over [`AValueRepr`] during GC.
//     forward_ptr: usize,
//     object_size: ValueAllocSize,
internal class AValueForward(
    /** The forward pointer to the moved object. */
    private val forward: ForwardPtr,
    /** Size of `AValueRepr<T>` including extra. */
    val objectSize: ValueAllocSize,
) {
    companion object {
        fun new(forwardPtr: ForwardPtr, objectSize: ValueAllocSize): AValueForward = AValueForward(forwardPtr, objectSize)
    }

    fun forwardPtr(): ForwardPtr = forward

    fun assertDoesNotOverwriteExtra(reprOffsetOfExtra: Int) {
        // In Rust: assert!(mem::size_of::<AValueForward>() <= AValueRepr::<T>::offset_of_extra())
        // AValueForward conceptual size is 2 words (forward ptr + size).
        val forwardSize = 2 * AValueHeader.ALIGN
        check(forwardSize <= reprOffsetOfExtra)
    }

    override fun toString(): String = "AValueForward(objectSize=$objectSize)"
}

// / Object on the heap, either a real object or a forward.
//     header: ManuallyDrop<AValueHeader>,
//     forward: ManuallyDrop<AValueForward>,
//     flags: usize,
internal sealed class AValueOrForward {
    class Header(
        val header: AValueHeader,
    ) : AValueOrForward()

    class Forward(
        val forward: AValueForward,
    ) : AValueOrForward()

    private fun isForward(): Boolean = this is Forward

    fun unpack(): AValueOrForwardUnpack =
        when (this) {
            is Header -> {
                val repr = reprRegistry[header.index]
                val forward = repr?.overwritten
                if (forward != null) {
                    AValueOrForwardUnpack.Forward(forward)
                } else {
                    AValueOrForwardUnpack.Header(header)
                }
            }
            is Forward -> AValueOrForwardUnpack.Forward(forward)
        }

    fun unpackHeaderUnchecked(): AValueHeader {
        check(!isForward())
        return (this as Header).header
    }

    fun unpackHeader(): AValueHeader? =
        when (this) {
            is Header -> header
            is Forward -> null
        }

    fun unpackForward(): AValueForward? =
        when (this) {
            is Header -> null
            is Forward -> forward
        }

    // / Size of allocation for this object:
    // / following object is allocated at `self + alloc_size + align up`.
    fun allocSize(): ValueAllocSize =
        when (val u = unpack()) {
            is AValueOrForwardUnpack.Header -> u.header.unpack().memorySize()
            is AValueOrForwardUnpack.Forward -> {
                // Overwritten, so the next word will be the size of the memory
                u.forward.objectSize
            }
        }
}

// / `AValueOrForward` as enum.
internal sealed class AValueOrForwardUnpack {
    class Header(
        val header: AValueHeader,
    ) : AValueOrForwardUnpack()

    class Forward(
        val forward: AValueForward,
    ) : AValueOrForwardUnpack()
}
