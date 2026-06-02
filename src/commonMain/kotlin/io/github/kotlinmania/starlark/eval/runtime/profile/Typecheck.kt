// port-lint: source src/eval/runtime/profile/typecheck.rs
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

/** Runtime typecheck profile. */

import io.github.kotlinmania.starlark.Error
import io.github.kotlinmania.starlark.eval.runtime.SmallDuration
import io.github.kotlinmania.starlark.eval.runtime.profile.csv.CsvWriter
import io.github.kotlinmania.starlark.eval.runtime.profile.data.ProfileData
import io.github.kotlinmania.starlark.eval.runtime.profile.data.ProfileDataImpl
import io.github.kotlinmania.starlark.eval.runtime.profile.mode.ProfileMode
import io.github.kotlinmania.starlark.util.ArcStr
import io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue
import kotlin.time.Duration

internal object TypecheckProfilerType : ProfilerType<TypecheckProfileData> {
    override val profileMode: ProfileMode = ProfileMode.Typecheck

    override fun dataFromGeneric(profileData: ProfileDataImpl): TypecheckProfileData? =
        (profileData as? ProfileDataImpl.Typecheck)?.data

    override fun dataToGeneric(data: TypecheckProfileData): ProfileDataImpl =
        ProfileDataImpl.Typecheck(data)

    override fun mergeProfilesImpl(profiles: List<TypecheckProfileData>): Result<TypecheckProfileData> {
        val byFunction = mutableMapOf<ArcStr, SmallDuration>()
        for (profile in profiles) {
            for ((name, time) in profile.byFunction) {
                byFunction[name] = (byFunction[name] ?: SmallDuration.ZERO) + time
            }
        }
        return Result.success(TypecheckProfileData(byFunction))
    }
}

private sealed class TypecheckProfileError(
    message: String,
) : Exception(message) {
    // #[error("Typecheck profile not enabled")]
    // NotEnabled
    class NotEnabled : TypecheckProfileError("Typecheck profile not enabled")
}

internal data class TypecheckProfileData(
    // by_function: SmallMap<ArcStr, SmallDuration>
    val byFunction: Map<ArcStr, SmallDuration> = emptyMap(),
) {
    fun genCsv(): String {
        val totalTime = byFunction.values.fold(SmallDuration.ZERO) { acc, v -> acc + v }

        val w = CsvWriter(listOf("Function", "Time (s)"))
        w.writeDisplay("TOTAL")
        w.writeValue(totalTime)
        w.finishRow()

        val sorted =
            byFunction.entries.sortedWith(
                compareBy<Map.Entry<ArcStr, SmallDuration>> { ULong.MAX_VALUE - it.value.nanos }
                    .thenBy { it.key },
            )

        for ((name, t) in sorted) {
            w.writeDisplay(name.asStr())
            w.writeValue(t)
            w.finishRow()
        }

        return w.finish()
    }
}

internal class TypecheckProfile {
    // pub(crate) enabled: bool
    var enabled: Boolean = false

    // by_function: HashMap<Hashed<FrozenStringValue>, SmallDuration, StarlarkHasherBuilder>
    private val byFunction: MutableMap<FrozenStringValue, SmallDuration> = mutableMapOf()

    fun add(function: FrozenStringValue, time: Duration) {
        check(enabled)
        byFunction[function] = (byFunction[function] ?: SmallDuration.ZERO) + SmallDuration.fromDuration(time)
    }

    fun gen(): ProfileData {
        if (!enabled) {
            throw Error.newOther(TypecheckProfileError.NotEnabled())
        }
        return ProfileData(
            profile =
                ProfileDataImpl.Typecheck(
                    TypecheckProfileData(
                        byFunction =
                            byFunction.entries.associate { (k, v) ->
                                ArcStr.from(k.asStr()) to v
                            },
                    ),
                ),
        )
    }
}
