// port-lint: source src/tests/type_annot.rs
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
import kotlin.test.Test
import io.github.kotlinmania.starlark.syntax.dialect.Dialect
import io.github.kotlinmania.starlark.syntax.dialect.DialectTypes

private const val PROGRAM = """def f(x: int): pass

f(noop('s'))
"""

class TypeAnnotTests {

    @Test
    fun testTypesDisable() {
        val dialect = Dialect.Standard.copy(enableTypes = DialectTypes.Disable)
        val a = Assert()
        a.dialect(dialect)
        a.fail(PROGRAM, "type annotations are not allowed in this dialect")
    }

    @Test
    fun testTypesParseOnly() {
        val dialect = Dialect.Standard.copy(enableTypes = DialectTypes.ParseOnly)
        val a = Assert()
        a.dialect(dialect)
        a.pass(PROGRAM)
    }

    @Test
    fun testTypesEnable() {
        val dialect = Dialect.Standard.copy(enableTypes = DialectTypes.Enable)
        val a = Assert()
        a.dialect(dialect)
        a.fail(PROGRAM, "does not match the type annotation")
    }

    @Test
    fun testTypeAssignAnnotation() {
        Assert.pass(
            """
x : str = "test"
xs: typing.Any = [1,2]
xs[0] : int = 4
"""
        )
        Assert.fail(
            "a, b : typing.Any = 1, 2",
            "not allowed on multiple assignments",
        )
        Assert.fail(
            "a = 1\na : typing.Any += 1",
            "not allowed on augmented assignments",
        )
        Assert.fail("a : str = noop(1)", "does not match the type annotation")
    }

    @Test
    fun testOnlyGlobalsOrBuiltinsAllowed() {
        Assert.fail(
            """
def f():
    x = "str"
    def g(p: x): pass
""",
            "Identifiers in type expressions can only refer globals or builtins: `x`",
        )
    }

    @Test
    fun testTypecheckOptInSelfCheck() {
        val a = Assert()
        a.disableStaticTypechecking()
        a.pass(
            """
def f(x: int): pass
def g(): f("")
"""
        )
    }

    /** Test `@starlark-rust: typecheck` enables typechecking when `Evaluator` does not. */
    @Test
    fun testTypecheckOptIn() {
        val a = Assert()
        a.disableStaticTypechecking()
        a.fail(
            """
# @starlark-rust: typecheck
def f(x: int): pass
def g(): f("")
""",
            "Expected type `int` but got `str`",
        )
    }

    @Test
    fun testStringLitAsType() {
        Assert.fail(
            """
def foo(x: ""): pass
""",
            "string literal expression is not allowed in type expression",
        )
    }

    @Test
    fun testStringConstAsType() {
        Assert.fail(
            """
T = ""
def foo(x: T): pass
""",
            "String literals are not allowed in type expressions",
        )
    }
}
