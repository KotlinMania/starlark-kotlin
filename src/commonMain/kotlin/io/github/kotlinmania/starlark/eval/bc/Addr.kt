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
// #[derive(Eq, PartialEq, Copy, Clone, Dupe, Debug, PartialOrd, Ord, Display, Hash, Default)]
// pub(crate) struct BcAddr(pub(crate) u32);
data class BcAddr(val value: UInt) : Comparable<BcAddr> {
    constructor() : this(0u)

    override fun toString(): String = "@$value"

    override fun compareTo(other: BcAddr): Int = value.compareTo(other.value)

    // impl BcAddr

    // pub(crate) fn offset_from(self, start: BcAddr) -> BcAddrOffset
    fun offsetFrom(start: BcAddr): BcAddrOffset {
        require(this >= start)
        return BcAddrOffset(this.value - start.value)
    }

    // pub(crate) fn offset(self, offset: BcAddrOffset) -> BcAddr
    fun offset(offset: BcAddrOffset): BcAddr {
        return BcAddr(this.value + offset.value)
    }

    // pub(crate) fn offset_neg(self, offset: BcAddrOffsetNeg) -> BcAddr
    fun offsetNeg(offset: BcAddrOffsetNeg): BcAddr {
        return BcAddr(this.value - offset.value)
    }

    // impl Sub<u32> for BcAddr
    operator fun minus(rhs: UInt): BcAddr {
        check(this.value >= rhs)
        return BcAddr(this.value - rhs)
    }

    // impl Add<u32> for BcAddr
    operator fun plus(rhs: UInt): BcAddr {
        return BcAddr(this.value + rhs)
    }

    // impl AddAssign<u32> for BcAddr
    // Note: BcAddr is immutable in Kotlin (data class), so no += operator.
    // Use reassignment: addr = addr + rhs
}

/**
 * Valid pointer range of bytecode.
 * Used for debugging assertions. This object is not created in release mode.
 */
// #[derive(Copy, Clone, Dupe, Debug, PartialEq)]
// pub(crate) struct BcPtrRange
data class BcPtrRange(
    // start: *const u8,
    val start: Int,
    /** Length in bytes. */
    // len: usize,
    val len: Int,
) {
    companion object {
        // pub(crate) fn for_slice(slice: &[u64]) -> BcPtrRange
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

    // pub(crate) fn assert_in_range(&self, ptr: *const u8)
    fun assertInRange(offset: Int) {
        check(offset >= 0)
        check(offset <= len)
    }

    // fn end(&self) -> *const u8
    fun end(): Int {
        return start + len
    }
}

/**
 * Pointer to an instruction in memory.
 *
 * In Rust, this is a raw pointer with debug range checks.
 * In Kotlin, this is an offset into a bytecode buffer with debug validation.
 */
// pub(crate) struct BcPtrAddr<'b>
data class BcPtrAddr(
    // ptr: *const u8
    val offset: Int,
    /** When assertions enabled, we validate the pointer is in this range. */
    // range: IfDebug<BcPtrRange>
    val range: IfDebug<BcPtrRange>,
) : Comparable<BcPtrAddr> {
    override fun compareTo(other: BcPtrAddr): Int = offset.compareTo(other.offset)

    companion object {
        // unsafe fn new(ptr: *const u8, range: IfDebug<BcPtrRange>) -> BcPtrAddr<'b>
        fun new(offset: Int, range: IfDebug<BcPtrRange>): BcPtrAddr {
            check(offset % BC_INSTR_ALIGN == 0)
            range.ifDebug { it.assertInRange(offset) }
            return BcPtrAddr(offset, range)
        }

        /** Create a pointer for the beginning of the slice. */
        // pub(crate) fn for_slice_start(slice: &'b [u64]) -> BcPtrAddr<'b>
        fun forSliceStart(slice: LongArray): BcPtrAddr {
            return new(
                0,
                IfDebug.new(BcPtrRange.forSlice(slice)),
            )
        }

        /** Create a pointer for the end of the slice. */
        // pub(crate) fn for_slice_end(slice: &'b [u64]) -> BcPtrAddr<'b>
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
    // fn remaining_if_debug(self) -> usize
    private fun remainingIfDebug(): Int {
        return range.getRefIfDebug().end() - offset
    }

    // pub(crate) fn get_instr<I: BcInstr>(self) -> &'b BcInstrRepr<I>
    fun <I : BcInstr> getInstr(instrClass: KClass<I>, instrs: Any): BcInstrRepr<I> {
        // Rust: debug_assert!(self.remaining_if_debug() >= mem::size_of::<BcInstrRepr<I>>())
        check(remainingIfDebug() >= BcInstrRepr.sizeOf(instrClass))
        @Suppress("UNCHECKED_CAST")
        val repr = instrs as BcInstrRepr<I>
        // Rust: debug_assert_eq!(repr.header.opcode, BcOpcode::for_instr::<I>())
        check(repr.header.opcode == BcOpcode.forInstr(instrClass))
        return repr
    }

    // pub(crate) fn get_instr_checked<I: BcInstr>(self) -> Option<&'b BcInstrRepr<I>>
    fun <I : BcInstr> getInstrChecked(instrClass: KClass<I>, instrs: Any): BcInstrRepr<I>? {
        return if (getOpcode(instrs) == BcOpcode.forInstr(instrClass)) {
            getInstr(instrClass, instrs)
        } else {
            null
        }
    }

    // pub(crate) fn get_opcode(self) -> BcOpcode
    fun getOpcode(instrs: Any): BcOpcode {
        val header = instrs as BcInstrHeader
        return header.opcode
    }

    // pub(crate) fn offset_from(self, start: BcPtrAddr) -> BcAddr
    fun offsetFrom(start: BcPtrAddr): BcAddr {
        val diff = this.offset - start.offset
        check(diff >= 0)
        check(diff <= Int.MAX_VALUE)
        return BcAddr(diff.toUInt())
    }

    // fn sub_usize(self, offset: usize) -> BcPtrAddr<'b>
    private fun subInt(offset: Int): BcPtrAddr {
        return new(this.offset - offset, this.range)
    }

    // pub(crate) fn sub(self, start: BcAddr) -> BcPtrAddr<'b>
    fun sub(start: BcAddr): BcPtrAddr {
        return subInt(start.value.toInt())
    }

    // pub(crate) fn offset(self, addr: BcAddr) -> BcPtrAddr<'b>
    fun offset(addr: BcAddr): BcPtrAddr {
        return add(addr.value.toInt())
    }

    // pub(crate) fn add_rel(self, rel: BcAddrOffset) -> BcPtrAddr<'b>
    fun addRel(rel: BcAddrOffset): BcPtrAddr {
        return add(rel.value.toInt())
    }

    // pub(crate) fn add_rel_neg(self, rel: BcAddrOffsetNeg) -> BcPtrAddr<'b>
    fun addRelNeg(rel: BcAddrOffsetNeg): BcPtrAddr {
        return subInt(rel.value.toInt())
    }

    // pub(crate) fn add(self, offset: usize) -> BcPtrAddr<'b>
    fun add(offset: Int): BcPtrAddr {
        return new(this.offset + offset, this.range)
    }

    // pub(crate) fn add_instr<I: BcInstr>(self) -> BcPtrAddr<'b>
    fun <I : BcInstr> addInstr(instrClass: KClass<I>): BcPtrAddr {
        return addRel(BcAddrOffset.forInstr(instrClass))
    }
}

/** Difference between addresses. */
// #[derive(Eq, PartialEq, Copy, Clone, Dupe, Debug, PartialOrd, Ord, Display)]
// pub(crate) struct BcAddrOffset(pub(crate) u32);
data class BcAddrOffset(val value: UInt) : Comparable<BcAddrOffset> {
    override fun toString(): String = value.toString()

    override fun compareTo(other: BcAddrOffset): Int = value.compareTo(other.value)

    companion object {
        /** Pointer to not yet known address. */
        // pub(crate) const FORWARD: BcAddrOffset = BcAddrOffset(0xdeadbeef);
        val FORWARD = BcAddrOffset(0xdeadbeefu)

        /** Size of an instruction. */
        // fn for_instr<I: BcInstr>() -> BcAddrOffset
        fun <I : BcInstr> forInstr(instrClass: KClass<I>): BcAddrOffset {
            BcInstrRepr.assertAlign(instrClass)
            return BcAddrOffset(BcInstrRepr.sizeOf(instrClass).toUInt())
        }
    }

    // pub(crate) fn neg(self) -> BcAddrOffsetNeg
    fun neg(): BcAddrOffsetNeg {
        return BcAddrOffsetNeg(this.value)
    }
}

/** Negative difference between addresses. */
// #[derive(Eq, PartialEq, Copy, Clone, Dupe, Debug, PartialOrd, Ord, Display)]
// pub(crate) struct BcAddrOffsetNeg(pub(crate) u32);
data class BcAddrOffsetNeg(val value: UInt) : Comparable<BcAddrOffsetNeg> {
    override fun toString(): String = value.toString()

    override fun compareTo(other: BcAddrOffsetNeg): Int = value.compareTo(other.value)
}
