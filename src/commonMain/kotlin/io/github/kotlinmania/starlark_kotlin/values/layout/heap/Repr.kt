// port-lint: source src/values/layout/heap/repr.rs
package io.github.kotlinmania.starlark_kotlin.values.layout.heap

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

import io.github.kotlinmania.starlark_kotlin.ReentrantLock
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.layout.AValue
import io.github.kotlinmania.starlark_kotlin.values.layout.AValueDyn
import io.github.kotlinmania.starlark_kotlin.values.layout.AValueVTable
import io.github.kotlinmania.starlark_kotlin.values.layout.StarlarkValueRawPtr
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.ValueAllocSize
import io.github.kotlinmania.starlark_kotlin.withLock

// #[derive(Clone)]
// #[repr(C)]
// pub(crate) struct AValueHeader(pub(crate) &'static AValueVTable);
class AValueHeader(
    val vtable: AValueVTable,
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

    // impl Hash for AValueHeader
    // fn hash<H: std::hash::Hasher>(&self, state: &mut H) {
    //     ptr::hash(self.0, state);
    // }
    override fun hashCode(): Int = index.hashCode()

    // impl PartialEq for AValueHeader
    // fn eq(&self, other: &Self) -> bool {
    //     ptr::eq(self.0, other.0)
    // }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AValueHeader) return false
        return index == other.index
    }

    // impl Eq for AValueHeader {}
    // impl Dupe for AValueHeader {}

    companion object {
        /** Alignment of objects in Starlark heap (8 bytes for tag bits). */
        // pub(crate) const ALIGN: usize = 8;
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

        // pub(crate) fn new<'v, T: AValue<'v>>() -> AValueHeader
        fun new(vtable: AValueVTable): AValueHeader {
            val header = newConst(vtable)

            val vtableIndex = header.index
            // Check that the LSB is not set, as we reuse that for overwrite
            check(vtableIndex and 1L == 0L)

            return header
        }

        // pub(crate) const fn new_const<'v, T: AValue<'v>>() -> AValueHeader
        fun newConst(vtable: AValueVTable): AValueHeader {
            return AValueHeader(vtable)
        }

        // pub unsafe fn overwrite_with_forward<'v, T: StarlarkValue<'v>>(
        //     me: *mut AValueRepr<T>,
        //     forward_ptr: ForwardPtr,
        // ) -> T
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

    // #[inline]
    // pub(crate) fn payload_ptr(&self) -> StarlarkValueRawPtr
    fun payloadPtr(): StarlarkValueRawPtr {
        return StarlarkValueRawPtr.newHeader(this)
    }

    // pub(crate) unsafe fn payload<'v, T: StarlarkValue<'v>>(&self) -> &T
    @Suppress("UNCHECKED_CAST")
    fun <T : StarlarkValue> payload(): T {
        return payloadPtr().valueRef()
    }

    // pub(crate) unsafe fn unpack_value<'v>(&'v self, heap_kind: HeapKind) -> Value<'v>
    internal fun unpackValue(heapKind: HeapKind): Value {
        return when (heapKind) {
            HeapKind.Unfrozen -> Value.newPtrQueryIsStr(this)
            HeapKind.Frozen -> FrozenValue.newPtrQueryIsStr(this).toValue()
        }
    }

    // pub(crate) fn unpack<'v>(&'v self) -> AValueDyn<'v>
    internal fun unpack(): AValueDyn {
        return AValueDyn(payloadPtr(), vtable)
    }

    // pub(crate) unsafe fn as_repr<'v, T: StarlarkValue<'v>>(&self) -> &AValueRepr<T>
    @Suppress("UNCHECKED_CAST")
    internal fun <T> asRepr(): AValueRepr<T> {
        // In Rust, this casts the header pointer to an AValueRepr pointer.
        // In Kotlin, the AValueRepr that owns this header is looked up
        // through the repr registry using the header's index.
        val repr = reprRegistry[index]
        check(repr != null) { "asRepr: header index $index" }
        return repr as AValueRepr<T>
    }

    // fn as_avalue_or_header(&self) -> &AValueOrForward
    private fun asAvalueOrForward(): AValueOrForward {
        return AValueOrForward.Header(this)
    }

    // pub(crate) fn alloc_size(&self) -> ValueAllocSize
    fun allocSize(): ValueAllocSize {
        return asAvalueOrForward().allocSize()
    }
}

/** Registry mapping header index to its owning AValueRepr, for asRepr() lookups. */
internal val reprRegistry: MutableMap<Long, AValueRepr<*>> = mutableMapOf()

/// How object is represented in arena.
// #[repr(C, align(8))]
// pub(crate) struct AValueRepr<T> {
//     pub(crate) header: AValueHeader,
//     pub(crate) payload: T,
// }
class AValueRepr<T>(
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
        // const _ASSERTIONS: () = {
        //     assert!(mem::align_of::<Self>() == AValueHeader::ALIGN);
        // };
        // (Alignment is managed by the JVM; no assertion needed.)

        // pub(crate) const fn with_metadata(
        //     metadata: &'static AValueVTable,
        //     payload: T,
        // ) -> AValueRepr<T>
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

    // pub(crate) fn offset_of_payload() -> usize
    fun offsetOfPayload(): Int {
        // In Rust, this is the byte offset of payload within the repr struct.
        // In Kotlin, we simulate with the header's conceptual size.
        return AValueHeader.ALIGN
    }

    // pub(crate) fn padding_after_header() -> usize
    fun paddingAfterHeader(): Int {
        return offsetOfPayload() - AValueHeader.ALIGN
    }

    // pub(crate) fn offset_of_extra<'v>() -> usize
    // where T: AValue<'v>
    fun offsetOfExtra(avalue: AValue): Int {
        return offsetOfPayload() + avalue.offsetOfExtra()
    }

    // pub(crate) fn from_payload_ptr_mut(payload_ptr: *mut T) -> *mut AValueRepr<T>
    // Pointer arithmetic for payload-to-repr conversion is handled by the registry.
}

/// "Forward" pointer (pointer to another heap during GC).
///
/// This pointer has `TAG_STR` bit set if it points to a string.
///
/// Lower bit (which is the same bit as `TAG_UNFROZEN`) is always unset
/// regardless of whether it points to frozen or unfrozen value.
/// User of this struct must set this bit explicitly if needed.
// #[derive(Copy, Clone, Dupe)]
// pub(crate) struct ForwardPtr(usize);
internal class ForwardPtr private constructor(
    private val rawValue: Long,
) {
    companion object {
        // fn new(ptr: usize) -> ForwardPtr
        private fun new(ptr: Long): ForwardPtr {
            check(ptr and 1L == 0L)
            return ForwardPtr(ptr)
        }

        // pub(crate) fn new_frozen(value: FrozenValue) -> ForwardPtr
        fun newFrozen(value: FrozenValue): ForwardPtr {
            return new(value.ptr.raw().ptrValue())
        }

        // pub(crate) fn new_unfrozen(value: Value) -> ForwardPtr
        fun newUnfrozen(value: Value): ForwardPtr {
            check(value.unpackFrozen() == null)
            return new(value.ptr.raw().ptrValue() and 1L.inv())
        }
    }

    // pub(crate) unsafe fn unpack_frozen_value(self) -> FrozenValue
    fun unpackFrozenValue(): FrozenValue {
        return FrozenValue.newPtrUsizeWithStrTag(rawValue)
    }

    // pub(crate) unsafe fn unpack_unfrozen_value<'v>(self) -> Value<'v>
    fun unpackUnfrozenValue(): Value {
        return Value.newPtrUsizeWithStrTag(rawValue)
    }

    // pub(crate) unsafe fn unpack_value<'v>(self, heap_kind: HeapKind) -> Value<'v>
    fun unpackValue(heapKind: HeapKind): Value {
        return when (heapKind) {
            HeapKind.Unfrozen -> unpackUnfrozenValue()
            HeapKind.Frozen -> unpackFrozenValue().toValue()
        }
    }
}

/// This is object written over [`AValueRepr`] during GC.
// #[repr(C)]
// #[derive(Debug)]
// pub(crate) struct AValueForward {
//     forward_ptr: usize,
//     object_size: ValueAllocSize,
// }
internal class AValueForward(
    /** The forward pointer to the moved object. */
    private val forward: ForwardPtr,
    /** Size of `AValueRepr<T>` including extra. */
    val objectSize: ValueAllocSize,
) {
    companion object {
        // pub(crate) fn new(forward_ptr: ForwardPtr, object_size: ValueAllocSize) -> AValueForward
        fun new(forwardPtr: ForwardPtr, objectSize: ValueAllocSize): AValueForward {
            return AValueForward(forwardPtr, objectSize)
        }
    }

    // pub(crate) fn forward_ptr(&self) -> ForwardPtr
    fun forwardPtr(): ForwardPtr = forward

    // pub(crate) fn assert_does_not_overwrite_extra<'v, T: AValue<'v>>()
    fun assertDoesNotOverwriteExtra(reprOffsetOfExtra: Int) {
        // In Rust: assert!(mem::size_of::<AValueForward>() <= AValueRepr::<T>::offset_of_extra())
        // AValueForward conceptual size is 2 words (forward ptr + size).
        val forwardSize = 2 * AValueHeader.ALIGN
        check(forwardSize <= reprOffsetOfExtra)
    }

    override fun toString(): String = "AValueForward(objectSize=$objectSize)"
}

/// Object on the heap, either a real object or a forward.
// #[repr(C)]
// pub(crate) union AValueOrForward {
//     header: ManuallyDrop<AValueHeader>,
//     forward: ManuallyDrop<AValueForward>,
//     flags: usize,
// }
internal sealed class AValueOrForward {
    class Header(val header: AValueHeader) : AValueOrForward()
    class Forward(val forward: AValueForward) : AValueOrForward()

    // #[inline]
    // fn is_forward(&self) -> bool
    private fun isForward(): Boolean = this is Forward

    // pub(crate) fn unpack(&self) -> AValueOrForwardUnpack<'_>
    fun unpack(): AValueOrForwardUnpack {
        return when (this) {
            is Header -> AValueOrForwardUnpack.Header(header)
            is Forward -> AValueOrForwardUnpack.Forward(forward)
        }
    }

    // #[inline]
    // pub(crate) unsafe fn unpack_header_unchecked(&self) -> &AValueHeader
    fun unpackHeaderUnchecked(): AValueHeader {
        check(!isForward())
        return (this as Header).header
    }

    // pub(crate) fn unpack_header(&self) -> Option<&AValueHeader>
    fun unpackHeader(): AValueHeader? {
        return when (this) {
            is Header -> header
            is Forward -> null
        }
    }

    // pub(crate) fn unpack_forward(&self) -> Option<&AValueForward>
    fun unpackForward(): AValueForward? {
        return when (this) {
            is Header -> null
            is Forward -> forward
        }
    }

    /// Size of allocation for this object:
    /// following object is allocated at `self + alloc_size + align up`.
    // pub(crate) fn alloc_size(&self) -> ValueAllocSize
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

/// `AValueOrForward` as enum.
// pub(crate) enum AValueOrForwardUnpack<'a> {
//     Header(&'a AValueHeader),
//     Forward(&'a AValueForward),
// }
internal sealed class AValueOrForwardUnpack {
    class Header(val header: AValueHeader) : AValueOrForwardUnpack()
    class Forward(val forward: AValueForward) : AValueOrForwardUnpack()
}
