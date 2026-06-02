// port-lint: source src/eval/runtime/profile/heap.rs
package io.github.kotlinmania.starlark.eval.runtime.profile.heap

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

import io.github.kotlinmania.starlark.eval.runtime.profile.ProfilerType
import io.github.kotlinmania.starlark.eval.runtime.profile.data.ProfileData
import io.github.kotlinmania.starlark.eval.runtime.profile.data.ProfileDataImpl
import io.github.kotlinmania.starlark.eval.runtime.profile.mode.ProfileMode
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.heap.profile.AggregateHeapProfileInfo

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
    // #[error("heap profile not enabled")]
    data object NotEnabled : HeapProfileError() {
        override val message: String get() = "heap profile not enabled"
    }
}

internal enum class HeapProfileFormat {
    FlameGraph,
    Summary,
    FlameGraphAndSummary,
}

// pub(crate) struct HeapProfile
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
