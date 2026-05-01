// port-lint: source src/tests/go.rs
package io.github.kotlinmania.starlark.tests

/*
 * Copyright 2018 The Starlark in Rust Authors.
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

/** Run Go implementation tests. */

import io.github.kotlinmania.starlark.assert.Assert
import io.github.kotlinmania.starlark.assert.conformanceExcept
import io.github.kotlinmania.starlark.assert.conformance
import kotlin.test.Test

/** Load a test case file from the testcases directory. */
private fun testCase(name: String): String {
    return io.github.kotlinmania.starlark.tests.loadTestResource("eval/go/$name")
}

/**
 * Load a test resource file by path relative to the testcases directory.
 * Platform-specific resource loading is provided by `expect`/`actual` declarations.
 */
internal expect fun loadTestResource(path: String): String

private fun ignoreBadLines(x: String, bad: List<String>): String {
    return x.lines()
        .filter { line -> bad.none { b -> line.contains(b) } }
        .joinToString("\n")
}

class GoTests {
    @Test
    fun testGo() {
        // The data for these tests was taken from
        // https://github.com/google/starlark-go/blob/e81fc95f7bd5bb1495fe69f27c1a99fcc77caa48/starlark/testdata/

        val assert = Assert()
        // NOTE(nga): fix and enable.
        assert.disableStaticTypechecking()
        assert.conformanceExcept(
            testCase("assign.star"),
            listOf(
                "hasfields()", // Not sure what this is, but we don't support
            ),
        )
        // Skip benchmark.star, for benchmarking not testing
        assert.conformance(testCase("bool.star"))
        assert.conformance(ignoreBadLines(
            testCase("builtin.star"),
            listOf(
                "[] not in {123: \"\"}", // We disagree, see testNotInUnhashable
                // Set, unsupported
                "set(",
                "(myset)",
                "(myset,",
                // Has fields, unsupported
                "hasfields()",
                "(hf)",
                "(hf,",
                "(hf,",
                "hf.",
                "(setfield,",
                // testInRange
                "True in range(3)",
                "\"one\" in range(10)",
                // We added copy, which throws off the assert
                "dir({})[:3]",
                "dir([])[:3]",
                // We do not support range to `i32::MAX` on 32 bit.
                "range(0x7fffffff)",
            ),
        ))
        assert.conformance(testCase("control.star"))
        assert.conformanceExcept(
            ignoreBadLines(
                testCase("dict.star"),
                listOf(
                    "unknown binary op: dict \\\\+ dict",   // We support {} + {}
                    "cannot insert into frozen hash table", // We don't actually have freeze
                    "cannot clear frozen hash table",
                    "asserts.eq(a, 1)", // End of the test above
                    "asserts.eq(x, {1: 2, 2: 4, 0: 2})",
                    "x9a", // Starlark spec does not allow test list in index expression
                ),
            ),
            listOf(
                "Verify position of an \"unhashable key\"", // FIXME: We should give a better error message
                "Verify position of a \"duplicate key\"",   // FIXME: Give a better line number
                "Verify position of an \"unhashable key\"", // FIXME: we should do better
            ),
        )
        assert.conformanceExcept(
            ignoreBadLines(
                testCase("set.star"),
                listOf(
                    "cannot insert into frozen hash table", // We don't actually have freeze
                    "cannot clear frozen hash table",
                    "discard: cannot delete from frozen hash table",
                ),
            ),
            emptyList(),
        )

        assert.conformance(ignoreBadLines(
            testCase("float.star"),
            listOf(
                // int's outside our range
                "1229999999999999973",
                "9223372036854775808",
                "1000000000000000000",
                "1230000000000000049",
                "9223372036854775807",
                "p53",
                "maxint64",
                "int too large to convert to float",
                "int(1e100)",
                "1000000 * 1000000 * 1000000",
                "int overflow in starlark-rust",
            ),
        ))
        assert.conformance(ignoreBadLines(
            testCase("function.star"),
            listOf(
                "eq(str",             // We render function names differently
                "frozen list",        // Our freeze does nothing
                "called recursively", // We allow recursion
                "hf",                 // We don't support hasfield
            ),
        ))
        // Skip int.star, a lot of bit mask stuff, floats and int's outside our range
        // Skip list.star, our strings disagree about whether they are lists of codepoints or lists of 1-char strings
        assert.conformanceExcept(
            ignoreBadLines(
                testCase("misc.star"),
                listOf(
                    "'<built-in function freeze>'", // Different display of functions
                ),
            ),
            listOf(
                "cyclic data structures", // We overflow on some of these
            ),
        )
        // Skip module.star, we don't support modules
        // Skip paths.star, a path support library, not tests
        // Skip recursion.star, we don't support `while` loops, which is what this mostly tests
        // Skip set.star, we don't support set
        // Skip string.star, our String's are fundamentally different
        assert.conformance(ignoreBadLines(
            testCase("tuple.star"),
            listOf(
                "1000000 * 1000000", // Some tests check that you can't create too large tuples, but that's not principled, so we allow it
                                     // But it takes approximately forever, so doing it is a bad idea.
            ),
        ))
    }

    @Test
    fun testInRange() {
        // Go Starlark considers this a type error (I think that is a mistake)
        Assert.allTrue(
            """
    not (True in range(3))
    not ("one" in range(10))
    """,
        )
    }
}
