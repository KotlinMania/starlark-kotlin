// port-lint: source src/eval/bc/compiler/def.rs
package io.github.kotlinmania.starlark.eval.bc.compiler.def

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

import io.github.kotlinmania.starlark.eval.bc.BcSlotOut
import io.github.kotlinmania.starlark.eval.bc.BcWriter
import io.github.kotlinmania.starlark.eval.bc.InstrDefData
import io.github.kotlinmania.starlark.eval.bc.compiler.writeBc
import io.github.kotlinmania.starlark.eval.compiler.DefCompiled
import io.github.kotlinmania.starlark.eval.compiler.ExprCompiled
import io.github.kotlinmania.starlark.eval.compiler.IrSpanned
import io.github.kotlinmania.starlark.eval.compiler.ParameterCompiled
import io.github.kotlinmania.starlark.eval.compiler.ParametersCompiled
import io.github.kotlinmania.starlark.eval.compiler.mapExpr
import io.github.kotlinmania.starlark.eval.runtime.FrameSpan

// impl DefCompiled

internal fun DefCompiled.markDefinitelyAssignedAfter(bc: BcWriter) {
    // Argument default values and types can be used
    // to mark variables definitely assigned.
    @Suppress("UNUSED_VARIABLE")
    val unused = bc
}

internal fun DefCompiled.writeBc(span: FrameSpan, target: BcSlotOut, bc: BcWriter) {
    val functionName = this.functionName
    val paramList = this.params.params
    val indices = this.params.indices

    val howManySlotsWeNeed = this.params.countExprs()

    bc.allocSlots(howManySlotsWeNeed, fun(slots, bc2) {
        val slotsIter = slots.iter().iterator()
        var valueCount = 0
        val mappedParams =
            paramList.map { p: IrSpanned<ParameterCompiled<IrSpanned<ExprCompiled>>> ->
                p.map { pc: ParameterCompiled<IrSpanned<ExprCompiled>> ->
                    pc.mapExpr { e: IrSpanned<ExprCompiled> ->
                        e.writeBc(slotsIter.next().toOut(), bc2)
                        val idx = valueCount
                        valueCount += 1
                        idx
                    }
                }
            }

        val compiledParams =
            ParametersCompiled(
                params = mappedParams,
                indices = indices,
            )
        val instrDefData =
            InstrDefData(
                functionName = functionName,
                params = compiledParams,
                returnType = this.returnType,
                info = this.info,
            )

        check(!slotsIter.hasNext())

        bc2.writeInstr("InstrDef", span, Triple(slots.toIn(), instrDefData, target))
    })
}
