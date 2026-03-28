// port-lint: source src/docs/parse.rs
package io.github.kotlinmania.starlark_kotlin.docs

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

import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ExprP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.StmtP
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.syntax.ast.Expr
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstStmtP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstPayload
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstLiteral

/// Controls the formatting to use when parsing [DocString]s from raw docstrings.
// #[derive(Copy, Clone, Dupe)]
// pub enum DocStringKind
enum class DocStringKind {
    /// Docstrings provided by users in starlark files, following python-y documentation style.
    ///
    /// For functions, they are the piece in `"""` that come right after the `def foo():` line,
    /// and they have sections for additional details. An example from a starlark file might be:
    ///
    /// ```python
    /// """ Module level docs here """
    ///
    /// def some_function(val: "string") -> "string":
    ///     """ This function takes a string and returns it.
    ///
    ///     This is where an explanation might go, but I have none
    ///
    ///     Args:
    ///         val: This is the value that gets returned
    ///
    ///     Returns:
    ///         The original value, because identity functions are fun.
    /// ```
    Starlark,

    /// Docstrings used with `#[starlark_module]` in rust / `@StarlarkModule` in Kotlin.
    ///
    /// These are the documentation strings prefixed by `///` on
    /// `@StarlarkModule`, and the functions / attributes within it. It supports
    /// a section `# Arguments`, and `# Returns`, and removes some lines from code
    /// blocks that are valid for KDoc/rustdoc, but not useful for people using these
    /// functions via starlark.
    Rust,
}

// --- Dedent utility ---
// Equivalent of textwrap::dedent from Rust.

/// Remove common leading whitespace from all non-blank lines.
private fun dedent(text: String): String {
    val lines = text.lines()
    // Find the minimum indentation of non-blank lines (ignoring the first line).
    var minIndent: Int? = null
    for ((i, line) in lines.withIndex()) {
        if (i == 0) continue
        val stripped = line.trimStart()
        if (stripped.isEmpty()) continue
        val indent = line.length - stripped.length
        if (minIndent == null || indent < minIndent) {
            minIndent = indent
        }
    }
    if (minIndent == null || minIndent == 0) return text

    return buildString {
        for ((i, line) in lines.withIndex()) {
            if (i > 0) append('\n')
            if (i == 0) {
                append(line)
            } else if (line.trimStart().isEmpty()) {
                // Blank line: keep empty
                append(line)
            } else {
                append(line.substring(minIndent.coerceAtMost(line.length)))
            }
        }
    }
}

// --- Regex patterns (compiled once, like Rust's Lazy<Regex>) ---

// remove_rust_comments
// ```(\w*)\n.*?``` with DOTALL
private val CODEBLOCK_RE = Regex("""```(\w*)\n[\s\S]*?```""")
// ^# .*$\n with MULTILINE
private val COMMENT_RE = Regex("""^# .*$\n""", RegexOption.MULTILINE)

// parse_and_remove_sections — Starlark
// ^([\w -]+):\s*$
private val STARLARK_SECTION_RE = Regex("""^([\w -]+):\s*$""")
// ^(?:\s|$)
private val STARLARK_INDENTED_RE = Regex("""^(?:\s|$)""")

// parse_and_remove_sections — Rust
// ^# ([\w -]+)\s*$
private val RUST_SECTION_RE = Regex("""^# ([\w -]+)\s*$""")
// ^.*
private val RUST_INDENTED_RE = Regex("""^.*""")

// parse_params
// ^\*{0,2}(\w+):\s*(.*)
private val STARLARK_ARG_RE = Regex("""^\*{0,2}(\w+):\s*(.*)""")
// ^(?:\* )?`(\w+)`:?\s*(.*)
private val RUST_ARG_RE = Regex("""^(?:\* )?`(\w+)`:?\s*(.*)""")
// ^(?:\s|$)
private val PARAM_INDENTED_RE = Regex("""^(?:\s|$)""")

// --- DocString parsing extensions ---

/// impl DocString

/// Extracts the docstring from a function or module body, iff the first
/// statement is a string literal.
// pub(crate) fn extract_raw_starlark_docstring<P: AstPayload>(body: &AstStmtP<P>) -> Option<String>
fun <P : AstPayload> DocString.Companion.extractRawStarlarkDocstring(body: AstStmtP<P>): String? {
    val stmtNode = body.node
    if (stmtNode is StmtP.Statements) {
        val first = stmtNode.stmts.firstOrNull() ?: return null
        val firstNode = first.node
        if (firstNode is StmtP.Expression) {
            val exprSpanned = firstNode.expr
            val exprNode = exprSpanned.node
            if (exprNode is ExprP.Literal) {
                val lit = exprNode.literal
                if (lit is AstLiteral.String) {
                    return lit.value.node
                }
            }
        }
    }
    return null
}

// fn split_summary_details(s: &str) -> Option<(&str, &str, &str)>
private fun splitSummaryDetails(s: String): Triple<String, String, String>? {
    val examplesString = "Examples:\n"

    var summaryLen = 0
    val examplesIdx = s.indexOf(examplesString)

    // split_inclusive('\n') equivalent: iterate lines keeping the newline
    var pos = 0
    while (pos < s.length) {
        val nlIdx = s.indexOf('\n', pos)
        val lineEnd = if (nlIdx == -1) s.length else nlIdx + 1
        val line = s.substring(pos, lineEnd)

        if (line.trim().isEmpty()) {
            val detailsStart = summaryLen + line.length

            return if (examplesIdx != -1) {
                Triple(
                    s.substring(0, summaryLen).trim(),
                    s.substring(detailsStart, examplesIdx),
                    s.substring(examplesIdx + examplesString.length),
                )
            } else {
                Triple(
                    s.substring(0, summaryLen).trim(),
                    s.substring(detailsStart),
                    "",
                )
            }
        } else {
            summaryLen += line.length
        }
        pos = lineEnd
    }
    return null
}

// fn normalize_summary(summary: &str) -> String
private fun normalizeSummary(summary: String): String {
    return buildString(summary.length) {
        for (line in summary.lines()) {
            if (isNotEmpty()) {
                append(' ')
            }
            append(line.trim())
        }
    }
}

/// Do common work to parse a docstring (dedenting, splitting summary and details, etc).
// pub fn from_docstring(kind: DocStringKind, user_docstring: &str) -> Option<DocString>
fun DocString.Companion.fromDocstring(kind: DocStringKind, userDocstring: String): DocString? {
    val trimmedDocs = userDocstring.trim()
    if (trimmedDocs.isEmpty()) {
        return null
    }

    val split = splitSummaryDetails(trimmedDocs)
    val summary: String
    val details: String?
    val examples: String?

    if (split != null && split.first.isNotEmpty()) {
        val (rawSummary, rawDetails, rawExamples) = split

        details = if (rawDetails.trim().isEmpty()) {
            null
        } else {
            // Dedent the details separately so that people can have the summary on the
            // same line as the opening quotes, and the details indented on subsequent lines.
            when (kind) {
                DocStringKind.Starlark -> dedent(rawDetails).trim()
                DocStringKind.Rust -> removeRustComments(dedent(rawDetails).trim())
            }
        }

        examples = if (rawExamples.isEmpty()) {
            null
        } else {
            when (kind) {
                DocStringKind.Starlark -> dedent(rawExamples).trim()
                DocStringKind.Rust -> removeRustComments(dedent(rawExamples).trim())
            }
        }

        summary = rawSummary
    } else {
        summary = trimmedDocs
        details = null
        examples = null
    }

    val normalizedSummary = normalizeSummary(summary)

    return DocString(
        summary = normalizedSummary,
        details = details,
        examples = examples,
    )
}

/// Removes rustdoc-style commented out lines from code blocks.
// fn remove_rust_comments(details: &str) -> String
private fun removeRustComments(details: String): String {
    return CODEBLOCK_RE.replace(details) { matchResult ->
        val lang = matchResult.groupValues[1]
        val fullMatch = matchResult.value
        when (lang) {
            "", "rust" -> COMMENT_RE.replace(fullMatch, "")
            else -> fullMatch
        }
    }
}

/// Join lines up, dedent them, and trim them.
// fn join_and_dedent_lines(lines: &[String]) -> String
private fun joinAndDedentLines(lines: List<String>): String {
    return dedent(lines.joinToString("\n")).trim()
}

/// Parse the sections out of a docstring's `details` text, and remove the requested
/// sections from the text.
///
/// "sections" are the various things in doc strings like "Arguments:", "Returns:", etc
///
/// Returns a new instance of [DocString] with the requested sections removed,
/// and a mapping of section name (lower case) to the cleaned up section text.
// fn parse_and_remove_sections(self, kind: DocStringKind, requested_sections: &[&str]) -> (Self, HashMap<String, String>)
private fun DocString.parseAndRemoveSections(
    kind: DocStringKind,
    requestedSections: List<String>,
): Pair<DocString, Map<String, String>> {
    val sections = mutableMapOf<String, String>()

    var currentSection: String? = null
    var currentSectionText = mutableListOf<String>()

    fun finishSection() {
        val s = currentSection
        if (s != null) {
            sections[s] = joinAndDedentLines(currentSectionText)
            currentSectionText.clear()
            currentSection = null
        }
    }

    val sectionRe: Regex
    val indentedRe: Regex
    when (kind) {
        DocStringKind.Starlark -> {
            sectionRe = STARLARK_SECTION_RE
            indentedRe = STARLARK_INDENTED_RE
        }
        DocStringKind.Rust -> {
            sectionRe = RUST_SECTION_RE
            indentedRe = RUST_INDENTED_RE
        }
    }

    val currentDetails = this.details
    if (currentDetails != null) {
        val newDetails = mutableListOf<String>()

        for (line in currentDetails.lines()) {
            val sectionMatch = sectionRe.matchEntire(line)
            if (sectionMatch != null) {
                finishSection()

                val foundSection = sectionMatch.groupValues[1].lowercase()
                if (foundSection in requestedSections) {
                    currentSection = foundSection
                } else {
                    newDetails.add(line)
                }
            } else if (currentSection != null && indentedRe.containsMatchIn(line)) {
                currentSectionText.add(line)
            } else {
                newDetails.add(line)
                finishSection()
            }
        }

        finishSection()

        val joinedDetails = newDetails.joinToString("\n").trim()
        val finalDetails = joinedDetails.ifEmpty { null }

        return Pair(
            DocString(
                summary = this.summary,
                details = finalDetails,
                examples = this.examples,
            ),
            sections,
        )
    } else {
        return Pair(this, sections)
    }
}

// --- DocFunction parsing extensions ---

/// impl DocFunction

/// Parses function documentation out of a docstring.
///
/// @param kind The kind of docstring. This determines the formatting that is parsed.
/// @param params The parameters of the function.
/// @param returnType The return type.
/// @param rawDocstring The raw docstring to be parsed and potentially modified.
// pub fn from_docstring(kind: DocStringKind, params: DocParams, return_type: Ty, raw_docstring: Option<&str>) -> Self
fun DocFunction.Companion.fromDocstring(
    kind: DocStringKind,
    params: DocParams,
    returnType: Ty,
    rawDocstring: String?,
): DocFunction {
    val ds = rawDocstring?.let { DocString.fromDocstring(kind, it) }
        ?: return DocFunction(
            docs = null,
            params = params,
            ret = DocReturn(
                docs = null,
                typ = returnType,
            ),
        )

    val (functionDocstring, sections) = ds.parseAndRemoveSections(
        kind,
        listOf("arguments", "args", "returns", "return"),
    )

    val argsSection = sections["arguments"] ?: sections["args"]
    if (argsSection != null) {
        val entries = parseParams(kind, argsSection)
        for (docParam in params.docParamsMut()) {
            val raw = entries[docParam.name]
            if (raw != null) {
                docParam.docs = DocString.fromDocstring(kind, raw)
            }
        }
    }

    val returnDocs = (sections["return"] ?: sections["returns"])
        ?.let { DocString.fromDocstring(kind, it) }

    return DocFunction(
        docs = functionDocstring,
        params = params,
        ret = DocReturn(
            docs = returnDocs,
            typ = returnType,
        ),
    )
}

/// Parse out parameter docs from an "Args:" section of a docstring.
///
/// `argsSection` should be dedented, and generally should just be the `args` key of
/// the [DocString.parseAndRemoveSections] function call.
// fn parse_params(kind: DocStringKind, args_section: &str) -> HashMap<String, String>
private fun parseParams(kind: DocStringKind, argsSection: String): Map<String, String> {
    val argRe = when (kind) {
        DocStringKind.Starlark -> STARLARK_ARG_RE
        DocStringKind.Rust -> RUST_ARG_RE
    }

    val ret = mutableMapOf<String, String>()
    var currentArg: String? = null
    var currentText = mutableListOf<String>()

    for (line in argsSection.lines()) {
        val argMatch = argRe.matchEntire(line)
        if (argMatch != null) {
            val a = currentArg
            if (a != null) {
                ret[a] = joinAndDedentLines(currentText)
            }

            currentArg = argMatch.groupValues[1]

            val docMatchStr = argMatch.groupValues[2]
            val docMatchStart = argMatch.groups[2]!!.range.first
            currentText = mutableListOf(
                " ".repeat(docMatchStart) + docMatchStr
            )
        } else if (currentArg != null && PARAM_INDENTED_RE.containsMatchIn(line)) {
            currentText.add(line)
        }
    }

    val a = currentArg
    if (a != null) {
        ret[a] = joinAndDedentLines(currentText)
    }

    return ret
}

// #[cfg(test)] mod tests { ... }
// Tests are in commonTest, not here.
