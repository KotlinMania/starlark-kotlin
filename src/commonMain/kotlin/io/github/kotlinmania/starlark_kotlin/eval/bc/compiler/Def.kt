// port-lint: source src/eval/bc/compiler/def.rs
package io.github.kotlinmania.starlark_kotlin.eval.bc.compiler.def

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

/** Compile def. */

import io.github.kotlinmania.starlark_kotlin.eval.bc.instr_impl.InstrDefData
import io.github.kotlinmania.starlark_kotlin.eval.bc.stack_ptr.BcSlotOut
import io.github.kotlinmania.starlark_kotlin.eval.bc.writer.BcWriter
import io.github.kotlinmania.starlark_kotlin.eval.compiler.def.DefCompiled
import io.github.kotlinmania.starlark_kotlin.eval.compiler.def.ParametersCompiled
import io.github.kotlinmania.starlark_kotlin.eval.runtime.frame_span.FrameSpan

// impl DefCompiled

// pub(crate) fn mark_definitely_assigned_after(&self, bc: &mut BcWriter)
internal fun DefCompiled.markDefinitelyAssignedAfter(bc: BcWriter) {
    // Argument default values and types can be used
    // to mark variables definitely assigned.
    @Suppress("UNUSED_PARAMETER")
    val _ = bc
}

// pub(crate) fn write_bc(&self, span: FrameSpan, target: BcSlotOut, bc: &mut BcWriter)
internal fun DefCompiled.writeBc(span: FrameSpan, target: BcSlotOut, bc: BcWriter) {
    val functionName = this.functionName
    val (paramList, indices) = this.params

    val howManySlotsWeNeed = params.countExprs()

    bc.allocSlots(howManySlotsWeNeed) { slots, bcWriter ->
        val slotsIter = slots.iterator()
        var valueCount = 0
        val params = paramList.map { p ->
            p.map { inner ->
                inner.mapExpr { e ->
                    e.writeBc(slotsIter.next().toOut(), bcWriter)
                    valueCount += 1
                    valueCount - 1
                }
            }
        }

        val compiledParams = ParametersCompiled(
            params = params,
            indices = indices,
        )
        val instrDefData = InstrDefData(
            functionName = functionName,
            params = compiledParams,
            returnType = this.returnType,
            info = this.info,
        )

        check(!slotsIter.hasNext())

        bcWriter.writeInstr<InstrDefData>(span, Triple(slots.toIn(), instrDefData, target))
    }
}
