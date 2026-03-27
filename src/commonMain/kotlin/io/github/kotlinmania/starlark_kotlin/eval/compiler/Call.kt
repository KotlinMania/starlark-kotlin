// port-lint: source src/eval/compiler/call.rs
package io.github.kotlinmania.starlark_kotlin.eval.compiler

import io.github.kotlinmania.starlark_kotlin.util.arc_or_static.clone
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

/// Compile function calls.

// Placeholder types referenced from other modules
// These will be replaced with real imports as the port progresses
class Symbol(val name: String)
class FrozenValue {
    fun toValue(): Value = Value()
    fun speculativeExecSafe(): Boolean = false
    fun downcastFrozenRef(): FrozenEnumType? = null
}


class Value {
    fun invoke(args: Any, eval: Any): kotlin.Result<Value> = kotlin.Result.success(Value())
    fun unpackFrozen(): FrozenValue? = null
}

class FrameSpan {
    val inlinedFrames: InlinedFrames = InlinedFrames()
}

class InlinedFrames {
    fun inlineInto(span: FrameSpan, frozenValue: FrozenValue, alloc: InlinedFrameAlloc) {}
}

class InlinedFrameAlloc {
    companion object {
        fun new(frozenHeap: Any): InlinedFrameAlloc = InlinedFrameAlloc()
    }
}

class IrSpanned<T>(val span: FrameSpan, val node: T) {
    fun asValue(): FrozenValue? = null

    fun isFnLen(): Boolean = false
    fun isFnType(): Boolean = false
    fun isFnIsinstance(): Boolean = false
    fun isPureInfallible(): Boolean = false

    fun optimize(ctx: OptCtx): IrSpanned<ExprCompiled> = IrSpanned(span, node as ExprCompiled)

    fun clone(): IrSpanned<T> = IrSpanned(span, node)

    fun visitSpans(visitor: (FrameSpan) -> Unit) {}
}

class ArgsCompiledValue {
    fun onePos(): IrSpanned<ExprCompiled>? = null
    fun twoPos(): Pair<IrSpanned<ExprCompiled>, IrSpanned<ExprCompiled>>? = null
    fun optimize(ctx: OptCtx): ArgsCompiledValue = ArgsCompiledValue()

    fun <R> allValues(handler: (Arguments) -> R?): R? = null
    fun <R> allValuesGeneric(
        exprToValue: (ExprCompiled) -> Value?,
        handler: (Arguments) -> R?,
    ): R? = null
}

class Arguments {
    fun frozenToV(): Any = this
}

class OptCtx {
    val paramCount: Int = 0
    fun heap(): Any = Unit
    fun frozenHeap(): Any = Unit
    fun eval(): Any? = null
}

class FrozenBoundMethod(val this_: FrozenValue, val method: MethodInfo)
class MethodInfo(val name: String)

class FrozenEnumType {
    val value: EnumTypeValue = EnumTypeValue()
}

class EnumTypeValue {
    fun construct(value: Value): kotlin.Result<FrozenValue> = kotlin.Result.success(FrozenValue())
}

sealed class InlineDefBody {
    class ReturnTypeIs(val type: FrozenStringValue) : InlineDefBody()
    class ReturnSafeToInlineExpr(val expr: IrSpanned<ExprCompiled>) : InlineDefBody()
}

class DefInfo(val inlineDefBody: InlineDefBody?)

class FrozenDef {
    val defInfo: DefInfo = DefInfo(null)
    val parameters: ParametersSpec = ParametersSpec()
    fun toFrozenValue(): FrozenValue = FrozenValue()
}

class ParametersSpec {
    fun hasArgsOrKwargs(): Boolean = false
    fun len(): Int = 0
    fun collect(args: Any, slots: Array<Value?>, heap: Any): kotlin.Result<Unit> = kotlin.Result.success(Unit)
}

class InlineDefCallSite(val ctx: OptCtx, val slots: List<FrozenValue>) {
    fun inline(expr: IrSpanned<ExprCompiled>): kotlin.Result<IrSpanned<ExprCompiled>> =
        kotlin.Result.success(expr)
}

sealed class Builtin1 {
    class Dot(val name: Symbol) : Builtin1()
    class Other : Builtin1()
}

sealed class ExprCompiled {
    class CallExpr(val call: IrSpanned<CallCompiled>) : ExprCompiled()
    class ValueExpr(val value: FrozenValue) : ExprCompiled()
    class Builtin1Expr(val builtin: Builtin1, val expr: IrSpanned<ExprCompiled>) : ExprCompiled()
    class Local(val index: Int) : ExprCompiled()
    class Other : ExprCompiled()

    fun asFrozenDef(): FrozenDef? = null
    fun asValue(): FrozenValue? = null
    fun asFrozenBoundMethod(): FrozenBoundMethod? = null

    companion object {
        fun compileTimeGetattr(value: FrozenValue, field: Symbol, ctx: OptCtx): FrozenValue? = null
        fun dot(this_: IrSpanned<ExprCompiled>, field: Symbol, ctx: OptCtx): ExprCompiled = Other()
        fun typeIs(expr: IrSpanned<ExprCompiled>, type: FrozenStringValue): ExprCompiled = Other()
        fun len(span: FrameSpan, arg: IrSpanned<ExprCompiled>): ExprCompiled = Other()
        fun typ(span: FrameSpan, arg: IrSpanned<ExprCompiled>): ExprCompiled = Other()
        fun formatOne(
            before: FrozenStringValue,
            arg: IrSpanned<ExprCompiled>,
            after: FrozenStringValue,
            ctx: OptCtx,
        ): ExprCompiled = Other()

        fun tryValue(span: FrameSpan, value: Value, frozenHeap: Any): ExprCompiled? = null
    }
}

fun parseFormatOne(format: FrozenStringValue): Pair<String, String>? = null

fun localAsValue(local: ExprCompiled.Local): FrozenValue? = null

// --- CallCompiled ---

internal class CallCompiled(
    val fun_: IrSpanned<ExprCompiled>,
    val args: ArgsCompiledValue,
) {
    companion object {
        fun newMethod(
            span: FrameSpan,
            this_: IrSpanned<ExprCompiled>,
            field: Symbol,
            getattrSpan: FrameSpan,
            args: ArgsCompiledValue,
            ctx: OptCtx,
        ): ExprCompiled {
            val thisValue = this_.asValue()
            if (thisValue != null) {
                val v = ExprCompiled.compileTimeGetattr(thisValue, field, ctx)
                if (v != null) {
                    val vExpr = ExprCompiled.ValueExpr(v)
                    val vSpanned = IrSpanned(getattrSpan, vExpr as ExprCompiled)
                    return call(span, vSpanned, args, ctx)
                }
            }

            return ExprCompiled.CallExpr(IrSpanned(
                span,
                CallCompiled(
                    fun_ = IrSpanned(
                        getattrSpan,
                        ExprCompiled.dot(this_, field, ctx),
                    ),
                    args = args,
                )
            ))
        }

        /// If this call expression is `len(x)`, return `x`.
        fun asLen(call: CallCompiled): IrSpanned<ExprCompiled>? {
            if (!call.fun_.isFnLen()) {
                return null
            }
            return call.args.onePos()
        }

        /// If this call expression is `type(x)`, return `x`.
        fun asType(call: CallCompiled): IrSpanned<ExprCompiled>? {
            if (!call.fun_.isFnType()) {
                return null
            }
            return call.args.onePos()
        }

        /// If this call expression is `isinstance(x, t)`, return `(x, t)`.
        fun asIsinstance(call: CallCompiled): Pair<IrSpanned<ExprCompiled>, FrozenValue>? {
            if (!call.fun_.isFnIsinstance()) {
                return null
            }
            val (x, t) = call.args.twoPos() ?: return null
            val tValue = t.asValue() ?: return null
            return x to tValue
        }

        /// This call is infallible and has no side effects.
        fun isPureInfallible(call: CallCompiled): Boolean {
            val arg = asType(call)
            return arg?.isPureInfallible() ?: false
        }

        /// This call is a method call.
        fun method(call: CallCompiled): Triple<IrSpanned<ExprCompiled>, Symbol, ArgsCompiledValue>? {
            val node = call.fun_.node
            if (node is ExprCompiled.Builtin1Expr && node.builtin is Builtin1.Dot) {
                return Triple(node.expr, (node.builtin as Builtin1.Dot).name, call.args)
            }
            return null
        }

        /// Try to inline a function like `lambda x: type(x) == "y"`.
        private fun tryTypeIs(fun_: ExprCompiled, args: ArgsCompiledValue): ExprCompiled? {
            val frozenDef = fun_.asFrozenDef() ?: return null
            val pos = args.onePos() ?: return null
            val body = frozenDef.defInfo.inlineDefBody
            if (body is InlineDefBody.ReturnTypeIs) {
                return ExprCompiled.typeIs(pos.clone(), body.type)
            }
            return null
        }

        /// Inline calls to functions which are safe to inline.
        private fun tryInline(
            span: FrameSpan,
            fun_: ExprCompiled,
            args: ArgsCompiledValue,
            ctx: OptCtx,
        ): IrSpanned<ExprCompiled>? {
            val frozenDef = fun_.asFrozenDef() ?: return null

            if (frozenDef.parameters.hasArgsOrKwargs()) {
                // Functions with `*args` or `**kwargs` are not marked safe to inline,
                // but it is safer to also handle it explicitly here.
                return null
            }

            val body = frozenDef.defInfo.inlineDefBody
            val expr = if (body is InlineDefBody.ReturnSafeToInlineExpr) {
                body.expr
            } else {
                return null
            }

            val paramCount = ctx.paramCount
            val exprToValue = { e: ExprCompiled ->
                when (e) {
                    is ExprCompiled.ValueExpr -> e.value.toValue()
                    is ExprCompiled.Local -> if (e.index < paramCount) {
                        // Definitely assigned local variable.
                        localAsValue(e)?.toValue()
                    } else {
                        null
                    }
                    else -> null
                }
            }

            return args.allValuesGeneric(exprToValue) { arguments ->
                val slots = arrayOfNulls<Value>(frozenDef.parameters.len())
                frozenDef.parameters
                    .collect(arguments.frozenToV(), slots, ctx.heap())
                    .getOrNull() ?: return@allValuesGeneric null

                val frozenSlots = mutableListOf<FrozenValue>()
                for (slot in slots) {
                    val value = slot ?: return@allValuesGeneric null
                    val frozen = value.unpackFrozen() ?: return@allValuesGeneric null
                    frozenSlots.add(frozen)
                }

                val inlinedExpr = IrSpanned(span, expr.node.clone())
                val spanAlloc = InlinedFrameAlloc.new(ctx.frozenHeap())
                inlinedExpr.visitSpans { exprSpan ->
                    exprSpan.inlinedFrames.inlineInto(span, frozenDef.toFrozenValue(), spanAlloc)
                }
                InlineDefCallSite(ctx, frozenSlots).inline(inlinedExpr).getOrNull()
            }
        }

        private fun trySpecExec(
            span: FrameSpan,
            fun_: ExprCompiled,
            args: ArgsCompiledValue,
            ctx: OptCtx,
        ): ExprCompiled? {
            val funValue = fun_.asValue() ?: return null

            if (!funValue.speculativeExecSafe()) {
                return null
            }

            val eval = ctx.eval() ?: return null

            // Only if all call arguments are frozen values.
            return args.allValues { arguments ->
                val v = funValue.toValue().invoke(arguments.frozenToV(), eval).getOrNull()
                    ?: return@allValues null
                ExprCompiled.tryValue(span, v, ctx.frozenHeap())
            }
        }

        // Optimize `MyEnum(arg)`.
        private fun tryEnumValue(
            fun_: IrSpanned<ExprCompiled>,
            args: ArgsCompiledValue,
        ): ExprCompiled? {
            val enumType = fun_.asValue()?.downcastFrozenRef() ?: return null
            val arg = args.onePos()?.asValue() ?: return null
            val constructed = enumType.value.construct(arg.toValue()).getOrNull()
                ?: return null
            return ExprCompiled.ValueExpr(constructed)
        }

        // Optimize `"aaa{}bbb".format(arg)`.
        private fun tryFormat(
            fun_: IrSpanned<ExprCompiled>,
            args: ArgsCompiledValue,
            ctx: OptCtx,
        ): ExprCompiled? {
            val boundMethod = fun_.node.asFrozenBoundMethod() ?: return null
            val format = FrozenStringValue.new(boundMethod.this_) ?: return null
            if (boundMethod.method.name != "format") {
                return null
            }
            val arg = args.onePos() ?: return null

            val (before, after) = parseFormatOne(format) ?: return null

            val beforeStr = FrozenStringValue(before)
            val afterStr = FrozenStringValue(after)
            return ExprCompiled.formatOne(beforeStr, arg.clone(), afterStr, ctx)
        }

        fun call(
            span: FrameSpan,
            fun_: IrSpanned<ExprCompiled>,
            args: ArgsCompiledValue,
            ctx: OptCtx,
        ): ExprCompiled {
            val typeIs = tryTypeIs(fun_.node, args)
            if (typeIs != null) {
                return typeIs
            }

            val inline = tryInline(span, fun_.node, args, ctx)
            if (inline != null) {
                return inline.node
            }

            if (fun_.isFnLen()) {
                val arg = args.onePos()
                if (arg != null) {
                    return ExprCompiled.len(span, arg.clone())
                }
            }

            if (fun_.isFnType()) {
                val arg = args.onePos()
                if (arg != null) {
                    return ExprCompiled.typ(span, arg.clone())
                }
            }

            val enumValue = tryEnumValue(fun_, args)
            if (enumValue != null) {
                return enumValue
            }

            val specExec = trySpecExec(span, fun_.node, args, ctx)
            if (specExec != null) {
                return specExec
            }

            val formatResult = tryFormat(fun_, args, ctx)
            if (formatResult != null) {
                return formatResult
            }

            val node = fun_.node
            if (node is ExprCompiled.Builtin1Expr && node.builtin is Builtin1.Dot) {
                return newMethod(
                    span,
                    node.expr.clone(),
                    (node.builtin as Builtin1.Dot).name,
                    fun_.span,
                    args,
                    ctx,
                )
            }

            return ExprCompiled.CallExpr(IrSpanned(
                span,
                CallCompiled(fun_, args),
            ))
        }
    }
}

internal fun IrSpanned<CallCompiled>.optimize(ctx: OptCtx): ExprCompiled {
    val (expr, args) = this.node.let { it.fun_ to it.args }
    val optimizedExpr = expr.optimize(ctx)
    val optimizedArgs = args.optimize(ctx)
    return CallCompiled.call(this.span, optimizedExpr, optimizedArgs, ctx)
}
