// port-lint: source analysis/unusedLoads/find_tests.rs
package io.github.kotlinmania.starlark.analysis.unusedloads

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

import io.github.kotlinmania.starlark.goldentesttemplate.goldenTestTemplate
import kotlin.test.Test

/** Tests for unused load finding using golden file comparison. */
class FindTestsTest {

    private fun testUnusedLoads(name: String, program: String) {
        val trimmed = program.trim()

        val out = buildString {
            appendLine("Program:")
            appendLine(trimmed)
            appendLine()

            val (codemap, unusedLoads) = findUnusedLoads(name, trimmed).getOrThrow()
            if (unusedLoads.isEmpty()) {
                appendLine("No unused loads")
            } else {
                appendLine("Unused loads:")
                for (load in unusedLoads) {
                    val spans = if (load.allUnused()) {
                        listOf(load.load.span)
                    } else {
                        load.unusedArgs.map { it.span() }
                    }
                    for (span in spans) {
                        appendLine()

                        val fileSpan = codemap.value.fileSpan(span)
                        // spanDisplay is from starlarkSyntax (not yet ported);
                        appendLine("Unused load at ${fileSpan.resolveSpan()}")
                    }
                }
            }
        }

        goldenTestTemplate(
            "src/analysis/unused_loads/find/$name.golden",
            out,
        )
    }

    @Test
    fun testSimple() {
        testUnusedLoads(
            "simple",
            """
load("foo", "x")
"""
        )
    }

    @Test
    fun testUsedInTopLevelAssignment() {
        testUnusedLoads(
            "used_in_top_level_assignment",
            """
load("foo", "x")
y = x
"""
        )
    }

    @Test
    fun testOneOfTwoUnused() {
        testUnusedLoads(
            "one_of_two_unused",
            """
load("foo", "x", "y")
print(x)
"""
        )
    }

    @Test
    fun testWithRename() {
        testUnusedLoads(
            "with_rename",
            """
load("foo", x="y", z="w")
y = z
"""
        )
    }

    @Test
    fun testUsedInTypeExpr() {
        testUnusedLoads(
            "used_in_type_expr",
            """
load("foo", "T")
load("bar", "U")

y: T = 1

def f(x: U):
    pass
"""
        )
    }

    @Test
    fun testUnusedAnnotationOnArg() {
        testUnusedLoads(
            "unused_annotation_on_arg",
            """
load("foo",
    "T", # @unused
    "U",
     )
"""
        )
    }
}
