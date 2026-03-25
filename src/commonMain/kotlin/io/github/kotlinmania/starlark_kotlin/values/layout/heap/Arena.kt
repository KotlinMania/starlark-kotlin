// port-lint: source src/values/layout/heap/arena.rs
package io.github.kotlinmania.starlark_kotlin.values.layout.heap.arena

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

import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.instant.ProfilerInstant
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.layout.aligned_size.AlignedSize
import io.github.kotlinmania.starlark_kotlin.values.layout.avalue.AValue
import io.github.kotlinmania.starlark_kotlin.values.layout.avalue.AValueImpl
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.profile.alloc_counts.AllocCounts
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.profile.HeapSummary
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.HeapKind
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.AValueRepr
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.AValueOrForward
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.AValueHeader
import io.github.kotlinmania.starlark_kotlin.values.layout.value
import io.github.kotlinmania.starlark_kotlin.syntax.payload_and_span.Payload
import io.github.kotlinmania.starlark_kotlin.values.typeName
import io.github.kotlinmania.starlark_kotlin.values.layout.avalue.totalMemoryForProfile
import io.github.kotlinmania.starlark_kotlin.values.empty
import io.github.kotlinmania.starlark_kotlin.tests.derive.module.repr

/// Min size of allocated object including header.
/// Should be able to fit `BlackHole` or forward.
// pub(crate) const MIN_ALLOC: AlignedSize
internal val MIN_ALLOC: AlignedSize = AlignedSize.ofBytes(16)

/// Reservation is morally a Reservation<T>, but we treat is as an
/// existential. Tied to the lifetime of the heap.
// pub(crate) struct Reservation<'v, T: AValue<'v>>
internal class Reservation<T : AValue>(
    private val repr: AValueRepr<StarlarkValue>,
) {
    // pub(crate) fn fill(self, x: T::StarlarkValue)
    fun fill(x: StarlarkValue) {
        repr.Payload = x
    }

    // pub(crate) fn ptr(&self) -> &'v AValueHeader
    fun ptr(): AValueHeader {
        return repr.header
    }
}

// pub(crate) trait ArenaVisitor<'v>
internal interface ArenaVisitor {
    // fn enter_bump(&mut self)
    fun enterBump()
    // fn regular_value(&mut self, value: &'v AValueOrForward)
    fun regularValue(value: AValueOrForward)
    // fn call_enter(&mut self, function: Value<'v>, time: ProfilerInstant)
    fun callEnter(function: Value, time: ProfilerInstant)
    // fn call_exit(&mut self, time: ProfilerInstant)
    fun callExit(time: ProfilerInstant)
}

// #[derive(Default)]
// pub(crate) struct Arena<A: ArenaAllocator>
// Kotlin: GC handles memory. Arena is a simple list of allocated values.
internal class Arena {
    /// Arena for things which don't need dropping (e.g. strings).
    // non_drop: A,
    private val nonDrop: MutableList<AValueOrForward> = mutableListOf()
    /// Arena for things which might need dropping (e.g. Vec, with memory on heap).
    // drop: A,
    private val drop: MutableList<AValueOrForward> = mutableListOf()

    // pub(crate) fn is_empty(&self) -> bool
    fun isEmpty(): Boolean {
        return allocatedBytes() == 0
    }

    /// Number of allocated bytes plus padding size.
    // pub(crate) fn allocated_bytes(&self) -> usize
    fun allocatedBytes(): Int {
        return drop.size + nonDrop.size
    }

    // pub(crate) fn available_bytes(&self) -> usize
    fun availableBytes(): Int {
        return Int.MAX_VALUE
    }

    /// Don't forget to call this function to release memory.
    // pub(crate) fn finish(&mut self)
    fun finish() {
        drop.clear()
        nonDrop.clear()
    }

    // pub(crate) fn reserve_with_extra<'v2, T: AValue<'v2>>(&self, extra_len: usize) -> (Reservation, extra)
    fun <T : AValue> reserveWithExtra(extraLen: Int): Reservation<T> {
        val repr = AValueRepr<StarlarkValue>(
            header = AValueHeader.empty(),
            payload = null,
        )
        val entry = AValueOrForward.value(repr)
        drop.add(entry)
        return Reservation(repr)
    }

    /// Allocate a type `T`.
    // pub(crate) fn alloc<'v, 'v2, T: AValue<'v2>>(&'v self, x: AValueImpl<'v2, T>) -> &'v AValueRepr
    fun <T : AValue> alloc(x: AValueImpl<T>): AValueRepr<StarlarkValue> {
        val repr = AValueRepr(
            header = AValueHeader.forValue(x.value),
            payload = x.value,
        )
        val entry = AValueOrForward.value(repr)
        drop.add(entry)
        return repr
    }

    /// Allocate a type `T` plus `extra` bytes.
    // pub(crate) fn alloc_extra<'v, T: AValue<'v>>(&self, x: AValueImpl) -> (repr, extra)
    fun <T : AValue> allocExtra(x: AValueImpl<T>): AValueRepr<StarlarkValue> {
        return alloc(x)
    }

    // pub(crate) fn alloc_str(&self, x: &str) -> *mut AValueHeader
    fun allocStr(x: String): AValueHeader {
        val repr = AValueRepr<StarlarkValue>(
            header = AValueHeader.forString(x),
            payload = null,
        )
        val entry = AValueOrForward.value(repr)
        nonDrop.add(entry)
        return repr.header
    }

    // fn for_each_ordered<'a>(&'a mut self, f: impl FnMut(ArenaVisitEvent<'a>))
    fun forEachOrdered(f: (AValueOrForward) -> Unit) {
        for (entry in drop) {
            f(entry)
        }
        for (entry in nonDrop) {
            f(entry)
        }
    }

    // pub(crate) unsafe fn visit_arena<'v>(&'v mut self, heap_kind, forward_heap_kind, visitor)
    fun visitArena(
        heapKind: HeapKind,
        forwardHeapKind: HeapKind,
        visitor: ArenaVisitor,
    ) {
        visitor.enterBump()
        for (entry in drop) {
            visitor.regularValue(entry)
        }
        visitor.enterBump()
        for (entry in nonDrop) {
            visitor.regularValue(entry)
        }
    }

    // pub(crate) fn for_each_drop_unordered<'a>(&'a mut self, f: impl FnMut(&'a AValueHeader))
    fun forEachDropUnordered(f: (AValueHeader) -> Unit) {
        for (entry in drop) {
            when (entry) {
                is AValueOrForward.value -> f(entry.repr.header)
                is AValueOrForward.Forward -> {}
            }
        }
    }

    // fn for_each_unordered<'a>(&'a self, f: impl FnMut(&'a AValueHeader))
    fun forEachUnordered(f: (AValueHeader) -> Unit) {
        forEachDropUnordered(f)
        for (entry in nonDrop) {
            when (entry) {
                is AValueOrForward.value -> f(entry.repr.header)
                is AValueOrForward.Forward -> {}
            }
        }
    }

    // pub(crate) fn allocated_summary(&self) -> HeapSummary
    fun allocatedSummary(): HeapSummary {
        val entries = mutableMapOf<String, AllocCounts>()
        forEachUnordered { header ->
            val typeName = header.typeName()
            val counts = entries.getOrPut(typeName) { AllocCounts() }
            counts.count += 1
            counts.bytes += header.totalMemoryForProfile()
        }
        return HeapSummary(summary = entries)
    }

    override fun toString(): String {
        return "Arena(drop=${drop.size}, non_drop=${nonDrop.size})"
    }
}
