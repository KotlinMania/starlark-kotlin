// port-lint: tests tests/opt.rs
package io.github.kotlinmania.starlark_kotlin.tests

import io.github.kotlinmania.starlark_kotlin.tests.bc.bcGoldenTest

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

/** Optimizer tests. */


// #[test]
// fn test_type_is_inlined()
internal fun testTypeIsInlined() {
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

// #[test]
// fn test_private_forward_mutable_module_vars_inlined()
internal fun testPrivateForwardMutableModuleVarsInlined() {
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

// #[test]
// fn test_same_module_struct_getattr_inlined()
internal fun testSameModuleStructGetattrInlined() {
    bcGoldenTest(
        "opt_same_module_struct_getattr_inlined",
        """
def test():
    return _s.f

_s = struct(f = 1)
""",
    )
}

// #[test]
// fn test_list_plus_list()
internal fun testListPlusList() {
    bcGoldenTest(
        "opt_list_plus_list",
        """
L = [1, 2]

def test():
    return L + [1]
""",
    )
}

// #[test]
// fn test_empty_iterable_optimized_away()
internal fun testEmptyIterableOptimizedAway() {
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

// #[test]
// fn test_unreachable_code_optimized_away()
internal fun testUnreachableCodeOptimizedAway() {
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

// #[test]
// fn test_recursion()
internal fun testRecursion() {
    bcGoldenTest(
        "opt_recursion",
        // Test inlining does not fail here.
        "def test(): return test()",
    )
}

// #[test]
// fn test_mutual_recursion()
internal fun testMutualRecursion() {
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
