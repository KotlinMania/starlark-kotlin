// port-lint: source src/eval/runtime/profile/time_flame.rs
package io.github.kotlinmania.starlark.eval.runtime.profile

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

import io.github.kotlinmania.starlark.Error
import io.github.kotlinmania.starlark.eval.runtime.SmallDuration
import io.github.kotlinmania.starlark.eval.runtime.profile.data.ProfileData
import io.github.kotlinmania.starlark.eval.runtime.profile.data.ProfileDataImpl
import io.github.kotlinmania.starlark.eval.runtime.profile.flamegraph.FlameGraphData
import io.github.kotlinmania.starlark.eval.runtime.profile.flamegraph.FlameGraphNode
import io.github.kotlinmania.starlark.eval.runtime.profile.mode.ProfileMode
import io.github.kotlinmania.starlark.util.ArcStr
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.RawPointer
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.layout.heap.ValueHolder

// pub(crate) struct TimeFlameProfilerType
internal object TimeFlameProfilerType : ProfilerType<FlameGraphData> {
    // const PROFILE_MODE: ProfileMode = ProfileMode::TimeFlame
    override val profileMode: ProfileMode = ProfileMode.TimeFlame

    // fn data_from_generic(profile_data: &ProfileDataImpl) -> Option<&Self::Data>
    override fun dataFromGeneric(profileData: ProfileDataImpl): FlameGraphData? =
        (profileData as? ProfileDataImpl.TimeFlameProfile)?.data

    // fn data_to_generic(data: Self::Data) -> ProfileDataImpl
    override fun dataToGeneric(data: FlameGraphData): ProfileDataImpl =
        ProfileDataImpl.TimeFlameProfile(data)

    // fn merge_profiles_impl(profiles: &[&Self::Data]) -> starlark_syntax::Result<Self::Data>
    override fun mergeProfilesImpl(profiles: List<FlameGraphData>): Result<FlameGraphData> =
        Result.success(FlameGraphData.merge(profiles))
}

// #[derive(Debug, thiserror::Error)]
// enum FlameProfileError
private sealed class FlameProfileError(
    message: String,
) : Exception(message) {
    // #[error("Flame profile not enabled")]
    // NotEnabled
    class NotEnabled : FlameProfileError("Flame profile not enabled")
}

// #[derive(Hash, PartialEq, Eq, Clone, Copy, Dupe)]
// struct MutableValueId(usize)
internal data class MutableValueId(
    val value: Int,
)

// #[derive(Hash, PartialEq, Eq, Clone, Copy, Dupe)]
// struct FrozenValueId(usize)
internal data class FrozenValueId(
    val value: Int,
)

/** Index into FlameData.values */
// enum ValueId
internal sealed class ValueId {
    // Mutable(MutableValueId)
    data class Mutable(
        val id: MutableValueId,
    ) : ValueId()

    // Frozen(FrozenValueId)
    data class Frozen(
        val id: FrozenValueId,
    ) : ValueId()

    // fn lookup<'a, T>(self, mutable: &'a [T], frozen: &'a [T]) -> &'a T
    fun <T> lookup(mutable: List<T>, frozen: List<T>): T =
        when (this) {
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
// #[derive(Default)]
// struct ValueIndex<'v>
private class ValueIndex {
    /** Map from MutableValueId to Value. */
    // mutable_values: Vec<Value<'v>>
    val mutableValues: MutableList<Value> = mutableListOf()

    /** Map from FrozenValueId to Value. */
    // frozen_values: Vec<FrozenValue>
    val frozenValues: MutableList<FrozenValue> = mutableListOf()

    /** Map from Value to MutableValueId. */
    // mutable_map: HashMap<RawPointer, MutableValueId, StarlarkHasherBuilder>
    val mutableMap: MutableMap<RawPointer, MutableValueId> = mutableMapOf()

    /** Map from Value to FrozenValueId. */
    // frozen_map: HashMap<RawPointer, FrozenValueId, StarlarkHasherBuilder>
    val frozenMap: MutableMap<RawPointer, FrozenValueId> = mutableMapOf()

    // unsafe impl Trace for ValueIndex
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
    // fn index(&mut self, value: Value<'v>) -> ValueId
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

// enum Frame
internal sealed class Frame {
    /** Entry recorded when we enter a function. */
    // Push(ValueId)
    data class Push(
        val id: ValueId,
    ) : Frame()

    /** Entry recorded when we exit a function. */
    // Pop
    data object Pop : Frame()
}

// pub(crate) struct TimeFlameProfile<'v>(Option<Box<FlameData<'v>>>)
internal class TimeFlameProfile {
    /** `non-null` means enabled. */
    private var data: FlameData? = null

    companion object {
        // pub(crate) fn new() -> Self
        fun new(): TimeFlameProfile = TimeFlameProfile()

        // fn gen_profile(x: &FlameData) -> ProfileData
        private fun genProfile(x: FlameData): ProfileData {
            val mutableNames = x.index.mutableValues.map { it.toRepr() }
            val frozenNames = x.index.frozenValues.map { it.toValue().toRepr() }
            return ProfileData(
                profile =
                    ProfileDataImpl.TimeFlameProfile(
                        Stacks.new(mutableNames, frozenNames, x.frames).render(),
                    ),
            )
        }
    }

    // pub(crate) fn enable(&mut self)
    fun enable() {
        data = FlameData()
    }

    // pub(crate) fn record_call_enter(&mut self, function: Value<'v>)
    fun recordCallEnter(function: Value) {
        val x = data ?: return
        val ind = x.index.index(function)
        x.frames.add(Pair(Frame.Push(ind), ProfilerInstant.now()))
    }

    // pub(crate) fn record_call_exit(&mut self)
    fun recordCallExit() {
        val x = data ?: return
        x.frames.add(Pair(Frame.Pop, ProfilerInstant.now()))
    }

    // pub(crate) fn gen(&self) -> crate::Result<ProfileData>
    fun gen(): ProfileData {
        val x = data ?: throw Error.newOther(FlameProfileError.NotEnabled())
        return genProfile(x)
    }

    // #[derive(Trace)] on TimeFlameProfile traces its inner FlameData.
    // FlameData contains ValueIndex which holds mutable Value references
    // that must be updated during GC.
    fun trace(tracer: Tracer) {
        val x = data ?: return
        x.index.trace(tracer)
    }
}

// #[derive(Default, Trace)]
// struct FlameData<'v>
private class FlameData {
    /** All events in the profile, i.e. function entry or exit with timestamp. */
    // frames: Vec<(Frame, ProfilerInstant)>
    val frames: MutableList<Pair<Frame, ProfilerInstant>> = mutableListOf()

    // index: ValueIndex<'v>
    val index: ValueIndex = ValueIndex()
}

// struct Stacks<'a>
private class Stacks(
    // name: &'a str
    val name: String,
    // time: SmallDuration
    var time: SmallDuration = SmallDuration.ZERO,
    // children: HashMap<ValueId, Stacks<'a>, StarlarkHasherBuilder>
    val children: MutableMap<ValueId, Stacks> = mutableMapOf(),
) {
    companion object {
        // fn blank(name: &'a str) -> Self
        fun blank(name: String): Stacks = Stacks(name)

        // fn new(mutable_names: &'a [String], frozen_names: &'a [String], frames: &[(Frame, ProfilerInstant)]) -> Self
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

    // fn add(&mut self, mutable_names: &'a [String], frozen_names: &'a [String], frames: &mut slice::Iter<(Frame, ProfilerInstant)>, last_time: &mut ProfilerInstant)
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
                    val child =
                        children.getOrPut(frame.id) {
                            blank(frame.id.lookup(mutableNames, frozenNames))
                        }
                    child.add(mutableNames, frozenNames, frames, lastTime)
                }
            }
        }
    }

    // fn render_with_buffer(&self, node: &mut FlameGraphNode)
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

    // fn render(&self) -> FlameGraphData
    fun render(): FlameGraphData {
        val data = FlameGraphData()
        renderWithBuffer(data.root())
        return data
    }
}
