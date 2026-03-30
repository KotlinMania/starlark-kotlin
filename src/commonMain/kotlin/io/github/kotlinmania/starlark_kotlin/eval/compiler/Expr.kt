// port-lint: source src/eval/compiler/expr.rs
package io.github.kotlinmania.starlark_kotlin.eval.compiler

/*
 * Copyright 2018 The Starlark in Rust Authors.
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
 * Evaluation of an expression.
 */

import io.github.kotlinmania.starlark_kotlin.codemap.Spanned
import io.github.kotlinmania.starlark_kotlin.collections.symbol.Symbol
import io.github.kotlinmania.starlark_kotlin.environment.ModuleSlotId
import io.github.kotlinmania.starlark_kotlin.errors.didYouMean
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Evaluator
import io.github.kotlinmania.starlark_kotlin.eval.compiler.args.ArgsCompiledValue
import io.github.kotlinmania.starlark_kotlin.eval.compiler.args.compileArgs
import io.github.kotlinmania.starlark_kotlin.eval.compiler.constants.Constants
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.CstExpr
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.CstIdent
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.CstPayload
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Arguments
import io.github.kotlinmania.starlark_kotlin.eval.runtime.FrameSpan
import io.github.kotlinmania.starlark_kotlin.eval.runtime.frozen_file_span.FrozenFileSpan
import io.github.kotlinmania.starlark_kotlin.eval.runtime.LocalCapturedSlotId
import io.github.kotlinmania.starlark_kotlin.eval.runtime.LocalSlotId
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstExprP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstLiteral
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstPayload
import io.github.kotlinmania.starlark_kotlin.syntax.ast.BinOp
import io.github.kotlinmania.starlark_kotlin.syntax.ast.CallArgsP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ClauseP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ExprP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ForClauseP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.FStringP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.LambdaP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.StmtP
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenRef
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.ValueError
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.toValue
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.allocTuple
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValueTyped
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark_kotlin.values.types.tuple.Tuple
import io.github.kotlinmania.starlark_kotlin.values.types.tuple.TupleGen
import io.github.kotlinmania.starlark_kotlin.values.types.tuple.fromValue
import io.github.kotlinmania.starlark_kotlin.eval.compiler.opt_ctx.OptCtx
import io.github.kotlinmania.starlark_kotlin.values.types.UnboundValue
import io.github.kotlinmania.starlark_kotlin.values.types.BoundMethodGen
import io.github.kotlinmania.starlark_kotlin.values.types.list.FrozenListData
import io.github.kotlinmania.starlark_kotlin.values.types.int.InlineInt
import io.github.kotlinmania.starlark_kotlin.values.types.list.ListData
import io.github.kotlinmania.starlark_kotlin.values.types.list.ListRef
import io.github.kotlinmania.starlark_kotlin.values.types.range.Range
import io.github.kotlinmania.starlark_kotlin.values.types.bool.BOOL_TYPE
import io.github.kotlinmania.starlark_kotlin.values.types.bool.StarlarkBool
import io.github.kotlinmania.starlark_kotlin.values.types.dict.Dict
import io.github.kotlinmania.starlark_kotlin.values.types.float.StarlarkFloat
import io.github.kotlinmania.starlark_kotlin.values.types.float.allocFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.types.int.StarlarkInt
import io.github.kotlinmania.starlark_kotlin.values.types.FrozenBoundMethod
import io.github.kotlinmania.starlark_kotlin.values.layout.constFrozenString
import io.github.kotlinmania.starlark_kotlin.eval.compiler.opt_ctx.OptCtxEvalForEvaluator

// ---------------------------------------------------------------------------
// MaybeNot
// ---------------------------------------------------------------------------

/** `bool` operation. */
internal enum class MaybeNot {
    Id,
    Not;

    fun negate(): MaybeNot = when (this) {
        Id -> Not
        Not -> Id
    }
}

// ---------------------------------------------------------------------------
// CompareOp
// ---------------------------------------------------------------------------

/** Map result of comparison to boolean. */
internal enum class CompareOp {
    Less,
    Greater,
    LessOrEqual,
    GreaterOrEqual;

    /**
     * Apply this comparison operator to an [Ordering][Int] result.
     *
     * Follows the Java/Kotlin convention where negative means "less than",
     * zero means "equal", and positive means "greater than".
     */
    fun apply(x: Int): Boolean = when (this) {
        Less -> x < 0
        Greater -> x > 0
        LessOrEqual -> x <= 0
        GreaterOrEqual -> x >= 0
    }
}

// ---------------------------------------------------------------------------
// Builtin1
// ---------------------------------------------------------------------------

/** Builtin function with one argument. */
internal sealed class Builtin1 {
    data object Minus : Builtin1()

    /** `+x`. */
    data object Plus : Builtin1()

    /** `~x`. */
    data object BitNot : Builtin1()

    /** `not x`. */
    data object Not : Builtin1()

    /** `type(arg) == "y"`. */
    data class TypeIs(val type: FrozenStringValue) : Builtin1()

    /** `"aaa%sbbb" % arg`. */
    data class PercentSOne(val before: FrozenStringValue, val after: FrozenStringValue) : Builtin1()

    /** `"aaa%sbbb".format(arg)`. */
    data class FormatOne(val before: FrozenStringValue, val after: FrozenStringValue) : Builtin1()

    /** `x.field`. */
    data class Dot(val field: Symbol) : Builtin1()

    internal fun eval(v: FrozenValue, ctx: OptCtx): Value? {
        return when (this) {
            is Minus -> v.toValue().minus(ctx.heap()).getOrNull()
            is Plus -> v.toValue().plus(ctx.heap()).getOrNull()
            is BitNot -> v.toValue().bitNot(ctx.heap()).getOrNull()
            is Not -> Value.newBool(!v.toValue().toBool())
            is TypeIs -> Value.newBool(v.toValue().getTypeValue() == type)
            is FormatOne -> {
                val result = io.github.kotlinmania.starlark_kotlin.values.types.string.formatOne(
                    before.asStr(), v.toValue(), after.asStr(), ctx.heap()
                )
                result.toValue()
            }
            is PercentSOne -> {
                io.github.kotlinmania.starlark_kotlin.values.types.string.percentSOne(
                    before.asStr(), v.toValue(), after.asStr(), ctx.heap()
                ).getOrNull()?.toValue()
            }
            is Dot -> ExprCompiled.compileTimeGetattr(v, field, ctx)?.toValue()
        }
    }
}

// ---------------------------------------------------------------------------
// Builtin2
// ---------------------------------------------------------------------------

/** Builtin function with two arguments. */
internal sealed class Builtin2 {
    /** `a == b`. */
    data object Equals : Builtin2()
    /** `a in b`. */
    data object In : Builtin2()
    /** `a - b`. */
    data object Sub : Builtin2()
    /** `a + b`. */
    data object Add : Builtin2()
    /** `a * b`. */
    data object Multiply : Builtin2()
    /** `a % b`. */
    data object Percent : Builtin2()
    /** `a / b`. */
    data object Divide : Builtin2()
    /** `a // b`. */
    data object FloorDivide : Builtin2()
    /** `a & b`. */
    data object BitAnd : Builtin2()
    /** `a | b`. */
    data object BitOr : Builtin2()
    /** `a ^ b`. */
    data object BitXor : Builtin2()
    /** `a << b`. */
    data object LeftShift : Builtin2()
    /** `a >> b`. */
    data object RightShift : Builtin2()
    /** `a <=> b`. */
    data class Compare(val op: CompareOp) : Builtin2()
    /** `a[b]`. */
    data object ArrayIndex : Builtin2()

    internal fun eval(a: Value, b: Value, heap: Heap): Result<Value> {
        return when (this) {
            is Equals -> a.equals(b).map { Value.newBool(it) }
            is Compare -> a.compare(b).map { Value.newBool(op.apply(it)) }
            is In -> b.isIn(a).map { Value.newBool(it) }
            is Sub -> a.sub(b, heap)
            is Add -> a.add(b, heap)
            is Multiply -> a.mul(b, heap)
            is Percent -> a.percent(b, heap)
            is Divide -> a.div(b, heap)
            is FloorDivide -> a.floorDiv(b, heap)
            is BitAnd -> a.bitAnd(b, heap)
            is BitOr -> a.bitOr(b, heap)
            is BitXor -> a.bitXor(b, heap)
            is LeftShift -> a.leftShift(b, heap)
            is RightShift -> a.rightShift(b, heap)
            is ArrayIndex -> a.at(b, heap)
        }
    }
}

// ---------------------------------------------------------------------------
// ExprLogicalBinOp
// ---------------------------------------------------------------------------

/** Logical binary operator. */
internal enum class ExprLogicalBinOp {
    And,
    Or,
}

// ---------------------------------------------------------------------------
// ExprCompiled
// ---------------------------------------------------------------------------

/**
 * Compiled expression IR node.
 *
 * Each variant corresponds to a distinct expression form that the compiler emits
 * after macro-expansion and scope resolution.
 */
internal sealed class ExprCompiled {
    data class ValueExpr(val value: FrozenValue) : ExprCompiled()

    /** Read local non-captured variable. */
    data class Local(val slot: LocalSlotId) : ExprCompiled()

    /** Read local captured variable. */
    data class LocalCaptured(val slot: LocalCapturedSlotId) : ExprCompiled()

    data class Module(val slot: ModuleSlotId) : ExprCompiled()

    data class TupleExpr(val elements: List<IrSpanned<ExprCompiled>>) : ExprCompiled()

    data class ListExpr(val elements: List<IrSpanned<ExprCompiled>>) : ExprCompiled()

    data class DictExpr(val entries: List<Pair<IrSpanned<ExprCompiled>, IrSpanned<ExprCompiled>>>) : ExprCompiled()

    /** Comprehension. */
    data class Compr(val compr: ComprCompiled) : ExprCompiled()

    data class If(
        val cond: IrSpanned<ExprCompiled>,
        val thenBranch: IrSpanned<ExprCompiled>,
        val elseBranch: IrSpanned<ExprCompiled>,
    ) : ExprCompiled()

    data class Slice(
        val obj: IrSpanned<ExprCompiled>,
        val start: IrSpanned<ExprCompiled>?,
        val stop: IrSpanned<ExprCompiled>?,
        val step: IrSpanned<ExprCompiled>?,
    ) : ExprCompiled()

    data class Builtin1Expr(val op: Builtin1, val expr: IrSpanned<ExprCompiled>) : ExprCompiled()

    data class LogicalBinOp(
        val op: ExprLogicalBinOp,
        val lhs: IrSpanned<ExprCompiled>,
        val rhs: IrSpanned<ExprCompiled>,
    ) : ExprCompiled()

    /**
     * Expression equivalent to `(x, y)[1]`: evaluate `x`, discard the result,
     * then evaluate `y` and use its result.
     */
    data class Seq(
        val first: IrSpanned<ExprCompiled>,
        val second: IrSpanned<ExprCompiled>,
    ) : ExprCompiled()

    data class Builtin2Expr(
        val op: Builtin2,
        val lhs: IrSpanned<ExprCompiled>,
        val rhs: IrSpanned<ExprCompiled>,
    ) : ExprCompiled()

    data class Index2(
        val obj: IrSpanned<ExprCompiled>,
        val index0: IrSpanned<ExprCompiled>,
        val index1: IrSpanned<ExprCompiled>,
    ) : ExprCompiled()

    data class Call(val call: IrSpanned<CallCompiled>) : ExprCompiled()

    data class Def(val def: DefCompiled) : ExprCompiled()

    // -----------------------------------------------------------------------
    // Instance methods
    // -----------------------------------------------------------------------

    fun asValue(): FrozenValue? = when (this) {
        is ValueExpr -> value
        else -> null
    }

    /** Expression is known to be a constant which is a `def`. */
    internal fun asFrozenDef(): FrozenValueTyped<FrozenDef>? {
        return FrozenValueTyped.new(asValue() ?: return null)
    }

    /** Expression is known to be a frozen bound method. */
    internal fun asFrozenBoundMethod(): FrozenValueTyped<FrozenBoundMethod>? {
        return FrozenValueTyped.new<FrozenBoundMethod>(asValue() ?: return null)
    }

    /** Expression is builtin `len` function. */
    internal fun isFnLen(): Boolean {
        val v = asValue() ?: return false
        return v == Constants.get().fnLen
    }

    /** Expression is builtin `type` function. */
    internal fun isFnType(): Boolean {
        val v = asValue() ?: return false
        return v == Constants.get().fnType
    }

    /** Expression is builtin `isinstance` function. */
    internal fun isFnIsinstance(): Boolean {
        val v = asValue() ?: return false
        return v == Constants.get().fnIsinstance
    }

    /** If expression is `type(x)`, return `x`. */
    internal fun asType(): IrSpanned<ExprCompiled>? = when (this) {
        is Call -> call.node.asType()
        else -> null
    }

    /** If expression is `type(x) == t`, return `x` and `t`. */
    internal fun asTypeIs(): Pair<IrSpanned<ExprCompiled>, FrozenStringValue>? = when (this) {
        is Builtin1Expr -> {
            val builtin = this.op
            if (builtin is Builtin1.TypeIs) Pair(this.expr, builtin.type) else null
        }
        else -> null
    }

    /** Expression is a frozen value which is builtin. */
    internal fun asBuiltinValue(): FrozenValue? = when (this) {
        is ValueExpr -> if (value.isBuiltin()) value else null
        else -> null
    }

    /** Is expression a constant string? */
    internal fun asString(): FrozenStringValue? {
        return FrozenStringValue.new(asValue() ?: return null)
    }

    /** Iterable produced by this expression results in empty. */
    internal fun isIterableEmpty(): Boolean = when (this) {
        is ListExpr -> elements.isEmpty()
        is TupleExpr -> elements.isEmpty()
        is DictExpr -> entries.isEmpty()
        is ValueExpr -> value.isBuiltin() && value.toValue().length().getOrNull() == 0
        else -> false
    }

    /**
     * Result of this expression is definitely `bool`
     * (if `false` it may also be `bool`).
     */
    private fun isDefinitelyBool(): Boolean = when (this) {
        is ValueExpr -> value.unpackBool() != null
        is Builtin1Expr -> op is Builtin1.Not || op is Builtin1.TypeIs
        is Builtin2Expr -> op is Builtin2.In || op is Builtin2.Equals || op is Builtin2.Compare
        else -> false
    }

    /**
     * This expression is definitely:
     * - infallible
     * - has no effects
     */
    internal fun isPureInfallible(): Boolean = when (this) {
        is ValueExpr -> true
        is ListExpr -> elements.all { it.node.isPureInfallible() }
        is TupleExpr -> elements.all { it.node.isPureInfallible() }
        is DictExpr -> entries.isEmpty()
        is Builtin1Expr -> (op is Builtin1.Not || op is Builtin1.TypeIs) && expr.node.isPureInfallible()
        is Seq -> first.node.isPureInfallible() && second.node.isPureInfallible()
        is LogicalBinOp -> lhs.node.isPureInfallible() && rhs.node.isPureInfallible()
        is If -> cond.node.isPureInfallible() && thenBranch.node.isPureInfallible() && elseBranch.node.isPureInfallible()
        is Call -> call.node.isPureInfallible()
        else -> false
    }

    /**
     * If this expression is pure, infallible, and known to produce a value,
     * return truth of that value.
     */
    internal fun isPureInfallibleToBool(): Boolean? = when (this) {
        is ValueExpr -> value.toValue().toBool()
        is ListExpr -> if (elements.all { it.node.isPureInfallible() }) elements.isNotEmpty() else null
        is TupleExpr -> if (elements.all { it.node.isPureInfallible() }) elements.isNotEmpty() else null
        is DictExpr -> if (entries.isEmpty()) false else null
        is Builtin1Expr -> if (op is Builtin1.Not) expr.node.isPureInfallibleToBool()?.let { !it } else null
        is LogicalBinOp -> {
            val xVal = lhs.node.isPureInfallibleToBool()
            val yVal = rhs.node.isPureInfallibleToBool()
            when {
                op == ExprLogicalBinOp.And && xVal == true -> yVal
                op == ExprLogicalBinOp.Or && xVal == false -> yVal
                op == ExprLogicalBinOp.And && xVal == false -> false
                op == ExprLogicalBinOp.Or && xVal == true -> true
                else -> null
            }
        }
        else -> null
    }

    /** This expression is local slot. */
    internal fun asLocalNonCaptured(): LocalSlotId? = when (this) {
        is Local -> slot
        else -> null
    }

    companion object {

        // ---------------------------------------------------------------
        // equals
        // ---------------------------------------------------------------

        internal fun equals(
            l: IrSpanned<ExprCompiled>,
            r: IrSpanned<ExprCompiled>,
        ): IrSpanned<ExprCompiled> {
            val span = l.span.merge(r.span)
            val lv = l.node.asValue()
            val rv = r.node.asValue()
            if (lv != null && rv != null) {
                // If comparison fails, let it fail in runtime.
                val result = lv.equals(rv.toValue())
                if (result.isSuccess) {
                    return IrSpanned(
                        span = span,
                        node = ValueExpr(FrozenValue.newBool(result.getOrThrow())),
                    )
                }
            }

            val tryLR = tryEvalTypeIs(l, r)
            if (tryLR.isSuccess) return tryLR.getOrThrow()
            val (l2, r2) = tryLR.exceptionOrNull()?.let {
                (it as TypeIsFoldFailed).pair
            } ?: return tryLR.getOrThrow()

            val tryRL = tryEvalTypeIs(r2, l2)
            if (tryRL.isSuccess) return tryRL.getOrThrow()
            val (r3, l3) = tryRL.exceptionOrNull()?.let {
                (it as TypeIsFoldFailed).pair
            } ?: return tryRL.getOrThrow()

            return IrSpanned(
                span = span,
                node = Builtin2Expr(Builtin2.Equals, l3, r3),
            )
        }

        // ---------------------------------------------------------------
        // not
        // ---------------------------------------------------------------

        internal fun not(span: FrameSpan, expr: IrSpanned<ExprCompiled>): IrSpanned<ExprCompiled> {
            return when (val node = expr.node) {
                is ValueExpr -> IrSpanned(
                    node = ValueExpr(FrozenValue.newBool(!node.value.toValue().toBool())),
                    span = span,
                )
                // Collapse `not not e` to `e` only if `e` is known to produce a boolean.
                is Builtin1Expr -> {
                    if (node.op is Builtin1.Not && node.expr.node.isDefinitelyBool()) {
                        node.expr
                    } else {
                        IrSpanned(
                            node = Builtin1Expr(Builtin1.Not, expr),
                            span = span,
                        )
                    }
                }
                else -> IrSpanned(
                    node = Builtin1Expr(Builtin1.Not, expr),
                    span = span,
                )
            }
        }

        // ---------------------------------------------------------------
        // or / and
        // ---------------------------------------------------------------

        private fun or(
            l: IrSpanned<ExprCompiled>,
            r: IrSpanned<ExprCompiled>,
        ): IrSpanned<ExprCompiled> = logicalBinOp(ExprLogicalBinOp.Or, l, r)

        private fun and(
            l: IrSpanned<ExprCompiled>,
            r: IrSpanned<ExprCompiled>,
        ): IrSpanned<ExprCompiled> = logicalBinOp(ExprLogicalBinOp.And, l, r)

        internal fun logicalBinOp(
            op: ExprLogicalBinOp,
            l: IrSpanned<ExprCompiled>,
            r: IrSpanned<ExprCompiled>,
        ): IrSpanned<ExprCompiled> {
            val lv = l.node.isPureInfallibleToBool()
            if (lv != null) {
                return if (lv == (op == ExprLogicalBinOp.Or)) {
                    l
                } else {
                    r
                }
            }
            val span = l.span.merge(r.span)
            return IrSpanned(
                node = LogicalBinOp(op, l, r),
                span = span,
            )
        }

        // ---------------------------------------------------------------
        // seq
        // ---------------------------------------------------------------

        internal fun seq(
            l: IrSpanned<ExprCompiled>,
            r: IrSpanned<ExprCompiled>,
        ): IrSpanned<ExprCompiled> {
            if (l.node.isPureInfallible()) {
                return r
            }
            val span = l.span.merge(r.span)
            return IrSpanned(
                node = Seq(l, r),
                span = span,
            )
        }

        // ---------------------------------------------------------------
        // percent
        // ---------------------------------------------------------------

        private fun percent(
            l: IrSpanned<ExprCompiled>,
            r: IrSpanned<ExprCompiled>,
            ctx: OptCtx,
        ): ExprCompiled {
            val str = l.node.asString()
            if (str != null) {
                val parsed = parsePercentSOne(str.asStr())
                if (parsed != null) {
                    val (before, after) = parsed
                    val frozenBefore = ctx.frozenHeap().allocStrIntern(before)
                    val frozenAfter = ctx.frozenHeap().allocStrIntern(after)
                    return percentSOne(frozenBefore, r, frozenAfter, ctx)
                }
            }
            return Builtin2Expr(Builtin2.Percent, l, r)
        }

        private fun percentSOne(
            before: FrozenStringValue,
            arg: IrSpanned<ExprCompiled>,
            after: FrozenStringValue,
            ctx: OptCtx,
        ): ExprCompiled {
            val argVal = arg.node.asValue()
            if (argVal != null) {
                val result = io.github.kotlinmania.starlark_kotlin.values.types.string.percentSOne(
                    before.asStr(), argVal.toValue(), after.asStr(), ctx.heap()
                )
                if (result.isSuccess) {
                    val value = ctx.frozenHeap().allocStrIntern(result.getOrThrow().asStr())
                    return ValueExpr(value.toFrozenValue())
                }
            }
            return Builtin1Expr(Builtin1.PercentSOne(before, after), arg)
        }

        // ---------------------------------------------------------------
        // formatOne
        // ---------------------------------------------------------------

        internal fun formatOne(
            before: FrozenStringValue,
            arg: IrSpanned<ExprCompiled>,
            after: FrozenStringValue,
            ctx: OptCtx,
        ): ExprCompiled {
            val argVal = arg.node.asValue()
            if (argVal != null) {
                val value = io.github.kotlinmania.starlark_kotlin.values.types.string.formatOne(
                    before.asStr(), argVal.toValue(), after.asStr(), ctx.heap()
                )
                val frozen = ctx.frozenHeap().allocStrIntern(value.asStr())
                return ValueExpr(frozen.toFrozenValue())
            }
            return Builtin1Expr(Builtin1.FormatOne(before, after), arg)
        }

        // ---------------------------------------------------------------
        // add
        // ---------------------------------------------------------------

        private fun add(
            l: IrSpanned<ExprCompiled>,
            r: IrSpanned<ExprCompiled>,
        ): ExprCompiled {
            val lList = l.asShortList()
            val rList = r.asShortList()
            if (lList != null && rList != null) {
                return ListExpr(lList.asExprs() + rList.asExprs())
            }
            return Builtin2Expr(Builtin2.Add, l, r)
        }

        // ---------------------------------------------------------------
        // binOp
        // ---------------------------------------------------------------

        internal fun binOp(
            binOp: Builtin2,
            l: IrSpanned<ExprCompiled>,
            r: IrSpanned<ExprCompiled>,
            ctx: OptCtx,
        ): ExprCompiled {
            val span = l.span.merge(r.span)
            // Binary operators should have no side effects,
            // but to avoid possible problems, we only fold binary operators on builtin types.
            val lv = l.node.asBuiltinValue()
            val rv = r.node.asBuiltinValue()
            if (lv != null && rv != null) {
                val evalResult = binOp.eval(lv.toValue(), rv.toValue(), ctx.heap())
                if (evalResult.isSuccess) {
                    val tried = tryValue(span, evalResult.getOrThrow(), ctx.frozenHeap())
                    if (tried != null) return tried
                }
            }

            return when (binOp) {
                Builtin2.Percent -> percent(l, r, ctx)
                Builtin2.Add -> add(l, r)
                Builtin2.Equals -> equals(l, r).node
                Builtin2.ArrayIndex -> index(l, r, ctx)
                else -> Builtin2Expr(binOp, l, r)
            }
        }

        // ---------------------------------------------------------------
        // ifExpr
        // ---------------------------------------------------------------

        internal fun ifExpr(
            cond: IrSpanned<ExprCompiled>,
            t: IrSpanned<ExprCompiled>,
            f: IrSpanned<ExprCompiled>,
        ): IrSpanned<ExprCompiled> {
            val condSpan = cond.span
            val condBool = ExprCompiledBool.new(cond)
            return when (val node = condBool.node) {
                is ExprCompiledBool.Const -> if (node.value) t else f
                is ExprCompiledBool.Expr -> {
                    when (val condExpr = node.expr) {
                        is Builtin1Expr -> {
                            if (condExpr.op is Builtin1.Not) {
                                ifExpr(condExpr.expr, f, t)
                            } else {
                                val fullCond = IrSpanned<ExprCompiled>(node = condExpr, span = condSpan)
                                val span = fullCond.span.merge(t.span).merge(f.span)
                                IrSpanned(
                                    node = If(fullCond, t, f),
                                    span = span,
                                )
                            }
                        }
                        is Seq -> {
                            seq(condExpr.first, ifExpr(condExpr.second, t, f))
                        }
                        else -> {
                            val fullCond = IrSpanned<ExprCompiled>(node = condExpr, span = condSpan)
                            val span = fullCond.span.merge(t.span).merge(f.span)
                            IrSpanned(
                                node = If(fullCond, t, f),
                                span = span,
                            )
                        }
                    }
                }
            }
        }

        // ---------------------------------------------------------------
        // unOp
        // ---------------------------------------------------------------

        internal fun unOp(
            span: FrameSpan,
            op: Builtin1,
            expr: IrSpanned<ExprCompiled>,
            ctx: OptCtx,
        ): ExprCompiled {
            val builtinVal = expr.node.asBuiltinValue()
            if (builtinVal != null) {
                val evalResult = op.eval(builtinVal, ctx)
                if (evalResult != null) {
                    val tried = tryValue(expr.span, evalResult, ctx.frozenHeap())
                    if (tried != null) return tried
                }
            }
            return when (op) {
                is Builtin1.FormatOne -> formatOne(op.before, expr, op.after, ctx)
                is Builtin1.PercentSOne -> percentSOne(op.before, expr, op.after, ctx)
                is Builtin1.Dot -> dot(expr, op.field, ctx)
                is Builtin1.TypeIs -> typeIs(expr, op.type)
                is Builtin1.Not -> not(span, expr).node
                else -> Builtin1Expr(op, expr)
            }
        }

        // ---------------------------------------------------------------
        // tryValues / tryValue
        // ---------------------------------------------------------------

        private fun tryValues(
            span: FrameSpan,
            values: List<Value>,
            heap: FrozenHeap,
        ): List<IrSpanned<ExprCompiled>>? {
            return values.map { v ->
                val expr = tryValue(span, v, heap) ?: return null
                IrSpanned(span = span, node = expr)
            }
        }

        /**
         * Try convert a maybe-not-frozen value to an expression, or discard it.
         */
        internal fun tryValue(span: FrameSpan, v: Value, heap: FrozenHeap): ExprCompiled? {
            val frozen = v.unpackFrozen()
            if (frozen != null) {
                // If frozen, we are lucky.
                return ValueExpr(frozen)
            }
            val str = v.unpackStr()
            if (str != null) {
                return if (str.length <= 1000) {
                    // If string, copy it to frozen heap.
                    ValueExpr(heap.allocStrIntern(str).toFrozenValue())
                } else {
                    // Long strings may lead to exponential explosion in the optimizer,
                    // so skip optimizations for them.
                    null
                }
            }
            val floatVal = v.downcastRef<StarlarkFloat>()
            if (floatVal != null) {
                return ValueExpr(heap.allocSimple(floatVal))
            }
            val rangeVal = v.downcastRef<Range>()
            if (rangeVal != null) {
                return ValueExpr(heap.allocSimple(rangeVal))
            }
            val listVal = ListRef.fromValue(v)
            if (listVal != null) {
                // When spec-safe function returned a non-frozen list,
                // we try to convert that list to a list of constants instruction.
                val items = tryValues(span, listVal.content(), heap) ?: return null
                return ListExpr(items)
            }
            val tupleVal = Tuple.fromValue(v)
            if (tupleVal != null) {
                val items = tryValues(span, tupleVal.content(), heap) ?: return null
                return tuple(items, heap)
            }
            return null
        }

        // ---------------------------------------------------------------
        // compr
        // ---------------------------------------------------------------

        internal fun compr(compr: ComprCompiled): ExprCompiled {
            return when (compr) {
                is ComprCompiled.List -> {
                    if (compr.clauses.isNop()) {
                        ListExpr(emptyList())
                    } else {
                        Compr(compr)
                    }
                }
                is ComprCompiled.Dict -> {
                    if (compr.clauses.isNop()) {
                        DictExpr(emptyList())
                    } else {
                        Compr(compr)
                    }
                }
            }
        }

        // ---------------------------------------------------------------
        // tuple
        // ---------------------------------------------------------------

        /**
         * Construct tuple expression from elements optimizing to frozen tuple value when possible.
         */
        internal fun tuple(elems: List<IrSpanned<ExprCompiled>>, heap: FrozenHeap): ExprCompiled {
            val frozenElems = elems.map { it.node.asValue() ?: return TupleExpr(elems) }
            return ValueExpr(heap.allocTuple(frozenElems))
        }

        // ---------------------------------------------------------------
        // compileTimeGetattr
        // ---------------------------------------------------------------

        internal fun compileTimeGetattr(
            left: FrozenValue,
            attr: Symbol,
            ctx: OptCtx,
        ): FrozenValue? {
            // We assume `getattr` has no side effects.
            val v = getAttrHashedRaw(left.toValue(), attr, ctx.heap()) ?: return null
            return when (v) {
                is MemberOrValue.Member -> {
                    val member = v.member
                    when (member) {
                        is UnboundValue.Method -> ctx.frozenHeap().allocSimple(
                            BoundMethodGen(member.method, left)
                        )
                        is UnboundValue.Attr -> null
                        else -> null
                    }
                }
                is MemberOrValue.ValueResult -> v.value.unpackFrozen()
            }
        }

        // ---------------------------------------------------------------
        // dot
        // ---------------------------------------------------------------

        internal fun dot(
            obj: IrSpanned<ExprCompiled>,
            field: Symbol,
            ctx: OptCtx,
        ): ExprCompiled {
            val leftVal = obj.node.asValue()
            if (leftVal != null) {
                val v = compileTimeGetattr(leftVal, field, ctx)
                if (v != null) return ValueExpr(v)
            }
            return Builtin1Expr(Builtin1.Dot(field), obj)
        }

        // ---------------------------------------------------------------
        // slice
        // ---------------------------------------------------------------

        internal fun slice(
            span: FrameSpan,
            array: IrSpanned<ExprCompiled>,
            start: IrSpanned<ExprCompiled>?,
            stop: IrSpanned<ExprCompiled>?,
            step: IrSpanned<ExprCompiled>?,
            ctx: OptCtx,
        ): ExprCompiled {
            val arrayVal = array.node.asBuiltinValue()
            val startVal = start?.node?.asValue()
            val stopVal = stop?.node?.asValue()
            val stepVal = step?.node?.asValue()
            if (arrayVal != null &&
                (start == null || startVal != null) &&
                (stop == null || stopVal != null) &&
                (step == null || stepVal != null)
            ) {
                val sliceResult = arrayVal.toValue().slice(
                    startVal?.toValue(),
                    stopVal?.toValue(),
                    stepVal?.toValue(),
                    ctx.heap(),
                )
                if (sliceResult.isSuccess) {
                    val tried = tryValue(span, sliceResult.getOrThrow(), ctx.frozenHeap())
                    if (tried != null) return tried
                }
            }
            return Slice(array, start, stop, step)
        }

        // ---------------------------------------------------------------
        // index
        // ---------------------------------------------------------------

        internal fun index(
            array: IrSpanned<ExprCompiled>,
            index: IrSpanned<ExprCompiled>,
            ctx: OptCtx,
        ): ExprCompiled {
            val span = array.span.merge(index.span)
            val arrayVal = array.node.asBuiltinValue()
            val indexVal = index.node.asValue()
            if (arrayVal != null && indexVal != null) {
                val result = arrayVal.toValue().at(indexVal.toValue(), ctx.heap())
                if (result.isSuccess) {
                    val tried = tryValue(span, result.getOrThrow(), ctx.frozenHeap())
                    if (tried != null) return tried
                }
            }
            return Builtin2Expr(Builtin2.ArrayIndex, array, index)
        }

        // ---------------------------------------------------------------
        // index2
        // ---------------------------------------------------------------

        internal fun index2(
            array: IrSpanned<ExprCompiled>,
            index0: IrSpanned<ExprCompiled>,
            index1: IrSpanned<ExprCompiled>,
        ): ExprCompiled {
            return Index2(array, index0, index1)
        }

        // ---------------------------------------------------------------
        // typ
        // ---------------------------------------------------------------

        internal fun typ(span: FrameSpan, v: IrSpanned<ExprCompiled>): ExprCompiled {
            return when (val node = v.node) {
                is ValueExpr -> {
                    ValueExpr(node.value.toValue().getTypeValue().toFrozenValue())
                }
                is TupleExpr -> {
                    if (node.elements.all { it.node.isPureInfallible() }) {
                        ValueExpr(constFrozenString(TupleGen.TYPE).toFrozenValue())
                    } else {
                        typCall(span, v)
                    }
                }
                is ListExpr -> {
                    if (node.elements.all { it.node.isPureInfallible() }) {
                        ValueExpr(constFrozenString(ListData.TYPE).toFrozenValue())
                    } else {
                        typCall(span, v)
                    }
                }
                is DictExpr -> {
                    if (node.entries.isEmpty()) {
                        ValueExpr(constFrozenString(Dict.TYPE).toFrozenValue())
                    } else {
                        typCall(span, v)
                    }
                }
                is Builtin1Expr -> {
                    if ((node.op is Builtin1.Not || node.op is Builtin1.TypeIs)
                        && node.expr.node.isPureInfallible()
                    ) {
                        ValueExpr(constFrozenString(BOOL_TYPE).toFrozenValue())
                    } else {
                        typCall(span, v)
                    }
                }
                else -> typCall(span, v)
            }
        }

        private fun typCall(span: FrameSpan, v: IrSpanned<ExprCompiled>): ExprCompiled {
            return Call(IrSpanned(
                span = span,
                node = CallCompiled(
                    fun_ = IrSpanned(
                        span = span,
                        node = ValueExpr(Constants.get().fnType!!.value),
                    ),
                    args = ArgsCompiledValue(
                        posNamed = mutableListOf(v),
                    ),
                ),
            ))
        }

        // ---------------------------------------------------------------
        // typeIs
        // ---------------------------------------------------------------

        internal fun typeIs(v: IrSpanned<ExprCompiled>, t: FrozenStringValue): ExprCompiled {
            val vVal = v.node.asValue()
            if (vVal != null) {
                return ValueExpr(FrozenValue.newBool(
                    vVal.toValue().getType() == t.asStr()
                ))
            }
            return Builtin1Expr(Builtin1.TypeIs(t), v)
        }

        // ---------------------------------------------------------------
        // len
        // ---------------------------------------------------------------

        internal fun len(span: FrameSpan, arg: IrSpanned<ExprCompiled>): ExprCompiled {
            val argVal = arg.node.asValue()
            if (argVal != null) {
                val lenResult = argVal.toValue().length()
                if (lenResult.isSuccess) {
                    val len = lenResult.getOrThrow()
                    val inlineInt = InlineInt.tryFrom(len).getOrNull()
                    if (inlineInt != null) {
                        return ValueExpr(FrozenValue.newInt(inlineInt))
                    }
                }
            }
            return Call(IrSpanned(
                span = span,
                node = CallCompiled(
                    fun_ = IrSpanned(
                        span = span,
                        node = ValueExpr(Constants.get().fnLen!!.value),
                    ),
                    args = ArgsCompiledValue(
                        posNamed = mutableListOf(arg),
                    ),
                ),
            ))
        }
    }
}

// ---------------------------------------------------------------------------
// ExprShortList - helper for list concatenation optimization
// ---------------------------------------------------------------------------

private sealed class ExprShortList {
    class Exprs(val exprs: List<IrSpanned<ExprCompiled>>) : ExprShortList()
    class Constants(val constants: List<FrozenValue>) : ExprShortList()
}

private data class SpannedExprShortList(
    val node: ExprShortList,
    val span: FrameSpan,
) {
    fun asExprs(): List<IrSpanned<ExprCompiled>> = when (node) {
        is ExprShortList.Exprs -> node.exprs
        is ExprShortList.Constants -> node.constants.map { c ->
            IrSpanned(
                node = ExprCompiled.ValueExpr(c),
                span = span,
            )
        }
    }
}

/** Try to extract `[e0, e1, ..., en]` from this expression. */
private fun IrSpanned<ExprCompiled>.asShortList(): SpannedExprShortList? {
    // Prevent exponential explosion during optimization.
    val maxLen = 1000
    return when (val n = node) {
        is ExprCompiled.ListExpr -> {
            if (n.elements.size <= maxLen) {
                SpannedExprShortList(ExprShortList.Exprs(n.elements), span)
            } else {
                null
            }
        }
        is ExprCompiled.ValueExpr -> {
            val list = FrozenListData.fromFrozenValue(n.value) ?: return null
            if (list.len() <= maxLen) {
                SpannedExprShortList(ExprShortList.Constants(list.contentFrozen()), span)
            } else {
                null
            }
        }
        else -> null
    }
}

// ---------------------------------------------------------------------------
// IrSpanned<ExprCompiled> extension: isPureInfallibleToBool
// ---------------------------------------------------------------------------

internal fun IrSpanned<ExprCompiled>.isPureInfallibleToBool(): Boolean? {
    return node.isPureInfallibleToBool()
}

// ---------------------------------------------------------------------------
// IrSpanned<ExprCompiled>.optimize
// ---------------------------------------------------------------------------

internal fun IrSpanned<ExprCompiled>.optimize(ctx: OptCtx): IrSpanned<ExprCompiled> {
    val span = this.span
    val expr = when (val e = this.node) {
        is ExprCompiled.ValueExpr,
        is ExprCompiled.Local,
        is ExprCompiled.LocalCaptured -> e

        is ExprCompiled.Module -> {
            val frozen = ctx.frozenModule()?.getSlot(e.slot)
            if (frozen != null) {
                ExprCompiled.ValueExpr(frozen)
            } else {
                // Let it fail at runtime.
                ExprCompiled.Module(e.slot)
            }
        }

        is ExprCompiled.TupleExpr -> {
            ExprCompiled.tuple(e.elements.map { it.optimize(ctx) }, ctx.frozenHeap())
        }

        is ExprCompiled.ListExpr -> {
            ExprCompiled.ListExpr(e.elements.map { it.optimize(ctx) })
        }

        is ExprCompiled.DictExpr -> {
            ExprCompiled.DictExpr(e.entries.map { (k, v) -> Pair(k.optimize(ctx), v.optimize(ctx)) })
        }

        is ExprCompiled.Compr -> e.compr.optimize(ctx)

        is ExprCompiled.If -> {
            val cond = IrSpanned(span = e.cond.span, node = e.cond.node).optimize(ctx)
            val t = e.thenBranch.optimize(ctx)
            val f = e.elseBranch.optimize(ctx)
            return ExprCompiled.ifExpr(cond, t, f)
        }

        is ExprCompiled.Slice -> {
            val v = IrSpanned(span = e.obj.span, node = e.obj.node).optimize(ctx)
            val start = e.start?.optimize(ctx)
            val stop = e.stop?.optimize(ctx)
            val step = e.step?.optimize(ctx)
            ExprCompiled.slice(span, v, start, stop, step, ctx)
        }

        is ExprCompiled.Builtin1Expr -> {
            val optimizedExpr = e.expr.optimize(ctx)
            ExprCompiled.unOp(span, e.op, optimizedExpr, ctx)
        }

        is ExprCompiled.LogicalBinOp -> {
            val l = e.lhs.optimize(ctx)
            val r = e.rhs.optimize(ctx)
            return ExprCompiled.logicalBinOp(e.op, l, r)
        }

        is ExprCompiled.Seq -> {
            val l = e.first.optimize(ctx)
            val r = e.second.optimize(ctx)
            return ExprCompiled.seq(l, r)
        }

        is ExprCompiled.Builtin2Expr -> {
            val l = e.lhs.optimize(ctx)
            val r = e.rhs.optimize(ctx)
            ExprCompiled.binOp(e.op, l, r, ctx)
        }

        is ExprCompiled.Index2 -> {
            val a = e.obj.optimize(ctx)
            val i0 = e.index0.optimize(ctx)
            val i1 = e.index1.optimize(ctx)
            ExprCompiled.index2(a, i0, i1)
        }

        is ExprCompiled.Def -> e

        is ExprCompiled.Call -> e.call.node.optimize(ctx)
    }
    return IrSpanned(node = expr, span = span)
}

// ---------------------------------------------------------------------------
// EvalError
// ---------------------------------------------------------------------------

internal sealed class EvalError(override val message: String) : Exception(message) {
    class DuplicateDictionaryKey(key: String) : EvalError("Dictionary key repeated for `$key`")
}

// ---------------------------------------------------------------------------
// tryEvalTypeIs - helper for folding type(x) == "y"
// ---------------------------------------------------------------------------

/**
 * Exception used to propagate original (l, r) pair when [tryEvalTypeIs] fails.
 */
private class TypeIsFoldFailed(
    val pair: Pair<IrSpanned<ExprCompiled>, IrSpanned<ExprCompiled>>,
) : Exception()

/**
 * Try fold expression `cmp(l == r)` into `cmp(type(x) == "y")`.
 * Return original `l` and `r` arguments if fold was unsuccessful.
 */
private fun tryEvalTypeIs(
    l: IrSpanned<ExprCompiled>,
    r: IrSpanned<ExprCompiled>,
): Result<IrSpanned<ExprCompiled>> {
    val span = l.span.merge(r.span)
    val lType = l.node.asType()
    val rStr = r.node.asString()
    return if (lType != null && rStr != null) {
        Result.success(IrSpanned(
            span = span,
            node = ExprCompiled.typeIs(lType, rStr),
        ))
    } else {
        Result.failure(TypeIsFoldFailed(Pair(l, r)))
    }
}

// ---------------------------------------------------------------------------
// AstLiteralCompile
// ---------------------------------------------------------------------------

/** Compile an AST literal to a frozen value. */
private fun AstLiteral.compile(heap: FrozenHeap): FrozenValue = when (this) {
    is AstLiteral.Int -> {
        val si = StarlarkInt.from(value.node)
        when (si) {
            is StarlarkInt.Small -> FrozenValue.newInt(si.value)
            is StarlarkInt.Big -> heap.allocSimple(si.value)
        }
    }
    is AstLiteral.Float -> StarlarkFloat(value.node).allocFrozenValue(heap)
    is AstLiteral.String -> heap.allocStrIntern(value.node).toFrozenValue()
    is AstLiteral.Ellipsis -> heap.alloc(io.github.kotlinmania.starlark_kotlin.values.types.ellipsis.Ellipsis)
}

// ---------------------------------------------------------------------------
// CompilerExprUtil - helper trait for AST expressions
// ---------------------------------------------------------------------------

/** Unpack a string literal from an expression. */
private fun <P : AstPayload> ExprP<P>.unpackStringLiteral(): String? = when (this) {
    is ExprP.Literal -> {
        val lit = this.literal
        if (lit is AstLiteral.String) lit.value.node else null
    }
    else -> null
}

/**
 * Check whether an entire sequence of additions reduces to a string literal.
 */
private fun <P : AstPayload> reducesToString(
    op: BinOp,
    left: AstExprP<P>,
    right: AstExprP<P>,
): String? {
    var currentOp = op
    var currentLeft = left
    var currentRight = right
    val results = mutableListOf<String>()

    while (true) {
        if (currentOp != BinOp.Add) return null
        // a + b + c  associates as  (a + b) + c
        val x = currentRight.node.unpackStringLiteral() ?: return null
        results.add(x)
        when (val leftNode = currentLeft.node) {
            is ExprP.Op<*> -> {
                currentOp = leftNode.op
                @Suppress("UNCHECKED_CAST")
                currentLeft = leftNode.lhs as AstExprP<P>
                @Suppress("UNCHECKED_CAST")
                currentRight = leftNode.rhs as AstExprP<P>
            }
            else -> {
                val y = currentLeft.node.unpackStringLiteral() ?: return null
                results.add(y)
                break
            }
        }
    }
    results.reverse()
    return results.joinToString("")
}

// ---------------------------------------------------------------------------
// getAttrNoAttrError
// ---------------------------------------------------------------------------

private fun getAttrNoAttrError(x: Value, attribute: Symbol): Exception {
    val attrStr = attribute.asStr()
    val candidates = x.dirAttr()
    val suggestion = didYouMean(attrStr, candidates)
    return if (suggestion == null) {
        ValueError.NoAttr(x.getType(), attrStr)
    } else {
        ValueError.NoAttrDidYouMean(x.getType(), attrStr, suggestion)
    }
}

// ---------------------------------------------------------------------------
// MemberOrValue
// ---------------------------------------------------------------------------

internal sealed class MemberOrValue {
    class Member(val member: UnboundValue) : MemberOrValue()
    class ValueResult(val value: Value) : MemberOrValue()

    internal fun invoke(
        thisVal: Value,
        span: FrozenRef<FrameSpan>,
        args: Arguments,
        eval: Evaluator,
    ): Result<Value> {
        return when (this) {
            is Member -> member.invokeMethod(thisVal, span, args, eval)
            is ValueResult -> value.invokeWithLoc(span, args, eval)
        }
    }
}

// ---------------------------------------------------------------------------
// getAttrHashedRaw
// ---------------------------------------------------------------------------

internal fun getAttrHashedRaw(
    x: Value,
    attribute: Symbol,
    heap: Heap,
): MemberOrValue? {
    val aref = x.getRef()
    val methods = aref.vtable().methods()
    if (methods != null) {
        val v = methods.getFrozenSymbol(attribute)
        if (v != null) {
            return MemberOrValue.Member(v)
        }
    }
    val attrResult = aref.getAttrHashed(attribute.asStrHashed(), heap)
    return if (attrResult != null) {
        MemberOrValue.ValueResult(attrResult)
    } else {
        throw getAttrNoAttrError(x, attribute)
    }
}

// ---------------------------------------------------------------------------
// getAttrHashedBind
// ---------------------------------------------------------------------------

internal fun getAttrHashedBind(
    x: Value,
    attribute: Symbol,
    heap: Heap,
): Result<Value> {
    val aref = x.getRef()
    val methods = aref.vtable().methods()
    if (methods != null) {
        val v = methods.getFrozenSymbol(attribute)
        if (v != null) {
            return v.bind(x, heap)
        }
    }
    val attrResult = aref.getAttrHashed(attribute.asStrHashed(), heap)
    return if (attrResult != null) {
        // Only `get_methods` is allowed to return unbound methods or attributes.
        // Both types are crate private, so we assume `get_attr` never returns them.
        Result.success(attrResult)
    } else {
        Result.failure(getAttrNoAttrError(x, attribute))
    }
}

// ---------------------------------------------------------------------------
// Compiler.exprIdent
// ---------------------------------------------------------------------------

private fun Compiler.exprIdent(ident: CstIdent): ExprCompiled {
    val resolvedIdent = ident.node.payload
        ?: error("variable not resolved: `${ident.node.ident}`")
    return when (resolvedIdent) {
        is ResolvedIdent.Slot -> {
            val slot = resolvedIdent.slot
            val bindingId = resolvedIdent.bindingId
            val binding = this.scopeData.getBinding(bindingId)

            when (slot) {
                is Slot.Local -> {
                    // We can't look up the local variables in advance, because they are different each time
                    // we go through a new function call.
                    when (binding.captured) {
                        Captured.Yes -> ExprCompiled.LocalCaptured(LocalCapturedSlotId(slot.id.index))
                        Captured.No -> ExprCompiled.Local(LocalSlotId(slot.id.index))
                    }
                }
                is Slot.Module -> {
                    // We can only inline variables if they were assigned once
                    // otherwise we might inline the wrong value.
                    if (binding.assignCount == AssignCount.AtMostOnce) {
                        val v = this.eval.moduleEnv.slots().getSlot(slot.id)
                        if (v != null) {
                            // We could inline non-frozen values, but these values
                            // can be garbage-collected, so it is somewhat harder to implement.
                            val frozen = v.unpackFrozen()
                            if (frozen != null) {
                                return ExprCompiled.ValueExpr(frozen)
                            }
                        }
                    }
                    ExprCompiled.Module(slot.id)
                }
            }
        }
        is ResolvedIdent.Global -> ExprCompiled.ValueExpr(resolvedIdent.value)
        else -> error("Unexpected resolved ident: $resolvedIdent")
    }
}

// ---------------------------------------------------------------------------
// Compiler.optCtx
// ---------------------------------------------------------------------------

private fun Compiler.optCtx(): OptCtx {
    val paramCount = this.currentScope().paramCount()
    return OptCtx.new(OptCtxEvalForEvaluator(this.eval), paramCount.toUInt())
}

// ---------------------------------------------------------------------------
// Compiler.expr (main entry point)
// ---------------------------------------------------------------------------

/**
 * Compile an expression from the CST to the IR.
 */
internal fun Compiler.expr(
    expr: CstExpr,
): Result<IrSpanned<ExprCompiled>> {
    val span = FrameSpan.new(FrozenFileSpan.new(this.codemap, expr.span))
    val compiledExpr: ExprCompiled = try {
        when (val node = expr.node) {
            is ExprP.Identifier<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                val ident = node.ident as CstIdent
                exprIdent(ident)
            }
            is ExprP.Lambda<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                val lambda = node.lambda as LambdaP<CstPayload, ScopeId>
                val signatureSpan = lambda.signatureSpan()
                val frozenSignatureSpan = FrozenFileSpan.new(this.codemap, signatureSpan)
                val suite = Spanned(
                    span = expr.span,
                    node = StmtP.Return(lambda.body),
                )
                this.function(
                    "lambda", frozenSignatureSpan, lambda.payload,
                    lambda.params, null, suite
                )
            }
            is ExprP.Tuple<*> -> {
                @Suppress("UNCHECKED_CAST")
                val elements = node.elements as List<CstExpr>
                val xs = this.exprs(elements).getOrThrow()
                ExprCompiled.tuple(xs, this.eval.moduleEnv.frozenHeap())
            }
            is ExprP.ListExpr<*> -> {
                @Suppress("UNCHECKED_CAST")
                val elements = node.elements as List<CstExpr>
                val xs = this.exprs(elements).getOrThrow()
                ExprCompiled.ListExpr(xs)
            }
            is ExprP.Dict<*> -> {
                @Suppress("UNCHECKED_CAST")
                val elements = node.elements as List<Pair<CstExpr, CstExpr>>
                val xs = elements.map { (k, v) ->
                    Pair(this.expr(k).getOrThrow(), this.expr(v).getOrThrow())
                }
                ExprCompiled.DictExpr(xs)
            }
            is ExprP.If<*> -> {
                @Suppress("UNCHECKED_CAST")
                val cond = this.expr(node.cond as CstExpr).getOrThrow()
                @Suppress("UNCHECKED_CAST")
                val thenExpr = this.expr(node.v1 as CstExpr).getOrThrow()
                @Suppress("UNCHECKED_CAST")
                val elseExpr = this.expr(node.v2 as CstExpr).getOrThrow()
                return Result.success(ExprCompiled.ifExpr(cond, thenExpr, elseExpr))
            }
            is ExprP.Dot<*> -> {
                @Suppress("UNCHECKED_CAST")
                val left = this.expr(node.expr as CstExpr).getOrThrow()
                val s = Symbol.new(node.field.node)
                ExprCompiled.dot(left, s, this.optCtx())
            }
            is ExprP.Call<*> -> {
                @Suppress("UNCHECKED_CAST")
                val left = this.expr(node.expr as CstExpr).getOrThrow()
                @Suppress("UNCHECKED_CAST")
                val args = this.compileArgs(node.args as CallArgsP<CstPayload>).getOrThrow()
                CallCompiled.call(span, left, args, this.optCtx())
            }
            is ExprP.Index<*> -> {
                @Suppress("UNCHECKED_CAST")
                val array = this.expr(node.expr as CstExpr).getOrThrow()
                @Suppress("UNCHECKED_CAST")
                val index = this.expr(node.index as CstExpr).getOrThrow()
                ExprCompiled.index(array, index, this.optCtx())
            }
            is ExprP.Index2<*> -> {
                @Suppress("UNCHECKED_CAST")
                val array = this.expr(node.expr as CstExpr).getOrThrow()
                @Suppress("UNCHECKED_CAST")
                val index0 = this.expr(node.index0 as CstExpr).getOrThrow()
                @Suppress("UNCHECKED_CAST")
                val index1 = this.expr(node.index1 as CstExpr).getOrThrow()
                ExprCompiled.index2(array, index0, index1)
            }
            is ExprP.Slice<*> -> {
                @Suppress("UNCHECKED_CAST")
                val collection = this.expr(node.expr as CstExpr).getOrThrow()
                @Suppress("UNCHECKED_CAST")
                val start = (node.start as CstExpr?)?.let { this.expr(it).getOrThrow() }
                @Suppress("UNCHECKED_CAST")
                val stop = (node.stop as CstExpr?)?.let { this.expr(it).getOrThrow() }
                @Suppress("UNCHECKED_CAST")
                val stride = (node.step as CstExpr?)?.let { this.expr(it).getOrThrow() }
                ExprCompiled.slice(span, collection, start, stop, stride, this.optCtx())
            }
            is ExprP.Not<*> -> {
                @Suppress("UNCHECKED_CAST")
                val inner = this.expr(node.expr as CstExpr).getOrThrow()
                return Result.success(ExprCompiled.not(span, inner))
            }
            is ExprP.Minus<*> -> {
                @Suppress("UNCHECKED_CAST")
                val inner = this.expr(node.expr as CstExpr).getOrThrow()
                ExprCompiled.unOp(span, Builtin1.Minus, inner, this.optCtx())
            }
            is ExprP.Plus<*> -> {
                @Suppress("UNCHECKED_CAST")
                val inner = this.expr(node.expr as CstExpr).getOrThrow()
                ExprCompiled.unOp(span, Builtin1.Plus, inner, this.optCtx())
            }
            is ExprP.BitNot<*> -> {
                @Suppress("UNCHECKED_CAST")
                val inner = this.expr(node.expr as CstExpr).getOrThrow()
                ExprCompiled.unOp(span, Builtin1.BitNot, inner, this.optCtx())
            }
            is ExprP.Op<*> -> {
                @Suppress("UNCHECKED_CAST")
                val reduced = reducesToString(node.op, node.lhs as CstExpr, node.rhs as CstExpr)
                if (reduced != null) {
                    // Note there's const propagation for `+` on compiled expressions,
                    // but special handling of `+` on AST might be slightly more efficient
                    // (no unnecessary allocations on the heap). So keep it.
                    val v = this.eval.moduleEnv.frozenHeap().allocStrIntern(reduced)
                    ExprCompiled.ValueExpr(v.toFrozenValue())
                } else {
                    @Suppress("UNCHECKED_CAST")
                    val right: CstExpr = if (node.op == BinOp.In || node.op == BinOp.NotIn) {
                        listToTuple(node.rhs as CstExpr)
                    } else {
                        node.rhs as CstExpr
                    }

                    @Suppress("UNCHECKED_CAST")
                    val l = this.expr(node.lhs as CstExpr).getOrThrow()
                    val r = this.expr(right).getOrThrow()

                    when (node.op) {
                        BinOp.Or -> return Result.success(ExprCompiled.logicalBinOp(ExprLogicalBinOp.Or, l, r))
                        BinOp.And -> return Result.success(ExprCompiled.logicalBinOp(ExprLogicalBinOp.And, l, r))
                        BinOp.Equal -> return Result.success(ExprCompiled.equals(l, r))
                        BinOp.NotEqual -> return Result.success(ExprCompiled.not(span, ExprCompiled.equals(l, r)))
                        BinOp.Less -> ExprCompiled.binOp(Builtin2.Compare(CompareOp.Less), l, r, this.optCtx())
                        BinOp.Greater -> ExprCompiled.binOp(Builtin2.Compare(CompareOp.Greater), l, r, this.optCtx())
                        BinOp.LessOrEqual -> ExprCompiled.binOp(Builtin2.Compare(CompareOp.LessOrEqual), l, r, this.optCtx())
                        BinOp.GreaterOrEqual -> ExprCompiled.binOp(Builtin2.Compare(CompareOp.GreaterOrEqual), l, r, this.optCtx())
                        BinOp.In -> ExprCompiled.binOp(Builtin2.In, l, r, this.optCtx())
                        BinOp.NotIn -> ExprCompiled.not(
                            span,
                            IrSpanned(
                                span = span,
                                node = ExprCompiled.binOp(Builtin2.In, l, r, this.optCtx()),
                            ),
                        ).node
                        BinOp.Subtract -> ExprCompiled.binOp(Builtin2.Sub, l, r, this.optCtx())
                        BinOp.Add -> ExprCompiled.binOp(Builtin2.Add, l, r, this.optCtx())
                        BinOp.Multiply -> ExprCompiled.binOp(Builtin2.Multiply, l, r, this.optCtx())
                        BinOp.Percent -> ExprCompiled.binOp(Builtin2.Percent, l, r, this.optCtx())
                        BinOp.Divide -> ExprCompiled.binOp(Builtin2.Divide, l, r, this.optCtx())
                        BinOp.FloorDivide -> ExprCompiled.binOp(Builtin2.FloorDivide, l, r, this.optCtx())
                        BinOp.BitAnd -> ExprCompiled.binOp(Builtin2.BitAnd, l, r, this.optCtx())
                        BinOp.BitOr -> ExprCompiled.binOp(Builtin2.BitOr, l, r, this.optCtx())
                        BinOp.BitXor -> ExprCompiled.binOp(Builtin2.BitXor, l, r, this.optCtx())
                        BinOp.LeftShift -> ExprCompiled.binOp(Builtin2.LeftShift, l, r, this.optCtx())
                        BinOp.RightShift -> ExprCompiled.binOp(Builtin2.RightShift, l, r, this.optCtx())
                    }
                }
            }
            is ExprP.ListComprehension<*> -> {
                @Suppress("UNCHECKED_CAST")
                this.listComprehension(
                    node.expr as CstExpr,
                    node.forClause as ForClauseP<CstPayload>,
                    node.clauses as List<ClauseP<CstPayload>>,
                ).getOrThrow()
            }
            is ExprP.DictComprehension<*> -> {
                @Suppress("UNCHECKED_CAST")
                this.dictComprehension(
                    node.key as CstExpr,
                    node.value as CstExpr,
                    node.forClause as ForClauseP<CstPayload>,
                    node.clauses as List<ClauseP<CstPayload>>,
                ).getOrThrow()
            }
            is ExprP.Literal<*> -> {
                val v = node.literal.compile(this.eval.moduleEnv.frozenHeap())
                ExprCompiled.ValueExpr(v)
            }
            is ExprP.FString<*> -> {
                @Suppress("UNCHECKED_CAST")
                val fstring = node.fstring as Spanned<FStringP<CstPayload>>
                val fstringSpan = FrameSpan.new(FrozenFileSpan.new(this.codemap, fstring.span))

                // Desugar f"foo{x}bar{y}" to "foo{}bar{}.format(x, y)"
                val heap = this.eval.moduleEnv.frozenHeap()

                val format = IrSpanned<ExprCompiled>(
                    node = ExprCompiled.ValueExpr(heap.allocStrIntern(fstring.node.format.node).toFrozenValue()),
                    span = fstringSpan,
                )
                val method = IrSpanned<ExprCompiled>(
                    node = ExprCompiled.dot(format, Symbol.new("format"), this.optCtx()),
                    span = fstringSpan,
                )

                val args = ArgsCompiledValue()
                for (expression in fstring.node.expressions) {
                    @Suppress("UNCHECKED_CAST")
                    args.pushPos(this.expr(expression as CstExpr).getOrThrow())
                }

                CallCompiled.call(span, method, args, this.optCtx())
            }
        }
    } catch (e: CompilerInternalError) {
        return Result.failure(e)
    }
    return Result.success(IrSpanned(node = compiledExpr, span = span))
}

// ---------------------------------------------------------------------------
// Compiler.exprTruth
// ---------------------------------------------------------------------------

/**
 * Like [expr] but returns an expression optimized assuming
 * only the truth of the result is needed.
 */
internal fun Compiler.exprTruth(
    expr: CstExpr,
): Result<IrSpanned<ExprCompiledBool>> {
    return this.expr(expr).map { ExprCompiledBool.new(it) }
}

// ---------------------------------------------------------------------------
// Compiler.exprs
// ---------------------------------------------------------------------------

/** Compile a list of expressions. */
internal fun Compiler.exprs(
    exprs: List<CstExpr>,
): Result<List<IrSpanned<ExprCompiled>>> {
    val results = mutableListOf<IrSpanned<ExprCompiled>>()
    for (e in exprs) {
        val result = this.expr(e).getOrElse { return Result.failure(it) }
        results.add(result)
    }
    return Result.success(results)
}

// ---------------------------------------------------------------------------
// Helpers: parsePercentSOne
// ---------------------------------------------------------------------------

/**
 * Parse a string of the form `"...%s..."` into the `(before, after)` pieces.
 * Returns `null` if the string doesn't match the single `%s` pattern.
 */
private fun parsePercentSOne(s: String): Pair<String, String>? {
    return io.github.kotlinmania.starlark_kotlin.values.types.string.parsePercentSOne(s)
}
