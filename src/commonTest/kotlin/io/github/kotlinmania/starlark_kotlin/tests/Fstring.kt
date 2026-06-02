// port-lint: tests tests/fstring.rs
package io.github.kotlinmania.starlark_kotlin.tests

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
import io.github.kotlinmania.starlark_kotlin.syntax.dialect.Dialect
import io.github.kotlinmania.starlark_kotlin.golden_test_template.goldenTestTemplate

// mod pass

private fun passAssert(): Assert {
    val a = Assert()
    a.dialect(Dialect.AllOptionsInternal)
    return a
}

// #[test]
// fn basic()
internal fun testFstringBasic() {
    passAssert().isTrue(
        """
x = "a"
f"{x}b" == "ab"
""",
    )

    passAssert().isTrue(
        """
x = "a"
f"{ x }b" == "ab"
""",
    )

    passAssert().isTrue(
        """
x = "a"
f"{x}b{x}" == "aba"
""",
    )
}

// #[test]
// fn escape()
internal fun testFstringEscape() {
    passAssert().isTrue(
        """
x = "a"
f"{{}}{x}b{{x}}" == "{}ab{x}"
""",
    )
}

// #[test]
// fn function_parameter()
internal fun testFstringFunctionParameter() {
    passAssert().isTrue(
        """
def f(x):
  return f"q{x}"

f("1") == "q1"
""",
    )
}

// #[test]
// fn multiple()
internal fun testFstringMultiple() {
    passAssert().isTrue(
        """
x = "x"
y = "y"
f"{x}{y}" == "xy"
""",
    )

    passAssert().isTrue(
        """
x = "x"
y = "y"
f"{x}{y}{x}" == "xyx"
""",
    )
}

// #[test]
// fn tuple()
internal fun testFstringTuple() {
    passAssert().isTrue(
        """
x = ("x",)
f"{x}" == '("x",)'
""",
    )
}

// #[test]
// fn conv()
internal fun testFstringConv() {
    passAssert().isTrue("""x = 'a'; f"{x}" == 'a'""")
    passAssert().isTrue("""x = 'a'; f"{x!s}" == 'a'""")
    passAssert().isTrue("""x = 'a'; f"{x!r}" == '"a"'""")
}

// mod fail

// fn fstring_golden_test_with_dialect(test_name: &str, text: &str, dialect: &Dialect)
private fun fstringGoldenTestWithDialect(testName: String, text: String, dialect: Dialect) {
    val a = Assert()
    a.dialect(dialect)

    val err = a.fails(text, emptyList())

    goldenTestTemplate(
        "src/tests/fstring/golden/$testName.err.golden.md",
        "$err",
    )
}

// fn fstring_golden_test(test_name: &str, text: &str)
private fun fstringGoldenTest(testName: String, text: String) {
    fstringGoldenTestWithDialect(testName, text, Dialect.AllOptionsInternal)
}

// #[test]
// fn undeclared_variable()
internal fun testFstringUndeclaredVariable() {
    fstringGoldenTest("undeclared_variable", "f'foo {bar}'")
}

// #[test]
// fn invalid_identifier()
internal fun testFstringInvalidIdentifier() {
    fstringGoldenTest("invalid_identifier", "f'foo {bar baz}'")
}

// #[test]
// fn invalid_identifier_expression()
internal fun testFstringInvalidIdentifierExpression() {
    fstringGoldenTest("invalid_identifier_expression", "f'foo {bar[123]}'")
}

// #[test]
// fn invalid_identifier_triple_quotes()
internal fun testFstringInvalidIdentifierTripleQuotes() {
    fstringGoldenTest("invalid_identifier_triple_quotes", "f'''foo {bar baz}'''")
}

// #[test]
// fn invalid_identifier_raw()
internal fun testFstringInvalidIdentifierRaw() {
    fstringGoldenTest("invalid_identifier_raw", "fr'foo {bar baz}'")
}

// #[test]
// fn invalid_identifier_multiline()
internal fun testFstringInvalidIdentifierMultiline() {
    fstringGoldenTest("invalid_identifier_multiline", "f''''foo \n {bar baz}'''")
}

// #[test]
// fn invalid_format()
internal fun testFstringInvalidFormat() {
    fstringGoldenTest("invalid_format", "f'foo {bar'")
}

// #[test]
// fn escape() (in fail mod)
internal fun testFstringEscapeFail() {
    // NOTE: this is wrong, we put the squiggly lines in the wrong place.
    fstringGoldenTest("escape", "f'foo \\n {bar baz}'")
}

// #[test]
// fn not_enabled()
internal fun testFstringNotEnabled() {
    fstringGoldenTestWithDialect("not_enabled", "f'{foo}'", Dialect.Standard)
}
