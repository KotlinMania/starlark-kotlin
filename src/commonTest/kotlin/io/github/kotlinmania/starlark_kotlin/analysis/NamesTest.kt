// port-lint: source src/analysis/names.rs (tests)
package io.github.kotlinmania.starlark_kotlin.analysis

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

import io.github.kotlinmania.starlark_kotlin.syntax.AstModule
import io.github.kotlinmania.starlark_kotlin.syntax.Dialect
import kotlin.test.Test
import kotlin.test.assertEquals

class NamesTest {
    private fun module(x: String): AstModule =
        AstModule.parse("X", x, Dialect.AllOptionsInternal)

    private fun lint(m: AstModule, globals: Set<String>? = null): List<String> {
        val res = namesLint(m, globals)
        return res.map { it.problem.about() }.sorted()
    }

    @Test
    fun testLintUnused() {
        // unused kind arg is fine if it starts with _
        // allowed to ignore things which are
        val m = module(
            """
load("test", "no1", "a")
b = a
_c = 18
_no2 = 17
d = _c
_e = 9
_no4 = 1
def magic(no3, f, _allowed):
    _ignore = f
    _no4 = f # shadows out _no4, which is thus unused
    no5 = 10
    (a, _) = f()
    (a2, _ignored) = f()
    f.go(_e, a, a2, _no4)
def uses_h():
    _h
_h = []
def dots(f, g):
    (d_a, d_b, d_c, d_d, d_e, d_f, d_g, d_h, d_i, d_j) = (20, 21, 22, 23, 24, 25, 26, 27, 28, 29)
    # Make sure that expressions on dotted accesses are accounted for.
    # Ignore that the integers referenced aren't functions. They're just names.
    f.foo[d_a].bar
    f.foo[d_b].bar[d_c]
    f.baz(g(["ignore", d_d]))
    f.foobar.baz(d_e, [d_f], d_g([d_h]))
    d_i(d_j).blah
    pass
def _no6(): pass
def foo():
    array = [1,2,3]
    x = 1
    array[x] = 9
    return array
"""
        )
        val res = lint(m)
        assertEquals(listOf("_no2", "_no4", "_no6", "no1", "no3", "no5"), res)
    }

    @Test
    fun testLintDuplicateAssign() {
        val m = module(
            """
load("test", "no1", "a")
no1 = 12
b = a
no2 = 8
no2 = 9
c = 8
c += 1
def foo():
    no3 = 1
    no3 = 1
    _ignore = a
    _ignore = a
    if foo2:
        d = 1
    else:
        d = 2
    _use = (no3, d)
no2 = 10
_no4 = 1
_no4 = 2
export = _no4

def capture_e(): print(e)
e = 8
capture_e()
e = 10
capture_e()
"""
        )
        val res = lint(m)
        assertEquals(listOf("_no4", "no1", "no2", "no2", "no3"), res)
    }

    @Test
    fun testLintUnassigned() {
        val m = module(
            """
a = no1
no1 = 9
test = no3()
def foo():
    b = no2 + undefined_variable
    for x in xs:
        no2 = 18 + x + b
        _use = no2
def no3(): pass
def uses_h():
    _h
_h = []
"""
        )
        val res = lint(m)
        assertEquals(listOf("no1", "no2", "no3"), res)
    }

    @Test
    fun testLintUndefined() {
        val globals = setOf("True", "fail")

        val m = module(
            """
load("test", imported = "more")
a = True + imported + no1
def foo():
    fail("test") + no2(t = 3)
def bar(ctx):
    ctx.attrs.dep[fail.index].default_outputs[0]
    ctx[0].default_outputs
"""
        )
        val res = lint(m, globals)
        assertEquals(listOf("no1", "no2"), res)
    }

    @Test
    fun testEarlyFail() {
        val m = module(
            """
def foo(x):
    if x == 1:
        ok1 = 2
    elif x == 3:
        ok1 = 7
    else:
        fail("bad")
    return ok1

def bar(xs):
    for x in xs:
        if x == 1:
            ok2 = 2
        else:
            continue
        print(ok2)
"""
        )
        val res = lint(m)
        assertEquals(emptyList(), res)
    }

    @Test
    fun testAssignForNext() {
        val m = module(
            """
def foo(xs):
    counter = 0
    reached = None
    for x in xs:
        if counter == 7:
            reached = x
        counter += 1
    return reached
"""
        )
        val res = lint(m)
        assertEquals(emptyList(), res)
    }

    @Test
    fun testFlowControl() {
        val m = module(
            """
def foo(b, xs):
    if b:
        no1 = xs
    for x in no1:
        return x
    for no2 in xs:
        print(no2)
    print(no2)
    if b:
        yes = xs
    else:
        yes = []
    return yes
"""
        )
        val res = lint(m)
        assertEquals(listOf("no1", "no2"), res)
    }

    @Test
    fun testLambdaCapture() {
        val m = module(
            """
def foo():
    lam = lambda: print(x)
    x = 1
    lam()
    x = 2
    lam()
"""
        )
        val res = lint(m)
        assertEquals(0, res.size)
    }

    @Test
    fun testGlobalDefinedLater() {
        val m = module(
            """
def foo():
    _bar()
def _bar():
    pass
"""
        )
        val res = lint(m, globals = emptySet())
        assertEquals(0, res.size)
    }
}
