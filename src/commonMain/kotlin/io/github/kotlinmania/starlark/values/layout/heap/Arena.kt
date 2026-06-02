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

import io.github.kotlinmania.starlark.eval.runtime.profile.ProfilerInstant
import io.github.kotlinmania.starlark.values.ComplexValue
import io.github.kotlinmania.starlark.values.Freeze
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.AValue
import io.github.kotlinmania.starlark.values.layout.AValueImpl
import io.github.kotlinmania.starlark.values.layout.AValueVTable
import io.github.kotlinmania.starlark.values.layout.AlignedSize
import io.github.kotlinmania.starlark.values.layout.BlackHole
import io.github.kotlinmania.starlark.values.layout.ConstTypeId
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.ValueAllocSize
import io.github.kotlinmania.starlark.values.layout.avalues.AValueComplex
import io.github.kotlinmania.starlark.values.layout.avalues.AValueComplexNoFreeze
import io.github.kotlinmania.starlark.values.layout.avalues.AValueList
import io.github.kotlinmania.starlark.values.layout.avalues.AValueTuple
import io.github.kotlinmania.starlark.values.layout.heap.AValueHeader
import io.github.kotlinmania.starlark.values.layout.heap.AValueOrForward
import io.github.kotlinmania.starlark.values.layout.heap.AValueOrForwardUnpack
import io.github.kotlinmania.starlark.values.layout.heap.AValueRepr
import io.github.kotlinmania.starlark.values.layout.heap.CallEnter
import io.github.kotlinmania.starlark.values.layout.heap.CallExit
import io.github.kotlinmania.starlark.values.layout.heap.ForwardPtr
import io.github.kotlinmania.starlark.values.layout.heap.HeapKind
import io.github.kotlinmania.starlark.values.layout.heap.profile.HeapSummary
import io.github.kotlinmania.starlark.values.layout.heap.profile.SmallMap
import io.github.kotlinmania.starlark.values.layout.heap.profile.alloccounts.AllocCounts
import io.github.kotlinmania.starlark.values.layout.heapCopyImpl
import io.github.kotlinmania.starlark.values.layout.totalMemoryForProfile
import io.github.kotlinmania.starlark.values.layout.tryFreezeDirectly
import io.github.kotlinmania.starlark.values.layout.typed.StarlarkStr
import io.github.kotlinmania.starlark.values.starlarktypeid.StarlarkTypeId
import io.github.kotlinmania.starlark.values.types.StarlarkAny
import io.github.kotlinmania.starlark.values.types.anycomplex.StarlarkAnyComplex
import io.github.kotlinmania.starlark.values.types.list.ListGen
import io.github.kotlinmania.starlark.values.types.tuple.TupleGen

/**
 * Min size of allocated object including header.
 * Should be able to fit `BlackHole` or forward.
 */
internal val MIN_ALLOC: AlignedSize = AlignedSize.newBytes(16)

private fun createAValueComplex(value: StarlarkValue): AValue = AValueComplex(value)

/**
 * Build an [AValueVTable] from a [StarlarkValue] instance.
 */
private fun vtableForValue(
    value: StarlarkValue,
    avalue: AValue? = null,
): AValueVTable {
    val typeId = ConstTypeId.of(value::class)
    val resolvedAvalue =
        avalue ?: when (value) {
            is ListGen<*> -> AValueList
            is TupleGen<*> -> AValueTuple
            is ComplexValue -> {
                if (value is Freeze<*>) {
                    createAValueComplex(value)
                } else {
                    AValueComplexNoFreeze(value)
                }
            }
            else -> null
        }
    return AValueVTable(
        staticTypeOfValue = typeId,
        starlarkTypeId = StarlarkTypeId.fromTypeId(typeId),
        typeName = value.TYPE,
        isStr = value is StarlarkStr,
        memorySizeFn = { _ -> ValueAllocSize.new(AlignedSize.newBytes(16)) },
        heapFreezeFn = freeze@{ repr, p, freezer ->
            if (resolvedAvalue != null) {
                return@freeze resolvedAvalue.heapFreeze(repr, freezer)
            }
            val sv = p.starlarkValue()
            val direct = tryFreezeDirectly(sv, freezer)
            if (direct != null) {
                if (direct.isSuccess) {
                    AValueHeader.overwriteWithForward(repr, ForwardPtr.newFrozen(direct.getOrThrow()))
                }
                return@freeze direct
            }

            val (fv, r) = freezer.reserve<AValue>()
            val x = AValueHeader.overwriteWithForward(repr, ForwardPtr.newFrozen(fv))
            r.fill(x)
            Result.success(fv)
        },
<<<<<<< HEAD
        heapCopyFn = { repr, p, tracer ->
            if (resolvedAvalue != null) {
                resolvedAvalue.heapCopy(repr, tracer)
            } else {
                val sv = p.starlarkValue()
                heapCopyImpl(repr, sv, tracer) { _, _ -> }
=======
        heapCopyFn = { p, tracer ->
            if (avalue != null) {
                avalue.heapCopy(tracer)
            } else {
                val sv = p.starlarkValue()
                heapCopyImpl(sv, tracer) { _, _ -> }
>>>>>>> origin/main
            }
        },
        starlarkValue = value,
        hasInvoke = value.HAS_invoke,
        hasEvalType = value.HAS_eval_type,
        hasIterate = value.HAS_iterate,
        hasEquals = value.HAS_equals,
    )
}

/**
 * Reservation tied to the lifetime of the heap.
 */
class Reservation<T : AValue> internal constructor(
    private val arena: Arena,
    private val list: MutableList<AValueOrForward>,
    private val index: Int,
    private val header: AValueHeader,
) {
    fun fill(x: StarlarkValue, vtable: AValueVTable? = null) {
        val oldBytes = header.allocSize().bytes().toInt()
        header.vtable = vtable ?: vtableForValue(x)
        AValueRepr(header = header, payload = x).also {
            require(it.header == header)
        }
        val newBytes = header.allocSize().bytes().toInt()
        arena.allocatedBytes += (newBytes - oldBytes)
    }

    fun ptr(): AValueHeader = header
}

internal interface ArenaVisitor {
    fun enterBump()

    fun regularValue(value: AValueOrForward)

    fun callEnter(function: Value, time: ProfilerInstant)

    fun callExit(time: ProfilerInstant)
}

private sealed class ArenaVisitEvent {
    data object EnterBump : ArenaVisitEvent()

    class Value(
        val value: AValueOrForward,
    ) : ArenaVisitEvent()
}

// Kotlin: GC handles memory. Arena is a simple list of allocated values.
internal class Arena {
    /** Arena for things which don't need dropping (e.g. strings). */
    // non_drop: A,
    private val nonDrop: MutableList<AValueOrForward> = mutableListOf()

    /** Arena for things which might need dropping (e.g. Vec, with memory on heap). */
    // drop: A,
    private val drop: MutableList<AValueOrForward> = mutableListOf()

    internal var allocatedBytes: Int = 0

    fun isEmpty(): Boolean = allocatedBytes() == 0

    fun dropSize(): Int = drop.size

    /** Number of allocated bytes plus padding size. */
    fun allocatedBytes(): Int = allocatedBytes

    fun availableBytes(): Int = Int.MAX_VALUE

    /** Release all arena entries. */
    fun finish() {
        for (entry in drop) {
            val unpacked = entry.unpack()
            if (unpacked is AValueOrForwardUnpack.Header) {
                val starlarkVal = unpacked.header.vtable.starlarkValue
                if (starlarkVal is AutoCloseable) {
                    try {
                        starlarkVal.close()
                    } catch (e: Throwable) {
                        // Ignore
                    }
                }
                if (starlarkVal is StarlarkAny<*>) {
                    val inner = starlarkVal.inner
                    if (inner is AutoCloseable) {
                        try {
                            inner.close()
                        } catch (e: Throwable) {
                            // Ignore
                        }
                    }
                }
                if (starlarkVal is StarlarkAnyComplex<*>) {
                    val inner = starlarkVal.value
                    if (inner is AutoCloseable) {
                        try {
                            inner.close()
                        } catch (e: Throwable) {
                            // Ignore
                        }
                    }
                }
            }
        }
        drop.clear()
        nonDrop.clear()
        allocatedBytes = 0
    }

    fun <T : AValue> reserveWithExtra(extraLen: Int): Reservation<T> {
        val blackHole = BlackHole(ValueAllocSize(AlignedSize(extraLen.toUInt())))
        val blackHoleHeader = AValueHeader(AValueVTable.newBlackHole(blackHole))
        val entry = AValueOrForward.Header(blackHoleHeader)
        val index = drop.size
        drop.add(entry)
        allocatedBytes += entry.allocSize().bytes().toInt()
        return Reservation(this, drop, index, blackHoleHeader)
    }

    /** Allocate a type `T`. */
    fun <T : AValue> alloc(x: AValueImpl<T>): AValueRepr<StarlarkValue> {
        val header = AValueHeader(vtableForValue(x.value, x.avalue))
        val repr =
            AValueRepr(
                header = header,
                payload = x.value,
            )
        val entry = AValueOrForward.Header(repr.header)
        drop.add(entry)
        allocatedBytes += entry.allocSize().bytes().toInt()
        return repr
    }

    /** Allocate a type `T` into the arena for values without finalization. */
    fun <T : AValue> allocNoDrop(x: AValueImpl<T>): AValueRepr<StarlarkValue> {
        val header = AValueHeader(vtableForValue(x.value, x.avalue))
        val repr =
            AValueRepr(
                header = header,
                payload = x.value,
            )
        val entry = AValueOrForward.Header(repr.header)
        nonDrop.add(entry)
        allocatedBytes += entry.allocSize().bytes().toInt()
        return repr
    }

    /** Allocate a type `T` plus `extra` bytes. */
    fun <T : AValue> allocExtra(x: AValueImpl<T>): AValueRepr<StarlarkValue> = alloc(x)

    fun allocStrInit(len: Int, init: (Int) -> String): AValueHeader {
        require(len > 1)
        val content = init(len)
        return allocStr(content)
    }

    fun allocStr(x: String): AValueHeader {
        val str = StarlarkStr(x)
        val typeId = ConstTypeId.of<StarlarkStr>()
        val header =
            AValueHeader(
                AValueVTable(
                    staticTypeOfValue = typeId,
                    starlarkTypeId = StarlarkTypeId.fromTypeId(typeId),
                    typeName = "string",
                    isStr = true,
                    memorySizeFn = { _ ->
                        val byteLen = str.len()
                        ValueAllocSize.new(
                            maxOf(
                                AlignedSize.alignUp(StarlarkStr.offsetOfContent() + byteLen),
                                MIN_ALLOC,
                            ),
                        )
                    },
                    heapFreezeFn = { repr, _, freezer ->
                        val fv = freezer.frozenHeap().allocStrIntern(str.asStr())
                        AValueHeader.overwriteWithForward(repr, ForwardPtr.newFrozen(fv.toFrozenValue()))
                        Result.success(fv.toFrozenValue())
                    },
                    heapCopyFn = { _, _, tracer ->
                        tracer.allocStr(str.asStr())
                    },
                    starlarkValue = str,
                    hasEquals = true,
                ),
            )
        AValueRepr(header = header, payload = str)
        val entry = AValueOrForward.Header(header)
        nonDrop.add(entry)
        allocatedBytes += entry.allocSize().bytes().toInt()
        return header
    }

    /** Iterate over heap values in insertion order. */
    private fun forEachOrdered(f: (ArenaVisitEvent) -> Unit) {
        for (bump in listOf(drop, nonDrop)) {
            f(ArenaVisitEvent.EnterBump)
            for (entry in bump) {
                f(ArenaVisitEvent.Value(entry))
            }
        }
    }

    internal fun visitArena(
        heapKind: HeapKind,
        forwardHeapKind: HeapKind,
        visitor: ArenaVisitor,
    ) {
        fun fixFunction(
            function: Value,
            @Suppress("UNUSED_PARAMETER") forwardHeapKind: HeapKind,
        ): Value {
            val frozen = function.unpackFrozen()
            if (frozen != null) {
                return frozen.toValue()
            }
            return function
        }

        forEachOrdered { event ->
            when (event) {
                is ArenaVisitEvent.EnterBump -> visitor.enterBump()
                is ArenaVisitEvent.Value -> {
                    val x = event.value
                    when (val unpacked = x.unpack()) {
                        is AValueOrForwardUnpack.Header -> {
                            val header = unpacked.header
                            val value = header.unpackValue(heapKind)
                            val starlarkVal = header.vtable.starlarkValue
                            if (starlarkVal is CallEnter<*>) {
                                visitor.callEnter(
                                    fixFunction(starlarkVal.function, forwardHeapKind),
                                    starlarkVal.time,
                                )
                            } else if (starlarkVal is CallExit<*>) {
                                visitor.callExit(starlarkVal.time)
                            } else {
                                visitor.regularValue(x)
                            }
                        }
                        is AValueOrForwardUnpack.Forward -> visitor.regularValue(x)
                    }
                }
            }
        }
    }

    fun forEachDropUnordered(f: (AValueHeader) -> Unit) {
        for (entry in drop) {
            when (entry) {
                is AValueOrForward.Header -> f(entry.header)
                is AValueOrForward.Forward -> {}
            }
        }
    }

    private fun forEachUnorderedInBump(bump: List<AValueOrForward>, f: (AValueHeader) -> Unit) {
        for (entry in bump) {
            when (entry) {
                is AValueOrForward.Header -> f(entry.header)
                is AValueOrForward.Forward -> {}
            }
        }
    }

    // Iterate over the values in the both bumps in any order
    private fun forEachUnordered(f: (AValueHeader) -> Unit) {
        for (bump in listOf(drop, nonDrop)) {
            forEachUnorderedInBump(bump, f)
        }
    }

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

    override fun toString(): String = "Arena(drop=${drop.size}, non_drop=${nonDrop.size})"
}
