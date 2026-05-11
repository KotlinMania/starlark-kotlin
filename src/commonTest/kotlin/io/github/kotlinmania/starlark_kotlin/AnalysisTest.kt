// port-lint: source tests:src/analysis.rs
package io.github.kotlinmania.starlark

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

import io.github.kotlinmania.starlark.codemap.Pos
import io.github.kotlinmania.starlark.syntax.AstModule
import io.github.kotlinmania.starlark.syntax.dialect.Dialect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnalysisTest {

    private fun module(x: String): AstModule {
        return AstModule.parse("X", x, Dialect.AllOptionsInternal).getOrThrow()
    }

    @Test
    fun testLintSuppressionsKeywordMatching() {
        val m = module(
            """
def good1() -> str: #starlark-lint-disable missing-return
    pass
def bad1() -> str: # invalid suppression starlark-lint-disable missing-return
    pass
def bad2() -> str: #starlark-lint-disable-also-invalid missing-return
    pass
def good2() -> str:
    pass       # starlark-lint-disable  ,,missing-return, misplaced-load , missing-return ,,
def bad3() -> str:
    pass       # # starlark-lint-disable missing-return # invalid prefix
"""
        )
        val res = m.lint(null)
        assertEquals(3, res.size)
        assertTrue("bad1" in res[0].problem)
        assertTrue("bad2" in res[1].problem)
        assertTrue("bad3" in res[2].problem)
    }

    @Test
    fun testLintSuppressionsFnWithManyIssues() {
        val m = module(
            """
def bad1(items):
    a = all(items)
    b = all({"a": a for a in []})
    c = any(list({}))

# suppressing issues fn-wide doesnt work
# starlark-lint-disable unused-assign, eager-and-inefficient-bool-check
def bad2(items):
    d = all(items)
    e = all({"e": e for e in []})
    f = any(list({}))

def good1(items):
    g = all(items)  # starlark-lint-disable unused-assign
    # starlark-lint-disable unused-assign
    # starlark-lint-disable eager-and-inefficient-bool-check
    h = all({"h": h for h in []})
    # starlark-lint-disable inefficient-bool-check
    i = any(list({})) # starlark-lint-disable unused-assign
"""
        )
        val res = m.lint(null)
        assertEquals(10, res.size)
        assertTrue("Unused assignment of `a`" in res[0].problem)
        assertTrue("`b`" in res[1].problem)
        assertTrue("`c`" in res[2].problem)
        assertTrue("`d`" in res[3].problem)
        assertTrue("`e`" in res[4].problem)
        assertTrue("`f`" in res[5].problem)
        assertTrue("all({\"a\": a for a in []})" in res[6].original)
        assertTrue("`any(list({}))` allocates a new list" in res[7].problem)
        assertTrue("all({\"e\": e for e in []})" in res[8].original)
        assertTrue("`any(list({}))` allocates a new list" in res[9].problem)
    }

    @Test
    fun testLintSuppressionsPrecedingWhitespace() {
        val m = module(
            """
def bad():
    a = 1

def good():
    # starlark-lint-disable unused-assign
    # extra comment
    b = 1
"""
        )
        val res = m.lint(null)
        assertEquals(1, res.size)
        assertTrue("Unused assignment of `a`" in res[0].problem)
    }

    @Test
    fun testLintSuppressionsWithSpaceSeparator() {
        val m = module(
            """
def good():
    #    starlark-lint-disable unused-assign FIXME
    b = 1
"""
        )
        val res = m.lint(null)
        assertTrue(res.isEmpty())
    }

    @Test
    fun testLintSuppressionsMultilineSpan() {
        val m = module(
            """
def bad() -> str:
    pass
def good() -> str:
    pass       # starlark-lint-disable missing-return
"""
        )
        val res = m.lint(null)
        assertEquals(1, res.size)
        assertTrue("bad" in res[0].problem)
    }

    @Test
    fun testLintSuppressionsSmallSpan() {
        val m = module(
            """
load("@cell//t:rust_library.bzl", "rust_library") # starlark-lint-disable unused-load

def bad() -> str:
    pass
"""
        )
        val res = m.lint(null)
        assertEquals(1, res.size)
        assertTrue("bad" in res[0].problem)
    }

    @Test
    fun testLintSuppressionsData() {
        val m = module(
            """
{no3: 1, no4: 2, yes: 3, no3: 3}

# starlark-lint-disable duplicate-key
{no3: 1, no4: 2, yes: 3, no3: 3}

{no3: 1, no4: 2, yes: 3, no3: 3} # starlark-lint-disable duplicate-key

{   no3: 1,
    no4: 2,
    yes: 3,
    # inline data suppression of one key doesnt work
    # starlark-lint-disable duplicate-key
    no3: 3
}

{   no3: 1,     # starlark-lint-disable duplicate-key
    no4: 2,
    yes: 3,
    # each offender has to be disabled
    # starlark-lint-disable duplicate-key
    no3: 3
}

# starlark-lint-disable duplicate-key
{   no3: 1,
    no4: 2,
    yes: 3,
    no3: 3
}
"""
        )
        val res = m.lint(null)
        assertEquals(2, res.size)
        assertEquals(Pos.new(2u), res[0].location.span.begin)
        assertEquals(Pos.new(183u), res[1].location.span.begin)
    }

    @Test
    fun testLintSuppressionsLineBefore() {
        val m = module(
            """
# starlark-lint-disable unused-load
load("@cell//buck/lib:rust_library.bzl", "rust_library")
load("@cell//buck/lib:rust_binary.bzl", "rust_binary")

def bad1() -> str:
    pass

# starlark-lint-disable missing-return
def good1() -> str:
    pass

# starlark-lint-disable missing-return
# must not be on the last line of a block of comments
def good2() -> str:
    pass

# suppressions accumulate in a block of comments,
# starlark-lint-disable missing-return, unreachable
# and you can put other comments between
# starlark-lint-disable unused-load
def good3() -> str:
    pass
"""
        )
        val res = m.lint(null)
        assertEquals(2, res.size)
        assertTrue("bad1" in res[0].problem)
        assertTrue("rust_binary" in res[1].problem)
    }

    @Test
    fun testLintSuppressionsLineBeforeWindowsNewlines() {
        val src = module(
            "# starlark-lint-disable unused-load\r\n" +
                "load('@cell//buck/lib:rust_library.bzl', 'rust_library')"
        )
        val res = src.lint(null)
        assertTrue(res.isEmpty())
    }

    @Test
    fun testLintSuppressionsInsideFn() {
        val m = module(
            """
def bad1() -> str:
    pass

def good1() -> str:
    # starlark-lint-disable missing-return
    pass

def good2() -> str:
    pass # starlark-lint-disable missing-return
"""
        )
        val res = m.lint(null)
        assertEquals(1, res.size)
        assertTrue("bad1" in res[0].problem)
    }
}
