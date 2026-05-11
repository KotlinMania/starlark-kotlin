// port-lint: source eval/runtime/profile/stmt.rs
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

import io.github.kotlinmania.starlarksyntax.codemap.CodeMap as CodeMap
import io.github.kotlinmania.starlarksyntax.codemap.CodeMapId as CodeMapId
import io.github.kotlinmania.starlarksyntax.codemap.CodeMaps as CodeMaps
import io.github.kotlinmania.starlarksyntax.codemap.FileSpan as FileSpan
import io.github.kotlinmania.starlarksyntax.codemap.Pos as Pos
import io.github.kotlinmania.starlarksyntax.codemap.ResolvedFileSpan as ResolvedFileSpan
import io.github.kotlinmania.starlarksyntax.codemap.Span as Span
import io.github.kotlinmania.starlarksyntax.codemap.FileSpanRef as FileSpanRef
import io.github.kotlinmania.starlark.eval.runtime.SmallDuration
import io.github.kotlinmania.starlark.eval.runtime.profile.csv.CsvWriter
import io.github.kotlinmania.starlark.eval.runtime.profile.data.ProfileData
import io.github.kotlinmania.starlark.eval.runtime.profile.data.ProfileDataImpl
import io.github.kotlinmania.starlark.eval.runtime.profile.mode.ProfileMode

internal object StmtProfilerType : ProfilerType<StmtProfileData> {
    override val profileMode: ProfileMode = ProfileMode.Statement

    override fun dataFromGeneric(profileData: ProfileDataImpl): StmtProfileData? =
        when (profileData) {
            is ProfileDataImpl.Statement -> profileData.data
            else -> null
        }

    override fun dataToGeneric(data: StmtProfileData): ProfileDataImpl =
        ProfileDataImpl.Statement(data)

    override fun mergeProfilesImpl(profiles: List<StmtProfileData>): Result<StmtProfileData> =
        Result.success(StmtProfileData.merge(profiles))
}

internal object CoverageProfileType : ProfilerType<StmtProfileData> {
    override val profileMode: ProfileMode = ProfileMode.Coverage

    override fun dataFromGeneric(profileData: ProfileDataImpl): StmtProfileData? =
        when (profileData) {
            is ProfileDataImpl.Coverage -> profileData.data
            else -> null
        }

    override fun dataToGeneric(data: StmtProfileData): ProfileDataImpl =
        ProfileDataImpl.Coverage(data)

    override fun mergeProfilesImpl(profiles: List<StmtProfileData>): Result<StmtProfileData> =
        Result.success(StmtProfileData.merge(profiles))
}

internal sealed class StmtProfileError : Exception() {
    data object NotEnabled : StmtProfileError() {
        override val message: String get() = "Statement or coverage profiling is not enabled"
    }
}

private data class Last(
    val file: CodeMapId,
    val span: Span,
    val start: ProfilerInstant,
)

private class StmtProfileState {
    var files: CodeMaps = CodeMaps()
    var stmts: MutableMap<Pair<CodeMapId, Span>, Pair<Int, SmallDuration>> = mutableMapOf()
    var last: Last? = null

    companion object {
        fun new(): StmtProfileState = StmtProfileState()
    }

    // Add the data from lastSpan into the entries
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
internal data class StmtProfileData(
    val stmts: MutableMap<FileSpan, Pair<Int, SmallDuration>> = mutableMapOf(),
) {
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
                totalTime = totalTime + time
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

    fun coverage(): MutableSet<ResolvedFileSpan> =
        stmts.keys
            .filter { it.file.id() != CodeMapId.EMPTY }
            .map { it.resolve() }
            .toMutableSet()

    companion object {
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

// Boxed because when profiling is not enabled, we want this to be small and cheap
internal class StmtProfile private constructor(
    private var state: StmtProfileState?,
) {
    companion object {
        fun new(): StmtProfile = StmtProfile(null)
    }

    fun enable() {
        state = StmtProfileState.new()
    }

    fun beforeStmt(span: FileSpanRef) {
        state?.beforeStmt(span.span, span.file)
    }

    fun gen(): ProfileData {
        val data = state ?: throw StmtProfileError.NotEnabled
        return ProfileData(
            profile = ProfileDataImpl.Statement(data.finish()),
        )
    }

    fun coverage(): MutableSet<ResolvedFileSpan> {
        val data = state ?: throw StmtProfileError.NotEnabled
        return data.finish().coverage()
    }

    fun genCoverage(): ProfileData {
        val data = state ?: throw StmtProfileError.NotEnabled
        return ProfileData(
            profile = ProfileDataImpl.Coverage(data.finish()),
        )
    }
}

// --- Tests ---

internal fun testCoverage() {
    // Test requires full evaluator infrastructure (Module, Evaluator, AstModule, etc.)
    // which depends on many other modules. The test logic is preserved here
    // for when those dependencies are fully ported.
    /*
    Module.withTempHeap { module ->
        val eval = Evaluator(module)

        val ast = AstModule.parse(
            "cov.star",
            """
def xx(x):
    return noop(x)

xx(*[1])
xx(*[2])
""",
            Dialect.AllOptionsInternal,
        ).getOrThrow()
        eval.enableProfile(ProfileMode.Coverage)
        val globals = GlobalsBuilder.standard()
        testFunctions(globals)
        eval.evalModule(ast, globals.build())

        val coverage = eval.coverage()
            .map { it.toString() }
            .sorted()
        check(
            coverage == listOf(
                "cov.star:2:1-5:1",
                "cov.star:3:5-19",
                "cov.star:5:1-9",
                "cov.star:6:1-9",
            )
        )
    }
    */
}

internal fun testEmpty() {
    val a = StmtProfile.new()
    a.enable()
    val data = a.gen()
    data.genCsv()
}

internal fun testMerge() {
    val x = CodeMap.new("x.star", "def a(): pass")
    val y = CodeMap.new("y.star", "def b(): pass")
    val z = CodeMap.new("z.star", "def c(): pass")

    val allFiles = CodeMaps()
    allFiles.add(x)
    allFiles.add(y)
    allFiles.add(z)

    val a = StmtProfile.new()
    a.enable()
    a.beforeStmt(FileSpanRef(
        file = x,
        span = Span(Pos(1), Pos(2)),
    ))
    a.beforeStmt(FileSpanRef(
        file = y,
        span = Span(Pos(2), Pos(4)),
    ))
    val aData = a.gen()

    val b = StmtProfile.new()
    b.enable()
    b.beforeStmt(FileSpanRef(
        file = y,
        span = Span(Pos(2), Pos(4)),
    ))
    b.beforeStmt(FileSpanRef(
        file = z,
        span = Span(Pos(3), Pos(5)),
    ))
    val bData = b.gen()

    val merged = ProfileData.merge(listOf(aData, bData)).profile
    check(merged is ProfileDataImpl.Statement)
    val mergedData = (merged).data

    val expected = StmtProfileData(
        stmts = mutableMapOf(
            FileSpan(file = x, span = Span(Pos(1), Pos(2))) to
                Pair(1, SmallDuration.fromMillis(ProfilerInstant.TEST_TICK_MILLIS.toULong())),
            FileSpan(file = y, span = Span(Pos(2), Pos(4))) to
                Pair(2, SmallDuration.fromMillis((ProfilerInstant.TEST_TICK_MILLIS * 2).toULong())),
            FileSpan(file = z, span = Span(Pos(3), Pos(5))) to
                Pair(1, SmallDuration.fromMillis(ProfilerInstant.TEST_TICK_MILLIS.toULong())),
        ),
    )
    check(mergedData == expected)
}
