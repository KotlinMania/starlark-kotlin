// port-lint: source src/values/layout/pointer.rs
package io.github.kotlinmania.starlark.values.layout

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

// ?00 => frozen pointer
// ?01 => mutable pointer
// ?10 => int (32 bit)
// third bit is a tag set by the user (getUserTag)

private const val TAG_BITS: Int = 3
private const val TAG_MASK: Int = 0b111

private const val TAG_INT: Int = 0b010

private const val TAG_STR: Int = 0b100

private const val TAG_UNFROZEN: Int = 0b001

private const val TAG_NICHE: Int = 0b1

private const val INT_SHIFT: Int = 64 - 32

private const val INT_DATA_MASK: Long = ((1L shl 32) - 1L) shl INT_SHIFT

/** All possible tag values, three least significant bits of a pointer. */
internal enum class PointerTags(val bits: Int) {
    Int(TAG_INT),
    StrUnfrozen(TAG_STR or TAG_UNFROZEN),
    StrFrozen(TAG_STR),
    OtherUnfrozen(TAG_UNFROZEN),
    OtherFrozen(0);

    companion object {
        fun fromUsize(x: Int): PointerTags {
            return entries.first { it.bits == x }
        }

        fun fromPointer(ptr: RawPointer): PointerTags {
            return fromUsize(ptr.ptrValue().toInt() and TAG_MASK)
        }
    }

    /** String value, frozen or not. */
    fun isStr(): Boolean {
        return bits and TAG_STR != 0
    }

    /** Inline integer. */
    fun isInt(): Boolean {
        return this == Int
    }

    /** Not frozen, not an integer. */
    fun isUnfrozen(): Boolean {
        return bits and TAG_UNFROZEN != 0
    }
}

/** All possible tag values for frozen pointers. */
private enum class FrozenPointerTags(val bits: Int) {
    Int(TAG_INT),
    Str(TAG_STR),
    Other(0);
}

/** Tagged pointer logically equivalent to `*mut AValueHeader`. */
internal class RawPointer private constructor(
    private val raw: Long,
) {
    init {
        require(raw != 0L) { "RawPointer cannot be zero" }
    }

    companion object {
        fun newUnchecked(ptr: Long): RawPointer {
            return RawPointer(ptr)
        }

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

    fun unpackInt(): Int? {
        return if (!isInt()) {
            null
        } else {
            unpackIntUnchecked()
        }
    }

    /** Unpack integer when it is known to be not a pointer. */
    fun unpackIntUnchecked(): Int {
        return ((raw) shr INT_SHIFT).toInt()
    }

    /** Unpack the index (stripping tag bits) when known to be not an int. */
    fun unpackPtrNoIntUnchecked(): Long {
        return raw and (TAG_STR.toLong() or TAG_UNFROZEN.toLong()).inv()
    }

    override fun toString(): String = "RawPointer(0x${raw.toString(16)})"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RawPointer) return false
        return raw == other.raw
    }

    override fun hashCode(): Int = raw.hashCode()
}

private fun untagPointer(x: Long): Long {
    return x and TAG_MASK.toLong().inv()
}

/** Pointer which may be frozen or unfrozen. */
internal class Pointer private constructor(
    private val ptr: RawPointer,
) {
    companion object {
        fun new(ptr: RawPointer): Pointer {
            return Pointer(ptr)
        }

        fun newUnfrozenUsizeWithStrTag(x: Long): Pointer {
            return new(RawPointer.newUnchecked(x or TAG_UNFROZEN.toLong()))
        }

        fun newUnfrozen(index: Long, isString: Boolean): Pointer {
            return new(RawPointer.newUnfrozen(index, isString))
        }
    }

    fun isStr(): Boolean = ptr.isStr()

    fun isUnfrozen(): Boolean = ptr.isUnfrozen()

    fun unpackIsInt(): Boolean = ptr.isInt()

    fun unpackPtr(): Long {
        return ptr.unpackPtrNoIntUnchecked()
    }

    fun unpackIntValue(): Int {
        return ptr.unpackIntUnchecked()
    }

    fun unpackInt(): Int? = ptr.unpackInt()

    fun unpackPtrOpt(): Long? {
        return if (!ptr.isInt()) {
            untagPointer(ptr.ptrValue())
        } else {
            null
        }
    }

    /** Unpack pointer when it is known to be not an integer. */
    fun unpackPtrNoIntUnchecked(): Long {
        return untagPointer(ptr.ptrValue())
    }

    /** Unpack integer when it is known to be not a pointer. */
    fun unpackPointerI32Unchecked(): Int {
        return ptr.unpackIntUnchecked()
    }

    fun ptrEq(other: Pointer): Boolean = ptr == other.ptr

    fun raw(): RawPointer = ptr

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

        fun newFrozenUsizeWithStrTag(x: Long): FrozenPointer {
            return new(RawPointer.newUnchecked(x))
        }

        fun newFrozen(index: Long, isStr: Boolean): FrozenPointer {
            return new(RawPointer.newFrozen(index, isStr))
        }

        fun newInt(x: Int): FrozenPointer {
            return new(RawPointer.newInt(x))
        }
    }

    /** It is safe to bitcast `FrozenPointer` to `Pointer` but not vice versa. */
    fun toPointer(): Pointer = Pointer.new(ptr)

    fun raw(): RawPointer = ptr

    /** Unpack pointer when it is known to be not an integer. */
    fun unpackPtrNoIntUnchecked(): Long {
        return ptr.unpackPtrNoIntUnchecked()
    }

    /** Unpack integer when it is known to be not a pointer. */
    fun unpackPointerI32Unchecked(): Int {
        return ptr.unpackIntUnchecked()
    }

    /** Unpack pointer when it is known to be frozen, not an integer, not a string. */
    fun unpackPtrNoIntNoStrUnchecked(): Long {
        return ptr.ptrValue()
    }
}

/**
 * Runtime regression guard verifying [FrozenPointer] handed out in one scope
 * continues to point at the same underlying [RawPointer] when it crosses
 * coroutine boundaries. Throws if the value loses identity across the
 * round-trip.
 */
internal fun testLifetimeCovariant() {
    kotlinx.coroutines.runBlocking {
        val original = FrozenPointer.newInt(42)
        val expectedRaw = original.raw().ptrValue()

        val channel = kotlinx.coroutines.channels.Channel<FrozenPointer>(1)
        val received = kotlinx.coroutines.async(kotlinx.coroutines.Dispatchers.Default) {
            val p = channel.receive()
            p.raw().ptrValue()
        }
        launch(kotlinx.coroutines.Dispatchers.Default) { channel.send(original) }
        val actualRaw = received.await()
        channel.close()

        check(expectedRaw == actualRaw) {
            "FrozenPointer did not survive Channel round-trip: expected=$expectedRaw actual=$actualRaw"
        }
    }
}
