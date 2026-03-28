// port-lint: source src/eval/bc/compiler/assign_modify.rs
package io.github.kotlinmania.starlark_kotlin.eval.bc.compiler

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

//! Write operators like `+=`.

import io.github.kotlinmania.starlark_kotlin.collections.symbol.Symbol
import io.github.kotlinmania.starlark_kotlin.eval.bc.BcSlotIn
import io.github.kotlinmania.starlark_kotlin.eval.bc.BcSlotOut
import io.github.kotlinmania.starlark_kotlin.eval.bc.BcSlotsN
import io.github.kotlinmania.starlark_kotlin.eval.bc.BcWriter
import io.github.kotlinmania.starlark_kotlin.eval.compiler.ExprCompiled
import io.github.kotlinmania.starlark_kotlin.eval.compiler.AssignModifyLhs
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AssignOp
import io.github.kotlinmania.starlark_kotlin.eval.compiler.IrSpanned
import io.github.kotlinmania.starlark_kotlin.eval.runtime.FrameSpan

// trait AssignOnWriteBc
// impl AssignOnWriteBc for AssignOp
// Extension function on AssignOp
private fun AssignOp.writeBc(
    v0: BcSlotIn,
    v1: BcSlotIn,
    target: BcSlotOut,
    span: FrameSpan,
    bc: BcWriter,
) {
    val arg = Triple(v0, v1, target)
    when (this) {
        AssignOp.Add -> bc.writeInstr("InstrAddAssign", span, arg)
        AssignOp.Subtract -> bc.writeInstr("InstrSub", span, arg)
        AssignOp.Multiply -> bc.writeInstr("InstrMultiply", span, arg)
        AssignOp.Divide -> bc.writeInstr("InstrDivide", span, arg)
        AssignOp.FloorDivide -> bc.writeInstr("InstrFloorDivide", span, arg)
        AssignOp.Percent -> bc.writeInstr("InstrPercent", span, arg)
        AssignOp.BitAnd -> bc.writeInstr("InstrBitAnd", span, arg)
        AssignOp.BitOr -> bc.writeInstr("InstrBitOrAssign", span, arg)
        AssignOp.BitXor -> bc.writeInstr("InstrBitXor", span, arg)
        AssignOp.LeftShift -> bc.writeInstr("InstrLeftShift", span, arg)
        AssignOp.RightShift -> bc.writeInstr("InstrRightShift", span, arg)
    }
}

// impl AssignModifyLhs

/// After evaluation of `x[y] += ...`, variables `x` and `y` are definitely assigned.
// pub(crate) fn mark_definitely_assigned_after(&self, bc: &mut BcWriter)
internal fun AssignModifyLhs.markDefinitelyAssignedAfter(bc: BcWriter) {
    when (this) {
        is AssignModifyLhs.Dot -> {
            expr.markDefinitelyAssignedAfter(bc)
        }
        is AssignModifyLhs.Array -> {
            expr.markDefinitelyAssignedAfter(bc)
            index.markDefinitelyAssignedAfter(bc)
        }
        is AssignModifyLhs.LocalCaptured -> {}
        is AssignModifyLhs.Local -> bc.markDefinitelyAssigned(slot.node)
        is AssignModifyLhs.Module -> {}
    }
}

// pub(crate) fn write_bc(&self, span: FrameSpan, op: AssignOp, rhs: &IrSpanned<ExprCompiled>, bc: &mut BcWriter)
internal fun AssignModifyLhs.writeBc(
    span: FrameSpan,
    op: AssignOp,
    rhs: IrSpanned<ExprCompiled>,
    bc: BcWriter,
) {
    when (this) {
        is AssignModifyLhs.Dot -> {
            expr.writeBcCb(bc) { objectSlot, bc ->
                bc.allocSlotsC(2) { lhsRhs: BcSlotsN, bc ->
                    val field = Symbol.new(name)
                    bc.writeInstr("InstrObjectField", 
                        span,
                        Triple(objectSlot, field, lhsRhs.get(0).toOut()),
                    )
                    rhs.writeBc(lhsRhs.get(1).toOut(), bc)
                    op.writeBc(
                        lhsRhs.get(0).toIn(),
                        lhsRhs.get(1).toIn(),
                        lhsRhs.get(1).toOut(),
                        span,
                        bc,
                    )
                    bc.writeInstr("InstrSetObjectField", 
                        span,
                        Triple(lhsRhs.get(1).toIn(), objectSlot, field),
                    )
                }
            }
        }
        is AssignModifyLhs.Array -> {
            writeNExprs(listOf(expr, index), bc) { slots, bc ->
                val (arraySlot, indexSlot) = slots
                bc.allocSlotsC(2) { tempSlots: BcSlotsN, bc ->
                    val tempSlot = tempSlots.get(0)
                    val rhsSlot = tempSlots.get(1)

                    bc.writeInstr("InstrArrayIndex", span, Triple(arraySlot, indexSlot, tempSlot.toOut()))
                    rhs.writeBc(rhsSlot.toOut(), bc)
                    op.writeBc(
                        tempSlot.toIn(),
                        rhsSlot.toIn(),
                        tempSlot.toOut(),
                        span,
                        bc,
                    )
                    bc.writeInstr("InstrArrayIndexSet", 
                        span,
                        Triple(arraySlot, indexSlot, tempSlot.toIn()),
                    )
                }
            }
        }
        is AssignModifyLhs.Local -> bc.allocSlotsC(2) { lhsRhs: BcSlotsN, bc ->
            val slot = this.slot.node
            bc.writeLoadLocal(span, slot, lhsRhs.get(0).toOut())
            rhs.writeBc(lhsRhs.get(1).toOut(), bc)

            op.writeBc(
                lhsRhs.get(0).toIn(),
                lhsRhs.get(1).toIn(),
                lhsRhs.get(1).toOut(),
                span,
                bc,
            )
            bc.writeMov(span, lhsRhs.get(1).toIn(), slot.toBcSlot().toOut())
        }
        is AssignModifyLhs.LocalCaptured -> bc.allocSlotsC(2) { lhsRhs: BcSlotsN, bc ->
            val slot = this.slot.node
            bc.writeLoadLocalCaptured(span, slot, lhsRhs.get(0).toOut())
            rhs.writeBc(lhsRhs.get(1).toOut(), bc)

            op.writeBc(
                lhsRhs.get(0).toIn(),
                lhsRhs.get(1).toIn(),
                lhsRhs.get(1).toOut(),
                span,
                bc,
            )
            bc.writeStoreLocalCaptured(span, lhsRhs.get(1).toIn(), slot)
        }
        is AssignModifyLhs.Module -> bc.allocSlotsC(2) { lhsRhs: BcSlotsN, bc ->
            val slot = this.slot.node
            bc.writeInstr("InstrLoadModule", span, Pair(slot, lhsRhs.get(0).toOut()))
            rhs.writeBc(lhsRhs.get(1).toOut(), bc)
            op.writeBc(
                lhsRhs.get(0).toIn(),
                lhsRhs.get(1).toIn(),
                lhsRhs.get(1).toOut(),
                span,
                bc,
            )
            bc.writeInstr("InstrStoreModule", span, Pair(lhsRhs.get(1).toIn(), slot))
        }
    }
}
