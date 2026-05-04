// port-lint: source eval/runtime/profile/profilerType.rs
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

import io.github.kotlinmania.starlark.eval.runtime.profile.data.ProfileDataImpl
import io.github.kotlinmania.starlark.eval.runtime.profile.data.ProfileData
import io.github.kotlinmania.starlark.eval.runtime.profile.mode.ProfileMode

//     InconsistentProfileType(ProfileMode, ProfileMode),
private class ProfileError(expected: ProfileMode, got: ProfileMode) :
    Exception("Inconsistent profile type, expected `$expected`, got `$got`")

//     type Data;
internal interface ProfilerType<Data> {
    /** Result of profiling. */
    // type Data;

    val profileMode: ProfileMode

    fun dataFromGeneric(profileData: ProfileDataImpl): Data?

    fun dataToGeneric(data: Data): ProfileDataImpl

    fun mergeProfilesImpl(profiles: List<Data>): Result<Data>

    fun mergeProfiles(profiles: List<ProfileData>): Result<ProfileData> {
        val typedProfiles = profiles.map { p ->
            dataFromGeneric(p.profile)
                ?: return Result.failure(ProfileError(profileMode, p.profile.profileMode()))
        }
        return mergeProfilesImpl(typedProfiles).map { merged ->
            ProfileData(dataToGeneric(merged))
        }
    }
}
