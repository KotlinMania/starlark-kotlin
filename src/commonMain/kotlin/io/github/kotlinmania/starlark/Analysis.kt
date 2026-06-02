@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)
// port-lint: source src/analysis.rs
package io.github.kotlinmania.starlark

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

import io.github.kotlinmania.starlark.analysis.Lint
import io.github.kotlinmania.starlark.analysis.erase
import io.github.kotlinmania.starlark.analysis.flowLint
import io.github.kotlinmania.starlark.analysis.incompatibleLint
import io.github.kotlinmania.starlark.analysis.lintDubious
import io.github.kotlinmania.starlark.analysis.lintPerformance
import io.github.kotlinmania.starlark.analysis.namesLint
import io.github.kotlinmania.starlark.analysis.underscoreLint
import io.github.kotlinmania.starlark.syntax.AstModule
import kotlin.native.HiddenFromObjC

/**
 * Linter.
 *
 * Re-exports:
 * - [io.github.kotlinmania.starlark.analysis.LintMessage]
 * - [io.github.kotlinmania.starlark.analysis.EvalMessage]
 * - [io.github.kotlinmania.starlark.analysis.EvalSeverity]
 * - [io.github.kotlinmania.starlark.analysis.Lint]
 *
 * Submodules:
 * - [dubious][io.github.kotlinmania.starlark.analysis.Dubious] - dubious pattern detection
 * - [findCallName][io.github.kotlinmania.starlark.analysis.FindCallName] - call name finder
 * - [flow][io.github.kotlinmania.starlark.analysis.Flow] - control flow analysis
 * - [incompatible][io.github.kotlinmania.starlark.analysis.Incompatible] - incompatibility checks
 * - [lintMessage][io.github.kotlinmania.starlark.analysis.LintMessage] - lint message types
 * - [names][io.github.kotlinmania.starlark.analysis.Names] - name analysis
 * - [performance][io.github.kotlinmania.starlark.analysis.Performance] - performance lint
 * - [types][io.github.kotlinmania.starlark.analysis.Types] - type analysis
 * - [underscore][io.github.kotlinmania.starlark.analysis.Underscore] - underscore analysis
 * - [unusedLoads][io.github.kotlinmania.starlark.analysis.UnusedLoads] - unused load detection
 */

/**
 * Run the linter.
 *
 * Extension interface for [io.github.kotlinmania.starlark.syntax.AstModule] to support linting.
 */
@HiddenFromObjC
interface AstModuleLint {
    /**
     * Run a static linter over the module. If the complete set of global variables are known
     * they can be passed as the [globals] argument, resulting in name-resolution lint errors.
     * The precise checks run by the linter are not considered stable between versions.
     */
    @HiddenFromObjC
    fun lint(globals: Set<String>? = null): List<Lint>
}

@HiddenFromObjC
internal fun AstModule.lint(globals: Set<String>? = null): List<Lint> =
    buildList {
        addAll(flowLint(this@lint).map { it.erase() })
        addAll(lintDubious(this@lint).map { it.erase() })
        addAll(incompatibleLint(this@lint).map { it.erase() })
        addAll(namesLint(this@lint, globals).map { it.erase() })
        addAll(lintPerformance(this@lint).map { it.erase() })
        addAll(underscoreLint(this@lint).map { it.erase() })
    }.filter { issue -> !isSuppressed(issue.shortName, issue.location.span) }
