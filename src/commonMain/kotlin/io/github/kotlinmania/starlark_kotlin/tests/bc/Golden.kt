// port-lint: source src/tests/bc/golden.rs
package io.github.kotlinmania.starlark_kotlin.tests.bc

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

import io.github.kotlinmania.starlark_kotlin.assert.Assert
import io.github.kotlinmania.starlark_kotlin.eval.compiler.FrozenDef
import io.github.kotlinmania.starlark_kotlin.syntax.dialect.Dialect
import io.github.kotlinmania.starlark_kotlin.values.owned.downcast
import io.github.kotlinmania.starlark_kotlin.golden_test_template.goldenTestTemplate

// fn test_function_bytecode(program: &str) -> String
private fun testFunctionBytecode(program: String): String {
    val trimmed = program.trim()

    val a = Assert()
    a.dialect(Dialect.AllOptionsInternal)
    val def = a
        .module("instrs.star", trimmed)
        .get("test")!!
        .downcast<FrozenDef>()!!

    val golden = StringBuilder()
    golden.appendLine(trimmed)
    golden.appendLine()
    golden.appendLine("# Bytecode:")
    golden.appendLine()
    golden.appendLine(def.bc().dumpDebug().trim())
    return golden.toString()
}

// pub(crate) fn bc_golden_test(test_name: &str, program: &str)
internal fun bcGoldenTest(testName: String, program: String) {
    // Bytecode addresses are platform-dependent; in Kotlin/Multiplatform
    // we assume 64-bit semantics (matching the Rust 64-bit guard).

    val output = testFunctionBytecode(program)

    goldenTestTemplate("src/tests/bc/golden/$testName.golden", output)
}
