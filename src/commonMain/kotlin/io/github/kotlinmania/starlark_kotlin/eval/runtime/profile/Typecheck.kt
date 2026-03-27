// port-lint: source src/eval/runtime/profile/typecheck.rs
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

/// Runtime typecheck profile.

import io.github.kotlinmania.starlark_kotlin.collections.SmallMap
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.csv.CsvWriter
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.data.ProfileData
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.data.ProfileDataImpl
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.profiler_type.ProfilerType
import io.github.kotlinmania.starlark_kotlin.eval.runtime.small_duration.SmallDuration
import kotlin.time.Duration
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark_kotlin.util.ArcStr
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.mode.ProfileMode
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark_kotlin.values.types.int.ZERO
import io.github.kotlinmania.starlark_kotlin.util.asStr

// pub(crate) struct TypecheckProfilerType
internal object TypecheckProfilerType : ProfilerType<TypecheckProfileData> {
    // const PROFILE_MODE: ProfileMode = ProfileMode::Typecheck
    override val profileMode: ProfileMode = ProfileMode.Typecheck

    // fn data_from_generic(profile_data: &ProfileDataImpl) -> Option<&Self::Data>
    override fun dataFromGeneric(profileData: ProfileDataImpl): TypecheckProfileData? =
        (profileData as? ProfileDataImpl.Typecheck)?.data

    // fn data_to_generic(data: Self::Data) -> ProfileDataImpl
    override fun dataToGeneric(data: TypecheckProfileData): ProfileDataImpl =
        ProfileDataImpl.Typecheck(data)

    // fn merge_profiles_impl(profiles: &[&Self::Data]) -> starlark_syntax::Result<Self::Data>
    override fun mergeProfilesImpl(profiles: List<TypecheckProfileData>): TypecheckProfileData {
        val byFunction = mutableMapOf<ArcStr, SmallDuration>()
        for (profile in profiles) {
            for ((name, time) in profile.byFunction) {
                byFunction[name] = (byFunction[name] ?: SmallDuration.ZERO) + time
            }
        }
        return TypecheckProfileData(byFunction)
    }
}

// #[derive(Debug, thiserror::Error)]
// enum TypecheckProfileError
private sealed class TypecheckProfileError(message: String) : Exception(message) {
    // #[error("Typecheck profile not enabled")]
    // NotEnabled
    class NotEnabled : TypecheckProfileError("Typecheck profile not enabled")
}

// #[derive(Default, Debug, Clone, Eq, PartialEq)]
// pub(crate) struct TypecheckProfileData
internal data class TypecheckProfileData(
    // by_function: SmallMap<ArcStr, SmallDuration>
    val byFunction: Map<ArcStr, SmallDuration> = emptyMap(),
) {
    // pub(crate) fn gen_csv(&self) -> String
    fun genCsv(): String {
        val totalTime = byFunction.values.fold(SmallDuration.ZERO) { acc, v -> acc + v }

        val w = CsvWriter(listOf("Function", "Time (s)"))
        w.writeDisplay("TOTAL")
        w.writeValue(totalTime)
        w.finishRow()

        val sorted = byFunction.entries.sortedWith(
            compareBy<Map.Entry<ArcStr, SmallDuration>> { ULong.MAX_VALUE - it.value.nanos }
                .thenBy { it.key }
        )

        for ((name, t) in sorted) {
            w.writeDisplay(name.asStr())
            w.writeValue(t)
            w.finishRow()
        }

        return w.finish()
    }
}

// #[derive(Default, Debug)]
// pub(crate) struct TypecheckProfile
internal class TypecheckProfile {
    // pub(crate) enabled: bool
    var enabled: Boolean = false

    // by_function: HashMap<Hashed<FrozenStringValue>, SmallDuration, StarlarkHasherBuilder>
    private val byFunction: MutableMap<FrozenStringValue, SmallDuration> = mutableMapOf()

    // pub(crate) fn add(&mut self, function: FrozenStringValue, time: Duration)
    fun add(function: FrozenStringValue, time: Duration) {
        check(enabled)
        byFunction[function] = (byFunction[function] ?: SmallDuration.ZERO) + SmallDuration.fromDuration(time)
    }

    // pub(crate) fn gen(&self) -> crate::Result<ProfileData>
    fun gen(): ProfileData {
        if (!enabled) {
            throw Error.newOther(TypecheckProfileError.NotEnabled())
        }
        return ProfileData(
            profile = ProfileDataImpl.Typecheck(
                TypecheckProfileData(
                    byFunction = byFunction.entries.associate { (k, v) ->
                        ArcStr.from(k.asStr()) to v
                    }
                )
            )
        )
    }
}
