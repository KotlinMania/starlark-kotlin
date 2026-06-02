// port-lint: source src/eval/bc/compiler/assign.rs
package io.github.kotlinmania.starlark.eval.bc.compiler.assign

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

/** Compile assignment lhs. */

import io.github.kotlinmania.starlark.collections.symbol.Symbol
import io.github.kotlinmania.starlark.eval.bc.BcSlotIn
import io.github.kotlinmania.starlark.eval.bc.BcSlotOut
import io.github.kotlinmania.starlark.eval.bc.BcWriter
import io.github.kotlinmania.starlark.eval.bc.compiler.writeNExprs
import io.github.kotlinmania.starlark.eval.bc.compiler.writeBcCb
import io.github.kotlinmania.starlark.eval.compiler.AssignCompiledValue
import io.github.kotlinmania.starlark.eval.compiler.IrSpanned
import io.github.kotlinmania.starlark.eval.compiler.asLocalNonCaptured
import io.github.kotlinmania.starlark.values.layout.avalues.allocAnySlice

// impl AssignCompiledValue

/**
 * After evaluation of `(x, y[z]) = ...`, variables `x`, `y` and `z` are definitely assigned.
 *
 * Marks all local variables referenced in the assignment target as definitely assigned
 * in the bytecode writer's tracking state. This allows downstream instructions to skip
 * "possibly uninitialized" checks for those variables.
 */
internal fun AssignCompiledValue.markDefinitelyAssignedAfter(bc: BcWriter) {
    when (this) {
        is AssignCompiledValue.Dot -> {
            obj.node.markDefinitelyAssignedAfter(bc)
            @Suppress("UNUSED_EXPRESSION")
            field
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

/**
 * Compile an assignment target to bytecode.
 *
 * Generates bytecode instructions that store [value] into the location
 * described by this assignment target. Handles dot access, indexing,
 * tuple unpacking, local variables, captured locals, and module slots.
 *
 * @param value the bytecode slot containing the value to assign
 * @param bc the bytecode writer to emit instructions into
 */
internal fun IrSpanned<AssignCompiledValue>.writeBc(value: BcSlotIn, bc: BcWriter) {
    val span = this.span
    when (val n = this.node) {
        is AssignCompiledValue.Dot -> {
            n.obj.writeBcCb(bc) { objectSlot, bc2 ->
                val symbol = Symbol.new(n.field)
                bc2.writeInstr("InstrSetObjectField", span, Triple(value, objectSlot, symbol))
            }
        }
        is AssignCompiledValue.Index -> {
            writeNExprs(listOf(n.array, n.index), bc) { slots, bc2 ->
                bc2.writeInstr("InstrSetArrayIndex", span, Triple(value, slots[0], slots[1]))
            }
        }
        is AssignCompiledValue.Tuple -> {
            // All assignments are to local variables, e.g.
            // ```
            // (x, y, z) = ...
            // ```
            // so we can avoid using intermediate register.
            val allLocal = n.elements.mapNotNull { x ->
                x.node.asLocalNonCaptured()?.toBcSlot()?.toOut()
            }.takeIf { it.size == n.elements.size }

            if (allLocal != null) {
                val args = bc.heap.allocAnySlice(allLocal)
                bc.writeInstr("InstrUnpack", span, Pair(value, args))
            } else {
                bc.allocSlots(n.elements.size) { slots, bc2 ->
                    val args: List<BcSlotOut> = slots.map { s -> s.toOut() }
                    val argsRef = bc2.heap.allocAnySlice(args)
                    bc2.writeInstr("InstrUnpack", span, Pair(value, argsRef))

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
            bc.writeInstr("InstrStoreModuleAndExport", span, Triple(value, n.slot, n.name))
        }
    }
}
