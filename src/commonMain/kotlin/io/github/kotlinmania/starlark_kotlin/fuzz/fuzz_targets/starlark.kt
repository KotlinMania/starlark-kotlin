// port-lint: source fuzz/fuzz_targets/starlark.rs
package io.github.kotlinmania.starlark_kotlin.fuzz.fuzz_targets

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

import io.github.kotlinmania.starlark_kotlin.environment.Globals
import io.github.kotlinmania.starlark_kotlin.environment.Module
import io.github.kotlinmania.starlark_kotlin.eval.Evaluator
import io.github.kotlinmania.starlark_kotlin.syntax.AstModule
import io.github.kotlinmania.starlark_kotlin.syntax.Dialect

// fn run_arbitrary_starlark_err(content: &str) -> starlark::Result<String>
private fun runArbitraryStarlarkErr(content: String): Result<String> {
    val ast = AstModule.parse("hello_world.star", content, Dialect.Standard)
        .getOrElse { return Result.failure(it) }
    val globals = Globals.standard()
    return Module.withTempHeap { module ->
        val eval = Evaluator.new(module)
        val value = eval.evalModule(ast, globals)
            .getOrElse { return@withTempHeap Result.failure(it) }
        Result.success(value.toString())
    }
}

// fn run_arbitrary_starlark(content: &str) -> String
private fun runArbitraryStarlark(content: String): String {
    return when (val result = runArbitraryStarlarkErr(content)) {
        else -> {
            if (result.isSuccess) {
                result.getOrThrow()
            } else {
                val INTERNAL_ERROR = "(internal error)"
                val e = result.exceptionOrNull()!!
                val s = e.toString()
                // We want to spot internal errors, but not encourage the fuzzer
                // to write internal error in the input.
                // A sufficiently smart fuzzer might outwit us, but hopefully not too quickly.
                if (s.contains(INTERNAL_ERROR) && !content.contains(INTERNAL_ERROR)) {
                    error("Internal error as Exception: $s")
                }
                s
            }
        }
    }
}

// fuzz_target!(|content: &str| { ... });
/// Entry point for fuzz testing. Call with arbitrary string content.
fun fuzzTarget(content: String) {
    @Suppress("UNUSED_VARIABLE")
    val ignore = runArbitraryStarlark(content)
}
