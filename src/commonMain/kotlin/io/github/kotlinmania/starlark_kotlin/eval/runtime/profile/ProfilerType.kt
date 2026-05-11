<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/eval/runtime/profile/ProfilerType.kt
// port-lint: source eval/runtime/profile/profiler_type.rs
package io.github.kotlinmania.starlark.eval.runtime.profile
=======
// port-lint: source src/eval/runtime/profile/profiler_type.rs
package io.github.kotlinmania.starlark_kotlin.eval.runtime.profile
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/eval/runtime/profile/ProfilerType.kt

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

import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.data.ProfileDataImpl
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.data.ProfileData
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.mode.ProfileMode

// #[derive(Debug, thiserror::Error)]
// enum ProfileError {
//     #[error("Inconsistent profile type, expected `{0}`, got `{1}`")]
//     InconsistentProfileType(ProfileMode, ProfileMode),
// }
private class ProfileError(expected: ProfileMode, got: ProfileMode) :
    Exception("Inconsistent profile type, expected `$expected`, got `$got`")

// pub(crate) trait ProfilerType {
//     type Data;
//     const PROFILE_MODE: ProfileMode;
//     fn data_from_generic(profile_data: &ProfileDataImpl) -> Option<&Self::Data>;
//     fn data_to_generic(data: Self::Data) -> ProfileDataImpl;
//     fn merge_profiles_impl(profiles: &[&Self::Data]) -> crate::Result<Self::Data>;
//     fn merge_profiles(profiles: &[&ProfileData]) -> crate::Result<ProfileData>;
// }
internal interface ProfilerType<Data> {
    /** Result of profiling. */
    // type Data;

    // const PROFILE_MODE: ProfileMode;
    val profileMode: ProfileMode

    // fn data_from_generic(profile_data: &ProfileDataImpl) -> Option<&Self::Data>
    fun dataFromGeneric(profileData: ProfileDataImpl): Data?

    // fn data_to_generic(data: Self::Data) -> ProfileDataImpl
    fun dataToGeneric(data: Data): ProfileDataImpl

    // fn merge_profiles_impl(profiles: &[&Self::Data]) -> crate::Result<Self::Data>
    fun mergeProfilesImpl(profiles: List<Data>): Result<Data>

    // fn merge_profiles(profiles: &[&ProfileData]) -> crate::Result<ProfileData>
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
