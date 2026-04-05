// port-lint: tests tests/bc/for_stmt.rs
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
// fn test_for()
internal fun testFor() {
    bcGoldenTest("for", "def test(x):\n  for i in x:\n    noop(i)")
}

// #[test]
// fn test_for_break()
internal fun testForBreak() {
    bcGoldenTest(
        "for_break",
        "def test(x):\n  for i in x:\n    if i: break\n    noop(i)",
    )
}

// #[test]
// fn test_for_continue()
internal fun testForContinue() {
    bcGoldenTest(
        "for_continue",
        "def test(x):\n  for i in x:\n    if i: continue\n    noop(i)",
    )
}
