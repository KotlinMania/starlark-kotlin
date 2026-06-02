// port-lint: tests tests/def.rs
package io.github.kotlinmania.starlark.tests

/*
 * Copyright 2018 The Starlark in Rust Authors.
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

/** Test for `def` and `lambda`. */

import io.github.kotlinmania.starlark.assert.Assert
import io.github.kotlinmania.starlark.environment.Module
import io.github.kotlinmania.starlark.eval.evalFunction
import io.github.kotlinmania.starlark.eval.runtime.Evaluator

// #[test]
// fn test_lambda()
internal fun testLambda() {
    Assert.isTrue("(lambda x: x)(1) == 1")
    Assert.isTrue("(lambda x: (x == 1))(1)")
    Assert.isTrue(
        """
xs = [lambda x: x + y for y in [1,2,3]]
ys = [lambda x: x + y for y in [4,5,6]]
[xs[1](0),ys[1](0)] == [3,6]""",
    )
}

// #[test]
// fn test_frozen_lambda()
internal fun testFrozenLambda() {
    val a = Assert()
    a.module(
        "lam",
        """
def my_func(a):
    return lambda b: a + b
add18 = my_func(18)
# This test used to fail if a GC happened, so add one
garbage_collect()
""",
    )
    a.pass(
        """
load("lam", "add18")
assert_eq(add18(24), 42)
""",
    )
}

// #[test]
// fn test_nested_def_1()
internal fun testNestedDef1() {
    Assert.isTrue(
        """
def foo(x):
    def bar(y):
        return x+y
    return bar(x)
foo(8) == 16""",
    )
}

// #[test]
// fn test_nested_def_2()
internal fun testNestedDef2() {
    Assert.isTrue(
        """
def squarer():
    x = [0]
    def f():
        x[0] += 1
        return x[0]*x[0]
    return f
sq = squarer()
[sq(), sq(), sq(), sq()] == [1,4,9,16]""",
    )
}

// #[test]
// fn test_nested_def_3()
internal fun testNestedDef3() {
    Assert.isTrue(
        """
def f(x):
    def g(y):
        return lambda z: x + y + z
    return g
f(1)(2)(3) == 6""",
    )
}

// #[test]
// fn test_lambda_capture_from_module()
internal fun testLambdaCaptureFromModule() {
    Assert.isTrue(
        """
f = lambda y: x + y
x = 100
f(42) == 142
""",
    )
}

// #[test]
// fn test_lambda_capture_from_def()
internal fun testLambdaCaptureFromDef() {
    Assert.isTrue(
        """
def inside():
    f = lambda y: x + y
    x = 100
    return f(42) == 142
inside()
""",
    )
}

// #[test]
// fn test_lambda_capture_reassigned_from_def()
internal fun testLambdaCaptureReassignedFromDef() {
    Assert.isTrue(
        """
def inside():
    x = 100
    f = lambda y: x + y
    x = 200
    return f(42) == 242
inside()
""",
    )
}

// #[test]
// fn test_def_freeze()
internal fun testDefFreeze() {
    val a = Assert()
    a.module(
        "f.bzl",
        """
def f(g):
    g(1)""",
    )
    a.isTrue(
        """
load('f.bzl', 'f')
x = []
def g(y):
    x.append(y)
f(g)
x == [1]""",
    )
}

// #[test]
// fn test_frozen_lambda_nest()
internal fun testFrozenLambdaNest() {
    val a = Assert()
    val m =
        a.module(
            "a",
            """
def outer_function(x):
    return x["test"]

def function(x):
    def inner_function():
        return outer_function(x)
    return inner_function()

value = {"test": "hello"}
""",
        )
    val f = m.get("function").getOrThrow()
    val x = m.get("value").getOrThrow()
    Module.withTempHeap { module ->
        val fVal = module.heap().accessOwnedFrozenValue(f)
        val xVal = module.heap().accessOwnedFrozenValue(x)
        val eval = Evaluator(module)
        val res = eval.evalFunction(fVal, listOf(xVal), emptyList()).getOrThrow()
        check(res.toStr() == "hello")
    }
}

// #[test]
// fn test_context_captured()
internal fun testContextCaptured() {
    val a = Assert()
    a.module("f.bzl", "x = 17\ndef f(): return x")
    // Import `f` but do not import `x`
    a.isTrue("load('f.bzl', 'f')\nf() == 17")
}

// #[test]
// fn test_lambda_errors()
internal fun testLambdaErrors() {
    // Test from https://github.com/facebook/starlark-rust/issues/36
    Assert.fail("lambda a,a:a", "duplicated parameter name")
}

// #[test]
// fn test_lambda_errors_nested()
internal fun testLambdaErrorsNested() {
    // Test from https://issues.oss-fuzz.com/issues/369003809
    Assert.fail("lambda: lambda a,a:a", "duplicated parameter name")
    Assert.fail("[lambda a,a:a]", "duplicated parameter name")
}

// #[test]
// fn test_double_capture_and_freeze()
internal fun testDoubleCaptureAndFreeze() {
    val a = Assert()
    a.module(
        "x.bzl",
        """
def f(x):
    # `x` is captured by `g` and then frozen.
    def g():
        # When `h` is instantiated, `x` is already captured and frozen.
        def h():
            return noop(x)
        return h

    return g

G = f(1)
     """,
    )

    a.pass("load('x.bzl', 'G')\nG()")
}
