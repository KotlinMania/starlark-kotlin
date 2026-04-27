// port-lint: source src/eval/runtime/profile/timeFlame.rs
package io.github.kotlinmania.starlark.eval.runtime.profile

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

import io.github.kotlinmania.starlark.eval.runtime.profile.data.ProfileData
import io.github.kotlinmania.starlark.eval.runtime.profile.data.ProfileDataImpl
import io.github.kotlinmania.starlark.eval.runtime.profile.flamegraph.FlameGraphData
import io.github.kotlinmania.starlark.eval.runtime.profile.flamegraph.FlameGraphNode
import io.github.kotlinmania.starlark.eval.runtime.SmallDuration
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.RawPointer
import io.github.kotlinmania.starlark.util.ArcStr
import io.github.kotlinmania.starlark.eval.runtime.profile.mode.ProfileMode
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.ValueHolder

internal object TimeFlameProfilerType : ProfilerType<FlameGraphData> {
    override val profileMode: ProfileMode = ProfileMode.TimeFlame

    override fun dataFromGeneric(profileData: ProfileDataImpl): FlameGraphData? =
        (profileData as? ProfileDataImpl.TimeFlameProfile)?.data

    override fun dataToGeneric(data: FlameGraphData): ProfileDataImpl =
        ProfileDataImpl.TimeFlameProfile(data)

    override fun mergeProfilesImpl(profiles: List<FlameGraphData>): Result<FlameGraphData> =
        Result.success(FlameGraphData.merge(profiles))
}

private sealed class FlameProfileError(message: String) : Exception(message) {
    // NotEnabled
    class NotEnabled : FlameProfileError("Flame profile not enabled")
}

internal data class MutableValueId(val value: Int)

internal data class FrozenValueId(val value: Int)

/** Index into FlameData.values */
internal sealed class ValueId {
    // Mutable(MutableValueId)
    data class Mutable(val id: MutableValueId) : ValueId()
    // Frozen(FrozenValueId)
    data class Frozen(val id: FrozenValueId) : ValueId()

    fun <T> lookup(mutable: List<T>, frozen: List<T>): T = when (this) {
        is Mutable -> mutable[id.value]
        is Frozen -> frozen[id.value]
    }
}

/**
 * Bimap between Value and ValueId.
 * In order to optimise GC (which otherwise quickly becomes O(n^2)) we have to
 * dedupe the values, so store them in values, with a fast map to get them in map.
 * Whenever we GC, regenerate map.
 */
private class ValueIndex {
    /** Map from MutableValueId to Value. */
    val mutableValues: MutableList<Value> = mutableListOf()
    /** Map from FrozenValueId to Value. */
    val frozenValues: MutableList<FrozenValue> = mutableListOf()
    /** Map from Value to MutableValueId. */
    val mutableMap: MutableMap<RawPointer, MutableValueId> = mutableMapOf()
    /** Map from Value to FrozenValueId. */
    val frozenMap: MutableMap<RawPointer, FrozenValueId> = mutableMapOf()

    // We only need to trace mutable values.
    fun trace(tracer: Tracer) {
        for (i in mutableValues.indices) {
            val holder = ValueHolder(mutableValues[i])
            tracer.trace(holder)
            mutableValues[i] = holder.value
        }
        // Have to rebuild the map, as its keyed by ValuePtr which changes on GC
        mutableMap.clear()
        for ((i, x) in mutableValues.withIndex()) {
            mutableMap[x.ptrValue()] = MutableValueId(i)
        }
    }

    /** Map Value to ValueId. */
    fun index(value: Value): ValueId {
        val frozen = value.unpackFrozen()
        if (frozen != null) {
            val ptr = frozen.ptrValue()
            val existing = frozenMap[ptr]
            if (existing != null) return ValueId.Frozen(existing)
            val res = FrozenValueId(frozenValues.size)
            frozenValues.add(frozen)
            frozenMap[ptr] = res
            return ValueId.Frozen(res)
        } else {
            val ptr = value.ptrValue()
            val existing = mutableMap[ptr]
            if (existing != null) return ValueId.Mutable(existing)
            val res = MutableValueId(mutableValues.size)
            mutableValues.add(value)
            mutableMap[ptr] = res
            return ValueId.Mutable(res)
        }
    }
}

internal sealed class Frame {
    /** Entry recorded when we enter a function. */
    // Push(ValueId)
    data class Push(val id: ValueId) : Frame()
    /** Entry recorded when we exit a function. */
    // Pop
    data object Pop : Frame()
}

internal class TimeFlameProfile {
    /** `non-null` means enabled. */
    private var data: FlameData? = null

    companion object {
        fun new(): TimeFlameProfile = TimeFlameProfile()

        private fun genProfile(x: FlameData): ProfileData {
            val mutableNames = x.index.mutableValues.map { it.toRepr() }
            val frozenNames = x.index.frozenValues.map { it.toValue().toRepr() }
            return ProfileData(
                profile = ProfileDataImpl.TimeFlameProfile(
                    Stacks.new(mutableNames, frozenNames, x.frames).render()
                )
            )
        }
    }

    fun enable() {
        data = FlameData()
    }

    fun recordCallEnter(function: Value) {
        val x = data ?: return
        val ind = x.index.index(function)
        x.frames.add(Pair(Frame.Push(ind), ProfilerInstant.now()))
    }

    fun recordCallExit() {
        val x = data ?: return
        x.frames.add(Pair(Frame.Pop, ProfilerInstant.now()))
    }

    fun gen(): ProfileData {
        val x = data ?: throw io.github.kotlinmania.starlark.Error.newOther(FlameProfileError.NotEnabled())
        return genProfile(x)
    }

    // FlameData contains ValueIndex which holds mutable Value references
    // that must be updated during GC.
    fun trace(tracer: Tracer) {
        val x = data ?: return
        x.index.trace(tracer)
    }
}

private class FlameData {
    /** All events in the profile, i.e. function entry or exit with timestamp. */
    val frames: MutableList<Pair<Frame, ProfilerInstant>> = mutableListOf()
    val index: ValueIndex = ValueIndex()
}

private class Stacks(
    val name: String,
    var time: SmallDuration = SmallDuration.default(),
    val children: MutableMap<ValueId, Stacks> = mutableMapOf(),
) {
    companion object {
        fun blank(name: String): Stacks = Stacks(name)

        fun new(
            mutableNames: List<String>,
            frozenNames: List<String>,
            frames: List<Pair<Frame, ProfilerInstant>>,
        ): Stacks {
            val res = blank("root")
            if (frames.isEmpty()) return res
            val iter = frames.iterator()
            val lastTime = arrayOf(frames.first().second)
            res.add(mutableNames, frozenNames, iter, lastTime)
            return res
        }
    }

    fun add(
        mutableNames: List<String>,
        frozenNames: List<String>,
        frames: Iterator<Pair<Frame, ProfilerInstant>>,
        lastTime: Array<ProfilerInstant>,
    ) {
        while (frames.hasNext()) {
            val (frame, time) = frames.next()
            this.time += time.durationSince(lastTime[0])
            lastTime[0] = time
            when (frame) {
                is Frame.Pop -> return
                is Frame.Push -> {
                    val child = children.getOrPut(frame.id) {
                        blank(frame.id.lookup(mutableNames, frozenNames))
                    }
                    child.add(mutableNames, frozenNames, frames, lastTime)
                }
            }
        }
    }

    fun renderWithBuffer(node: FlameGraphNode) {
        val childNode = node.child(ArcStr.from(name))
        val count = time.toDuration().inWholeMilliseconds
        if (count > 0) {
            childNode.add(count.toULong())
        }
        for (x in children.values) {
            x.renderWithBuffer(childNode)
        }
    }

    fun render(): FlameGraphData {
        val data = FlameGraphData()
        renderWithBuffer(data.root())
        return data
    }
}
