<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/values/layout/heap/profile/Aggregated.kt
// port-lint: source values/layout/heap/profile/aggregated.rs
package io.github.kotlinmania.starlark.values.layout.heap.profile
=======
// port-lint: source src/values/layout/heap/profile/aggregated.rs
package io.github.kotlinmania.starlark_kotlin.values.layout.heap.profile

import io.github.kotlinmania.starlark_kotlin.values.layout.heap.profile.alloc_counts.AllocCounts
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.data.ProfileData
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.data.ProfileDataImpl
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.ProfilerInstant
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.profile.string_index.StringId
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.profile.string_index.StringIndex
import io.github.kotlinmania.starlark_kotlin.eval.runtime.SmallDuration
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.HeapKind
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.heap.RetainedHeapProfileMode
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.AValueOrForwardUnpack
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.AValueOrForward
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.arena.ArenaVisitor
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.flamegraph.FlameGraphData
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.flamegraph.FlameGraphNode
import io.github.kotlinmania.starlark_kotlin.util.ArcStr
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.RawPointer
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/values/layout/heap/profile/Aggregated.kt


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

// Real types should be imported from their respective packages:
// ProfileData, ProfileDataImpl → eval.runtime.profile.data
// FlameGraphData, FlameGraphNode → eval.runtime.profile.flamegraph
// HeapKind → values.layout.heap
// SmallDuration → eval.runtime
// Value → values.layout
// Heap → values.layout.heap
// StringId, StringIndex → values.layout.heap.profile.string_index
class SmallMap<K, V> {
    private val map: MutableMap<K, V> = mutableMapOf()
    fun entry(key: K): SmallMapEntry<K, V> = SmallMapEntry(map, key)
    fun iter(): Iterator<Pair<K, V>> = map.entries.map { Pair(it.key, it.value) }.iterator()
    operator fun iterator(): Iterator<Pair<K, V>> = iter()
    fun len(): Int = map.size
    fun values(): Collection<V> = map.values
    fun isEmpty(): Boolean = map.isEmpty()
    companion object {
        fun <K, V> new(): SmallMap<K, V> = SmallMap()
    }
}
class SmallMapEntry<K, V>(private val map: MutableMap<K, V>, private val key: K) {
    fun orInsertWith(factory: () -> V): V {
        return map.getOrPut(key, factory)
    }
    fun orDefault(default: () -> V): V {
        return map.getOrPut(key, default)
    }
}

/** A mapping from function Value to FunctionId, which must be continuous */
private class FunctionIds(
    val values: MutableMap<RawPointer, StringId> = mutableMapOf(),
    val strings: StringIndex = StringIndex(),
) {
    fun getValue(x: Value): StringId {
        val ptr = x.ptrValue()
        val existing = values[ptr]
        if (existing != null) return existing
        val functionId = strings.index(x.toStr())
        values[ptr] = functionId
        return functionId
    }
}

/** A stack frame, its caller and the functions it called, and the allocations it made itself. */
private class StackFrameData(
    val callees: SmallMap<StringId, StackFrameBuilder> = SmallMap.new(),
    var allocs: HeapSummary = HeapSummary(),
    /**
     * Time spent in this frame excluding callees.
     * Double, because enter/exit are recorded twice, in drop and non-drop heaps.
     */
    var timeX2: SmallDuration = SmallDuration.default(),
    /**
     * How many times this function was called (with this stack).
     * Double.
     */
    var callsX2: Int = 0,
)

private class StackFrameBuilder(
    val data: StackFrameData = StackFrameData(),
) {
    /** Enter a new stack frame. */
    fun push(function: StringId): StackFrameBuilder {
        return data.callees.entry(function).orInsertWith { StackFrameBuilder() }
    }

    fun build(): StackFrame {
        val callees = SmallMap<StringId, StackFrame>()
        for ((f, s) in data.callees) {
            callees.entry(f).orInsertWith { s.build() }
        }
        return StackFrame(
            callees = callees,
            allocs = data.allocs.copy(),
            timeX2 = data.timeX2,
            callsX2 = data.callsX2,
        )
    }
}

/** An accumulator for stack frames that lets us visit the heap. */
internal class StackCollector(
    /**
     * What we are collecting.
     * When unset, we are collecting allocated memory (not retained).
     * When set, must be set to correct heap type (unfrozen or frozen), we are traversing.
     */
    private val retained: HeapKind?,
) : ArenaVisitor {
    /** Timestamp of last call enter or exit. */
    private var lastTime: ProfilerInstant? = null
    private val ids: FunctionIds = FunctionIds()
    private val current: MutableList<StackFrameBuilder> = mutableListOf(StackFrameBuilder())

    override fun enterBump() {
        lastTime = null
    }

    override fun regularValue(value: AValueOrForward) {
        val unpacked = value.unpack()
        val v = when {
            unpacked is AValueOrForwardUnpack.Header && retained == null -> {
                Value.newPtrQueryIsStr(unpacked.header)
            }
            unpacked is AValueOrForwardUnpack.Forward && retained != null -> {
                unpacked.forward.forwardPtr().unpackValue(retained)
            }
            else -> return
        }

        val frame = current.lastOrNull() ?: return

        // Value allocated in this frame, record it!
        val typ = v.vtable().typeName
        frame.data.allocs.add(
            typ,
            AllocCounts(
                count = 1,
                bytes = v.getRef().memorySize().bytes().toLong(),
            ),
        )
    }

    override fun callEnter(function: Value, time: ProfilerInstant) {
        val lt = lastTime
        if (lt != null) {
            current.last().data.timeX2 = current.last().data.timeX2 + time.durationSince(lt)
            current.last().data.callsX2 += 1
        }

        val frame = current.lastOrNull() ?: return

        // New frame, enter it.
        val id = ids.getValue(function)
        val newFrame = frame.push(id)
        current.add(newFrame)

        lastTime = time
    }

    override fun callExit(time: ProfilerInstant) {
        val lt = lastTime
        if (lt != null) {
            current.last().data.timeX2 = current.last().data.timeX2 + time.durationSince(lt)
        }
        current.removeAt(current.lastIndex)
        lastTime = time
    }

    fun finish(): AggregateHeapProfileInfo {
        check(current.size == 1)
        return AggregateHeapProfileInfo(
            strings = ids.strings,
            root = current.removeAt(current.lastIndex).build(),
        )
    }
}

/** Aggregated stack frame data. */
internal class StackFrame(
    /** Aggregated callees. */
    val callees: SmallMap<StringId, StackFrame> = SmallMap.new(),
    /** Aggregated allocations in this frame, without callees. */
    val allocs: HeapSummary = HeapSummary(),
    /**
     * Time spend in this frame excluding callees.
     * `x2` because enter/exit are recorded twice, in drop and non-drop heaps.
     */
    val timeX2: SmallDuration = SmallDuration.default(),
    /**
     * How many times this frame was called with the same callers.
     * `x2` because enter/exit are recorded twice, in drop and non-drop heaps.
     */
    val callsX2: Int = 0,
) {
    companion object {
        fun default(): StackFrame = StackFrame()

        fun mergeCallees(
            frames: List<StackFrameWithContext>,
            strings: StringIndex,
        ): SmallMap<StringId, StackFrame> {
            val groupByCallee = mutableMapOf<String, MutableList<StackFrameWithContext>>()
            for (frame in frames) {
                for ((name, callee) in frame.callees()) {
                    groupByCallee.getOrPut(name) { mutableListOf() }.add(callee)
                }
            }
            val result = SmallMap<StringId, StackFrame>()
            for ((name, grouped) in groupByCallee) {
                val nameId = strings.index(name)
                result.entry(nameId).orInsertWith { merge(grouped, strings) }
            }
            return result
        }

        fun merge(
            frames: Iterable<StackFrameWithContext>,
            strings: StringIndex,
        ): StackFrame {
            val framesList = frames.toList()
            val callees = mergeCallees(framesList, strings)
            val allocs = HeapSummary.merge(framesList.map { it.frame.allocs })
            var timeX2 = SmallDuration.default()
            var callsX2 = 0
            for (f in framesList) {
                timeX2 = timeX2 + f.frame.timeX2
                callsX2 += f.frame.callsX2
            }
            return StackFrame(
                callees = callees,
                allocs = allocs,
                timeX2 = timeX2,
                callsX2 = callsX2,
            )
        }
    }

    // #[cfg(test)]
    // pub(crate) fn normalize_for_golden_tests(&mut self)
    internal fun normalizeForGoldenTests() {
        for ((_, v) in callees) {
            v.normalizeForGoldenTests()
        }
        allocs.normalizeForGoldenTests()
    }
}

internal class StackFrameWithContext(
    val frame: StackFrame,
    val strings: StringIndex,
) {
    fun callees(): List<Pair<String, StackFrameWithContext>> {
        val result = mutableListOf<Pair<String, StackFrameWithContext>>()
        for ((id, callee) in frame.callees) {
            result.add(Pair(strings.get(id), StackFrameWithContext(callee, strings)))
        }
        return result
    }

    /** Accumulate this stack frame's data into the given FlameGraphNode */
    fun genFlameGraphData(node: FlameGraphNode) {
        for ((k, v) in frame.allocs.summary) {
            node.child(ArcStr.newStatic(k)).add(v.bytes.toULong())
        }

        for ((id, frameCtx) in callees()) {
            val childNode = node.child(ArcStr.newStatic(id))
            frameCtx.genFlameGraphData(childNode)
        }
    }
}

/**
 * Aggregated heap profiling data when heap profiling is enabled.
 *
 * Can be:
 * * written as CSV or flamegraph
 * * merged with another data
 */
internal class AggregateHeapProfileInfo(
    val strings: StringIndex = StringIndex(),
    val root: StackFrame = StackFrame.default(),
) {
    fun root(): StackFrameWithContext {
        return StackFrameWithContext(
            frame = root,
            strings = strings,
        )
    }

    /** Generate the flame graph data and return it as a string. */
    fun genFlameGraphData(): String {
        val data = FlameGraphData()
        root().genFlameGraphData(data.root())
        return data.write()
    }

    /** Generate per-function summary in CSV format. */
    fun genSummaryCsv(): String {
        return HeapSummaryByFunction.init(this).genCsv()
    }

    fun clone(): AggregateHeapProfileInfo {
        return AggregateHeapProfileInfo(strings, root)
    }

    // #[cfg(test)]
    // pub(crate) fn normalize_for_golden_tests(&mut self)
    internal fun normalizeForGoldenTests() {
        root.normalizeForGoldenTests()
    }

    override fun toString(): String = "AggregateHeapProfileInfo(..)"

    companion object {
        fun default(): AggregateHeapProfileInfo = AggregateHeapProfileInfo()

        fun collect(heap: Heap, retained: HeapKind?): AggregateHeapProfileInfo {
            val collector = StackCollector(retained)
            heap.visitArena(HeapKind.Unfrozen, collector)
            return collector.finish()
        }

        /** Merge aggregated heap profile from multiple sources (e.g. from several runs). */
        fun merge(
            profiles: Iterable<AggregateHeapProfileInfo>,
        ): AggregateHeapProfileInfo {
            val profilesList = profiles.toList()
            val strings = StringIndex()
            val roots = profilesList.map { it.root() }
            val root = StackFrame.merge(roots, strings)
            return AggregateHeapProfileInfo(strings, root)
        }
    }
}

internal class RetainedHeapProfile(
    val info: AggregateHeapProfileInfo,
    val mode: RetainedHeapProfileMode,
) {
    fun toProfile(): ProfileData {
        return ProfileData(
            profile = when (mode) {
                RetainedHeapProfileMode.FlameAndSummary ->
                    ProfileDataImpl.HeapRetained(info.clone())
                RetainedHeapProfileMode.Flame ->
                    ProfileDataImpl.HeapFlameRetained(info.clone())
                RetainedHeapProfileMode.Summary ->
                    ProfileDataImpl.HeapSummaryRetained(info.clone())
                else -> throw IllegalStateException("Unexpected mode: $mode")
            },
        )
    }

    override fun toString(): String = "RetainedHeapProfile(mode=$mode)"
}
