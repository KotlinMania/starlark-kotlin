// port-lint: source src/tests/bc/andOr.rs
package io.github.kotlinmania.starlark.tests.bc

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

class AndOrTests {
    @Test
    fun testXAndTrue() {
        bcGoldenTest("and_or_x_and_true", "def test(x): return x and True")
    }

    @Test
    fun testXAndFalse() {
        bcGoldenTest("and_or_x_and_false", "def test(x): return x and False")
    }

    @Test
    fun testXOrTrue() {
        bcGoldenTest("and_or_x_or_true", "def test(x): return x or True")
    }

    @Test
    fun testXOrFalse() {
        bcGoldenTest("and_or_x_or_false", "def test(x): return x or False")
    }

    @Test
    fun testTrueAndX() {
        bcGoldenTest("and_or_true_and_x", "def test(x): return True and x")
    }

    @Test
    fun testFalseAndX() {
        bcGoldenTest("and_or_false_and_x", "def test(x): return False and x")
    }

    @Test
    fun testTrueOrX() {
        bcGoldenTest("and_or_true_or_x", "def test(x): return True or x")
    }

    @Test
    fun testFalseOrX() {
        bcGoldenTest("and_or_false_or_x", "def test(x): return False or x")
    }
}
