// port-lint: source src/eval/compiler/args.rs
package io.github.kotlinmania.starlark.eval.compiler.args

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

import io.github.kotlinmania.starlark.collections.symbol.Symbol
import io.github.kotlinmania.starlark.eval.compiler.Compiler
import io.github.kotlinmania.starlark.eval.compiler.ExprCompiled
import io.github.kotlinmania.starlark.eval.compiler.IrSpanned
import io.github.kotlinmania.starlark.eval.compiler.expr
import io.github.kotlinmania.starlark.eval.compiler.optctx.OptCtx
import io.github.kotlinmania.starlark.eval.compiler.optimize
import io.github.kotlinmania.starlark.eval.compiler.scope.CstPayload
import io.github.kotlinmania.starlark.eval.runtime.ArgNames
import io.github.kotlinmania.starlark.eval.runtime.Arguments
import io.github.kotlinmania.starlark.eval.runtime.ArgumentsFull
import io.github.kotlinmania.starlark.syntax.ast.ArgumentP
import io.github.kotlinmania.starlark.syntax.ast.CallArgsP
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue

// #[derive(Default, Clone, Debug, VisitSpanMut)]
// pub(crate) struct ArgsCompiledValue {
//     pub(crate) pos_named: Vec<IrSpanned<ExprCompiled>>,
//     pub(crate) names: Vec<(Symbol, FrozenStringValue)>,
//     pub(crate) args: Option<IrSpanned<ExprCompiled>>,
//     pub(crate) kwargs: Option<IrSpanned<ExprCompiled>>,
// }

/**
 * Compiled representation of function call arguments.
 *
 * Contains positional and named arguments, plus optional star-args
 * and star-star-kwargs expressions.
 */
internal class ArgsCompiledValue(
    /** Positional and named argument expressions concatenated. */
    val posNamed: MutableList<IrSpanned<ExprCompiled>> = mutableListOf(),
    /**
     * Named arguments compiled.
     *
     * Note names are guaranteed to be unique here because names are validated in AST:
     * named arguments in [ExprP.Call] are unique.
     */
    val names: MutableList<Pair<Symbol, FrozenStringValue>> = mutableListOf(),
    /** Star-args (`*args`) expression, if present. */
    var args: IrSpanned<ExprCompiled>? = null,
    /** Star-star-kwargs (`**kwargs`) expression, if present. */
    var kwargs: IrSpanned<ExprCompiled>? = null,
) {
    /**
     * Check if arguments is one positional argument.
     *
     * Returns the single positional argument if there are no named, star-args,
     * or star-star-kwargs arguments, or `null` otherwise.
     */
    // pub(crate) fn one_pos(&self) -> Option<&IrSpanned<ExprCompiled>>
    fun onePos(): IrSpanned<ExprCompiled>? =
        if (posNamed.size == 1 && names.isEmpty() && args == null && kwargs == null) {
            posNamed[0]
        } else {
            null
        }

    /**
     * Check if arguments is two positional arguments.
     *
     * Returns the two positional arguments if there are no named, star-args,
     * or star-star-kwargs arguments, or `null` otherwise.
     */
    // pub(crate) fn two_pos(&self) -> Option<(&IrSpanned<ExprCompiled>, &IrSpanned<ExprCompiled>)>
    fun twoPos(): Pair<IrSpanned<ExprCompiled>, IrSpanned<ExprCompiled>>? =
        if (posNamed.size == 2 && names.isEmpty() && args == null && kwargs == null) {
            Pair(posNamed[0], posNamed[1])
        } else {
            null
        }

    /**
     * Return all arguments if they are positional-only (no named, star-args, or star-star-kwargs).
     */
    // pub(crate) fn pos_only(&self) -> Option<&[IrSpanned<ExprCompiled>]>
    fun posOnly(): List<IrSpanned<ExprCompiled>>? =
        if (names.isEmpty() && args == null && kwargs == null) {
            posNamed
        } else {
            null
        }

    /**
     * Split [posNamed] into positional-only and named argument expressions.
     *
     * The first element contains purely positional expressions, the second contains
     * expressions corresponding to the entries in [names].
     */
    // fn split_pos_names(&self) -> (&[IrSpanned<ExprCompiled>], &[IrSpanned<ExprCompiled>])
    private fun splitPosNames(): Pair<List<IrSpanned<ExprCompiled>>, List<IrSpanned<ExprCompiled>>> {
        val splitAt = posNamed.size - names.size
        return Pair(
            posNamed.subList(0, splitAt),
            posNamed.subList(splitAt, posNamed.size),
        )
    }

    /**
     * Invoke a callback if all arguments are frozen values.
     *
     * Returns the result of [handler] if every argument expression is a compile-time
     * constant [FrozenValue], or `null` if any argument is not a frozen value.
     */
    // pub(crate) fn all_values<'v, R>(
    //     &self,
    //     handler: impl FnOnce(&Arguments<'v, '_>) -> R,
    // ) -> Option<R>
    fun <R> allValues(handler: (Arguments) -> R): R? = allValuesGeneric({ e -> e.asValue()?.toValue() }, handler)

    /**
     * Invoke a callback if all arguments can be converted to values.
     *
     * Uses [exprToValue] to attempt conversion of each expression. Returns the
     * result of [handler] if all conversions succeed, or `null` if any fails.
     */
    // pub(crate) fn all_values_generic<'v, R>(
    //     &self,
    //     expr_to_value: impl Fn(&ExprCompiled) -> Option<Value<'v>>,
    //     handler: impl FnOnce(&Arguments<'v, '_>) -> R,
    // ) -> Option<R>
    fun <R> allValuesGeneric(
        exprToValue: (ExprCompiled) -> Value?,
        handler: (Arguments) -> R,
    ): R? {
        val (pos, named) = splitPosNames()
        val posValues = pos.map { e -> exprToValue(e.node) ?: return null }
        val namedValues = named.map { e -> exprToValue(e.node) ?: return null }
        val argsValue = args?.let { exprToValue(it.node) ?: return null }
        val kwargsValue = kwargs?.let { exprToValue(it.node) ?: return null }
        return handler(
            Arguments(
                ArgumentsFull(
                    pos = posValues,
                    named = namedValues,
                    names = ArgNames.newUnique(names.map { (s, fsv) -> Pair(s, fsv.toStringValue()) }),
                    args = argsValue,
                    kwargs = kwargsValue,
                ),
            ),
        )
    }

    /**
     * Iterate over expressions of all arguments: positional, named, star-args, star-star-args.
     */
    // pub(crate) fn arg_exprs(&self) -> impl Iterator<Item = &IrSpanned<ExprCompiled>>
    fun argExprs(): Sequence<IrSpanned<ExprCompiled>> =
        sequence {
            yieldAll(posNamed)
            args?.let { yield(it) }
            kwargs?.let { yield(it) }
        }

    /**
     * Map a transformation function over all argument expressions.
     *
     * Applies [f] to each positional, named, star-args, and star-star-kwargs expression.
     * Names are cloned unchanged. Propagates exceptions thrown by [f].
     */
    // pub(crate) fn map_exprs<E>(
    //     &self,
    //     mut f: impl FnMut(&IrSpanned<ExprCompiled>) -> Result<IrSpanned<ExprCompiled>, E>,
    // ) -> Result<ArgsCompiledValue, E>
    fun <E : Exception> mapExprs(
        f: (IrSpanned<ExprCompiled>) -> IrSpanned<ExprCompiled>,
    ): ArgsCompiledValue =
        ArgsCompiledValue(
            posNamed = posNamed.map { f(it) }.toMutableList(),
            names = names.toMutableList(),
            args = args?.let { f(it) },
            kwargs = kwargs?.let { f(it) },
        )

    /**
     * Optimize all argument expressions using the given optimization context.
     */
    // pub(crate) fn optimize(&self, ctx: &mut OptCtx) -> ArgsCompiledValue
    fun optimize(ctx: OptCtx): ArgsCompiledValue = mapExprs<Nothing> { e -> e.optimize(ctx) }

    /**
     * Append a positional argument expression.
     */
    // pub(crate) fn push_pos(&mut self, expr: IrSpanned<ExprCompiled>)
    fun pushPos(expr: IrSpanned<ExprCompiled>) {
        posNamed.add(expr)
    }
}

/**
 * Compile call arguments from the AST [CallArgsP] into [ArgsCompiledValue].
 *
 * This is an extension on [Compiler] matching the Rust `impl Compiler` block.
 * Propagates compilation errors via [Result].
 */
// impl Compiler<'_, '_, '_, '_> {
//     pub(crate) fn args(
//         &mut self,
//         args: &CallArgsP<CstPayload>,
//     ) -> Result<ArgsCompiledValue, CompilerInternalError>
// }
internal fun Compiler.compileArgs(
    callArgs: CallArgsP<CstPayload>,
): Result<ArgsCompiledValue> {
    val res = ArgsCompiledValue()
    for (x in callArgs.args) {
        when (val node = x.node) {
            is ArgumentP.Positional -> {
                val compiled = this.expr(node.expr).getOrElse { return Result.failure(it) }
                res.posNamed.add(compiled)
            }
            is ArgumentP.Named -> {
                val fv = this.eval.frozenHeap().allocStrIntern(node.name.node)
                res.names.add(Pair(Symbol.new(node.name.node), fv))
                val compiled = this.expr(node.expr).getOrElse { return Result.failure(it) }
                res.posNamed.add(compiled)
            }
            is ArgumentP.Args -> {
                val compiled = this.expr(node.expr).getOrElse { return Result.failure(it) }
                res.args = compiled
            }
            is ArgumentP.KwArgs -> {
                val compiled = this.expr(node.expr).getOrElse { return Result.failure(it) }
                res.kwargs = compiled
            }
        }
    }
    return Result.success(res)
}
