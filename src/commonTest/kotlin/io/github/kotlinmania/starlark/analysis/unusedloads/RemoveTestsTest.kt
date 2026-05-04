// port-lint: source analysis/unusedLoads/removeTests.rs
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

/** Tests for unused load removal using golden file comparison. */
class RemoveTestsTest {

    private fun testRemove(name: String, program: String) {
        val trimmed = program.trim()

        val out = buildString {
            appendLine("Program:")
            appendLine(trimmed)
            appendLine()

            val removed = removeUnusedLoads(name, trimmed).getOrThrow()
            if (removed == null) {
                appendLine("No unused loads")
            } else {
                appendLine("Removed unused loads:")
                appendLine(removed)
            }
        }

        goldenTestTemplate(
            "src/analysis/unused_loads/remove/$name.golden",
            out,
        )
    }

    @Test
    fun testRemoveFirstOfTwo() {
        testRemove(
            "remove_first_of_two",
            """
load("foo", "x", "y")
print(y)
"""
        )
    }

    @Test
    fun testRemoveSecondOfTwo() {
        testRemove(
            "remove_second_of_two",
            """
load("foo", "x", "y")
print(x)
"""
        )
    }

    @Test
    fun testRemoveAll() {
        testRemove(
            "remove_all",
            """
load("foo", "x", "y")
print("test")
"""
        )
    }
}
