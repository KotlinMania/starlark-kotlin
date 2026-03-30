// port-lint: source src/values/layout/typed/string.rs
package io.github.kotlinmania.starlark_kotlin.values.layout.typed

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

import io.github.kotlinmania.starlark_kotlin.collections.Hashed
import io.github.kotlinmania.starlark_kotlin.collections.StarlarkHashValue
import io.github.kotlinmania.starlark_kotlin.collections.StarlarkHasher
import io.github.kotlinmania.starlark_kotlin.environment.Methods
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.layout.Freezer
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.freeze_error.FreezeResult
import io.github.kotlinmania.starlark_kotlin.values.ValueError
import io.github.kotlinmania.starlark_kotlin.values.types.string.starlarkStrAt
import io.github.kotlinmania.starlark_kotlin.values.types.string.starlarkStrCollectRepr
import io.github.kotlinmania.starlark_kotlin.values.types.string.starlarkStrMul
import io.github.kotlinmania.starlark_kotlin.values.types.string.starlarkStrPercent
import io.github.kotlinmania.starlark_kotlin.values.types.string.starlarkStrSlice
import io.github.kotlinmania.starlark_kotlin.values.types.string.starlarkStrAdd
import io.github.kotlinmania.starlark_kotlin.values.types.string.strMethods

// pub struct StarlarkStr { ... }
class StarlarkStr(val value: String) : StarlarkValue {
    override val TYPE: String get() = "string"

    // pub fn as_str(&self) -> &str
    fun asStr(): String = value

    // pub fn len(&self) -> usize
    fun len(): Int = value.encodeToByteArray().size

    override fun getHash(): Result<StarlarkHashValue> = Result.success(StarlarkHashValue.new(value))

    // fn equals(&self, other: Value) -> crate::Result<bool>
    override fun equals(other: Value): Result<Boolean> {
        val otherStr = other.unpackStarlarkStr()
        return if (otherStr != null) {
            Result.success(value == otherStr.value)
        } else {
            Result.success(false)
        }
    }

    // fn compare(&self, other: Value) -> crate::Result<Ordering>
    override fun compare(other: Value): Result<Int> {
        val otherStr = other.unpackStarlarkStr()
        return if (otherStr != null) {
            Result.success(value.compareTo(otherStr.value))
        } else {
            ValueError.unsupportedWith(TYPE, "cmp()", other)
        }
    }

    // fn is_in(&self, other: Value) -> crate::Result<bool>
    override fun isIn(other: Value): Result<Boolean> {
        val s = other.unpackStarlarkStr()
            ?: return Result.failure(
                IllegalArgumentException("'in' requires string as left operand, not '${other.getType()}'")
            )
        // self is the container, other (s) is the needle
        return Result.success(value.contains(s.value))
    }

    // fn is_special(_: Private) -> bool
    override fun isSpecial(): Boolean = true

    // fn get_methods() -> Option<&'static Methods>
    override fun getMethods(): Methods? = strMethods()

    // fn collect_repr(&self, buffer: &mut String)
    override fun collectRepr(collector: StringBuilder) {
        starlarkStrCollectRepr(this, collector)
    }

    // fn to_bool(&self) -> bool
    override fun toBool(): Boolean = value.isNotEmpty()

    // fn write_hash(&self, hasher: &mut StarlarkHasher) -> crate::Result<()>
    override fun writeHash(hasher: StarlarkHasher): Result<Unit> {
        // Don't defer to str because we cache the Hash in StarlarkStr
        val hashValue = StarlarkHashValue.new(value)
        hasher.writeU32(hashValue.get())
        return Result.success(Unit)
    }

    // fn at(&self, index: Value<'v>, heap: Heap<'v>) -> crate::Result<Value<'v>>
    override fun at(index: Value, heap: Heap): Result<Value> {
        return starlarkStrAt(this, index, heap)
    }

    // fn length(&self) -> crate::Result<i32>
    override fun length(): Result<Int> {
        // In Starlark, len() returns the number of Unicode codepoints, not bytes
        return Result.success(value.length)
    }

    // fn slice(...)
    override fun slice(start: Value?, stop: Value?, stride: Value?, heap: Heap): Result<Value> {
        return starlarkStrSlice(this, start, stop, stride, heap)
    }

    // fn add(&self, other: Value<'v>, heap: Heap<'v>) -> Option<crate::Result<Value<'v>>>
    override fun add(rhs: Value, heap: Heap): Result<Value>? {
        return starlarkStrAdd(this, rhs, heap)
    }

    // fn mul(&self, other: Value<'v>, heap: Heap<'v>) -> Option<crate::Result<Value<'v>>>
    override fun mul(rhs: Value, heap: Heap): Result<Value>? {
        return starlarkStrMul(this, rhs, heap)
    }

    // fn rmul(&self, lhs: Value<'v>, heap: Heap<'v>) -> Option<crate::Result<Value<'v>>>
    override fun rmul(lhs: Value, heap: Heap): Result<Value>? = mul(lhs, heap)

    // fn percent(&self, other: Value<'v>, heap: Heap<'v>) -> crate::Result<Value<'v>>
    override fun percent(other: Value, heap: Heap): Result<Value> {
        return starlarkStrPercent(this, other, heap)
    }

    // fn typechecker_ty(&self) -> Option<Ty>
    override fun typecheckerTy(): Ty? = Ty.string()

    override fun toString(): String = value
    override fun hashCode(): Int = value.hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StarlarkStr) return false
        return value == other.value
    }

    companion object {
        // pub(crate) const UNINIT_HASH: StarlarkHashValue = StarlarkHashValue::new_unchecked(0)
        val UNINIT_HASH: StarlarkHashValue get() = StarlarkHashValue.newUnchecked(0u)

        // pub const fn payload_len_for_len(len: usize) -> usize { len.div_ceil(mem::size_of::<usize>()) }
        fun payloadLenForLen(len: Int): Int = (len + 7) / 8

        // pub(crate) fn offset_of_content() -> usize { memoffset::offset_of!(StarlarkStrN<0>, body) }
        fun offsetOfContent(): Int = 8
    }
}

// Hashed is defined in io.github.kotlinmania.starlark_kotlin.collections.Hashed
// Freezer is defined in io.github.kotlinmania.starlark_kotlin.values.layout.Freezer

/// Convenient type alias.
///
/// We use `FrozenStringValue` often, but also we define more operations
/// on `FrozenStringValue` than on generic `FrozenValueTyped<T>`.
class FrozenStringValue(
    private val str: StarlarkStr,
    private val frozenValue: FrozenValue,
) : StringValueLike, Comparable<FrozenStringValue> {

    fun asStr(): String = str.asStr()

    fun toFrozenValue(): FrozenValue = frozenValue

    fun toValue(): Value = frozenValue.toValue()

    fun getHash(): StarlarkHashValue = StarlarkHashValue.new(str.value)

    /// Get self along with the hash.
    fun getHashed(): Hashed<FrozenStringValue> {
        return Hashed.newUnchecked(getHash(), this)
    }

    /// Get the FrozenValue along with the hash.
    fun getHashedValue(): Hashed<FrozenValue> {
        return Hashed.newUnchecked(getHash(), toFrozenValue())
    }

    /// Get the string reference along with the hash.
    fun getHashedStr(): Hashed<String> {
        return Hashed.newUnchecked(getHash(), asStr())
    }

    override fun toStringValue(): StringValue {
        return StringValue(str, frozenValue.toValue())
    }

    override fun asStrValue(): String = asStr()

    override fun compareTo(other: FrozenStringValue): Int {
        return asStr().compareTo(other.asStr())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is FrozenStringValue) return str == other.str
        if (other is StringValue) return str == other.starlarkStr()
        return false
    }

    override fun hashCode(): Int = str.hashCode()

    override fun toString(): String = str.toString()

    companion object {
        fun default(): FrozenStringValue {
            return io.github.kotlinmania.starlark_kotlin.values.layout.VALUE_EMPTY_STRING.erase()
        }

        /// Construct without checking type.
        fun newUnchecked(value: FrozenValue): FrozenStringValue {
            val str = value.toValue().unpackStarlarkStr()
                ?: error("FrozenStringValue.newUnchecked: value is not a StarlarkStr")
            return FrozenStringValue(str, value)
        }

        /** Downcast a [FrozenValue] to a [FrozenStringValue], returning null if the value is not a string. */
        fun new(value: FrozenValue): FrozenStringValue? {
            val str = value.toValue().unpackStarlarkStr() ?: return null
            return FrozenStringValue(str, value)
        }
    }
}

/// Convenient type alias.
///
/// We use `StringValue` often, but also we define more operations
/// on `StringValue` than on generic `ValueTyped<T>`.
class StringValue(
    private val str: StarlarkStr,
    private val value: Value,
) : StringValueLike, Comparable<StringValue> {

    internal fun starlarkStr(): StarlarkStr = str

    fun asStr(): String = str.asStr()

    fun toValue(): Value = value

    fun getHash(): StarlarkHashValue = StarlarkHashValue.new(str.value)

    /// Convert a value to a FrozenStringValue using a supplied Freezer.
    fun freeze(freezer: Freezer): FreezeResult<FrozenStringValue> {
        val frozen = freezer.freeze(toValue()).getOrElse { return Result.failure(it) }
        return Result.success(FrozenStringValue.newUnchecked(frozen))
    }

    /// Get self along with the hash.
    fun getHashed(): Hashed<StringValue> {
        return Hashed.newUnchecked(getHash(), this)
    }

    /// Get the string reference along with the hash.
    fun getHashedStr(): Hashed<String> {
        return Hashed.newUnchecked(getHash(), asStr())
    }

    /// Get the Value along with the hash.
    fun getHashedValue(): Hashed<Value> {
        return Hashed.newUnchecked(getHash(), toValue())
    }

    /// If this string value is frozen, return it.
    fun unpackFrozen(): FrozenStringValue? {
        val frozen = toValue().unpackFrozen() ?: return null
        return FrozenStringValue.newUnchecked(frozen)
    }

    override fun toStringValue(): StringValue = this

    override fun asStrValue(): String = asStr()

    override fun compareTo(other: StringValue): Int {
        return asStr().compareTo(other.asStr())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is StringValue) return str == other.str
        if (other is FrozenStringValue) return str == other.toStringValue().starlarkStr()
        return false
    }

    override fun hashCode(): Int = str.hashCode()

    override fun toString(): String = str.toString()

    companion object {
        fun default(): StringValue = FrozenStringValue.default().toStringValue()

        /// Construct without checking type.
        fun newUnchecked(value: Value): StringValue {
            val str = value.unpackStarlarkStr()
                ?: error("StringValue.newUnchecked: value is not a StarlarkStr")
            return StringValue(str, value)
        }

        /** Downcast a [Value] to a [StringValue], returning null if the value is not a string. */
        fun new(value: Value): StringValue? {
            val str = value.unpackStarlarkStr() ?: return null
            return StringValue(str, value)
        }
    }
}

/// Common interface for StringValue and FrozenStringValue.
interface StringValueLike {
    /// Convert to a StringValue.
    fun toStringValue(): StringValue

    /// Convert to a str.
    fun asStrValue(): String {
        return toStringValue().asStr()
    }
}
