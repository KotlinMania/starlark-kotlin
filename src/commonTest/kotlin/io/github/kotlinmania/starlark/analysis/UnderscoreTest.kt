// port-lint: source src/analysis/underscore.rs (tests)
package io.github.kotlinmania.starlark.analysis

/*
 * Copyright 2019 The Starlark in Rust Authors.
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

import io.github.kotlinmania.starlark.syntax.AstModule
import io.github.kotlinmania.starlark.syntax.dialect.Dialect
import kotlin.test.Test
import kotlin.test.assertEquals

class UnderscoreTest {

    // fn module(x: &str) -> AstModule
    private fun module(x: String): AstModule {
        return AstModule.parse("X", x, Dialect.AllOptionsInternal).getOrThrow()
    }

    // #[test]
    // fn test_lint_inappropriate_underscore()
    @Test
    fun testLintInappropriateUnderscore() {
        val m = module(
            """
def _ok():
    def _no1(foo):
        _no2 = lambda x: x
        _no3 = 8
        _unused = foo
"""
        )
        val res = mutableListOf<LintT<UnderscoreWarning>>()
        inappropriateUnderscore(m.codemap, m.statement, true, res)
        val names = res.map { it.problem.about() }.sorted()
        assertEquals(listOf("_no1", "_no2", "_no3"), names)
    }

    // #[test]
    // fn test_lint_use_ignored()
    @Test
    fun testLintUseIgnored() {
        val m = module(
            """
def _foo(): pass
_allowed = 1
_missed = 1
def bar():
    def _no1(): pass
    _foo()
    _no1()
    _no2 = 1
    print(_no2)
    _no3 = 1
    _missed = 7
    # Could argue that missed should be an error, since it shadows
    print(_missed)
    print(_allowed)
    def deeper():
        print(_no3)
        _foo(__internal__)
"""
        )
        val res = mutableListOf<LintT<UnderscoreWarning>>()
        useIgnored(m.codemap, m.statement, res)
        val names = res.map { it.problem.about() }.sorted()
        assertEquals(listOf("_no1", "_no2", "_no3"), names)
    }
}
