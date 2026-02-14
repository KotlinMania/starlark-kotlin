// port-lint: source src/tests/opt/constant_folding.rs
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

import io.github.kotlinmania.starlark_kotlin.assert.Assert
import io.github.kotlinmania.starlark_kotlin.tests.bc.bcGoldenTest

// #[test]
// fn test_fold_list_add()
internal fun testFoldListAdd() {
    bcGoldenTest("constant_folding_list_add", "def test(): return [1] + [2]")
}

// #[test]
// fn test_fold_list_add_too_large()
internal fun testFoldListAddTooLarge() {
    val a = Assert()
    a.module("a0.bzl", "def a0(): return [1]")
    for (i in 1 until 100) {
        // `a_17()` is inlined into `a_18()`.
        // If inlining is not limited, this test will run for very long time
        // and eat up all the memory.
        val i1 = i - 1
        a.module(
            "a$i.bzl",
            "load('a$i1.bzl', 'a$i1')\n" +
                "def a$i(): return a$i1() + a$i1() + a$i1() + a$i1() + a$i1()",
        )
    }
}
