// port-lint: tests src/eval/runtime/profile/heap.rs
package io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.heap

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
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Evaluator
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.mode.ProfileMode
import io.github.kotlinmania.starlark_kotlin.eval.evalFunction
import io.github.kotlinmania.starlark_kotlin.eval.evalModule
import io.github.kotlinmania.starlark_kotlin.syntax.AstModule
import io.github.kotlinmania.starlark_kotlin.syntax.dialect.Dialect
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import kotlin.test.Test

internal class HeapTest {
    @Test
    fun testProfiling() {
        val ast = AstModule.parse(
            "foo.bzl",
            """
def f(x):
    return (x * 5) + 3
y = 8 * 9 + 2
f
""",
            Dialect.AllOptionsInternal,
        ).getOrThrow()
        val globals = Globals.standard()
        Heap.temp { heap ->
            val module = Module.withHeap(heap)
            val module2 = Module.withHeap(heap)
            val module3 = Module.withHeap(heap)

            val eval = Evaluator(module)
            eval.enableProfile(ProfileMode.HeapSummaryAllocated)
            val f = eval.evalModule(ast, globals).getOrThrow()

            HeapProfile.writeSummarizedHeapProfile(module.heap())
            HeapProfile.writeFlameHeapProfile(module.heap())

            val eval2 = Evaluator(module2)
            eval2.enableProfile(ProfileMode.HeapSummaryAllocated)
            eval2.evalFunction(f, listOf(Value.testingNewInt(100)), listOf()).getOrThrow()

            HeapProfile.writeSummarizedHeapProfile(module2.heap())
            HeapProfile.writeFlameHeapProfile(module2.heap())

            val eval3 = Evaluator(module3)
            module3.heap().allocStr("Thing that goes before")
            eval3.enableProfile(ProfileMode.HeapSummaryAllocated)
            eval3.evalFunction(f, listOf(Value.testingNewInt(100)), listOf()).getOrThrow()

            module3.heap().allocStr("Thing that goes after")
            HeapProfile.writeSummarizedHeapProfile(module3.heap())
            HeapProfile.writeFlameHeapProfile(module3.heap())
        }
    }
}
