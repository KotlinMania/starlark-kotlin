// port-lint: source src/eval/bc/stack_ptr.rs
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
 * Stack pointer.
 */

/**
 * Index of the slot in the function frame.
 * This can be both a local variable or a temporary.
 * When reading local variable, it must be definitely initialized (e.g. function parameter).
 */
data class BcSlot(
    val index: UInt,
) : Comparable<BcSlot> {
    fun toIn(): BcSlotIn = BcSlotIn(this)

    fun toOut(): BcSlotOut = BcSlotOut(this)

    operator fun plus(rhs: UInt): BcSlot = BcSlot(index + rhs)

    override fun compareTo(other: BcSlot): Int = index.compareTo(other.index)

    override fun toString(): String = "&$index"
}

/**
 * [N] slots starting with given number.
 */
class BcSlotsN(
    /** The const generic N parameter from Rust. */
    val n: Int,
    /** [n] slots starting with given slot. */
    val start: BcSlot,
) {
    fun get(i: UInt): BcSlot {
        // assert!((I as usize) < N);
        check(i.toInt() < n)
        return start + i
    }

    fun get(i: Int): BcSlot = get(i.toUInt())

    companion object {
        fun fromRange(n: Int, range: BcSlotRange): BcSlotsN {
            // assert_eq!(N, range.len() as usize);
            check(n == range.len().toInt())
            return BcSlotsN(n, range.start)
        }
    }
}

data class BcSlotRange(
    val start: BcSlot,
    val end: BcSlot,
) : Iterable<BcSlot> {
    fun len(): UInt = end.index - start.index

    fun iter(): Sequence<BcSlot> =
        // (self.start.0..self.end.0).map(BcSlot)
        (start.index..<end.index).asSequence().map { BcSlot(it) }

    override fun iterator(): Iterator<BcSlot> = iter().iterator()

    fun toIn(): BcSlotInRange =
        BcSlotInRange(
            start = start.toIn(),
            end = end.toIn(),
        )

    override fun toString(): String = "$start..$end"
}

/**
 * Slot containing a value.
 *
 * The slot may be a local variable, so this slot cannot be used to store a temporary value.
 */
data class BcSlotIn(
    val slot: BcSlot,
) {
    operator fun plus(rhs: UInt): BcSlotIn = BcSlotIn(slot + rhs)

    /**
     * Take the slot.
     *
     * This operation need to be used carefully: this slot cannot be used for writing.
     */
    fun get(): BcSlot = slot

    override fun toString(): String = slot.toString()
}

data class BcSlotInRange(
    var start: BcSlotIn,
    var end: BcSlotIn,
) {
    fun len(): UInt = end.slot.index - start.slot.index

    fun toRangeFrom(): BcSlotInRangeFrom = BcSlotInRangeFrom(start)

    fun iter(): Sequence<BcSlotIn> =
        // (self.start.0.0..self.end.0.0).map(|s| BcSlotIn(BcSlot(s)))
        (start.slot.index..<end.slot.index).asSequence().map { BcSlotIn(BcSlot(it)) }

    /**
     * Add an element to the slot range if possible.
     */
    fun tryPush(slot: BcSlotIn): Boolean =
        if (len() == 0u) {
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

    override fun toString(): String = "$start..$end"

    companion object {
        fun default(): BcSlotInRange =
            BcSlotInRange(
                start = BcSlotIn(BcSlot(0u)),
                end = BcSlotIn(BcSlot(0u)),
            )
    }
}

data class BcSlotInRangeFrom(
    val start: BcSlotIn,
) {
    fun toRange(len: UInt): BcSlotInRange =
        BcSlotInRange(
            start = start,
            end = start + len,
        )
}

/**
 * Slot where the value should be stored.
 *
 * The slot may be a local variable, so this slot cannot be used to store a temporary value.
 */
data class BcSlotOut(
    val slot: BcSlot,
) {
    fun get(): BcSlot = slot

    override fun toString(): String = slot.toString()
}
