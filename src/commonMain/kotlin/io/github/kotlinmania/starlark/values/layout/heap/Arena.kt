// port-lint: source src/values/layout/heap/arena.rs
package io.github.kotlinmania.starlark.values.layout.heap.arena

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

import starlarkmap.StarlarkHashValue
import io.github.kotlinmania.starlark.eval.runtime.profile.ProfilerInstant
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.AValue
import io.github.kotlinmania.starlark.values.layout.AValueImpl
import io.github.kotlinmania.starlark.values.layout.AValueVTable
import io.github.kotlinmania.starlark.values.layout.AlignedSize
import io.github.kotlinmania.starlark.values.layout.BlackHole
import io.github.kotlinmania.starlark.values.layout.ConstTypeId
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.ValueAllocSize
import io.github.kotlinmania.starlark.values.layout.heapCopyImpl
import io.github.kotlinmania.starlark.values.layout.heapFreezeSimpleImpl
import io.github.kotlinmania.starlark.values.layout.tryFreezeDirectly
import io.github.kotlinmania.starlark.values.layout.heap.AValueHeader
import io.github.kotlinmania.starlark.values.layout.heap.AValueOrForward
import io.github.kotlinmania.starlark.values.layout.heap.AValueOrForwardUnpack
import io.github.kotlinmania.starlark.values.layout.heap.AValueRepr
import io.github.kotlinmania.starlark.values.layout.heap.HeapKind
import io.github.kotlinmania.starlark.values.layout.heap.CallEnter
import io.github.kotlinmania.starlark.values.layout.heap.CallExit
import io.github.kotlinmania.starlark.values.layout.heap.NeedsDrop
import io.github.kotlinmania.starlark.values.layout.heap.NoDrop
import io.github.kotlinmania.starlark.values.layout.heap.allocator.ArenaAllocator
import io.github.kotlinmania.starlark.values.layout.heap.allocator.alloc.ChunkAllocator
import io.github.kotlinmania.starlark.values.layout.heap.profile.HeapSummary
import io.github.kotlinmania.starlark.values.layout.heap.profile.SmallMap
import io.github.kotlinmania.starlark.values.layout.heap.profile.alloccounts.AllocCounts
import io.github.kotlinmania.starlark.values.types.string.StarlarkStr
import io.github.kotlinmania.starlark.values.starlarktypeid.StarlarkTypeId
import kotlin.math.max

/**
 * Min size of allocated object including header.
 * Should be able to fit `BlackHole` or forward.
 */
// pub(crate) const MIN_ALLOC: AlignedSize
internal val MIN_ALLOC: AlignedSize = run {
    fun maxAligned(a: AlignedSize, b: AlignedSize): AlignedSize {
        return if (a.bytes() > b.bytes()) a else b
    }

    // Kotlin: we don't have `mem::size_of`, but we know the forward node is 2 words.
    val forward = AlignedSize.of(2 * AValueHeader.ALIGN)
    // Kotlin: BlackHole is at least 2 words as well (header + size word).
    val blackHole = AlignedSize.of(2 * AValueHeader.ALIGN)
    maxAligned(forward, blackHole)
}

/**
 * Build an [AValueVTable] from a [StarlarkValue] instance.
 *
 * Rust uses a single static vtable per type and stores the payload in arena memory.
 * Kotlin does not expose raw arena layout, so we store the payload inside the vtable
 * (so `AValueHeader.payloadPtr()` can find it).
 */
private fun vtableForValue(value: StarlarkValue): AValueVTable {
    val typeId = ConstTypeId.of(value::class)
    return AValueVTable(
        staticTypeOfValue = typeId,
        starlarkTypeId = StarlarkTypeId.fromTypeId(typeId),
        typeName = value.TYPE,
        isStr = value is StarlarkStr,
        memorySizeFn = { _ -> ValueAllocSize.new(MIN_ALLOC) },
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

/** Construct a BlackHole vtable holding the given [blackHole] instance. */
private fun vtableForBlackHole(blackHole: BlackHole): AValueVTable {
    return AValueVTable(
        staticTypeOfValue = ConstTypeId.of(BlackHole::class),
        starlarkTypeId = StarlarkTypeId.fromTypeId(ConstTypeId.of(BlackHole::class)),
        typeName = "BlackHole",
        isStr = false,
        memorySizeFn = { p ->
            val bh = p.valueRef<BlackHole>()
            bh.size
        },
        heapFreezeFn = { _, _ -> error("BlackHole") },
        heapCopyFn = { _, _ -> error("BlackHole") },
        starlarkValue = blackHole,
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
        header.vtable = vtableForValue(x)
        AValueRepr(header = header, payload = x)
        list[index] = AValueOrForward.Header(header)
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

// enum ArenaVisitEvent<'a>
internal sealed class ArenaVisitEvent {
    data object EnterBump : ArenaVisitEvent()
    data class Value(val value: AValueOrForward) : ArenaVisitEvent()
}

/** Iterate over chunk contents. */
internal class ChunkIter(
    private val chunk: List<AValueOrForward>,
) : Iterator<AValueOrForward> {
    private var index: Int = 0

    override fun hasNext(): Boolean = index < chunk.size

    // fn next(&mut self) -> Option<&'c AValueOrForward>
    override fun next(): AValueOrForward {
        if (!hasNext()) throw NoSuchElementException()
        val v = chunk[index]
        index += 1
        return v
    }
}

/** Result of allocation. Both fields are uninitialized. */
internal class ArenaUninit<T : AValue>(
    private val bumpItems: MutableList<AValueOrForward>,
    private val header: AValueHeader,
    private val extra: Array<Any?>,
) {
    // pub(crate) unsafe fn write_black_hole(self, extra_len: usize) -> (Reservation<'v, T>, *mut [MaybeUninit<T::ExtraElem>])
    fun writeBlackHole(
        extraLen: Int,
    ): Pair<Reservation<T>, Array<Any?>> {
        val index = bumpItems.size
        AValueRepr(header = header, payload = header.payload<StarlarkValue>())
        bumpItems.add(AValueOrForward.Header(header))
        return Pair(Reservation(bumpItems, index, header), extra)
    }

    // pub(crate) fn debug_assert_extra_is_empty(&self)
    fun debugAssertExtraIsEmpty() {
        check(extra.isEmpty())
    }

    // pub(crate) fn write(self, x: T::StarlarkValue) -> (*mut AValueRepr<AValueImpl<'v, T>>, *mut [MaybeUninit<T::ExtraElem>])
    fun write(x: StarlarkValue): Pair<AValueRepr<StarlarkValue>, Array<Any?>> {
        header.vtable = vtableForValue(x)
        val repr = AValueRepr(header = header, payload = x)
        bumpItems.add(AValueOrForward.Header(header))
        return Pair(repr, extra)
    }

    fun writeWithExistingVtable(x: StarlarkValue): Pair<AValueRepr<StarlarkValue>, Array<Any?>> {
        val repr = AValueRepr(header = header, payload = x)
        bumpItems.add(AValueOrForward.Header(header))
        return Pair(repr, extra)
    }

    // pub(crate) fn write_no_extra(self, x: T::StarlarkValue) -> *mut AValueRepr<AValueImpl<'v, T>>
    fun writeNoExtra(x: StarlarkValue): AValueRepr<StarlarkValue> {
        debugAssertExtraIsEmpty()
        return write(x).first
    }

    fun writeNoExtraWithExistingVtable(x: StarlarkValue): AValueRepr<StarlarkValue> {
        debugAssertExtraIsEmpty()
        return writeWithExistingVtable(x).first
    }
}

// #[derive(Default)]
// pub(crate) struct Arena<A: ArenaAllocator>
internal class Arena {
    /** Arena for things which don't need dropping (e.g. strings). */
    // non_drop: A,
    private val nonDrop: ArenaAllocator = ChunkAllocator()
    private val nonDropItems: MutableList<AValueOrForward> = mutableListOf()
    /** Arena for things which might need dropping (e.g. Vec, with memory on heap). */
    // drop: A,
    private val drop: ArenaAllocator = ChunkAllocator()
    private val dropItems: MutableList<AValueOrForward> = mutableListOf()

    // fn alloc_uninit<'v, 'v2, T: AValue<'v2>>(bump: &'v A, extra_len: usize) -> ArenaUninit<'v2, T>
    private fun <T : AValue> allocUninit(
        bump: ArenaAllocator,
        bumpItems: MutableList<AValueOrForward>,
        header: AValueHeader,
        extraLen: Int,
    ): ArenaUninit<T> {
        // Kotlin: we allocate objects directly, but still charge allocation size to the bump.
        val size = header.allocSize()
        bump.alloc(size)
        return ArenaUninit(
            bumpItems = bumpItems,
            header = header,
            extra = Array(extraLen) { null },
        )
    }

    // fn bump_for_type<'v, T: AValue<'v>>(&self) -> &A
    private fun bumpForType(value: StarlarkValue): Pair<ArenaAllocator, MutableList<AValueOrForward>> {
        return if (value is StarlarkStr) {
            Pair(nonDrop, nonDropItems)
        } else {
            Pair(drop, dropItems)
        }
    }

    // pub(crate) fn is_empty(&self) -> bool
    fun isEmpty(): Boolean {
        return allocatedBytes() == 0
    }

    /** Number of allocated bytes plus padding size. */
    // pub(crate) fn allocated_bytes(&self) -> usize
    fun allocatedBytes(): Int {
        // Like Rust bumpalo, we may overestimate by including padding.
        return drop.allocatedBytes() + nonDrop.allocatedBytes()
    }

    // pub(crate) fn available_bytes(&self) -> usize
    fun availableBytes(): Int {
        return drop.remainingCapacity() + nonDrop.remainingCapacity()
    }

    /** Don't forget to call this function to release memory. */
    // pub(crate) fn finish(&mut self)
    fun finish() {
        drop.finish()
        nonDrop.finish()
        dropItems.clear()
        nonDropItems.clear()
    }

    // pub(crate) fn reserve_with_extra<'v2, T: AValue<'v2>>(&self, extra_len: usize) -> (Reservation, extra)
    fun <T : AValue> reserveWithExtra(extraLen: Int): Reservation<T> {
        // Rust writes a one-word BlackHole so it can safely iterate even if the reservation is unfilled.
        // Kotlin simulates it by allocating a header whose payload is a BlackHole carrying the alloc size.
        val blackHoleSize = ValueAllocSize.new(MIN_ALLOC)
        val blackHole = BlackHole(blackHoleSize)
        val header = AValueHeader.new(vtableForBlackHole(blackHole))
        val arenaUninit = allocUninit<T>(drop, dropItems, header, extraLen)
        // If we don't have a vtable we can't skip over missing elements to drop,
        // so very important to put in a current vtable.
        val (reservation, _) = arenaUninit.writeBlackHole(extraLen)
        return reservation
    }

    /** Allocate a type `T`. */
    // pub(crate) fn alloc<'v, 'v2, T: AValue<'v2>>(&'v self, x: AValueImpl<'v2, T>) -> &'v AValueRepr
    fun <T : AValue> alloc(x: AValueImpl<T>): AValueRepr<StarlarkValue> {
        val header = AValueHeader.new(vtableForValue(x.value))
        val (bump, bumpItems) = bumpForType(x.value)
        val arenaUninit = allocUninit<T>(bump, bumpItems, header, 0)
        return arenaUninit.writeNoExtra(x.value)
    }

    /** Allocate a type `T` plus `extra` bytes. */
    // pub(crate) fn alloc_extra<'v, T: AValue<'v>>(&self, x: AValueImpl) -> (repr, extra)
    fun <T : AValue> allocExtra(x: AValueImpl<T>): AValueRepr<StarlarkValue> {
        val header = AValueHeader.new(vtableForValue(x.value))
        val (bump, bumpItems) = bumpForType(x.value)
        val arenaUninit = allocUninit<T>(bump, bumpItems, header, 0)
        return arenaUninit.writeNoExtra(x.value)
    }

    // pub(crate) fn alloc_str_init(&self, len: usize, hash: StarlarkHashValue, init: impl FnOnce(*mut u8)) -> *mut AValueHeader
    fun allocStrInit(
        len: Int,
        @Suppress("UNUSED_PARAMETER") hash: StarlarkHashValue,
        init: (ByteArray) -> Unit,
    ): AValueHeader {
        require(len > 1)
        val bytes = ByteArray(len)
        init(bytes)
        return allocStr(bytes.decodeToString())
    }

    // pub(crate) fn alloc_str(&self, x: &str) -> *mut AValueHeader
    fun allocStr(x: String): AValueHeader {
        // Kotlin: we don't store bytes inline in the arena, but still charge the size.
        val str = StarlarkStr(x)
        val typeId = ConstTypeId.of<StarlarkStr>()
        val header = AValueHeader.new(
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

        val arenaUninit = allocUninit<AValue>(nonDrop, nonDropItems, header, 0)
        arenaUninit.debugAssertExtraIsEmpty()
        arenaUninit.writeNoExtraWithExistingVtable(str)
        return header
    }

    // fn iter_chunk<'a>(chunk: &'a [MaybeUninit<u8>]) -> ChunkIter<'a>
    private fun iterChunk(chunk: List<AValueOrForward>): ChunkIter {
        return ChunkIter(chunk)
    }

    // fn for_each_ordered<'a>(&'a mut self, f: impl FnMut(ArenaVisitEvent<'a>))
    internal fun forEachOrdered(f: (ArenaVisitEvent) -> Unit) {
        for (bump in listOf(dropItems, nonDropItems)) {
            f(ArenaVisitEvent.EnterBump)
            for (x in bump) {
                f(ArenaVisitEvent.Value(x))
            }
        }
    }

    // pub(crate) unsafe fn visit_arena<'v>(&'v mut self, heap_kind, forward_heap_kind, visitor)
    internal fun visitArena(
        heapKind: HeapKind,
        forwardHeapKind: HeapKind,
        visitor: ArenaVisitor,
    ) {
        fun fixFunction(function: Value, forwardHeapKind0: HeapKind): Value {
            val frozen = function.unpackFrozen()
            if (frozen != null) {
                return frozen.toValue()
            }

            val ptrIndex = function.ptr.unpackPtrOpt() ?: return function
            val header = AValueHeader.fromIndex(ptrIndex)
            val orForward = AValueOrForward.Header(header)
            return when (val u = orForward.unpack()) {
                is AValueOrForwardUnpack.Header -> function
                is AValueOrForwardUnpack.Forward -> u.forward.forwardPtr().unpackValue(forwardHeapKind0)
            }
        }

        forEachOrdered { x ->
            when (x) {
                is ArenaVisitEvent.EnterBump -> visitor.enterBump()
                is ArenaVisitEvent.Value -> {
                    when (val u = x.value.unpack()) {
                        is AValueOrForwardUnpack.Forward -> visitor.regularValue(x.value)
                        is AValueOrForwardUnpack.Header -> {
                            val value = u.header.unpackValue(heapKind)
                            val callEnterNeedsDrop: CallEnter<NeedsDrop>? = value.downcastRef()
                            val callEnterNoDrop: CallEnter<NoDrop>? = value.downcastRef()
                            val callExitNeedsDrop: CallExit<NeedsDrop>? = value.downcastRef()
                            val callExitNoDrop: CallExit<NoDrop>? = value.downcastRef()

                            when {
                                callEnterNeedsDrop != null ->
                                    visitor.callEnter(
                                        fixFunction(callEnterNeedsDrop.function, forwardHeapKind),
                                        callEnterNeedsDrop.time,
                                    )
                                callEnterNoDrop != null ->
                                    visitor.callEnter(
                                        fixFunction(callEnterNoDrop.function, forwardHeapKind),
                                        callEnterNoDrop.time,
                                    )
                                callExitNeedsDrop != null -> visitor.callExit(callExitNeedsDrop.time)
                                callExitNoDrop != null -> visitor.callExit(callExitNoDrop.time)
                                else -> visitor.regularValue(x.value)
                            }
                        }
                    }
                }
            }
        }
    }

    // pub(crate) fn for_each_drop_unordered<'a>(&'a mut self, f: impl FnMut(&'a AValueHeader))
    fun forEachDropUnordered(f: (AValueHeader) -> Unit) {
        for (x in iterChunk(dropItems)) {
            val header = x.unpackHeader()
            if (header != null) f(header)
        }
    }

    // fn for_each_unordered_in_bump<'a>(bump: &'a A, f: impl FnMut(&'a AValueHeader))
    private fun forEachUnorderedInBump(
        bump: List<AValueOrForward>,
        f: (AValueHeader) -> Unit,
    ) {
        for (x in iterChunk(bump)) {
            val header = x.unpackHeader()
            if (header != null) f(header)
        }
    }

    // fn for_each_unordered<'a>(&'a self, f: impl FnMut(&'a AValueHeader))
    fun forEachUnordered(f: (AValueHeader) -> Unit) {
        forEachUnorderedInBump(dropItems, f)
        forEachUnorderedInBump(nonDropItems, f)
    }

    // pub(crate) fn allocated_summary(&self) -> HeapSummary
    fun allocatedSummary(): HeapSummary {
        // First, count by header (faster, mirrors Rust).
        val entries: MutableMap<AValueHeader, Pair<String, AllocCounts>> = mutableMapOf()
        forEachUnordered { header ->
            val v = header.unpack()
            val existing = entries[header]
            if (existing == null) {
                val counts = AllocCounts()
                counts.count += 1
                counts.bytes += header.allocSize().bytes().toInt()
                entries[header] = Pair(v.vtable().typeName, counts)
            } else {
                existing.second.count += 1
                existing.second.bytes += header.allocSize().bytes().toInt()
            }
        }

        // Then, collapse by type name.
        val summary = SmallMap<String, AllocCounts>()
        for ((_, pair) in entries) {
            val (name, counts) = pair
            val out = summary.entry(name).orInsertWith { AllocCounts() }
            out.count += counts.count
            out.bytes += counts.bytes
        }

        return HeapSummary(summary = summary)
    }

    override fun toString(): String {
        return "Arena(drop=${dropItems.size}, non_drop=${nonDropItems.size})"
    }
}
