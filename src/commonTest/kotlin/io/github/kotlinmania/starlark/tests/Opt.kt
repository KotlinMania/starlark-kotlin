// port-lint: source tests/opt.rs
package io.github.kotlinmania.starlark.tests

import io.github.kotlinmania.starlark.tests.bc.bcGoldenTest
import kotlin.test.Test

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

/** Optimizer tests. */

class OptTests {
    @Test
    fun testTypeIsInlined() {
        bcGoldenTest(
            "opt_type_is_inlined",
            """
    def is_list(x):
        return type(x) == type([])

    def test(x):
        return is_list(x)
    """,
        )
    }

    @Test
    fun testPrivateForwardMutableModuleVarsInlined() {
        bcGoldenTest(
            "opt_private_forward_mutable_module_vars_inlined",
            """
    def test():
        # Reference to module variable should be replaced with constant
        return _private_forward_mutable

    _private_forward_mutable = {1: 2}
    """,
        )
    }

    @Test
    fun testSameModuleStructGetattrInlined() {
        bcGoldenTest(
            "opt_same_module_struct_getattr_inlined",
            """
    def test():
        return _s.f

    _s = struct(f = 1)
    """,
        )
    }

    @Test
    fun testListPlusList() {
        bcGoldenTest(
            "opt_list_plus_list",
            """
    L = [1, 2]

    def test():
        return L + [1]
    """,
        )
    }

    @Test
    fun testEmptyIterableOptimizedAway() {
        bcGoldenTest(
            "opt_empty_iterable_optimized_away",
            """
    L = []
    def test():
        for x in L:
            print(x)
    """,
        )
    }

    @Test
    fun testUnreachableCodeOptimizedAway() {
        bcGoldenTest(
            "opt_unreachable_code_optimized_away",
            """
    def test():
        if True:
            return
        fail("unreachable")
    """,
        )
    }

    @Test
    fun testRecursion() {
        bcGoldenTest(
            "opt_recursion",
            // Test inlining does not fail here.
            "def test(): return test()",
        )
    }

    @Test
    fun testMutualRecursion() {
        // Just check we do not enter an infinite recursion in the optimizer here.
        bcGoldenTest(
            "opt_mutual_recursion",
            """
    def test():
        return g()

    def g():
        return test()
    """,
        )
    }
}
