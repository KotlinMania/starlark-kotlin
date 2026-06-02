// port-lint: tests tests/for_loop.rs
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

import io.github.kotlinmania.starlark.assert.Assert

// #[test]
// fn test_for_loop_bug_1()
internal fun testForLoopBug1() {
    Assert.pass(
        """
def test(x):
    for i in x:
        # This should release mutation lock on `x`.
        return i

l = [1]
test(l)
l.append(1)
""",
    )
}
