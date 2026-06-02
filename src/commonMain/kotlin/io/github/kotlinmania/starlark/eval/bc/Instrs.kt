// port-lint: source src/eval/bc/instrs.rs
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

/**
 * Instructions serialized in a buffer.
 *
 * In Rust, instructions are serialized into a `u64`-aligned byte array and accessed via raw pointer
 * arithmetic with manual `Drop` for cleanup. In Kotlin, we use a list-of-objects representation
 * where each instruction is stored as a (`BcInstrHeader`, arg) pair. The garbage collector handles
 * cleanup, so `Drop` semantics become no-ops.
 */

// --- Drop support ---

/**
 * Drop instruction at given address.
 *
 * In Rust, this dispatches via [BcOpcodeHandler] to call `ptr::drop_in_place` for the
 * concrete `BcInstrRepr<I>` at the pointer. In Kotlin, the garbage collector handles
 * cleanup automatically.
 */
private fun BcOpcode.dropInPlace(ptr: BcPtrAddr) {
}

/**
 * Invoke drop for instructions in the buffer.
 *
 * In Rust, this walks the raw byte buffer calling `opcode.drop_in_place(ptr)` for each
 * instruction. In Kotlin, the garbage collector handles cleanup.
 */
private fun dropInstrs(instrs: List<Any>) {
    // while ptr != end {
    //     assert!(ptr < end);
    //     opcode.drop_in_place(ptr);
    //     ptr = ptr.add(opcode.size_of_repr());
}

// --- Static empty instructions ---

/**
 * Statically allocate a valid instruction buffer micro-optimization.
 *
 * Valid bytecode must end with the `End` instruction, otherwise evaluation overruns
 * the instruction buffer.
 *
 * [BcInstrs] type needs to have a default constructor (it is convenient).
 *
 * In Rust, allocating a vec in `BcInstrs::default` is non-free, so a static
 * `BcInstrRepr<InstrEnd>` is reinterpreted as `&'static [u64]`. In Kotlin,
 * we simply create a small list with the `End` instruction.
 */
private fun emptyInstrs(): List<Any> {
    // static END_OF_BC: BcInstrRepr<InstrEnd> = BcInstrRepr {
    //     header: BcInstrHeader::for_opcode(BcOpcode::End),
    //     arg: BcInstrEndArg {
    //         end_addr: BcAddr(0),
    //         slow_args: Vec::new(),
    //         local_names: FrozenRef::new(&[]),
    //     },
    //     _align: [],
    // };
    //         &END_OF_BC as *const BcInstrRepr<_> as *const u64,
    //     )
    return listOf(
        BcInstrHeader.forOpcode(BcOpcode.End),
        BcInstrEndArg(),
    )
}

// --- BcInstrs ---

/**
 * Bytecode instructions container.
 *
 * In Rust, this holds either heap-allocated (`Box<[u64]>`) or statically-allocated
 * (`&'static [u64]`) instruction bytes via `Either`. The heap-allocated variant
 * calls `drop_instrs` on `Drop`. In Kotlin, we use a simple [List] since the
 * garbage collector manages allocation and deallocation.
 *
 * Instructions are stored as `(BcInstrHeader, arg)` pairs. Each header occupies
 * one list slot and its corresponding argument occupies the next slot.
 */
class BcInstrs private constructor(
    private val instrs: List<Any>,
    internal val stmtLocs: BcStatementLocations,
) {
    //       match &self.instrs {
    // In Kotlin, GC handles cleanup.

    companion object {
        /** Create a default (empty) [BcInstrs] containing only the End instruction. */
        fun default(): BcInstrs = forInstrs(emptyInstrs(), BcStatementLocations.new())

        //     stmt_locs: BcStatementLocations,

        /** Create [BcInstrs] from an instruction buffer and statement locations. */
        fun forInstrs(instrs: List<Any>, stmtLocs: BcStatementLocations): BcInstrs = BcInstrs(instrs, stmtLocs)
    }

    /** Get a pointer to the start of the instruction buffer. */
    fun startPtr(): BcPtrAddr = BcPtrAddr.forSliceStart(instrs)

    /**
     * Get the end address of the instruction buffer.
     *
     * In Rust: `BcAddr(self.instrs.len().checked_mul(mem::size_of::<u64>()).unwrap().try_into().unwrap())`
     * In Kotlin, each element in the list represents one instruction slot.
     */
    fun end(): BcAddr = BcAddr(instrs.size.toUInt())

    /** Get a pointer to the end of the instruction buffer. */
    fun endPtr(): BcPtrAddr = startPtr().offset(end())

    /**
     * Get all opcodes in the instruction buffer.
     *
     * In Rust, this is `#[cfg(test)]`. Kept for testing and debugging.
     */
    fun opcodes(): List<BcOpcode> {
        val result = mutableListOf<BcOpcode>()
        val endAddr = BcPtrAddr.forSliceEnd(instrs)
        var ptr = BcPtrAddr.forSliceStart(instrs)
        while (ptr != endAddr) {
            require(ptr < endAddr)
            val opcode = getOpcodeAt(ptr)
            result.add(opcode)
            ptr = ptr.add(opcode.sizeOfRepr())
        }
        return result
    }

    /**
     * Iterate over all instructions, yielding `(ptr, ip)` pairs.
     *
     * Each pair contains the pointer to the instruction and its address
     * relative to the start of the buffer.
     */
    private fun iter(): Sequence<Pair<BcPtrAddr, BcAddr>> {
        val start = startPtr()
        val end = endPtr()
        return sequence {
            var nextPtr = start
            while (true) {
                require(nextPtr <= end)
                if (nextPtr == end) break
                val ptr = nextPtr
                val ip = ptr.offsetFrom(start)
                nextPtr = nextPtr.add(getOpcodeAt(ptr).sizeOfRepr())
                yield(Pair(ptr, ip))
            }
        }
    }

    /**
     * Find the [BcInstrEndArg] from the `End` instruction, if present.
     *
     * In Rust: `self.iter().find_map(|(ptr, _ip)| ptr.get_instr_checked::<InstrEnd>().map(|i| &i.arg))`
     */
    private fun endArg(): BcInstrEndArg? {
        for ((ptr, _) in iter()) {
            if (getOpcodeAt(ptr) == BcOpcode.End) {
                return getArgAt(ptr) as? BcInstrEndArg
            }
        }
        return null
    }

    /**
     * Format the instructions for display.
     *
     * When [newline] is true, each instruction is on its own line with IP padding
     * and statement location comments. When false, instructions are separated by `"; "`.
     */
    internal fun fmtImpl(sb: StringBuilder, newline: Boolean) {
        val endArg = endArg()
        val ipPad =
            if (newline) {
                val maxIp = iter().maxOfOrNull { (_, ip) -> ip.value } ?: 0u
                maxIp.toString().length
            } else {
                0
            }

        val loopEnds = mutableListOf<BcAddr>()
        val jumpTargets = mutableSetOf<BcAddr>()
        for ((ptr, ip) in iter()) {
            visitJumpAddr(ptr, ip) { jumpAddr ->
                jumpTargets.add(jumpAddr)
            }
        }
        for ((ptr, ip) in iter()) {
            if (loopEnds.lastOrNull() == ip) {
                loopEnds.removeLast()
            }
            val loopPadCount = loopEnds.size * 2

            if (newline) {
                val stmtAt = stmtLocs.stmtAt(ip)
                if (stmtAt != null) {
                    val (loc, _) = stmtAt
                    sb.appendLine(
                        "${" ".repeat(loopPadCount)} ${" ".repeat(ipPad)}  # ${loc.span}",
                    )
                }
            } else {
                if (ptr != startPtr()) {
                    sb.append("; ")
                }
            }

            val opcode = getOpcodeAt(ptr)
            if (jumpTargets.isNotEmpty()) {
                if (jumpTargets.contains(ip)) {
                    sb.append(">")
                } else if (newline) {
                    sb.append(" ")
                }
            }
            if (newline) {
                sb.append(" ".repeat(loopPadCount))
            }
            sb.append("${ip.value.toString().padStart(ipPad)}: $opcode")
            if (opcode != BcOpcode.End) {
                // `End` args are too verbose and not really instruction args.
                fmtAppendArg(ptr, ip, endArg, sb)
            }
            if (newline) {
                sb.appendLine()
            }
            if (opcode == BcOpcode.Iter) {
                // loop_ends.push(ip.offset(for_loop.arg.4));
                val iterOffset = getIterForwardOffset(ptr)
                if (iterOffset != null) {
                    loopEnds.add(ip.offset(iterOffset))
                }
            }
        }
    }

    /** Dump instructions in debug (multiline) format. */
    internal fun dumpDebug(): String {
        val sb = StringBuilder()
        fmtImpl(sb, true)
        return sb.toString()
    }

    //     self.fmt_impl(f, false)
    override fun toString(): String {
        val sb = StringBuilder()
        fmtImpl(sb, false)
        return sb.toString()
    }

    // --- Private helpers for buffer access ---

    /**
     * Get the opcode at the given pointer address in the instruction buffer.
     *
     * In Rust, this reads the [BcInstrHeader] from the raw byte buffer via pointer cast.
     * In Kotlin, instructions are stored as header/arg pairs in a list.
     */
    internal fun getOpcodeAt(ptr: BcPtrAddr): BcOpcode {
        val idx = ptr.offset
        if (idx >= instrs.size) return BcOpcode.End
        val element = instrs[idx]
        if (element is BcInstrHeader) return element.opcode
        return BcOpcode.End
    }

    /**
     * Get the argument at the given pointer address in the instruction buffer.
     *
     * The argument is stored at the index immediately following the header.
     */
    internal fun getArgAt(ptr: BcPtrAddr): Any? {
        val idx = ptr.offset + 1
        if (idx >= instrs.size) return null
        return instrs[idx]
    }

    /**
     * Get the forward-jump offset from an `InstrIter` arg.
     *
     * In Rust: `ptr.get_instr::<InstrIter>().arg.4` (the 5th tuple element).
     * The iter arg is stored as a list: `[over, loopDepth, iterSlot, varSlot, BcAddrOffset]`.
     */
    private fun getIterForwardOffset(ptr: BcPtrAddr): BcAddrOffset? {
        val arg = getArgAt(ptr) ?: return null
        if (arg is List<*> && arg.size >= 5) {
            val offset = arg[4]
            if (offset is BcAddrOffset) return offset
        }
        return null
    }

    /**
     * Visit all jump addresses referenced by the instruction at [ptr] with address [ip].
     *
     * Calls [visitor] for each target address the instruction may jump to.
     * Used for building the set of jump targets for display formatting.
     *
     * In Rust: `ptr.get_opcode().visit_jump_addr(ptr, ip, &mut |jump_addr| { ... })`
     */
    private fun visitJumpAddr(ptr: BcPtrAddr, ip: BcAddr, visitor: (BcAddr) -> Unit) {
        val opcode = getOpcodeAt(ptr)
        val arg = getArgAt(ptr) ?: return

        when (opcode) {
            BcOpcode.Br -> {
                if (arg is BcAddrOffset && arg != BcAddrOffset.FORWARD) {
                    visitor(ip.offset(arg))
                }
            }
            BcOpcode.IfBr, BcOpcode.IfNotBr -> {
                if (arg is Pair<*, *>) {
                    val offset = arg.second
                    if (offset is BcAddrOffset && offset != BcAddrOffset.FORWARD) {
                        visitor(ip.offset(offset))
                    }
                }
            }
            BcOpcode.Iter, BcOpcode.Continue -> {
                if (arg is List<*> && arg.size >= 5) {
                    val offset = arg[4]
                    if (offset is BcAddrOffset && offset != BcAddrOffset.FORWARD) {
                        visitor(ip.offset(offset))
                    }
                }
            }
            BcOpcode.Break -> {
                if (arg is Pair<*, *>) {
                    val offset = arg.second
                    if (offset is BcAddrOffset && offset != BcAddrOffset.FORWARD) {
                        visitor(ip.offset(offset))
                    }
                }
            }
            else -> { /* Not a jump instruction */ }
        }
    }

    /**
     * Append a formatted representation of the instruction argument to [sb].
     *
     * In Rust: `opcode.fmt_append_arg(ptr, ip, end_arg, f)` dispatches through
     * the opcode handler to format the argument via its `Debug` impl.
     */
    private fun fmtAppendArg(
        ptr: BcPtrAddr,
        _ip: BcAddr,
        endArg: BcInstrEndArg?,
        sb: StringBuilder,
    ) {
        val arg = getArgAt(ptr) ?: return
        sb.append(" ")
        sb.append(formatInstrArg(arg, endArg))
    }
}

// --- PatchAddr ---

/**
 * Address to be patched later with the actual target address.
 *
 * Used during bytecode writing for forward jumps where the target
 * address is not yet known at write time.
 */
class PatchAddr(
    val instrStart: BcAddr,
    val arg: BcAddr,
)

// --- BcInstrsWriter ---

/**
 * Raw instructions writer.
 *
 * Higher level wrapper is [BcWriter].
 *
 * In Rust, this writes into a `Vec<u64>` buffer with raw pointer arithmetic.
 * The `Drop` impl calls `drop_instrs`. In Kotlin, we use a [MutableList]
 * where each instruction is stored as a header/arg pair of objects, and the
 * garbage collector handles cleanup.
 */
class BcInstrsWriter {
    internal val instrs: MutableList<Any> = mutableListOf()

    // In Kotlin, GC handles cleanup.

    companion object {
        fun new(): BcInstrsWriter = BcInstrsWriter()
    }

    /** Current number of elements in the instructions list. */
    fun instrsSize(): Int = instrs.size

    /**
     * Length of instructions buffer.
     *
     * In Rust: `self.instrs.len().checked_mul(mem::size_of::<u64>()).unwrap()`
     * In Kotlin, each list element is one instruction slot.
     */
    private fun instrsLenBytes(): Int = instrs.size

    /**
     * Current instruction pointer (address of next instruction to be written).
     *
     * In Rust: `BcAddr(self.instrs_len_bytes().try_into().unwrap())`
     */
    fun ip(): BcAddr = BcAddr(instrsLenBytes().toUInt())

    /**
     * Write an instruction with the given header and argument.
     *
     * In Rust, this creates a `BcInstrRepr<I>`, copies it into the `u64` buffer via
     * `ptr::write`, and returns `(ip, &(*ptr).arg)`. In Kotlin, we add the header
     * and arg to the list and return the instruction address.
     */
    fun write(header: BcInstrHeader, arg: Any): BcAddr {
        // assert!(mem::size_of_val(&repr).is_multiple_of(mem::size_of::<u64>()));
        // self.instrs.resize(..., 0);
        //     (ip, &(*ptr).arg)
        val instrIp = ip()
        instrs.add(header)
        instrs.add(arg)
        return instrIp
    }

    /**
     * Create a [PatchAddr] for a forward jump that needs to be patched later.
     *
     * In Rust, this computes the byte offset of [addr] within the `instrs` buffer and
     * asserts that it currently holds [BcAddrOffset.FORWARD]. In Kotlin, we use the
     * list index of the element to be patched.
     *
     * @param instrStart the start address of the instruction containing the jump offset.
     * @param argIndex the index in the instrs list of the offset to be patched.
     */
    fun addrToPatch(instrStart: BcAddr, argIndex: Int): PatchAddr {
        // assert!((offset_bytes as usize) < self.instrs_len_bytes());
        return PatchAddr(
            instrStart = instrStart,
            arg = BcAddr(argIndex.toUInt()),
        )
    }

    /**
     * Patch a previously written forward jump address with the current IP.
     *
     * In Rust, this writes `self.ip().offset_from(addr.instr_start)` into the raw
     * buffer at the address stored in `addr.arg` and asserts the old value was
     * [BcAddrOffset.FORWARD] and the new offset is aligned to [BC_INSTR_ALIGN].
     *
     * In Kotlin, we update the list entry at the patch index.
     */
    fun patchAddr(addr: PatchAddr) {
        // In Rust, the patch writes directly into the raw byte buffer at the
        // address of the BcAddrOffset field inside the compound instruction arg.
        // In Kotlin, instruction args are boxed objects (Pair, List, or bare
        // BcAddrOffset). We reconstruct the containing object with the patched
        // offset replacing the BcAddrOffset.FORWARD sentinel.
        val index = addr.arg.value.toInt()
        if (index >= instrs.size) return
        val newOffset = ip().offsetFrom(addr.instrStart)
        val existing = instrs[index]
        instrs[index] =
            when (existing) {
                is BcAddrOffset -> {
                    check(existing == BcAddrOffset.FORWARD) { "Expected FORWARD, got $existing" }
                    newOffset
                }
                is Pair<*, *> -> {
                    check(existing.second == BcAddrOffset.FORWARD) { "Expected FORWARD in Pair.second, got ${existing.second}" }
                    Pair(existing.first, newOffset)
                }
                is List<*> -> {
                    val mutable = existing.toMutableList()
                    val fwdIndex = mutable.indexOfLast { it == BcAddrOffset.FORWARD }
                    check(fwdIndex >= 0) { "Expected FORWARD in List, got $existing" }
                    mutable[fwdIndex] = newOffset
                    mutable
                }
                else -> error("patchAddr: unexpected arg type: ${existing::class.simpleName}")
            }
    }

    /**
     * Finish writing instructions.
     *
     * Appends the `End` instruction with metadata ([BcInstrEndArg]) containing all
     * slow args, statement locations, and local names, then returns the completed [BcInstrs].
     *
     * In Rust, `mem::take` is used to extract `instrs` because `Self` has `Drop`,
     * preventing direct field moves. The buffer is then boxed and verified to be aligned.
     * In Kotlin, we just take a snapshot of the list.
     */
    fun finish(
        slowArgs: MutableList<Pair<BcAddr, BcInstrSlowArg>>,
        stmtLocs: BcStatementLocations,
        localNames: List<io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue>,
    ): BcInstrs {
        // self.write::<InstrEnd>(BcInstrEndArg {
        //     end_addr: self.ip(),
        //     slow_args,
        //     local_names,
        // });
        write(
            BcInstrHeader.forOpcode(BcOpcode.End),
            BcInstrEndArg(
                endAddr = ip(),
                slowArgs = slowArgs,
                localNames =
                    io.github.kotlinmania.starlark.values.FrozenRef
                        .new(localNames),
            ),
        )
        // assert!((instrs.as_ptr() as usize).is_multiple_of(BC_INSTR_ALIGN));
        return BcInstrs.forInstrs(instrs.toList(), stmtLocs)
    }
}

// --- Instruction argument formatting helpers ---

/**
 * Format an instruction argument for display.
 *
 * Handles common arg patterns: pairs, lists, and scalar values.
 * When [endArg] provides local names, slot references are annotated with variable names.
 */
private fun formatInstrArg(arg: Any?, endArg: BcInstrEndArg?): String {
    if (arg == null) return ""
    return when (arg) {
        is Pair<*, *> -> {
            "${formatInstrArg(arg.first, endArg)} ${formatInstrArg(arg.second, endArg)}"
        }
        is SlotRangeTargetArg -> {
            "${formatInstrArg(arg.values, endArg)} ${formatInstrArg(arg.target, endArg)}"
        }
        is List<*> -> {
            arg.joinToString(" ") { formatInstrArg(it, endArg) }
        }
        is BcSlotIn -> formatSlotWithName(arg.get(), endArg)
        is BcSlotOut -> "->${formatSlotWithName(arg.get(), endArg)}"
        is BcSlot -> formatSlotWithName(arg, endArg)
        is BcAddrOffset -> "@${arg.value}"
        is BcAddrOffsetNeg -> "-@${arg.value}"
        else -> arg.toString()
    }
}

/**
 * Format a slot reference, annotating it with the local variable name if available
 * from the End instruction's [BcInstrEndArg.localNames].
 */
private fun formatSlotWithName(slot: BcSlot, endArg: BcInstrEndArg?): String {
    val names = endArg?.localNames?.asRef()
    val idx = slot.index.toInt()
    return if (names != null && idx < names.size) {
        "&${names[idx]}"
    } else {
        slot.toString()
    }
}
