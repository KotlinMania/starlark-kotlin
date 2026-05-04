// port-lint: source eval/compiler/defInline.rs
package io.github.kotlinmania.starlark.eval.compiler

import io.github.kotlinmania.starlark.eval.compiler.args.ArgsCompiledValue
import io.github.kotlinmania.starlark.eval.runtime.FrameSpan
import io.github.kotlinmania.starlark.eval.runtime.LocalSlotId
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark.values.layout.FrozenValueTyped
import io.github.kotlinmania.starlark.eval.compiler.optctx.OptCtx
import io.github.kotlinmania.starlark.eval.compiler.definline.localasvalue.LocalAsValue

/*
 * Copyright 2019 The Starlark in Rust Authors.
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

/** Inline functions. */

/** Function body suitable for inlining. */
internal sealed class InlineDefBody {
    /** Function body is `return type(x) == "y"`. */
    class ReturnTypeIs(val type: FrozenStringValue) : InlineDefBody()

    /** Any expression which can be safely inlined. */
    class ReturnSafeToInlineExpr(val expr: IrSpanned<ExprCompiled>) : InlineDefBody()
}

/** If a statement is `return type(x) == "y"` where `x` is a first slot. */
private fun isReturnTypeIs(stmt: StmtsCompiled): FrozenStringValue? {
    val first = stmt.first() ?: return null
    val ret = first.node as? StmtCompiled.Return ?: return null
    val typeIs = ret.expr.node.asTypeIs() ?: return null
    val (x, t) = typeIs
    val local = x.node.asLocalNonCaptured() ?: return null
    if (local.index != 0u) return null
    return t
}

private class IsSafeToInlineExpr(
    /** Function parameter count. */
    private val paramCount: Int,
) {
    /** How many expressions we visited already. */
    private var counter: Int = 0

    fun isSafeToInlineOptExpr(expr: IrSpanned<ExprCompiled>?): Boolean {
        return if (expr != null) {
            isSafeToInlineExpr(expr.node)
        } else {
            true
        }
    }

    /** Expression which has no access to locals or globals. */
    fun isSafeToInlineExpr(expr: ExprCompiled): Boolean {
        // Do not inline too large functions.
        if (counter > 100) {
            return false
        }
        counter += 1
        return when (expr) {
            is ExprCompiled.ValueExpr -> true
            is ExprCompiled.LocalCaptured,
            is ExprCompiled.Module,
            is ExprCompiled.Def -> false
            is ExprCompiled.Local -> {
                // `l >= paramCount` should be unreachable, but it is safer this way.
                expr.slot.index < paramCount.toUInt()
            }
            is ExprCompiled.Call -> {
                isSafeToInlineExpr(expr.call.node.fun_.node)
                    && expr.call.node.args.argExprs().all { isSafeToInlineExpr(it.node) }
            }
            is ExprCompiled.Compr -> {
                false
            }
            is ExprCompiled.Slice -> {
                isSafeToInlineExpr(expr.obj.node)
                    && isSafeToInlineOptExpr(expr.start)
                    && isSafeToInlineOptExpr(expr.stop)
                    && isSafeToInlineOptExpr(expr.step)
            }
            is ExprCompiled.Builtin2Expr -> {
                isSafeToInlineExpr(expr.lhs.node)
                    && isSafeToInlineExpr(expr.rhs.node)
            }
            is ExprCompiled.Index2 -> {
                isSafeToInlineExpr(expr.obj.node)
                    && isSafeToInlineExpr(expr.index0.node)
                    && isSafeToInlineExpr(expr.index1.node)
            }
            is ExprCompiled.Builtin1Expr -> {
                isSafeToInlineExpr(expr.expr.node)
            }
            is ExprCompiled.TupleExpr -> {
                expr.elements.all { isSafeToInlineExpr(it.node) }
            }
            is ExprCompiled.ListExpr -> {
                expr.elements.all { isSafeToInlineExpr(it.node) }
            }
            is ExprCompiled.DictExpr -> {
                expr.entries.all { (x, y) ->
                    isSafeToInlineExpr(x.node) && isSafeToInlineExpr(y.node)
                }
            }
            is ExprCompiled.If -> {
                isSafeToInlineExpr(expr.cond.node)
                    && isSafeToInlineExpr(expr.thenBranch.node)
                    && isSafeToInlineExpr(expr.elseBranch.node)
            }
            is ExprCompiled.LogicalBinOp -> {
                isSafeToInlineExpr(expr.lhs.node)
                    && isSafeToInlineExpr(expr.rhs.node)
            }
            is ExprCompiled.Seq -> {
                isSafeToInlineExpr(expr.first.node)
                    && isSafeToInlineExpr(expr.second.node)
            }
        }
    }
}

/** Function body is a `return` safe to inline expression (as defined above). */
private fun isReturnSafeToInlineExpr(
    stmts: StmtsCompiled,
    paramCount: Int,
): IrSpanned<ExprCompiled>? {
    val first = stmts.first()
    if (first == null) {
        // Empty function is equivalent to `return None`.
        return IrSpanned(
            FrameSpan.DEFAULT,
            ExprCompiled.ValueExpr(FrozenValue.newNone()),
        )
    }
    val ret = first.node as? StmtCompiled.Return ?: return null
    val checker = IsSafeToInlineExpr(paramCount)
    return if (checker.isSafeToInlineExpr(ret.expr.node)) {
        ret.expr
    } else {
        null
    }
}

internal fun inlineDefBody(
    params: ParametersCompiled<IrSpanned<ExprCompiled>>,
    body: StmtsCompiled,
): InlineDefBody? {
    if (params.params.size == 1 && params.params[0].node.acceptsPositional()) {
        val t = isReturnTypeIs(body)
        if (t != null) {
            return InlineDefBody.ReturnTypeIs(t)
        }
    }
    if (!params.hasArgsOrKwargs()) {
        // It is possible to sometimes inline functions with *args or **kwargs,
        // but let's postpone that for now.
        val paramCount = params.countParamVariables()
        val expr = isReturnSafeToInlineExpr(body, paramCount)
        if (expr != null) {
            return InlineDefBody.ReturnSafeToInlineExpr(expr)
        }
    }
    return null
}

internal class CannotInline : Exception()

/** Utility to inline function body at call site. */
internal class InlineDefCallSite(
    val ctx: OptCtx,
    // Values in the slots are either real frozen values
    // or `LocalAsValue` which are the parameters to be substituted with caller locals.
    val slots: List<FrozenValue>,
) {
    fun inlineOpt(
        expr: IrSpanned<ExprCompiled>?,
    ): IrSpanned<ExprCompiled>? {
        return when (expr) {
            null -> null
            else -> inline(expr)
        }
    }

    fun inlineArgs(args: ArgsCompiledValue): ArgsCompiledValue {
        return args.mapExprs<CannotInline> { inline(it) }
    }

    fun inlineCall(
        call: IrSpanned<CallCompiled>,
    ): IrSpanned<ExprCompiled> {
        val span = call.span
        val funExpr = inline(call.node.fun_)
        val args = inlineArgs(call.node.args)
        return IrSpanned(
            span,
            CallCompiled.call(span, funExpr, args, ctx),
        )
    }

    fun inline(
        expr: IrSpanned<ExprCompiled>,
    ): IrSpanned<ExprCompiled> {
        val span = expr.span
        return when (val node = expr.node) {
            is ExprCompiled.ValueExpr -> IrSpanned(span, node)
            is ExprCompiled.Local -> {
                val value = slots[node.slot.index.toInt()]
                val localAsValue = FrozenValueTyped.new<LocalAsValue>(value)
                val inlinedExpr = if (localAsValue != null) {
                    ExprCompiled.Local(localAsValue.asRef().local)
                } else {
                    ExprCompiled.ValueExpr(value)
                }
                IrSpanned(span, inlinedExpr)
            }
            is ExprCompiled.If -> {
                val c = inline(node.cond)
                val t = inline(node.thenBranch)
                val f = inline(node.elseBranch)
                ExprCompiled.ifExpr(c, t, f)
            }
            is ExprCompiled.LogicalBinOp -> {
                val l = inline(node.lhs)
                val r = inline(node.rhs)
                ExprCompiled.logicalBinOp(node.op, l, r)
            }
            is ExprCompiled.ListExpr -> {
                val xs = node.elements.map { inline(it) }
                IrSpanned(span, ExprCompiled.ListExpr(xs))
            }
            is ExprCompiled.TupleExpr -> {
                val xs = node.elements.map { inline(it) }
                IrSpanned(span, ExprCompiled.tuple(xs, ctx.frozenHeap()))
            }
            is ExprCompiled.DictExpr -> {
                val xs = node.entries.map { (x, y) -> Pair(inline(x), inline(y)) }
                IrSpanned(span, ExprCompiled.DictExpr(xs))
            }
            is ExprCompiled.Builtin2Expr -> {
                val l = inline(node.lhs)
                val r = inline(node.rhs)
                IrSpanned(span, ExprCompiled.binOp(node.op, l, r, ctx))
            }
            is ExprCompiled.Index2 -> {
                val a = inline(node.obj)
                val i0 = inline(node.index0)
                val i1 = inline(node.index1)
                IrSpanned(span, ExprCompiled.index2(a, i0, i1))
            }
            is ExprCompiled.Builtin1Expr -> {
                val x = inline(node.expr)
                IrSpanned(span, ExprCompiled.unOp(span, node.op, x, ctx))
            }
            is ExprCompiled.Slice -> {
                val l = inline(node.obj)
                val a = inlineOpt(node.start)
                val b = inlineOpt(node.stop)
                val c = inlineOpt(node.step)
                IrSpanned(span, ExprCompiled.Slice(l, a, b, c))
            }
            is ExprCompiled.Seq -> {
                val a = inline(node.first)
                val b = inline(node.second)
                ExprCompiled.seq(a, b)
            }
            is ExprCompiled.Call -> return inlineCall(node.call)
            // These should be unreachable, but it is safer
            // to do unnecessary work in compiler than crash.
            is ExprCompiled.LocalCaptured,
            is ExprCompiled.Module,
            is ExprCompiled.Compr,
            is ExprCompiled.Def -> throw CannotInline()
        }
    }
}
