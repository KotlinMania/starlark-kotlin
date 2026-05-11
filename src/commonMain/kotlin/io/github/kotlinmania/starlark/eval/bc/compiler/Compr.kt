// port-lint: source eval/bc/compiler/compr.rs
package io.github.kotlinmania.starlark.eval.bc.compiler.compr

/*
 * Copyright 2019 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not import this file except in compliance with the License.
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
 * Compile comprehensions.
 *
 * Handles bytecode generation for list and dict comprehensions,
 * including nested for-loops and if-clauses within comprehension expressions.
 */

import io.github.kotlinmania.starlark.eval.bc.compiler.markDefinitelyAssignedAfter
import io.github.kotlinmania.starlark.eval.bc.compiler.writeBcCb
import io.github.kotlinmania.starlark.eval.bc.compiler.writeFor
import io.github.kotlinmania.starlark.eval.bc.compiler.writeIfThen
import io.github.kotlinmania.starlark.eval.bc.compiler.writeNExprs
import io.github.kotlinmania.starlark.eval.bc.BcSlotOut
import io.github.kotlinmania.starlark.eval.bc.BcWriter
import io.github.kotlinmania.starlark.eval.runtime.FrameSpan
import io.github.kotlinmania.starlark.eval.compiler.ClauseCompiled
import io.github.kotlinmania.starlark.eval.compiler.ComprCompiled
import io.github.kotlinmania.starlark.eval.compiler.MaybeNot

/**
 * Compiles a single comprehension clause to bytecode.
 *
 * Generates the for-loop for this clause, applies any if-filters,
 * then either recurses into the remaining clauses or invokes the
 * terminal expression callback.
 *
 * @param bc the bytecode writer
 * @param rem the remaining clauses to process (in reverse order)
 * @param term callback to emit the comprehension body expression
 */
internal fun ClauseCompiled.writeBc(
    bc: BcWriter,
    rem: List<ClauseCompiled>,
    term: (BcWriter) -> Unit,
) {
    writeFor(over, variable, over.span, bc) { bc ->
        for (c in ifs) {
            writeIfThen(c, MaybeNot.Not, { bc -> bc.writeContinue(c.span) }, bc)
        }

        if (rem.isNotEmpty()) {
            val next = rem.last()
            val rest = rem.dropLast(1)
            next.writeBc(bc, rest, term)
        } else {
            term(bc)
        }
    }
}

/**
 * Marks variables that are definitely assigned after evaluation of this comprehension.
 *
 * After evaluation of a comprehension like `[(x, z) for x in y for z in w]`,
 * we can mark `y` as definitely assigned because the first loop argument
 * is always executed. We do not know anything about inner loop arguments
 * since they may not execute if an outer iterable is empty.
 *
 * @param bc the bytecode writer used to track definitely-assigned state
 */
internal fun ComprCompiled.markDefinitelyAssignedAfter(bc: BcWriter) {
    val clauses = this.clauses()
    // We know that first loop argument is executed, and we don't know anything else.
    clauses.splitLast().first.over.markDefinitelyAssignedAfter(bc)
}

/**
 * Compiles this comprehension expression to bytecode.
 *
 * Allocates a temporary slot to hold the accumulating list or dict,
 * emits the appropriate `New` instruction, then generates the nested
 * loop/filter structure that appends or inserts into the collection.
 * Finally, moves the result from the temporary slot to the target output slot.
 *
 * @param span the source span for this comprehension
 * @param target the output slot where the final collection value is stored
 * @param bc the bytecode writer
 */
internal fun ComprCompiled.writeBc(span: FrameSpan, target: BcSlotOut, bc: BcWriter) {
    bc.allocSlot { temp, bc ->
        when (this) {
            is ComprCompiled.List -> {
                bc.writeInstr("InstrListNew", span, temp.toOut())
                val (first, rem) = clauses.splitLast()
                first.writeBc(bc, rem) { bc ->
                    x.writeBcCb(bc) { exprSlot, bc ->
                        bc.writeInstr(
                            "InstrComprListAppend",
                            x.span,
                            listOf(temp.toIn(), exprSlot),
                        )
                    }
                }
            }
            is ComprCompiled.Dict -> {
                val (k, v) = kv
                bc.writeInstr("InstrDictNew", span, temp.toOut())
                val (first, rem) = clauses.splitLast()
                first.writeBc(bc, rem) { bc ->
                    writeNExprs(listOf(k, v), bc) { slots, bc ->
                        bc.writeInstr(
                            "InstrComprDictInsert",
                            k.span,
                            listOf(temp.toIn(), slots[0], slots[1]),
                        )
                    }
                }
            }
        }
        bc.writeMov(span, temp.toIn(), target)
    }
}
