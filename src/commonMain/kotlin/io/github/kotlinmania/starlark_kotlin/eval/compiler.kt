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

import io.github.kotlinmania.starlark_kotlin.environment.Globals
import io.github.kotlinmania.starlark_kotlin.values.FrozenRef
import io.github.kotlinmania.starlark_kotlin.values.types.string.Evaluator
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ModuleScopeData
import io.github.kotlinmania.starlark_kotlin.typing.error.EvalException
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.ModuleScopeData
import io.github.kotlinmania.starlark_kotlin.eval.runtime.inlinedFrames
import io.github.kotlinmania.starlark_kotlin.eval.runtime.callStack
import io.github.kotlinmania.starlark_kotlin.analysis.span
import io.github.kotlinmania.starlark_kotlin.codemap.CodeMap

// pub(crate) mod args;
// pub(crate) mod call;
// pub(crate) mod compr;
// pub(crate) mod constants;
// pub(crate) mod def;
// pub(crate) mod def_inline;
// pub(crate) mod error;
// pub(crate) mod expr;
// pub(crate) mod expr_bool;
// pub(crate) mod known;
// pub(crate) mod module;
// pub(crate) mod opt_ctx;
// pub(crate) mod scope;
// pub(crate) mod small_vec_1;
// pub(crate) mod span;
// pub(crate) mod stmt;
// pub(crate) mod types;

// #[cold]
// #[inline(never)]
// pub(crate) fn add_span_to_expr_error(e: crate::Error, span: FrameSpan, eval: &Evaluator) -> EvalException
internal fun addSpanToExprError(
    e: Exception,
    span: FrameSpan,
    eval: Evaluator,
): EvalException {
    return EvalException.newWithCallstack(e, span.span.span(), span.span.file()) {
        eval.callStack.toDiagnosticFrames(span.inlinedFrames)
    }
}

/// Convert syntax error to spanned evaluation exception.
// #[inline(always)]
// pub(crate) fn expr_throw<'v, T>(r: crate::Result<T>, span: FrameSpan, eval: &Evaluator) -> Result<T, EvalException>
internal fun <T> exprThrow(
    r: Result<T>,
    span: FrameSpan,
    eval: Evaluator,
): T {
    return r.getOrElse { e ->
        throw addSpanToExprError(e as Exception, span, eval)
    }
}

/// Convert syntax error to spanned evaluation exception.
// #[inline(always)]
// pub(crate) fn expr_throw_starlark_result<'v, T>(r: crate::Result<T>, span: FrameSpan, eval: &Evaluator) -> Result<T, EvalException>
internal fun <T> exprThrowStarlarkResult(
    r: Result<T>,
    span: FrameSpan,
    eval: Evaluator,
): T {
    return r.getOrElse { e ->
        throw addSpanToExprError(e as Exception, span, eval)
    }
}

// pub(crate) struct Compiler<'v, 'a, 'e, 'x> {
//     pub(crate) eval: &'x mut Evaluator<'v, 'a, 'e>,
//     pub(crate) scope_data: ModuleScopeData<'x>,
//     pub(crate) locals: Vec<ScopeId>,
//     pub(crate) globals: FrozenRef<'static, Globals>,
//     pub(crate) codemap: FrozenRef<'static, CodeMap>,
//     pub(crate) check_types: bool,
//     pub(crate) top_level_stmt_count: usize,
//     pub(crate) typecheck: bool,
// }
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
    // impl Compiler<'_, '_, '_, '_>

    // pub(crate) fn enter_scope(&mut self, scope_id: ScopeId)
    internal fun enterScope(scopeId: ScopeId) {
        locals.add(scopeId)
    }

    // pub(crate) fn exit_scope(&mut self) -> ScopeId
    internal fun exitScope(): ScopeId {
        return locals.removeLast()
    }

    // pub(crate) fn current_scope(&self) -> &ScopeNames<'_>
    internal fun currentScope(): ScopeNames {
        return scopeData.getScope(locals.last())
    }
}
