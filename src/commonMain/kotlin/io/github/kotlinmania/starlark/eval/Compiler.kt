// port-lint: source src/eval/compiler.rs
package io.github.kotlinmania.starlark.eval.compiler

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

import io.github.kotlinmania.starlark.codemap.CodeMap
import io.github.kotlinmania.starlark.environment.Globals
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
// ModuleScopeData, ScopeId, ScopeNames are in the same package (eval.compiler)
import io.github.kotlinmania.starlark.eval.runtime.FrameSpan
import io.github.kotlinmania.starlark.typing.EvalException
import io.github.kotlinmania.starlark.values.FrozenRef

/**
 * Compiler module.
 *
 * This module mirrors `src/eval/compiler.rs` which declares submodules, error
 * helpers, and the [Compiler] struct.
 *
 * ## Submodules
 *
 * | Rust submodule    | Kotlin package                          |
 * |-------------------|-----------------------------------------|
 * | `args`            | `eval.compiler.args`                    |
 * | `call`            | `eval.compiler.call`                    |
 * | `compr`           | `eval.compiler.compr`                   |
 * | `constants`       | `eval.compiler.constants`               |
 * | `def`             | `eval.compiler.def`                     |
 * | `def_inline`      | `eval.compiler.def_inline`              |
 * | `error`           | `eval.compiler.error`                   |
 * | `expr`            | `eval.compiler.expr`                    |
 * | `expr_bool`       | `eval.compiler.expr_bool`               |
 * | `known`           | `eval.compiler.known`                   |
 * | `module`          | `eval.compiler.module`                  |
 * | `opt_ctx`         | `eval.compiler.opt_ctx`                 |
 * | `scope`           | `eval.compiler.scope`                   |
 * | `small_vec_1`     | `eval.compiler.small_vec_1`             |
 * | `span`            | `eval.compiler.span`                    |
 * | `stmt`            | `eval.compiler.stmt`                    |
 * | `types`           | `eval.compiler.types`                   |
 */

// ) -> EvalException

/**
 * Attach span information to an error, converting it to an [EvalException].
 */
internal fun addSpanToExprError(
    e: Throwable,
    span: FrameSpan,
    eval: Evaluator,
): EvalException =
    EvalException.newWithCallStack(e, span.span.span(), span.span.file().value) {
        listOf(eval.callStack.toDiagnosticFrames(span.inlinedFrames))
    }

/**
 * Convert a [Result] error to a spanned evaluation exception.
 */
internal fun <T> exprThrow(
    r: Result<T>,
    span: FrameSpan,
    eval: Evaluator,
): T = r.getOrElse { e -> throw addSpanToExprError(e, span, eval) }

/**
 * Convert a Starlark [Result] error to a spanned evaluation exception.
 */
internal fun <T> exprThrowStarlarkResult(
    r: Result<T>,
    span: FrameSpan,
    eval: Evaluator,
): T = r.getOrElse { e -> throw addSpanToExprError(e, span, eval) }

/**
 * The expression/statement compiler.
 *
 * Holds references to the evaluator, scope data, globals, and codemap needed
 * during compilation from AST to bytecode or IR.
 */
internal class Compiler(
    val eval: Evaluator,
    val scopeData: ModuleScopeData,
    val locals: MutableList<ScopeId> = mutableListOf(),
    val globals: FrozenRef<Globals>,
    val codemap: FrozenRef<CodeMap>,
    var checkTypes: Boolean,
    var topLevelStmtCount: Int,
    /** Set with `@starlark-rust: typecheck`. */
    var typecheck: Boolean,
) {
    fun enterScope(scopeId: ScopeId) {
        locals.add(scopeId)
    }

    fun exitScope(): ScopeId = locals.removeLast()

    fun currentScope(): ScopeNames = scopeData.getScope(locals.last())
}
