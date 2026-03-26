// port-lint: source src/eval/compiler/def_inline.rs
package io.github.kotlinmania.starlark_kotlin.eval.compiler.def_inline

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

/**
 * Inline functions.
 */

import io.github.kotlinmania.starlark_kotlin.eval.compiler.IrSpanned
import io.github.kotlinmania.starlark_kotlin.eval.compiler.ParametersCompiled
import io.github.kotlinmania.starlark_kotlin.eval.compiler.StmtCompiled
import io.github.kotlinmania.starlark_kotlin.eval.compiler.StmtsCompiled
import io.github.kotlinmania.starlark_kotlin.eval.compiler.acceptsPositional
import io.github.kotlinmania.starlark_kotlin.eval.compiler.args.ArgsCompiledValue
import io.github.kotlinmania.starlark_kotlin.eval.compiler.call.CallCompiled
import io.github.kotlinmania.starlark_kotlin.eval.compiler.def_inline.local_as_value.LocalAsValue
import io.github.kotlinmania.starlark_kotlin.eval.compiler.expr.Builtin1
import io.github.kotlinmania.starlark_kotlin.eval.compiler.expr.Builtin2
import io.github.kotlinmania.starlark_kotlin.eval.compiler.expr.ExprCompiled
import io.github.kotlinmania.starlark_kotlin.eval.compiler.expr.ExprLogicalBinOp
import io.github.kotlinmania.starlark_kotlin.eval.compiler.opt_ctx.OptCtx
import io.github.kotlinmania.starlark_kotlin.eval.runtime.FrameSpan
import io.github.kotlinmania.starlark_kotlin.eval.runtime.LocalSlotId
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.FrozenValueTyped
import io.github.kotlinmania.starlark_kotlin.values.types.string.intern.FrozenStringValue

/**
 * Function body suitable for inlining.
 */
internal sealed class InlineDefBody {
    /**
     * Function body is `return type(x) == "y"`.
     */
    class ReturnTypeIs(val type: FrozenStringValue) : InlineDefBody()

    /**
     * Any expression which can be safely inlined.
     *
     * See the function where this enum variant is computed for the definition
     * of safe to inline expression.
     */
    class ReturnSafeToInlineExpr(val expr: IrSpanned<ExprCompiled>) : InlineDefBody()
}

/**
 * If a statement is `return type(x) == "y"` where `x` is a first slot.
 */
private fun isReturnTypeIs(stmt: StmtsCompiled): FrozenStringValue? {
    val first = stmt.first() ?: return null
    val returnExpr = (first.node as? StmtCompiled.Return)?.expr
        ?: return null
    val (x, t) = returnExpr.node.asTypeIs() ?: return null
    return when (x.node) {
        // Slot 0 is a slot for the first function parameter.
        is ExprCompiled.Local -> if ((x.node as ExprCompiled.Local).slot.value == 0u) t else null
        else -> null
    }
}

/**
 * Helper to check whether an expression is safe to inline.
 */
private class IsSafeToInlineExpr(
    /** Function parameter count. */
    private val paramCount: UInt,
) {
    /** How many expressions we visited already. */
    private var counter: UInt = 0u

    fun isSafeToInlineOptExpr(expr: IrSpanned<ExprCompiled>?): Boolean {
        return if (expr != null) {
            isSafeToInlineExpr(expr.node)
        } else {
            true
        }
    }

    /**
     * Expression which has no access to locals or globals.
     */
    fun isSafeToInlineExpr(expr: ExprCompiled): Boolean {
        // Do not inline too large functions.
        if (counter > 100u) {
            return false
        }
        counter += 1u
        return when (expr) {
            is ExprCompiled.ValueExpr -> true
            is ExprCompiled.LocalCaptured,
            is ExprCompiled.Module,
            is ExprCompiled.Def -> false
            is ExprCompiled.Local -> {
                // `l >= paramCount` should be unreachable, but it is safer this way.
                expr.slot.value < paramCount
            }
            is ExprCompiled.Call -> {
                isSafeToInlineExpr(expr.call.node.fun_.node)
                    && expr.call.node.args.argExprs().all { isSafeToInlineExpr(it.node) }
            }
            is ExprCompiled.Compr -> {
                // Some comprehensions are safe to inline, but not handled yet.
                false
            }
            is ExprCompiled.Slice -> {
                isSafeToInlineExpr(expr.obj.node)
                    && isSafeToInlineOptExpr(expr.start)
                    && isSafeToInlineOptExpr(expr.stop)
                    && isSafeToInlineOptExpr(expr.step)
            }
            is ExprCompiled.Builtin2Expr -> {
                @Suppress("UNUSED_VARIABLE")
                val unused: Builtin2 = expr.op
                isSafeToInlineExpr(expr.lhs.node)
                    && isSafeToInlineExpr(expr.rhs.node)
            }
            is ExprCompiled.Index2 -> {
                isSafeToInlineExpr(expr.obj.node)
                    && isSafeToInlineExpr(expr.a.node)
                    && isSafeToInlineExpr(expr.b.node)
            }
            is ExprCompiled.Builtin1Expr -> {
                @Suppress("UNUSED_VARIABLE")
                val unused: Builtin1 = expr.op
                isSafeToInlineExpr(expr.expr.node)
            }
            is ExprCompiled.TupleExpr, is ExprCompiled.ListExpr -> {
                val xs = when (expr) {
                    is ExprCompiled.TupleExpr -> expr.elements
                    is ExprCompiled.ListExpr -> expr.elements
                    else -> error("unreachable")
                }
                xs.all { isSafeToInlineExpr(it.node) }
            }
            is ExprCompiled.DictExpr -> {
                expr.entries.all { (x, y) ->
                    isSafeToInlineExpr(x.node) && isSafeToInlineExpr(y.node)
                }
            }
            is ExprCompiled.If -> {
                isSafeToInlineExpr(expr.cond.node)
                    && isSafeToInlineExpr(expr.then.node)
                    && isSafeToInlineExpr(expr.elseExpr.node)
            }
            is ExprCompiled.LogicalBinOp -> {
                @Suppress("UNUSED_VARIABLE")
                val unused: ExprLogicalBinOp = expr.op
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

/**
 * Function body is a `return` safe to inline expression (as defined above).
 */
private fun isReturnSafeToInlineExpr(
    stmts: StmtsCompiled,
    paramCount: UInt,
): IrSpanned<ExprCompiled>? {
    val first = stmts.first()
    if (first == null) {
        // Empty function is equivalent to `return None`.
        return IrSpanned(
            span = FrameSpan.default(),
            node = ExprCompiled.ValueExpr(FrozenValue.newNone()),
        )
    }
    val returnNode = first.node as? StmtCompiled.Return
        ?: return null
    val expr = returnNode.expr
    return if (IsSafeToInlineExpr(paramCount).isSafeToInlineExpr(expr.node)) {
        expr
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
        // It is possible to sometimes inline functions with `*args` or `**kwargs`,
        // but let's postpone that for now.
        val paramCount = params.countParamVariables().toUInt()
        val expr = isReturnSafeToInlineExpr(body, paramCount)
        if (expr != null) {
            return InlineDefBody.ReturnSafeToInlineExpr(expr)
        }
    }
    return null
}

internal class CannotInline : Exception()

/**
 * Construct a logical binary operation expression, with constant folding.
 */
private fun logicalBinOp(
    op: ExprLogicalBinOp,
    l: IrSpanned<ExprCompiled>,
    r: IrSpanned<ExprCompiled>,
): IrSpanned<ExprCompiled> {
    val lv = l.node.isPureInfallibleToBool()
    if (lv != null) {
        return if (lv == (op == ExprLogicalBinOp.Or)) l else r
    }
    val span = l.span.merge(r.span)
    return IrSpanned(
        span = span,
        node = ExprCompiled.LogicalBinOp(op, l, r),
    )
}

/**
 * Construct tuple expression from elements, optimizing to frozen tuple value when possible.
 */
private fun tupleExpr(
    elems: List<IrSpanned<ExprCompiled>>,
    frozenHeap: FrozenHeap,
): ExprCompiled {
    val frozenElems = elems.map { it.node.asValue() ?: return ExprCompiled.TupleExpr(elems) }
    return ExprCompiled.ValueExpr(frozenHeap.allocTuple(frozenElems))
}

/**
 * Utility to inline function body at call site.
 */
internal class InlineDefCallSite(
    val ctx: OptCtx,
    /**
     * Values in the slots are either real frozen values
     * or [LocalAsValue] which are the parameters to be substituted with caller locals.
     */
    val slots: List<FrozenValue>,
) {
    private fun inlineOpt(
        expr: IrSpanned<ExprCompiled>?,
    ): IrSpanned<ExprCompiled>? {
        return when (expr) {
            null -> null
            else -> inline(expr)
        }
    }

    private fun inlineArgs(args: ArgsCompiledValue): ArgsCompiledValue {
        return args.mapExprs<CannotInline> { inline(it) }
    }

    private fun inlineCall(
        call: IrSpanned<CallCompiled>,
    ): IrSpanned<ExprCompiled> {
        val span = call.span
        val compiled = call.node
        val funExpr = inline(compiled.fun_)
        val args = inlineArgs(compiled.args)
        return IrSpanned(
            span = span,
            node = CallCompiled.call(span, funExpr, args, ctx),
        )
    }

    fun inline(
        expr: IrSpanned<ExprCompiled>,
    ): IrSpanned<ExprCompiled> {
        val span = expr.span
        return when (val node = expr.node) {
            is ExprCompiled.ValueExpr -> IrSpanned(
                span = span,
                node = node,
            )
            is ExprCompiled.Local -> {
                val value = slots[node.slot.value.toInt()]
                val local = FrozenValueTyped.new<LocalAsValue>(value)
                val inlinedExpr = if (local != null) {
                    ExprCompiled.Local(local.asRef().local)
                } else {
                    ExprCompiled.ValueExpr(value)
                }
                IrSpanned(span = span, node = inlinedExpr)
            }
            is ExprCompiled.If -> {
                val c = inline(node.cond)
                val t = inline(node.then)
                val f = inline(node.elseExpr)
                ExprCompiled.ifExpr(c, t, f)
            }
            is ExprCompiled.LogicalBinOp -> {
                val l = inline(node.lhs)
                val r = inline(node.rhs)
                logicalBinOp(node.op, l, r)
            }
            is ExprCompiled.ListExpr -> {
                val xs = node.elements.map { x -> inline(x) }
                IrSpanned(
                    span = span,
                    node = ExprCompiled.ListExpr(xs),
                )
            }
            is ExprCompiled.TupleExpr -> {
                val xs = node.elements.map { x -> inline(x) }
                IrSpanned(
                    span = span,
                    node = tupleExpr(xs, ctx.frozenHeap()),
                )
            }
            is ExprCompiled.DictExpr -> {
                val xs = node.entries.map { (x, y) -> Pair(inline(x), inline(y)) }
                IrSpanned(
                    span = span,
                    node = ExprCompiled.DictExpr(xs),
                )
            }
            is ExprCompiled.Builtin2Expr -> {
                val l = inline(node.lhs)
                val r = inline(node.rhs)
                IrSpanned(
                    span = span,
                    node = ExprCompiled.binOp(node.op, l, r, ctx),
                )
            }
            is ExprCompiled.Index2 -> {
                val a = inline(node.obj)
                val i0 = inline(node.a)
                val i1 = inline(node.b)
                IrSpanned(
                    span = span,
                    node = ExprCompiled.Index2(a, i0, i1),
                )
            }
            is ExprCompiled.Builtin1Expr -> {
                val x = inline(node.expr)
                IrSpanned(
                    span = span,
                    node = ExprCompiled.unOp(span, node.op, x, ctx),
                )
            }
            is ExprCompiled.Slice -> {
                val l = inline(node.obj)
                val a = inlineOpt(node.start)
                val b = inlineOpt(node.stop)
                val c = inlineOpt(node.step)
                IrSpanned(
                    span = span,
                    node = ExprCompiled.Slice(l, a, b, c),
                )
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
