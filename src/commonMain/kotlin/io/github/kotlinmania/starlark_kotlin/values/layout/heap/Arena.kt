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

import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.ProfilerInstant
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.layout.AValue
import io.github.kotlinmania.starlark_kotlin.values.layout.AValueImpl
import io.github.kotlinmania.starlark_kotlin.values.layout.AValueVTable
import io.github.kotlinmania.starlark_kotlin.values.layout.AlignedSize
import io.github.kotlinmania.starlark_kotlin.values.layout.ConstTypeId
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.ValueAllocSize
import io.github.kotlinmania.starlark_kotlin.values.layout.heapCopyImpl
import io.github.kotlinmania.starlark_kotlin.values.layout.heapFreezeSimpleImpl
import io.github.kotlinmania.starlark_kotlin.values.layout.tryFreezeDirectly
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.str_.VALUE_STR_A_VALUE_PTR
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.StarlarkStr
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.AValueHeader
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.AValueOrForward
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.AValueRepr
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.HeapKind
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.profile.HeapSummary
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.profile.SmallMap
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.profile.alloc_counts.AllocCounts
import io.github.kotlinmania.starlark_kotlin.values.layout.totalMemoryForProfile
import io.github.kotlinmania.starlark_kotlin.values.starlark_type_id.StarlarkTypeId

/**
 * Min size of allocated object including header.
 * Should be able to fit `BlackHole` or forward.
 */
// pub(crate) const MIN_ALLOC: AlignedSize
internal val MIN_ALLOC: AlignedSize = AlignedSize.newBytes(16)

/**
 * Build an [AValueVTable] from a [StarlarkValue] instance.
 * This mirrors the Rust `AValueHeader::new::<T>()` which creates
 * a static vtable from the type parameter at compile time.
 */
private fun vtableForValue(value: StarlarkValue): AValueVTable {
    val typeId = ConstTypeId.of(value::class)
    return AValueVTable(
        staticTypeOfValue = typeId,
        starlarkTypeId = StarlarkTypeId.fromTypeId(typeId),
        typeName = value.TYPE,
        isStr = value is StarlarkStr,
        memorySizeFn = { _ -> ValueAllocSize.new(AlignedSize.newBytes(16)) },
        heapFreezeFn = { p, freezer ->
            val sv = p.valueRef<StarlarkValue>()
            val direct = tryFreezeDirectly(sv, freezer)
            if (direct != null) direct
            else heapFreezeSimpleImpl(sv, freezer)
        },
        heapCopyFn = { p, tracer ->
            val sv = p.valueRef<StarlarkValue>()
            heapCopyImpl(sv, tracer) { _, _ -> }
        },
        starlarkValue = value,
    )
}

/**
 * Reservation is morally a Reservation<T>, but we treat is as an
 * existential. Tied to the lifetime of the heap.
 */
// pub(crate) struct Reservation<'v, T: AValue<'v>>
class Reservation<T : AValue> internal constructor(
    private val list: MutableList<AValueOrForward>,
    private val index: Int,
    private val header: AValueHeader,
) {
    // pub(crate) fn fill(self, x: T::StarlarkValue)
    fun fill(x: StarlarkValue) {
        val newHeader = AValueHeader(vtableForValue(x))
        val newRepr = AValueRepr(header = newHeader, payload = x)
        list[index] = AValueOrForward.Header(newRepr.header)
    }

    // pub(crate) fn ptr(&self) -> &'v AValueHeader
    fun ptr(): AValueHeader {
        return header
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
    /** Arena for things which don't need dropping (e.g. strings). */
    // non_drop: A,
    private val nonDrop: MutableList<AValueOrForward> = mutableListOf()
    /** Arena for things which might need dropping (e.g. Vec, with memory on heap). */
    // drop: A,
    private val drop: MutableList<AValueOrForward> = mutableListOf()

    // pub(crate) fn is_empty(&self) -> bool
    fun isEmpty(): Boolean {
        return allocatedBytes() == 0
    }

    /** Number of allocated bytes plus padding size. */
    // pub(crate) fn allocated_bytes(&self) -> usize
    fun allocatedBytes(): Int {
        return drop.size + nonDrop.size
    }

    // pub(crate) fn available_bytes(&self) -> usize
    fun availableBytes(): Int {
        return Int.MAX_VALUE
    }

    /** Don't forget to call this function to release memory. */
    // pub(crate) fn finish(&mut self)
    fun finish() {
        drop.clear()
        nonDrop.clear()
    }

    // pub(crate) fn reserve_with_extra<'v2, T: AValue<'v2>>(&self, extra_len: usize) -> (Reservation, extra)
    fun <T : AValue> reserveWithExtra(_extraLen: Int): Reservation<T> {
        // In Rust, a BlackHole is written as a placeholder until fill() is called.
        // In Kotlin, we insert a placeholder Header entry.
        val blackHoleHeader = AValueHeader(AValueVTable.newBlackHole())
        val entry = AValueOrForward.Header(blackHoleHeader)
        val index = drop.size
        drop.add(entry)
        return Reservation(drop, index, blackHoleHeader)
    }

    /** Allocate a type `T`. */
    // pub(crate) fn alloc<'v, 'v2, T: AValue<'v2>>(&'v self, x: AValueImpl<'v2, T>) -> &'v AValueRepr
    fun <T : AValue> alloc(x: AValueImpl<T>): AValueRepr<StarlarkValue> {
        val header = AValueHeader(vtableForValue(x.value))
        val repr = AValueRepr(
            header = header,
            payload = x.value,
        )
        val entry = AValueOrForward.Header(repr.header)
        drop.add(entry)
        return repr
    }

    /** Allocate a type `T` plus `extra` bytes. */
    // pub(crate) fn alloc_extra<'v, T: AValue<'v>>(&self, x: AValueImpl) -> (repr, extra)
    fun <T : AValue> allocExtra(x: AValueImpl<T>): AValueRepr<StarlarkValue> {
        return alloc(x)
    }

    // pub(crate) fn alloc_str(&self, x: &str) -> *mut AValueHeader
    fun allocStr(x: String): AValueHeader {
        // In Rust, the StarlarkStr header + string bytes are laid out contiguously in
        // the arena's byte buffer.  In Kotlin there is no raw memory, so we create an
        // AValueHeader whose vtable carries the actual StarlarkStr instance (via
        // `starlarkValue`) so that `Value.unpackStarlarkStr()` can find it through
        // `getRef().downcastRef<StarlarkStr>()`.
        val str = StarlarkStr(x)
        val typeId = ConstTypeId.of<StarlarkStr>()
        val header = AValueHeader(
            AValueVTable(
                staticTypeOfValue = typeId,
                starlarkTypeId = StarlarkTypeId.fromTypeId(typeId),
                typeName = "string",
                isStr = true,
                memorySizeFn = { _ ->
                    val byteLen = str.len()
                    ValueAllocSize.new(
                        AlignedSize.alignUp(StarlarkStr.offsetOfContent() + byteLen)
                    )
                },
                heapFreezeFn = { _, freezer ->
                    val fv = freezer.frozenHeap().allocStrIntern(str.asStr())
                    Result.success(fv.toFrozenValue())
                },
                heapCopyFn = { _, tracer ->
                    tracer.allocStr(str.asStr())
                },
                starlarkValue = str,
            )
        )
        val entry = AValueOrForward.Header(header)
        nonDrop.add(entry)
        return header
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
    internal fun visitArena(
        _heapKind: HeapKind,
        _forwardHeapKind: HeapKind,
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
                is AValueOrForward.Header -> f(entry.header)
                is AValueOrForward.Forward -> {}
            }
        }
    }

    // fn for_each_unordered<'a>(&'a self, f: impl FnMut(&'a AValueHeader))
    fun forEachUnordered(f: (AValueHeader) -> Unit) {
        forEachDropUnordered(f)
        for (entry in nonDrop) {
            when (entry) {
                is AValueOrForward.Header -> f(entry.header)
                is AValueOrForward.Forward -> {}
            }
        }
    }

    // pub(crate) fn allocated_summary(&self) -> HeapSummary
    fun allocatedSummary(): HeapSummary {
        val summary = SmallMap<String, AllocCounts>()
        forEachUnordered { header ->
            val typeName = header.vtable.typeName
            val counts = summary.entry(typeName).orInsertWith { AllocCounts() }
            counts.count += 1
            counts.bytes += header.totalMemoryForProfile()
        }
        return HeapSummary(summary = summary)
    }

    override fun toString(): String {
        return "Arena(drop=${drop.size}, non_drop=${nonDrop.size})"
    }
}
