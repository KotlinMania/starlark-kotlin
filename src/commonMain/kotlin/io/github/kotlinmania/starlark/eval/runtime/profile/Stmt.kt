// port-lint: source src/eval/runtime/profile/stmt.rs
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

import io.github.kotlinmania.starlark.codemap.CodeMap
import io.github.kotlinmania.starlark.codemap.CodeMapId
import io.github.kotlinmania.starlark.codemap.CodeMaps
import io.github.kotlinmania.starlark.codemap.FileSpan
import io.github.kotlinmania.starlark.codemap.ResolvedFileSpan
import io.github.kotlinmania.starlark.codemap.Span
import io.github.kotlinmania.starlark.codemap.FileSpanRef
import io.github.kotlinmania.starlark.eval.runtime.SmallDuration
import io.github.kotlinmania.starlark.eval.runtime.profile.csv.CsvWriter
import io.github.kotlinmania.starlark.eval.runtime.profile.data.ProfileData
import io.github.kotlinmania.starlark.eval.runtime.profile.data.ProfileDataImpl
import io.github.kotlinmania.starlark.eval.runtime.profile.mode.ProfileMode

// pub(crate) struct StmtProfilerType
internal object StmtProfilerType : ProfilerType<StmtProfileData> {
    override val profileMode: ProfileMode = ProfileMode.Statement

    // fn data_from_generic(profile_data: &ProfileDataImpl) -> Option<&Self::Data>
    override fun dataFromGeneric(profileData: ProfileDataImpl): StmtProfileData? =
        when (profileData) {
            is ProfileDataImpl.Statement -> profileData.data
            else -> null
        }

    // fn data_to_generic(data: Self::Data) -> ProfileDataImpl
    override fun dataToGeneric(data: StmtProfileData): ProfileDataImpl =
        ProfileDataImpl.Statement(data)

    // fn merge_profiles_impl(profiles: &[&Self::Data]) -> Result<Self::Data>
    override fun mergeProfilesImpl(profiles: List<StmtProfileData>): Result<StmtProfileData> =
        Result.success(StmtProfileData.merge(profiles))
}

// pub(crate) struct CoverageProfileType
internal object CoverageProfileType : ProfilerType<StmtProfileData> {
    override val profileMode: ProfileMode = ProfileMode.Coverage

    // fn data_from_generic(profile_data: &ProfileDataImpl) -> Option<&Self::Data>
    override fun dataFromGeneric(profileData: ProfileDataImpl): StmtProfileData? =
        when (profileData) {
            is ProfileDataImpl.Coverage -> profileData.data
            else -> null
        }

    // fn data_to_generic(data: Self::Data) -> ProfileDataImpl
    override fun dataToGeneric(data: StmtProfileData): ProfileDataImpl =
        ProfileDataImpl.Coverage(data)

    // fn merge_profiles_impl(profiles: &[&Self::Data]) -> Result<Self::Data>
    override fun mergeProfilesImpl(profiles: List<StmtProfileData>): Result<StmtProfileData> =
        Result.success(StmtProfileData.merge(profiles))
}

// #[derive(Debug, thiserror::Error)]
// enum StmtProfileError
internal sealed class StmtProfileError : Exception() {
    // #[error("Statement or coverage profiling is not enabled")]
    data object NotEnabled : StmtProfileError() {
        override val message: String get() = "Statement or coverage profiling is not enabled"
    }
}

// #[derive(Clone)]
// struct Last
private data class Last(
    val file: CodeMapId,
    val span: Span,
    val start: ProfilerInstant,
)

// #[derive(Clone)]
// struct StmtProfileState
private class StmtProfileState {
    var files: CodeMaps = CodeMaps()
    var stmts: MutableMap<Pair<CodeMapId, Span>, Pair<Int, SmallDuration>> = mutableMapOf()
    var last: Last? = null

    // fn new() -> Self
    companion object {
        fun new(): StmtProfileState = StmtProfileState()
    }

    // Add the data from last_span into the entries
    // fn add_last(&mut self, now: ProfilerInstant)
    fun addLast(now: ProfilerInstant) {
        val last = this.last ?: return
        val time = now - last.start
        val key = Pair(last.file, last.span)
        val existing = stmts[key]
        if (existing != null) {
            stmts[key] = Pair(existing.first + 1, existing.second + SmallDuration.fromDuration(time))
        } else {
            stmts[key] = Pair(1, SmallDuration.fromDuration(time))
        }
    }

    // fn before_stmt(&mut self, span: Span, codemap: &CodeMap)
    fun beforeStmt(span: Span, codemap: CodeMap) {
        val now = ProfilerInstant.now()
        addLast(now)
        when (val last = this.last) {
            null -> files.add(codemap)
            else -> {
                if (last.file != codemap.id()) {
                    files.add(codemap)
                }
            }
        }
        this.last = Last(
            file = codemap.id(),
            span = span,
            start = now,
        )
    }

    // fn finish(&self) -> crate::Result<StmtProfileData>
    fun finish(): StmtProfileData {
        val now = ProfilerInstant.now()
        val data = StmtProfileState().also {
            it.files = this.files
            it.stmts = this.stmts.toMutableMap()
            it.last = this.last
        }
        data.addLast(now)

        val resultStmts = mutableMapOf<FileSpan, Pair<Int, SmallDuration>>()
        for ((key, v) in data.stmts) {
            val (fileId, span) = key
            val file = data.files.get(fileId)
                ?: error("no file corresponding to file id")
            resultStmts[FileSpan(file = file, span = span)] = v
        }
        return StmtProfileData(stmts = resultStmts)
    }
}

/** Result of running statement or coverage profiler. */
// #[derive(Clone, Debug, Default, PartialEq)]
// pub(crate) struct StmtProfileData
internal data class StmtProfileData(
    val stmts: MutableMap<FileSpan, Pair<Int, SmallDuration>> = mutableMapOf(),
) {
    // pub(crate) fn write_to_string(&self) -> String
    fun writeToString(): String {
        data class Item(
            val span: FileSpan,
            val time: SmallDuration,
            val count: Int,
        )

        val items = mutableListOf<Item>()
        var totalTime = SmallDuration.default()
        var totalCount = 0
        for ((fileSpan, value) in stmts) {
            val (count, time) = value
            // EMPTY represents the first time special-case
            if (fileSpan.file.id() != CodeMapId.EMPTY) {
                totalTime += time
                totalCount += count
                items.add(Item(span = fileSpan, time = time, count = count))
            }
        }

        items.sortWith(
            compareByDescending<Item> { it.time }
                .thenByDescending { it.count }
                .thenBy { it.span }
        )

        val csv = CsvWriter(listOf("File", "Span", "Duration(s)", "Count"))
        csv.writeValue("TOTAL")
        csv.writeValue("")
        csv.writeValue(totalTime)
        csv.writeValue(totalCount)
        csv.finishRow()

        for (x in items) {
            csv.writeValue(x.span.file.filename)
            csv.writeDisplay(x.span.resolveSpan())
            csv.writeValue(x.time)
            csv.writeValue(x.count)
            csv.finishRow()
        }

        return csv.finish()
    }

    // pub(crate) fn write_coverage(&self) -> String
    fun writeCoverage(): String {
        val sb = StringBuilder()
        val keys = stmts.keys
            .filter { it.file.id() != CodeMapId.EMPTY }
            .map { it.resolve() }
            .sorted()
        for (key in keys) {
            sb.appendLine(key.toString())
        }
        return sb.toString()
    }

    // fn coverage(&self) -> HashSet<ResolvedFileSpan>
    fun coverage(): MutableSet<ResolvedFileSpan> =
        stmts.keys
            .filter { it.file.id() != CodeMapId.EMPTY }
            .map { it.resolve() }
            .toMutableSet()

    companion object {
        // fn merge(profiles: &[&StmtProfileData]) -> StmtProfileData
        fun merge(profiles: List<StmtProfileData>): StmtProfileData {
            val result = StmtProfileData()
            for (profile in profiles) {
                for ((fileSpan, value) in profile.stmts) {
                    val (count, time) = value
                    val existing = result.stmts[fileSpan]
                    if (existing != null) {
                        result.stmts[fileSpan] = Pair(existing.first + count, existing.second + time)
                    } else {
                        result.stmts[fileSpan] = Pair(count, time)
                    }
                }
            }
            return result
        }
    }
}

// pub(crate) struct StmtProfile
// Box because when profiling is not enabled, we want this to be small and cheap
internal class StmtProfile private constructor(
    private var state: StmtProfileState?,
) {
    companion object {
        // pub(crate) fn new() -> Self
        fun new(): StmtProfile = StmtProfile(null)
    }

    // pub(crate) fn enable(&mut self)
    fun enable() {
        state = StmtProfileState.new()
    }

    // pub(crate) fn before_stmt(&mut self, span: FileSpanRef)
    fun beforeStmt(span: FileSpanRef) {
        state?.beforeStmt(span.span, span.file)
    }

    // pub(crate) fn gen(&self) -> crate::Result<ProfileData>
    fun gen(): ProfileData {
        val data = state ?: throw StmtProfileError.NotEnabled
        return ProfileData(
            profile = ProfileDataImpl.Statement(data.finish()),
        )
    }

    // pub(crate) fn coverage(&self) -> crate::Result<HashSet<ResolvedFileSpan>>
    fun coverage(): MutableSet<ResolvedFileSpan> {
        val data = state ?: throw StmtProfileError.NotEnabled
        return data.finish().coverage()
    }

    // pub(crate) fn gen_coverage(&self) -> crate::Result<ProfileData>
    fun genCoverage(): ProfileData {
        val data = state ?: throw StmtProfileError.NotEnabled
        return ProfileData(
            profile = ProfileDataImpl.Coverage(data.finish()),
        )
    }
}
