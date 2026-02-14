// port-lint: source src/eval/bc/stack_ptr.rs
package io.github.kotlinmania.starlark_kotlin.eval.bc.stack_ptr

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

/// Stack pointer.

/// Index of the slot in the function frame.
/// This can be both a local variable or a temporary.
/// When reading local variable, it must be definitely initialized (e.g. function parameter).
internal data class BcSlot(val value: UInt) : Comparable<BcSlot> {
    fun toIn(): BcSlotIn = BcSlotIn(this)

    fun toOut(): BcSlotOut = BcSlotOut(this)

    operator fun plus(rhs: UInt): BcSlot = BcSlot(value + rhs)

    override fun compareTo(other: BcSlot): Int = value.compareTo(other.value)

    override fun toString(): String = "&$value"
}

/// `N` slots starting with given number.
internal class BcSlotsN(
    val n: Int,
    /// `N` slots starting with given slot.
    val start: BcSlot,
) {
    fun get(i: UInt): BcSlot {
        check(i.toInt() < n)
        return start + i
    }

    companion object {
        fun fromRange(n: Int, range: BcSlotRange): BcSlotsN {
            check(n == range.len().toInt())
            return BcSlotsN(n, range.start)
        }
    }
}

internal data class BcSlotRange(
    val start: BcSlot,
    val end: BcSlot,
) {
    fun len(): UInt = end.value - start.value

    fun iter(): Sequence<BcSlot> =
        (start.value..<end.value).asSequence().map { BcSlot(it) }

    fun toIn(): BcSlotInRange = BcSlotInRange(
        start = start.toIn(),
        end = end.toIn(),
    )

    override fun toString(): String = "$start..$end"
}

/// Slot containing a value.
///
/// The slot may be a local variable, so this slot cannot be used to store a temporary value.
internal data class BcSlotIn(val slot: BcSlot) {
    operator fun plus(rhs: UInt): BcSlotIn = BcSlotIn(slot + rhs)

    /// Take the slot.
    ///
    /// This operation need to be used carefully: this slot cannot be used for writing.
    fun get(): BcSlot = slot

    override fun toString(): String = slot.toString()
}

internal data class BcSlotInRange(
    var start: BcSlotIn,
    var end: BcSlotIn,
) {
    companion object {
        fun default(): BcSlotInRange = BcSlotInRange(
            start = BcSlotIn(BcSlot(0u)),
            end = BcSlotIn(BcSlot(0u)),
        )
    }

    fun len(): UInt = end.slot.value - start.slot.value

    fun toRangeFrom(): BcSlotInRangeFrom = BcSlotInRangeFrom(start)

    fun iter(): Sequence<BcSlotIn> =
        (start.slot.value..<end.slot.value).asSequence().map { BcSlotIn(BcSlot(it)) }

    /// Add an element to the slot range if possible.
    fun tryPush(slot: BcSlotIn): Boolean {
        return if (len() == 0u) {
            start = slot
            end = slot + 1u
            true
        } else if (end == slot) {
            end = slot + 1u
            true
        } else {
            false
        }
    }

    override fun toString(): String = "$start..$end"
}

internal data class BcSlotInRangeFrom(val start: BcSlotIn) {
    fun toRange(len: UInt): BcSlotInRange = BcSlotInRange(
        start = start,
        end = start + len,
    )
}

/// Slot where the value should be stored.
///
/// The slot may be a local variable, so this slot cannot be used to store a temporary value.
internal data class BcSlotOut(val slot: BcSlot) {
    fun get(): BcSlot = slot

    override fun toString(): String = slot.toString()
}
