// port-lint: tests src/tests/bc/and_or.rs
package io.github.kotlinmania.starlark_kotlin.tests.bc

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

// #[test]
// fn test_x_and_true()
internal fun testXAndTrue() {
    bcGoldenTest("and_or_x_and_true", "def test(x): return x and True")
}

// #[test]
// fn test_x_and_false()
internal fun testXAndFalse() {
    bcGoldenTest("and_or_x_and_false", "def test(x): return x and False")
}

// #[test]
// fn test_x_or_true()
internal fun testXOrTrue() {
    bcGoldenTest("and_or_x_or_true", "def test(x): return x or True")
}

// #[test]
// fn test_x_or_false()
internal fun testXOrFalse() {
    bcGoldenTest("and_or_x_or_false", "def test(x): return x or False")
}

// #[test]
// fn test_true_and_x()
internal fun testTrueAndX() {
    bcGoldenTest("and_or_true_and_x", "def test(x): return True and x")
}

// #[test]
// fn test_false_and_x()
internal fun testFalseAndX() {
    bcGoldenTest("and_or_false_and_x", "def test(x): return False and x")
}

// #[test]
// fn test_true_or_x()
internal fun testTrueOrX() {
    bcGoldenTest("and_or_true_or_x", "def test(x): return True or x")
}

// #[test]
// fn test_false_or_x()
internal fun testFalseOrX() {
    bcGoldenTest("and_or_false_or_x", "def test(x): return False or x")
}
