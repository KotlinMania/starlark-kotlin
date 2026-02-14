// port-lint: source src/analysis.rs
package io.github.kotlinmania.starlark_kotlin.analysis

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

/// Linter.

// Module gateway: sub-modules
// dubious
// find_call_name
// flow
// incompatible
// lint_message
// names
// performance
// types
// underscore
// unused_loads

// Placeholder types referenced from other modules
// These will be replaced with real imports as the port progresses
class Span
class FileSpan(val span: Span, val description: String) {
    fun begin(): Int = 0
}

class AstModule {
    fun isSuppressed(shortName: String, span: Span): Boolean = false
}

class Lint(
    val shortName: String,
    val problem: String,
    val original: String,
    val location: FileSpan,
)

class LintT<T>(val inner: T) {
    fun erase(): Lint = Lint("", "", "", FileSpan(Span(), ""))
}

enum class EvalSeverity { Warning, Error }

class EvalMessage
class LintMessage

/// Run the linter.
interface AstModuleLint {
    /// Run a static linter over the module. If the complete set of global variables are known
    /// they can be passed as the `globals` argument, resulting in name-resolution lint errors.
    /// The precise checks run by the linter are not considered stable between versions.
    fun lint(globals: Set<String>?): List<Lint>
}

fun AstModule.lint(globals: Set<String>?): List<Lint> {
    val res = mutableListOf<Lint>()
    // res.addAll(flow.lint(this).map { it.erase() })
    // res.addAll(incompatible.lint(this).map { it.erase() })
    // res.addAll(dubious.lint(this).map { it.erase() })
    // res.addAll(names.lint(this, globals).map { it.erase() })
    // res.addAll(underscore.lint(this).map { it.erase() })
    // res.addAll(performance.lint(this).map { it.erase() })
    res.retainAll { issue -> !this.isSuppressed(issue.shortName, issue.location.span) }
    return res
}
