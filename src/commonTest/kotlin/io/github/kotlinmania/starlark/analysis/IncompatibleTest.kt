// port-lint: source tests:src/analysis/incompatible.rs
package io.github.kotlinmania.starlark.analysis

/*
 * Copyright 2019 The Starlark in Rust Authors.
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

import io.github.kotlinmania.starlark.syntax.AstModule
import io.github.kotlinmania.starlark.syntax.dialect.Dialect
import kotlin.test.Test
import kotlin.test.assertEquals

class IncompatibleTest {
    private fun module(x: String): AstModule = AstModule.parse("bad.py", x, Dialect.AllOptionsInternal).getOrThrow()

    @Test
    fun testLintIncompatible() {
        val res = mutableListOf<LintT<Incompatibility>>()
        badTypeEquality(
            module(
                """
def foo():
    if type(x) == str and type(y) == type(list) and type(z) == foobar:
        pass
""",
            ),
            res,
        )
        assertEquals(
            listOf(
                "bad.py:3:8-22: Type check `(type(x) == str)` should be written `type(x) == type(\"\")`",
            ),
            res.map { it.toString() },
        )
    }

    @Test
    fun testLintDuplicateTopLevelAssign() {
        val m =
            module(
                """
load("file", "foo", "no3", "no4")
no1 = 1
no1 = 4
no1 += 8
foo = foo # Starlark reexport
no3 = no3
no3 = no3
no4 = no4 + 1
def no2(): pass
def no2():
    x = 1
    x += 1
    return x
""",
            )
        val res = mutableListOf<LintT<Incompatibility>>()
        duplicateTopLevelAssignment(m, res)
        val names =
            res
                .map { lt ->
                    when (val problem = lt.problem) {
                        is Incompatibility.DuplicateTopLevelAssign -> problem.name
                        else -> error("Unexpected lint")
                    }
                }.sorted()
        assertEquals(listOf("no1", "no1", "no2", "no3", "no4"), names)
    }
}
