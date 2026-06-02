// port-lint: source src/eval/bc/addr.rs
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

/** Address types used in bytecode interpreter. */

import kotlin.reflect.KClass

/** Address relative to bytecode start. */
data class BcAddr(
    val value: UInt,
) : Comparable<BcAddr> {
    constructor() : this(0u)

    override fun toString(): String = "@$value"

    override fun compareTo(other: BcAddr): Int = value.compareTo(other.value)

    // impl BcAddr

    fun offsetFrom(start: BcAddr): BcAddrOffset {
        require(this >= start)
        return BcAddrOffset(this.value - start.value)
    }

    fun offset(offset: BcAddrOffset): BcAddr = BcAddr(this.value + offset.value)

    fun offsetNeg(offset: BcAddrOffsetNeg): BcAddr = BcAddr(this.value - offset.value)

    operator fun minus(rhs: UInt): BcAddr {
        check(this.value >= rhs)
        return BcAddr(this.value - rhs)
    }

    operator fun plus(rhs: UInt): BcAddr = BcAddr(this.value + rhs)

    // Note: BcAddr is immutable in Kotlin (data class), so no += operator.
    // Use reassignment: addr = addr + rhs
}

/**
 * Valid pointer range of bytecode.
 * Used for debugging assertions. This object is not created in release mode.
 */
data class BcPtrRange(
    // start: *const u8,
    val start: Int,
    /** Length in bytes. */
    // len: usize,
    val len: Int,
) {
    companion object {
        fun forSlice(slice: LongArray): BcPtrRange =
            BcPtrRange(
                start = 0,
                len = slice.size * Long.SIZE_BYTES,
            )

        /** Overload for list-based instruction buffers (used by BcInstrs). */
        fun forSlice(slice: List<Any>): BcPtrRange =
            BcPtrRange(
                start = 0,
                len = slice.size,
            )
    }

    fun assertInRange(offset: Int) {
        check(offset >= 0)
        check(offset <= len)
    }

    fun end(): Int = start + len
}

/**
 * Pointer to an instruction in memory.
 *
 * In Rust, this is a raw pointer with debug range checks.
 * In Kotlin, this is an offset into a bytecode buffer with debug validation.
 */
data class BcPtrAddr(
    // ptr: *const u8
    val offset: Int,
    /** When assertions enabled, we validate the pointer is in this range. */
    // range: IfDebug<BcPtrRange>
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
        fun forSliceStart(slice: LongArray): BcPtrAddr =
            new(
                0,
                IfDebug.new(BcPtrRange.forSlice(slice)),
            )

        /** Create a pointer for the end of the slice. */
        fun forSliceEnd(slice: LongArray): BcPtrAddr =
            new(
                slice.size * Long.SIZE_BYTES,
                IfDebug.new(BcPtrRange.forSlice(slice)),
            )

        /** Overload for list-based instruction buffers (used by BcInstrs). */
        fun forSliceStart(slice: List<Any>): BcPtrAddr =
            BcPtrAddr(
                0,
                IfDebug.new(BcPtrRange.forSlice(slice)),
            )

        /** Overload for list-based instruction buffers (used by BcInstrs). */
        fun forSliceEnd(slice: List<Any>): BcPtrAddr =
            BcPtrAddr(
                slice.size,
                IfDebug.new(BcPtrRange.forSlice(slice)),
            )
    }

    /** Distance from current ptr to the end of instructions. */
    private fun remainingIfDebug(): Int = range.getRefIfDebug().end() - offset

    fun <I : BcInstr> getInstr(instrClass: KClass<I>, instrs: Any): BcInstrRepr<I> {
        // Rust: debug_assert!(self.remaining_if_debug() >= mem::size_of::<BcInstrRepr<I>>())
        check(remainingIfDebug() >= BcInstrRepr.sizeOf(instrClass))
        @Suppress("UNCHECKED_CAST")
        val repr = instrs as BcInstrRepr<I>
        // Rust: debug_assert_eq!(repr.header.opcode, BcOpcode::for_instr::<I>())
        check(repr.header.opcode == BcOpcode.forInstr(instrClass))
        return repr
    }

    fun <I : BcInstr> getInstrChecked(instrClass: KClass<I>, instrs: Any): BcInstrRepr<I>? =
        if (getOpcode(instrs) == BcOpcode.forInstr(instrClass)) {
            getInstr(instrClass, instrs)
        } else {
            null
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

    private fun subInt(offset: Int): BcPtrAddr = new(this.offset - offset, this.range)

    fun sub(start: BcAddr): BcPtrAddr = subInt(start.value.toInt())

    fun offset(addr: BcAddr): BcPtrAddr = add(addr.value.toInt())

    fun addRel(rel: BcAddrOffset): BcPtrAddr = add(rel.value.toInt())

    fun addRelNeg(rel: BcAddrOffsetNeg): BcPtrAddr = subInt(rel.value.toInt())

    fun add(offset: Int): BcPtrAddr = new(this.offset + offset, this.range)

    fun <I : BcInstr> addInstr(instrClass: KClass<I>): BcPtrAddr = addRel(BcAddrOffset.forInstr(instrClass))
}

/** Difference between addresses. */
data class BcAddrOffset(
    val value: UInt,
) : Comparable<BcAddrOffset> {
    override fun toString(): String = value.toString()

    override fun compareTo(other: BcAddrOffset): Int = value.compareTo(other.value)

    companion object {
        /** Pointer to not yet known address. */
        val FORWARD = BcAddrOffset(0xdeadbeefu)

        /** Size of an instruction. */
        fun <I : BcInstr> forInstr(instrClass: KClass<I>): BcAddrOffset {
            BcInstrRepr.assertAlign(instrClass)
            return BcAddrOffset(BcInstrRepr.sizeOf(instrClass).toUInt())
        }
    }

    fun neg(): BcAddrOffsetNeg = BcAddrOffsetNeg(this.value)
}

/** Negative difference between addresses. */
data class BcAddrOffsetNeg(
    val value: UInt,
) : Comparable<BcAddrOffsetNeg> {
    override fun toString(): String = value.toString()

    override fun compareTo(other: BcAddrOffsetNeg): Int = value.compareTo(other.value)
}
