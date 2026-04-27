// port-lint: source src/eval/runtime/profile/heap.rs
package io.github.kotlinmania.starlark.eval.runtime.profile.heap

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

import io.github.kotlinmania.starlark.environment.Globals
import io.github.kotlinmania.starlark.environment.Module
import io.github.kotlinmania.starlark.eval.runtime.profile.data.ProfileDataImpl
import io.github.kotlinmania.starlark.eval.runtime.profile.mode.ProfileMode
import io.github.kotlinmania.starlark.eval.runtime.profile.ProfilerType
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.eval.runtime.profile.data.ProfileData
import io.github.kotlinmania.starlark.values.layout.heap.profile.AggregateHeapProfileInfo
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.syntax.dialect.Dialect
import io.github.kotlinmania.starlark.eval.evalModule
import io.github.kotlinmania.starlark.eval.evalFunction
import io.github.kotlinmania.starlark.syntax.AstModule

internal object HeapAllocatedProfilerType : ProfilerType<AggregateHeapProfileInfo> {
    override val profileMode: ProfileMode = ProfileMode.HeapAllocated

    override fun dataFromGeneric(profileData: ProfileDataImpl): AggregateHeapProfileInfo? =
        when (profileData) {
            is ProfileDataImpl.HeapAllocated -> profileData.data
            else -> null
        }

    override fun dataToGeneric(data: AggregateHeapProfileInfo): ProfileDataImpl =
        ProfileDataImpl.HeapAllocated(data)

    override fun mergeProfilesImpl(profiles: List<AggregateHeapProfileInfo>): Result<AggregateHeapProfileInfo> =
        Result.success(AggregateHeapProfileInfo.merge(profiles))
}

internal object HeapRetainedProfilerType : ProfilerType<AggregateHeapProfileInfo> {
    override val profileMode: ProfileMode = ProfileMode.HeapRetained

    override fun dataFromGeneric(profileData: ProfileDataImpl): AggregateHeapProfileInfo? =
        when (profileData) {
            is ProfileDataImpl.HeapRetained -> profileData.data
            else -> null
        }

    override fun dataToGeneric(data: AggregateHeapProfileInfo): ProfileDataImpl =
        ProfileDataImpl.HeapRetained(data)

    override fun mergeProfilesImpl(profiles: List<AggregateHeapProfileInfo>): Result<AggregateHeapProfileInfo> =
        Result.success(AggregateHeapProfileInfo.merge(profiles))
}

internal object HeapSummaryAllocatedProfilerType : ProfilerType<AggregateHeapProfileInfo> {
    override val profileMode: ProfileMode = ProfileMode.HeapSummaryAllocated

    override fun dataFromGeneric(profileData: ProfileDataImpl): AggregateHeapProfileInfo? =
        when (profileData) {
            is ProfileDataImpl.HeapSummaryAllocated -> profileData.data
            else -> null
        }

    override fun dataToGeneric(data: AggregateHeapProfileInfo): ProfileDataImpl =
        ProfileDataImpl.HeapSummaryAllocated(data)

    override fun mergeProfilesImpl(profiles: List<AggregateHeapProfileInfo>): Result<AggregateHeapProfileInfo> =
        Result.success(AggregateHeapProfileInfo.merge(profiles))
}

internal object HeapFlameAllocatedProfilerType : ProfilerType<AggregateHeapProfileInfo> {
    override val profileMode: ProfileMode = ProfileMode.HeapFlameAllocated

    override fun dataFromGeneric(profileData: ProfileDataImpl): AggregateHeapProfileInfo? =
        when (profileData) {
            is ProfileDataImpl.HeapFlameAllocated -> profileData.data
            else -> null
        }

    override fun dataToGeneric(data: AggregateHeapProfileInfo): ProfileDataImpl =
        ProfileDataImpl.HeapFlameAllocated(data)

    override fun mergeProfilesImpl(profiles: List<AggregateHeapProfileInfo>): Result<AggregateHeapProfileInfo> =
        Result.success(AggregateHeapProfileInfo.merge(profiles))
}

internal object HeapSummaryRetainedProfilerType : ProfilerType<AggregateHeapProfileInfo> {
    override val profileMode: ProfileMode = ProfileMode.HeapSummaryRetained

    override fun dataFromGeneric(profileData: ProfileDataImpl): AggregateHeapProfileInfo? =
        when (profileData) {
            is ProfileDataImpl.HeapSummaryRetained -> profileData.data
            else -> null
        }

    override fun dataToGeneric(data: AggregateHeapProfileInfo): ProfileDataImpl =
        ProfileDataImpl.HeapSummaryRetained(data)

    override fun mergeProfilesImpl(profiles: List<AggregateHeapProfileInfo>): Result<AggregateHeapProfileInfo> =
        Result.success(AggregateHeapProfileInfo.merge(profiles))
}

internal object HeapFlameRetainedProfilerType : ProfilerType<AggregateHeapProfileInfo> {
    override val profileMode: ProfileMode = ProfileMode.HeapFlameRetained

    override fun dataFromGeneric(profileData: ProfileDataImpl): AggregateHeapProfileInfo? =
        when (profileData) {
            is ProfileDataImpl.HeapFlameRetained -> profileData.data
            else -> null
        }

    override fun dataToGeneric(data: AggregateHeapProfileInfo): ProfileDataImpl =
        ProfileDataImpl.HeapFlameRetained(data)

    override fun mergeProfilesImpl(profiles: List<AggregateHeapProfileInfo>): Result<AggregateHeapProfileInfo> =
        Result.success(AggregateHeapProfileInfo.merge(profiles))
}

internal enum class RetainedHeapProfileMode {
    Flame,
    Summary,
    FlameAndSummary,
}

private sealed class HeapProfileError : Exception() {
    data object NotEnabled : HeapProfileError() {
        override val message: String get() = "heap profile not enabled"
    }
}

internal enum class HeapProfileFormat {
    FlameGraph,
    Summary,
    FlameGraphAndSummary,
}

internal class HeapProfile(
    private var enabled: Boolean = false,
) {
    fun enable() {
        enabled = true
    }

    fun recordCallEnter(function: Value, heap: Heap) {
        if (enabled) {
            heap.recordCallEnter(function)
        }
    }

    fun recordCallExit(heap: Heap) {
        if (enabled) {
            heap.recordCallExit()
        }
    }

    fun gen(heap: Heap, format: HeapProfileFormat): ProfileData {
        if (!enabled) {
            throw HeapProfileError.NotEnabled
        }
        return genEnabled(heap, format)
    }

    companion object {
        fun new(): HeapProfile = HeapProfile()

        fun genEnabled(heap: Heap, format: HeapProfileFormat): ProfileData =
            when (format) {
                HeapProfileFormat.FlameGraphAndSummary -> writeFlameAndSummarizedHeapProfile(heap)
                HeapProfileFormat.Summary -> writeSummarizedHeapProfile(heap)
                HeapProfileFormat.FlameGraph -> writeFlameHeapProfile(heap)
            }

        fun writeFlameHeapProfile(heap: Heap): ProfileData {
            val stacks = AggregateHeapProfileInfo.collect(heap, null)
            return ProfileData(profile = ProfileDataImpl.HeapFlameAllocated(stacks))
        }

        fun writeSummarizedHeapProfile(heap: Heap): ProfileData {
            val stacks = AggregateHeapProfileInfo.collect(heap, null)
            return ProfileData(profile = ProfileDataImpl.HeapSummaryAllocated(stacks))
        }

        fun writeFlameAndSummarizedHeapProfile(heap: Heap): ProfileData {
            val stacks = AggregateHeapProfileInfo.collect(heap, null)
            return ProfileData(profile = ProfileDataImpl.HeapAllocated(stacks))
        }
    }
}

// --- Tests ---

fun testProfiling() {
    val ast = AstModule.parse(
        "foo.bzl",
        """
def f(x):
    return (x * 5) + 3
y = 8 * 9 + 2
f
""",
        Dialect.AllOptionsInternal,
    ).getOrThrow()
    val globals = Globals.standard()
    Heap.temp { heap ->
        val module = Module.withHeap(heap)
        val module2 = Module.withHeap(heap)
        val module3 = Module.withHeap(heap)

        val eval = Evaluator(module)
        eval.enableProfile(ProfileMode.HeapSummaryAllocated)
        val f = eval.evalModule(ast, globals).getOrThrow()

        // first check module profiling works
        HeapProfile.writeSummarizedHeapProfile(module.heap())
        HeapProfile.writeFlameHeapProfile(module.heap())

        // second check function profiling works
        val eval2 = Evaluator(module2)
        eval2.enableProfile(ProfileMode.HeapSummaryAllocated)
        eval2.evalFunction(f, listOf(Value.testingNewInt(100)), listOf()).getOrThrow()

        HeapProfile.writeSummarizedHeapProfile(module2.heap())
        HeapProfile.writeFlameHeapProfile(module2.heap())

        // finally, check a user can add values into the heap before/after
        val eval3 = Evaluator(module3)
        module3.heap().allocStr("Thing that goes before")
        eval3.enableProfile(ProfileMode.HeapSummaryAllocated)
        eval3.evalFunction(f, listOf(Value.testingNewInt(100)), listOf()).getOrThrow()

        module3.heap().allocStr("Thing that goes after")
        HeapProfile.writeSummarizedHeapProfile(module3.heap())
        HeapProfile.writeFlameHeapProfile(module3.heap())
    }
}
