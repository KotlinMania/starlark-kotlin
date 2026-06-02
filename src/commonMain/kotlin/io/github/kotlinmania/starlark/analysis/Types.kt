// port-lint: source src/analysis/types.rs
package io.github.kotlinmania.starlark.analysis

import io.github.kotlinmania.starlark.codemap.FileSpan
import io.github.kotlinmania.starlark.codemap.ResolvedFileSpan

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

class Lint(
    /** Which code location does this lint refer to. */
    val location: FileSpan,
    /** kebab-case constant describing this issue, e.g. `missing-return`. */
    val shortName: String,
    /**
     * Is this code highly-likely to be wrong, rather
     * than merely stylistically non-ideal.
     */
    val severity: EvalSeverity,
    /** A description of the underlying problem. */
    val problem: String,
    /** The source code at [location]. */
    val original: String,
) {
    override fun toString(): String = "$location: $problem"
}

/** Erase the typed problem into a generic [Lint]. */
internal fun <T> LintT<T>.erase(): Lint where T : LintWarning =
    Lint(
        location = this.location,
        shortName = this.problem.shortName(),
        severity = this.problem.severity(),
        problem = this.problem.toString(),
        original = this.location.sourceSpan(),
    )

/** A standardised set of severities. */
// Note: EvalSeverity is already defined in flow.kt.
// The Rust version also has Error, Warning, Advice, Disabled.
// flow.kt currently has: Disabled, Warning, Error.
// When unified, add Advice.

/** Potential problems that occurred while parsing a starlark program. */
class EvalMessage(
    /** The path to the starlark program. */
    val path: String,
    /** If present, where in the program the problem occurred. */
    val span: ResolvedFileSpan? = null,
    /** How severe the problem is. */
    val severity: EvalSeverity,
    /** The general name of the issue. */
    val name: String,
    /** The details of the issue, generally displayed to the user. */
    val description: String,
    /** The full error details. */
    val fullErrorWithSpan: String? = null,
    /** The text referred to by [span]. */
    val original: String? = null,
) {
    override fun toString(): String =
        buildString {
            append("$severity: $path:")
            if (span != null) {
                append("$span")
            }
            append(" $description")
        }

    companion object {
        /** Produce an `EvalMessage` from a `starlark::Error`. */
        fun fromError(file: String, err: Exception): EvalMessage = fromAnyError(file, err)

        /**
         * Create an `EvalMessage` from any kind of error.
         *
         * Prefer to use `fromError` if at all possible.
         */
        fun fromAnyError(file: String, x: Any): EvalMessage =
            EvalMessage(
                path = file,
                span = null,
                severity = EvalSeverity.Error,
                name = "error",
                description = x.toString(),
                fullErrorWithSpan = null,
                original = null,
            )

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
                original = null,
            )
        }
    }
}

internal fun Lint.toEvalMessage(): EvalMessage =
    EvalMessage(
        path = location.description,
        span = location.resolve(),
        severity = severity,
        name = shortName,
        description = problem,
        fullErrorWithSpan = null,
        original = original,
    )
