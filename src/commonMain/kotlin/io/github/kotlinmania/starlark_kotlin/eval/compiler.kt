// port-lint: source src/eval/compiler.rs
package io.github.kotlinmania.starlark_kotlin.eval.compiler

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

import io.github.kotlinmania.starlark_kotlin.codemap.CodeMap
import io.github.kotlinmania.starlark_kotlin.environment.Globals
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Evaluator
import io.github.kotlinmania.starlark_kotlin.eval.runtime.FrameSpan
import io.github.kotlinmania.starlark_kotlin.typing.error.EvalException
import io.github.kotlinmania.starlark_kotlin.values.FrozenRef

// Rust: add_span_to_expr_error(e, span, eval) -> EvalException
internal fun addSpanToExprError(
    e: Exception,
    span: FrameSpan,
    eval: Evaluator,
): EvalException {
    val callStack = eval.callStack.toDiagnosticFrames(span.inlinedFrames)
    return EvalException("${e.message} at ${span.span.span()} in ${span.span.file()}", e)
}

/** Convert syntax error to spanned evaluation exception. */
// Rust: expr_throw<T>(r, span, eval) -> Result<T, EvalException>
internal fun <T> exprThrow(
    r: Result<T>,
    span: FrameSpan,
    eval: Evaluator,
): T {
    return r.getOrElse { e ->
        throw addSpanToExprError(e as Exception, span, eval)
    }
}

/** Convert syntax error to spanned evaluation exception. */
// Rust: expr_throw_starlark_result<T>(r, span, eval) -> Result<T, EvalException>
internal fun <T> exprThrowStarlarkResult(
    r: Result<T>,
    span: FrameSpan,
    eval: Evaluator,
): T {
    return r.getOrElse { e ->
        throw addSpanToExprError(e as Exception, span, eval)
    }
}

/** Compiler state for transforming AST into executable form. */
internal class Compiler(
    internal val eval: Evaluator,
    internal val scopeData: ModuleScopeData,
    internal val locals: MutableList<ScopeId>,
    internal val globals: FrozenRef<Globals>,
    internal val codemap: FrozenRef<CodeMap>,
    internal val checkTypes: Boolean,
    internal var topLevelStmtCount: Int,
    /** Set with `@starlark-rust: typecheck`. */
    internal val typecheck: Boolean,
) {
    internal fun enterScope(scopeId: ScopeId) {
        locals.add(scopeId)
    }

    internal fun exitScope(): ScopeId {
        return locals.removeLast()
    }

    internal fun currentScope(): ScopeNames {
        return scopeData.getScope(locals.last())
    }
}
