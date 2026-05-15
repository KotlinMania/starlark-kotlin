// port-lint: source tests:src/analysis/dubious.rs
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

class DubiousTest {

    private fun module(x: String): AstModule {
        return AstModule.parse("X", x, Dialect.AllOptionsInternal).getOrThrow()
    }

    @Test
    fun testLintDuplicateKeys() {
        val m = module(
            """
{'no1': 1, 'no1': 2}
{42: 1, 78: 9, 'no2': 100, 42: 6, 'no2': 8}
{123.0: "f", 123: "i"}
{0.25: "frac", 25e-2: "exp"}

# Variables can't change as a result of expression evaluation,
# so it's always an error if you see the same expression
{no3: 1, no4: 2, yes: 3, no3: 1, no3: 3, no4: 8}

# Functions can change each time round, so don't lint on them.
{f(): 1, f(): 2}
"""
        )
        val res = mutableListOf<LintT<Dubious>>()
        duplicateDictionaryKey(m, res)
        assertEquals(
            listOf(
                "\"no1\"", "42", "\"no2\"", "123", "0.25", "no3", "no3", "no4"
            ),
            res.map { it.problem.about() },
        )
    }

    @Test
    fun testLintIdentifierAsStatement() {
        val m = module(
            """
no1
def foo():
    f(yes)
    no2
"""
        )
        val res = mutableListOf<LintT<Dubious>>()
        identifierAsStatement(m, res)
        assertEquals(listOf("no1", "no2"), res.map { it.problem.about() })
    }
}
