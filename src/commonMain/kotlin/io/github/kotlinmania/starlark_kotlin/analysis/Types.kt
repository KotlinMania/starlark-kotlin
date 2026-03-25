// port-lint: source src/analysis/types.rs
package io.github.kotlinmania.starlark_kotlin.analysis

import io.github.kotlinmania.starlark_kotlin.eval.bc.call.resolve
import io.github.kotlinmania.starlark_kotlin.codemap.ResolvedFileSpan
import io.github.kotlinmania.starlark_kotlin.codemap.FileSpan
import io.github.kotlinmania.starlark_kotlin.codemap.Span

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

/// Types supporting documentation for code written in or for Starlark.

// Note: LintWarning interface, LintT<T>, and EvalSeverity enum
// are defined in flow.kt as forward references. The canonical definitions
// from types.rs are transliterated below; they will unify once flow.kt
// is updated.

// pub(crate) trait LintWarning: Display
// Already defined in flow.kt as:
//   interface LintWarning {
//       fun severity(): EvalSeverity
//       fun shortName(): String
//   }

// pub(crate) struct LintT<T>
// Already defined in flow.kt. The Rust version also has `original: String`.
// Extension: add erase() to convert LintT<T> -> Lint.

/// A lint produced by `AstModule::lint`.
// #[derive(Debug)]
// pub struct Lint
class Lint(
    /// Which code location does this lint refer to.
    val location: FileSpan,
    /// kebab-case constant describing this issue, e.g. `missing-return`.
    val shortName: String,
    /// Is this code highly-likely to be wrong, rather
    /// than merely stylistically non-ideal.
    val severity: EvalSeverity,
    /// A description of the underlying problem.
    val problem: String,
    /// The source code at [location].
    val original: String,
) {
    // impl Display for Lint
    override fun toString(): String {
        return "$location: $problem"
    }
}

// impl<T: Display> Display for LintT<T>
// Already handled: LintT.toString() delegates via location and problem.

// impl<T: LintWarning> LintT<T>
// pub(crate) fn new(codemap: &CodeMap, span: Span, problem: T) -> Self
// Already in flow.kt as LintT.Companion.new

/// Erase the typed problem into a generic [Lint].
// pub(crate) fn erase(self) -> Lint
internal fun <T> LintT<T>.erase(): Lint where T : LintWarning {
    return Lint(
        location = this.location,
        shortName = this.problem.shortName(),
        severity = this.problem.severity(),
        problem = this.problem.toString(),
        original = "", // LintT in flow.kt lacks `original`; will be filled when unified
    )
}

/// A standardised set of severities.
// Note: EvalSeverity is already defined in flow.kt.
// The Rust version also has Error, Warning, Advice, Disabled.
// flow.kt currently has: Disabled, Warning, Error.
// When unified, add Advice.

/// Potential problems that occurred while parsing a starlark program.
// #[derive(Debug, Clone)]
// pub struct EvalMessage
class EvalMessage(
    /// The path to the starlark program.
    val path: String,
    /// If present, where in the program the problem occurred.
    val span: ResolvedFileSpan? = null,
    /// How severe the problem is.
    val severity: EvalSeverity,
    /// The general name of the issue.
    val name: String,
    /// The details of the issue, generally displayed to the user.
    val description: String,
    /// The full error details.
    val fullErrorWithSpan: String? = null,
    /// The text referred to by [span].
    val original: String? = null,
) {
    // impl Display for EvalMessage
    override fun toString(): String {
        return buildString {
            append("$severity: $path:")
            if (Span != null) {
                append("$Span")
            }
            append(" $description")
        }
    }

    companion object {
        /// Produce an `EvalMessage` from a `starlark::Error`.
        // pub fn from_error(file: &Path, err: &crate::Error) -> Self
        fun fromError(file: String, err: Exception): EvalMessage {
            // If the error has span information, use from_diagnostic.
            // Otherwise fall back to from_any_error.
            return fromAnyError(file, err)
        }

        /// Create an `EvalMessage` from any kind of error.
        ///
        /// Prefer to use `fromError` if at all possible.
        // pub fn from_any_error(file: &Path, x: &impl std::fmt::Display) -> Self
        fun fromAnyError(file: String, x: Any): EvalMessage {
            return EvalMessage(
                path = file,
                span = null,
                severity = EvalSeverity.Error,
                name = "error",
                description = x.toString(),
                fullErrorWithSpan = null,
                original = null,
            )
        }

        // fn from_diagnostic(span: &FileSpan, message: impl Display, full_error: impl Display) -> Self
        private fun fromDiagnostic(
            span: FileSpan,
            message: Any,
            fullError: Any,
        ): EvalMessage {
            val resolvedSpan = span.resolve()
            return EvalMessage(
                path = span.description,
                span = resolvedSpan,
                severity = EvalSeverity.Error,
                name = "error",
                description = message.toString(),
                fullErrorWithSpan = fullError.toString(),
                original = null, // span.source_span() not yet available
            )
        }
    }
}

// impl From<Lint> for EvalMessage
fun Lint.toEvalMessage(): EvalMessage {
    return EvalMessage(
        path = location.description,
        span = location.resolve(),
        severity = severity,
        name = shortName,
        description = problem,
        fullErrorWithSpan = null,
        original = original,
    )
}
