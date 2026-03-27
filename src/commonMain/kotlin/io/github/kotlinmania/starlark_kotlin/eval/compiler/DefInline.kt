// port-lint: source src/eval/compiler/def_inline.rs
package io.github.kotlinmania.starlark_kotlin.eval.compiler.def_inline

import io.github.kotlinmania.starlark_kotlin.values.types.tuple.it
import io.github.kotlinmania.starlark_kotlin.analysis.node
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.FrozenStringValue


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

/// Inline functions.

// Sub-module: local_as_value

// Placeholder types referenced from other modules
// These will be replaced with real imports as the port progresses
// TODO: stub - FrozenValue needs real import
class FrozenValue {
    companion object {
        fun newNone(): FrozenValue = FrozenValue()
    }
}
// TODO: stub - FrozenValueTyped needs real import
class FrozenValueTyped<T>(val value: T) {
    companion object {
        fun <T : Any> new(value: FrozenValue): FrozenValueTyped<T>? = null
    }
}
// TODO: stub - IrSpanned needs real import
class IrSpanned<T>(
    val span: FrameSpan,
    val node: T,
) {
    fun clone(): IrSpanned<T> = IrSpanned(span, node)
}
class FrameSpan {
    companion object {
        fun default(): FrameSpan = FrameSpan()
    }
}
// TODO: stub - LocalSlotId needs real import
class LocalSlotId(val index: Int)

class LocalAsValue(val local: LocalSlotId)

sealed class ExprCompiled {
    // TODO: stub - Value needs real import
    data class Value(val value: FrozenValue) : ExprCompiled()
    data class Local(val slot: LocalSlotId) : ExprCompiled()
    data class LocalCaptured(val slot: LocalSlotId) : ExprCompiled()
    data class Module(val slot: Int) : ExprCompiled()
    // TODO: stub - Def needs real import
    class Def : ExprCompiled()
    data class Call(val call: IrSpanned<CallCompiled>) : ExprCompiled()
    class Compr : ExprCompiled()
    data class Slice(val args: SliceArgs) : ExprCompiled()
    // TODO: stub - Builtin2 needs real import
    data class Builtin2(val op: Builtin2Op, val args: Builtin2Args) : ExprCompiled()
    data class Index2(val args: Index2Args) : ExprCompiled()
    // TODO: stub - Builtin1 needs real import
    data class Builtin1(val op: Builtin1Op, val arg: IrSpanned<ExprCompiled>) : ExprCompiled()
    // TODO: stub - Tuple needs real import
    data class Tuple(val elems: List<IrSpanned<ExprCompiled>>) : ExprCompiled()
    // TODO: stub - List needs real import
    data class List(val elems: List<IrSpanned<ExprCompiled>>) : ExprCompiled()
    // TODO: stub - Dict needs real import
    data class Dict(val entries: List<Pair<IrSpanned<ExprCompiled>, IrSpanned<ExprCompiled>>>) : ExprCompiled()
    data class If(val args: IfArgs) : ExprCompiled()
    data class LogicalBinOp(val op: ExprLogicalBinOp, val args: LogicalBinOpArgs) : ExprCompiled()
    data class Seq(val args: SeqArgs) : ExprCompiled()

    companion object {
        fun ifExpr(
            c: IrSpanned<ExprCompiled>,
            t: IrSpanned<ExprCompiled>,
            f: IrSpanned<ExprCompiled>,
        ): IrSpanned<ExprCompiled> {
            return IrSpanned(c.span, If(IfArgs(c, t, f)))
        }

        fun logicalBinOp(
            op: ExprLogicalBinOp,
            l: IrSpanned<ExprCompiled>,
            r: IrSpanned<ExprCompiled>,
        ): IrSpanned<ExprCompiled> {
            return IrSpanned(l.span, LogicalBinOp(op, LogicalBinOpArgs(l, r)))
        }

        fun tuple(xs: kotlin.collections.List<IrSpanned<ExprCompiled>>, frozenHeap: Any?): ExprCompiled {
            return Tuple(xs)
        }

        fun binOp(
            op: Builtin2Op,
            l: IrSpanned<ExprCompiled>,
            r: IrSpanned<ExprCompiled>,
            ctx: OptCtx,
        ): ExprCompiled {
            return Builtin2(op, Builtin2Args(l, r))
        }

        fun unOp(
            span: FrameSpan,
            op: Builtin1Op,
            x: IrSpanned<ExprCompiled>,
            ctx: OptCtx,
        ): ExprCompiled {
            return Builtin1(op, x)
        }

        fun seq(
            a: IrSpanned<ExprCompiled>,
            b: IrSpanned<ExprCompiled>,
        ): IrSpanned<ExprCompiled> {
            return IrSpanned(a.span, Seq(SeqArgs(a, b)))
        }
    }
}

class SliceArgs(
    val obj: IrSpanned<ExprCompiled>,
    val start: IrSpanned<ExprCompiled>?,
    val stop: IrSpanned<ExprCompiled>?,
    val step: IrSpanned<ExprCompiled>?,
)

class Builtin2Op
class Builtin2Args(
    val left: IrSpanned<ExprCompiled>,
    val right: IrSpanned<ExprCompiled>,
)

class Index2Args(
    val obj: IrSpanned<ExprCompiled>,
    val index0: IrSpanned<ExprCompiled>,
    val index1: IrSpanned<ExprCompiled>,
)

class Builtin1Op

class IfArgs(
    val cond: IrSpanned<ExprCompiled>,
    val thenExpr: IrSpanned<ExprCompiled>,
    val elseExpr: IrSpanned<ExprCompiled>,
)

// TODO: stub - ExprLogicalBinOp needs real import
class ExprLogicalBinOp

class LogicalBinOpArgs(
    val left: IrSpanned<ExprCompiled>,
    val right: IrSpanned<ExprCompiled>,
)

class SeqArgs(
    val first: IrSpanned<ExprCompiled>,
    val second: IrSpanned<ExprCompiled>,
)

class CallCompiled(
    val funExpr: IrSpanned<ExprCompiled>,
    val args: ArgsCompiledValue,
) {
    companion object {
        fun call(
            span: FrameSpan,
            funExpr: IrSpanned<ExprCompiled>,
            args: ArgsCompiledValue,
            ctx: OptCtx,
        ): ExprCompiled {
            return ExprCompiled.Call(IrSpanned(span, CallCompiled(funExpr, args)))
        }
    }
}

class ArgsCompiledValue {
    fun argExprs(): List<IrSpanned<ExprCompiled>> = emptyList()

    fun mapExprs(
        transform: (IrSpanned<ExprCompiled>) -> IrSpanned<ExprCompiled>,
    ): ArgsCompiledValue {
        return ArgsCompiledValue()
    }
}

// TODO: stub - OptCtx needs real import
class OptCtx {
    fun frozenHeap(): Any? = null
}

// TODO: stub - StmtCompiled needs real import
class StmtCompiled {
    class Return(val expr: IrSpanned<ExprCompiled>)
}

class StmtsCompiled {
    fun first(): IrSpanned<StmtCompiled>? = null
}

class ParametersCompiled<T> {
    val params: List<ParamCompiled> = emptyList()
    fun hasArgsOrKwargs(): Boolean = false
    fun countParamVariables(): Int = 0
}

class ParamCompiled {
    fun acceptsPositional(): Boolean = false
}

/// Function body suitable for inlining.
sealed class InlineDefBody {
    /// Function body is `return type(x) == "y"`
    class ReturnTypeIs(val type: FrozenStringValue) : InlineDefBody()

    /// Any expression which can be safely inlined.
    class ReturnSafeToInlineExpr(val expr: IrSpanned<ExprCompiled>) : InlineDefBody()
}

/// If a statement is `return type(x) == "y"` where `x` is a first slot.
private fun isReturnTypeIs(stmt: StmtsCompiled): FrozenStringValue? {
    val first = stmt.first() ?: return null
    // first.asReturn()?.asTypeIs() would give (x, t)
    // Check if slot is LocalSlotId(0) — the first function parameter
    return null
}

private class IsSafeToInlineExpr(
    /// Function parameter count.
    private val paramCount: Int,
) {
    /// How many expressions we visited already.
    private var counter: Int = 0

    fun isSafeToInlineOptExpr(expr: IrSpanned<ExprCompiled>?): Boolean {
        return if (expr != null) {
            isSafeToInlineExpr(expr.node)
        } else {
            true
        }
    }

    /// Expression which has no access to locals or globals.
    fun isSafeToInlineExpr(expr: ExprCompiled): Boolean {
        // Do not inline too large functions.
        if (counter > 100) {
            return false
        }
        counter += 1
        return when (expr) {
            is ExprCompiled.Value -> true
            is ExprCompiled.LocalCaptured,
            is ExprCompiled.Module,
            is ExprCompiled.Def -> false
            is ExprCompiled.Local -> {
                // `l >= paramCount` should be unreachable, but it is safer this way.
                expr.slot.index < paramCount
            }
            is ExprCompiled.Call -> {
                isSafeToInlineExpr(expr.call.node.funExpr.node)
                    && expr.call.node.args.argExprs().all { isSafeToInlineExpr(it.node) }
            }
            is ExprCompiled.Compr -> {
                false
            }
            is ExprCompiled.Slice -> {
                isSafeToInlineExpr(expr.args.obj.node)
                    && isSafeToInlineOptExpr(expr.args.start)
                    && isSafeToInlineOptExpr(expr.args.stop)
                    && isSafeToInlineOptExpr(expr.args.step)
            }
            is ExprCompiled.Builtin2 -> {
                isSafeToInlineExpr(expr.args.left.node)
                    && isSafeToInlineExpr(expr.args.right.node)
            }
            is ExprCompiled.Index2 -> {
                isSafeToInlineExpr(expr.args.obj.node)
                    && isSafeToInlineExpr(expr.args.index0.node)
                    && isSafeToInlineExpr(expr.args.index1.node)
            }
            is ExprCompiled.Builtin1 -> {
                isSafeToInlineExpr(expr.arg.node)
            }
            is ExprCompiled.Tuple -> {
                expr.elems.all { isSafeToInlineExpr(it.node) }
            }
            is ExprCompiled.List -> {
                expr.elems.all { isSafeToInlineExpr(it.node) }
            }
            is ExprCompiled.Dict -> {
                expr.entries.all { (x, y) ->
                    isSafeToInlineExpr(x.node) && isSafeToInlineExpr(y.node)
                }
            }
            is ExprCompiled.If -> {
                isSafeToInlineExpr(expr.args.cond.node)
                    && isSafeToInlineExpr(expr.args.thenExpr.node)
                    && isSafeToInlineExpr(expr.args.elseExpr.node)
            }
            is ExprCompiled.LogicalBinOp -> {
                isSafeToInlineExpr(expr.args.left.node)
                    && isSafeToInlineExpr(expr.args.right.node)
            }
            is ExprCompiled.Seq -> {
                isSafeToInlineExpr(expr.args.first.node)
                    && isSafeToInlineExpr(expr.args.second.node)
            }
        }
    }
}

/// Function body is a `return` safe to inline expression (as defined above).
private fun isReturnSafeToInlineExpr(
    stmts: StmtsCompiled,
    paramCount: Int,
): IrSpanned<ExprCompiled>? {
    val first = stmts.first()
    if (first == null) {
        // Empty function is equivalent to `return None`.
        return IrSpanned(
            FrameSpan.default(),
            ExprCompiled.Value(FrozenValue.newNone()),
        )
    }
    // match &stmt.node { StmtCompiled::Return(expr) if safe => Some(expr.clone()) }
    return null
}

fun inlineDefBody(
    params: ParametersCompiled<IrSpanned<ExprCompiled>>,
    body: StmtsCompiled,
): InlineDefBody? {
    if (params.params.size == 1 && params.params[0].acceptsPositional()) {
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

class CannotInline : Exception()

/// Utility to inline function body at call site.
// TODO: stub - InlineDefCallSite needs real import
class InlineDefCallSite(
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
        return args.mapExprs { inline(it) }
    }

    fun inlineCall(
        call: IrSpanned<CallCompiled>,
    ): IrSpanned<ExprCompiled> {
        val span = call.span
        val funExpr = inline(call.node.funExpr)
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
            is ExprCompiled.Value -> IrSpanned(span, node)
            is ExprCompiled.Local -> {
                val value = slots[node.slot.index]
                val localAsValue = FrozenValueTyped.new<LocalAsValue>(value)
                val inlinedExpr = if (localAsValue != null) {
                    ExprCompiled.Local(localAsValue.value.local)
                } else {
                    ExprCompiled.Value(value)
                }
                IrSpanned(span, inlinedExpr)
            }
            is ExprCompiled.If -> {
                val c = inline(node.args.cond)
                val t = inline(node.args.thenExpr)
                val f = inline(node.args.elseExpr)
                ExprCompiled.ifExpr(c, t, f)
            }
            is ExprCompiled.LogicalBinOp -> {
                val l = inline(node.args.left)
                val r = inline(node.args.right)
                ExprCompiled.logicalBinOp(node.op, l, r)
            }
            is ExprCompiled.List -> {
                val xs = node.elems.map { inline(it) }
                IrSpanned(span, ExprCompiled.List(xs))
            }
            is ExprCompiled.Tuple -> {
                val xs = node.elems.map { inline(it) }
                IrSpanned(span, ExprCompiled.tuple(xs, ctx.frozenHeap()))
            }
            is ExprCompiled.Dict -> {
                val xs = node.entries.map { (x, y) -> Pair(inline(x), inline(y)) }
                IrSpanned(span, ExprCompiled.Dict(xs))
            }
            is ExprCompiled.Builtin2 -> {
                val l = inline(node.args.left)
                val r = inline(node.args.right)
                IrSpanned(span, ExprCompiled.binOp(node.op, l, r, ctx))
            }
            is ExprCompiled.Index2 -> {
                val a = inline(node.args.obj)
                val i0 = inline(node.args.index0)
                val i1 = inline(node.args.index1)
                IrSpanned(span, ExprCompiled.Index2(Index2Args(a, i0, i1)))
            }
            is ExprCompiled.Builtin1 -> {
                val x = inline(node.arg)
                IrSpanned(span, ExprCompiled.unOp(span, node.op, x, ctx))
            }
            is ExprCompiled.Slice -> {
                val l = inline(node.args.obj)
                val a = inlineOpt(node.args.start)
                val b = inlineOpt(node.args.stop)
                val c = inlineOpt(node.args.step)
                IrSpanned(span, ExprCompiled.Slice(SliceArgs(l, a, b, c)))
            }
            is ExprCompiled.Seq -> {
                val a = inline(node.args.first)
                val b = inline(node.args.second)
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
