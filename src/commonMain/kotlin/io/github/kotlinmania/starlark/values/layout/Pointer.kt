// port-lint: source src/values/layout/pointer.rs
package io.github.kotlinmania.starlark.values.layout

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

// Rust uses pointer tagging on the bottom three bits:
// ?00 => frozen pointer
// ?01 => mutable pointer
// ?10 => int (32 bit)
// third bit is a tag set by the user (get_user_tag)
//
// Kotlin: GC handles all memory. We simulate pointer tagging using
// an enum for the tag and a backing array for identity-based indexing.

private const val TAG_BITS: Int = 3
private const val TAG_MASK: Int = 0b111

private const val TAG_INT: Int = 0b010

private const val TAG_STR: Int = 0b100

private const val TAG_UNFROZEN: Int = 0b001

private const val TAG_NICHE: Int = 0b1

// Kotlin: Long is 64 bits, InlineInt.BITS is 32
private const val INT_SHIFT: Int = 64 - 32 // = 32

private const val INT_DATA_MASK: Long = ((1L shl 32) - 1L) shl INT_SHIFT

/** All possible tag values, three least significant bits of a pointer. */
// #[repr(usize)]
internal enum class PointerTags(
    val bits: Int,
) {
    Int(TAG_INT),
    StrUnfrozen(TAG_STR or TAG_UNFROZEN),
    StrFrozen(TAG_STR),
    OtherUnfrozen(TAG_UNFROZEN),
    OtherFrozen(0),
    ;

    companion object {
        fun fromUsize(x: Int): PointerTags = entries.first { it.bits == x }

        fun fromPointer(ptr: RawPointer): PointerTags = fromUsize(ptr.ptrValue().toInt() and TAG_MASK)
    }

    /** String value, frozen or not. */
    fun isStr(): Boolean = bits and TAG_STR != 0

    /** Inline integer. */
    fun isInt(): Boolean = this == Int

    /** Not frozen, not an integer. */
    fun isUnfrozen(): Boolean = bits and TAG_UNFROZEN != 0
}

/** All possible tag values for frozen pointers. */
private enum class FrozenPointerTags(
    val bits: Int,
) {
    Int(TAG_INT),
    Str(TAG_STR),
    Other(0),
}

// Kotlin: We simulate raw tagged pointers using a Long for the tagged value.
// The actual AValueHeader/AValueOrForward references are stored in a
// side table (the heap's arena list), and the "pointer" is an index
// combined with tag bits—following the user's recommendation to use
// array indices as simulated pointers.

/** Tagged pointer logically equivalent to `*mut AValueHeader`. */
internal class RawPointer private constructor(
    // Kotlin: stores the tagged value as a Long.
    // For int tags: upper bits hold InlineInt, lower bits hold TAG_INT.
    // For pointer tags: upper bits hold the index, lower bits hold tag.
    private val raw: Long,
) {
    init {
        require(raw != 0L) { "RawPointer cannot be zero" }
    }

    companion object {
        fun newUnchecked(ptr: Long): RawPointer = RawPointer(ptr)

        fun newInt(i: Int): RawPointer {
            val ptr = ((i.toLong()) shl INT_SHIFT) or TAG_INT.toLong()
            return newUnchecked(ptr)
        }

        fun newUnfrozen(index: Long, isString: Boolean): RawPointer {
            require(index and TAG_MASK.toLong() == 0L) { "Index must be aligned" }
            var ptr = index
            if (isString) {
                ptr = ptr or TAG_STR.toLong()
            }
            ptr = ptr or TAG_UNFROZEN.toLong()
            return newUnchecked(ptr)
        }

        fun newFrozen(index: Long, isString: Boolean): RawPointer {
            require(index and TAG_MASK.toLong() == 0L) { "Index must be aligned" }
            var ptr = index
            if (isString) {
                ptr = ptr or TAG_STR.toLong()
            }
            return newUnchecked(ptr)
        }
    }

    fun ptrValue(): Long = raw

    fun tags(): PointerTags = PointerTags.fromPointer(this)

    fun isStr(): Boolean = tags().isStr()

    fun isInt(): Boolean = tags().isInt()

    fun isUnfrozen(): Boolean = tags().isUnfrozen()

    fun unpackInt(): Int? =
        if (!isInt()) {
            null
        } else {
            unpackIntUnchecked()
        }

    /** Unpack integer when it is known to be not a pointer. */
    fun unpackIntUnchecked(): Int = ((raw) shr INT_SHIFT).toInt()

    /** Unpack the index (stripping tag bits) when known to be not an int. */
    fun unpackPtrNoIntUnchecked(): Long = raw and (TAG_STR.toLong() or TAG_UNFROZEN.toLong()).inv()

    override fun toString(): String = "RawPointer(0x${raw.toString(16)})"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RawPointer) return false
        return raw == other.raw
    }

    override fun hashCode(): Int = raw.hashCode()
}

private fun untagPointer(x: Long): Long = x and TAG_MASK.toLong().inv()

/** Pointer which may be frozen or unfrozen. */
internal class Pointer private constructor(
    private val ptr: RawPointer,
) {
    companion object {
        fun new(ptr: RawPointer): Pointer = Pointer(ptr)

        fun newUnfrozenUsizeWithStrTag(x: Long): Pointer = new(RawPointer.newUnchecked(x or TAG_UNFROZEN.toLong()))

        fun newUnfrozen(index: Long, isString: Boolean): Pointer = new(RawPointer.newUnfrozen(index, isString))
    }

    fun isStr(): Boolean = ptr.isStr()

    fun isUnfrozen(): Boolean = ptr.isUnfrozen()

    // Kotlin: returns Pair<Long?, Int?> where first is ptr index, second is int value
    // (exactly one is non-null)
    fun unpackIsInt(): Boolean = ptr.isInt()

    fun unpackPtr(): Long = ptr.unpackPtrNoIntUnchecked()

    fun unpackIntValue(): Int = ptr.unpackIntUnchecked()

    fun unpackInt(): Int? = ptr.unpackInt()

    fun unpackPtrOpt(): Long? =
        if (!ptr.isInt()) {
            untagPointer(ptr.ptrValue())
        } else {
            null
        }

    /** Unpack pointer when it is known to be not an integer. */
    fun unpackPtrNoIntUnchecked(): Long = untagPointer(ptr.ptrValue())

    /** Unpack integer when it is known to be not a pointer. */
    fun unpackPointerI32Unchecked(): Int = ptr.unpackIntUnchecked()

    fun ptrEq(other: Pointer): Boolean = ptr == other.ptr

    fun raw(): RawPointer = ptr

    // Kotlin: no lifetimes, just return same pointer
    fun castLifetime(): Pointer = Pointer(ptr)

    fun toFrozenPointerUnchecked(): FrozenPointer = FrozenPointer.new(ptr)
}

/** Pointer which is known to be frozen (immutable). */
internal class FrozenPointer private constructor(
    private val ptr: RawPointer,
) {
    companion object {
        fun new(ptr: RawPointer): FrozenPointer {
            require(!ptr.isUnfrozen()) { "FrozenPointer must not be unfrozen" }
            return FrozenPointer(ptr)
        }

        fun newFrozenUsizeWithStrTag(x: Long): FrozenPointer = new(RawPointer.newUnchecked(x))

        fun newFrozen(index: Long, isStr: Boolean): FrozenPointer = new(RawPointer.newFrozen(index, isStr))

        fun newInt(x: Int): FrozenPointer = new(RawPointer.newInt(x))
    }

    /** It is safe to bitcast `FrozenPointer` to `Pointer` but not vice versa. */
    fun toPointer(): Pointer = Pointer.new(ptr)

    fun raw(): RawPointer = ptr

    /** Unpack pointer when it is known to be not an integer. */
    fun unpackPtrNoIntUnchecked(): Long = ptr.unpackPtrNoIntUnchecked()

    /** Unpack integer when it is known to be not a pointer. */
    fun unpackPointerI32Unchecked(): Int = ptr.unpackIntUnchecked()

    /** Unpack pointer when it is known to be frozen, not an integer, not a string. */
    fun unpackPtrNoIntNoStrUnchecked(): Long = ptr.ptrValue()
}

// Tests are in commonTest, not here.
