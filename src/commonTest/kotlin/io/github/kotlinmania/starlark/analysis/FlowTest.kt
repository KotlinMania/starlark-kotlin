// port-lint: tests src/analysis/flow.rs
package io.github.kotlinmania.starlark.analysis

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

import io.github.kotlinmania.starlark.syntax.AstModule
import io.github.kotlinmania.starlark.syntax.dialect.Dialect
import kotlin.test.Test
import kotlin.test.assertEquals

class FlowTest {
    private fun module(x: String): AstModule = AstModule.parse("X", x, Dialect.AllOptionsInternal).getOrThrow()

    @Test
    fun testLintReturns() {
        val m =
            module(
                """
def no1() -> str:
    pass
def no2():
    if x:
        return 1
def no3():
    if x:
        return
    return 1
def ok():
    def no4() -> int:
        no4()
    pass
def yes1():
    pass
def yes2() -> str:
    yes1()
    if x:
        return "x"
    else:
        return "y"
def yes3():
    if x:
        return
    pass
def yes4() -> str:
    fail("die")
""",
            )
        val res = mutableListOf<LintT<FlowIssue>>()
        stmt(m.codemap(), m.statement(), res)
        assertEquals(
            listOf("no1", "no2", "no3", "no4"),
            res.map { it.problem.about() },
        )
    }

    @Test
    fun testLintUnreachable() {
        val m =
            module(
                """
def test():
    return 1
    no1
def test2():
    if x:
        return 1
    yes
def test3():
    if x:
        return
    else:
        return
    no2
    ignored
def test4():
    for x in xs:
        continue
        no3
    reachable
def test5():
    for x in xs:
        if test:
            continue
        reachable
        return
    reachable
def test6():
    fail(1)
    no4
def f():
    def g():
        return 5
    reachable
""",
            )
        val res = mutableListOf<LintT<FlowIssue>>()
        reachable(m.codemap(), m.statement(), res)
        assertEquals(
            listOf("no1", "no2", "no3", "no4"),
            res.map { it.problem.about() },
        )
    }

    @Test
    fun testLintRedundant() {
        val m =
            module(
                """
def test(): # 1
    foo
    return # bad: 3
def test2(): # 4
    return
    foo
def test3(): # 7
    if x:
        return # bad: 9
    else:
        y + 1
def test4(): # 12
    def test5():
        for x in xs:
            test
            if x:
                return
            else:
                continue # bad: 19
    test5()
def test6():
    if x:
        return
    y + 1
def test7():
    for x in xs:
        if x:
            continue
        return
""",
            )
        val res = mutableListOf<LintT<FlowIssue>>()
        redundant(m.codemap(), m.statement(), res)
        assertEquals(
            listOf(3, 9, 19),
            res.map {
                it.location
                    .resolveSpan()
                    .begin.line
            },
        )
    }

    @Test
    fun testLintMisplacedLoad() {
        val m =
            module(
                """
load("a", "a")
""${'"'}
this is some comment
over multiple lines
""${'"'}
load("b", "b")

x = 1
load("c", "b")
""",
            )
        val res = mutableListOf<LintT<FlowIssue>>()
        misplacedLoad(m.codemap(), m.statement(), res)
        assertEquals(1, res.size)
    }

    @Test
    fun testLintNoEffect() {
        val src = """
""${'"'}
a doc block
""${'"'}
load("b", "b")

x = 1
7 ## BAD
def foo():
    [18] ## BAD
1 + 2
"""

        val m = module(src)
        val bad =
            src
                .lines()
                .withIndex()
                .filter { (_, line) -> "## BAD" in line }
                .map { (i, _) -> i }
        val res = mutableListOf<LintT<FlowIssue>>()
        noEffect(m.codemap(), m.statement(), res)
        assertEquals(
            bad,
            res.map {
                it.location
                    .resolveSpan()
                    .begin.line
            },
        )
    }
}
