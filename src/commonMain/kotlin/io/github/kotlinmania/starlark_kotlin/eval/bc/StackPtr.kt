// port-lint: source src/eval/bc/stack_ptr.rs
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
 * Stack pointer.
 */

// use std::ops::Add;
// use dupe::Dupe;

/**
 * Index of the slot in the function frame.
 * This can be both a local variable or a temporary.
 * When reading local variable, it must be definitely initialized (e.g. function parameter).
 */
// #[derive(Copy, Clone, Dupe, Debug, PartialOrd, Ord, PartialEq, Eq, Hash, derive_more::Display)]
// #[display("&{}", _0)]
// pub(crate) struct BcSlot(pub(crate) u32);
internal data class BcSlot(val value: UInt) : Comparable<BcSlot> {

    // impl BcSlot

    // pub(crate) fn to_in(self) -> BcSlotIn
    fun toIn(): BcSlotIn = BcSlotIn(this)

    // pub(crate) fn to_out(self) -> BcSlotOut
    fun toOut(): BcSlotOut = BcSlotOut(this)

    // impl Add<u32> for BcSlot
    // fn add(self, rhs: u32) -> BcSlot
    operator fun plus(rhs: UInt): BcSlot = BcSlot(value + rhs)

    override fun compareTo(other: BcSlot): Int = value.compareTo(other.value)

    // #[display("&{}", _0)]
    override fun toString(): String = "&$value"
}

/**
 * [N] slots starting with given number.
 */
// #[derive(Copy, Clone, Dupe, Debug)]
// pub(crate) struct BcSlotsN<const N: usize> { pub(crate) start: BcSlot }
// TODO: stub - BcSlotsN needs real import
internal class BcSlotsN(
    /** The const generic N parameter from Rust. */
    val n: Int,
    /** [n] slots starting with given slot. */
    // pub(crate) start: BcSlot,
    val start: BcSlot,
) {
    // impl<const N: usize> BcSlotsN<N>

    // pub(crate) fn get<const I: u32>(self) -> BcSlot
    fun get(i: UInt): BcSlot {
        // assert!((I as usize) < N);
        check(i.toInt() < n)
        return start + i
    }

    companion object {
        // pub(crate) fn from_range(range: BcSlotRange) -> BcSlotsN<N>
        fun fromRange(n: Int, range: BcSlotRange): BcSlotsN {
            // assert_eq!(N, range.len() as usize);
            check(n == range.len().toInt())
            return BcSlotsN(n, range.start)
        }
    }
}

// #[derive(Copy, Clone, Dupe, Debug, derive_more::Display)]
// #[display("{}..{}", start, end)]
// pub(crate) struct BcSlotRange { pub(crate) start: BcSlot, pub(crate) end: BcSlot }
// TODO: stub - BcSlotRange needs real import
internal data class BcSlotRange(
    val start: BcSlot,
    val end: BcSlot,
) {
    // impl BcSlotRange

    // pub(crate) fn len(self) -> u32
    fun len(): UInt = end.value - start.value

    // pub(crate) fn iter(self) -> impl Iterator<Item = BcSlot>
    fun iter(): Sequence<BcSlot> =
        // (self.start.0..self.end.0).map(BcSlot)
        (start.value..<end.value).asSequence().map { BcSlot(it) }

    // pub(crate) fn to_in(self) -> BcSlotInRange
    fun toIn(): BcSlotInRange = BcSlotInRange(
        start = start.toIn(),
        end = end.toIn(),
    )

    // #[display("{}..{}", start, end)]
    override fun toString(): String = "$start..$end"
}

/**
 * Slot containing a value.
 *
 * The slot may be a local variable, so this slot cannot be used to store a temporary value.
 */
// #[derive(Debug, Copy, Clone, Dupe, derive_more::Display, PartialEq, Eq)]
// pub(crate) struct BcSlotIn(BcSlot);
internal data class BcSlotIn(val slot: BcSlot) {

    // impl Add<u32> for BcSlotIn
    // fn add(self, rhs: u32) -> BcSlotIn
    operator fun plus(rhs: UInt): BcSlotIn = BcSlotIn(slot + rhs)

    // impl BcSlotIn

    /**
     * Take the slot.
     *
     * This operation need to be used carefully: this slot cannot be used for writing.
     */
    // pub(crate) fn get(self) -> BcSlot
    fun get(): BcSlot = slot

    override fun toString(): String = slot.toString()
}

// #[derive(Copy, Clone, Dupe, Debug, derive_more::Display)]
// #[display("{}..{}", start, end)]
// pub(crate) struct BcSlotInRange { pub(crate) start: BcSlotIn, pub(crate) end: BcSlotIn }
internal data class BcSlotInRange(
    var start: BcSlotIn,
    var end: BcSlotIn,
) {
    // impl BcSlotInRange

    // pub(crate) fn len(self) -> u32
    fun len(): UInt = end.slot.value - start.slot.value

    // pub(crate) fn to_range_from(self) -> BcSlotInRangeFrom
    fun toRangeFrom(): BcSlotInRangeFrom = BcSlotInRangeFrom(start)

    // pub(crate) fn iter(self) -> impl Iterator<Item = BcSlotIn>
    fun iter(): Sequence<BcSlotIn> =
        // (self.start.0.0..self.end.0.0).map(|s| BcSlotIn(BcSlot(s)))
        (start.slot.value..<end.slot.value).asSequence().map { BcSlotIn(BcSlot(it)) }

    /**
     * Add an element to the slot range if possible.
     */
    // pub(crate) fn try_push(&mut self, slot: BcSlotIn) -> bool
    fun tryPush(slot: BcSlotIn): Boolean {
        return if (len() == 0u) {
            // *self = BcSlotInRange { start: slot, end: slot + 1 };
            start = slot
            end = slot + 1u
            true
        } else if (end == slot) {
            // self.end = slot + 1;
            end = slot + 1u
            true
        } else {
            false
        }
    }

    // #[display("{}..{}", start, end)]
    override fun toString(): String = "$start..$end"

    companion object {
        // impl Default for BcSlotInRange
        // fn default() -> Self
        fun default(): BcSlotInRange = BcSlotInRange(
            start = BcSlotIn(BcSlot(0u)),
            end = BcSlotIn(BcSlot(0u)),
        )
    }
}

// #[derive(Copy, Clone, Dupe, Debug)]
// pub(crate) struct BcSlotInRangeFrom(pub(crate) BcSlotIn);
internal data class BcSlotInRangeFrom(val start: BcSlotIn) {

    // impl BcSlotInRangeFrom

    // pub(crate) fn to_range(self, len: u32) -> BcSlotInRange
    fun toRange(len: UInt): BcSlotInRange = BcSlotInRange(
        start = start,
        end = start + len,
    )
}

/**
 * Slot where the value should be stored.
 *
 * The slot may be a local variable, so this slot cannot be used to store a temporary value.
 */
// #[derive(Debug, Copy, Clone, Dupe, derive_more::Display)]
// pub(crate) struct BcSlotOut(BcSlot);
internal data class BcSlotOut(val slot: BcSlot) {

    // impl BcSlotOut

    // pub(crate) fn get(self) -> BcSlot
    fun get(): BcSlot = slot

    override fun toString(): String = slot.toString()
}
