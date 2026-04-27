// port-lint: source src/eval/runtime/profile/typecheck.rs
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

/** Runtime typecheck profile. */

import starlarkmap.smallmap.SmallMap
import io.github.kotlinmania.starlark.eval.runtime.profile.csv.CsvWriter
import io.github.kotlinmania.starlark.eval.runtime.profile.data.ProfileData
import io.github.kotlinmania.starlark.eval.runtime.profile.data.ProfileDataImpl
import io.github.kotlinmania.starlark.eval.runtime.SmallDuration
import kotlin.time.Duration
import io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark.util.ArcStr
import io.github.kotlinmania.starlark.eval.runtime.profile.mode.ProfileMode

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
                byFunction[name] = (byFunction[name] ?: SmallDuration.default()) + time
            }
        }
        return Result.success(TypecheckProfileData(byFunction))
    }
}

private sealed class TypecheckProfileError(message: String) : Exception(message) {
    // NotEnabled
    class NotEnabled : TypecheckProfileError("Typecheck profile not enabled")
}

internal data class TypecheckProfileData(
    val byFunction: Map<ArcStr, SmallDuration> = emptyMap(),
) {
    fun genCsv(): String {
        val totalTime = byFunction.values.fold(SmallDuration.default()) { acc, v -> acc + v }

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

internal class TypecheckProfile {
    var enabled: Boolean = false

    private val byFunction: MutableMap<FrozenStringValue, SmallDuration> = mutableMapOf()

    fun add(function: FrozenStringValue, time: Duration) {
        check(enabled)
        byFunction[function] = (byFunction[function] ?: SmallDuration.default()) + SmallDuration.fromDuration(time)
    }

    fun gen(): ProfileData {
        if (!enabled) {
            throw io.github.kotlinmania.starlark.Error.newOther(TypecheckProfileError.NotEnabled())
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
