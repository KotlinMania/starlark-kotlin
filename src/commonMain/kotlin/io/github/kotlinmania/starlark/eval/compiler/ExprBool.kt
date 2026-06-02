// port-lint: source src/eval/compiler/expr_bool.rs
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

/** Boolean expression. */

import io.github.kotlinmania.starlark.eval.runtime.FrameSpan
import io.github.kotlinmania.starlark.values.layout.FrozenValue

/** Boolean expression. */
internal sealed class ExprCompiledBool {
    // Const(bool)
    data class Const(
        val value: Boolean,
    ) : ExprCompiledBool()

    /** Non-const expression. */
    // Expr(ExprCompiled)
    data class Expr(
        val expr: ExprCompiled,
    ) : ExprCompiledBool()

    fun intoExpr(): ExprCompiled =
        when (this) {
            is Const -> ExprCompiled.ValueExpr(FrozenValue.newBool(value))
            is Expr -> expr
        }

    fun constValue(): Boolean? =
        when (this) {
            is Const -> value
            is Expr -> null
        }

    companion object {
        /** `bool(x)` and do trivial optimizations. */
        fun new(expr: IrSpanned<ExprCompiled>): IrSpanned<ExprCompiledBool> {
            fun newBool(span: FrameSpan, b: Boolean): IrSpanned<ExprCompiledBool> =
                IrSpanned(node = Const(b), span = span)

            val span = expr.span

            val pureResult = expr.isPureInfallibleToBool()
            if (pureResult != null) {
                return newBool(span, pureResult)
            }

            return when (val node = expr.node) {
                is ExprCompiled.Builtin1Expr -> {
                    if (node.op == Builtin1.Not) {
                        val x = new(node.expr)
                        val xConst = x.node.constValue()
                        if (xConst != null) {
                            newBool(span, !xConst)
                        } else {
                            IrSpanned(
                                node =
                                    Expr(
                                        ExprCompiled.Builtin1Expr(
                                            Builtin1.Not,
                                            x.intoExpr(),
                                        ),
                                    ),
                                span = span,
                            )
                        }
                    } else {
                        IrSpanned(node = Expr(node), span = span)
                    }
                }
                is ExprCompiled.LogicalBinOp -> {
                    val op = node.op
                    val x = new(node.lhs)
                    val y = new(node.rhs)
                    val xConst = x.node.constValue()
                    val yConst = y.node.constValue()
                    when {
                        op == ExprLogicalBinOp.And && xConst == false -> newBool(span, false)
                        op == ExprLogicalBinOp.Or && xConst == true -> newBool(span, true)
                        op == ExprLogicalBinOp.And && xConst == true -> y
                        op == ExprLogicalBinOp.Or && xConst == false -> y
                        op == ExprLogicalBinOp.And && xConst == null && yConst == true -> x
                        op == ExprLogicalBinOp.Or && xConst == null && yConst == false -> x
                        op == ExprLogicalBinOp.And && xConst == null && yConst == false -> {
                            // The expression evaluates to false,
                            // but we need to preserve LHS for the effect.
                            IrSpanned(
                                span = span,
                                node =
                                    Expr(
                                        ExprCompiled
                                            .seq(
                                                x.intoExpr(),
                                                newBool(y.span, false).intoExpr(),
                                            ).node,
                                    ),
                            )
                        }
                        op == ExprLogicalBinOp.Or && xConst == null && yConst == true -> {
                            // The expression evaluates to true,
                            // but we need to preserve LHS for the effect.
                            IrSpanned(
                                span = span,
                                node =
                                    Expr(
                                        ExprCompiled
                                            .seq(
                                                x.intoExpr(),
                                                newBool(y.span, true).intoExpr(),
                                            ).node,
                                    ),
                            )
                        }
                        else ->
                            IrSpanned(
                                node =
                                    Expr(
                                        ExprCompiled.LogicalBinOp(
                                            op,
                                            x.intoExpr(),
                                            y.intoExpr(),
                                        ),
                                    ),
                                span = span,
                            )
                    }
                }
                else -> IrSpanned(node = Expr(node), span = span)
            }
        }
    }
}

/** Extension to convert IrSpanned<ExprCompiledBool> to IrSpanned<ExprCompiled>. */
internal fun IrSpanned<ExprCompiledBool>.intoExpr(): IrSpanned<ExprCompiled> =
    IrSpanned(span = span, node = node.intoExpr())
