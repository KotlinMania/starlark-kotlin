// port-lint: ignore
package io.github.kotlinmania.starlark.syntax.dialect

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

/** How to handle type annotations in Starlark. */
enum class DialectTypes {
    /** Prohibit types at parse time. */
    Disable,
    /** Allow types at parse time, but ignore types at runtime. */
    ParseOnly,
    /** Check types at runtime. */
    Enable
}

/** Starlark language features to enable. */
data class Dialect(
    val enableDef: Boolean,
    val enableLambda: Boolean,
    val enableLoad: Boolean,
    val enableKeywordOnlyArguments: Boolean,
    val enablePositionalOnlyArguments: Boolean,
    val enableTypes: DialectTypes,
    val enableLoadReexport: Boolean,
    val enableTopLevelStmt: Boolean,
    val enableFStrings: Boolean
) {
    companion object {
        /** Follow the Starlark language standard as much as possible. */
        val Standard = Dialect(
            enableDef = true,
            enableLambda = true,
            enableLoad = true,
            enableKeywordOnlyArguments = false,
            enablePositionalOnlyArguments = false,
            enableTypes = DialectTypes.Disable,
            enableLoadReexport = true,
            enableTopLevelStmt = false,
            enableFStrings = false
        )

        /** This option is deprecated. Extend Standard instead. */
        val Extended = Dialect(
            enableDef = true,
            enableLambda = true,
            enableLoad = true,
            enableKeywordOnlyArguments = true,
            enablePositionalOnlyArguments = false,
            enableTypes = DialectTypes.Enable,
            enableLoadReexport = true,
            enableTopLevelStmt = true,
            enableFStrings = false
        )

        /** Only for starlark tests. */
        val AllOptionsInternal = Dialect(
            enableDef = true,
            enableLambda = true,
            enableLoad = true,
            enableKeywordOnlyArguments = true,
            enablePositionalOnlyArguments = true,
            enableTypes = DialectTypes.Enable,
            enableLoadReexport = true,
            enableTopLevelStmt = true,
            enableFStrings = true
        )
    }
}
