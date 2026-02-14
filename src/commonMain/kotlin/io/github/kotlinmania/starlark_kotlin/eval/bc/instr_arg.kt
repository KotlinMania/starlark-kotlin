// port-lint: source src/eval/bc/instr_arg.rs
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

/// Instruction arguments.

// Placeholder types referenced from other modules
// These will be replaced with real imports as the port progresses
class BcAddr(val offset: Int) {
    fun offset(rel: BcAddrOffset): BcAddr = BcAddr(offset + rel.offset)
    fun offsetNeg(rel: BcAddrOffsetNeg): BcAddr = BcAddr(offset - rel.offset)
}
class BcAddrOffset(val offset: Int)
class BcAddrOffsetNeg(val offset: Int)
class BcPtrAddr {
    fun <I> getInstr(): BcInstrRef = BcInstrRef()
}
class BcInstrRef {
    val arg: Any? = null
}
class BcInstrEndArg {
    val localNames: List<FrozenStringValue> = emptyList()
}
class FrozenValue {
    fun toValue(): Value = Value()
    fun toFrozenValue(): FrozenValue = this
}
class Value {
    fun toRepr(): String = ""
    fun getType(): String = ""
}
class FrozenValueNotSpecial {
    fun toFrozenValue(): FrozenValue = FrozenValue()
}
class TypeCompiled {
    override fun toString(): String = "TypeCompiled"
}
class FrozenStringValue {
    fun asStr(): String = ""
}
class FrozenValueTyped<T> {
    fun toFrozenValue(): FrozenValue = FrozenValue()
}
class FrozenRef<T>(val value: T) {
    fun asRef(): T = value
    fun iter(): Iterator<T> = listOf(value).iterator()
}
class Hashed<T>(val key: T)
class SmallMap<K, V> {
    private val entries: MutableList<Pair<K, V>> = mutableListOf()
    fun iter(): Iterator<Pair<K, V>> = entries.iterator()
    operator fun iterator(): Iterator<Pair<K, V>> = iter()
}
class BcNativeFunction {
    fun funValue(): FrozenValue = FrozenValue()
}
class BcSlot(val index: Int)
class BcSlotIn(val index: Int) {
    fun get(): BcSlot = BcSlot(index)
}
class BcSlotOut(val index: Int) {
    fun get(): BcSlot = BcSlot(index)
}
class BcSlotInRange(val start: Int, val count: Int) {
    fun iter(): List<BcSlotIn> = (start until start + count).map { BcSlotIn(it) }
}
class BcSlotInRangeFrom(val start: Int)
class LocalSlotId(val index: Int) {
    fun toBcSlot(): BcSlot = BcSlot(index)
}
class LocalCapturedSlotId(val index: Int) {
    fun toBcSlot(): BcSlot = BcSlot(index)
}
class ModuleSlotId(val index: Int)
class FrameSpan {
    override fun toString(): String = "FrameSpan"
}
class BcOpcode {
    fun dispatch(handler: BcOpcodeHandler) {}
}
interface BcOpcodeHandler {
    fun handle()
}
class Symbol {
    fun asStr(): String = ""
}
class LoopDepth(val depth: Int) {
    override fun toString(): String = depth.toString()
}
class KnownMethod
class InstrDefData
class BcCallArgsFull<S> {
    override fun toString(): String = "BcCallArgsFull"
}
class BcCallArgsPos {
    val pos: BcSlotInRange = BcSlotInRange(0, 0)
}

/// Truncate value if it is too long.
private class TruncateValueRepr(val value: FrozenValue) {
    override fun toString(): String {
        val repr = value.toValue().toRepr()
        // Truncate too long constants (like dicts with hundreds of elements).
        return if (repr.length > 100) {
            "<${value.toValue().getType()}>"
        } else {
            repr
        }
    }
}

/// Instruction fixed argument.
interface BcInstrArg {
    /// Append space then append the argument, or append nothing if the argument is empty.
    fun fmtAppend(
        ip: BcAddr,
        endArg: BcInstrEndArg?,
        f: StringBuilder,
    )

    /// Collect instruction jump addresses.
    fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit)
}

/// BcInstrArg for Unit (empty argument).
object UnitInstrArg : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {}
    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

/// BcInstrArg for Int (u32/i32).
class IntInstrArg(val value: Int) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" $value")
    }
    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

/// BcInstrArg for a pair of args.
class PairInstrArg(val a: BcInstrArg, val b: BcInstrArg) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        a.fmtAppend(ip, endArg, f)
        b.fmtAppend(ip, endArg, f)
    }
    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {
        a.visitJumpAddr(ip, consumer)
        b.visitJumpAddr(ip, consumer)
    }
}

/// BcInstrArg for a triple of args.
class TripleInstrArg(val a: BcInstrArg, val b: BcInstrArg, val c: BcInstrArg) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        a.fmtAppend(ip, endArg, f)
        b.fmtAppend(ip, endArg, f)
        c.fmtAppend(ip, endArg, f)
    }
    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {
        a.visitJumpAddr(ip, consumer)
        b.visitJumpAddr(ip, consumer)
        c.visitJumpAddr(ip, consumer)
    }
}

/// BcInstrArg for a quad of args.
class QuadInstrArg(
    val a: BcInstrArg,
    val b: BcInstrArg,
    val c: BcInstrArg,
    val d: BcInstrArg,
) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        a.fmtAppend(ip, endArg, f)
        b.fmtAppend(ip, endArg, f)
        c.fmtAppend(ip, endArg, f)
        d.fmtAppend(ip, endArg, f)
    }
    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {
        a.visitJumpAddr(ip, consumer)
        b.visitJumpAddr(ip, consumer)
        c.visitJumpAddr(ip, consumer)
        d.visitJumpAddr(ip, consumer)
    }
}

/// BcInstrArg for a 5-tuple of args.
class QuintInstrArg(
    val a: BcInstrArg,
    val b: BcInstrArg,
    val c: BcInstrArg,
    val d: BcInstrArg,
    val e: BcInstrArg,
) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        a.fmtAppend(ip, endArg, f)
        b.fmtAppend(ip, endArg, f)
        c.fmtAppend(ip, endArg, f)
        d.fmtAppend(ip, endArg, f)
        e.fmtAppend(ip, endArg, f)
    }
    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {
        a.visitJumpAddr(ip, consumer)
        b.visitJumpAddr(ip, consumer)
        c.visitJumpAddr(ip, consumer)
        d.visitJumpAddr(ip, consumer)
        e.visitJumpAddr(ip, consumer)
    }
}

/// BcInstrArg for a 6-tuple of args.
class SextInstrArg(
    val a: BcInstrArg,
    val b: BcInstrArg,
    val c: BcInstrArg,
    val d: BcInstrArg,
    val e: BcInstrArg,
    val fArg: BcInstrArg,
) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        a.fmtAppend(ip, endArg, f)
        b.fmtAppend(ip, endArg, f)
        c.fmtAppend(ip, endArg, f)
        d.fmtAppend(ip, endArg, f)
        e.fmtAppend(ip, endArg, f)
        fArg.fmtAppend(ip, endArg, f)
    }
    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {
        a.visitJumpAddr(ip, consumer)
        b.visitJumpAddr(ip, consumer)
        c.visitJumpAddr(ip, consumer)
        d.visitJumpAddr(ip, consumer)
        e.visitJumpAddr(ip, consumer)
        fArg.visitJumpAddr(ip, consumer)
    }
}

/// BcInstrArg for a list of args.
class ListInstrArg(val items: List<BcInstrArg>) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        for (item in items) {
            item.fmtAppend(ip, endArg, f)
        }
    }
    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {
        for (item in items) {
            item.visitJumpAddr(ip, consumer)
        }
    }
}

/// BcInstrArg for BcAddrOffset (forward jump).
class BcAddrOffsetInstrArg(val offset: BcAddrOffset) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" ${ip.offset(offset).offset}")
    }
    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {
        consumer(ip.offset(offset))
    }
}

/// BcInstrArg for BcAddrOffsetNeg (backward jump).
class BcAddrOffsetNegInstrArg(val offset: BcAddrOffsetNeg) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" ${ip.offsetNeg(offset).offset}")
    }
    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {
        consumer(ip.offsetNeg(offset))
    }
}

/// BcInstrArg for FrozenValue.
class FrozenValueInstrArg(val value: FrozenValue) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" ${TruncateValueRepr(value)}")
    }
    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

/// BcInstrArg for FrozenValueNotSpecial.
class FrozenValueNotSpecialInstrArg(val value: FrozenValueNotSpecial) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        FrozenValueInstrArg(value.toFrozenValue()).fmtAppend(ip, endArg, f)
    }
    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

/// BcInstrArg for TypeCompiled.
class TypeCompiledInstrArg(val value: TypeCompiled) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" $value")
    }
    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

/// BcInstrArg for Optional values.
class OptionalInstrArg(val inner: BcInstrArg?) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        if (inner == null) {
            f.append(" ()")
        } else {
            inner.fmtAppend(ip, endArg, f)
        }
    }
    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {
        inner?.visitJumpAddr(ip, consumer)
    }
}

/// BcInstrArg for String.
class StringInstrArg(val value: String) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" \"$value\"")
    }
    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

/// BcInstrArg for FrozenRef<T>.
class FrozenRefInstrArg<T>(val ref: FrozenRef<T>) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" ${ref.asRef()}")
    }
    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

/// BcInstrArg for FrozenValueTyped.
class FrozenValueTypedInstrArg<T>(val value: FrozenValueTyped<T>) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" ${TruncateValueRepr(value.toFrozenValue())}")
    }
    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

/// BcInstrArg for BcNativeFunction.
class BcNativeFunctionInstrArg(val func: BcNativeFunction) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        FrozenValueInstrArg(func.funValue()).fmtAppend(ip, endArg, f)
    }
    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

/// Display a BcSlot with optional name resolution.
private class BcSlotDisplay(val slot: BcSlot, val endArg: BcInstrEndArg?) {
    override fun toString(): String {
        val name = endArg?.localNames?.getOrNull(slot.index)
        return if (name != null) {
            "&${name.asStr()}"
        } else {
            "&${slot.index}"
        }
    }
}

/// BcInstrArg for LocalSlotId.
class LocalSlotIdInstrArg(val slot: LocalSlotId) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" ${BcSlotDisplay(slot.toBcSlot(), endArg)}")
    }
    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

/// BcInstrArg for LocalCapturedSlotId.
class LocalCapturedSlotIdInstrArg(val slot: LocalCapturedSlotId) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" ${BcSlotDisplay(slot.toBcSlot(), endArg)}")
    }
    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

/// BcInstrArg for BcSlotIn.
class BcSlotInInstrArg(val slot: BcSlotIn) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" ${BcSlotDisplay(slot.get(), endArg)}")
    }
    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

/// BcInstrArg for BcSlotOut.
class BcSlotOutInstrArg(val slot: BcSlotOut) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" ->${BcSlotDisplay(slot.get(), endArg)}")
    }
    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

/// BcInstrArg for BcSlotInRange.
class BcSlotInRangeInstrArg(val range: BcSlotInRange) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        val items = range.iter().joinToString(", ") { BcSlotDisplay(it.get(), endArg).toString() }
        f.append(" [$items]")
    }
    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

/// BcInstrArg for BcSlotInRangeFrom.
class BcSlotInRangeFromInstrArg(val rangeFrom: BcSlotInRangeFrom) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" ${rangeFrom.start}..")
    }
    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

/// BcInstrArg for ModuleSlotId.
class ModuleSlotIdInstrArg(val slot: ModuleSlotId) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" m${slot.index}")
    }
    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

/// BcInstrArg for FrameSpan.
class FrameSpanInstrArg(val span: FrameSpan) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" $span")
    }
    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

/// Opcode as instruction argument.
class BcOpcodeInstrArg(val opcode: BcOpcode) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" $opcode")
    }
    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

/// BcInstrArg for LoopDepth.
class LoopDepthInstrArg(val depth: LoopDepth) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" $depth")
    }
    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

/// BcInstrArg for KnownMethod.
class KnownMethodInstrArg(val method: KnownMethod) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" <m>")
    }
    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

/// BcInstrArg for Symbol.
class SymbolInstrArg(val symbol: Symbol) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" ${symbol.asStr()}")
    }
    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

/// BcInstrArg for a list of FrozenValues.
class FrozenValueListInstrArg(val values: List<FrozenValue>) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" [")
        for ((i, v) in values.withIndex()) {
            if (i != 0) f.append(", ")
            f.append(TruncateValueRepr(v))
        }
        f.append("]")
    }
    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

/// BcInstrArg for a list of Hashed<FrozenValue>.
class HashedFrozenValueListInstrArg(val values: List<Hashed<FrozenValue>>) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" [")
        for ((i, v) in values.withIndex()) {
            if (i != 0) f.append(", ")
            f.append(TruncateValueRepr(v.key))
        }
        f.append("]")
    }
    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

/// BcInstrArg for SmallMap<FrozenValue, FrozenValue>.
class SmallMapInstrArg(val map: SmallMap<FrozenValue, FrozenValue>) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" {")
        var first = true
        for ((k, v) in map) {
            if (!first) f.append(", ")
            first = false
            f.append("${TruncateValueRepr(k)}: ${TruncateValueRepr(v)}")
        }
        f.append("}")
    }
    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

/// BcInstrArg for InstrDefData.
class InstrDefDataInstrArg(val data: InstrDefData) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" InstrDefData")
    }
    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

/// BcInstrArg for BcCallArgsFull.
class BcCallArgsFullInstrArg<S>(val args: BcCallArgsFull<S>) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" {$args}")
    }
    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

/// BcInstrArg for BcCallArgsPos.
class BcCallArgsPosInstrArg(val args: BcCallArgsPos) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" ${args.pos}")
    }
    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

/// BcInstrArg for BcInstrEndArg.
class BcInstrEndArgInstrArg(val endArgValue: BcInstrEndArg) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" BcInstrEndArg")
    }
    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

/// Format instruction argument via opcode dispatch.
fun BcOpcode.fmtAppendArg(
    ptr: BcPtrAddr,
    ip: BcAddr,
    endArg: BcInstrEndArg?,
    f: StringBuilder,
) {
    // Dispatch to the appropriate handler based on opcode
    // In Rust this uses a generic handler pattern; in Kotlin we use dynamic dispatch
    dispatch(object : BcOpcodeHandler {
        override fun handle() {
            val instr = ptr.getInstr<Any>()
            if (instr.arg is BcInstrArg) {
                (instr.arg as BcInstrArg).fmtAppend(ip, endArg, f)
            }
        }
    })
}

/// Visit jump addresses via opcode dispatch.
fun BcOpcode.visitJumpAddr(
    ptr: BcPtrAddr,
    addr: BcAddr,
    consumer: (BcAddr) -> Unit,
) {
    dispatch(object : BcOpcodeHandler {
        override fun handle() {
            val instr = ptr.getInstr<Any>()
            if (instr.arg is BcInstrArg) {
                (instr.arg as BcInstrArg).visitJumpAddr(addr, consumer)
            }
        }
    })
}
