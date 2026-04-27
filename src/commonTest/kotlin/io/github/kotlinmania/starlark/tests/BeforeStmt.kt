// port-lint: source src/tests/beforeStmt.rs
package io.github.kotlinmania.starlark.tests

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

import io.github.kotlinmania.starlark.environment.Globals
import io.github.kotlinmania.starlark.environment.Module
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.codemap.FileSpanRef
import io.github.kotlinmania.starlark.syntax.dialect.Dialect
import io.github.kotlinmania.starlark.eval.runtime.beforeStmtFn
import io.github.kotlinmania.starlark.eval.evalModule
import io.github.kotlinmania.starlark.syntax.AstModule

internal fun beforeStmt() {
    Module.withTempHeap { module ->
        val globals = Globals.new()
        var counter = 0
        val beforeStmt = { span: FileSpanRef, _continued: Boolean, eval: Evaluator ->
            counter += 1
        }

        val evaluator = Evaluator(module)
        evaluator.beforeStmtFn(beforeStmt)

        // For a top-level statement, we get an additional beforeStmt call for the possible gc, and one after each call instruction
        val program = "" +
            "x = 1          # 0 + 1\n" +
            "def f():       # 1 + 1\n" +
            "  return x + 1 # 3\n" +
            "f()            # 2 + 1 + 1\n"
        val ast = AstModule.parse("a.star", program, Dialect.AllOptionsInternal).getOrThrow()
        evaluator.evalModule(ast, globals).getOrThrow()
        check(8 == counter)
    }
}
