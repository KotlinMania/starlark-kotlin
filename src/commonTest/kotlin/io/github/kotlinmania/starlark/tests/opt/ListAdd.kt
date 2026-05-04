// port-lint: source tests/opt/listAdd.rs
package io.github.kotlinmania.starlark.tests.opt

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

class ListAddTests {
    @Test
    fun testListOfConstAddOpt() {
        bcGoldenTest(
            "opt_list_of_const_add",
            """
    def test():
        return [1, 2] + [3, 4, 5]
    """,
        )
    }

    @Test
    fun testListOfExprAdd() {
        bcGoldenTest(
            "opt_list_of_expr_add",
            """
    def test():
        return [noop(), noop()] + [noop(), noop(), noop()]
    """,
        )
    }
}
