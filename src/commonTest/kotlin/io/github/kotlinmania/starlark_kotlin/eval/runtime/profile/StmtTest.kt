// port-lint: tests src/eval/runtime/profile/stmt.rs
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

import io.github.kotlinmania.starlark.assert.testFunctions
import io.github.kotlinmania.starlark_kotlin.codemap.CodeMap
import io.github.kotlinmania.starlark_kotlin.codemap.CodeMaps
import io.github.kotlinmania.starlark_kotlin.codemap.FileSpan
import io.github.kotlinmania.starlark_kotlin.codemap.FileSpanRef
import io.github.kotlinmania.starlark_kotlin.codemap.Pos
import io.github.kotlinmania.starlark_kotlin.codemap.Span
import io.github.kotlinmania.starlark_kotlin.environment.GlobalsBuilder
import io.github.kotlinmania.starlark_kotlin.environment.Module
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Evaluator
import io.github.kotlinmania.starlark_kotlin.eval.evalModule
import io.github.kotlinmania.starlark_kotlin.syntax.AstModule
import io.github.kotlinmania.starlark_kotlin.syntax.dialect.Dialect
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.data.ProfileData
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.data.ProfileDataImpl
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.mode.ProfileMode
import io.github.kotlinmania.starlark_kotlin.eval.runtime.SmallDuration
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class StmtTest {
    @BeforeTest
    fun setUp() {
        ProfilerInstant.testMode = true
        ProfilerInstant.resetTestCounter()
    }

    @AfterTest
    fun tearDown() {
        ProfilerInstant.testMode = false
    }

    @Test
    fun testCoverage() {
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
            assertEquals(
                listOf(
                    "cov.star:2:1-5:1",
                    "cov.star:3:5-19",
                    "cov.star:5:1-9",
                    "cov.star:6:1-9",
                ),
                coverage,
            )
        }
    }

    @Test
    fun testEmpty() {
        val profile = StmtProfile.new()
        profile.enable()
        val data = profile.gen()
        data.genCsv()
    }

    @Test
    fun testMerge() {
        val x = CodeMap("x.star", "def a(): pass")
        val y = CodeMap("y.star", "def b(): pass")
        val z = CodeMap("z.star", "def c(): pass")

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
        assertEquals(true, merged is ProfileDataImpl.Statement)
        val mergedData = (merged as ProfileDataImpl.Statement).data
        assertEquals(3, mergedData.stmts.size)

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
        assertEquals(expected, mergedData)
    }
}
