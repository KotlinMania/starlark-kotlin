// port-lint: source src/eval/runtime/profile/or_instrumentation.rs
package io.github.kotlinmania.starlark_kotlin.eval.runtime.profile

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

import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.mode.ProfileMode

// #[derive(Debug, Default, Clone, Dupe, Eq, PartialEq)]
// pub(crate) enum ProfileOrInstrumentationMode
internal enum class ProfileOrInstrumentationMode {
    // #[default] None
    None,
    // Profile(ProfileMode)
    // Note: requires wrapping in a separate holder
    ;

    companion object {
        fun profile(mode: ProfileMode): ProfileOrInstrumentationModeValue =
            ProfileOrInstrumentationModeValue.Profile(mode)

        fun collected(): ProfileOrInstrumentationModeValue =
            ProfileOrInstrumentationModeValue.Collected
    }
}

// Sealed class version to hold the actual variant data
internal sealed class ProfileOrInstrumentationModeValue {
    // None
    data object None : ProfileOrInstrumentationModeValue()
    // Profile(ProfileMode)
    data class Profile(val mode: ProfileMode) : ProfileOrInstrumentationModeValue()
    // Collected
    data object Collected : ProfileOrInstrumentationModeValue()
}
