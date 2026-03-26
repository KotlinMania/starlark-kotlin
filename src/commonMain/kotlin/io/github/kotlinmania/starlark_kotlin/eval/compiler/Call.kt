// port-lint: source src/eval/compiler/call.rs
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

/** Compile function calls. */

import io.github.kotlinmania.starlark_kotlin.collections.symbol.Symbol
import io.github.kotlinmania.starlark_kotlin.eval.compiler.args.ArgsCompiledValue
import io.github.kotlinmania.starlark_kotlin.eval.compiler.def_inline.local_as_value.localAsValue
import io.github.kotlinmania.starlark_kotlin.eval.compiler.expr.Builtin1
import io.github.kotlinmania.starlark_kotlin.eval.compiler.expr.ExprCompiled
import io.github.kotlinmania.starlark_kotlin.eval.compiler.opt_ctx.OptCtx
import io.github.kotlinmania.starlark_kotlin.eval.runtime.FrameSpan
import io.github.kotlinmania.starlark_kotlin.eval.runtime.InlinedFrameAlloc
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.types.enumeration.FrozenEnumType
import io.github.kotlinmania.starlark_kotlin.values.types.string.intern.FrozenStringValue
import io.github.kotlinmania.starlark_kotlin.values.types.string.dot_format.parseFormatOne

/**
 * Compiled representation of a function call expression.
 *
 * Contains the function expression and its arguments. This is the IR node
 * for all call expressions (direct calls, method calls, etc.).
 */
internal class CallCompiled(
    /** The function expression being called. */
    val fun_: IrSpanned<ExprCompiled>,
    /** The compiled arguments to the call. */
    val args: ArgsCompiledValue,
) {

    /**
     * If this call expression is `len(x)`, return `x`.
     */
    fun asLen(): IrSpanned<ExprCompiled>? {
        if (!fun_.node.isFnLen()) {
            return null
        }
        return args.onePos()
    }

    /**
     * If this call expression is `type(x)`, return `x`.
     */
    fun asType(): IrSpanned<ExprCompiled>? {
        if (!fun_.node.isFnType()) {
            return null
        }
        return args.onePos()
    }

    /**
     * If this call expression is `isinstance(x, t)`, return `(x, t)`.
     */
    fun asIsinstance(): Pair<IrSpanned<ExprCompiled>, FrozenValue>? {
        if (!fun_.node.isFnIsinstance()) {
            return null
        }
        val (x, t) = args.twoPos() ?: return null
        val tValue = t.node.asValue() ?: return null
        return x to tValue
    }

    /**
     * This call is infallible and has no side effects.
     */
    fun isPureInfallible(): Boolean {
        val arg = asType()
        return arg?.node?.isPureInfallible() ?: false
    }

    /**
     * This call is a method call.
     *
     * Returns the receiver expression, method name, and arguments if
     * the function expression is a dot-access (i.e. `obj.method(args)`).
     */
    fun method(): Triple<IrSpanned<ExprCompiled>, Symbol, ArgsCompiledValue>? {
        val node = fun_.node
        if (node is ExprCompiled.Builtin1Expr && node.op is Builtin1.Dot) {
            return Triple(node.expr, node.op.field, args)
        }
        return null
    }

    companion object {

        /**
         * Compile a method call expression.
         *
         * If the receiver is a known value, attempts to resolve the attribute
         * at compile time and convert to a direct call. Otherwise, wraps
         * in a dot-access call.
         */
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
                    val vExpr: ExprCompiled = ExprCompiled.ValueExpr(v)
                    val vSpanned = IrSpanned(getattrSpan, vExpr)
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
                ),
            ))
        }

        /**
         * Try to inline a function like `lambda x: type(x) == "y"`.
         */
        private fun tryTypeIs(fun_: ExprCompiled, args: ArgsCompiledValue): ExprCompiled? {
            val frozenDef = fun_.asFrozenDef() ?: return null
            val pos = args.onePos() ?: return null
            val body = frozenDef.value.defInfo.inlineDefBody
            if (body is InlineDefBody.ReturnTypeIs) {
                return ExprCompiled.typeIs(pos.copy(), body.type)
            }
            return null
        }

        /**
         * Inline calls to functions which are safe to inline.
         */
        private fun tryInline(
            span: FrameSpan,
            fun_: ExprCompiled,
            args: ArgsCompiledValue,
            ctx: OptCtx,
        ): IrSpanned<ExprCompiled>? {
            val frozenDef = fun_.asFrozenDef() ?: return null

            if (frozenDef.value.parameters.hasArgsOrKwargs()) {
                // Functions with `*args` or `**kwargs` are not marked safe to inline,
                // but it is safer to also handle it explicitly here.
                return null
            }

            val body = frozenDef.value.defInfo.inlineDefBody
            val expr = if (body is InlineDefBody.ReturnSafeToInlineExpr) {
                body.expr
            } else {
                return null
            }

            val paramCount = ctx.paramCount
            val exprToValue = { e: ExprCompiled ->
                when (e) {
                    is ExprCompiled.ValueExpr -> e.value.toValue()
                    is ExprCompiled.Local -> if (e.slot.index.toInt() < paramCount.toInt()) {
                        // Definitely assigned local variable.
                        //
                        // Consider this example:
                        // ```
                        // def foo(x): bar() + x
                        // ```
                        // We can inline calls like `foo(x)` when `x` is definitely assigned,
                        // but if `x` is not we cannot do that, because
                        // we should emit `x` is not assigned error before call to `bar()`
                        // which may fail. We can implement inlining of variables which
                        // may be not assigned, or inlining of any expression arguments,
                        // but more work is needed for that.
                        localAsValue(e.slot)?.value?.toValue()
                    } else {
                        null
                    }
                    else -> null
                }
            }

            return args.allValuesGeneric(exprToValue) { arguments ->
                val slots = arrayOfNulls<Value>(frozenDef.value.parameters.len())
                frozenDef.value.parameters
                    .collect(arguments.frozenToV(), slots, ctx.heap())
                    .getOrNull() ?: return@allValuesGeneric null

                val frozenSlots = mutableListOf<FrozenValue>()
                for (slot in slots) {
                    // Value must be set, but better ignore optimization here than panic.
                    val value = slot ?: return@allValuesGeneric null
                    // Everything should be frozen here, but if not,
                    // it is safer to abandon optimization.
                    val frozen = value.unpackFrozen() ?: return@allValuesGeneric null
                    frozenSlots.add(frozen)
                }

                val inlinedExpr = IrSpanned(span, expr.node.copy())
                val spanAlloc = InlinedFrameAlloc.new(ctx.frozenHeap())
                inlinedExpr.visitSpans { exprSpan ->
                    exprSpan.inlinedFrames.inlineInto(
                        span,
                        frozenDef.value.toFrozenValue(),
                        spanAlloc,
                    )
                }
                InlineDefCallSite(ctx, frozenSlots).inline(inlinedExpr).getOrNull()
            }
        }

        /**
         * Try speculative execution of a call with all-constant arguments.
         */
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

        /**
         * Optimize `MyEnum(arg)`.
         */
        private fun tryEnumValue(
            fun_: IrSpanned<ExprCompiled>,
            args: ArgsCompiledValue,
        ): ExprCompiled? {
            val enumType = fun_.node.asValue()?.downcastFrozenRef<FrozenEnumType>() ?: return null
            val arg = args.onePos()?.node?.asValue() ?: return null
            val constructed = enumType.value.construct(arg.toValue()).getOrNull()
                ?: return null
            return ExprCompiled.ValueExpr(constructed)
        }

        /**
         * Optimize `"aaa{}bbb".format(arg)`.
         */
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

            val (before, after) = parseFormatOne(format.asStr()) ?: return null

            val beforeStr = ctx.frozenHeap().allocStrIntern(before)
            val afterStr = ctx.frozenHeap().allocStrIntern(after)
            return ExprCompiled.formatOne(beforeStr, arg.copy(), afterStr, ctx)
        }

        /**
         * Compile a call expression with optimizations.
         *
         * Attempts various optimizations in order:
         * 1. Inline `type(x) == "y"` pattern
         * 2. Inline safe-to-inline function bodies
         * 3. Optimize `len(x)` calls
         * 4. Optimize `type(x)` calls
         * 5. Optimize enum construction
         * 6. Speculative execution of pure functions
         * 7. Optimize `"...".format(arg)` calls
         * 8. Convert dot-access calls to method calls
         *
         * Falls back to a regular [ExprCompiled.Call] if no optimization applies.
         */
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
                    return ExprCompiled.len(span, arg.copy())
                }
            }

            if (fun_.node.isFnType()) {
                val arg = args.onePos()
                if (arg != null) {
                    return ExprCompiled.typ(span, arg.copy())
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
                    node.expr.copy(),
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

/**
 * Optimize a spanned [CallCompiled] expression.
 *
 * Recursively optimizes the function expression and arguments,
 * then applies call-level optimizations via [CallCompiled.call].
 */
internal fun IrSpanned<CallCompiled>.optimize(ctx: OptCtx): ExprCompiled {
    val (expr, args) = this.node.let { it.fun_ to it.args }
    val optimizedExpr = expr.optimize(ctx)
    val optimizedArgs = args.optimize(ctx)
    return CallCompiled.call(this.span, optimizedExpr, optimizedArgs, ctx)
}
