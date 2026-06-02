// port-lint: tests src/docs/parse.rs
@file:Suppress("UNNECESSARY_NOT_NULL_ASSERTION")

package io.github.kotlinmania.starlark.docs

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

import io.github.kotlinmania.starlark.typing.Ty
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ParseTest {
    @Test
    fun parsesStarlarkDocstring() {
        // Empty / whitespace-only → None
        assertNull(DocString.fromDocstring(DocStringKind.Starlark, " "))

        // Summary only (with surrounding whitespace/newlines)
        assertEquals(
            DocString(
                summary = "This should be the summary",
                details = null,
                examples = null,
            ),
            DocString.fromDocstring(
                DocStringKind.Starlark,
                " \n\nThis should be the summary\n\n",
            ),
        )

        // Summary only (trailing space after blank line)
        assertEquals(
            DocString(
                summary = "This should be the summary",
                details = null,
                examples = null,
            ),
            DocString.fromDocstring(
                DocStringKind.Starlark,
                " \n\nThis should be the summary\n\n ",
            ),
        )

        // Summary + details
        assertEquals(
            DocString(
                summary = "Summary line here",
                details = "Details after some spaces\n\nand some more newlines",
                examples = null,
            ),
            DocString.fromDocstring(
                DocStringKind.Starlark,
                "Summary line here\n    \nDetails after some spaces\n\nand some more newlines",
            ),
        )

        // Full docstring with summary, details, and examples
        val fullDocstring = """
        This is the summary.
          It has multiple lines and some spaces, and should be collapsed

        This should be a multiline set of details.
        It should be:
            - Dedented
            - Trimmed
            - Split properly from the summary

        Examples:
        Some example code

"""
        assertEquals(
            DocString(
                summary = "This is the summary. It has multiple lines and some spaces, and should be collapsed",
                details =
                    "This should be a multiline set of details.\n" +
                        "It should be:\n" +
                        "    - Dedented\n" +
                        "    - Trimmed\n" +
                        "    - Split properly from the summary",
                examples = "Some example code",
            ),
            DocString.fromDocstring(DocStringKind.Starlark, fullDocstring),
        )

        // Summary on first line (not indented like details)
        val mixedIndent = """This is a summary line that is not dedented like the 'details'

        Typing the first line right after the ${"\"\"\"" /* triple-quote */} in python docstrings is common,
        while putting the rest of the docstring indented. Just support both so it
        doesn't surprise anyone.
        """
        assertEquals(
            DocString(
                summary = "This is a summary line that is not dedented like the 'details'",
                details =
                    "Typing the first line right after the \"\"\" in python docstrings is common,\n" +
                        "while putting the rest of the docstring indented. Just support both so it\n" +
                        "doesn't surprise anyone.",
                examples = null,
            ),
            DocString.fromDocstring(DocStringKind.Starlark, mixedIndent),
        )
    }

    @Test
    fun parsesRustDocstring() {
        val raw = """
        This is the summary line
          that sometimes is split on two lines

        Examples:
        This is the second part. It has some code blocks

        ```
        # foo() {
        "bar"
        # }
        ```

        ```python
        # This is a python comment. Leave it be
        print(1)
        ```

        ```rust
        # other_foo() {
        "other_bar"
        # }
        ```
        """

        assertEquals(
            DocString(
                summary = "This is the summary line that sometimes is split on two lines",
                details = null,
                examples =
                    "This is the second part. It has some code blocks\n\n" +
                        "```\n" +
                        "\"bar\"\n" +
                        "```\n\n" +
                        "```python\n" +
                        "# This is a python comment. Leave it be\n" +
                        "print(1)\n" +
                        "```\n\n" +
                        "```rust\n" +
                        "\"other_bar\"" +
                        "\n```",
            ),
            DocString.fromDocstring(DocStringKind.Rust, raw),
        )
    }

    @Test
    fun parsesAndRemovesSectionsFromStarlarkDocstring() {
        val rawDocs = """This is an example docstring

        We have some details up here that should not be parsed

        Some empty section:
        Example:
            First line of the section

            A newline with no space after it before the second one,
                and a third that's indented further.
        This is not in the example section

        Last:
            This is something in the last section
        """

        val expectedDocstring =
            DocString.fromDocstring(
                DocStringKind.Starlark,
                """This is an example docstring

            We have some details up here that should not be parsed

            Some empty section:
            This is not in the example section

            Last:
                This is something in the last section
            """,
            )
        assertNotNull(expectedDocstring)

        val expectedSections =
            mapOf(
                "example" to (
                    "First line of the section\n\n" +
                        "A newline with no space after it before the second one,\n" +
                        "    and a third that's indented further."
                ),
            )

        val ds = DocString.fromDocstring(DocStringKind.Starlark, rawDocs)
        assertNotNull(ds)
        val (newDs, sections) = ds.parseAndRemoveSections(DocStringKind.Starlark, listOf("example"))

        assertEquals(expectedDocstring, newDs)
        assertEquals(expectedSections, sections)
    }

    @Test
    fun parsesAndRemovesSectionsFromRustDocstring() {
        val rawDocs = """This is an example docstring

        We have some details up here that should not be parsed

        # Some Section

        ```
        # This is a commented out line in a codeblock
        fn some_func() {}
        ```

        # Example
        First line of the section

        Note that, unlike starlark doc strings,
        we don't require indentation. The end of a
        section is either a new section appearing,
        or the end of the string.

        # Last
        This is something in the last section
        """

        val expectedDocstring =
            DocString.fromDocstring(
                DocStringKind.Rust,
                """This is an example docstring

        We have some details up here that should not be parsed

        # Some Section

        ```
        fn some_func() {}
        ```

        # Last
        This is something in the last section
        """,
            )
        assertNotNull(expectedDocstring)

        val expectedSections =
            mapOf(
                "example" to (
                    "First line of the section\n\n" +
                        "Note that, unlike starlark doc strings,\n" +
                        "we don't require indentation. The end of a\n" +
                        "section is either a new section appearing,\n" +
                        "or the end of the string."
                ),
            )

        val ds = DocString.fromDocstring(DocStringKind.Rust, rawDocs)
        assertNotNull(ds)
        val (newDs, sections) = ds.parseAndRemoveSections(DocStringKind.Rust, listOf("example"))

        assertEquals(expectedDocstring, newDs)
        assertEquals(expectedSections, sections)
    }

    // Helper for function docstring tests
    private fun arg(name: String): DocParam =
        DocParam(
            name = name,
            docs = null,
            typ = Ty.any(),
            defaultValue = null,
        )

    @Test
    fun parsesStarlarkFunctionDocstring() {
        val docstring = """This is an example docstring

        Details here

        Args:
            arg_foo: The argument named foo
            arg_bar: The argument named bar. It has
                     a longer doc string that spans
                     over three lines
            *args: Docs for args
            **kwargs: Docs for kwargs

        Returns:
            A value
        """

        val kind = DocStringKind.Starlark
        val returnType = Ty.int()

        val functionDocs =
            DocFunction.fromDocstring(
                kind,
                DocParams(
                    kwargs = arg("kwargs"),
                    args = arg("args"),
                    posOrNamed = listOf(arg("arg_bar"), arg("arg_foo")),
                    posOnly = emptyList(),
                    namedOnly = emptyList(),
                ),
                returnType,
                docstring,
            )

        // Verify function-level docs
        assertNotNull(functionDocs.docs)
        assertEquals("This is an example docstring", functionDocs.docs!!.summary)
        assertEquals("Details here", functionDocs.docs!!.details)

        // Verify kwargs docs
        assertNotNull(functionDocs.params.kwargs)
        assertNotNull(functionDocs.params.kwargs!!.docs)
        assertEquals(
            "Docs for kwargs",
            functionDocs.params.kwargs!!
                .docs!!
                .summary,
        )

        // Verify args docs
        assertNotNull(functionDocs.params.args)
        assertNotNull(functionDocs.params.args!!.docs)
        assertEquals(
            "Docs for args",
            functionDocs.params.args!!
                .docs!!
                .summary,
        )

        // Verify positional param docs
        val argBar = functionDocs.params.posOrNamed[0]
        assertNotNull(argBar.docs)
        assertEquals("arg_bar", argBar.name)
        // from_docstring normalizes the summary by collapsing newlines into spaces
        assertEquals(
            DocString.fromDocstring(
                kind,
                "The argument named bar. It has\n" +
                    "a longer doc string that spans\n" +
                    "over three lines",
            ),
            argBar.docs,
        )

        val argFoo = functionDocs.params.posOrNamed[1]
        assertNotNull(argFoo.docs)
        assertEquals("arg_foo", argFoo.name)
        assertEquals(
            DocString.fromDocstring(kind, "The argument named foo"),
            argFoo.docs,
        )

        // Verify return docs
        assertNotNull(functionDocs.ret.docs)
        assertEquals(
            DocString.fromDocstring(kind, "A value"),
            functionDocs.ret.docs,
        )
    }

    @Test
    fun parsesRustFunctionDocstring() {
        val docstring = """This is an example docstring

        Details here

        # Arguments
        * `arg_foo`: The argument named foo
        `arg_bar`: The argument named bar. It has
                   a longer doc string that spans
                   over three lines

        # Returns
        A value
        """

        val kind = DocStringKind.Rust
        val returnType = Ty.int()

        val functionDocs =
            DocFunction.fromDocstring(
                kind,
                DocParams(
                    posOrNamed = listOf(arg("arg_bar"), arg("arg_foo")),
                ),
                returnType,
                docstring,
            )

        // Verify function-level docs
        assertNotNull(functionDocs.docs)
        assertEquals("This is an example docstring", functionDocs.docs!!.summary)
        assertEquals("Details here", functionDocs.docs!!.details)

        // Verify positional param docs
        val argBar = functionDocs.params.posOrNamed[0]
        assertNotNull(argBar.docs)
        assertEquals("arg_bar", argBar.name)
        assertEquals(
            DocString.fromDocstring(
                kind,
                "The argument named bar. It has\n" +
                    "a longer doc string that spans\n" +
                    "over three lines",
            ),
            argBar.docs,
        )

        val argFoo = functionDocs.params.posOrNamed[1]
        assertNotNull(argFoo.docs)
        assertEquals("arg_foo", argFoo.name)
        assertEquals(
            DocString.fromDocstring(kind, "The argument named foo"),
            argFoo.docs,
        )

        // Verify return docs
        assertNotNull(functionDocs.ret.docs)
        assertEquals(
            DocString.fromDocstring(kind, "A value"),
            functionDocs.ret.docs,
        )
    }
}
