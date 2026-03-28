// port-lint: source src/eval/runtime/profile/data.rs
package io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.data

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

import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.BcPairsProfileData
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.BcPairsProfilerType
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.BcProfileData
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.BcProfilerType
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.CoverageProfileType
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.StmtProfileData
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.StmtProfilerType
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.flamegraph.FlameGraphData
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.heap.HeapAllocatedProfilerType
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.heap.HeapFlameAllocatedProfilerType
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.heap.HeapFlameRetainedProfilerType
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.heap.HeapRetainedProfilerType
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.heap.HeapSummaryAllocatedProfilerType
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.heap.HeapSummaryRetainedProfilerType
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.mode.ProfileMode
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.profile.AggregateHeapProfileInfo
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.TypecheckProfilerType
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.TypecheckProfileData
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.TimeFlameProfilerType

// #[derive(Debug, thiserror::Error)]
// enum ProfileDataError
sealed class ProfileDataError : Exception() {
    // #[error("Empty profile list cannot be merged")]
    data object EmptyProfileList : ProfileDataError() {
        override val message: String get() = "Empty profile list cannot be merged"
    }
    // #[error("Different profile modes in profile")]
    data object DifferentProfileModes : ProfileDataError() {
        override val message: String get() = "Different profile modes in profile"
    }
}

// #[derive(Clone, Debug)]
// pub(crate) enum ProfileDataImpl
internal sealed class ProfileDataImpl {
    // Bc(Box<BcProfileData>)
    data class Bc(val data: BcProfileData) : ProfileDataImpl()
    // BcPairs(BcPairsProfileData)
    data class BcPairs(val data: BcPairsProfileData) : ProfileDataImpl()
    // HeapRetained(Box<AggregateHeapProfileInfo>)
    data class HeapRetained(val data: AggregateHeapProfileInfo) : ProfileDataImpl()
    // HeapAllocated(Box<AggregateHeapProfileInfo>)
    data class HeapAllocated(val data: AggregateHeapProfileInfo) : ProfileDataImpl()
    // HeapFlameRetained(Box<AggregateHeapProfileInfo>)
    data class HeapFlameRetained(val data: AggregateHeapProfileInfo) : ProfileDataImpl()
    // HeapFlameAllocated(Box<AggregateHeapProfileInfo>)
    data class HeapFlameAllocated(val data: AggregateHeapProfileInfo) : ProfileDataImpl()
    // HeapSummaryRetained(Box<AggregateHeapProfileInfo>)
    data class HeapSummaryRetained(val data: AggregateHeapProfileInfo) : ProfileDataImpl()
    // HeapSummaryAllocated(Box<AggregateHeapProfileInfo>)
    data class HeapSummaryAllocated(val data: AggregateHeapProfileInfo) : ProfileDataImpl()
    /// Flame graph data is in milliseconds.
    // TimeFlameProfile(FlameGraphData)
    data class TimeFlameProfile(val data: FlameGraphData) : ProfileDataImpl()
    // Statement(StmtProfileData)
    data class Statement(val data: StmtProfileData) : ProfileDataImpl()
    // Coverage(StmtProfileData)
    data class Coverage(val data: StmtProfileData) : ProfileDataImpl()
    // Typecheck(TypecheckProfileData)
    data class Typecheck(val data: TypecheckProfileData) : ProfileDataImpl()
    // None
    data object None : ProfileDataImpl()

    // pub(crate) fn profile_mode(&self) -> ProfileMode
    fun profileMode(): ProfileMode = when (this) {
        is Bc -> ProfileMode.Bytecode
        is BcPairs -> ProfileMode.BytecodePairs
        is HeapRetained -> ProfileMode.HeapRetained
        is HeapAllocated -> ProfileMode.HeapAllocated
        is HeapFlameRetained -> ProfileMode.HeapFlameRetained
        is HeapFlameAllocated -> ProfileMode.HeapFlameAllocated
        is HeapSummaryRetained -> ProfileMode.HeapSummaryRetained
        is HeapSummaryAllocated -> ProfileMode.HeapSummaryAllocated
        is TimeFlameProfile -> ProfileMode.TimeFlame
        is Statement -> ProfileMode.Statement
        is Coverage -> ProfileMode.Coverage
        is Typecheck -> ProfileMode.Typecheck
        is None -> ProfileMode.None
    }
}

/// Collected profiling data.
// #[derive(Clone, Debug)]
// pub struct ProfileData
data class ProfileData internal constructor(
    internal val profile: ProfileDataImpl,
) {
    /// Profile mode used to collect this data.
    // pub fn profile_mode(&self) -> ProfileMode
    fun profileMode(): ProfileMode = profile.profileMode()

    /// Generate a string with flamegraph profile data, depending on profile type.
    // pub fn gen_flame_data(&self) -> crate::Result<String>
    fun genFlameData(): String = when (val p = profile) {
        is ProfileDataImpl.TimeFlameProfile -> p.data.write()
        is ProfileDataImpl.HeapRetained -> p.data.genFlameGraphData()
        is ProfileDataImpl.HeapAllocated -> p.data.genFlameGraphData()
        is ProfileDataImpl.HeapFlameRetained -> p.data.genFlameGraphData()
        is ProfileDataImpl.HeapFlameAllocated -> p.data.genFlameGraphData()
        else -> ""
    }

    /// Generate a string with csv profile data, depending on profile type.
    // pub fn gen_csv(&self) -> crate::Result<String>
    fun genCsv(): String = when (val p = profile) {
        is ProfileDataImpl.Bc -> p.data.genCsv()
        is ProfileDataImpl.BcPairs -> p.data.genCsv()
        is ProfileDataImpl.HeapRetained -> p.data.genSummaryCsv()
        is ProfileDataImpl.HeapAllocated -> p.data.genSummaryCsv()
        is ProfileDataImpl.HeapSummaryRetained -> p.data.genSummaryCsv()
        is ProfileDataImpl.HeapSummaryAllocated -> p.data.genSummaryCsv()
        is ProfileDataImpl.TimeFlameProfile -> p.data.write()
        is ProfileDataImpl.Statement -> p.data.writeToString()
        is ProfileDataImpl.Coverage -> p.data.writeCoverage()
        is ProfileDataImpl.Typecheck -> p.data.genCsv()
        else -> ""
    }

    companion object {
        /// Merge profiles (aggregate).
        // pub fn merge(profiles: impl IntoIterator<Item = &'a ProfileData>) -> crate::Result<ProfileData>
        fun merge(profiles: Iterable<ProfileData>): ProfileData {
            val list = profiles.toList()

            if (list.size == 1) {
                return list[0].copy()
            }

            val profileMode = list.firstOrNull()?.profile?.profileMode()
                ?: throw ProfileDataError.EmptyProfileList

            for (p in list) {
                if (p.profile.profileMode() != profileMode) {
                    throw ProfileDataError.DifferentProfileModes
                }
            }

            val profile = when (profileMode) {
                ProfileMode.Bytecode -> BcProfilerType.mergeProfiles(list).getOrThrow().profile
                ProfileMode.BytecodePairs -> BcPairsProfilerType.mergeProfiles(list).getOrThrow().profile
                ProfileMode.HeapAllocated -> HeapAllocatedProfilerType.mergeProfiles(list).getOrThrow().profile
                ProfileMode.HeapRetained -> HeapRetainedProfilerType.mergeProfiles(list).getOrThrow().profile
                ProfileMode.HeapSummaryAllocated -> HeapSummaryAllocatedProfilerType.mergeProfiles(list).getOrThrow().profile
                ProfileMode.HeapSummaryRetained -> HeapSummaryRetainedProfilerType.mergeProfiles(list).getOrThrow().profile
                ProfileMode.HeapFlameAllocated -> HeapFlameAllocatedProfilerType.mergeProfiles(list).getOrThrow().profile
                ProfileMode.HeapFlameRetained -> HeapFlameRetainedProfilerType.mergeProfiles(list).getOrThrow().profile
                ProfileMode.TimeFlame -> TimeFlameProfilerType.mergeProfiles(list).getOrThrow().profile
                ProfileMode.Typecheck -> TypecheckProfilerType.mergeProfiles(list).getOrThrow().profile
                ProfileMode.Statement -> StmtProfilerType.mergeProfiles(list).getOrThrow().profile
                ProfileMode.Coverage -> CoverageProfileType.mergeProfiles(list).getOrThrow().profile
                ProfileMode.None -> ProfileDataImpl.None
            }
            return ProfileData(profile)
        }
    }
}
