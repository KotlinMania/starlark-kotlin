// port-lint: source src/tests/opt/def_inline.rs
package io.github.kotlinmania.starlark_kotlin.tests.opt

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

//! Test function bodies inlined.

import io.github.kotlinmania.starlark_kotlin.assert.Assert
import io.github.kotlinmania.starlark_kotlin.eval.compiler.DefGen
import io.github.kotlinmania.starlark_kotlin.values.layout.ValueLike
import io.github.kotlinmania.starlark_kotlin.eval.bc.BcOpcode
import io.github.kotlinmania.starlark_kotlin.values.types.list.display
import io.github.kotlinmania.starlark_kotlin.tests.bc.bcGoldenTest
import io.github.kotlinmania.starlark_kotlin.eval.bc.compiler.bc

// #[test]
// fn test_def_const_inlined()
internal fun testDefConstInlined() {
    bcGoldenTest(
        "def_inline_const_inlined",
        """
def trivial():
    return 10

def test():
    return trivial()
""",
    )
}

// #[test]
// fn test_def_list_inlined()
internal fun testDefListInlined() {
    bcGoldenTest(
        "def_inline_list_inlined",
        """
def test():
    return returns_list()

# Also test function is inlined if it is defined after the caller.
def returns_list():
    return [10, True]
""",
    )
}

// #[test]
// fn test_dict_inlined()
internal fun testDictInlined() {
    bcGoldenTest(
        "def_inline_dict_inlined",
        """
def returns_dict():
    # This should fail at runtime.
    return {[]: 10}

def test():
    return returns_dict()
""",
    )
}

// #[test]
// fn test_do_not_inline_functions_with_return_type()
internal fun testDoNotInlineFunctionsWithReturnType() {
    bcGoldenTest(
        "def_inline_return_type_inlined",
        """
def smth() -> str:
    return "10"

def test():
    # This call should not be inlined.
    return smth()
""",
    )
}

// #[test]
// fn test_dict_inlined_call_stack()
internal fun testDictInlinedCallStack() {
    val a = Assert()
    a.module("f.bzl", "def f(): return {[]: 10}")
    // For now inlining doesn't work within one module, so do different modules.
    val mG = a.module("g.bzl", "load('f.bzl', 'f')\ndef g(): return f()")
    val mH = a.module("h.bzl", "load('g.bzl', 'g')\ndef h(): return g()")

    // Check `f` is inlined into `g` and `h`.
    for ((m, f) in listOf(Pair(mG, "g"), Pair(mH, "h"))) {
        val fVal = m.get(f)!!
        val fDef = fVal.value().downcastRef<DefGen<*>>()!!
        check(BcOpcode.ListNew == fDef.bc().instrs.opcodes()[0]) {
            "in `$fDef`"
        }
    }

    val error = a.fail(
        """
load('h.bzl', 'h')
h()
""",
        "",
    )

    check(
        """
Traceback (most recent call last):
  * assert.bzl:3, in <module>
      h()
  * h.bzl.bzl:2, in h
      def h(): return g()
  * g.bzl.bzl:2, in g
      def g(): return f()
error: Value of type `list` is not hashable
 --> f.bzl.bzl:1:18
  |
1 | def f(): return {[]: 10}
  |                  ^^
  |
""" == "\n${error.display()}"
    )
}

// #[test]
// fn test_do_not_inline_too_large_functions()
internal fun testDoNotInlineTooLargeFunctions() {
    val a = Assert()
    a.module("a0.bzl", "def a0(): return noop()")
    for (i in 1 until 100) {
        // `a_17()` is inlined into `a_18()`.
        // If inlining is not limited, this test will run for very long time
        // and eat up all the memory.
        val i1 = i - 1
        a.module(
            "a$i.bzl",
            "load('a$i1.bzl', 'a$i1')\n" +
                "def a$i(): return a$i1() or a$i1()",
        )
    }
}

// #[test]
// fn test_calls_with_const_args_inlined()
internal fun testCallsWithConstArgsInlined() {
    bcGoldenTest(
        "def_inline_const_args_inlined",
        """
def foo(x, y):
    return noop(y, x)

def test():
    # This call should be inlined.
    return foo(10, True)
""",
    )
}

// #[test]
// fn test_calls_with_locals_inlined()
internal fun testCallsWithLocalsInlined() {
    bcGoldenTest(
        "def_inline_locals_inlined",
        """
def foo(x, y):
    return noop(y, x)

def test(x, y):
    # This call should be inlined.
    return foo(y, x)
""",
    )
}
