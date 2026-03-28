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

import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.layout.ValueAllocSize
import io.github.kotlinmania.starlark_kotlin.values.types.int.AValueVTable
import io.github.kotlinmania.starlark_kotlin.values.types.int.AValueDyn
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.memorySize

/// In Kotlin, AValueHeader wraps the vtable reference for an allocated value.
/// The low-level pointer tagging and alignment concerns from Rust are not
/// applicable; the JVM handles object layout and GC.
// pub(crate) struct AValueHeader
class AValueHeader(
    val vtable: AValueVTable,
) {
    /// Unpack the header into its dynamic value accessor.
    // pub(crate) fn unpack<'v>(&'v self) -> AValueDyn<'v>
    fun unpack(): AValueDyn {
        return AValueDyn(vtable)
    }

    /// Size of allocation for this object.
    // pub(crate) fn alloc_size(&self) -> ValueAllocSize
    fun allocSize(): ValueAllocSize = unpack().memorySize()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AValueHeader) return false
        return vtable === other.vtable
    }

    override fun hashCode(): Int = System.identityHashCode(vtable)

    companion object {
        /// Alignment of objects in Starlark heap (8 bytes for tag bits).
        const val ALIGN: Int = 8
    }
}

/// How object is represented in arena.
///
/// In Kotlin, this is simplified: the JVM manages object layout,
/// so we just pair the header with its payload.
// pub(crate) struct AValueRepr<T>
class AValueRepr<T>(
    val header: AValueHeader,
    /// Payload of the object, i.e. the StarlarkValue.
    val payload: T,
)

/// "Forward" pointer used during GC to indicate an object has been moved.
///
/// In Kotlin, where the JVM handles GC, this is a simplified wrapper
/// that stores a reference to the moved value.
// pub(crate) struct ForwardPtr
class ForwardPtr private constructor(
    private val target: Any?,
) {
    companion object {
        // pub(crate) fn new_frozen(value: FrozenValue) -> ForwardPtr
        fun newFrozen(value: FrozenValue): ForwardPtr = ForwardPtr(value)

        // pub(crate) fn new_unfrozen(value: Value) -> ForwardPtr
        fun newUnfrozen(value: Value): ForwardPtr = ForwardPtr(value)
    }

    // pub(crate) unsafe fn unpack_frozen_value(self) -> FrozenValue
    fun unpackFrozenValue(): FrozenValue = target as FrozenValue

    // pub(crate) unsafe fn unpack_unfrozen_value<'v>(self) -> Value<'v>
    fun unpackUnfrozenValue(): Value = target as Value

    // pub(crate) unsafe fn unpack_value<'v>(self, heap_kind: HeapKind) -> Value<'v>
    fun unpackValue(heapKind: HeapKind): Value = when (heapKind) {
        HeapKind.Unfrozen -> unpackUnfrozenValue()
        HeapKind.Frozen -> unpackFrozenValue().toValue()
    }
}

/// Object written over an AValueRepr during GC to mark it as forwarded.
///
/// In Kotlin this is a simplified data holder since we don't overwrite memory.
// pub(crate) struct AValueForward
class AValueForward(
    /// The forward pointer to the moved object.
    private val forward: ForwardPtr,
    /// Size of the original allocation.
    val objectSize: ValueAllocSize,
) {
    // pub(crate) fn forward_ptr(&self) -> ForwardPtr
    fun forwardPtr(): ForwardPtr = forward

    companion object {
        // pub(crate) fn new(forward_ptr: ForwardPtr, object_size: ValueAllocSize) -> AValueForward
        fun new(forwardPtr: ForwardPtr, objectSize: ValueAllocSize): AValueForward =
            AValueForward(forwardPtr, objectSize)
    }
}

/// Object on the heap, either a real object or a forward.
// pub(crate) union AValueOrForward
sealed class AValueOrForward {
    class Header(val header: AValueHeader) : AValueOrForward()
    class Forward(val forward: AValueForward) : AValueOrForward()

    // pub(crate) fn unpack(&self) -> AValueOrForwardUnpack<'_>
    fun unpack(): AValueOrForwardUnpack = when (this) {
        is Header -> AValueOrForwardUnpack.Header(header)
        is Forward -> AValueOrForwardUnpack.Forward(forward)
    }

    // pub(crate) fn unpack_header(&self) -> Option<&AValueHeader>
    fun unpackHeader(): AValueHeader? = when (this) {
        is Header -> header
        is Forward -> null
    }

    // pub(crate) fn unpack_forward(&self) -> Option<&AValueForward>
    fun unpackForward(): AValueForward? = when (this) {
        is Header -> null
        is Forward -> forward
    }

    /// Size of allocation for this object.
    // pub(crate) fn alloc_size(&self) -> ValueAllocSize
    fun allocSize(): ValueAllocSize = when (this) {
        is Header -> header.unpack().memorySize()
        is Forward -> forward.objectSize
    }
}

/// AValueOrForward as enum.
// pub(crate) enum AValueOrForwardUnpack<'a>
sealed class AValueOrForwardUnpack {
    class Header(val header: AValueHeader) : AValueOrForwardUnpack()
    class Forward(val forward: AValueForward) : AValueOrForwardUnpack()
}
