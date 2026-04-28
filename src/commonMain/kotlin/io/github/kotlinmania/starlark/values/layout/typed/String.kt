// port-lint: source src/values/layout/typed/string.rs
package io.github.kotlinmania.starlark.values.layout.typed

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

import starlarkmap.Hashed
import starlarkmap.StarlarkHashValue
import starlarkmap.StarlarkHasher
import io.github.kotlinmania.starlark.CoerceKey
import io.github.kotlinmania.starlark.Sealed
import io.github.kotlinmania.starlark.environment.Methods
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.Freeze
import io.github.kotlinmania.starlark.values.Trace
import io.github.kotlinmania.starlark.values.layout.Freezer
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.types.string.StarlarkStr

// Hashed is defined in starlarkmap.Hashed
// Freezer is defined in io.github.kotlinmania.starlark.values.layout.Freezer

/**
 * Convenient type alias.
 *
 * We use `FrozenStringValue` often, but also we define more operations
 * on `FrozenStringValue` than on generic `FrozenValueTyped<T>`.
 */
class FrozenStringValue(
    private val str: StarlarkStr,
    private val frozenValue: FrozenValue,
) : StringValueLike, Comparable<FrozenStringValue> {

    fun asStr(): String = str.asStr()

    fun toFrozenValue(): FrozenValue = frozenValue

    fun toValue(): Value = frozenValue.toValue()

    fun getHash(): StarlarkHashValue = str.getHashValue()

    /** Get self along with the hash. */
    fun getHashed(): Hashed<FrozenStringValue> {
        return Hashed.newUnchecked(getHash(), this)
    }

    /** Get the FrozenValue along with the hash. */
    fun getHashedValue(): Hashed<FrozenValue> {
        return Hashed.newUnchecked(getHash(), toFrozenValue())
    }

    /** Get the string reference along with the hash. */
    fun getHashedStr(): Hashed<String> {
        return Hashed.newUnchecked(getHash(), asStr())
    }

    override fun toStringValue(): StringValue {
        return StringValue(str, frozenValue.toValue())
    }

    override fun asStrValue(): String = asStr()

    override fun freeze(freezer: Freezer): Result<FrozenStringValue> = Result.success(this)

    override fun trace(tracer: Tracer) {
        // Frozen values are immovable; nothing to trace.
    }

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
            return io.github.kotlinmania.starlark.values.layout.VALUE_EMPTY_STRING.erase()
        }

        /** Construct without checking type. */
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

/**
 * Convenient type alias.
 *
 * We use `StringValue` often, but also we define more operations
 * on `StringValue` than on generic `ValueTyped<T>`.
 */
class StringValue(
    private val str: StarlarkStr,
    private val value: Value,
) : StringValueLike, Comparable<StringValue> {

    internal fun starlarkStr(): StarlarkStr = str

    fun asStr(): String = str.asStr()

    fun toValue(): Value = value

    fun getHash(): StarlarkHashValue = str.getHashValue()

    /** Convert a value to a FrozenStringValue using a supplied Freezer. */
    override fun freeze(freezer: Freezer): Result<FrozenStringValue> {
        val frozen = freezer.freeze(toValue()).getOrElse { return Result.failure(it) }
        return Result.success(FrozenStringValue.newUnchecked(frozen))
    }

    override fun trace(tracer: Tracer) {
        value.trace(tracer)
    }

    /** Get self along with the hash. */
    fun getHashed(): Hashed<StringValue> {
        return Hashed.newUnchecked(getHash(), this)
    }

    /** Get the string reference along with the hash. */
    fun getHashedStr(): Hashed<String> {
        return Hashed.newUnchecked(getHash(), asStr())
    }

    /** Get the Value along with the hash. */
    fun getHashedValue(): Hashed<Value> {
        return Hashed.newUnchecked(getHash(), toValue())
    }

    /** If this string value is frozen, return it. */
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

        /** Construct without checking type. */
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

/**
 * Common type for [StringValue] and [FrozenStringValue].
 *
 * Sealed via [Sealed]: the upstream `pub trait StringValueLike` is closed
 * through the `Sealed` supertype pattern, with the only impls (`StringValue`
 * and `FrozenStringValue`) sitting in this same file. Marking the Kotlin
 * interface `sealed` gives the compiler the same closed-variant guarantee.
 *
 * Auto-traits dropped from the upstream supertype list because they have no
 * Kotlin analog or are universal in Kotlin: `Borrow<str>`, `Display`, `Debug`,
 * `Default`, `Eq`, `Ord`, `Copy`, `Clone`, `Dupe`, `Serialize`, `Allocative`,
 * and the `'v` lifetime bound.
 */
sealed interface StringValueLike :
    Sealed,
    Trace,
    Freeze<FrozenStringValue>,
    CoerceKey<StringValue> {
    /** Convert to a [StringValue]. */
    fun toStringValue(): StringValue

    /** Convert to a [String]. */
    fun asStrValue(): String {
        return toStringValue().asStr()
    }
}
