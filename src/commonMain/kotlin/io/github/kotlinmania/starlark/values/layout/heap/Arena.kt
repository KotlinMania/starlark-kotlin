// port-lint: source src/values/layout/heap/arena.rs
package io.github.kotlinmania.starlark.values.layout.heap.arena

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
internal val MIN_ALLOC: AlignedSize = run {
    fun maxAligned(a: AlignedSize, b: AlignedSize): AlignedSize {
        return if (a.bytes() > b.bytes()) a else b
    }

    // The forward node is 2 words.
    val forward = AlignedSize.of(2 * AValueHeader.ALIGN)
    // BlackHole is at least 2 words as well (header + size word).
    val blackHole = AlignedSize.of(2 * AValueHeader.ALIGN)
    maxAligned(forward, blackHole)
}

/**
 * Build an [AValueVTable] from a [StarlarkValue] instance.
 *
 * The payload is stored inside the vtable so `AValueHeader.payloadPtr()` can find it.
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
 * Reservation is morally a parameterised reservation, but we treat it as an
 * existential. Tied to the lifetime of the heap.
 */
class Reservation<T : AValue> internal constructor(
    private val list: MutableList<AValueOrForward>,
    private val index: Int,
    private val header: AValueHeader,
) {
    fun fill(x: StarlarkValue) {
        header.vtable = vtableForValue(x)
        AValueRepr(header = header, payload = x)
        list[index] = AValueOrForward.Header(header)
    }

    fun ptr(): AValueHeader {
        return header
    }
}

internal interface ArenaVisitor {
    fun enterBump()
    fun regularValue(value: AValueOrForward)
    fun callEnter(function: Value, time: ProfilerInstant)
    fun callExit(time: ProfilerInstant)
}

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
    fun writeBlackHole(
        extraLen: Int,
    ): Pair<Reservation<T>, Array<Any?>> {
        val index = bumpItems.size
        AValueRepr(header = header, payload = header.payload<StarlarkValue>())
        bumpItems.add(AValueOrForward.Header(header))
        return Pair(Reservation(bumpItems, index, header), extra)
    }

    fun debugAssertExtraIsEmpty() {
        check(extra.isEmpty())
    }

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

    fun writeNoExtra(x: StarlarkValue): AValueRepr<StarlarkValue> {
        debugAssertExtraIsEmpty()
        return write(x).first
    }

    fun writeNoExtraWithExistingVtable(x: StarlarkValue): AValueRepr<StarlarkValue> {
        debugAssertExtraIsEmpty()
        return writeWithExistingVtable(x).first
    }
}

/**
 * A heap storing [AValue] traits. The heap is a sequence of the [AValue] vtable
 * followed by the payload. Every payload must be at least one machine word large
 * (even zero-sized values).
 *
 * Some elements are created using [reserveWithExtra], in which case they point
 * to a [BlackHole] until they are filled in.
 *
 * Some elements can be overwritten (typically during GC) by a forwarding pointer.
 * In those cases the bottom bit is used to tag the slot as a forwarding entry,
 * and the next word records the size of the item it replaced.
 */
internal class Arena {
    /** Arena for things which don't need dropping (e.g. strings). */
    private val nonDrop: ArenaAllocator = ChunkAllocator()
    private val nonDropItems: MutableList<AValueOrForward> = mutableListOf()
    /** Arena for things which might need dropping (e.g. Vec, with memory on heap). */
    private val drop: ArenaAllocator = ChunkAllocator()
    private val dropItems: MutableList<AValueOrForward> = mutableListOf()

    private fun <T : AValue> allocUninit(
        bump: ArenaAllocator,
        bumpItems: MutableList<AValueOrForward>,
        header: AValueHeader,
        extraLen: Int,
    ): ArenaUninit<T> {
        // Allocate objects directly, but still charge allocation size to the bump.
        val size = header.allocSize()
        bump.alloc(size)
        return ArenaUninit(
            bumpItems = bumpItems,
            header = header,
            extra = Array(extraLen) { null },
        )
    }

    private fun bumpForType(value: StarlarkValue): Pair<ArenaAllocator, MutableList<AValueOrForward>> {
        return if (value is StarlarkStr) {
            Pair(nonDrop, nonDropItems)
        } else {
            Pair(drop, dropItems)
        }
    }

    fun isEmpty(): Boolean {
        return allocatedBytes() == 0
    }

    /** Number of allocated bytes plus padding size. */
    fun allocatedBytes(): Int {
        // May overestimate by including padding.
        return drop.allocatedBytes() + nonDrop.allocatedBytes()
    }

    fun availableBytes(): Int {
        return drop.remainingCapacity() + nonDrop.remainingCapacity()
    }

    /** Don't forget to call this function to release memory. */
    fun finish() {
        drop.finish()
        nonDrop.finish()
        dropItems.clear()
        nonDropItems.clear()
    }

    fun <T : AValue> reserveWithExtra(extraLen: Int): Reservation<T> {
        // Write a one-word BlackHole so we can safely iterate even if the reservation is unfilled.
        // We allocate a header whose payload is a BlackHole carrying the alloc size.
        val blackHoleSize = ValueAllocSize.new(MIN_ALLOC)
        val blackHole = BlackHole(blackHoleSize)
        val header = AValueHeader.new(vtableForBlackHole(blackHole))
        val arenaUninit = allocUninit<T>(drop, dropItems, header, extraLen)
        // If we don't have a vtable we can't skip over missing elements to drop,
        // so very important to put in a current vtable.
        val (reservation, _) = arenaUninit.writeBlackHole(extraLen)
        return reservation
    }

    /** Allocate a value of [AValue] type [T]. */
    fun <T : AValue> alloc(x: AValueImpl<T>): AValueRepr<StarlarkValue> {
        val header = AValueHeader.new(vtableForValue(x.value))
        val (bump, bumpItems) = bumpForType(x.value)
        val arenaUninit = allocUninit<T>(bump, bumpItems, header, 0)
        return arenaUninit.writeNoExtra(x.value)
    }

    /** Allocate a value of [AValue] type [T] plus `extra` bytes. */
    fun <T : AValue> allocExtra(x: AValueImpl<T>): AValueRepr<StarlarkValue> {
        val header = AValueHeader.new(vtableForValue(x.value))
        val (bump, bumpItems) = bumpForType(x.value)
        val arenaUninit = allocUninit<T>(bump, bumpItems, header, 0)
        return arenaUninit.writeNoExtra(x.value)
    }

    fun allocStrInit(
        len: Int,
        hash: StarlarkHashValue,
        init: (ByteArray) -> Unit,
    ): AValueHeader {
        require(len > 1)
        val bytes = ByteArray(len)
        init(bytes)
        return allocStr(bytes.decodeToString())
    }

    fun allocStr(x: String): AValueHeader {
        // We don't store bytes inline in the arena, but still charge the size.
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

    private fun iterChunk(chunk: List<AValueOrForward>): ChunkIter {
        return ChunkIter(chunk)
    }

    internal fun forEachOrdered(f: (ArenaVisitEvent) -> Unit) {
        for (bump in listOf(dropItems, nonDropItems)) {
            f(ArenaVisitEvent.EnterBump)
            for (x in bump) {
                f(ArenaVisitEvent.Value(x))
            }
        }
    }

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

    fun forEachDropUnordered(f: (AValueHeader) -> Unit) {
        for (x in iterChunk(dropItems)) {
            val header = x.unpackHeader()
            if (header != null) f(header)
        }
    }

    private fun forEachUnorderedInBump(
        bump: List<AValueOrForward>,
        f: (AValueHeader) -> Unit,
    ) {
        for (x in iterChunk(bump)) {
            val header = x.unpackHeader()
            if (header != null) f(header)
        }
    }

    fun forEachUnordered(f: (AValueHeader) -> Unit) {
        forEachUnorderedInBump(dropItems, f)
        forEachUnorderedInBump(nonDropItems, f)
    }

    fun allocatedSummary(): HeapSummary {
        // First, count by header (faster).
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
