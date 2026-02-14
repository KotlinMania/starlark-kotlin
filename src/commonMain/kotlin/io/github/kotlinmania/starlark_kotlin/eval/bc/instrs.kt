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

/// Instructions serialized in byte array.

// Placeholder types referenced from other modules
// These will be replaced with real imports as the port progresses
class BcAddr(val value: UInt) {
    fun offset(off: BcAddrOffset): BcAddr = BcAddr(value + off.value.toUInt())
    fun offsetFrom(start: BcAddr): BcAddrOffset = BcAddrOffset((value.toInt() - start.value.toInt()))
    override fun equals(other: Any?): Boolean = other is BcAddr && value == other.value
    override fun hashCode(): Int = value.hashCode()
    override fun toString(): String = value.toString()
}
class BcAddrOffset(val value: Int) {
    companion object {
        val FORWARD: BcAddrOffset = BcAddrOffset(-1)
    }
    override fun equals(other: Any?): Boolean = other is BcAddrOffset && value == other.value
    override fun hashCode(): Int = value.hashCode()
}
class BcPtrAddr(
    private val buffer: List<Any>,
    private val index: Int,
) {
    fun getOpcode(): BcOpcode {
        val header = buffer[index]
        if (header is BcInstrHeader) return header.opcode
        return BcOpcode.End
    }
    fun add(size: Int): BcPtrAddr = BcPtrAddr(buffer, index + size)
    fun offsetFrom(start: BcPtrAddr): BcAddr = BcAddr((index - start.index).toUInt())
    override fun equals(other: Any?): Boolean = other is BcPtrAddr && index == other.index
    override fun hashCode(): Int = index
    operator fun compareTo(other: BcPtrAddr): Int = index.compareTo(other.index)
    companion object {
        fun forSliceStart(buffer: List<Any>): BcPtrAddr = BcPtrAddr(buffer, 0)
        fun forSliceEnd(buffer: List<Any>): BcPtrAddr = BcPtrAddr(buffer, buffer.size)
    }
}

class BcInstrSlowArg
class BcInstrEndArg(
    val endAddr: BcAddr,
    val slowArgs: List<Pair<BcAddr, BcInstrSlowArg>>,
    val localNames: List<String>,
)
class BcStatementLocations {
    companion object {
        fun new(): BcStatementLocations = BcStatementLocations()
    }
    fun stmtAt(ip: BcAddr): Pair<StmtLoc, Boolean>? = null
}
class StmtLoc(val span: SpanInfo)
class SpanInfo(val span: String = "")

/// Drop instructions in the buffer.
private fun dropInstrs(instrs: List<Any>) {
    // In Rust this invokes destructors via raw pointer traversal.
    // In Kotlin, GC handles cleanup. We just clear references.
}

/// Statically allocate a valid instruction buffer.
///
/// Valid bytecode must end with `End` instruction, otherwise evaluation overruns
/// the instruction buffer.
private fun emptyInstrs(): List<Any> {
    return listOf(
        BcInstrHeader.forOpcode(BcOpcode.End),
        BcInstrEndArg(
            endAddr = BcAddr(0u),
            slowArgs = emptyList(),
            localNames = emptyList(),
        ),
    )
}

internal class BcInstrs private constructor(
    // We use a list to store instructions. In Rust this is Either<Box<[u64]>, &'static [u64]>.
    private val instrs: List<Any>,
    internal val stmtLocs: BcStatementLocations,
) {
    companion object {
        fun default(): BcInstrs = forInstrs(emptyInstrs(), BcStatementLocations.new())

        fun forInstrs(instrs: List<Any>, stmtLocs: BcStatementLocations): BcInstrs {
            return BcInstrs(instrs, stmtLocs)
        }
    }

    fun startPtr(): BcPtrAddr = BcPtrAddr.forSliceStart(instrs)

    fun end(): BcAddr = BcAddr(instrs.size.toUInt())

    fun endPtr(): BcPtrAddr = BcPtrAddr.forSliceEnd(instrs)

    private fun iter(): Sequence<Pair<BcPtrAddr, BcAddr>> {
        return sequence {
            var nextPtr = startPtr()
            val endPtr = endPtr()
            while (nextPtr != endPtr) {
                require(nextPtr.compareTo(endPtr) < 0)
                val ptr = nextPtr
                val ip = ptr.offsetFrom(startPtr())
                nextPtr = nextPtr.add(ptr.getOpcode().sizeOfRepr())
                yield(Pair(ptr, ip))
            }
        }
    }

    private fun endArg(): BcInstrEndArg? {
        for ((ptr, _) in iter()) {
            if (ptr.getOpcode() == BcOpcode.End) {
                // In Rust this casts to InstrEnd and gets the arg
                return null
            }
        }
        return null
    }

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
            // ptr.getOpcode().visitJumpAddr(ptr, ip) { jumpAddr -> jumpTargets.add(jumpAddr) }
        }
        var first = true
        for ((ptr, ip) in iter()) {
            if (loopEnds.lastOrNull() == ip) {
                loopEnds.removeLast()
            }
            val loopPadCount = loopEnds.size * 2

            if (newline) {
                val stmtAt = stmtLocs.stmtAt(ip)
                if (stmtAt != null) {
                    val (loc, _) = stmtAt
                    sb.appendLine("${" ".repeat(loopPadCount)} ${" ".repeat(ipPad)}  # ${loc.span.span}")
                }
            } else {
                if (!first) {
                    sb.append("; ")
                }
            }
            first = false

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
                // opcode.fmtAppendArg(ptr, ip, endArg, sb)
            }
            if (newline) {
                sb.appendLine()
            }
            if (opcode == BcOpcode.Iter) {
                // val forLoop = ptr.getInstr<InstrIter>()
                // loopEnds.add(ip.offset(forLoop.arg.4))
            }
        }
    }

    internal fun dumpDebug(): String {
        val sb = StringBuilder()
        fmtImpl(sb, true)
        return sb.toString()
    }

    override fun toString(): String {
        val sb = StringBuilder()
        fmtImpl(sb, false)
        return sb.toString()
    }
}

class PatchAddr(
    val instrStart: BcAddr,
    val arg: BcAddr,
)

/// Raw instructions writer.
///
/// Higher level wrapper is `BcWriter`.
internal class BcInstrsWriter {
    internal val instrs: MutableList<Any> = mutableListOf()

    companion object {
        fun new(): BcInstrsWriter = BcInstrsWriter()
    }

    private fun instrsLenBytes(): Int = instrs.size

    fun ip(): BcAddr = BcAddr(instrsLenBytes().toUInt())

    fun write(header: BcInstrHeader, arg: Any): BcAddr {
        val instrIp = ip()
        instrs.add(header)
        instrs.add(arg)
        return instrIp
    }

    fun addrToPatch(instrStart: BcAddr, argIndex: Int): PatchAddr {
        return PatchAddr(
            instrStart = instrStart,
            arg = BcAddr(argIndex.toUInt()),
        )
    }

    fun patchAddr(addr: PatchAddr) {
        // In Rust this patches raw memory. In Kotlin we update the list entry.
        val index = addr.arg.value.toInt()
        if (index < instrs.size) {
            instrs[index] = ip().offsetFrom(addr.instrStart)
        }
    }

    fun finish(
        slowArgs: List<Pair<BcAddr, BcInstrSlowArg>>,
        stmtLocs: BcStatementLocations,
        localNames: List<String>,
    ): BcInstrs {
        write(
            BcInstrHeader.forOpcode(BcOpcode.End),
            BcInstrEndArg(
                endAddr = ip(),
                slowArgs = slowArgs,
                localNames = localNames,
            ),
        )
        return BcInstrs.forInstrs(instrs.toList(), stmtLocs)
    }
}
