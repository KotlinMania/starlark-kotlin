// port-lint: source src/eval/bc/addr.rs
package io.github.kotlinmania.starlark.eval.bc

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

/** Address types used in bytecode interpreter. */

import kotlin.reflect.KClass
import io.github.kotlinmania.starlark.eval.bc.BcInstr
import io.github.kotlinmania.starlark.eval.bc.BcInstrRepr
import io.github.kotlinmania.starlark.eval.bc.BcInstrHeader
import io.github.kotlinmania.starlark.eval.bc.BC_INSTR_ALIGN

/** Address relative to bytecode start. */
internal data class BcAddr(val value: UInt) : Comparable<BcAddr> {
    constructor() : this(0u)

    override fun toString(): String = "@$value"

    override fun compareTo(other: BcAddr): Int = value.compareTo(other.value)

    fun offsetFrom(start: BcAddr): BcAddrOffset {
        require(this >= start)
        return BcAddrOffset(this.value - start.value)
    }

    fun offset(offset: BcAddrOffset): BcAddr {
        return BcAddr(this.value + offset.value)
    }

    fun offsetNeg(offset: BcAddrOffsetNeg): BcAddr {
        return BcAddr(this.value - offset.value)
    }

    operator fun minus(rhs: UInt): BcAddr {
        check(this.value >= rhs)
        return BcAddr(this.value - rhs)
    }

    operator fun plus(rhs: UInt): BcAddr {
        return BcAddr(this.value + rhs)
    }

    // Note: BcAddr is immutable in Kotlin (data class), so no += operator.
    // Use reassignment: addr = addr + rhs
}

/**
 * Valid pointer range of bytecode.
 * Used for debugging assertions. This object is not created in release mode.
 */
internal data class BcPtrRange(
    // start: *const u8,
    val start: Int,
    /** Length in bytes. */
    val len: Int,
) {
    companion object {
        fun forSlice(slice: LongArray): BcPtrRange {
            return BcPtrRange(
                start = 0,
                len = slice.size * Long.SIZE_BYTES,
            )
        }

        /** Overload for list-based instruction buffers (used by BcInstrs). */
        fun forSlice(slice: List<Any>): BcPtrRange {
            return BcPtrRange(
                start = 0,
                len = slice.size,
            )
        }
    }

    fun assertInRange(offset: Int) {
        check(offset >= 0)
        check(offset <= len)
    }

    fun end(): Int {
        return start + len
    }
}

/**
 * Pointer to an instruction in memory.
 *
 * In Kotlin, this is an offset into a bytecode buffer with debug validation.
 */
internal data class BcPtrAddr(
    // ptr: *const u8
    val offset: Int,
    /** When assertions enabled, we validate the pointer is in this range. */
    val range: IfDebug<BcPtrRange>,
) : Comparable<BcPtrAddr> {
    override fun compareTo(other: BcPtrAddr): Int = offset.compareTo(other.offset)

    companion object {
        fun new(offset: Int, range: IfDebug<BcPtrRange>): BcPtrAddr {
            check(offset % BC_INSTR_ALIGN == 0)
            range.ifDebug { it.assertInRange(offset) }
            return BcPtrAddr(offset, range)
        }

        /** Create a pointer for the beginning of the slice. */
        fun forSliceStart(slice: LongArray): BcPtrAddr {
            return new(
                0,
                IfDebug.new(BcPtrRange.forSlice(slice)),
            )
        }

        /** Create a pointer for the end of the slice. */
        fun forSliceEnd(slice: LongArray): BcPtrAddr {
            return new(
                slice.size * Long.SIZE_BYTES,
                IfDebug.new(BcPtrRange.forSlice(slice)),
            )
        }

        /** Overload for list-based instruction buffers (used by BcInstrs). */
        fun forSliceStart(slice: List<Any>): BcPtrAddr {
            return BcPtrAddr(
                0,
                IfDebug.new(BcPtrRange.forSlice(slice)),
            )
        }

        /** Overload for list-based instruction buffers (used by BcInstrs). */
        fun forSliceEnd(slice: List<Any>): BcPtrAddr {
            return BcPtrAddr(
                slice.size,
                IfDebug.new(BcPtrRange.forSlice(slice)),
            )
        }
    }

    /** Distance from current ptr to the end of instructions. */
    private fun remainingIfDebug(): Int {
        return range.getRefIfDebug().end() - offset
    }

    fun <I : BcInstr<*>> getInstr(instrClass: KClass<I>, instrs: Any): BcInstrRepr<I> {
        @Suppress("UNCHECKED_CAST")
        return instrs as BcInstrRepr<I>
    }

    fun <I : BcInstr<*>> getInstrChecked(instrClass: KClass<I>, instrs: Any): BcInstrRepr<I>? {
        return if (getOpcode(instrs) == BcOpcode.forInstr(instrClass)) {
            getInstr(instrClass, instrs)
        } else {
            null
        }
    }

    fun getOpcode(instrs: Any): BcOpcode {
        val header = instrs as BcInstrHeader
        return header.opcode
    }

    fun offsetFrom(start: BcPtrAddr): BcAddr {
        val diff = this.offset - start.offset
        check(diff >= 0)
        check(diff <= Int.MAX_VALUE)
        return BcAddr(diff.toUInt())
    }

    private fun subInt(offset: Int): BcPtrAddr {
        return new(this.offset - offset, this.range)
    }

    fun sub(start: BcAddr): BcPtrAddr {
        return subInt(start.value.toInt())
    }

    fun offset(addr: BcAddr): BcPtrAddr {
        return add(addr.value.toInt())
    }

    fun addRel(rel: BcAddrOffset): BcPtrAddr {
        return add(rel.value.toInt())
    }

    fun addRelNeg(rel: BcAddrOffsetNeg): BcPtrAddr {
        return subInt(rel.value.toInt())
    }

    fun add(offset: Int): BcPtrAddr {
        return new(this.offset + offset, this.range)
    }

    fun <I : BcInstr<*>> addInstr(instrClass: KClass<I>): BcPtrAddr {
        return addRel(BcAddrOffset.forInstr(instrClass))
    }
}

/** Difference between addresses. */
internal data class BcAddrOffset(val value: UInt) : Comparable<BcAddrOffset> {
    override fun toString(): String = value.toString()

    override fun compareTo(other: BcAddrOffset): Int = value.compareTo(other.value)

    companion object {
        /** Pointer to not yet known address. */
        val FORWARD = BcAddrOffset(0xdeadbeefu)

        /** Size of an instruction. */
        fun <I : BcInstr<*>> forInstr(instrClass: KClass<I>): BcAddrOffset {
            BcInstrRepr.assertAlign(instrClass)
            return BcAddrOffset(BcInstrRepr.sizeOf(instrClass).toUInt())
        }
    }

    fun neg(): BcAddrOffsetNeg {
        return BcAddrOffsetNeg(this.value)
    }
}

/** Negative difference between addresses. */
internal data class BcAddrOffsetNeg(val value: UInt) : Comparable<BcAddrOffsetNeg> {
    override fun toString(): String = value.toString()

    override fun compareTo(other: BcAddrOffsetNeg): Int = value.compareTo(other.value)
}
