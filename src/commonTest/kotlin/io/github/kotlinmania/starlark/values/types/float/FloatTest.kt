// port-lint: source tests:src/values/types/float/float.rs
package io.github.kotlinmania.starlark.values.types.float

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

import io.github.kotlinmania.starlark.assert.Assert
import kotlin.test.Test

class FloatTest {

    @Test
    fun testDictionaryKey() {
        Assert.pass(
            """
x = {0: 123}
assert_eq(x[0], 123)
# TODO(nga): fix typechecker, and remove `noop`.
assert_eq(x[noop(0.0)], 123)
assert_eq(x[noop(-0.0)], 123)
assert_eq(1 in x, False)
        """,
        )
    }

    @Test
    fun testComparisons() {
        val a = Assert()
        // TODO(nga): fix and enable.
        a.disableStaticTypechecking()
        a.allTrue(
            """
+0.0 == -0.0
0.0 == 0
0 == 0.0
0 < 1.0
0.0 < 1
1 > 0.0
1.0 > 0
0.0 < float("nan")
float("+inf") < float("nan")
""",
        )
    }

    @Test
    fun testComparisonsBySorting() {
        Assert.eq(
            "sorted([float('inf'), float('-inf'), float('nan'), 1e300, -1e300, 1.0, -1.0, 1, -1, 1e-300, -1e-300, 0, 0.0, float('-0.0'), 1e-300, -1e-300])",
            "[float('-inf'), -1e+300, -1.0, -1, -1e-300, -1e-300, 0, 0.0, -0.0, 1e-300, 1e-300, 1.0, 1, 1e+300, float('+inf'), float('nan')]",
        )
    }
}
