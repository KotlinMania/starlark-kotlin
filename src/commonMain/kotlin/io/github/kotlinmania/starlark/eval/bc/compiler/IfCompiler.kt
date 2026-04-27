// port-lint: source src/eval/bc/compiler/ifCompiler.rs
package io.github.kotlinmania.starlark.eval.bc.compiler

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

import io.github.kotlinmania.starlark.eval.bc.BcSlotIn
import io.github.kotlinmania.starlark.eval.bc.BcWriter
import io.github.kotlinmania.starlark.eval.compiler.Builtin1
import io.github.kotlinmania.starlark.eval.compiler.ExprCompiled
import io.github.kotlinmania.starlark.eval.compiler.ExprLogicalBinOp
import io.github.kotlinmania.starlark.eval.compiler.MaybeNot
import io.github.kotlinmania.starlark.eval.compiler.IrSpanned
import io.github.kotlinmania.starlark.eval.bc.PatchAddr

/** Common code for compiling if statements and if expressions. */
internal fun writeIfElse(
    c: IrSpanned<ExprCompiled>,
    t: (BcWriter) -> Unit,
    f: (BcWriter) -> Unit,
    bc: BcWriter,
) {
    writeIfElseImpl(c, MaybeNot.Id, t, f, bc)
}

/** Common code for compiling if statements and if conditions in comprehensions. */
internal fun writeIfThen(
    c: IrSpanned<ExprCompiled>,
    maybeNot: MaybeNot,
    t: (BcWriter) -> Unit,
    bc: BcWriter,
) {
    writeIfElseImpl(c, maybeNot, t, null, bc)
}

/** Common code for writing if-then or if-then-else expression or statement. */
private fun writeIfElseImpl(
    cond: IrSpanned<ExprCompiled>,
    maybeNot: MaybeNot,
    t: (BcWriter) -> Unit,
    f: ((BcWriter) -> Unit)?,
    bc: BcWriter,
) {
    val thenAddrs = mutableListOf<PatchAddr>()
    val elseAddrs = mutableListOf<PatchAddr>()

    writeCond(cond, maybeNot, thenAddrs, elseAddrs, bc)

    val definitelyAssigned = bc.saveDefinitelyAssigned()

    bc.patchAddrs(thenAddrs)
    t(bc)
    if (f != null) {
        val endAddr = bc.writeBr(cond.span)

        bc.restoreDefinitelyAssigned(definitelyAssigned.copy())

        bc.patchAddrs(elseAddrs)
        f(bc)

        bc.patchAddr(endAddr)
    } else {
        bc.patchAddrs(elseAddrs)
    }

    bc.restoreDefinitelyAssigned(definitelyAssigned)
}

/**
 * Write boolean binary condition.
 *
 * The condition is: `maybeNot(x binOp y)`.
 *
 * See `writeCond` for semantics of `t`, `f` parameters.
 */
private fun writeCondBinOp(
    x: IrSpanned<ExprCompiled>,
    y: IrSpanned<ExprCompiled>,
    binOp: ExprLogicalBinOp,
    maybeNot: MaybeNot,
    t: MutableList<PatchAddr>,
    f: MutableList<PatchAddr>,
    bc: BcWriter,
) {
    if ((binOp == ExprLogicalBinOp.And) == (maybeNot == MaybeNot.Id)) {
        // This branch handles either of expressions:
        // expression   | binOp | maybeNot
        // --------------+-------+----------
        // x and y      | and    | id
        // not (x or y) | or     | not

        val xSkip = mutableListOf<PatchAddr>()
        writeCond(x, maybeNot, xSkip, f, bc)
        bc.patchAddrs(xSkip)

        writeCond(y, maybeNot, t, f, bc)
    } else {
        // This branch handles either of expressions:
        // expression    | binOp | maybeNot
        // --------------+-----+--+----------
        // x or y        | or     | id
        // not (x and y) | and    | not

        val xSkip = mutableListOf<PatchAddr>()
        writeCond(x, maybeNot.negate(), xSkip, t, bc)
        bc.patchAddrs(xSkip)

        writeCond(y, maybeNot, t, f, bc)
    }
}

/**
 * Write if condition bytecode.
 *
 * The condition is `maybeNot(cond)`.
 *
 * This function assumes there are two address:
 * * address of then block
 * * address of else block
 *
 * Generated code will:
 * * jump to else address if condition is false
 * * jump to then address **or** fall through if condition is true
 *
 * This function will populate `t` and `f` with addresses of instructions
 * which jump to then or else block respectively. Caller needs to patch these.
 */
private fun writeCond(
    cond: IrSpanned<ExprCompiled>,
    maybeNot: MaybeNot,
    t: MutableList<PatchAddr>,
    f: MutableList<PatchAddr>,
    bc: BcWriter,
) {
    when (val node = cond.node) {
        is ExprCompiled.Builtin1Expr -> {
            if (node.op == Builtin1.Not) {
                writeCond(node.expr, maybeNot.negate(), t, f, bc)
                return
            }
            // Fall through to default case
            cond.writeBcCb(bc) { condSlot, bc ->
                val addr = when (maybeNot) {
                    MaybeNot.Id -> bc.writeIfNotBr(condSlot, cond.span)
                    MaybeNot.Not -> bc.writeIfBr(condSlot, cond.span)
                }
                f.add(addr)
            }
        }
        is ExprCompiled.LogicalBinOp -> {
            writeCondBinOp(node.lhs, node.rhs, node.op, maybeNot, t, f, bc)
        }
        else -> {
            cond.writeBcCb(bc) { condSlot, bc ->
                val addr = when (maybeNot) {
                    MaybeNot.Id -> bc.writeIfNotBr(condSlot, cond.span)
                    MaybeNot.Not -> bc.writeIfBr(condSlot, cond.span)
                }
                f.add(addr)
            }
        }
    }
}
