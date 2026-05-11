<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/values/layout/Pointer.kt
// port-lint: source values/layout/pointer.rs
package io.github.kotlinmania.starlark.values.layout
=======
// port-lint: source src/values/layout/pointer.rs
package io.github.kotlinmania.starlark_kotlin.values.layout
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/values/layout/Pointer.kt

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

// const TAG_BITS: usize = 3;
// const TAG_MASK: usize = 0b111;
private const val TAG_BITS: Int = 3
private const val TAG_MASK: Int = 0b111

// const TAG_INT: usize = 0b010;
private const val TAG_INT: Int = 0b010

// const TAG_STR: usize = 0b100;
private const val TAG_STR: Int = 0b100

// const TAG_UNFROZEN: usize = 0b001;
private const val TAG_UNFROZEN: Int = 0b001

// const TAG_NICHE: usize = 0b1;
private const val TAG_NICHE: Int = 0b1

// const INT_SHIFT: usize = mem::size_of::<usize>() * 8 - InlineInt::BITS;
// Kotlin: Long is 64 bits, InlineInt.BITS is 32
private const val INT_SHIFT: Int = 64 - 32 // = 32

// const INT_DATA_MASK: usize = ((1usize << InlineInt::BITS) - 1) << INT_SHIFT;
private const val INT_DATA_MASK: Long = ((1L shl 32) - 1L) shl INT_SHIFT

/** All possible tag values, three least significant bits of a pointer. */
// #[repr(usize)]
// pub(crate) enum PointerTags { Int, StrUnfrozen, StrFrozen, OtherUnfrozen, OtherFrozen }
internal enum class PointerTags(val bits: Int) {
    Int(TAG_INT),
    StrUnfrozen(TAG_STR or TAG_UNFROZEN),
    StrFrozen(TAG_STR),
    OtherUnfrozen(TAG_UNFROZEN),
    OtherFrozen(0);

    companion object {
        // unsafe fn from_usize_unchecked(x: usize) -> Self
        fun fromUsize(x: Int): PointerTags {
            return entries.first { it.bits == x }
        }

        // fn from_pointer(ptr: RawPointer) -> Self
        fun fromPointer(ptr: RawPointer): PointerTags {
            return fromUsize(ptr.ptrValue().toInt() and TAG_MASK)
        }
    }

    /** String value, frozen or not. */
    // fn is_str(self) -> bool
    fun isStr(): Boolean {
        return bits and TAG_STR != 0
    }

    /** Inline integer. */
    // fn is_int(self) -> bool
    fun isInt(): Boolean {
        return this == Int
    }

    /** Not frozen, not an integer. */
    // fn is_unfrozen(self) -> bool
    fun isUnfrozen(): Boolean {
        return bits and TAG_UNFROZEN != 0
    }
}

/** All possible tag values for frozen pointers. */
// enum _FrozenPointerTags { Int, Str, Other }
private enum class FrozenPointerTags(val bits: Int) {
    Int(TAG_INT),
    Str(TAG_STR),
    Other(0);
}

// Kotlin: We simulate raw tagged pointers using a Long for the tagged value.
// The actual AValueHeader/AValueOrForward references are stored in a
// side table (the heap's arena list), and the "pointer" is an index
// combined with tag bits—following the user's recommendation to use
// array indices as simulated pointers.

/** Tagged pointer logically equivalent to `*mut AValueHeader`. */
// #[derive(Clone, Copy, Dupe, PartialEq, Eq, Hash, Allocative)]
// pub(crate) struct RawPointer(pub(crate) NonZeroUsize);
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
        // unsafe fn new_unchecked(ptr: usize) -> RawPointer
        fun newUnchecked(ptr: Long): RawPointer {
            return RawPointer(ptr)
        }

        // pub(crate) fn new_int(i: InlineInt) -> RawPointer
        fun newInt(i: Int): RawPointer {
            val ptr = ((i.toLong()) shl INT_SHIFT) or TAG_INT.toLong()
            return newUnchecked(ptr)
        }

        // pub(crate) fn new_unfrozen(ptr: &AValueHeader, is_string: bool) -> RawPointer
        fun newUnfrozen(index: Long, isString: Boolean): RawPointer {
            require(index and TAG_MASK.toLong() == 0L) { "Index must be aligned" }
            var ptr = index
            if (isString) {
                ptr = ptr or TAG_STR.toLong()
            }
            ptr = ptr or TAG_UNFROZEN.toLong()
            return newUnchecked(ptr)
        }

        // pub(crate) fn new_frozen(ptr: &AValueHeader, is_string: bool) -> RawPointer
        fun newFrozen(index: Long, isString: Boolean): RawPointer {
            require(index and TAG_MASK.toLong() == 0L) { "Index must be aligned" }
            var ptr = index
            if (isString) {
                ptr = ptr or TAG_STR.toLong()
            }
            return newUnchecked(ptr)
        }
    }

    // pub(crate) fn ptr_value(self) -> usize
    fun ptrValue(): Long = raw

    // pub(crate) fn tags(self) -> PointerTags
    fun tags(): PointerTags = PointerTags.fromPointer(this)

    // pub(crate) fn is_str(self) -> bool
    fun isStr(): Boolean = tags().isStr()

    // pub(crate) fn is_int(self) -> bool
    fun isInt(): Boolean = tags().isInt()

    // pub(crate) fn is_unfrozen(self) -> bool
    fun isUnfrozen(): Boolean = tags().isUnfrozen()

    // pub(crate) fn unpack_int(self) -> Option<InlineInt>
    fun unpackInt(): Int? {
        return if (!isInt()) {
            null
        } else {
            unpackIntUnchecked()
        }
    }

    /** Unpack integer when it is known to be not a pointer. */
    // pub(crate) unsafe fn unpack_int_unchecked(self) -> InlineInt
    fun unpackIntUnchecked(): Int {
        return ((raw) shr INT_SHIFT).toInt()
    }

    /** Unpack the index (stripping tag bits) when known to be not an int. */
    // pub(crate) unsafe fn unpack_ptr_no_int_unchecked(self) -> &'v AValueOrForward
    fun unpackPtrNoIntUnchecked(): Long {
        return raw and (TAG_STR.toLong() or TAG_UNFROZEN.toLong()).inv()
    }

    // impl Debug for RawPointer
    override fun toString(): String = "RawPointer(0x${raw.toString(16)})"

    // impl PartialEq for RawPointer
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RawPointer) return false
        return raw == other.raw
    }

    // impl Hash for RawPointer
    override fun hashCode(): Int = raw.hashCode()
}

// unsafe fn untag_pointer<'a>(x: usize) -> &'a AValueOrForward
private fun untagPointer(x: Long): Long {
    return x and TAG_MASK.toLong().inv()
}

/** Pointer which may be frozen or unfrozen. */
// pub(crate) struct Pointer<'p> { ptr: RawPointer, _phantom: PhantomData<*mut &'p ()> }
internal class Pointer private constructor(
    private val ptr: RawPointer,
) {
    companion object {
        // unsafe fn new(ptr: RawPointer) -> Pointer<'p>
        fun new(ptr: RawPointer): Pointer {
            return Pointer(ptr)
        }

        // pub(crate) unsafe fn new_unfrozen_usize_with_str_tag(x: usize) -> Self
        fun newUnfrozenUsizeWithStrTag(x: Long): Pointer {
            return new(RawPointer.newUnchecked(x or TAG_UNFROZEN.toLong()))
        }

        // pub(crate) fn new_unfrozen(x: &'p AValueHeader, is_string: bool) -> Self
        fun newUnfrozen(index: Long, isString: Boolean): Pointer {
            return new(RawPointer.newUnfrozen(index, isString))
        }
    }

    // pub(crate) fn is_str(self) -> bool
    fun isStr(): Boolean = ptr.isStr()

    // pub(crate) fn is_unfrozen(self) -> bool
    fun isUnfrozen(): Boolean = ptr.isUnfrozen()

    // pub(crate) fn unpack(self) -> Either<&'p AValueOrForward, &'static PointerI32>
    // Kotlin: returns Pair<Long?, Int?> where first is ptr index, second is int value
    // (exactly one is non-null)
    fun unpackIsInt(): Boolean = ptr.isInt()

    fun unpackPtr(): Long {
        return ptr.unpackPtrNoIntUnchecked()
    }

    fun unpackIntValue(): Int {
        return ptr.unpackIntUnchecked()
    }

    // pub(crate) fn unpack_int(self) -> Option<InlineInt>
    fun unpackInt(): Int? = ptr.unpackInt()

    // pub(crate) fn unpack_ptr(self) -> Option<&'p AValueOrForward>
    fun unpackPtrOpt(): Long? {
        return if (!ptr.isInt()) {
            untagPointer(ptr.ptrValue())
        } else {
            null
        }
    }

    /** Unpack pointer when it is known to be not an integer. */
    // pub(crate) unsafe fn unpack_ptr_no_int_unchecked(self) -> &'p AValueOrForward
    fun unpackPtrNoIntUnchecked(): Long {
        return untagPointer(ptr.ptrValue())
    }

    /** Unpack integer when it is known to be not a pointer. */
    // pub(crate) unsafe fn unpack_pointer_i32_unchecked(self) -> &'static PointerI32
    fun unpackPointerI32Unchecked(): Int {
        return ptr.unpackIntUnchecked()
    }

    // pub(crate) fn ptr_eq(self, other: Pointer<'_>) -> bool
    fun ptrEq(other: Pointer): Boolean = ptr == other.ptr

    // pub(crate) fn raw(self) -> RawPointer
    fun raw(): RawPointer = ptr

    // pub(crate) unsafe fn cast_lifetime<'p2>(self) -> Pointer<'p2>
    // Kotlin: no lifetimes, just return same pointer
    fun castLifetime(): Pointer = Pointer(ptr)

    // pub(crate) unsafe fn to_frozen_pointer_unchecked(self) -> FrozenPointer<'p>
    fun toFrozenPointerUnchecked(): FrozenPointer = FrozenPointer.new(ptr)
}

/** Pointer which is known to be frozen (immutable). */
// pub(crate) struct FrozenPointer<'p> { ptr: RawPointer, phantom: PhantomData<&'p AValueHeader> }
internal class FrozenPointer private constructor(
    private val ptr: RawPointer,
) {
    companion object {
        // pub(crate) unsafe fn new(ptr: RawPointer) -> FrozenPointer<'p>
        fun new(ptr: RawPointer): FrozenPointer {
            require(!ptr.isUnfrozen()) { "FrozenPointer must not be unfrozen" }
            return FrozenPointer(ptr)
        }

        // pub(crate) fn new_frozen_usize_with_str_tag(x: usize) -> Self
        fun newFrozenUsizeWithStrTag(x: Long): FrozenPointer {
            return new(RawPointer.newUnchecked(x))
        }

        // pub(crate) fn new_frozen(x: &'p AValueHeader, is_str: bool) -> Self
        fun newFrozen(index: Long, isStr: Boolean): FrozenPointer {
            return new(RawPointer.newFrozen(index, isStr))
        }

        // pub(crate) fn new_int(x: InlineInt) -> Self
        fun newInt(x: Int): FrozenPointer {
            return new(RawPointer.newInt(x))
        }
    }

    /** It is safe to bitcast `FrozenPointer` to `Pointer` but not vice versa. */
    // pub(crate) fn to_pointer(self) -> Pointer<'p>
    fun toPointer(): Pointer = Pointer.new(ptr)

    // pub(crate) fn raw(self) -> RawPointer
    fun raw(): RawPointer = ptr

    /** Unpack pointer when it is known to be not an integer. */
    // pub(crate) unsafe fn unpack_ptr_no_int_unchecked(self) -> &'p AValueOrForward
    fun unpackPtrNoIntUnchecked(): Long {
        return ptr.unpackPtrNoIntUnchecked()
    }

    /** Unpack integer when it is known to be not a pointer. */
    // pub(crate) unsafe fn unpack_pointer_i32_unchecked(self) -> &'static PointerI32
    fun unpackPointerI32Unchecked(): Int {
        return ptr.unpackIntUnchecked()
    }

    /** Unpack pointer when it is known to be frozen, not an integer, not a string. */
    // pub(crate) unsafe fn unpack_ptr_no_int_no_str_unchecked(self) -> &'p AValueOrForward
    fun unpackPtrNoIntNoStrUnchecked(): Long {
        return ptr.ptrValue()
    }
}

<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/values/layout/Pointer.kt
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
        val received = this.async(kotlinx.coroutines.Dispatchers.Default) {
            val p = channel.receive()
            p.raw().ptrValue()
        }
        this.launch(kotlinx.coroutines.Dispatchers.Default) { channel.send(original) }
        val actualRaw = received.await()
        channel.close()

        check(expectedRaw == actualRaw) {
            "FrozenPointer did not survive Channel round-trip: expected=$expectedRaw actual=$actualRaw"
        }
    }
}
=======
// #[cfg(test)] #[test] fn test_int_tag()
// Tests are in commonTest, not here.
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/values/layout/Pointer.kt
