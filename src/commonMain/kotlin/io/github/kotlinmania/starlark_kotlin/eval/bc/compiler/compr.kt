// port-lint: source src/eval/bc/compiler/compr.rs
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

/// Compile comprehensions.

import io.github.kotlinmania.starlark_kotlin.eval.bc.compiler.expr.writeNExprs
import io.github.kotlinmania.starlark_kotlin.eval.bc.compiler.if_compiler.writeIfThen
import io.github.kotlinmania.starlark_kotlin.eval.bc.compiler.stmt.writeFor
import io.github.kotlinmania.starlark_kotlin.eval.bc.stack_ptr.BcSlotOut
import io.github.kotlinmania.starlark_kotlin.eval.bc.writer.BcWriter
import io.github.kotlinmania.starlark_kotlin.eval.compiler.compr.ClauseCompiled
import io.github.kotlinmania.starlark_kotlin.eval.compiler.compr.ComprCompiled
import io.github.kotlinmania.starlark_kotlin.eval.compiler.expr.MaybeNot
import io.github.kotlinmania.starlark_kotlin.eval.runtime.frame_span.FrameSpan

// impl ClauseCompiled
// fn write_bc(&self, bc: &mut BcWriter, rem: &[ClauseCompiled], term: impl FnOnce(&mut BcWriter))
private fun ClauseCompiled.writeBc(
    bc: BcWriter,
    rem: List<ClauseCompiled>,
    term: (BcWriter) -> Unit,
) {
    writeFor(over, variable, over.span, bc) { bc2 ->
        for (c in ifs) {
            writeIfThen(c, MaybeNot.Not, { bc3 -> bc3.writeContinue(c.span) }, bc2)
        }

        if (rem.isNotEmpty()) {
            // rem.split_last() -> (last, all_but_last)
            val next = rem.last()
            val rest = rem.dropLast(1)
            next.writeBc(bc2, rest, term)
        } else {
            term(bc2)
        }
    }
}

// impl ComprCompiled

/// After evaluation of comprehension like `[(x, z) for x in y for z in w]`,
/// we can mark `y` as definitely assigned.
// pub(crate) fn mark_definitely_assigned_after(&self, bc: &mut BcWriter)
internal fun ComprCompiled.markDefinitelyAssignedAfter(bc: BcWriter) {
    val clauses = this.clauses()
    // We know that first loop argument is executed, and we don't know anything else.
    clauses.splitLast().first.over.markDefinitelyAssignedAfter(bc)
}

// pub(crate) fn write_bc(&self, span: FrameSpan, target: BcSlotOut, bc: &mut BcWriter)
internal fun ComprCompiled.writeBc(span: FrameSpan, target: BcSlotOut, bc: BcWriter) {
    bc.allocSlot { temp, bc2 ->
        when (this) {
            is ComprCompiled.List -> {
                bc2.writeInstr("InstrListNew", span, temp.toOut())
                val (first, rem) = clauses.splitLast()
                first.writeBc(bc2, rem) { bc3 ->
                    x.writeBcCb(bc3) { exprSlot, bc4 ->
                        bc4.writeInstr(
                            "InstrComprListAppend",
                            x.span,
                            listOf(temp.toIn(), exprSlot),
                        )
                    }
                }
            }
            is ComprCompiled.Dict -> {
                val (k, v) = kv
                bc2.writeInstr("InstrDictNew", span, temp.toOut())
                val (first, rem) = clauses.splitLast()
                first.writeBc(bc2, rem) { bc3 ->
                    writeNExprs(listOf(k, v), bc3) { slots, bc4 ->
                        bc4.writeInstr(
                            "InstrComprDictInsert",
                            k.span,
                            listOf(temp.toIn(), slots[0], slots[1]),
                        )
                    }
                }
            }
        }
        bc2.writeMov(span, temp.toIn(), target)
    }
}
