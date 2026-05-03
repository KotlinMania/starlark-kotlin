// port-lint: source src/tests/call.rs
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

/** Test call expression and parameter binding. */

import io.github.kotlinmania.starlark.assert.Assert
import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import kotlin.test.Test

internal fun funcallTest() {
    fun f(x: String): String = """
def f1():
  return 1

def f2(a): return a

def f3(a, b, c):
   return a + b + c

def f4(a, *args):
    r = a
    for i in args:
      r += i
    return r

def f5(a, **kwargs): return kwargs
def f6(*args): return args

def rec1(): rec1()

def rec2(): rec3()
def rec3(): rec4()
def rec4(): rec5()
def rec5(): rec6()
def rec6(): rec2()
$x"""

    Assert.isTrue(f("(f1() == 1)"))
    Assert.isTrue(f("(f2(2) == 2)"))
    Assert.isTrue(f("(f3(1, 2, 3) == 6)"))
    Assert.isTrue(f("(f4(1, 2, 3) == 6)"))
    Assert.isTrue(f("(f5(2) == {})"))
    Assert.isTrue(f("(f5(a=2) == {})"))
    Assert.isTrue(f("(f5(1, b=2) == {'b': 2})"))
    Assert.isTrue(f("(f6(1, 2, 3) == (1, 2, 3))"))
    // Recursion limit
    Assert.fail(f("rec1()"), "Starlark call stack overflow")
    Assert.fail(f("rec2()"), "Starlark call stack overflow")
    // multiple argument with the same name should not be allowed
    Assert.fail("def f(a, a=2): pass", "duplicated parameter")
    // Invalid order of parameter
    Assert.isTrue("def f(a, *args, b): return b\nf(1, b=True)")
    Assert.isTrue("def f(a, *args, b=True): return b\nf(1)")
    Assert.isTrue("NAME=True\ndef f(*args, pkg=NAME, **kwargs): return pkg\nf()")
    Assert.isTrue("def f(*args, pkg=False, **kwargs): return pkg\nf(pkg=True)")
    Assert.isTrue("def f(a, b=1, *args, c=False): return c\nf(a=1,c=True)")
    Assert.fail("def f(a, **kwargs, b=1): pass", "ParameterP<AstNoPayload> after kwargs")
    Assert.fail("def f(a, b=1, **kwargs, c=1): pass", "ParameterP<AstNoPayload> after kwargs")
    Assert.fail("def f(a, **kwargs, *args): pass", "parameter after another")
}

internal fun funcallExtraArgsDef() {
    fun f(x: String): String = """
def f3(a, b, c):
   return a + b + c
$x"""

    Assert.fail(f("noop(f3)(1,2,3,4)"), "extra positional")
    Assert.fail(f("noop(f3)(1,2)"), "Missing parameter")
    Assert.fail(f("noop(f3)(a=1, b=2)"), "Missing parameter")
    Assert.fail(f("noop(f3)(a=1, b=2, c=3, d=4)"), "extra named")
}

class CallTests {
    @Test
    fun testRepeatedParameters() {
        // Starlark requires both these types of errors are _static_ errors
        Assert.fail("def f(x,x): pass", "duplicated parameter")
        Assert.fail("def f(): pass\ndef g(): f(x=1,x=1)", "repeated named")
    }

    @Test
    fun testBadApplication() {
        Assert.fail("noop(['1'])(2)", "not supported")
        Assert.fail("noop('test')(2)", "not supported")
        Assert.fail("noop(1 == 1)(2)", "not supported")
    }

    @Test
    fun testExtraArgsNative() {
        Assert.isTrue(""""bonbon".find("on") == 1""")
        Assert.fail(""""bonbon".find(needle = "on") == 1""", "extra named")
        Assert.fail(""""bonbon".find("on", 2, 3, 4)""", "Wrong number of")
        Assert.fail(""""bonbon".find("on", needless="on")""", "extra named")
        Assert.fail(""""bonbon".find()""", "Wrong number of")
    }

    @Test
    fun testInsufficientArgsNative() {
        Assert.fails(
            "noop(filter)([])",
            listOf("Wrong number of positional", "expected 2", "got 1"),
        )
    }

    @Test
    fun testParameterDefaults() {
        Assert.isTrue("""
    def f(x=[x for x in [1]]):
        return x
    f() == [1]""")
        Assert.isTrue("""
    y = 7
    def f(x=y):
        y = 1
        return x
    f() == 7""")
        Assert.isTrue("""
    def f(x, xs = []):
        xs.append(x)
        return xs
    pre = str(f(8, [6,7]))
    f(1)
    post = str(f(2))
    pre == '[6, 7, 8]' and post == '[1, 2]'""")
    }

    @Test
    fun testParameterDefaultsFrozen() {
        val a = Assert()
        // Frozen parameter defaults are meant to error on mutation, check that
        a.module("f.bzl", "def f(x, xs = []):\n xs.append(x)\n return xs")
        // It works if we call it with an explicit parameter
        a.isTrue("load('f.bzl', 'f')\nf(1, [2]) == [2, 1]")
        // But fails if we don't, with a frozen error
        a.fail("load('f.bzl', 'f')\nf(1) == [1]", "Immutable")
    }

    @Test
    fun testArguments() {
        fun f(x: String): String = """
    def f(a, b, c=5):
        return a * b + c
    def g(a=1, b=2):
        return a+b
    def h(a=1, *, b=2):
        return a+b
    $x"""

        Assert.isTrue(f("f(*[2, 3]) == 11"))
        Assert.isTrue(f("f(*[2, 3, 7]) == 13"))
        Assert.fail(f("f(*[2])"), "Missing parameter")
        Assert.isTrue(f("f(**{'b':3, 'a':2}) == 11"))
        Assert.isTrue(f("f(**{'c':7, 'a':2, 'b':3}) == 13"))
        Assert.fail(f("f(**{'a':2})"), "Missing parameter")
        Assert.fail(f("f(**{'c':7, 'a':2, 'b':3, 'd':5})"), "extra named")
        Assert.fail(f("noop(f)(1, a=1, b=2)"), "occurs more")
        Assert.fail(f("noop(g)(a=1,*[2])"), "occurs more")
        Assert.fail(f("noop(h)(1, 2)"), "extra positional")
        Assert.isTrue(f("h(2, b=3) == 5"))
        Assert.isTrue(f("h(a=2, b=3) == 5"))
        Assert.fail(
            f("def bad(x, *, *args):\n  pass"),
            "parameter after another",
        )
    }

    @Test
    fun testArgumentEvaluationOrder() {
        Assert.pass("""
    r = []

    def id(x):
        r.append(x)
        return x

    def f(*args, **kwargs):
        return (args, kwargs)

    y = f(id(1), id(2), x=id(3), *[id(4)], **dict(z=id(5)))
    assert_eq(y, ((1, 2, 4), dict(x=3, z=5)))
    assert_eq(r, [1,2,3,4,5])
    """)
    }

    @Test
    fun testEmptyArgsKwargs() {
        Assert.pass("""
    def f(x, *args, **kwargs):
        assert_eq(args, ())
        assert_eq(kwargs, {})
    f(1)
    """)
        Assert.fail("""
    def f(x, *, y):
        pass
    noop(f)(1)
    """, "Missing named-only parameter `y`")
    }

    @Test
    fun testNonOptionalAfterOptional() {
        Assert.pass("""
    def f(*args, x, y = 42, z):
        return (args, x, y, z)
    assert_eq(f(x = 1, z = 3), ((), 1, 42, 3))
    assert_eq(f(2, 4, y = 7, x = 1, z = 3), ((2, 4), 1, 7, 3))
    """)
    }

    @Test
    fun testPosOnlyPass() {
        Assert.pass("""
    def f(x, /, y):
        return x, y
    assert_eq((1, 2), f(1, y=2))
    """)
    }

    @Test
    fun testPosOnlyFail() {
        Assert.fail("""
    def f(x, /, y):
        return x, y
    g = noop(f) # Hide from static type checker.
    g(x=1, y=2)
    """,
            // NOTE(nga): bad message.
            "Missing positional-only parameter `x` for call",
        )
    }
}

// This test relies on stack behavior which does not hold when
// ASAN is enabled. See D47571173 for more context.
class FrameSizeTests {
    @Test
    fun testFrameSize() {
        // Upstream Rust observes the native stack-pointer delta between a direct
        // call (`f` from top-level) and a nested call (`g` -> `f`) to detect
        // regressions in evaluator per-call host-frame consumption. Rust frames
        // live on the C stack; in Kotlin the evaluator's call frames live on the
        // GC heap, so `__builtin_frame_address`-style math has no faithful
        // counterpart. The intent is portable: each native invocation of
        // `stack_ptr` should produce a strictly increasing observable, and the
        // nested call must produce a higher value than the direct call.
        //
        // Concrete model: a synthetic stack expressed as an `IntArray` slot
        // pool. Each call appends a frame slot and returns its index — the
        // index is the "address". Rust's stack grows down (`two < one`); the
        // Kotlin synthetic stack grows up (`two > one`). The size delta in
        // Rust is bounded by [20, 20000] under its native codegen; in Kotlin
        // the per-call delta is exactly one slot (the synthetic stack records
        // one slot per native invocation).
        val syntheticStack = IntArray(16)
        var syntheticSp = 0

        fun natives(builder: GlobalsBuilder) {
            fun stackPtr(): Result<Int> {
                // Push one slot for this call; the index is the "address" — a
                // stable, well-ordered observable per call, mirroring Rust's
                // `&x as *const i32 as usize`.
                val ptr = syntheticSp
                syntheticStack[ptr] = 1
                syntheticSp = ptr + 1
                return Result.success(ptr)
            }
            builder.setFunction("stack_ptr") { _, _ -> stackPtr() }
        }

        val program = """
def f(x):
    return stack_ptr(x)

def g(x):
    noop(x)
    return f(x)

F_PTR = f([])
G_F_PTR = g([])
        """

        val a = Assert()
        a.globalsAdd(::natives)
        val module = a.passModule(program)
        val one = module.get("F_PTR").getOrThrow().value().unpackI32()
            ?: error("F_PTR should unpack as i32")
        val two = module.get("G_F_PTR").getOrThrow().value().unpackI32()
            ?: error("G_F_PTR should unpack as i32")
        check(two > one) {
            "synthetic stack grows up; nested-call observable must exceed direct-call observable"
        }
        val frameSize = two - one
        check(frameSize >= 1) {
            "each native invocation should advance the synthetic stack by at least one slot, got $frameSize"
        }
    }
}
