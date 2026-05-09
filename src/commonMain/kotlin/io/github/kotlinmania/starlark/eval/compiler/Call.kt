// port-lint: source eval/compiler/call.rs
package io.github.kotlinmania.starlark.eval.compiler

import io.github.kotlinmania.starlark.collections.symbol.Symbol
import io.github.kotlinmania.starlark.eval.compiler.args.ArgsCompiledValue
import io.github.kotlinmania.starlark.eval.compiler.optctx.OptCtx
import io.github.kotlinmania.starlark.eval.compiler.definline.localasvalue.localAsValue
import io.github.kotlinmania.starlark.eval.runtime.FrameSpan
import io.github.kotlinmania.starlark.eval.runtime.InlinedFrameAlloc
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.FrozenValueTyped
import io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark.values.types.BoundMethodGen
import io.github.kotlinmania.starlark.values.types.enumeration.enumtype.EnumTypeGen
import io.github.kotlinmania.starlark.values.types.string.parseFormatOne

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

/** Compile function calls. */

// --- CallCompiled ---

internal class CallCompiled(
    val fun_: IrSpanned<ExprCompiled>,
    val args: ArgsCompiledValue,
) {
    /** If this call expression is `len(x)`, return `x`. */
    fun asLen(): IrSpanned<ExprCompiled>? {
        if (!fun_.node.isFnLen()) {
            return null
        }
        return args.onePos()
    }

    /** If this call expression is `type(x)`, return `x`. */
    fun asType(): IrSpanned<ExprCompiled>? {
        if (!fun_.node.isFnType()) {
            return null
        }
        return args.onePos()
    }

    /** If this call expression is `isinstance(x, t)`, return `(x, t)`. */
    fun asIsinstance(): Pair<IrSpanned<ExprCompiled>, FrozenValue>? {
        if (!fun_.node.isFnIsinstance()) {
            return null
        }
        val (x, t) = args.twoPos() ?: return null
        val tValue = t.node.asValue() ?: return null
        return x to tValue
    }

    /** This call is infallible and has no side effects. */
    fun isPureInfallible(): Boolean {
        val arg = asType()
        return arg?.node?.isPureInfallible() ?: false
    }

    /** This call is a method call. */
    fun method(): Triple<IrSpanned<ExprCompiled>, Symbol, ArgsCompiledValue>? {
        val node = fun_.node
        if (node is ExprCompiled.Builtin1Expr && node.op is Builtin1.Dot) {
            return Triple(node.expr, node.op.field, args)
        }
        return null
    }

    fun optimize(ctx: OptCtx): ExprCompiled {
        val optimizedExpr = fun_.optimize(ctx)
        val optimizedArgs = args.optimize(ctx)
        return call(fun_.span, optimizedExpr, optimizedArgs, ctx)
    }

    companion object {
        fun newMethod(
            span: FrameSpan,
            this_: IrSpanned<ExprCompiled>,
            field: Symbol,
            getattrSpan: FrameSpan,
            args: ArgsCompiledValue,
            ctx: OptCtx,
        ): ExprCompiled {
            val thisValue = this_.node.asValue()
            if (thisValue != null) {
                val v = ExprCompiled.compileTimeGetattr(thisValue, field, ctx)
                if (v != null) {
                    val vSpanned = IrSpanned(getattrSpan, ExprCompiled.ValueExpr(v) as ExprCompiled)
                    return call(span, vSpanned, args, ctx)
                }
            }

            return ExprCompiled.Call(IrSpanned(
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

        /** Try to inline a function like `lambda x: type(x) == "y"`. */
        private fun tryTypeIs(fun_: ExprCompiled, args: ArgsCompiledValue): ExprCompiled? {
            val frozenDef: FrozenValueTyped<DefGen<FrozenValue>> = fun_.asFrozenDef() ?: return null
            val pos = args.onePos() ?: return null
            val body = frozenDef.asRef().defInfo.inlineDefBody
            if (body is InlineDefBody.ReturnTypeIs) {
                return ExprCompiled.typeIs(pos, body.type)
            }
            return null
        }

        /** Inline calls to functions which are safe to inline. */
        private fun tryInline(
            span: FrameSpan,
            fun_: ExprCompiled,
            args: ArgsCompiledValue,
            ctx: OptCtx,
        ): IrSpanned<ExprCompiled>? {
            val frozenDef: FrozenValueTyped<DefGen<FrozenValue>> = fun_.asFrozenDef() ?: return null

            if (frozenDef.asRef().parameters.hasArgsOrKwargs()) {
                // Functions with `*args` or `**kwargs` are not marked safe to inline,
                // but it is safer to also handle it explicitly here.
                return null
            }

            val body = frozenDef.asRef().defInfo.inlineDefBody
            val expr = if (body is InlineDefBody.ReturnSafeToInlineExpr) {
                body.expr
            } else {
                return null
            }

            val paramCount = ctx.paramCount
            val exprToValue = { e: ExprCompiled ->
                when (e) {
                    is ExprCompiled.ValueExpr -> e.value.toValue()
                    is ExprCompiled.Local -> if (e.slot.index < paramCount) {
                        localAsValue(e.slot)?.toValue()
                    } else {
                        null
                    }
                    else -> null
                }
            }

            return args.allValuesGeneric(exprToValue) { arguments ->
                val slots = MutableList<Value?>(frozenDef.asRef().parameters.len()) { null }
                try {
                    frozenDef.asRef().parameters
                        .collect(arguments.frozenToV(), slots, ctx.heap())
                } catch (_: Exception) {
                    return@allValuesGeneric null
                }

                val frozenSlots = mutableListOf<FrozenValue>()
                for (slot in slots) {
                    val value = slot ?: return@allValuesGeneric null
                    val frozen = value.unpackFrozen() ?: return@allValuesGeneric null
                    frozenSlots.add(frozen)
                }

                val inlinedExpr = IrSpanned(span, expr.node)
                val spanAlloc = InlinedFrameAlloc.new(ctx.frozenHeap())
                inlinedExpr.span.inlinedFrames.inlineInto(span, frozenDef.toFrozenValue(), spanAlloc)
                try {
                    InlineDefCallSite(ctx, frozenSlots).inline(inlinedExpr)
                } catch (_: CannotInline) {
                    null
                }
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

        /** Optimize `MyEnum(arg)`. */
        private fun tryEnumValue(
            fun_: IrSpanned<ExprCompiled>,
            args: ArgsCompiledValue,
        ): ExprCompiled? {
            val enumType = fun_.node.asValue()?.downcastFrozenRef<EnumTypeGen>() ?: return null
            val arg = args.onePos()?.let { it.node.asValue() } ?: return null
            val constructed = try {
                enumType.value.construct(arg.toValue())
            } catch (_: Exception) {
                return null
            }
            return ExprCompiled.ValueExpr(constructed.unpackFrozen() ?: return null)
        }

        /** Optimize `"aaa{}bbb".format(arg)`. */
        private fun tryFormat(
            fun_: IrSpanned<ExprCompiled>,
            args: ArgsCompiledValue,
            ctx: OptCtx,
        ): ExprCompiled? {
            val boundMethod: FrozenValueTyped<BoundMethodGen<FrozenValue>> = fun_.node.asFrozenBoundMethod() ?: return null
            val format = FrozenStringValue.new(boundMethod.asRef().thisValue) ?: return null
            if (boundMethod.asRef().method.asRef().name != "format") {
                return null
            }
            val arg = args.onePos() ?: return null

            val (before, after) = parseFormatOne(format.asStr()) ?: return null

            val beforeFsv = ctx.frozenHeap().allocStrIntern(before)
            val afterFsv = ctx.frozenHeap().allocStrIntern(after)
            return ExprCompiled.formatOne(beforeFsv, arg, afterFsv, ctx)
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

            if (fun_.node.isFnLen()) {
                val arg = args.onePos()
                if (arg != null) {
                    return ExprCompiled.len(span, arg)
                }
            }

            if (fun_.node.isFnType()) {
                val arg = args.onePos()
                if (arg != null) {
                    return ExprCompiled.typ(span, arg)
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
            if (node is ExprCompiled.Builtin1Expr && node.op is Builtin1.Dot) {
                return newMethod(
                    span,
                    node.expr,
                    node.op.field,
                    fun_.span,
                    args,
                    ctx,
                )
            }

            return ExprCompiled.Call(IrSpanned(
                span,
                CallCompiled(fun_, args),
            ))
        }
    }
}

internal fun IrSpanned<CallCompiled>.optimize(ctx: OptCtx): ExprCompiled {
    return this.node.optimize(ctx)
}
