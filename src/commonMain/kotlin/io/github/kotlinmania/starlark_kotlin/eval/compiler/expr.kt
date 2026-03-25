// port-lint: source src/eval/compiler/expr.rs
package io.github.kotlinmania.starlark_kotlin.eval.compiler.expr

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

/// Evaluation of an expression.

import io.github.kotlinmania.starlark_kotlin.eval.compiler.Compiler
import io.github.kotlinmania.starlark_kotlin.eval.compiler.args.ArgsCompiledValue
import io.github.kotlinmania.starlark_kotlin.eval.compiler.call.CallCompiled
import io.github.kotlinmania.starlark_kotlin.eval.compiler.compr.ComprCompiled
import io.github.kotlinmania.starlark_kotlin.eval.compiler.constants.Constants
import io.github.kotlinmania.starlark_kotlin.eval.compiler.def.DefCompiled
import io.github.kotlinmania.starlark_kotlin.eval.compiler.def.FrozenDef
import io.github.kotlinmania.starlark_kotlin.eval.compiler.opt_ctx.OptCtx
import io.github.kotlinmania.starlark_kotlin.eval.runtime.frozen_file_span.FrozenFileSpan
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenRef
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.ValueError
import io.github.kotlinmania.starlark_kotlin.values.types.string.intern.FrozenStringValue
import io.github.kotlinmania.starlark_kotlin.values.types.string.ValueLike
import io.github.kotlinmania.starlark_kotlin.values.types.string.Evaluator
import io.github.kotlinmania.starlark_kotlin.values.owned.FrozenValueTyped
import io.github.kotlinmania.starlark_kotlin.syntax.ast.Slot
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ResolvedIdent
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ModuleSlotId
import io.github.kotlinmania.starlark_kotlin.syntax.ast.CstExpr
import io.github.kotlinmania.starlark_kotlin.stdlib.Symbol
import io.github.kotlinmania.starlark_kotlin.eval.compiler.compr.ExprCompiledBool
import io.github.kotlinmania.starlark_kotlin.eval.compiler.args.IrSpanned
import io.github.kotlinmania.starlark_kotlin.eval.compiler.Captured
import io.github.kotlinmania.starlark_kotlin.eval.compiler.AssignCount
import io.github.kotlinmania.starlark_kotlin.eval.bc.writer.LocalSlotId
import io.github.kotlinmania.starlark_kotlin.eval.bc.writer.LocalCapturedSlotId
import io.github.kotlinmania.starlark_kotlin.eval.bc.writer.FrameSpan
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenStringValue
import io.github.kotlinmania.starlark_kotlin.values.length
import io.github.kotlinmania.starlark_kotlin.stdlib.new
import io.github.kotlinmania.starlark_kotlin.values.types.tuple.it
import io.github.kotlinmania.starlark_kotlin.values.types.asType
import io.github.kotlinmania.starlark_kotlin.values.toBool
import io.github.kotlinmania.starlark_kotlin.values.sub
import io.github.kotlinmania.starlark_kotlin.values.rightShift
import io.github.kotlinmania.starlark_kotlin.values.percent
import io.github.kotlinmania.starlark_kotlin.values.owned.unpackBool
import io.github.kotlinmania.starlark_kotlin.values.mul
import io.github.kotlinmania.starlark_kotlin.values.leftShift
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.profile.merge
import io.github.kotlinmania.starlark_kotlin.values.isIn
import io.github.kotlinmania.starlark_kotlin.values.div
import io.github.kotlinmania.starlark_kotlin.values.bitXor
import io.github.kotlinmania.starlark_kotlin.values.bitOr
import io.github.kotlinmania.starlark_kotlin.values.bitNot
import io.github.kotlinmania.starlark_kotlin.values.bitAnd
import io.github.kotlinmania.starlark_kotlin.values.at
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.Slot
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.ResolvedIdent
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.ModuleSlotId
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.CstExpr
import io.github.kotlinmania.starlark_kotlin.stdlib.add
import io.github.kotlinmania.starlark_kotlin.eval.bc.getTypeValue

/// `bool` operation.
// #[derive(Copy, Clone, Dupe, Eq, PartialEq, Debug)]
// pub(crate) enum MaybeNot { Id, Not }
internal enum class MaybeNot {
    Id,
    Not;

    // pub(crate) fn negate(self) -> MaybeNot
    fun negate(): MaybeNot = when (this) {
        Id -> Not
        Not -> Id
    }
}

/// Map result of comparison to boolean.
// #[derive(Copy, Clone, Dupe, Debug)]
// pub(crate) enum CompareOp { Less, Greater, LessOrEqual, GreaterOrEqual }
internal enum class CompareOp {
    Less,
    Greater,
    LessOrEqual,
    GreaterOrEqual;

    // fn apply(self, x: Ordering) -> bool
    fun apply(x: Int): Boolean = when (this) {
        Less -> x < 0
        Greater -> x > 0
        LessOrEqual -> x <= 0
        GreaterOrEqual -> x >= 0
    }
}

/// Builtin function with one argument.
// #[derive(Clone, Debug, VisitSpanMut)]
// pub(crate) enum Builtin1 { Minus, Plus, BitNot, Not, TypeIs(..), PercentSOne(..), FormatOne(..), Dot(..) }
internal sealed class Builtin1 {
    data object Minus : Builtin1()
    /// `+x`.
    data object Plus : Builtin1()
    /// `~x`.
    data object BitNot : Builtin1()
    /// `not x`.
    data object Not : Builtin1()
    /// `type(arg) == "y"`.
    data class TypeIs(val type: FrozenStringValue) : Builtin1()
    /// `"aaa%sbbb" % arg`.
    data class PercentSOne(val before: FrozenStringValue, val after: FrozenStringValue) : Builtin1()
    /// `"aaa%sbbb".format(arg)`.
    data class FormatOne(val before: FrozenStringValue, val after: FrozenStringValue) : Builtin1()
    /// `x.field`.
    data class Dot(val field: Symbol) : Builtin1()

    // fn eval<'v>(&self, v: FrozenValue, ctx: &mut OptCtx) -> Option<Value<'v>>
    internal fun eval(v: FrozenValue, ctx: OptCtx): Value? {
        return when (this) {
            is Minus -> v.toValue().minus(ctx.heap()).getOrNull()
            is Plus -> v.toValue().plus(ctx.heap()).getOrNull()
            is BitNot -> v.toValue().bitNot(ctx.heap()).getOrNull()
            is Not -> Value.newBool(!v.toValue().toBool())
            is TypeIs -> Value.newBool(v.toValue().getTypeValue() == type)
            is FormatOne -> ExprCompiled.formatOne(before, v, after, ctx)
            is PercentSOne -> ExprCompiled.percentSOne(before, v, after, ctx)
            is Dot -> ExprCompiled.compileTimeGetattr(v, field, ctx)?.toValue()
        }
    }
}

/// Builtin function with two arguments.
// #[derive(Copy, Clone, Dupe, Debug, VisitSpanMut)]
// pub(crate) enum Builtin2 { ... }
internal enum class Builtin2 {
    /// `a == b`.
    Equals,
    /// `a in b`.
    In,
    /// `a - b`.
    Sub,
    /// `a + b`.
    Add,
    /// `a * b`.
    Multiply,
    /// `a % b`.
    Percent,
    /// `a / b`.
    Divide,
    /// `a // b`.
    FloorDivide,
    /// `a & b`.
    BitAnd,
    /// `a | b`.
    BitOr,
    /// `a ^ b`.
    BitXor,
    /// `a << b`.
    LeftShift,
    /// `a >> b`.
    RightShift,
    /// `a[b]`.
    ArrayIndex;

    // fn eval<'v>(self, a: Value<'v>, b: Value<'v>, heap: Heap<'v>) -> crate::Result<Value<'v>>
    internal fun eval(a: Value, b: Value, heap: Heap): Result<Value> {
        return when (this) {
            Equals -> a.equals(b).map { Value.newBool(it) }
            In -> b.isIn(a).map { Value.newBool(it) }
            Sub -> a.sub(b, heap)
            Add -> a.add(b, heap) ?: Result.failure(ValueError.unsupportedException(a, "+", b))
            Multiply -> a.mul(b, heap) ?: Result.failure(ValueError.unsupportedException(a, "*", b))
            Percent -> a.percent(b, heap)
            Divide -> a.div(b, heap)
            FloorDivide -> a.floorDiv(b, heap)
            BitAnd -> a.bitAnd(b, heap)
            BitOr -> a.bitOr(b, heap)
            BitXor -> a.bitXor(b, heap)
            LeftShift -> a.leftShift(b, heap)
            RightShift -> a.rightShift(b, heap)
            ArrayIndex -> a.at(b, heap)
        }
    }
}

// Builtin2 with embedded CompareOp.
internal data class Builtin2Compare(val op: CompareOp) {
    fun eval(a: Value, b: Value): Result<Value> {
        return a.compare(b).map { Value.newBool(op.apply(it)) }
    }
}

/// Logical binary operator.
// #[derive(Copy, Clone, Dupe, Debug, VisitSpanMut, Eq, PartialEq)]
// pub(crate) enum ExprLogicalBinOp { And, Or }
internal enum class ExprLogicalBinOp {
    And,
    Or,
}

// #[derive(Clone, Debug, VisitSpanMut)]
// pub(crate) enum ExprCompiled { ... }
internal sealed class ExprCompiled {
    // Value(FrozenValue)
    data class ValueExpr(val value: FrozenValue) : ExprCompiled()
    /// Read local non-captured variable.
    // Local(LocalSlotId)
    data class Local(val slot: LocalSlotId) : ExprCompiled()
    /// Read local captured variable.
    // LocalCaptured(LocalCapturedSlotId)
    data class LocalCaptured(val slot: LocalCapturedSlotId) : ExprCompiled()
    // Module(ModuleSlotId)
    data class Module(val slot: ModuleSlotId) : ExprCompiled()
    // Tuple(Vec<IrSpanned<ExprCompiled>>)
    data class TupleExpr(val elements: List<IrSpanned<ExprCompiled>>) : ExprCompiled()
    // List(Vec<IrSpanned<ExprCompiled>>)
    data class ListExpr(val elements: List<IrSpanned<ExprCompiled>>) : ExprCompiled()
    // Dict(Vec<(IrSpanned<ExprCompiled>, IrSpanned<ExprCompiled>)>)
    data class DictExpr(val entries: List<Pair<IrSpanned<ExprCompiled>, IrSpanned<ExprCompiled>>>) : ExprCompiled()
    /// Comprehension.
    // Compr(ComprCompiled)
    data class Compr(val compr: ComprCompiled) : ExprCompiled()
    // If(Box<(IrSpanned<ExprCompiled>, IrSpanned<ExprCompiled>, IrSpanned<ExprCompiled>)>)
    data class If(val cond: IrSpanned<ExprCompiled>, val then: IrSpanned<ExprCompiled>, val elseExpr: IrSpanned<ExprCompiled>) : ExprCompiled()
    // Slice(Box<(IrSpanned<ExprCompiled>, Option<..>, Option<..>, Option<..>)>)
    data class Slice(
        val obj: IrSpanned<ExprCompiled>,
        val start: IrSpanned<ExprCompiled>?,
        val stop: IrSpanned<ExprCompiled>?,
        val step: IrSpanned<ExprCompiled>?,
    ) : ExprCompiled()
    // Builtin1(Builtin1, Box<IrSpanned<ExprCompiled>>)
    data class Builtin1Expr(val op: Builtin1, val expr: IrSpanned<ExprCompiled>) : ExprCompiled()
    // LogicalBinOp(ExprLogicalBinOp, Box<(IrSpanned<ExprCompiled>, IrSpanned<ExprCompiled>)>)
    data class LogicalBinOp(val op: ExprLogicalBinOp, val lhs: IrSpanned<ExprCompiled>, val rhs: IrSpanned<ExprCompiled>) : ExprCompiled()
    /// Expression equivalent to `(x, y)[1]`: evaluate `x`, discard the result,
    /// then evaluate `y` and use its result.
    // Seq(Box<(IrSpanned<ExprCompiled>, IrSpanned<ExprCompiled>)>)
    data class Seq(val first: IrSpanned<ExprCompiled>, val second: IrSpanned<ExprCompiled>) : ExprCompiled()
    // Builtin2(Builtin2, Box<(IrSpanned<ExprCompiled>, IrSpanned<ExprCompiled>)>)
    data class Builtin2Expr(val op: Builtin2, val lhs: IrSpanned<ExprCompiled>, val rhs: IrSpanned<ExprCompiled>) : ExprCompiled()
    // Index2(Box<(IrSpanned<ExprCompiled>, IrSpanned<ExprCompiled>, IrSpanned<ExprCompiled>)>)
    data class Index2(val obj: IrSpanned<ExprCompiled>, val a: IrSpanned<ExprCompiled>, val b: IrSpanned<ExprCompiled>) : ExprCompiled()
    // Call(Box<IrSpanned<CallCompiled>>)
    data class Call(val call: IrSpanned<CallCompiled>) : ExprCompiled()
    // Def(DefCompiled)
    data class Def(val def: DefCompiled) : ExprCompiled()

    // impl ExprCompiled

    // pub fn as_value(&self) -> Option<FrozenValue>
    fun asValue(): FrozenValue? = when (this) {
        is ValueExpr -> value
        else -> null
    }

    /// Expression is known to be a constant which is a `def`.
    // pub(crate) fn as_frozen_def(&self) -> Option<FrozenValueTyped<'_, FrozenDef>>
    internal fun asFrozenDef(): FrozenValueTyped<FrozenDef>? {
        return FrozenValueTyped.new(asValue() ?: return null)
    }

    /// Expression is builtin `len` function.
    // pub(crate) fn is_fn_len(&self) -> bool
    internal fun isFnLen(): Boolean {
        val v = asValue() ?: return false
        return v == Constants.get().fnLen
    }

    /// Expression is builtin `type` function.
    // pub(crate) fn is_fn_type(&self) -> bool
    internal fun isFnType(): Boolean {
        val v = asValue() ?: return false
        return v == Constants.get().fnType
    }

    /// Expression is builtin `isinstance` function.
    // pub(crate) fn is_fn_isinstance(&self) -> bool
    internal fun isFnIsinstance(): Boolean {
        val v = asValue() ?: return false
        return v == Constants.get().fnIsinstance
    }

    /// If expression is `type(x)`, return `x`.
    // pub(crate) fn as_type(&self) -> Option<&IrSpanned<ExprCompiled>>
    internal fun asType(): IrSpanned<ExprCompiled>? = when (this) {
        is Call -> call.node.asType()
        else -> null
    }

    /// If expression is `type(x) == t`, return `x` and `t`.
    // pub(crate) fn as_type_is(&self) -> Option<(&IrSpanned<ExprCompiled>, FrozenStringValue)>
    internal fun asTypeIs(): Pair<IrSpanned<ExprCompiled>, FrozenStringValue>? = when (this) {
        is Builtin1Expr -> {
            val op = this.op
            if (op is Builtin1.TypeIs) Pair(this.expr, op.type) else null
        }
        else -> null
    }

    /// Expression is a frozen value which is builtin.
    // pub(crate) fn as_builtin_value(&self) -> Option<FrozenValue>
    internal fun asBuiltinValue(): FrozenValue? = when (this) {
        is ValueExpr -> if (value.isBuiltin()) value else null
        else -> null
    }

    /// Is expression a constant string?
    // pub(crate) fn as_string(&self) -> Option<FrozenStringValue>
    internal fun asString(): FrozenStringValue? {
        return FrozenStringValue.new(asValue() ?: return null)
    }

    /// Iterable produced by this expression results in empty.
    // pub(crate) fn is_iterable_empty(&self) -> bool
    internal fun isIterableEmpty(): Boolean = when (this) {
        is ListExpr -> elements.isEmpty()
        is TupleExpr -> elements.isEmpty()
        is DictExpr -> entries.isEmpty()
        is ValueExpr -> value.isBuiltin() && value.toValue().length().getOrNull() == 0
        else -> false
    }

    /// Result of this expression is definitely `bool`.
    // fn is_definitely_bool(&self) -> bool
    private fun isDefinitelyBool(): Boolean = when (this) {
        is ValueExpr -> value.unpackBool() != null
        is Builtin1Expr -> op is Builtin1.Not || op is Builtin1.TypeIs
        is Builtin2Expr -> op == Builtin2.In || op == Builtin2.Equals
        else -> false
    }

    /// This expression is definitely infallible and has no effects.
    // pub(crate) fn is_pure_infallible(&self) -> bool
    internal fun isPureInfallible(): Boolean = when (this) {
        is ValueExpr -> true
        is ListExpr -> elements.all { it.node.isPureInfallible() }
        is TupleExpr -> elements.all { it.node.isPureInfallible() }
        is DictExpr -> entries.isEmpty()
        is Builtin1Expr -> (op is Builtin1.Not || op is Builtin1.TypeIs) && expr.node.isPureInfallible()
        is Seq -> first.node.isPureInfallible() && second.node.isPureInfallible()
        is LogicalBinOp -> lhs.node.isPureInfallible() && rhs.node.isPureInfallible()
        is If -> cond.node.isPureInfallible() && then.node.isPureInfallible() && elseExpr.node.isPureInfallible()
        is Call -> call.node.isPureInfallible()
        else -> false
    }

    /// If this expression is pure, infallible, and known to produce a value,
    /// return truth of that value.
    // pub(crate) fn is_pure_infallible_to_bool(&self) -> Option<bool>
    internal fun isPureInfallibleToBool(): Boolean? = when (this) {
        is ValueExpr -> value.toValue().toBool()
        is ListExpr -> if (elements.all { it.node.isPureInfallible() }) elements.isNotEmpty() else null
        is TupleExpr -> if (elements.all { it.node.isPureInfallible() }) elements.isNotEmpty() else null
        is DictExpr -> if (entries.isEmpty()) false else null
        is Builtin1Expr -> if (op is Builtin1.Not) expr.node.isPureInfallibleToBool()?.let { !it } else null
        is LogicalBinOp -> {
            val x = lhs.node.isPureInfallibleToBool()
            val y = rhs.node.isPureInfallibleToBool()
            when {
                op == ExprLogicalBinOp.And && x == true -> y
                op == ExprLogicalBinOp.Or && x == false -> y
                op == ExprLogicalBinOp.And && x == false -> false
                op == ExprLogicalBinOp.Or && x == true -> true
                else -> null
            }
        }
        else -> null
    }

    /// This expression is local slot.
    // pub(crate) fn as_local_non_captured(&self) -> Option<LocalSlotId>
    internal fun asLocalNonCaptured(): LocalSlotId? = when (this) {
        is Local -> slot
        else -> null
    }

    companion object {
        // Static helper methods for optimization (see Rust source for full implementations).

        // pub(crate) fn bin_op(bin_op: Builtin2, l: IrSpanned<ExprCompiled>, r: IrSpanned<ExprCompiled>, ctx: &mut OptCtx) -> ExprCompiled
        internal fun binOp(binOp: Builtin2, l: IrSpanned<ExprCompiled>, r: IrSpanned<ExprCompiled>, ctx: OptCtx): ExprCompiled {
            return Builtin2Expr(binOp, l, r)
        }

        // pub(crate) fn if_expr(cond: .., t: .., f: ..) -> IrSpanned<ExprCompiled>
        internal fun ifExpr(cond: IrSpanned<ExprCompiled>, t: IrSpanned<ExprCompiled>, f: IrSpanned<ExprCompiled>): IrSpanned<ExprCompiled> {
            val span = cond.span.merge(t.span).merge(f.span)
            return IrSpanned(If(cond, t, f), span)
        }

        // pub(crate) fn un_op(span: FrameSpan, op: &Builtin1, expr: IrSpanned<ExprCompiled>, ctx: &mut OptCtx) -> ExprCompiled
        internal fun unOp(span: FrameSpan, op: Builtin1, expr: IrSpanned<ExprCompiled>, ctx: OptCtx): ExprCompiled {
            return Builtin1Expr(op, expr)
        }

        // pub(crate) fn not(span: FrameSpan, expr: IrSpanned<ExprCompiled>) -> IrSpanned<ExprCompiled>
        internal fun not(span: FrameSpan, expr: IrSpanned<ExprCompiled>): IrSpanned<ExprCompiled> {
            return IrSpanned(Builtin1Expr(Builtin1.Not, expr), span)
        }

        // pub(crate) fn seq(x: IrSpanned<ExprCompiled>, y: IrSpanned<ExprCompiled>) -> IrSpanned<ExprCompiled>
        internal fun seq(x: IrSpanned<ExprCompiled>, y: IrSpanned<ExprCompiled>): IrSpanned<ExprCompiled> {
            val span = x.span.merge(y.span)
            return IrSpanned(Seq(x, y), span)
        }

        // Internal optimization helpers (full bodies in Rust source).
        internal fun formatOne(before: FrozenStringValue, v: FrozenValue, after: FrozenStringValue, ctx: OptCtx): Value? = null
        internal fun percentSOne(before: FrozenStringValue, v: FrozenValue, after: FrozenStringValue, ctx: OptCtx): Value? = null
        internal fun compileTimeGetattr(v: FrozenValue, field: Symbol, ctx: OptCtx): FrozenValue? = null
    }
}

// impl Compiler -- expression compilation methods

/// Compile an expression.
// pub(crate) fn expr(&mut self, expr: &CstExpr) -> Result<IrSpanned<ExprCompiled>, CompilerInternalError>
internal fun Compiler.expr(expr: CstExpr): Result<IrSpanned<ExprCompiled>> {
    // Full compilation logic delegates to ExprP pattern match (see Rust source, ~400 lines).
    // This is the main entry point for compiling all expression forms.
    throw NotImplementedError("expr compilation")
}

/// Like `expr` but returns an expression optimized assuming
/// only the truth of the result is needed.
// pub(crate) fn expr_truth(&mut self, expr: &CstExpr) -> Result<IrSpanned<ExprCompiledBool>, CompilerInternalError>
internal fun Compiler.exprTruth(expr: CstExpr): Result<IrSpanned<ExprCompiledBool>> {
    return this.expr(expr).map { ExprCompiledBool.new(it) }
}

/// Compile a list of expressions.
// pub(crate) fn exprs(&mut self, exprs: &[CstExpr]) -> Result<Vec<IrSpanned<ExprCompiled>>, CompilerInternalError>
internal fun Compiler.exprs(exprs: List<CstExpr>): Result<List<IrSpanned<ExprCompiled>>> {
    val results = mutableListOf<IrSpanned<ExprCompiled>>()
    for (e in exprs) {
        val result = this.expr(e).getOrElse { return Result.failure(it) }
        results.add(result)
    }
    return Result.success(results)
}
