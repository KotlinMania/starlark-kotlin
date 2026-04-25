// port-lint: source src/eval/bc/writer.rs
package io.github.kotlinmania.starlark.eval.bc

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

import kotlin.math.max
import io.github.kotlinmania.starlark.eval.bc.BcDefinitelyAssigned
import io.github.kotlinmania.starlark.eval.bc.BC_INSTR_ALIGN
import io.github.kotlinmania.starlark.eval.bc.BcInstrHeader
import io.github.kotlinmania.starlark.eval.compiler.MaybeNot
import io.github.kotlinmania.starlark.eval.runtime.FrameSpan
import io.github.kotlinmania.starlark.eval.runtime.LocalCapturedSlotId
import io.github.kotlinmania.starlark.eval.runtime.LocalSlotId
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue

// --- BcStmtLoc ---

internal class BcStmtLoc(
    val span: FrameSpan,
)

/**
 * This records the locations of the first instruction for each starlark statement. It's effectively
 * Map<BcAddr, BcStmtLoc>. This is very performance sensitive (when profiling/debugging are enabled we
 * do a lookup for every instruction) and so it's implemented as a vec of statements and then a vec of
 * statement indexes for each possible BcAddr in a bytecode Bc.
 */
internal class BcStatementLocations(
    val locs: MutableList<BcStmtLoc> = mutableListOf(),
    /** Map bytecode offset to index in `locs`. */
    val stmts: MutableList<Int> = mutableListOf(),
) {
    companion object {
        const val CONTINUED_BIT: Int = 1 shl 31
        const val MAX_VALUE: Int = Int.MAX_VALUE

        fun new(): BcStatementLocations = BcStatementLocations()

        private fun idxFor(addr: BcAddr): Int {
            val addrVal = addr.value.toInt()
            return addrVal / BC_INSTR_ALIGN
        }
    }

    fun push(addr: BcAddr, span: BcStmtLoc) {
        val idx = idxFor(addr)
        val stmtIdx = locs.size
        locs.add(span)
        while (stmts.size <= idx) {
            stmts.add(MAX_VALUE)
        }
        // we could use .add() to get this in place, but doing by index just makes it clearer
        // that we're doing it correctly.
        stmts[idx] = stmtIdx
    }

    private fun lastStmtIdx(): Int? {
        for (stmtIdx in stmts.asReversed()) {
            if (stmtIdx != MAX_VALUE) {
                return stmtIdx and CONTINUED_BIT.inv()
            }
        }
        return null
    }

    fun pushPrev(addr: BcAddr) {
        val stmtIdx = lastStmtIdx() ?: return
        val idx = idxFor(addr)
        while (stmts.size <= idx) {
            stmts.add(MAX_VALUE)
        }
        // If the preceding statement ended in a call opcode, and the
        // current opcode is the start of a new statement, it may already be
        // marked. If so, preserve that, rather than marking it as a part of
        // the previous statement (which it is not).
        if (stmts[idx] == MAX_VALUE) {
            stmts[idx] = stmtIdx or CONTINUED_BIT
        }
    }

    fun stmtAt(offset: BcAddr): Pair<BcStmtLoc, Boolean>? {
        val idx = idxFor(offset)
        if (idx >= stmts.size) return null
        val v = stmts[idx]
        if (v == MAX_VALUE) return null
        val continued = (v and CONTINUED_BIT) != 0
        val stmtIdx = v and CONTINUED_BIT.inv()
        return locs[stmtIdx] to continued
    }
}

/** For loop during bytecode write. */
internal class BcWriterForLoop(
    /** Iterator variable. */
    val iter: BcSlotIn,
    /** Variable to store the next value in. */
    val var_: BcSlotOut,
    /** Address of the first instruction in the loop body. */
    val innerAddr: BcAddr,
    /** Addresses to patch with the address of the instruction after the loop. */
    val endAddrsToPatch: MutableList<PatchAddr>,
)

/** Write bytecode here. */
internal class BcWriter(
    /** Serialized instructions. */
    private val instrs: BcInstrsWriter,
    /** Instruction spans, used for errors. */
    private val slowArgs: MutableList<Pair<BcAddr, BcInstrSlowArg>>,
    /** For each statement, will store the span for the first instruction and any instruction after a call. */
    private val stmtLocs: BcStatementLocations,
    /** The last-written opcode. */
    private var lastOpcode: BcOpcode,
    /** Current stack size. */
    private var stackSize: Int,
    /** Local slot count. */
    private val localNames: List<FrozenStringValue>,
    /** Local variables which are known to be definitely assigned at current program point. */
    private var definitelyAssigned: BcDefinitelyAssigned,
    /** Max observed stack size. */
    private var maxStackSize: Int,
    /** Current loop depth. */
    private val forLoops: MutableList<BcWriterForLoop>,
    /** Max observed loop depth. */
    private var maxLoopDepth: LoopDepth,
    /** Allocate various objects here. */
    val heap: FrozenHeap,
) {
    companion object {
        /** Empty. */
        fun new(
            localNames: List<FrozenStringValue>,
            paramCount: Int,
            heap: FrozenHeap,
        ): BcWriter {
            check(paramCount <= localNames.size)
            val definitelyAssigned = BcDefinitelyAssigned(localNames.size)
            for (i in 0 until paramCount) {
                definitelyAssigned.markDefinitelyAssigned(LocalSlotId(i.toUInt()))
            }
            return BcWriter(
                instrs = BcInstrsWriter.new(),
                slowArgs = mutableListOf(),
                stmtLocs = BcStatementLocations.new(),
                lastOpcode = BcOpcode.End,
                stackSize = 0,
                localNames = localNames,
                definitelyAssigned = definitelyAssigned,
                maxStackSize = 0,
                heap = heap,
                forLoops = mutableListOf(),
                maxLoopDepth = LoopDepth(0),
            )
        }

        /** Map instruction name (e.g. "InstrConst") to BcOpcode (e.g. BcOpcode.Const). */
        internal fun instrNameToOpcode(instrName: String): BcOpcode {
            val opcodeName = instrName.removePrefix("Instr")
            return BcOpcode.valueOf(opcodeName)
        }
    }

    /** Finish writing the bytecode. */
    fun finish(): Bc {
        check(stackSize == 0)
        check(forLoops.isEmpty())
        return Bc(
            instrs = instrs.finish(slowArgs, stmtLocs, localNames),
            localCount = localNames.size.toUInt(),
            maxStackSize = maxStackSize.toUInt(),
            maxLoopDepth = maxLoopDepth,
        )
    }

    private fun localCount(): Int = localNames.size

    /** Current offset. */
    private fun ip(): BcAddr = instrs.ip()

    /** Version of instruction write with explicit slow arg. */
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
        val opcode = instrNameToOpcode(instrName)
        lastOpcode = opcode

        slowArgs.add(ip() to slowArg)
        val header = BcInstrHeader.forOpcode(opcode)
        // The arg is stored at (current instrs size + 1) since write adds header then arg.
        val argIndex = instrs.instrsSize() + 1
        val addr = instrs.write(header, arg)
        return addr to argIndex
    }

    fun markBeforeStmt(span: FrameSpan) {
        stmtLocs.push(ip(), BcStmtLoc(span))
    }

    /** Write an instruction, return address and argument. */
    private fun writeInstrRetArgExplicit(
        instrName: String,
        slowArg: BcInstrSlowArg,
        arg: Any,
    ): Pair<BcAddr, Int> {
        return doWriteGenericExplicit(instrName, slowArg, arg)
    }

    private fun writeInstrRetArg(
        instrName: String,
        span: FrameSpan,
        arg: Any,
    ): Pair<BcAddr, Int> {
        return writeInstrRetArgExplicit(instrName, BcInstrSlowArg(span), arg)
    }

    fun writeInstrExplicit(
        instrName: String,
        slowArg: BcInstrSlowArg,
        arg: Any,
    ) {
        writeInstrRetArgExplicit(instrName, slowArg, arg)
    }

    /** Write an instruction. */
    fun writeInstr(instrName: String, span: FrameSpan, arg: Any) {
        writeInstrExplicit(instrName, BcInstrSlowArg(span), arg)
    }

    /** Write load constant instruction. */
    fun writeConst(span: FrameSpan, value: FrozenValue, slot: BcSlotOut) {
        check(slot.get().index < (localCount() + stackSize).toUInt())
        writeInstr("InstrConst", span, value to slot)
    }

    /** Write load local instruction. */
    fun writeLoadLocal(
        span: FrameSpan,
        slot: LocalSlotId,
        target: BcSlotOut,
    ) {
        check(slot.index < localCount().toUInt())
        val definiteSlot = tryDefinitelyAssigned(slot)
        if (definiteSlot != null) {
            writeMov(span, definiteSlot, target)
        } else {
            writeInstr("InstrLoadLocal", span, slot to target)
        }
    }

    fun writeLoadLocalCaptured(
        span: FrameSpan,
        source: LocalCapturedSlotId,
        target: BcSlotOut,
    ) {
        check(source.index < localCount().toUInt())
        check(target.get().index < (localCount() + stackSize).toUInt())
        writeInstrRetArg("InstrLoadLocalCaptured", span, source to target)
    }

    fun writeMov(span: FrameSpan, source: BcSlotIn, target: BcSlotOut) {
        check(source.get().index < (localCount() + stackSize).toUInt())
        check(target.get().index < (localCount() + stackSize).toUInt())

        // Do not emit no-op `Mov`.
        // It can occur when compiling code like `x = x`.
        // Currently we do not erase these no-op assignments at IR.
        if (source.get().index == target.get().index) {
            return
        }

        writeInstrRetArg("InstrMov", span, source to target)
    }

    fun writeStoreLocalCaptured(
        span: FrameSpan,
        source: BcSlotIn,
        target: LocalCapturedSlotId,
    ) {
        check(source.get().index < (localCount() + stackSize).toUInt())
        check(target.index < localCount().toUInt())
        writeInstrRetArg("InstrStoreLocalCaptured", span, source to target)
    }

    /** Patch previously written address with current IP. */
    fun patchAddr(addr: PatchAddr) {
        instrs.patchAddr(addr)
    }

    fun patchAddrs(addrs: List<PatchAddr>) {
        for (addr in addrs) {
            patchAddr(addr)
        }
    }

    /** Write branch. */
    fun writeBr(span: FrameSpan): PatchAddr {
        val (addr, argIndex) = writeInstrRetArg("InstrBr", span, BcAddrOffset.FORWARD)
        return instrs.addrToPatch(addr, argIndex)
    }

    /** Write conditional branch. */
    fun writeIfNotBr(cond: BcSlotIn, span: FrameSpan): PatchAddr {
        val (addr, argIndex) = writeInstrRetArg("InstrIfNotBr", span, cond to BcAddrOffset.FORWARD)
        return instrs.addrToPatch(addr, argIndex)
    }

    /** Write conditional branch. */
    fun writeIfBr(cond: BcSlotIn, span: FrameSpan): PatchAddr {
        val (addr, argIndex) = writeInstrRetArg("InstrIfBr", span, cond to BcAddrOffset.FORWARD)
        return instrs.addrToPatch(addr, argIndex)
    }

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

    fun writeContinue(span: FrameSpan) {
        val loopDepth = LoopDepth(forLoops.size - 1)
        val forLoop = forLoops.last()
        val jumpBack = ip().offsetFrom(forLoop.innerAddr).neg()
        val var_ = forLoop.var_
        val (addr, argIndex) = writeInstrRetArg(
            "InstrContinue",
            span,
            listOf(forLoop.iter, loopDepth, var_, jumpBack, BcAddrOffset.FORWARD),
        )
        val endPatch = instrs.addrToPatch(addr, argIndex)
        forLoops.last().endAddrsToPatch.add(endPatch)
    }

    fun writeBreak(span: FrameSpan) {
        val forLoop = forLoops.last()
        val (addr, argIndex) = writeInstrRetArg(
            "InstrBreak",
            span,
            forLoop.iter to BcAddrOffset.FORWARD,
        )
        val endPatch = instrs.addrToPatch(addr, argIndex)
        forLoops.last().endAddrsToPatch.add(endPatch)
    }

    /** Write for loop. */
    fun writeFor(
        over: BcSlotIn,
        var_: BcSlotOut,
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
                listOf(over, loopDepth, iter.toOut(), var_, BcAddrOffset.FORWARD),
            )
            val endPatch = bc.instrs.addrToPatch(addr, argIndex)
            bc.forLoops.add(BcWriterForLoop(
                innerAddr = bc.ip(),
                endAddrsToPatch = mutableListOf(endPatch),
                var_ = var_,
                iter = iter.toIn(),
            ))
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
    fun writeIterStop(span: FrameSpan) {
        // We can stop iteration in any order, but let's for consistency stop them in reverse order.
        for (depth in (forLoops.indices).reversed()) {
            val iter = forLoops[depth].iter
            writeInstr("InstrIterStop", span, iter)
        }
    }

    private fun stackAdd(add: Int) {
        stackSize += add
        maxStackSize = max(maxStackSize, stackSize)
    }

    private fun stackSub(sub: Int) {
        check(stackSize >= sub)
        stackSize -= sub
    }

    /**
     * Convert local variable to BC slot if it is known to be definitely assigned
     * at this execution point.
     */
    fun tryDefinitelyAssigned(local: LocalSlotId): BcSlotIn? {
        check(local.index < localCount().toUInt())
        return if (definitelyAssigned.isDefinitelyAssigned(local)) {
            local.toBcSlot().toIn()
        } else {
            null
        }
    }

    fun markDefinitelyAssigned(local: LocalSlotId) {
        definitelyAssigned.markDefinitelyAssigned(local)
    }

    fun saveDefinitelyAssigned(): BcDefinitelyAssigned {
        return definitelyAssigned.copy()
    }

    fun restoreDefinitelyAssigned(saved: BcDefinitelyAssigned) {
        saved.assertSmallerThan(definitelyAssigned)
        definitelyAssigned = saved
    }

    /**
     * Allocate a temporary slot, and call a callback.
     *
     * The slot is valid during the callback run, and can be reused later.
     */
    fun <R> allocSlot(k: (BcSlot, BcWriter) -> R): R {
        val slot = BcSlot((localCount() + stackSize).toUInt())
        stackAdd(1)
        val r = k(slot, this)
        stackSub(1)
        return r
    }

    /** Allocate several slots for the duration of callback run. */
    fun <R> allocSlots(count: Int, k: (BcSlotRange, BcWriter) -> R): R {
        val slots = BcSlotRange(
            start = BcSlot((localCount() + stackSize).toUInt()),
            end = BcSlot((localCount() + stackSize + count).toUInt()),
        )
        stackAdd(count)
        val r = k(slots, this)
        stackSub(count)
        return r
    }

    /** Allocate several slots as [BcSlotsN], wrapping [allocSlots]. */
    // pub(crate) fn alloc_slots_c<const N: usize, R>(&mut self, k: impl FnOnce(BcSlotsN<N>, &mut BcWriter) -> R) -> R
    fun <R> allocSlotsC(count: Int, k: (BcSlotsN, BcWriter) -> R): R {
        return allocSlots(count) { slots, bc -> k(BcSlotsN.fromRange(count, slots), bc) }
    }

    /** Allocate several slots for typical compilation of several expressions. */
    fun <K, R> allocSlotsForExprs(
        // Iterate over the elements.
        exprs: Iterable<K>,
        // Invoke a callback which fills the slots.
        expr: (BcSlot, K, BcWriter) -> Unit,
        // And then invoke a callback which consumes all the slots again together.
        k: (BcSlotInRange, BcWriter) -> R,
    ): R {
        val start = BcSlot((localCount() + stackSize).toUInt())
        var end = start
        for (item in exprs) {
            stackAdd(1)
            // `expr` callback may allocate more temporary slots,
            // but they are released after the callback returns.
            // So resulting slots are sequential.
            expr(end, item, this)
            end = BcSlot(end.index + 1.toUInt())
        }
        val range = if (end.index == start.index) {
            // This is not really necessary, empty range is equally valid
            // with any starting point, but this makes bytecode output
            // (in particular, in golden tests) more readable.
            BcSlotInRange.default()
        } else {
            BcSlotRange(start, end).toIn()
        }
        val r = k(range, this)
        stackSub((end.index - start.index).toInt())
        return r
    }

    fun allocFileSpan(span: FrameSpan): FrameSpan {
        // In Rust, this allocates the span on the frozen heap via alloc_any.
        // In Kotlin, the GC manages memory so we just return the span.
        return span
    }
}
