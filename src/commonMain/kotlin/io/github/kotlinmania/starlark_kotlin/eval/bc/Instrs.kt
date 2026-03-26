// port-lint: source src/eval/bc/instrs.rs
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

/**
 * Instructions serialized in a buffer.
 *
 * In Rust, instructions are serialized into a u64-aligned byte array and accessed via raw pointer
 * arithmetic with manual drop. In Kotlin, we use a list-of-objects representation where each
 * instruction is stored as a (BcInstrHeader, arg) pair. The GC handles cleanup.
 */

// impl BcOpcode { unsafe fn drop_in_place(self, ptr: BcPtrAddr) }
/**
 * Drop instruction at given address.
 *
 * In Rust, this dispatches via [BcOpcodeHandler] to call `ptr::drop_in_place` for the
 * concrete instruction repr at [ptr]. In Kotlin, the GC handles cleanup automatically.
 */
private fun BcOpcode.dropInPlace(ptr: BcPtrAddr) {
    // struct HandlerImpl<'b> { ptr: BcPtrAddr<'b> }
    // impl BcOpcodeHandler<()> for HandlerImpl<'_> {
    //     fn handle<I: BcInstr>(self) {
    //         let HandlerImpl { ptr } = self;
    //         let instr = ptr.get_instr_mut::<I>();
    //         unsafe { ptr::drop_in_place(instr); }
    //     }
    // }
    // self.dispatch(HandlerImpl { ptr });
    // No-op in Kotlin: GC handles cleanup.
}

// unsafe fn drop_instrs(instrs: &[u64])
/**
 * Invoke drop for instructions in the buffer.
 *
 * In Rust, this walks the raw byte buffer calling [BcOpcode.dropInPlace] for each instruction.
 * In Kotlin, the GC handles all cleanup, so this is a no-op.
 */
private fun dropInstrs(instrs: List<Any>) {
    // unsafe {
    //     let end = BcPtrAddr::for_slice_end(instrs);
    //     let mut ptr = BcPtrAddr::for_slice_start(instrs);
    //     while ptr != end {
    //         assert!(ptr < end);
    //         let opcode = ptr.get_opcode();
    //         opcode.drop_in_place(ptr);
    //         ptr = ptr.add(opcode.size_of_repr());
    //     }
    // }
    // No-op in Kotlin: GC handles cleanup.
}

// fn empty_instrs() -> &'static [u64]
/**
 * Statically allocate a valid instruction buffer micro-optimization.
 *
 * Valid bytecode must end with `End` instruction, otherwise evaluation overruns
 * the instruction buffer.
 *
 * [BcInstrs] type needs to have a default (it is convenient).
 *
 * In Rust, allocating a vec in `BcInstrs::default` is non-free, so a static `BcInstrRepr<InstrEnd>`
 * is reinterpreted as a `&'static [u64]`. In Kotlin, we simply create a small list with the
 * End instruction header and its argument.
 */
private fun emptyInstrs(): List<Any> {
    // static END_OF_BC: BcInstrRepr<InstrEnd> = BcInstrRepr {
    //     header: BcInstrHeader::for_opcode(BcOpcode::End),
    //     arg: BcInstrEndArg { end_addr: BcAddr(0), slow_args: Vec::new(), local_names: FrozenRef::new(&[]) },
    //     _align: [],
    // };
    return listOf(
        BcInstrHeader.forOpcode(BcOpcode.End),
        BcInstrEndArg(
            endAddr = BcAddr(0u),
            slowArgs = mutableListOf(),
            localNames = io.github.kotlinmania.starlark_kotlin.values.FrozenRef.empty(),
        ),
    )
}

// pub(crate) struct BcInstrs
/**
 * Bytecode instructions container.
 *
 * In Rust, this holds either heap-allocated (`Box<[u64]>`) or statically-allocated (`&'static [u64]`)
 * instruction bytes via `Either`. In Kotlin, we use a [List] since the GC handles allocation
 * and deallocation.
 */
internal class BcInstrs private constructor(
    // instrs: Either<Box<[u64]>, &'static [u64]>
    // We use a list of objects where instructions are stored as header/arg pairs.
    private val instrs: List<Any>,
    // pub(crate) stmt_locs: BcStatementLocations
    internal val stmtLocs: BcStatementLocations,
) {
    // impl Default for BcInstrs
    // fn default() -> Self {
    //     Self::for_instrs(Either::Right(empty_instrs()), BcStatementLocations::new())
    // }

    // impl Drop for BcInstrs
    // In Rust, Drop calls drop_instrs for the heap-allocated (Either::Left) variant.
    // In Kotlin, the GC handles cleanup.

    companion object {
        /** Create a default (empty) [BcInstrs] containing only the End instruction. */
        fun default(): BcInstrs = forInstrs(emptyInstrs(), BcStatementLocations.new())

        // pub(crate) fn for_instrs(instrs: Either<Box<[u64]>, &'static [u64]>, stmt_locs: BcStatementLocations) -> Self
        /** Create [BcInstrs] from instruction buffer and statement locations. */
        fun forInstrs(instrs: List<Any>, stmtLocs: BcStatementLocations): BcInstrs {
            return BcInstrs(instrs, stmtLocs)
        }
    }

    // pub(crate) fn start_ptr(&self) -> BcPtrAddr<'_>
    /** Get a pointer to the start of the instruction buffer. */
    fun startPtr(): BcPtrAddr = BcPtrAddr.forSliceStart(instrs)

    // fn instrs field used via Either in Rust, simple delegations here
    // pub(crate) fn end(&self) -> BcAddr
    /**
     * Get the end address of the instruction buffer.
     *
     * In Rust: `BcAddr(self.instrs.len().checked_mul(mem::size_of::<u64>()).unwrap().try_into().unwrap())`
     * In Kotlin, each element in the list represents one instruction slot.
     */
    fun end(): BcAddr = BcAddr(instrs.size.toUInt())

    // pub(crate) fn end_ptr(&self) -> BcPtrAddr<'_>
    /** Get a pointer to the end of the instruction buffer. */
    fun endPtr(): BcPtrAddr = startPtr().offset(end())

    // #[cfg(test)]
    // pub(crate) fn opcodes(&self) -> Vec<BcOpcode>
    /**
     * Get all opcodes in the instruction buffer.
     * In Rust this is `#[cfg(test)]`.
     */
    fun opcodes(): List<BcOpcode> {
        val result = mutableListOf<BcOpcode>()
        val end = BcPtrAddr.forSliceEnd(instrs)
        var ptr = BcPtrAddr.forSliceStart(instrs)
        while (ptr != end) {
            require(ptr < end)
            val opcode = ptr.getOpcode()
            result.add(opcode)
            ptr = ptr.add(opcode.sizeOfRepr())
        }
        return result
    }

    // fn iter(&self) -> impl Iterator<Item = (BcPtrAddr<'_>, BcAddr)>
    /**
     * Iterate over all instructions, yielding `(ptr, ip)` pairs.
     *
     * Each pair contains the pointer to the instruction and its address relative
     * to the start of the buffer.
     */
    private fun iter(): Sequence<Pair<BcPtrAddr, BcAddr>> {
        return sequence {
            var nextPtr = startPtr()
            val endPtrVal = endPtr()
            while (true) {
                require(nextPtr <= endPtrVal)
                if (nextPtr == endPtrVal) break
                val ptr = nextPtr
                val ip = ptr.offsetFrom(startPtr())
                nextPtr = nextPtr.add(ptr.getOpcode().sizeOfRepr())
                yield(Pair(ptr, ip))
            }
        }
    }

    // fn end_arg(&self) -> Option<&BcInstrEndArg>
    /**
     * Find the [BcInstrEndArg] from the End instruction, if present.
     *
     * Iterates through all instructions looking for the End opcode, then
     * extracts its argument.
     */
    private fun endArg(): BcInstrEndArg? {
        for ((ptr, _) in iter()) {
            if (ptr.getOpcode() == BcOpcode.End) {
                // In Rust: ptr.get_instr_checked::<InstrEnd>().map(|i| &i.arg)
                val argIndex = ptr.offset + 1
                if (argIndex < instrs.size) {
                    val arg = instrs[argIndex]
                    if (arg is BcInstrEndArg) return arg
                }
                return null
            }
        }
        return null
    }

    // pub(crate) fn fmt_impl(&self, f: &mut dyn Write, newline: bool) -> fmt::Result
    /**
     * Format the instructions for display.
     *
     * When [newline] is true, each instruction is on its own line with IP padding
     * and statement location comments. When false, instructions are separated by `"; "`.
     */
    internal fun fmtImpl(sb: StringBuilder, newline: Boolean) {
        val endArg = endArg()
        val ipPad = if (newline) {
            val maxIp = iter().maxOfOrNull { (_, ip) -> ip.value } ?: 0u
            maxIp.toString().length
        } else {
            0
        }

        val loopEnds = mutableListOf<BcAddr>()
        val jumpTargets = mutableSetOf<BcAddr>()
        for ((ptr, ip) in iter()) {
            ptr.getOpcode().visitJumpAddr(ptr, ip) { jumpAddr ->
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
                        "${" ".repeat(loopPadCount)} ${" ".repeat(ipPad)}  # ${loc.span}"
                    )
                }
            } else {
                if (ptr != startPtr()) {
                    sb.append("; ")
                }
            }

            val opcode = ptr.getOpcode()
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
                opcode.fmtAppendArg(ptr, ip, endArg, sb)
            }
            if (newline) {
                sb.appendLine()
            }
            if (opcode == BcOpcode.Iter) {
                // let for_loop = ptr.get_instr::<InstrIter>();
                // loop_ends.push(ip.offset(for_loop.arg.4));
                val iterOffset = getIterForwardOffset(ptr)
                if (iterOffset != null) {
                    loopEnds.add(ip.offset(iterOffset))
                }
            }
        }
    }

    // pub(crate) fn dump_debug(&self) -> String
    /** Dump instructions in debug (multiline) format. */
    internal fun dumpDebug(): String {
        val sb = StringBuilder()
        fmtImpl(sb, true)
        return sb.toString()
    }

    // impl Display for BcInstrs
    // fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result
    override fun toString(): String {
        val sb = StringBuilder()
        fmtImpl(sb, false)
        return sb.toString()
    }

    // --- Private helpers for instruction arg inspection ---

    /**
     * Get the forward-jump offset from an InstrIter arg (the 5th tuple element).
     * Returns null if the arg format doesn't match.
     */
    private fun getIterForwardOffset(ptr: BcPtrAddr): BcAddrOffset? {
        val argIndex = ptr.offset + 1
        if (argIndex >= instrs.size) return null
        val arg = instrs[argIndex]
        // InstrIter arg is typically stored as a list: [over, loopDepth, iterSlot, varSlot, BcAddrOffset]
        if (arg is List<*> && arg.size >= 5) {
            val offset = arg[4]
            if (offset is BcAddrOffset) return offset
        }
        return null
    }
}

// pub(crate) struct PatchAddr
/**
 * Address to be patched later with the actual target address.
 *
 * Used during bytecode writing for forward jumps where the target
 * address is not yet known.
 */
internal class PatchAddr(
    // pub(crate) instr_start: BcAddr
    val instrStart: BcAddr,
    // pub(crate) arg: BcAddr
    val arg: BcAddr,
)

// pub(crate) struct BcInstrsWriter
/**
 * Raw instructions writer.
 *
 * Higher level wrapper is [BcWriter].
 *
 * In Rust, this writes into a `Vec<u64>` buffer with raw pointer arithmetic.
 * In Kotlin, we use a [MutableList] where each instruction is stored as a
 * header/arg pair of objects.
 */
internal class BcInstrsWriter {
    // pub(crate) instrs: Vec<u64>
    internal val instrs: MutableList<Any> = mutableListOf()

    // impl Drop for BcInstrsWriter
    // fn drop(&mut self) { unsafe { drop_instrs(&self.instrs); } }
    // In Kotlin, the GC handles cleanup.

    companion object {
        // pub(crate) fn new() -> BcInstrsWriter
        fun new(): BcInstrsWriter = BcInstrsWriter()
    }

    // fn instrs_len_bytes(&self) -> usize
    /**
     * Length of instructions in bytes.
     *
     * In Rust: `self.instrs.len().checked_mul(mem::size_of::<u64>()).unwrap()`.
     * In Kotlin, each list element represents one instruction slot.
     */
    private fun instrsLenBytes(): Int = instrs.size

    // pub(crate) fn ip(&self) -> BcAddr
    /** Current instruction pointer (address of next instruction to be written). */
    fun ip(): BcAddr = BcAddr(instrsLenBytes().toUInt())

    // pub(crate) fn write<I: BcInstr>(&mut self, arg: I::Arg) -> (BcAddr, *const I::Arg)
    /**
     * Write an instruction with the given header and argument.
     *
     * In Rust, this creates a `BcInstrRepr<I>`, copies it into the u64 buffer,
     * and returns `(ip, ptr_to_arg)`. In Kotlin, we add the header and arg
     * to the list and return the instruction address.
     */
    fun write(header: BcInstrHeader, arg: Any): BcAddr {
        val instrIp = ip()
        instrs.add(header)
        instrs.add(arg)
        return instrIp
    }

    // pub(crate) fn addr_to_patch(&self, instr_start: BcAddr, addr: *const BcAddrOffset) -> PatchAddr
    /**
     * Create a [PatchAddr] for a forward jump that needs to be patched later.
     *
     * In Rust, this takes a raw pointer to the `BcAddrOffset` field and computes
     * its byte offset in the buffer. In Kotlin, we use the list index.
     *
     * @param instrStart the start address of the instruction containing the jump offset.
     * @param argIndex the index in the instrs list of the offset to be patched.
     */
    fun addrToPatch(instrStart: BcAddr, argIndex: Int): PatchAddr {
        // unsafe { assert_eq!(*addr, BcAddrOffset::FORWARD) };
        // let offset_bytes = unsafe { (addr as *const u8).offset_from(self.instrs.as_ptr() as *const u8) };
        // assert!((offset_bytes as usize) < self.instrs_len_bytes());
        return PatchAddr(
            instrStart = instrStart,
            arg = BcAddr(argIndex.toUInt()),
        )
    }

    // pub(crate) fn patch_addr(&mut self, addr: PatchAddr)
    /**
     * Patch a previously written forward jump address with the current IP.
     *
     * In Rust, this writes the offset from instr_start to current IP into the raw
     * buffer at the address stored in [PatchAddr.arg]. In Kotlin, we update
     * the list entry at the patch index.
     */
    fun patchAddr(addr: PatchAddr) {
        // unsafe {
        //     let mem_addr = (self.instrs.as_mut_ptr() as *mut u8).add(addr.arg.0 as usize) as *mut BcAddrOffset;
        //     assert!(*mem_addr == BcAddrOffset::FORWARD);
        //     *mem_addr = self.ip().offset_from(addr.instr_start);
        //     debug_assert!(((*mem_addr).0 as usize).is_multiple_of(BC_INSTR_ALIGN));
        // }
        val index = addr.arg.value.toInt()
        if (index < instrs.size) {
            instrs[index] = ip().offsetFrom(addr.instrStart)
        }
    }

    // pub(crate) fn finish(mut self, slow_args, stmt_locs, local_names) -> BcInstrs
    /**
     * Finish writing instructions.
     *
     * Appends the End instruction with metadata ([BcInstrEndArg]) and
     * returns the completed [BcInstrs].
     *
     * In Rust, `mem::take` is used to extract `instrs` because `Self` has Drop, so
     * the field cannot be moved out directly. In Kotlin, we just copy the list.
     */
    fun finish(
        slowArgs: MutableList<Pair<BcAddr, BcInstrSlowArg>>,
        stmtLocs: BcStatementLocations,
        localNames: List<String>,
    ): BcInstrs {
        // self.write::<InstrEnd>(BcInstrEndArg { ... });
        write(
            BcInstrHeader.forOpcode(BcOpcode.End),
            BcInstrEndArg(
                endAddr = ip(),
                slowArgs = slowArgs,
                localNames = io.github.kotlinmania.starlark_kotlin.values.FrozenRef.new(
                    localNames.map {
                        io.github.kotlinmania.starlark_kotlin.values.types.string.intern.FrozenStringValue(it)
                    }
                ),
            ),
        )
        // let instrs = mem::take(&mut self.instrs);
        // let instrs = instrs.into_boxed_slice();
        // assert!((instrs.as_ptr() as usize).is_multiple_of(BC_INSTR_ALIGN));
        // BcInstrs::for_instrs(Either::Left(instrs), stmt_locs)
        return BcInstrs.forInstrs(instrs.toList(), stmtLocs)
    }
}

// --- Extension helpers for formatting and jump address visiting ---

// BcPtrAddr helper: get the opcode at the current instruction pointer.
// In the list-based buffer, even indices are headers, odd indices are args.
/**
 * Get the opcode at the current instruction pointer.
 *
 * In Rust, this reads the [BcInstrHeader] from the raw byte buffer.
 * In our list-based buffer, instruction headers are at even indices.
 */
internal fun BcPtrAddr.getOpcode(): BcOpcode {
    val buffer = this.getBuffer() ?: return BcOpcode.End
    val idx = this.offset
    if (idx >= buffer.size) return BcOpcode.End
    val element = buffer[idx]
    if (element is BcInstrHeader) return element.opcode
    return BcOpcode.End
}

/**
 * Visit all jump addresses referenced by the instruction at [ptr] with address [ip].
 *
 * Calls [visitor] for each target address the instruction may jump to.
 * Used for building the set of jump targets for display formatting.
 */
internal fun BcOpcode.visitJumpAddr(ptr: BcPtrAddr, ip: BcAddr, visitor: (BcAddr) -> Unit) {
    val buffer = ptr.getBuffer() ?: return
    val argIdx = ptr.offset + 1
    if (argIdx >= buffer.size) return
    val arg = buffer[argIdx]

    // Extract BcAddrOffset values from instruction args for jump instructions.
    when (this) {
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
 * the opcode handler to format the argument. In Kotlin, we inspect the arg
 * object and render it generically.
 */
internal fun BcOpcode.fmtAppendArg(
    ptr: BcPtrAddr,
    ip: BcAddr,
    endArg: BcInstrEndArg?,
    sb: StringBuilder,
) {
    val buffer = ptr.getBuffer() ?: return
    val argIdx = ptr.offset + 1
    if (argIdx >= buffer.size) return
    val arg = buffer[argIdx]

    sb.append(" ")
    sb.append(formatInstrArg(arg, endArg))
}

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
 * from the End instruction's local_names.
 */
private fun formatSlotWithName(slot: BcSlot, endArg: BcInstrEndArg?): String {
    val names = endArg?.localNames?.value
    val idx = slot.value.toInt()
    return if (names != null && idx < names.size) {
        "&${names[idx]}"
    } else {
        slot.toString()
    }
}

// --- BcPtrAddr buffer access helpers ---

/**
 * Get the instruction buffer associated with this [BcPtrAddr].
 *
 * In Rust, `BcPtrAddr` is a raw pointer into a byte buffer. In Kotlin, we need
 * to carry the buffer reference for element access. We store it via the companion
 * object's `forSliceStart`/`forSliceEnd` factory methods which set a thread-local
 * or carry the reference in a wrapper.
 *
 * This is a best-effort accessor that works with the list-based instruction buffers
 * stored internally in [BcInstrs].
 */
@Suppress("NOTHING_TO_INLINE")
private inline fun BcPtrAddr.getBuffer(): List<Any>? {
    // The BcPtrAddr from Addr.kt is a data class and doesn't carry a buffer reference.
    // In practice, getOpcode/fmtAppendArg/visitJumpAddr are only called from within BcInstrs
    // methods which have access to the instrs list. We use a thread-local for the buffer.
    return currentInstrBuffer.get()
}

/**
 * Thread-local holding the current instruction buffer for [BcPtrAddr] operations.
 *
 * Set by [BcInstrs] methods before calling extension functions that need buffer access.
 * This avoids needing to modify the [BcPtrAddr] data class.
 */
private val currentInstrBuffer = InstrBufferHolder()

/**
 * Holder for the current instruction buffer.
 * Uses a simple nullable field since Kotlin Multiplatform does not have `ThreadLocal`.
 */
private class InstrBufferHolder {
    private var buffer: List<Any>? = null

    fun get(): List<Any>? = buffer
    fun set(value: List<Any>?) { buffer = value }
}

/**
 * Execute [block] with the instruction buffer set as the current buffer
 * for [BcPtrAddr] extension functions.
 */
internal inline fun <R> withInstrBuffer(buffer: List<Any>, block: () -> R): R {
    val prev = currentInstrBuffer.get()
    currentInstrBuffer.set(buffer)
    try {
        return block()
    } finally {
        currentInstrBuffer.set(prev)
    }
}

// --- BcPtrAddr.Companion extension for List<Any> ---

/**
 * Create a [BcPtrAddr] for the start of an instruction list.
 *
 * This overload works with `List<Any>` instruction buffers (as opposed to `LongArray`).
 */
internal fun BcPtrAddr.Companion.forSliceStart(instrs: List<Any>): BcPtrAddr {
    currentInstrBuffer.set(instrs)
    return BcPtrAddr(
        offset = 0,
        range = IfDebug.new(BcPtrRange(start = 0, len = instrs.size)),
    )
}

/**
 * Create a [BcPtrAddr] for the end of an instruction list.
 *
 * This overload works with `List<Any>` instruction buffers (as opposed to `LongArray`).
 */
internal fun BcPtrAddr.Companion.forSliceEnd(instrs: List<Any>): BcPtrAddr {
    currentInstrBuffer.set(instrs)
    return BcPtrAddr(
        offset = instrs.size,
        range = IfDebug.new(BcPtrRange(start = 0, len = instrs.size)),
    )
}
