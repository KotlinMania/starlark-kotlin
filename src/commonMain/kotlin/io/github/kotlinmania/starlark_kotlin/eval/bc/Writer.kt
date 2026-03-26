// port-lint: source src/eval/bc/writer.rs
package io.github.kotlinmania.starlark_kotlin.eval.bc

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

/** Bytecode writer. */

import io.github.kotlinmania.starlark_kotlin.eval.bc.addr.BcAddr
import io.github.kotlinmania.starlark_kotlin.eval.bc.addr.BcAddrOffset
import io.github.kotlinmania.starlark_kotlin.eval.bc.definitely_assigned.BcDefinitelyAssigned
import io.github.kotlinmania.starlark_kotlin.eval.bc.repr.BC_INSTR_ALIGN
import io.github.kotlinmania.starlark_kotlin.eval.bc.stack_ptr.BcSlot
import io.github.kotlinmania.starlark_kotlin.eval.bc.stack_ptr.BcSlotIn
import io.github.kotlinmania.starlark_kotlin.eval.bc.stack_ptr.BcSlotInRange
import io.github.kotlinmania.starlark_kotlin.eval.bc.stack_ptr.BcSlotOut
import io.github.kotlinmania.starlark_kotlin.eval.bc.stack_ptr.BcSlotRange
import io.github.kotlinmania.starlark_kotlin.eval.bc.stack_ptr.BcSlotsN
import io.github.kotlinmania.starlark_kotlin.eval.compiler.expr.MaybeNot
import io.github.kotlinmania.starlark_kotlin.eval.runtime.FrameSpan
import io.github.kotlinmania.starlark_kotlin.eval.runtime.LocalCapturedSlotId
import io.github.kotlinmania.starlark_kotlin.eval.runtime.LocalSlotId
import io.github.kotlinmania.starlark_kotlin.values.FrozenRef
import io.github.kotlinmania.starlark_kotlin.values.layout.Value as FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.types.string.intern.FrozenStringValue
import kotlin.math.max

// #[derive(Debug)]
// pub(crate) struct BcStmtLoc
internal class BcStmtLoc(
    // pub(crate) span: FrameSpan,
    val span: FrameSpan,
)

/**
 * This records the locations of the first instruction for each starlark statement. It is effectively
 * Map<BcAddr, BcStmtLoc>. This is very performance sensitive (when profiling/debugging are enabled we
 * do a lookup for every instruction) and so it is implemented as a vec of statements and then a vec of
 * statement indexes for each possible BcAddr in a bytecode Bc.
 */
// pub(crate) struct BcStatementLocations
internal class BcStatementLocations(
    // pub(crate) locs: Vec<BcStmtLoc>,
    val locs: MutableList<BcStmtLoc> = mutableListOf(),
    /** Map bytecode offset to index in [locs]. */
    // pub(crate) stmts: Vec<u32>,
    val stmts: MutableList<UInt> = mutableListOf(),
) {
    // impl BcStatementLocations

    companion object {
        // const CONTINUED_BIT: u32 = 1 << 31;
        const val CONTINUED_BIT: UInt = 1u shl 31

        val NO_STMT: UInt = UInt.MAX_VALUE

        // pub(crate) fn new() -> Self
        fun new(): BcStatementLocations = BcStatementLocations()
    }

    // fn idx_for(addr: BcAddr) -> usize
    private fun idxFor(addr: BcAddr): Int {
        val addrVal = addr.value.toInt()
        check(addrVal % BC_INSTR_ALIGN == 0)
        return addrVal / BC_INSTR_ALIGN
    }

    // fn push(&mut self, addr: BcAddr, span: BcStmtLoc)
    fun push(addr: BcAddr, span: BcStmtLoc) {
        val idx = idxFor(addr)
        val stmtIdx = locs.size.toUInt()
        check(stmtIdx and CONTINUED_BIT == 0u)
        locs.add(span)
        while (stmts.size <= idx) {
            stmts.add(NO_STMT)
        }
        // We could use .add() to get this in place, but doing by index just makes it clearer
        // that we are doing it correctly.
        stmts[idx] = stmtIdx
    }

    // fn last_stmt_idx(&self) -> Option<u32>
    private fun lastStmtIdx(): UInt? {
        for (stmtIdx in stmts.asReversed()) {
            if (stmtIdx != NO_STMT) {
                return stmtIdx and CONTINUED_BIT.inv()
            }
        }
        return null
    }

    // fn push_prev(&mut self, addr: BcAddr)
    fun pushPrev(addr: BcAddr) {
        val stmtIdx = lastStmtIdx() ?: return
        val idx = idxFor(addr)
        while (stmts.size <= idx) {
            stmts.add(NO_STMT)
        }
        // If the preceding statement ended in a call opcode, and the
        // current opcode is the start of a new statement, it may already be
        // marked. If so, preserve that, rather than marking it as a part of
        // the previous statement (which it is not).
        if (stmts[idx] == NO_STMT) {
            stmts[idx] = stmtIdx or CONTINUED_BIT
        }
    }

    // pub(crate) fn stmt_at(&self, offset: BcAddr) -> Option<(&BcStmtLoc, bool)>
    fun stmtAt(offset: BcAddr): Pair<BcStmtLoc, Boolean>? {
        val idx = idxFor(offset)
        if (idx >= stmts.size) return null
        val v = stmts[idx]
        if (v == NO_STMT) return null
        val continued = (v and CONTINUED_BIT) != 0u
        val stmtIdx = v and CONTINUED_BIT.inv()
        return locs[stmtIdx.toInt()] to continued
    }
}

/**
 * For loop during bytecode write.
 */
// struct BcWriterForLoop
private class BcWriterForLoop(
    /** Iterator variable. */
    // iter: BcSlotIn,
    val iter: BcSlotIn,
    /** Variable to store the next value in. */
    // var: BcSlotOut,
    val variable: BcSlotOut,
    /** Address of the first instruction in the loop body. */
    // inner_addr: BcAddr,
    val innerAddr: BcAddr,
    /** Addresses to patch with the address of the instruction after the loop. */
    // end_addrs_to_patch: Vec<PatchAddr>,
    val endAddrsToPatch: MutableList<PatchAddr>,
)

/**
 * Write bytecode here.
 */
// pub(crate) struct BcWriter<'f>
internal class BcWriter(
    /** Serialized instructions. */
    // instrs: BcInstrsWriter,
    private val instrs: BcInstrsWriter,
    /** Instruction spans, used for errors. */
    // slow_args: Vec<(BcAddr, BcInstrSlowArg)>,
    private val slowArgs: MutableList<Pair<BcAddr, BcInstrSlowArg>>,
    /** For each statement, will store the span for the first instruction and any instruction after a call. */
    // stmt_locs: BcStatementLocations,
    private val stmtLocs: BcStatementLocations,
    /** The last-written opcode. */
    // last_opcode: BcOpcode,
    private var lastOpcode: BcOpcode,
    /** Current stack size. */
    // stack_size: u32,
    private var stackSize: UInt,
    /** Local slot count. */
    // local_names: FrozenRef<'f, [FrozenStringValue]>,
    private val localNames: FrozenRef<List<FrozenStringValue>>,
    /** Local variables which are known to be definitely assigned at current program point. */
    // definitely_assigned: BcDefinitelyAssigned,
    private var definitelyAssigned: BcDefinitelyAssigned,
    /** Max observed stack size. */
    // max_stack_size: u32,
    private var maxStackSize: UInt,
    /** Current loop depth. */
    // for_loops: Vec<BcWriterForLoop>,
    private val forLoops: MutableList<BcWriterForLoop>,
    /** Max observed loop depth. */
    // max_loop_depth: LoopDepth,
    private var maxLoopDepth: LoopDepth,
    /** Allocate various objects here. */
    // pub(crate) heap: &'f FrozenHeap,
    val heap: FrozenHeap,
) {
    // impl<'f> BcWriter<'f>

    companion object {
        /** Empty. */
        // pub(crate) fn new(
        //     local_names: FrozenRef<'f, [FrozenStringValue]>,
        //     param_count: u32,
        //     heap: &'f FrozenHeap,
        // ) -> BcWriter<'f>
        fun new(
            localNames: FrozenRef<List<FrozenStringValue>>,
            paramCount: UInt,
            heap: FrozenHeap,
        ): BcWriter {
            check(paramCount.toInt() <= localNames.value.size)
            val definitelyAssigned = BcDefinitelyAssigned(localNames.value.size)
            for (i in 0u until paramCount) {
                definitelyAssigned.markDefinitelyAssigned(LocalSlotId(i))
            }
            return BcWriter(
                instrs = BcInstrsWriter.new(),
                slowArgs = mutableListOf(),
                stmtLocs = BcStatementLocations.new(),
                lastOpcode = BcOpcode.End,
                stackSize = 0u,
                localNames = localNames,
                definitelyAssigned = definitelyAssigned,
                maxStackSize = 0u,
                heap = heap,
                forLoops = mutableListOf(),
                maxLoopDepth = LoopDepth(0),
            )
        }
    }

    /** Finish writing the bytecode. */
    // pub(crate) fn finish(self) -> Bc
    fun finish(): Bc {
        check(stackSize == 0u)
        check(forLoops.isEmpty())
        return Bc(
            instrs = instrs.finish(slowArgs, stmtLocs, localNames.value.map { it.toString() }),
            localCount = localNames.value.size.toUInt(),
            maxStackSize = maxStackSize,
            maxLoopDepth = maxLoopDepth,
        )
    }

    // fn local_count(&self) -> u32
    private fun localCount(): UInt = localNames.value.size.toUInt()

    /** Current offset. */
    // fn ip(&self) -> BcAddr
    private fun ip(): BcAddr = instrs.ip()

    /**
     * Version of instruction write with explicit slow arg.
     */
    // fn do_write_generic_explicit<I: BcInstr>(
    //     &mut self,
    //     slow_arg: BcInstrSlowArg,
    //     arg: I::Arg,
    // ) -> (BcAddr, *const I::Arg)
    private fun doWriteGenericExplicit(
        instrName: String,
        slowArg: BcInstrSlowArg,
        arg: Any,
    ): Pair<BcAddr, Int> {
        // If the previously written instruction was a form of a call
        // instruction, instrument this instruction with the current statement
        // span so that the time this and following instructions take can be
        // attributed to the correct statement.
        if (lastOpcode.isCall()) {
            stmtLocs.pushPrev(ip())
        }
        lastOpcode = BcOpcode.valueOf(instrName.removePrefix("Instr"))

        slowArgs.add(ip() to slowArg)
        val instrIp = instrs.write(BcInstrHeader.forOpcode(lastOpcode), arg)
        return instrIp to instrs.instrs.size - 1
    }

    // pub(crate) fn mark_before_stmt(&mut self, span: FrameSpan)
    fun markBeforeStmt(span: FrameSpan) {
        stmtLocs.push(ip(), BcStmtLoc(span))
    }

    /**
     * Write an instruction, return address and argument.
     */
    // fn write_instr_ret_arg_explicit<I: BcInstr>(
    //     &mut self,
    //     slow_arg: BcInstrSlowArg,
    //     arg: I::Arg,
    // ) -> (BcAddr, *const I::Arg)
    private fun writeInstrRetArgExplicit(
        instrName: String,
        slowArg: BcInstrSlowArg,
        arg: Any,
    ): Pair<BcAddr, Int> {
        return doWriteGenericExplicit(instrName, slowArg, arg)
    }

    // fn write_instr_ret_arg<I: BcInstr>(
    //     &mut self,
    //     span: FrameSpan,
    //     arg: I::Arg,
    // ) -> (BcAddr, *const I::Arg)
    private fun writeInstrRetArg(
        instrName: String,
        span: FrameSpan,
        arg: Any,
    ): Pair<BcAddr, Int> {
        return writeInstrRetArgExplicit(
            instrName,
            BcInstrSlowArg(span),
            arg,
        )
    }

    // pub(crate) fn write_instr_explicit<I: BcInstr>(
    //     &mut self,
    //     slow_arg: BcInstrSlowArg,
    //     arg: I::Arg,
    // )
    fun writeInstrExplicit(
        instrName: String,
        slowArg: BcInstrSlowArg,
        arg: Any,
    ) {
        writeInstrRetArgExplicit(instrName, slowArg, arg)
    }

    /** Write an instruction. */
    // pub(crate) fn write_instr<I: BcInstr>(&mut self, span: FrameSpan, arg: I::Arg)
    fun writeInstr(instrName: String, span: FrameSpan, arg: Any) {
        writeInstrExplicit(
            instrName,
            BcInstrSlowArg(span),
            arg,
        )
    }

    /** Write load constant instruction. */
    // pub(crate) fn write_const(&mut self, span: FrameSpan, value: FrozenValue, slot: BcSlotOut)
    fun writeConst(span: FrameSpan, value: FrozenValue, slot: BcSlotOut) {
        check(slot.get().value < localCount() + stackSize)

        writeInstr("InstrConst", span, value to slot)
    }

    /** Write load local instruction. */
    // pub(crate) fn write_load_local(
    //     &mut self,
    //     span: FrameSpan,
    //     slot: LocalSlotId,
    //     target: BcSlotOut,
    // )
    fun writeLoadLocal(
        span: FrameSpan,
        slot: LocalSlotId,
        target: BcSlotOut,
    ) {
        check(slot.value < localCount())

        val definiteSlot = tryDefinitelyAssigned(slot)
        if (definiteSlot != null) {
            writeMov(span, definiteSlot, target)
        } else {
            writeInstr("InstrLoadLocal", span, slot to target)
        }
    }

    // pub(crate) fn write_load_local_captured(
    //     &mut self,
    //     span: FrameSpan,
    //     source: LocalCapturedSlotId,
    //     target: BcSlotOut,
    // )
    fun writeLoadLocalCaptured(
        span: FrameSpan,
        source: LocalCapturedSlotId,
        target: BcSlotOut,
    ) {
        check(source.value < localCount())
        check(target.get().value < localCount() + stackSize)
        writeInstrRetArg("InstrLoadLocalCaptured", span, source to target)
    }

    // pub(crate) fn write_mov(&mut self, span: FrameSpan, source: BcSlotIn, target: BcSlotOut)
    fun writeMov(span: FrameSpan, source: BcSlotIn, target: BcSlotOut) {
        check(source.get().value < localCount() + stackSize)
        check(target.get().value < localCount() + stackSize)

        // Do not emit no-op `Mov`.
        // It can occur when compiling code like `x = x`.
        // Currently we do not erase these no-op assignments at IR.
        if (source.get() == target.get()) {
            return
        }

        writeInstrRetArg("InstrMov", span, source to target)
    }

    // pub(crate) fn write_store_local_captured(
    //     &mut self,
    //     span: FrameSpan,
    //     source: BcSlotIn,
    //     target: LocalCapturedSlotId,
    // )
    fun writeStoreLocalCaptured(
        span: FrameSpan,
        source: BcSlotIn,
        target: LocalCapturedSlotId,
    ) {
        check(source.get().value < localCount() + stackSize)
        check(target.value < localCount())
        writeInstrRetArg("InstrStoreLocalCaptured", span, source to target)
    }

    /** Patch previously written address with current IP. */
    // pub(crate) fn patch_addr(&mut self, addr: PatchAddr)
    fun patchAddr(addr: PatchAddr) {
        instrs.patchAddr(addr)
    }

    // pub(crate) fn patch_addrs(&mut self, addrs: Vec<PatchAddr>)
    fun patchAddrs(addrs: List<PatchAddr>) {
        for (addr in addrs) {
            patchAddr(addr)
        }
    }

    /** Write branch. */
    // pub(crate) fn write_br(&mut self, span: FrameSpan) -> PatchAddr
    fun writeBr(span: FrameSpan): PatchAddr {
        val (addr, argIndex) = writeInstrRetArg("InstrBr", span, BcAddrOffset.FORWARD)
        return instrs.addrToPatch(addr, argIndex)
    }

    /** Write conditional branch. */
    // pub(crate) fn write_if_not_br(&mut self, cond: BcSlotIn, span: FrameSpan) -> PatchAddr
    fun writeIfNotBr(cond: BcSlotIn, span: FrameSpan): PatchAddr {
        val (addr, argIndex) =
            writeInstrRetArg("InstrIfNotBr", span, cond to BcAddrOffset.FORWARD)
        return instrs.addrToPatch(addr, argIndex)
    }

    /** Write conditional branch. */
    // pub(crate) fn write_if_br(&mut self, cond: BcSlotIn, span: FrameSpan) -> PatchAddr
    fun writeIfBr(cond: BcSlotIn, span: FrameSpan): PatchAddr {
        val (addr, argIndex) =
            writeInstrRetArg("InstrIfBr", span, cond to BcAddrOffset.FORWARD)
        return instrs.addrToPatch(addr, argIndex)
    }

    // fn write_if_else_impl(
    //     &mut self,
    //     cond: BcSlotIn,
    //     span: FrameSpan,
    //     then_block: impl FnOnce(&mut Self),
    //     else_block: impl FnOnce(&mut Self),
    // )
    private fun writeIfElseImpl(
        cond: BcSlotIn,
        span: FrameSpan,
        thenBlock: (BcWriter) -> Unit,
        elseBlock: (BcWriter) -> Unit,
    ) {
        val saved = saveDefinitelyAssigned()

        val elseTarget = writeIfNotBr(cond, span)
        thenBlock(this)
        val endTarget = writeBr(span)

        restoreDefinitelyAssigned(saved.copy())

        patchAddr(elseTarget)
        elseBlock(this)
        patchAddr(endTarget)

        restoreDefinitelyAssigned(saved)
    }

    /** Write if-else block. */
    // pub(crate) fn write_if_else(
    //     &mut self,
    //     cond: BcSlotIn,
    //     maybe_not: MaybeNot,
    //     span: FrameSpan,
    //     then_block: impl FnOnce(&mut Self),
    //     else_block: impl FnOnce(&mut Self),
    // )
    fun writeIfElse(
        cond: BcSlotIn,
        maybeNot: MaybeNot,
        span: FrameSpan,
        thenBlock: (BcWriter) -> Unit,
        elseBlock: (BcWriter) -> Unit,
    ) {
        when (maybeNot) {
            MaybeNot.Id -> writeIfElseImpl(cond, span, thenBlock, elseBlock)
            MaybeNot.Not -> writeIfElseImpl(cond, span, elseBlock, thenBlock)
        }
    }

    // pub(crate) fn write_continue(&mut self, span: FrameSpan)
    fun writeContinue(span: FrameSpan) {
        val loopDepth = LoopDepth(forLoops.size - 1)
        val forLoop = forLoops.last()
        val jumpBack = ip().offsetFrom(forLoop.innerAddr).neg()
        val variable = forLoop.variable
        val (addr, argIndex) = writeInstrRetArg(
            "InstrContinue",
            span,
            listOf(forLoop.iter, loopDepth, variable, jumpBack, BcAddrOffset.FORWARD),
        )
        val endPatch = instrs.addrToPatch(addr, argIndex)
        forLoops.last().endAddrsToPatch.add(endPatch)
    }

    // pub(crate) fn write_break(&mut self, span: FrameSpan)
    fun writeBreak(span: FrameSpan) {
        val forLoop = forLoops.last()
        val (addr, argIndex) =
            writeInstrRetArg("InstrBreak", span, forLoop.iter to BcAddrOffset.FORWARD)
        val endPatch = instrs.addrToPatch(addr, argIndex)
        forLoops.last().endAddrsToPatch.add(endPatch)
    }

    /** Write for loop. */
    // pub(crate) fn write_for(
    //     &mut self,
    //     over: BcSlotIn,
    //     var: BcSlotOut,
    //     span: FrameSpan,
    //     body: impl FnOnce(&mut BcWriter),
    // )
    fun writeFor(
        over: BcSlotIn,
        variable: BcSlotOut,
        span: FrameSpan,
        body: (BcWriter) -> Unit,
    ) {
        // Allocate a slot to store the iterator.
        allocSlot { iter, bc ->
            // Definitely assigned save/restore is redundant here, it is performed more precisely
            // by the caller. But it is safer to do it here anyway.
            val saved = bc.saveDefinitelyAssigned()

            val loopDepth = LoopDepth(bc.forLoops.size)
            val (addr, argIndex) = bc.writeInstrRetArg(
                "InstrIter",
                span,
                listOf(over, loopDepth, iter.toOut(), variable, BcAddrOffset.FORWARD),
            )
            val endPatch = bc.instrs.addrToPatch(addr, argIndex)
            bc.forLoops.add(
                BcWriterForLoop(
                    innerAddr = bc.ip(),
                    endAddrsToPatch = mutableListOf(endPatch),
                    variable = variable,
                    iter = iter.toIn(),
                )
            )
            bc.maxLoopDepth = maxOf(bc.maxLoopDepth, LoopDepth(bc.forLoops.size))
            body(bc)
            bc.writeContinue(span)
            val completedForLoop = bc.forLoops.removeLast()
            for (addrToPatch in completedForLoop.endAddrsToPatch) {
                bc.patchAddr(addrToPatch)
            }

            bc.restoreDefinitelyAssigned(saved)
        }
    }

    /**
     * Write instructions to stop all current iterations.
     * This is done before `return`.
     */
    // pub(crate) fn write_iter_stop(&mut self, span: FrameSpan)
    fun writeIterStop(span: FrameSpan) {
        // We can stop iteration in any order, but for consistency stop them in reverse order.
        for (depth in (0 until forLoops.size).reversed()) {
            val iter = forLoops[depth].iter
            writeInstr("InstrIterStop", span, iter)
        }
    }

    // fn stack_add(&mut self, add: u32)
    private fun stackAdd(add: UInt) {
        stackSize += add
        maxStackSize = max(maxStackSize, stackSize)
    }

    // fn stack_sub(&mut self, sub: u32)
    private fun stackSub(sub: UInt) {
        check(stackSize >= sub)
        stackSize -= sub
    }

    /**
     * Convert local variable to BC slot if it is known to be definitely assigned
     * at this execution point.
     */
    // pub(crate) fn try_definitely_assigned(&self, local: LocalSlotId) -> Option<BcSlotIn>
    fun tryDefinitelyAssigned(local: LocalSlotId): BcSlotIn? {
        check(local.value < localCount())
        return if (definitelyAssigned.isDefinitelyAssigned(local)) {
            local.toBcSlot().toIn()
        } else {
            null
        }
    }

    // pub(crate) fn mark_definitely_assigned(&mut self, local: LocalSlotId)
    fun markDefinitelyAssigned(local: LocalSlotId) {
        definitelyAssigned.markDefinitelyAssigned(local)
    }

    // pub(crate) fn save_definitely_assigned(&self) -> BcDefinitelyAssigned
    fun saveDefinitelyAssigned(): BcDefinitelyAssigned {
        return definitelyAssigned.copy()
    }

    // pub(crate) fn restore_definitely_assigned(&mut self, saved: BcDefinitelyAssigned)
    fun restoreDefinitelyAssigned(saved: BcDefinitelyAssigned) {
        saved.assertSmallerThan(definitelyAssigned)
        definitelyAssigned = saved
    }

    /**
     * Allocate a temporary slot, and call a callback.
     *
     * The slot is valid during the callback run, and can be reused later.
     */
    // pub(crate) fn alloc_slot<R>(&mut self, k: impl FnOnce(BcSlot, &mut BcWriter) -> R) -> R
    fun <R> allocSlot(k: (BcSlot, BcWriter) -> R): R {
        val slot = BcSlot(localCount() + stackSize)
        stackAdd(1u)
        val r = k(slot, this)
        stackSub(1u)
        return r
    }

    /** Allocate several slots for the duration of callback run. */
    // pub(crate) fn alloc_slots<R>(
    //     &mut self,
    //     count: u32,
    //     k: impl FnOnce(BcSlotRange, &mut BcWriter) -> R,
    // ) -> R
    fun <R> allocSlots(count: UInt, k: (BcSlotRange, BcWriter) -> R): R {
        val slots = BcSlotRange(
            start = BcSlot(localCount() + stackSize),
            end = BcSlot(localCount() + stackSize + count),
        )
        stackAdd(count)
        val r = k(slots, this)
        stackSub(count)
        return r
    }

    /** Allocate several slots. */
    // pub(crate) fn alloc_slots_c<const N: usize, R>(
    //     &mut self,
    //     k: impl FnOnce(BcSlotsN<N>, &mut BcWriter) -> R,
    // ) -> R
    fun <R> allocSlotsC(n: Int, k: (BcSlotsN, BcWriter) -> R): R {
        return allocSlots(n.toUInt()) { slots, bc ->
            k(BcSlotsN.fromRange(n, slots), bc)
        }
    }

    /**
     * Allocate several slots for typical compilation of several expressions.
     */
    // pub(crate) fn alloc_slots_for_exprs<K, R>(
    //     &mut self,
    //     exprs: impl IntoIterator<Item = K>,
    //     mut expr: impl FnMut(BcSlot, K, &mut BcWriter),
    //     k: impl FnOnce(BcSlotInRange, &mut BcWriter) -> R,
    // ) -> R
    fun <K, R> allocSlotsForExprs(
        // Iterate over the elements.
        exprs: Iterable<K>,
        // Invoke a callback which fills the slots.
        expr: (BcSlot, K, BcWriter) -> Unit,
        // And then invoke a callback which consumes all the slots again together.
        k: (BcSlotInRange, BcWriter) -> R,
    ): R {
        val start = BcSlot(localCount() + stackSize)
        var end = start
        for (item in exprs) {
            stackAdd(1u)
            // `expr` callback may allocate more temporary slots,
            // but they are released after the callback returns.
            // So resulting slots are sequential.
            expr(end, item, this)
            end = BcSlot(end.value + 1u)
        }
        val range = if (end == start) {
            // This is not really necessary, empty range is equally valid
            // with any starting point, but this makes bytecode output
            // (in particular, in golden tests) more readable.
            BcSlotInRange.default()
        } else {
            BcSlotRange(start, end).toIn()
        }
        val r = k(range, this)
        stackSub(end.value - start.value)
        return r
    }

    // pub(crate) fn alloc_file_span(&self, span: FrameSpan) -> FrozenRef<'static, FrameSpan>
    fun allocFileSpan(span: FrameSpan): FrozenRef<FrameSpan> {
        return FrozenRef.new(span)
    }
}
