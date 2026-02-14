// port-lint: source src/eval/runtime/profile/heap.rs
package io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.heap

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

import io.github.kotlinmania.starlark_kotlin.environment.Globals
import io.github.kotlinmania.starlark_kotlin.environment.Module
import io.github.kotlinmania.starlark_kotlin.eval.Evaluator
import io.github.kotlinmania.starlark_kotlin.eval.ProfileData
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.data.ProfileDataImpl
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.mode.ProfileMode
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.profiler_type.ProfilerType
import io.github.kotlinmania.starlark_kotlin.syntax.AstModule
import io.github.kotlinmania.starlark_kotlin.syntax.Dialect
import io.github.kotlinmania.starlark_kotlin.values.Heap
import io.github.kotlinmania.starlark_kotlin.values.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.profile.aggregated.AggregateHeapProfileInfo

// pub(crate) struct HeapAllocatedProfilerType
internal object HeapAllocatedProfilerType : ProfilerType<AggregateHeapProfileInfo> {
    override val PROFILE_MODE: ProfileMode = ProfileMode.HeapAllocated

    override fun dataFromGeneric(profileData: ProfileDataImpl): AggregateHeapProfileInfo? =
        when (profileData) {
            is ProfileDataImpl.HeapAllocated -> profileData.data
            else -> null
        }

    override fun dataToGeneric(data: AggregateHeapProfileInfo): ProfileDataImpl =
        ProfileDataImpl.HeapAllocated(data)

    override fun mergeProfilesImpl(profiles: List<AggregateHeapProfileInfo>): AggregateHeapProfileInfo =
        AggregateHeapProfileInfo.merge(profiles)
}

// pub(crate) struct HeapRetainedProfilerType
internal object HeapRetainedProfilerType : ProfilerType<AggregateHeapProfileInfo> {
    override val PROFILE_MODE: ProfileMode = ProfileMode.HeapRetained

    override fun dataFromGeneric(profileData: ProfileDataImpl): AggregateHeapProfileInfo? =
        when (profileData) {
            is ProfileDataImpl.HeapRetained -> profileData.data
            else -> null
        }

    override fun dataToGeneric(data: AggregateHeapProfileInfo): ProfileDataImpl =
        ProfileDataImpl.HeapRetained(data)

    override fun mergeProfilesImpl(profiles: List<AggregateHeapProfileInfo>): AggregateHeapProfileInfo =
        AggregateHeapProfileInfo.merge(profiles)
}

// pub(crate) struct HeapSummaryAllocatedProfilerType
internal object HeapSummaryAllocatedProfilerType : ProfilerType<AggregateHeapProfileInfo> {
    override val PROFILE_MODE: ProfileMode = ProfileMode.HeapSummaryAllocated

    override fun dataFromGeneric(profileData: ProfileDataImpl): AggregateHeapProfileInfo? =
        when (profileData) {
            is ProfileDataImpl.HeapSummaryAllocated -> profileData.data
            else -> null
        }

    override fun dataToGeneric(data: AggregateHeapProfileInfo): ProfileDataImpl =
        ProfileDataImpl.HeapSummaryAllocated(data)

    override fun mergeProfilesImpl(profiles: List<AggregateHeapProfileInfo>): AggregateHeapProfileInfo =
        AggregateHeapProfileInfo.merge(profiles)
}

// pub(crate) struct HeapFlameAllocatedProfilerType
internal object HeapFlameAllocatedProfilerType : ProfilerType<AggregateHeapProfileInfo> {
    override val PROFILE_MODE: ProfileMode = ProfileMode.HeapFlameAllocated

    override fun dataFromGeneric(profileData: ProfileDataImpl): AggregateHeapProfileInfo? =
        when (profileData) {
            is ProfileDataImpl.HeapFlameAllocated -> profileData.data
            else -> null
        }

    override fun dataToGeneric(data: AggregateHeapProfileInfo): ProfileDataImpl =
        ProfileDataImpl.HeapFlameAllocated(data)

    override fun mergeProfilesImpl(profiles: List<AggregateHeapProfileInfo>): AggregateHeapProfileInfo =
        AggregateHeapProfileInfo.merge(profiles)
}

// pub(crate) struct HeapSummaryRetainedProfilerType
internal object HeapSummaryRetainedProfilerType : ProfilerType<AggregateHeapProfileInfo> {
    override val PROFILE_MODE: ProfileMode = ProfileMode.HeapSummaryRetained

    override fun dataFromGeneric(profileData: ProfileDataImpl): AggregateHeapProfileInfo? =
        when (profileData) {
            is ProfileDataImpl.HeapSummaryRetained -> profileData.data
            else -> null
        }

    override fun dataToGeneric(data: AggregateHeapProfileInfo): ProfileDataImpl =
        ProfileDataImpl.HeapSummaryRetained(data)

    override fun mergeProfilesImpl(profiles: List<AggregateHeapProfileInfo>): AggregateHeapProfileInfo =
        AggregateHeapProfileInfo.merge(profiles)
}

// pub(crate) struct HeapFlameRetainedProfilerType
internal object HeapFlameRetainedProfilerType : ProfilerType<AggregateHeapProfileInfo> {
    override val PROFILE_MODE: ProfileMode = ProfileMode.HeapFlameRetained

    override fun dataFromGeneric(profileData: ProfileDataImpl): AggregateHeapProfileInfo? =
        when (profileData) {
            is ProfileDataImpl.HeapFlameRetained -> profileData.data
            else -> null
        }

    override fun dataToGeneric(data: AggregateHeapProfileInfo): ProfileDataImpl =
        ProfileDataImpl.HeapFlameRetained(data)

    override fun mergeProfilesImpl(profiles: List<AggregateHeapProfileInfo>): AggregateHeapProfileInfo =
        AggregateHeapProfileInfo.merge(profiles)
}

// #[derive(Copy, Clone, Dupe, Debug, Allocative)]
// pub(crate) enum RetainedHeapProfileMode
internal enum class RetainedHeapProfileMode {
    Flame,
    Summary,
    FlameAndSummary,
}

// #[derive(Debug, thiserror::Error)]
// enum HeapProfileError
internal sealed class HeapProfileError : Exception() {
    // #[error("heap profile not enabled")]
    data object NotEnabled : HeapProfileError() {
        override val message: String get() = "heap profile not enabled"
    }
}

// #[derive(Copy, Clone, Dupe, Debug)]
// pub(crate) enum HeapProfileFormat
internal enum class HeapProfileFormat {
    FlameGraph,
    Summary,
    FlameGraphAndSummary,
}

// pub(crate) struct HeapProfile
internal class HeapProfile(
    private var enabled: Boolean = false,
) {
    // pub(crate) fn enable(&mut self)
    fun enable() {
        enabled = true
    }

    // pub(crate) fn record_call_enter<'v>(&self, function: Value<'v>, heap: Heap<'v>)
    fun recordCallEnter(function: Value, heap: Heap) {
        if (enabled) {
            heap.recordCallEnter(function)
        }
    }

    // pub(crate) fn record_call_exit<'v>(&self, heap: Heap<'v>)
    fun recordCallExit(heap: Heap) {
        if (enabled) {
            heap.recordCallExit()
        }
    }

    // pub(crate) fn gen(&self, heap: Heap<'_>, format: HeapProfileFormat) -> crate::Result<ProfileData>
    fun gen(heap: Heap, format: HeapProfileFormat): ProfileData {
        if (!enabled) {
            throw HeapProfileError.NotEnabled
        }
        return genEnabled(heap, format)
    }

    companion object {
        // pub(crate) fn new() -> Self
        fun new(): HeapProfile = HeapProfile()

        // pub(crate) fn gen_enabled(heap: Heap<'_>, format: HeapProfileFormat) -> ProfileData
        fun genEnabled(heap: Heap, format: HeapProfileFormat): ProfileData =
            when (format) {
                HeapProfileFormat.FlameGraphAndSummary -> writeFlameAndSummarizedHeapProfile(heap)
                HeapProfileFormat.Summary -> writeSummarizedHeapProfile(heap)
                HeapProfileFormat.FlameGraph -> writeFlameHeapProfile(heap)
            }

        // fn write_flame_heap_profile(heap: Heap<'_>) -> ProfileData
        fun writeFlameHeapProfile(heap: Heap): ProfileData {
            val stacks = AggregateHeapProfileInfo.collect(heap, null)
            return ProfileData(profile = ProfileDataImpl.HeapFlameAllocated(stacks))
        }

        // fn write_summarized_heap_profile(heap: Heap<'_>) -> ProfileData
        fun writeSummarizedHeapProfile(heap: Heap): ProfileData {
            val stacks = AggregateHeapProfileInfo.collect(heap, null)
            return ProfileData(profile = ProfileDataImpl.HeapSummaryAllocated(stacks))
        }

        // fn write_flame_and_summarized_heap_profile(heap: Heap<'_>) -> ProfileData
        fun writeFlameAndSummarizedHeapProfile(heap: Heap): ProfileData {
            val stacks = AggregateHeapProfileInfo.collect(heap, null)
            return ProfileData(profile = ProfileDataImpl.HeapAllocated(stacks))
        }
    }
}

// --- Tests ---

// #[test] fn test_profiling()
internal fun testProfiling() {
    val ast = AstModule.parse(
        "foo.bzl",
        """
def f(x):
    return (x * 5) + 3
y = 8 * 9 + 2
f
""",
        Dialect.AllOptionsInternal,
    )
    val globals = Globals.standard()
    Heap.temp { heap ->
        val module = Module.withHeap(heap)
        val module2 = Module.withHeap(heap)
        val module3 = Module.withHeap(heap)

        val eval = Evaluator(module)
        eval.enableProfile(ProfileMode.HeapSummaryAllocated)
        val f = eval.evalModule(ast, globals)

        // first check module profiling works
        HeapProfile.writeSummarizedHeapProfile(module.heap())
        HeapProfile.writeFlameHeapProfile(module.heap())

        // second check function profiling works
        val eval2 = Evaluator(module2)
        eval2.enableProfile(ProfileMode.HeapSummaryAllocated)
        eval2.evalFunction(f, listOf(Value.testingNewInt(100)), listOf())

        HeapProfile.writeSummarizedHeapProfile(module2.heap())
        HeapProfile.writeFlameHeapProfile(module2.heap())

        // finally, check a user can add values into the heap before/after
        val eval3 = Evaluator(module3)
        module3.heap().alloc("Thing that goes before")
        eval3.enableProfile(ProfileMode.HeapSummaryAllocated)
        eval3.evalFunction(f, listOf(Value.testingNewInt(100)), listOf())

        module3.heap().alloc("Thing that goes after")
        HeapProfile.writeSummarizedHeapProfile(module3.heap())
        HeapProfile.writeFlameHeapProfile(module3.heap())
    }
}
