// port-lint: source src/tests/fstring.rs
package io.github.kotlinmania.starlark.tests

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
import io.github.kotlinmania.starlark.syntax.dialect.Dialect
import io.github.kotlinmania.starlark.goldentesttemplate.goldenTestTemplate
import kotlin.test.Test

class FstringPassTest {
    private fun assert(): Assert {
        val a = Assert()
        a.dialect(Dialect.AllOptionsInternal)
        return a
    }

    @Test
    fun basic() {
        assert().isTrue(
            """
x = "a"
f"{x}b" == "ab"
""",
        )

        assert().isTrue(
            """
x = "a"
f"{ x }b" == "ab"
""",
        )

        assert().isTrue(
            """
x = "a"
f"{x}b{x}" == "aba"
""",
        )
    }

    @Test
    fun escape() {
        assert().isTrue(
            """
x = "a"
f"{{}}{x}b{{x}}" == "{}ab{x}"
""",
        )
    }

    @Test
    fun functionParameter() {
        assert().isTrue(
            """
def f(x):
  return f"q{x}"

f("1") == "q1"
""",
        )
    }

    @Test
    fun multiple() {
        assert().isTrue(
            """
x = "x"
y = "y"
f"{x}{y}" == "xy"
""",
        )

        assert().isTrue(
            """
x = "x"
y = "y"
f"{x}{y}{x}" == "xyx"
""",
        )
    }

    @Test
    fun tuple() {
        assert().isTrue(
            """
x = ("x",)
f"{x}" == '("x",)'
""",
        )
    }

    @Test
    fun conv() {
        assert().isTrue("""x = 'a'; f"{x}" == 'a'""")
        assert().isTrue("""x = 'a'; f"{x!s}" == 'a'""")
        assert().isTrue("""x = 'a'; f"{x!r}" == '"a"'""")
    }
}

class FstringFailTest {
    private fun fstringGoldenTestWithDialect(testName: String, text: String, dialect: Dialect) {
        val a = Assert()
        a.dialect(dialect)

        val err = a.fails(text, emptyList())

        goldenTestTemplate(
            "src/tests/fstring/golden/$testName.err.golden.md",
            "$err",
        )
    }

    private fun fstringGoldenTest(testName: String, text: String) {
        fstringGoldenTestWithDialect(testName, text, Dialect.AllOptionsInternal)
    }

    @Test
    fun undeclaredVariable() {
        fstringGoldenTest("undeclared_variable", "f'foo {bar}'")
    }

    @Test
    fun invalidIdentifier() {
        fstringGoldenTest("invalid_identifier", "f'foo {bar baz}'")
    }

    @Test
    fun invalidIdentifierExpression() {
        fstringGoldenTest("invalid_identifier_expression", "f'foo {bar[123]}'")
    }

    @Test
    fun invalidIdentifierTripleQuotes() {
        fstringGoldenTest("invalid_identifier_triple_quotes", "f'''foo {bar baz}'''")
    }

    @Test
    fun invalidIdentifierRaw() {
        fstringGoldenTest("invalid_identifier_raw", "fr'foo {bar baz}'")
    }

    @Test
    fun invalidIdentifierMultiline() {
        fstringGoldenTest("invalid_identifier_multiline", "f''''foo \n {bar baz}'''")
    }

    @Test
    fun invalidFormat() {
        fstringGoldenTest("invalid_format", "f'foo {bar'")
    }

    @Test
    fun escape() {
        // NOTE: this is wrong, we put the squiggly lines in the wrong place.
        fstringGoldenTest("escape", "f'foo \\n {bar baz}'")
    }

    @Test
    fun notEnabled() {
        fstringGoldenTestWithDialect("not_enabled", "f'{foo}'", Dialect.Standard)
    }
}
