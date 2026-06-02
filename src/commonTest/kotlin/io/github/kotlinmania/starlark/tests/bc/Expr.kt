// port-lint: tests tests/bc/expr.rs
package io.github.kotlinmania.starlark.tests.bc

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

import io.github.kotlinmania.starlark.assert.Assert

internal fun testType() {
    bcGoldenTest("expr_type", "def test(x): return type(x)")
}

internal fun testPercentSOne() {
    bcGoldenTest("expr_percent_s_one", "def test(x): return '((%s))' % x")
}

internal fun testFormatOne() {
    bcGoldenTest("expr_format_one", "def test(x): return '(({}))'.format(x)")
}

internal fun testPercentSOneFormatOneEval() {
    Assert.pass(
        """
load("asserts.star", "asserts")

def test(x):
    return ("<{}>".format(x), "<%s>" % x)

asserts.eq(("<1>", "<1>"), test(1))
# Test format does not accidentally call `PercentSOne`.
asserts.eq(("<(1,)>", "<1>"), test((1,)))
""",
    )
}

internal fun testSpecExecList() {
    // `list` function is const-evaluated and the resulting list is compiled as list instruction.
    bcGoldenTest("expr_spec_exec_list", "def test(): return list((10, 20))")
}

internal fun testCallMaybeKnownMethod() {
    bcGoldenTest("expr_call_maybe_known_method", "def test(x): x.append(1)")
}

internal fun testFstring() {
    bcGoldenTest("expr_fstring", "def test(x): return f'test: {x}'")
}
