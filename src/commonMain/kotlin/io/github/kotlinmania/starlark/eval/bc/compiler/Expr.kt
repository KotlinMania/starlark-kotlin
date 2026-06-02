// port-lint: source src/eval/bc/compiler/expr.rs
package io.github.kotlinmania.starlark.eval.bc.compiler

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

/** Compile expressions. */

import io.github.kotlinmania.starlark.collections.Hashed
import io.github.kotlinmania.starlark.collections.SmallMap
import io.github.kotlinmania.starlark.eval.bc.ArrayIndex2Arg
import io.github.kotlinmania.starlark.eval.bc.BcInstrSlowArg
import io.github.kotlinmania.starlark.eval.bc.BcSlotIn
import io.github.kotlinmania.starlark.eval.bc.BcSlotInRange
import io.github.kotlinmania.starlark.eval.bc.BcSlotOut
import io.github.kotlinmania.starlark.eval.bc.BcWriter
import io.github.kotlinmania.starlark.eval.bc.SliceArg
import io.github.kotlinmania.starlark.eval.bc.SlotRangeTargetArg
import io.github.kotlinmania.starlark.eval.compiler.Builtin1
import io.github.kotlinmania.starlark.eval.compiler.Builtin2
import io.github.kotlinmania.starlark.eval.compiler.CompareOp
import io.github.kotlinmania.starlark.eval.compiler.ExprCompiled
import io.github.kotlinmania.starlark.eval.compiler.ExprLogicalBinOp
import io.github.kotlinmania.starlark.eval.compiler.IrSpanned
import io.github.kotlinmania.starlark.eval.compiler.MaybeNot
import io.github.kotlinmania.starlark.eval.runtime.FrameSpan
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.FrozenValueNotSpecial
import io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark.eval.bc.compiler.compr.markDefinitelyAssignedAfter as markDefinitelyAssignedAfterCompr
import io.github.kotlinmania.starlark.eval.bc.compiler.compr.writeBc as comprWriteBc
import io.github.kotlinmania.starlark.eval.bc.compiler.def.markDefinitelyAssignedAfter as markDefinitelyAssignedAfterDef
import io.github.kotlinmania.starlark.eval.bc.compiler.def.writeBc as defWriteBc

/** Try extract consecutive definitely initialized locals from expressions. */
private fun trySlotRange(
    exprs: Iterable<IrSpanned<ExprCompiled>>,
    bc: BcWriter,
): BcSlotInRange? {
    val range = BcSlotInRange.default()
    for (expr in exprs) {
        val local = expr.node.asLocalNonCaptured() ?: return null
        val slot = bc.tryDefinitelyAssigned(local) ?: return null
        if (!range.tryPush(slot)) {
            return null
        }
    }
    return range
}

/** Compile several expressions into consecutive registers. */
internal fun writeExprs(
    exprs: Iterable<IrSpanned<ExprCompiled>>,
    bc: BcWriter,
    k: (BcSlotInRange, BcWriter) -> Unit,
) {
    val exprList = exprs.toList()

    val slots = trySlotRange(exprList, bc)
    if (slots != null) {
        k(slots, bc)
    } else {
        bc.allocSlotsForExprs(
            exprList,
            { slot, expr, bc2 -> expr.writeBc(slot.toOut(), bc2) },
            k,
        )
    }
}

internal fun writeExprOpt(
    expr: IrSpanned<ExprCompiled>?,
    bc: BcWriter,
    k: (BcSlotIn?, BcWriter) -> Unit,
) {
    if (expr != null) {
        expr.writeBcCb(bc) { slot, bc2 -> k(slot, bc2) }
    } else {
        k(null, bc)
    }
}

// pub(crate) fn write_n_exprs<const N: usize>(exprs, bc, k)
internal fun writeNExprs(
    exprs: List<IrSpanned<ExprCompiled>>,
    bc: BcWriter,
    k: (List<BcSlotIn>, BcWriter) -> Unit,
) {
    fun help(
        filled: MutableList<BcSlotIn>,
        remExprs: List<IrSpanned<ExprCompiled>>,
        bc: BcWriter,
        k: (List<BcSlotIn>, BcWriter) -> Unit,
    ) {
        if (remExprs.isEmpty()) {
            k(filled, bc)
        } else {
            val first = remExprs.first()
            val rem = remExprs.drop(1)
            first.writeBcCb(bc) { slot, bc2 ->
                filled.add(slot)
                help(filled, rem, bc2, k)
            }
        }
    }

    help(mutableListOf(), exprs, bc, k)
}

// impl ExprCompiled { pub(crate) fn mark_definitely_assigned_after(&self, bc) }
internal fun ExprCompiled.markDefinitelyAssignedAfter(bc: BcWriter) {
    when (this) {
        is ExprCompiled.ValueExpr -> {}
        is ExprCompiled.Local -> bc.markDefinitelyAssigned(slot)
        is ExprCompiled.LocalCaptured -> {}
        is ExprCompiled.Module -> {}
        is ExprCompiled.TupleExpr -> {
            for (x in elements) {
                x.node.markDefinitelyAssignedAfter(bc)
            }
        }
        is ExprCompiled.ListExpr -> {
            for (x in elements) {
                x.node.markDefinitelyAssignedAfter(bc)
            }
        }
        is ExprCompiled.DictExpr -> {
            for ((k, v) in entries) {
                k.node.markDefinitelyAssignedAfter(bc)
                v.node.markDefinitelyAssignedAfter(bc)
            }
        }
        is ExprCompiled.Compr -> compr.markDefinitelyAssignedAfterCompr(bc)
        is ExprCompiled.If -> {
            // Condition is executed unconditionally, so we use it to mark definitely assigned.
            // But we don't know which of the branches will be executed.
            cond.node.markDefinitelyAssignedAfter(bc)
        }
        is ExprCompiled.Slice -> {
            obj.node.markDefinitelyAssignedAfter(bc)
            start?.node?.markDefinitelyAssignedAfter(bc)
            stop?.node?.markDefinitelyAssignedAfter(bc)
            step?.node?.markDefinitelyAssignedAfter(bc)
        }
        is ExprCompiled.Builtin1Expr -> {
            expr.node.markDefinitelyAssignedAfter(bc)
        }
        is ExprCompiled.LogicalBinOp -> {
            // `lhs` is executed unconditionally, but `rhs` is not,
            // so we mark only `lhs` as definitely assigned.
            lhs.node.markDefinitelyAssignedAfter(bc)
        }
        is ExprCompiled.Seq -> {
            first.node.markDefinitelyAssignedAfter(bc)
            second.node.markDefinitelyAssignedAfter(bc)
        }
        is ExprCompiled.Builtin2Expr -> {
            lhs.node.markDefinitelyAssignedAfter(bc)
            rhs.node.markDefinitelyAssignedAfter(bc)
        }
        is ExprCompiled.Index2 -> {
            obj.node.markDefinitelyAssignedAfter(bc)
            index0.node.markDefinitelyAssignedAfter(bc)
            index1.node.markDefinitelyAssignedAfter(bc)
        }
        is ExprCompiled.Call -> call.node.markDefinitelyAssignedAfterCall(bc)
        is ExprCompiled.Def -> def.markDefinitelyAssignedAfterDef(bc)
    }
}

// Helper: IrSpanned<ExprCompiled>::mark_definitely_assigned_after
internal fun IrSpanned<ExprCompiled>.markDefinitelyAssignedAfter(bc: BcWriter) {
    this.node.markDefinitelyAssignedAfter(bc)
}

// fn try_dict_of_consts(xs) -> Option<SmallMap<FrozenValue, FrozenValue>>
private fun tryDictOfConsts(
    xs: List<Pair<IrSpanned<ExprCompiled>, IrSpanned<ExprCompiled>>>,
): SmallMap<FrozenValue, FrozenValue>? {
    val res = SmallMap.new<FrozenValue, FrozenValue>()
    for ((k, v) in xs) {
        val kVal = k.node.asValue() ?: return null
        val kHash = kVal.toValue().getHash().getOrNull() ?: return null
        val kHashed = Hashed.newUnchecked(kHash, kVal)
        val vVal = v.node.asValue() ?: return null
        val prev = res.insertHashed(kHashed, vVal)
        if (prev != null) {
            // If there are duplicates, don't take the fast-literal
            // path and go down the slow runtime path (which will raise the error).
            return null
        }
    }
    return res
}

// fn try_dict_const_keys(xs) -> Option<Box<[Hashed<FrozenValue>]>>
private fun tryDictConstKeys(
    xs: List<Pair<IrSpanned<ExprCompiled>, IrSpanned<ExprCompiled>>>,
): List<Hashed<FrozenValue>>? {
    val keys = mutableListOf<Hashed<FrozenValue>>()
    val keysUnique = mutableSetOf<Hashed<FrozenValue>>()
    for ((k, _) in xs) {
        val kVal = k.node.asValue() ?: return null
        val kHash = kVal.toValue().getHash().getOrNull() ?: return null
        val kHashed = Hashed.newUnchecked(kHash, kVal)
        keys.add(kHashed)
        val inserted = keysUnique.add(kHashed)
        if (!inserted) {
            // Otherwise fail at runtime.
            return null
        }
    }
    return keys
}

// fn write_dict(span, xs, target, bc)
private fun writeDict(
    span: FrameSpan,
    xs: List<Pair<IrSpanned<ExprCompiled>, IrSpanned<ExprCompiled>>>,
    target: BcSlotOut,
    bc: BcWriter,
) {
    if (xs.isEmpty()) {
        bc.writeInstr("InstrDictNew", span, target)
    } else {
        val dictConsts = tryDictOfConsts(xs)
        if (dictConsts != null) {
            bc.writeInstr("InstrDictOfConsts", span, dictConsts to target)
        } else {
            val keys = tryDictConstKeys(xs)
            if (keys != null) {
                check(keys.size == xs.size)
                writeExprs(xs.map { (_, v) -> v }, bc) { values, bc2 ->
                    check(values.len().toInt() == keys.size)
                    bc2.writeInstr(
                        "InstrDictConstKeys",
                        span,
                        Triple(keys, values.toRangeFrom(), target),
                    )
                }
            } else {
                val keySpans = xs.map { (k, _) -> k.span }.toMutableList()
                writeExprs(xs.flatMap { (k, v) -> listOf(k, v) }, bc) { kvs, bc2 ->
                    bc2.writeInstrExplicit(
                        "InstrDictNPop",
                        BcInstrSlowArg(span, keySpans),
                        SlotRangeTargetArg(kvs, target),
                    )
                }
            }
        }
    }
}

// fn write_not(expr, target, bc)
private fun writeNot(
    expr: IrSpanned<ExprCompiled>,
    target: BcSlotOut,
    bc: BcWriter,
) {
    expr.writeBcCb(bc) { slot, bc2 ->
        bc2.writeInstr("InstrNot", expr.span, slot to target)
    }
}

// fn write_equals_const(span, a, b, target, bc)
private fun writeEqualsConst(
    span: FrameSpan,
    a: IrSpanned<ExprCompiled>,
    b: FrozenValue,
    target: BcSlotOut,
    bc: BcWriter,
) {
    a.writeBcCb(bc) { aSlot, bc2 ->
        val intVal = b.toValue().unpackIntValue()
        if (intVal != null) {
            bc2.writeInstr("InstrEqInt", span, Triple(aSlot, intVal, target))
        } else if (b.eqIsPtrEq()) {
            bc2.writeInstr("InstrEqPtr", span, Triple(aSlot, b, target))
        } else {
            val strVal = FrozenStringValue.new(b)
            if (strVal != null) {
                bc2.writeInstr("InstrEqStr", span, Triple(aSlot, strVal, target))
            } else {
                val notSpecial = FrozenValueNotSpecial.new(b)
                if (notSpecial != null) {
                    bc2.writeInstr("InstrEqConst", span, Triple(aSlot, notSpecial, target))
                } else {
                    error("FrozenValue must be either i32, str or not-special")
                }
            }
        }
    }
}

// fn write_equals(span, a, b, target, bc)
private fun writeEquals(
    span: FrameSpan,
    a: IrSpanned<ExprCompiled>,
    b: IrSpanned<ExprCompiled>,
    target: BcSlotOut,
    bc: BcWriter,
) {
    val aConst = a.node.asValue()
    val bConst = b.node.asValue()
    if (aConst != null) {
        writeEqualsConst(span, b, aConst, target, bc)
    } else if (bConst != null) {
        writeEqualsConst(span, a, bConst, target, bc)
    } else {
        writeNExprs(listOf(a, b), bc) { slots, bc2 ->
            bc2.writeInstr("InstrEq", span, Triple(slots[0], slots[1], target))
        }
    }
}

// impl IrSpanned<ExprCompiled> { pub(crate) fn write_bc(&self, target, bc) }
internal fun IrSpanned<ExprCompiled>.writeBc(target: BcSlotOut, bc: BcWriter) {
    val span = this.span
    when (val expr = this.node) {
        is ExprCompiled.ValueExpr -> {
            bc.writeConst(span, expr.value, target)
        }
        is ExprCompiled.Local -> {
            bc.writeLoadLocal(span, expr.slot, target)
        }
        is ExprCompiled.LocalCaptured -> {
            bc.writeLoadLocalCaptured(span, expr.slot, target)
        }
        is ExprCompiled.Module -> {
            bc.writeInstr("InstrLoadModule", span, expr.slot to target)
        }
        is ExprCompiled.TupleExpr -> {
            writeExprs(expr.elements, bc) { xs, bc2 ->
                bc2.writeInstr("InstrTupleNPop", span, SlotRangeTargetArg(xs, target))
            }
        }
        is ExprCompiled.ListExpr -> {
            if (expr.elements.isEmpty()) {
                bc.writeInstr("InstrListNew", span, target)
            } else if (expr.elements.all { it.node.asValue() != null }) {
                val content = expr.elements.map { it.node.asValue()!! }
                bc.writeInstr("InstrListOfConsts", span, content to target)
            } else {
                writeExprs(expr.elements, bc) { xs, bc2 ->
                    bc2.writeInstr("InstrListNPop", span, SlotRangeTargetArg(xs, target))
                }
            }
        }
        is ExprCompiled.DictExpr -> writeDict(span, expr.entries, target, bc)
        is ExprCompiled.Compr -> expr.compr.comprWriteBc(span, target, bc)
        is ExprCompiled.Slice -> {
            expr.obj.writeBcCb(bc) { l, bc2 ->
                writeExprOpt(expr.start, bc2) { start, bc3 ->
                    writeExprOpt(expr.stop, bc3) { stop, bc4 ->
                        writeExprOpt(expr.step, bc4) { step, bc5 ->
                            bc5.writeInstr("InstrSlice", span, SliceArg(l, start, stop, step, target))
                        }
                    }
                }
            }
        }
        is ExprCompiled.Builtin1Expr -> {
            if (expr.op is Builtin1.Not) {
                writeNot(expr.expr, target, bc)
            } else {
                expr.expr.writeBcCb(bc) { slot, bc2 ->
                    val arg = slot to target
                    when (expr.op) {
                        is Builtin1.Not -> error("handled above")
                        is Builtin1.Minus -> bc2.writeInstr("InstrMinus", span, arg)
                        is Builtin1.Plus -> bc2.writeInstr("InstrPlus", span, arg)
                        is Builtin1.BitNot -> bc2.writeInstr("InstrBitNot", span, arg)
                        is Builtin1.TypeIs -> {
                            bc2.writeInstr("InstrTypeIs", span, Triple(slot, expr.op.type, target))
                        }
                        is Builtin1.PercentSOne -> {
                            bc2.writeInstr(
                                "InstrPercentSOne",
                                span,
                                listOf(expr.op.before, slot, expr.op.after, target),
                            )
                        }
                        is Builtin1.FormatOne -> {
                            bc2.writeInstr(
                                "InstrFormatOne",
                                span,
                                listOf(expr.op.before, slot, expr.op.after, target),
                            )
                        }
                        is Builtin1.Dot -> {
                            bc2.writeInstr(
                                "InstrObjectField",
                                span,
                                Triple(slot, expr.op.field, target),
                            )
                        }
                    }
                }
            }
        }
        is ExprCompiled.If -> {
            writeIfElse(
                expr.cond,
                { bc2 -> expr.thenBranch.writeBc(target, bc2) },
                { bc2 -> expr.elseBranch.writeBc(target, bc2) },
                bc,
            )
        }
        is ExprCompiled.LogicalBinOp -> {
            expr.lhs.writeBcCb(bc) { lSlot, bc2 ->
                val maybeNot =
                    when (expr.op) {
                        ExprLogicalBinOp.And -> MaybeNot.Id
                        ExprLogicalBinOp.Or -> MaybeNot.Not
                    }
                bc2.writeIfElse(
                    lSlot,
                    maybeNot,
                    expr.lhs.span,
                    { bc3 -> expr.rhs.writeBc(target, bc3) },
                    { bc3 -> bc3.writeMov(span, lSlot, target) },
                )
            }
        }
        is ExprCompiled.Seq -> {
            expr.first.writeBcForEffect(bc)
            expr.second.writeBc(target, bc)
        }
        is ExprCompiled.Builtin2Expr -> {
            if (expr.op == Builtin2.Equals) {
                writeEquals(span, expr.lhs, expr.rhs, target, bc)
            } else {
                writeNExprs(listOf(expr.lhs, expr.rhs), bc) { slots, bc2 ->
                    val l = slots[0]
                    val r = slots[1]
                    val arg = Triple(l, r, target)
                    when (expr.op) {
                        Builtin2.Equals -> error("handled above")
                        Builtin2.In -> bc2.writeInstr("InstrIn", span, arg)
                        Builtin2.Sub -> bc2.writeInstr("InstrSub", span, arg)
                        Builtin2.Add -> bc2.writeInstr("InstrAdd", span, arg)
                        Builtin2.Multiply -> bc2.writeInstr("InstrMultiply", span, arg)
                        Builtin2.Divide -> bc2.writeInstr("InstrDivide", span, arg)
                        Builtin2.FloorDivide -> bc2.writeInstr("InstrFloorDivide", span, arg)
                        Builtin2.Percent -> bc2.writeInstr("InstrPercent", span, arg)
                        Builtin2.BitAnd -> bc2.writeInstr("InstrBitAnd", span, arg)
                        Builtin2.BitOr -> bc2.writeInstr("InstrBitOr", span, arg)
                        Builtin2.BitXor -> bc2.writeInstr("InstrBitXor", span, arg)
                        Builtin2.LeftShift -> bc2.writeInstr("InstrLeftShift", span, arg)
                        Builtin2.RightShift -> bc2.writeInstr("InstrRightShift", span, arg)
                        Builtin2.ArrayIndex -> bc2.writeInstr("InstrArrayIndex", span, arg)
                        is Builtin2.Compare ->
                            when (expr.op.op) {
                                CompareOp.Less -> bc2.writeInstr("InstrLess", span, arg)
                                CompareOp.Greater -> bc2.writeInstr("InstrGreater", span, arg)
                                CompareOp.LessOrEqual -> bc2.writeInstr("InstrLessOrEqual", span, arg)
                                CompareOp.GreaterOrEqual -> bc2.writeInstr("InstrGreaterOrEqual", span, arg)
                            }
                    }
                }
            }
        }
        is ExprCompiled.Index2 -> {
            writeNExprs(listOf(expr.obj, expr.index0, expr.index1), bc) { slots, bc2 ->
                bc2.writeInstr("InstrArrayIndex2", span, ArrayIndex2Arg(slots[0], slots[1], slots[2], target))
            }
        }
        is ExprCompiled.Call -> expr.call.writeBcCall(target, bc)
        is ExprCompiled.Def -> expr.def.defWriteBc(span, target, bc)
    }
}

/**
 * Allocate temporary slot, write expression into it,
 * and then consume the slot with the callback.
 */
// pub(crate) fn write_bc_cb(&self, bc, k) -> R
internal fun <R> IrSpanned<ExprCompiled>.writeBcCb(
    bc: BcWriter,
    k: (BcSlotIn, BcWriter) -> R,
): R {
    val local = this.node.asLocalNonCaptured()
    if (local != null) {
        // Local is known to be definitely assigned, so there's no need
        // to "load" it just to trigger check that it is assigned.
        val slot = bc.tryDefinitelyAssigned(local)
        if (slot != null) {
            return k(slot, bc)
        }
    }

    return bc.allocSlot { slot, bc2 ->
        this.writeBc(slot.toOut(), bc2)
        k(slot.toIn(), bc2)
    }
}

// pub(crate) fn write_bc_for_effect(&self, bc)
internal fun IrSpanned<ExprCompiled>.writeBcForEffect(bc: BcWriter) {
    this.writeBcCb(bc) { _, _ -> }
}
