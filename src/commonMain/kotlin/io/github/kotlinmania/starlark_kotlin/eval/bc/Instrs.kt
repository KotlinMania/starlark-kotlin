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

/** Instructions serialized in a buffer. */

// In Rust, instructions are serialized into a u64-aligned byte buffer and accessed via raw pointer
// arithmetic. In Kotlin, we use a list-of-objects representation where each instruction is stored
// as a (BcInstrHeader, arg) pair. The GC handles cleanup, so Drop semantics are not needed.

/**
 * Drop instruction at given address.
 *
 * In Rust this invokes destructors via raw pointer traversal using [BcOpcode.dispatch].
 * In Kotlin, the GC handles all cleanup, so this is a no-op.
 */
private fun BcOpcode.dropInPlace(ptr: BcPtrAddr) {
    // In Rust, this dispatches to ptr::drop_in_place for each instruction's BcInstrRepr.
    // In Kotlin/Multiplatform, garbage collection handles memory cleanup automatically.
}

/**
 * Invoke drop for instructions in the buffer.
 *
 * In Rust this walks the raw byte buffer calling drop_in_place for each instruction.
 * In Kotlin, the GC handles all cleanup, so this is a no-op.
 */
private fun dropInstrs(instrs: List<Any>) {
    // In Rust, this iterates through the u64 slice calling opcode.drop_in_place(ptr)
    // for each instruction. In Kotlin, GC handles cleanup automatically.
}

/**
 * Statically allocate a valid instruction buffer micro-optimization.
 *
 * Valid bytecode must end with `End` instruction, otherwise evaluation overruns
 * the instruction buffer.
 *
 * [BcInstrs] type needs to have a default (it is convenient).
 *
 * In Rust, allocating a vec in `BcInstrs::default` is non-free, so a static allocation
 * is used. In Kotlin, we simply create a small list with the End instruction.
 */
private fun emptyInstrs(): List<Any> {
    return listOf(
        BcInstrHeader.forOpcode(BcOpcode.End),
        BcInstrEndArg(
            endAddr = BcAddr(0u),
            slowArgs = mutableListOf(),
            localNames = io.github.kotlinmania.starlark_kotlin.values.FrozenRef.empty(),
        ),
    )
}

/**
 * Bytecode instructions container.
 *
 * In Rust, this holds either heap-allocated (`Box<[u64]>`) or statically-allocated (`&'static [u64]`)
 * instruction bytes via `Either`. In Kotlin, we use a simple [List] since the GC handles allocation.
 */
internal class BcInstrs private constructor(
    // In Rust: Either<Box<[u64]>, &'static [u64]>.
    // We use a list of objects where instructions are stored as header/arg pairs.
    private val instrs: List<Any>,
    internal val stmtLocs: BcStatementLocations,
) {
    // impl Default for BcInstrs
    // In Rust, Drop calls drop_instrs for heap-allocated variant.
    // In Kotlin, GC handles cleanup.

    companion object {
        /** Create a default (empty) [BcInstrs] containing only the End instruction. */
        fun default(): BcInstrs = forInstrs(emptyInstrs(), BcStatementLocations.new())

        /**
         * Create [BcInstrs] from a list of instruction objects and statement locations.
         *
         * In Rust: `fn for_instrs(instrs: Either<Box<[u64]>, &'static [u64]>, stmt_locs: BcStatementLocations) -> Self`
         */
        fun forInstrs(instrs: List<Any>, stmtLocs: BcStatementLocations): BcInstrs {
            return BcInstrs(instrs, stmtLocs)
        }
    }

    /** Get a pointer to the start of the instruction buffer. */
    fun startPtr(): BcPtrAddr = BcPtrAddr.forSliceStart(instrs)

    /**
     * Get the end address of the instruction buffer.
     *
     * In Rust this computes `instrs.len() * size_of::<u64>()` and converts to [BcAddr].
     * In Kotlin, we use the list size directly since each element represents one "slot".
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

    /**
     * Iterate over all instructions, yielding `(ptr, ip)` pairs.
     *
     * In Rust: `fn iter(&self) -> impl Iterator<Item = (BcPtrAddr<'_>, BcAddr)>`
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

    /**
     * Find the [BcInstrEndArg] from the End instruction, if present.
     *
     * In Rust: `fn end_arg(&self) -> Option<&BcInstrEndArg>`
     */
    private fun endArg(): BcInstrEndArg? {
        for ((ptr, _) in iter()) {
            if (ptr.getOpcode() == BcOpcode.End) {
                // In Rust: ptr.get_instr_checked::<InstrEnd>().map(|i| &i.arg)
                // We look at the next element in the buffer for the arg.
                val argIndex = ptr.index() + 1
                if (argIndex < instrs.size) {
                    val arg = instrs[argIndex]
                    if (arg is BcInstrEndArg) return arg
                }
                return null
            }
        }
        return null
    }

    /**
     * Format the instructions for display.
     *
     * When [newline] is true, each instruction is on its own line with IP padding
     * and statement location comments. When false, instructions are separated by "; ".
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
                // In Rust: let for_loop = ptr.get_instr::<InstrIter>();
                //          loop_ends.push(ip.offset(for_loop.arg.4));
                val iterArg = ptr.getIterArg()
                if (iterArg != null) {
                    loopEnds.add(ip.offset(iterArg))
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

    // impl Display for BcInstrs
    override fun toString(): String {
        val sb = StringBuilder()
        fmtImpl(sb, false)
        return sb.toString()
    }
}

/**
 * Address to be patched later with the actual target address.
 *
 * Used during bytecode writing for forward jumps where the target
 * address is not yet known.
 */
internal class PatchAddr(
    val instrStart: BcAddr,
    val arg: BcAddr,
)

/**
 * Raw instructions writer.
 *
 * Higher level wrapper is [BcWriter].
 *
 * In Rust, this writes into a `Vec<u64>` buffer with raw pointer arithmetic.
 * In Kotlin, we use a `MutableList<Any>` where each instruction is stored
 * as a header/arg pair.
 */
internal class BcInstrsWriter {
    // In Rust: pub(crate) instrs: Vec<u64>
    internal val instrs: MutableList<Any> = mutableListOf()

    // In Rust, Drop calls drop_instrs. In Kotlin, GC handles cleanup.

    companion object {
        fun new(): BcInstrsWriter = BcInstrsWriter()
    }

    /**
     * Length of instructions in bytes.
     *
     * In Rust: `fn instrs_len_bytes(&self) -> usize` returns `self.instrs.len() * size_of::<u64>()`.
     * In Kotlin, each list element represents one instruction "slot".
     */
    private fun instrsLenBytes(): Int = instrs.size

    /** Current instruction pointer (address of next instruction to be written). */
    fun ip(): BcAddr = BcAddr(instrsLenBytes().toUInt())

    /**
     * Write an instruction with the given header and argument.
     *
     * In Rust: `fn write<I: BcInstr>(&mut self, arg: I::Arg) -> (BcAddr, *const I::Arg)`
     *
     * Returns the address of the written instruction.
     */
    fun write(header: BcInstrHeader, arg: Any): BcAddr {
        val instrIp = ip()
        instrs.add(header)
        instrs.add(arg)
        return instrIp
    }

    /**
     * Create a [PatchAddr] for a forward jump that needs to be patched later.
     *
     * In Rust: `fn addr_to_patch(&self, instr_start: BcAddr, addr: *const BcAddrOffset) -> PatchAddr`
     *
     * @param instrStart the start address of the instruction containing the jump offset.
     * @param argIndex the index in the instrs list of the offset to be patched.
     */
    fun addrToPatch(instrStart: BcAddr, argIndex: Int): PatchAddr {
        return PatchAddr(
            instrStart = instrStart,
            arg = BcAddr(argIndex.toUInt()),
        )
    }

    /**
     * Patch a previously written forward jump address with the current IP.
     *
     * In Rust: `fn patch_addr(&mut self, addr: PatchAddr)` writes the offset
     * from instr_start to current IP into the raw buffer. In Kotlin, we update
     * the list entry at the patch index.
     */
    fun patchAddr(addr: PatchAddr) {
        val index = addr.arg.value.toInt()
        if (index < instrs.size) {
            instrs[index] = ip().offsetFrom(addr.instrStart)
        }
    }

    /**
     * Finish writing instructions.
     *
     * Appends the [InstrEnd] instruction with metadata ([BcInstrEndArg]) and
     * returns the completed [BcInstrs].
     *
     * In Rust: `fn finish(mut self, slow_args, stmt_locs, local_names) -> BcInstrs`
     * In Rust, `mem::take` is used because Self has Drop. In Kotlin, we just copy the list.
     */
    fun finish(
        slowArgs: MutableList<Pair<BcAddr, BcInstrSlowArg>>,
        stmtLocs: BcStatementLocations,
        localNames: List<String>,
    ): BcInstrs {
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
        return BcInstrs.forInstrs(instrs.toList(), stmtLocs)
    }
}

// --- Extension helpers for BcPtrAddr to work with the list-based instruction buffer ---

/**
 * Get the list index corresponding to this [BcPtrAddr].
 *
 * Since instructions are stored as (header, arg) pairs, the offset maps to a list index.
 */
private fun BcPtrAddr.index(): Int = this.offset

/**
 * Get the opcode at the current pointer address in the instruction buffer.
 *
 * Reads the [BcInstrHeader] from the buffer and returns its opcode.
 */
internal fun BcPtrAddr.getOpcode(): BcOpcode {
    val buffer = this.buffer ?: return BcOpcode.End
    val idx = index()
    if (idx >= buffer.size) return BcOpcode.End
    val header = buffer[idx]
    if (header is BcInstrHeader) return header.opcode
    return BcOpcode.End
}

/**
 * Get the argument for an Iter instruction, returning the forward-jump offset
 * (the 5th element of the InstrIter arg tuple).
 *
 * Returns null if the argument cannot be read.
 */
private fun BcPtrAddr.getIterArg(): BcAddrOffset? {
    val buffer = this.buffer ?: return null
    val idx = index() + 1
    if (idx >= buffer.size) return null
    val arg = buffer[idx]
    // InstrIter arg is typically a list: [over, loopDepth, iterSlot, varSlot, BcAddrOffset]
    if (arg is List<*> && arg.size >= 5) {
        val offset = arg[4]
        if (offset is BcAddrOffset) return offset
    }
    return null
}

/**
 * Get the [InstrEnd] instruction argument at the current pointer.
 *
 * Returns the [BcInstrEndArg] if the instruction at this address is End.
 */
internal fun BcPtrAddr.getInstrEndArg(): BcInstrEndArg? {
    val buffer = this.buffer ?: return null
    val idx = index() + 1
    if (idx >= buffer.size) return null
    val arg = buffer[idx]
    if (arg is BcInstrEndArg) return arg
    return null
}

// --- Extension functions on BcPtrAddr for buffer access ---

/**
 * Create a [BcPtrAddr] for the start of an instruction list.
 *
 * This overload works with `List<Any>` instruction buffers.
 */
internal fun BcPtrAddr.Companion.forSliceStart(instrs: List<Any>): BcPtrAddr =
    BcPtrAddr(offset = 0, buffer = instrs)

/**
 * Create a [BcPtrAddr] for the end of an instruction list.
 *
 * This overload works with `List<Any>` instruction buffers.
 */
internal fun BcPtrAddr.Companion.forSliceEnd(instrs: List<Any>): BcPtrAddr =
    BcPtrAddr(offset = instrs.size, buffer = instrs)

// --- Extension functions on BcOpcode for formatting and jump visiting ---

/**
 * Visit all jump addresses referenced by the instruction at [ptr] with address [ip].
 *
 * Calls [visitor] for each target address the instruction may jump to.
 * This is used for building the set of jump targets for display formatting.
 */
internal fun BcOpcode.visitJumpAddr(ptr: BcPtrAddr, ip: BcAddr, visitor: (BcAddr) -> Unit) {
    // Jump instructions store an offset as part of their arg.
    // We inspect the arg to find BcAddrOffset values.
    val buffer = ptr.buffer ?: return
    val argIdx = ptr.index() + 1
    if (argIdx >= buffer.size) return
    val arg = buffer[argIdx]

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
        BcOpcode.Iter -> {
            if (arg is List<*> && arg.size >= 5) {
                val offset = arg[4]
                if (offset is BcAddrOffset && offset != BcAddrOffset.FORWARD) {
                    visitor(ip.offset(offset))
                }
            }
        }
        BcOpcode.Continue -> {
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
 * In Rust: `fn fmt_append_arg(self, ptr, ip, end_arg, f) -> fmt::Result`
 * Formats instruction arguments in a human-readable form for debugging.
 */
internal fun BcOpcode.fmtAppendArg(
    ptr: BcPtrAddr,
    ip: BcAddr,
    endArg: BcInstrEndArg?,
    sb: StringBuilder,
) {
    val buffer = ptr.buffer ?: return
    val argIdx = ptr.index() + 1
    if (argIdx >= buffer.size) return
    val arg = buffer[argIdx]

    // Format the arg for display. In Rust, each instruction type has its own Debug impl.
    // In Kotlin, we provide a generic rendering.
    sb.append(" ")
    sb.append(formatArg(arg, endArg))
}

/**
 * Format an instruction argument for display.
 *
 * Handles common arg patterns: pairs, lists, and scalar values.
 * When [endArg] provides local names, slot references are annotated with variable names.
 */
private fun formatArg(arg: Any?, endArg: BcInstrEndArg?): String {
    if (arg == null) return ""
    return when (arg) {
        is Pair<*, *> -> "${formatArg(arg.first, endArg)} ${formatArg(arg.second, endArg)}"
        is List<*> -> arg.joinToString(" ") { formatArg(it, endArg) }
        is BcSlotIn -> formatSlot(arg.get(), endArg)
        is BcSlotOut -> "->${formatSlot(arg.get(), endArg)}"
        is BcSlot -> formatSlot(arg, endArg)
        is BcAddrOffset -> "@${arg.value}"
        is BcAddrOffsetNeg -> "-@${arg.value}"
        else -> arg.toString()
    }
}

/**
 * Format a slot reference, annotating it with the local variable name if available.
 */
private fun formatSlot(slot: BcSlot, endArg: BcInstrEndArg?): String {
    val names = endArg?.localNames?.value
    val idx = slot.value.toInt()
    return if (names != null && idx < names.size) {
        "&${names[idx]}"
    } else {
        slot.toString()
    }
}

// --- BcPtrAddr extensions for list-based buffer ---

/**
 * Extended [BcPtrAddr] constructor that also stores a reference to the backing instruction buffer.
 *
 * This is needed because in Kotlin we don't have raw pointers and must access the buffer
 * through the [BcPtrAddr] during formatting and iteration.
 */
internal fun BcPtrAddr(offset: Int, buffer: List<Any>?): BcPtrAddr {
    return BcPtrAddrWithBuffer(offset, buffer)
}

/**
 * Subclass of [BcPtrAddr] that carries a reference to the backing instruction buffer.
 *
 * In Rust, `BcPtrAddr` is a raw pointer. In Kotlin, we need to carry
 * the buffer reference for element access.
 */
private class BcPtrAddrWithBuffer(
    offset: Int,
    override val buffer: List<Any>?,
) : BcPtrAddr(
    offset = offset,
    range = io.github.kotlinmania.starlark_kotlin.eval.bc.if_debug.IfDebug.new(
        BcPtrRange(start = 0, len = buffer?.size ?: 0)
    ),
) {
    override fun add(addOffset: Int): BcPtrAddr =
        BcPtrAddrWithBuffer(offset + addOffset, buffer)

    override fun offset(addr: BcAddr): BcPtrAddr =
        BcPtrAddrWithBuffer(offset + addr.value.toInt(), buffer)

    override fun sub(start: BcAddr): BcPtrAddr =
        BcPtrAddrWithBuffer(offset - start.value.toInt(), buffer)
}

/**
 * The backing instruction buffer, if available.
 *
 * Open property on [BcPtrAddr] so that [BcPtrAddrWithBuffer] can override it.
 */
internal val BcPtrAddr.buffer: List<Any>?
    get() = if (this is BcPtrAddrWithBuffer) this.buffer else null

/**
 * Get the list index for this [BcPtrAddr].
 */
internal val BcPtrAddr.index: Int
    get() = this.offset
