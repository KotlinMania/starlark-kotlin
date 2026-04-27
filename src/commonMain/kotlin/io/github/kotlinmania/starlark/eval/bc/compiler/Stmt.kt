// port-lint: source src/eval/bc/compiler/stmt.rs
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

import io.github.kotlinmania.starlark.eval.bc.Bc
import io.github.kotlinmania.starlark.eval.bc.BcSlotIn
import io.github.kotlinmania.starlark.eval.bc.BcWriter
import io.github.kotlinmania.starlark.eval.runtime.FrameSpan
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark.eval.compiler.ExprCompiled
import io.github.kotlinmania.starlark.eval.compiler.IrSpanned
import io.github.kotlinmania.starlark.eval.compiler.StmtCompiled
import io.github.kotlinmania.starlark.eval.compiler.StmtsCompiled
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.FrozenRef
import io.github.kotlinmania.starlark.values.typing.typecompiled.TypeCompiled
import io.github.kotlinmania.starlark.eval.compiler.StmtCompileContext
import io.github.kotlinmania.starlark.eval.compiler.AssignCompiledValue
import io.github.kotlinmania.starlark.eval.compiler.MaybeNot
import io.github.kotlinmania.starlark.eval.compiler.asLocalNonCaptured
import io.github.kotlinmania.starlark.eval.bc.compiler.assign.markDefinitelyAssignedAfter
import io.github.kotlinmania.starlark.eval.bc.compiler.assign.writeBc

/** Compile a for-loop to bytecode. */
internal fun writeFor(
    over: IrSpanned<ExprCompiled>,
    variable: IrSpanned<AssignCompiledValue>,
    span: FrameSpan,
    bc: BcWriter,
    body: (BcWriter) -> Unit,
) {
    val definitelyAssigned = bc.saveDefinitelyAssigned()

    over.writeBcCb(bc) { overSlot, bc2 ->
        val localVar = variable.node.asLocalNonCaptured()
        if (localVar != null) {
            // Typical case: `for x in ...: ...`,
            // compile loop assignment directly to a local variable.
            bc2.writeFor(overSlot, localVar.toBcSlot().toOut(), span) { bc3 ->
                bc3.markDefinitelyAssigned(localVar)
                body(bc3)
            }
        } else {
            // General case, e.g. `for (x, y[0]) in ...: ...`,
            // compile loop assignment to a temporary variable,
            // and reassign it in the loop body.
            bc2.allocSlot { varSlot, bc3 ->
                bc3.writeFor(overSlot, varSlot.toOut(), span) { bc4 ->
                    variable.writeBc(varSlot.toIn(), bc4)
                    variable.node.markDefinitelyAssignedAfter(bc4)
                    body(bc4)
                }
            }
        }
    }

    bc.restoreDefinitelyAssigned(definitelyAssigned)
}

/** Extension: compile a [StmtsCompiled] block to bytecode. */
internal fun StmtsCompiled.writeBc(compiler: StmtCompileContext, bc: BcWriter) {
    for (stmt in this.stmts()) {
        stmt.writeBcStmt(compiler, bc)
    }
}

/** Mark local variables that are definitely assigned after this statement executed. */
internal fun StmtCompiled.markDefinitelyAssignedAfter(bc: BcWriter) {
    when (this) {
        is StmtCompiled.PossibleGc -> {}
        is StmtCompiled.Return -> {
            // `expr` is definitely assigned after `return` statement,
            // but no code is executed after `return`, so marking would be useless.
            @Suppress("UNUSED_EXPRESSION")
            expr
        }
        is StmtCompiled.Expr -> expr.markDefinitelyAssignedAfter(bc)
        is StmtCompiled.Assign -> {
            lhs.node.markDefinitelyAssignedAfter(bc)
            rhs.markDefinitelyAssignedAfter(bc)
            // We might have evaluate types turned off
            @Suppress("UNUSED_EXPRESSION")
            ty
        }
        is StmtCompiled.AssignModify -> {
            rhs.markDefinitelyAssignedAfter(bc)
            lhs.markDefinitelyAssignedAfter(bc)
        }
        is StmtCompiled.If -> {
            cond.markDefinitelyAssignedAfter(bc)
            // We could merge `t` and `f` definitely assigned, e.g.
            // ```
            // if cond:
            //   x = 1
            // else:
            //   x = 2
            // ```
            // we could mark `x` as definitely assigned.
            @Suppress("UNUSED_EXPRESSION")
            thenBlock
            @Suppress("UNUSED_EXPRESSION")
            elseBlock
        }
        is StmtCompiled.For -> {
            over.markDefinitelyAssignedAfter(bc)
        }
        is StmtCompiled.Break -> {}
        is StmtCompiled.Continue -> {}
    }
}

/** If statement is `return x`, return `x`. */
internal fun StmtCompiled.asReturn(): IrSpanned<ExprCompiled>? {
    return when (this) {
        is StmtCompiled.Return -> expr
        else -> null
    }
}

/** Extension: compile a single spanned statement to bytecode. */
private fun IrSpanned<StmtCompiled>.writeBcStmt(
    compiler: StmtCompileContext,
    bc: BcWriter,
) {
    bc.markBeforeStmt(this.span)
    this.writeBcInner(compiler, bc)
    this.node.markDefinitelyAssignedAfter(bc)
}

/** Write an if-then block (no else). */
private fun writeIfThenStmt(
    compiler: StmtCompileContext,
    bc: BcWriter,
    c: IrSpanned<ExprCompiled>,
    maybeNot: MaybeNot,
    t: (StmtCompileContext, BcWriter) -> Unit,
) {
    writeIfThen(
        c,
        maybeNot,
        { bc2 -> t(compiler, bc2) },
        bc,
    )
}

/** Write an if-else block. */
private fun writeIfElseStmt(
    c: IrSpanned<ExprCompiled>,
    t: StmtsCompiled,
    f: StmtsCompiled,
    compiler: StmtCompileContext,
    bc: BcWriter,
) {
    check(!t.isEmpty() || !f.isEmpty())
    if (f.isEmpty()) {
        writeIfThenStmt(compiler, bc, c, MaybeNot.Id) { compiler2, bc2 ->
            t.writeBc(compiler2, bc2)
        }
    } else if (t.isEmpty()) {
        writeIfThenStmt(compiler, bc, c, MaybeNot.Not) { compiler2, bc2 ->
            f.writeBc(compiler2, bc2)
        }
    } else {
        writeIfElse(
            c,
            { bc2 -> t.writeBc(compiler, bc2) },
            { bc2 -> f.writeBc(compiler, bc2) },
            bc,
        )
    }
}

/** Write a return statement. */
private fun writeReturn(
    span: FrameSpan,
    expr: IrSpanned<ExprCompiled>,
    compiler: StmtCompileContext,
    bc: BcWriter,
) {
    bc.writeIterStop(span)
    if (compiler.hasReturnType) {
        expr.writeBcCb(bc) { slot, bc2 ->
            bc2.writeInstr("InstrReturnCheckType", span, slot)
        }
    } else {
        val value = expr.node.asValue()
        if (value != null) {
            bc.writeInstr("InstrReturnConst", span, value)
        } else {
            expr.writeBcCb(bc) { slot, bc2 ->
                bc2.writeInstr("InstrReturn", span, slot)
            }
        }
    }
}

/** Inner compilation of a single statement variant. */
private fun IrSpanned<StmtCompiled>.writeBcInner(
    compiler: StmtCompileContext,
    bc: BcWriter,
) {
    val span = this.span
    when (val stmt = this.node) {
        is StmtCompiled.PossibleGc -> bc.writeInstr("InstrPossibleGc", span, Unit)
        is StmtCompiled.Return -> writeReturn(span, stmt.expr, compiler, bc)
        is StmtCompiled.Expr -> {
            stmt.expr.writeBcForEffect(bc)
        }
        is StmtCompiled.Assign -> {
            val local = stmt.lhs.node.asLocalNonCaptured()
            if (local != null) {
                // Write expression directly to local slot.
                stmt.rhs.writeBc(local.toBcSlot().toOut(), bc)
                checkType(stmt.ty, local.toBcSlot().toIn(), bc)
            } else {
                stmt.rhs.writeBcCb(bc) { slot, bc2 ->
                    checkType(stmt.ty, slot, bc2)
                    stmt.lhs.writeBc(slot, bc2)
                }
            }
        }
        is StmtCompiled.AssignModify -> {
            stmt.lhs.writeBc(span, stmt.op, stmt.rhs, bc)
        }
        is StmtCompiled.If -> {
            writeIfElseStmt(stmt.cond, stmt.thenBlock, stmt.elseBlock, compiler, bc)
        }
        is StmtCompiled.For -> {
            writeFor(stmt.over, stmt.variable, span, bc) { bc2 ->
                stmt.body.writeBc(compiler, bc2)
            }
        }
        is StmtCompiled.Break -> {
            bc.writeBreak(span)
        }
        is StmtCompiled.Continue -> {
            bc.writeContinue(span)
        }
    }
}

/** Helper to write type check instruction if type annotation is present. */
private fun checkType(
    ty: IrSpanned<TypeCompiled>?,
    slotExpr: BcSlotIn,
    bc: BcWriter,
) {
    if (ty != null) {
        bc.writeInstr("InstrCheckType", ty.span, slotExpr to ty.node)
    }
}

/** Compile a statement block to bytecode [Bc]. */
internal fun StmtsCompiled.asBc(
    compiler: StmtCompileContext,
    localNames: FrozenRef<List<FrozenStringValue>>,
    paramCount: Int,
    heap: FrozenHeap,
): Bc {
    val bc = BcWriter.new(localNames.value, paramCount, heap)
    this.writeBc(compiler, bc)

    // Small optimization: if the last statement is return,
    // we do not need to write another return.
    val lastStmt = this.last()?.node
    if (lastStmt !is StmtCompiled.Return) {
        val span = this.last()?.span?.endSpan() ?: FrameSpan.DEFAULT
        if (compiler.hasReturnType) {
            bc.allocSlot { slot, bc2 ->
                bc2.writeConst(span, FrozenValue.newNone(), slot.toOut())
                bc2.writeInstr("InstrReturnCheckType", span, slot.toIn())
            }
        } else {
            bc.writeInstr("InstrReturnConst", span, FrozenValue.newNone())
        }
    }

    return bc.finish()
}
