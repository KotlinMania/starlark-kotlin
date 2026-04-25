// port-lint: source src/eval/bc/instr_arg.rs
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
 * Instruction arguments.
 */

import kotlin.reflect.KClass
import io.github.kotlinmania.starlark.eval.runtime.ArgSymbol
import io.github.kotlinmania.starlark.eval.runtime.FrameSpan
import io.github.kotlinmania.starlark.eval.runtime.LocalCapturedSlotId
import io.github.kotlinmania.starlark.eval.runtime.LocalSlotId
import starlarkmap.Hashed
import starlarkmap.smallmap.SmallMap
import io.github.kotlinmania.starlark.collections.symbol.Symbol
import io.github.kotlinmania.starlark.environment.ModuleSlotId
import io.github.kotlinmania.starlark.eval.bc.BcInstr
import io.github.kotlinmania.starlark.eval.bc.BcInstrRepr
import io.github.kotlinmania.starlark.values.FrozenRef
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.FrozenValueNotSpecial
import io.github.kotlinmania.starlark.values.types.KnownMethod
import io.github.kotlinmania.starlark.values.typing.typecompiled.TypeCompiled
import io.github.kotlinmania.starlark.values.layout.FrozenValueTyped

// ---- TruncateValueRepr ----

/**
 * Truncate value if it is too long.
 */
private class TruncateValueRepr(private val value: FrozenValue) {
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

// ---- BcInstrArg interface ----

/**
 * Instruction fixed argument.
 */
internal interface BcInstrArg {
    /**
     * Append space then append the argument, or append nothing if the argument is empty.
     */
    fun fmtAppend(
        ip: BcAddr,
        endArg: BcInstrEndArg?,
        f: StringBuilder,
    )

    /**
     * Collect instruction jump addresses.
     */
    fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit)
}

// ---- BcInstrArg for Unit (empty argument) ----

/**
 * [BcInstrArg] implementation for empty arguments (Rust `()`).
 */
internal object UnitInstrArg : BcInstrArg {
    override fun fmtAppend(_ip: BcAddr, endArg: BcInstrEndArg?, _f: StringBuilder) {
        // Nothing to append for empty argument.
    }

    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {
        // No jump addresses in empty argument.
    }
}

// ---- BcInstrArg for u32 ----

/**
 * [BcInstrArg] implementation for [UInt] (Rust `u32`).
 */
internal class UIntInstrArg(val value: UInt) : BcInstrArg {
    override fun fmtAppend(_ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" $value")
    }

    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

// ---- BcInstrArg for i32 ----

/**
 * [BcInstrArg] implementation for [Int] (Rust `i32`).
 */
internal class IntInstrArg(val value: Int) : BcInstrArg {
    override fun fmtAppend(_ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" $value")
    }

    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

// ---- BcInstrArg for (A, B) ----

/**
 * [BcInstrArg] implementation for a pair of arguments (Rust `(A, B)`).
 */
internal class PairInstrArg(
    val a: BcInstrArg,
    val b: BcInstrArg,
) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        a.fmtAppend(ip, endArg, f)
        b.fmtAppend(ip, endArg, f)
    }

    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {
        a.visitJumpAddr(ip, consumer)
        b.visitJumpAddr(ip, consumer)
    }
}

// ---- BcInstrArg for (A, B, C) ----

/**
 * [BcInstrArg] implementation for a triple of arguments (Rust `(A, B, C)`).
 */
internal class TripleInstrArg(
    val a: BcInstrArg,
    val b: BcInstrArg,
    val c: BcInstrArg,
) : BcInstrArg {
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

// ---- BcInstrArg for (A, B, C, D) ----

/**
 * [BcInstrArg] implementation for a quad of arguments (Rust `(A, B, C, D)`).
 */
internal class QuadInstrArg(
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

// ---- BcInstrArg for (A, B, C, D, E) ----

/**
 * [BcInstrArg] implementation for a 5-tuple of arguments (Rust `(A, B, C, D, E)`).
 */
internal class QuintInstrArg(
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

// ---- BcInstrArg for (A, B, C, D, E, F) ----

/**
 * [BcInstrArg] implementation for a 6-tuple of arguments (Rust `(A, B, C, D, E, F)`).
 */
internal class SextInstrArg(
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

// ---- BcInstrArg for [A; N] (fixed-size array) ----

/**
 * [BcInstrArg] implementation for a list of arguments (Rust `[A; N]`).
 */
internal class ArrayInstrArg(val items: List<BcInstrArg>) : BcInstrArg {
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

// ---- BcInstrArg for BcAddrOffset ----

/**
 * [BcInstrArg] implementation for [BcAddrOffset] (forward jump offset).
 */
internal class BcAddrOffsetInstrArg(val offset: BcAddrOffset) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" ${ip.offset(offset).value}")
    }

    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {
        consumer(ip.offset(offset))
    }
}

// ---- BcInstrArg for BcAddrOffsetNeg ----

/**
 * [BcInstrArg] implementation for [BcAddrOffsetNeg] (backward jump offset).
 */
internal class BcAddrOffsetNegInstrArg(val offset: BcAddrOffsetNeg) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" ${ip.offsetNeg(offset).value}")
    }

    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {
        consumer(ip.offsetNeg(offset))
    }
}

// ---- BcInstrArg for FrozenValue ----

/**
 * [BcInstrArg] implementation for [FrozenValue].
 */
internal class FrozenValueInstrArg(val value: FrozenValue) : BcInstrArg {
    override fun fmtAppend(_ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" ${TruncateValueRepr(value)}")
    }

    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

// ---- BcInstrArg for FrozenValueNotSpecial ----

/**
 * [BcInstrArg] implementation for [FrozenValueNotSpecial].
 */
internal class FrozenValueNotSpecialInstrArg(val value: FrozenValueNotSpecial) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        FrozenValueInstrArg(value.toFrozenValue()).fmtAppend(ip, endArg, f)
    }

    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

// ---- BcInstrArg for TypeCompiled<FrozenValue> ----

/**
 * [BcInstrArg] implementation for [TypeCompiled].
 */
internal class TypeCompiledInstrArg(val value: TypeCompiled) : BcInstrArg {
    override fun fmtAppend(_ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" $value")
    }

    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

// ---- BcInstrArg for Option<T> ----

/**
 * [BcInstrArg] implementation for optional arguments (Rust `Option<T>`).
 */
internal class OptionalInstrArg(val inner: BcInstrArg?) : BcInstrArg {
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

// ---- BcInstrArg for String ----

/**
 * [BcInstrArg] implementation for [String].
 */
internal class StringInstrArg(val value: String) : BcInstrArg {
    override fun fmtAppend(_ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        // Rust: write!(f, "{param:?}") -- debug format, which quotes the string
        f.append(" \"$value\"")
    }

    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

// ---- BcInstrArg for FrozenRef<T> (single element) ----

/**
 * [BcInstrArg] implementation for [FrozenRef] of a single value.
 */
internal class FrozenRefInstrArg<T>(val ref: FrozenRef<T>) : BcInstrArg {
    override fun fmtAppend(_ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" ${ref.asRef()}")
    }

    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

// ---- BcInstrArg for FrozenRef<List<T>> (slice) ----

/**
 * [BcInstrArg] implementation for [FrozenRef] of a list (Rust `FrozenRef<[T]>`).
 */
internal class FrozenRefListInstrArg<T>(val ref: FrozenRef<List<T>>) : BcInstrArg {
    override fun fmtAppend(_ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" [${ref.asRef().joinToString(", ")}]")
    }

    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

// ---- BcInstrArg for FrozenValueTyped<T> ----

/**
 * [BcInstrArg] implementation for [FrozenValueTyped].
 */
internal class FrozenValueTypedInstrArg<T : StarlarkValue>(
    val value: FrozenValueTyped<T>,
) : BcInstrArg {
    override fun fmtAppend(_ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" ${TruncateValueRepr(value.toFrozenValue())}")
    }

    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

// ---- BcInstrArg for BcNativeFunction ----

/**
 * [BcInstrArg] implementation for [BcNativeFunction].
 */
internal class BcNativeFunctionInstrArg(val func: BcNativeFunction) : BcInstrArg {
    override fun fmtAppend(ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        FrozenValueInstrArg(func.func().toFrozenValue()).fmtAppend(ip, endArg, f)
    }

    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

// ---- BcSlotDisplay helper ----

/**
 * Display a [BcSlot] with optional name resolution from [BcInstrEndArg].
 */
private class BcSlotDisplay(
    private val slot: BcSlot,
    private val endArg: BcInstrEndArg?,
) {
    override fun toString(): String {
        val name = endArg?.localNames?.asRef()?.getOrNull(slot.index.toInt())
        return if (name != null) {
            "&${name.asStr()}"
        } else {
            "&${slot.index}"
        }
    }
}

// ---- BcInstrArg for LocalSlotId ----

/**
 * [BcInstrArg] implementation for [LocalSlotId].
 */
internal class LocalSlotIdInstrArg(val slot: LocalSlotId) : BcInstrArg {
    override fun fmtAppend(_ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" ${BcSlotDisplay(slot.toBcSlot(), endArg)}")
    }

    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

// ---- BcInstrArg for LocalCapturedSlotId ----

/**
 * [BcInstrArg] implementation for [LocalCapturedSlotId].
 */
internal class LocalCapturedSlotIdInstrArg(val slot: LocalCapturedSlotId) : BcInstrArg {
    override fun fmtAppend(_ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" ${BcSlotDisplay(slot.toBcSlot(), endArg)}")
    }

    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

// ---- BcInstrArg for BcSlotIn ----

/**
 * [BcInstrArg] implementation for [BcSlotIn].
 */
internal class BcSlotInInstrArg(val slot: BcSlotIn) : BcInstrArg {
    override fun fmtAppend(_ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" ${BcSlotDisplay(slot.get(), endArg)}")
    }

    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

// ---- BcInstrArg for BcSlotOut ----

/**
 * [BcInstrArg] implementation for [BcSlotOut].
 */
internal class BcSlotOutInstrArg(val slot: BcSlotOut) : BcInstrArg {
    override fun fmtAppend(_ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" ->${BcSlotDisplay(slot.get(), endArg)}")
    }

    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

// ---- BcInstrArg for BcSlotInRange ----

/**
 * [BcInstrArg] implementation for [BcSlotInRange].
 */
internal class BcSlotInRangeInstrArg(val range: BcSlotInRange) : BcInstrArg {
    override fun fmtAppend(_ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        val items = range.iter()
            .map { s -> BcSlotDisplay(s.get(), endArg).toString() }
            .joinToString(", ")
        f.append(" [$items]")
    }

    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

// ---- BcInstrArg for BcSlotInRangeFrom ----

/**
 * [BcInstrArg] implementation for [BcSlotInRangeFrom].
 */
internal class BcSlotInRangeFromInstrArg(val rangeFrom: BcSlotInRangeFrom) : BcInstrArg {
    override fun fmtAppend(_ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" ${rangeFrom.start}..")
    }

    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

// ---- BcInstrArg for ModuleSlotId ----

/**
 * [BcInstrArg] implementation for [ModuleSlotId].
 */
internal class ModuleSlotIdInstrArg(val slot: ModuleSlotId) : BcInstrArg {
    override fun fmtAppend(_ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" m${slot.index}")
    }

    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

// ---- BcInstrArg for FrameSpan ----

/**
 * [BcInstrArg] implementation for [FrameSpan].
 */
internal class FrameSpanInstrArg(val span: FrameSpan) : BcInstrArg {
    override fun fmtAppend(_ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" $span")
    }

    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

// ---- BcInstrArg for BcOpcode ----

/**
 * Opcode as instruction argument.
 */
internal class BcOpcodeInstrArg(val opcode: BcOpcode) : BcInstrArg {
    override fun fmtAppend(_ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" $opcode")
    }

    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

// ---- BcInstrArg for LoopDepth ----

/**
 * [BcInstrArg] implementation for [LoopDepth].
 */
internal class LoopDepthInstrArg(val depth: LoopDepth) : BcInstrArg {
    override fun fmtAppend(_ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" $depth")
    }

    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

// ---- BcInstrArg for KnownMethod ----

/**
 * [BcInstrArg] implementation for [KnownMethod].
 */
internal class KnownMethodInstrArg(val method: KnownMethod) : BcInstrArg {
    override fun fmtAppend(_ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" <m>")
    }

    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

// ---- BcInstrArg for Symbol ----

/**
 * [BcInstrArg] implementation for [Symbol].
 */
internal class SymbolInstrArg(val symbol: Symbol) : BcInstrArg {
    override fun fmtAppend(_ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" ${symbol.asStr()}")
    }

    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

// ---- BcInstrArg for Box<[FrozenValue]> ----

/**
 * [BcInstrArg] implementation for a list of [FrozenValue] (Rust `Box<[FrozenValue]>`).
 */
internal class FrozenValueListInstrArg(val values: List<FrozenValue>) : BcInstrArg {
    override fun fmtAppend(_ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" [")
        for ((i, v) in values.withIndex()) {
            if (i != 0) {
                f.append(", ")
            }
            f.append(TruncateValueRepr(v))
        }
        f.append("]")
    }

    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

// ---- BcInstrArg for Box<[Hashed<FrozenValue>]> ----

/**
 * [BcInstrArg] implementation for a list of [Hashed]<[FrozenValue]>
 * (Rust `Box<[Hashed<FrozenValue>]>`).
 */
internal class HashedFrozenValueListInstrArg(
    val values: List<Hashed<FrozenValue>>,
) : BcInstrArg {
    override fun fmtAppend(_ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" [")
        for ((i, v) in values.withIndex()) {
            if (i != 0) {
                f.append(", ")
            }
            f.append(TruncateValueRepr(v.key()))
        }
        f.append("]")
    }

    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

// ---- BcInstrArg for SmallMap<FrozenValue, FrozenValue> ----

/**
 * [BcInstrArg] implementation for [SmallMap]<[FrozenValue], [FrozenValue]>.
 */
internal class SmallMapInstrArg(
    val map: SmallMap<FrozenValue, FrozenValue>,
) : BcInstrArg {
    override fun fmtAppend(_ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" {")
        for ((i, entry) in map.iter().withIndex()) {
            if (i != 0) {
                f.append(", ")
            }
            f.append("${TruncateValueRepr(entry.first)}: ${TruncateValueRepr(entry.second)}")
        }
        f.append("}")
    }

    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

// ---- BcInstrArg for InstrDefData ----

/**
 * [BcInstrArg] implementation for [InstrDefData].
 */
internal class InstrDefDataInstrArg(val data: InstrDefData) : BcInstrArg {
    override fun fmtAppend(_ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" InstrDefData")
    }

    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

// ---- BcInstrArg for BcCallArgsFull<S> ----

/**
 * [BcInstrArg] implementation for [BcCallArgsFull].
 */
internal class BcCallArgsFullInstrArg<S : ArgSymbol>(
    val args: BcCallArgsFull<S>,
) : BcInstrArg {
    override fun fmtAppend(_ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" {$args}")
    }

    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

// ---- BcInstrArg for BcCallArgsPos ----

/**
 * [BcInstrArg] implementation for [BcCallArgsPos].
 */
internal class BcCallArgsPosInstrArg(val args: BcCallArgsPos) : BcInstrArg {
    override fun fmtAppend(_ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" ${args.pos}")
    }

    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

// ---- BcInstrArg for BcInstrEndArg ----

/**
 * [BcInstrArg] implementation for [BcInstrEndArg].
 */
internal class BcInstrEndArgInstrArg(val endArgValue: BcInstrEndArg) : BcInstrArg {
    override fun fmtAppend(_ip: BcAddr, endArg: BcInstrEndArg?, f: StringBuilder) {
        f.append(" BcInstrEndArg")
    }

    override fun visitJumpAddr(ip: BcAddr, consumer: (BcAddr) -> Unit) {}
}

// ---- BcOpcode extension: fmtAppendArg ----

/**
 * Format instruction argument via opcode dispatch.
 *
 * In Rust, this uses a generic handler with `I::Arg::fmt_append`. In Kotlin,
 * the instruction's arg is stored as [Any] in [BcInstrRepr], so we cast to
 * [BcInstrArg] and call [BcInstrArg.fmtAppend].
 */
internal fun BcOpcode.fmtAppendArg(
    ptr: BcPtrAddr,
    ip: BcAddr,
    endArg: BcInstrEndArg?,
    f: StringBuilder,
) {
    this.dispatch(object : BcOpcodeHandler<Unit> {
        override fun <I : BcInstr<*>> handle(instrClass: KClass<I>): Unit {
            val instr: BcInstrRepr<I> = ptr.getInstr(instrClass, ptr)
            val arg = instr.arg
            if (arg is BcInstrArg) {
                arg.fmtAppend(ip, endArg, f)
            }
        }
    })
}

// ---- BcOpcode extension: visitJumpAddr ----

/**
 * Visit jump addresses via opcode dispatch.
 *
 * In Rust, this uses a generic handler with `I::Arg::visit_jump_addr`. In Kotlin,
 * the instruction's arg is stored as [Any] in [BcInstrRepr], so we cast to
 * [BcInstrArg] and call [BcInstrArg.visitJumpAddr].
 */
internal fun BcOpcode.visitJumpAddr(
    ptr: BcPtrAddr,
    addr: BcAddr,
    consumer: (BcAddr) -> Unit,
) {
    this.dispatch(object : BcOpcodeHandler<Unit> {
        override fun <I : BcInstr<*>> handle(instrClass: KClass<I>): Unit {
            val instr: BcInstrRepr<I> = ptr.getInstr(instrClass, ptr)
            val arg = instr.arg
            if (arg is BcInstrArg) {
                arg.visitJumpAddr(addr, consumer)
            }
        }
    })
}
