// port-lint: source src/eval/bc/compiler/assign.rs
package io.github.kotlinmania.starlark_kotlin.eval.bc.compiler.assign

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

/// Compile assignment lhs.

import io.github.kotlinmania.starlark_kotlin.collections.symbol.Symbol
import io.github.kotlinmania.starlark_kotlin.eval.bc.writer.BcSlotIn
import io.github.kotlinmania.starlark_kotlin.eval.bc.writer.BcSlotOut
import io.github.kotlinmania.starlark_kotlin.eval.bc.writer.BcWriter
import io.github.kotlinmania.starlark_kotlin.eval.compiler.stmt.AssignCompiledValue
import io.github.kotlinmania.starlark_kotlin.eval.compiler.args.IrSpanned
import io.github.kotlinmania.starlark_kotlin.values.layout.avalue.size
import io.github.kotlinmania.starlark_kotlin.eval.bc.writer.toOut
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.allocAnySlice
import io.github.kotlinmania.starlark_kotlin.eval.bc.writer.writeInstr
import io.github.kotlinmania.starlark_kotlin.eval.bc.writer.toIn
import io.github.kotlinmania.starlark_kotlin.eval.bc.compiler.writeNExprs
import io.github.kotlinmania.starlark_kotlin.eval.bc.compiler.writeBcCb
import io.github.kotlinmania.starlark_kotlin.analysis.node
import io.github.kotlinmania.starlark_kotlin.syntax.ast.Expr
import io.github.kotlinmania.starlark_kotlin.analysis.span
import io.github.kotlinmania.starlark_kotlin.values.layout.size

// impl AssignCompiledValue
/// After evaluation of `(x, y[z]) = ...`, variables `x`, `y` and `z` are definitely assigned.
// pub(crate) fn mark_definitely_assigned_after(&self, bc: &mut BcWriter)
internal fun AssignCompiledValue.markDefinitelyAssignedAfter(bc: BcWriter) {
    when (this) {
        is AssignCompiledValue.Dot -> {
            obj.node.markDefinitelyAssignedAfter(bc)
        }
        is AssignCompiledValue.Module -> {}
        is AssignCompiledValue.Index -> {
            array.node.markDefinitelyAssignedAfter(bc)
            index.node.markDefinitelyAssignedAfter(bc)
        }
        is AssignCompiledValue.LocalCaptured -> {}
        is AssignCompiledValue.Local -> {
            bc.markDefinitelyAssigned(slot)
        }
        is AssignCompiledValue.Tuple -> {
            for (x in elements) {
                x.node.markDefinitelyAssignedAfter(bc)
            }
        }
    }
}

// impl IrSpanned<AssignCompiledValue>
// pub(crate) fn write_bc(&self, value: BcSlotIn, bc: &mut BcWriter)
internal fun IrSpanned<AssignCompiledValue>.writeBc(value: BcSlotIn, bc: BcWriter) {
    val span = this.span
    when (val n = this.node) {
        is AssignCompiledValue.Dot -> {
            n.obj.writeBcCb(bc) { objectSlot, bc2 ->
                val symbol = Symbol.new(n.field)
                bc2.writeInstr("SetObjectField", span, Triple(value, objectSlot, symbol))
            }
        }
        is AssignCompiledValue.Index -> {
            writeNExprs(listOf(n.array, n.index), bc) { slots, bc2 ->
                bc2.writeInstr("SetArrayIndex", span, Triple(value, slots[0], slots[1]))
            }
        }
        is AssignCompiledValue.Tuple -> {
            // All assignments are to local variables, e. g.
            // ```
            // (x, y, z) = ...
            // ```
            // so we can avoid using intermediate register.
            val allLocal = n.elements.mapNotNull { x ->
                x.node.asLocalNonCaptured()?.toBcSlot()?.toOut()
            }.takeIf { it.size == n.elements.size }

            if (allLocal != null) {
                val args = bc.heap.allocAnySlice(allLocal)
                bc.writeInstr("Unpack", span, Pair(value, args))
            } else {
                bc.allocSlots(n.elements.size) { slots, bc2 ->
                    val args: List<BcSlotOut> = slots.map { s -> s.toOut() }
                    val argsRef = bc2.heap.allocAnySlice(args)
                    bc2.writeInstr("Unpack", span, Pair(value, argsRef))

                    for ((x, slot) in n.elements.zip(slots)) {
                        IrSpanned(span = x.span, node = x.node).writeBc(slot.toIn(), bc2)
                    }
                }
            }
        }
        is AssignCompiledValue.Local -> {
            bc.writeMov(span, value, n.slot.toBcSlot().toOut())
        }
        is AssignCompiledValue.LocalCaptured -> {
            bc.writeStoreLocalCaptured(span, value, n.slot)
        }
        is AssignCompiledValue.Module -> {
            bc.writeInstr("StoreModuleAndExport", span, Triple(value, n.slot, n.name))
        }
    }
}
