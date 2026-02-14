// port-lint: source src/tests/replace_binary.rs
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

//! Run Go implementation tests.

import io.github.kotlinmania.starlark_kotlin.environment.Globals
import io.github.kotlinmania.starlark_kotlin.environment.Module
import io.github.kotlinmania.starlark_kotlin.eval.Evaluator
import io.github.kotlinmania.starlark_kotlin.syntax.AstModule
import io.github.kotlinmania.starlark_kotlin.syntax.Dialect
import kotlin.test.Test
import kotlin.test.assertEquals

class ReplaceBinaryTests {

    @Test
    fun testReplaceBinary() {
        val ast = AstModule.parse(
            "file.sky",
            """
def equals(a, b):
    return "(" + str(a) + " == " + str(b) + ")"

def my_subtract(a, b):
    return "(" + str(a) + " - " + str(b) + ")"

(7 + 8) - 9 == True
    """,
            Dialect.Standard,
        ).getOrThrow()
        ast.replaceBinaryOperators(mapOf(
            "==" to "equals",
            "-" to "my_subtract",
        ))
        Module.withTempHeap { module ->
            val eval = Evaluator(module)
            val v = eval.evalModule(ast, Globals.standard()).getOrThrow()
            assertEquals("((15 - 9) == True)", v.unpackStr())
            Result.success(Unit)
        }.getOrThrow()
    }
}
