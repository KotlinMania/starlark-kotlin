// port-lint: source src/eval/compiler/opt_ctx.rs
package io.github.kotlinmania.starlark_kotlin.eval.compiler.opt_ctx

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

import io.github.kotlinmania.starlark_kotlin.environment.FrozenModuleData
import io.github.kotlinmania.starlark_kotlin.eval.compiler.stmt.OptimizeOnFreezeContext
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.types.string.Evaluator

/**
 * Trait for optimization context evaluation.
 *
 * Abstracts over [OptimizeOnFreezeContext] and [Evaluator] so that
 * optimization passes can work in both compilation and freeze contexts.
 */
internal interface OptCtxEval {
    fun heap(): Heap
    fun frozenHeap(): FrozenHeap
    fun eval(): Evaluator?
    fun frozenModule(): FrozenModuleData?
}

/** [OptCtxEval] implementation for [OptimizeOnFreezeContext]. */
internal class OptCtxEvalForOptimizeOnFreeze(
    private val ctx: OptimizeOnFreezeContext,
) : OptCtxEval {
    override fun heap(): Heap = ctx.heap
    override fun frozenHeap(): FrozenHeap = ctx.frozenHeap
    override fun eval(): Evaluator? = null
    override fun frozenModule(): FrozenModuleData = ctx.module
}

/** [OptCtxEval] implementation for [Evaluator]. */
internal class OptCtxEvalForEvaluator(
    private val evaluator: Evaluator,
) : OptCtxEval {
    override fun heap(): Heap = evaluator.heap()
    override fun frozenHeap(): FrozenHeap = evaluator.frozenHeap()
    override fun eval(): Evaluator = evaluator
    override fun frozenModule(): FrozenModuleData? = null
}

/**
 * Optimization context.
 *
 * We perform optimization:
 * - during compilation of AST to IR, and
 * - when freezing the heap.
 */
internal class OptCtx(
    internal val eval: OptCtxEval,
    /** Current function parameter slot count. Zero when compiling module. */
    internal val paramCount: UInt,
) {
    companion object {
        fun new(eval: OptCtxEval, paramCount: UInt): OptCtx {
            return OptCtx(eval, paramCount)
        }
    }

    fun heap(): Heap = eval.heap()

    fun frozenHeap(): FrozenHeap = eval.frozenHeap()

    fun eval(): Evaluator? = eval.eval()

    fun frozenModule(): FrozenModuleData? = eval.frozenModule()
}
