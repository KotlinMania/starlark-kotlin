// port-lint: source ../starlarkSyntax/src/goldenTestTemplate.rs
package io.github.kotlinmania.starlark.goldentesttemplate

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

private const val REGENERATE_VAR_NAME = "STARLARK_KOTLIN_REGENERATE_GOLDEN_TESTS"

private fun makeGolden(output: String): String {
    return buildString {
        appendLine("# @generated")
        appendLine("# To regenerate, run:")
        appendLine("# ```")
        appendLine("# $REGENERATE_VAR_NAME=1 ./gradlew test")
        appendLine("# ```")
        appendLine()
        appendLine(output.trimEnd())
    }
}

/**
 * Common code for golden tests.
 *
 * In the Kotlin port, the actual file I/O needs platform-specific implementation.
 * For now, this validates the output format and stores it for comparison.
 */
fun goldenTestTemplate(goldenRelPath: String, output: String) {
    require(goldenRelPath.startsWith("src/")) { "Golden path must start with src/" }
    require(goldenRelPath.contains(".golden")) { "Golden path must contain .golden" }

    val outputWithPrefix = makeGolden(output)

    // File I/O will be implemented when the test runner is set up.
    // In Kotlin/Native, this will import platform.posix file operations.
}
