// port-lint: source src/typing/basic.rs
package io.github.kotlinmania.starlark_kotlin.typing

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
 * Type that is not a union.
 *
 * Represents a single, non-union type in the Starlark type system.
 * Union types are represented as collections of [TyBasic] values in [Ty].
 */
sealed class TyBasic : Comparable<TyBasic> {

    /** Type that contains anything. */
    data object Any : TyBasic()

    /** Type is handled by `StarlarkValue` trait implementation. */
    data class StarlarkValue(val value: TyStarlarkValue) : TyBasic()

    /**
     * Iter is a type that supports iteration, only used as arguments to primitive functions.
     * The inner type is applicable for each iteration element.
     */
    data class Iter(val item: ArcTy) : TyBasic()

    /** `typing.Callable`. */
    data class Callable(val callable: TyCallable) : TyBasic()

    /** `type`. */
    data object Type : TyBasic()

    /** A list. */
    data class List(val element: ArcTy) : TyBasic()

    /** A tuple. May be empty, to indicate the empty tuple. */
    data class Tuple(val tuple: TyTuple) : TyBasic()

    /** A dictionary, with key and value types. */
    data class Dict(val key: ArcTy, val value: ArcTy) : TyBasic()

    /** Custom type. */
    data class Custom(val custom: TyCustom) : TyBasic()

    /** A set. */
    data class Set(val item: ArcTy) : TyBasic()

    companion object {
        /** Create a `None` type. */
        fun none(): TyBasic = StarlarkValue(TyStarlarkValue.none())

        /** Create a `StarlarkValue` type from a type descriptor. */
        fun starlarkValue(value: TyStarlarkValue): TyBasic = StarlarkValue(value)

        /** Create a `string` type. */
        fun string(): TyBasic = StarlarkValue(TyStarlarkValue.string())

        /** Create an `int` type. */
        fun int(): TyBasic = StarlarkValue(TyStarlarkValue.int())

        /** Create a `float` type. */
        fun float(): TyBasic = StarlarkValue(TyStarlarkValue.float())

        /** Create a list type. */
        fun list(element: Ty): TyBasic = List(ArcTy.new(element))

        /** `list[typing.Any]`. */
        fun anyList(): TyBasic = List(ArcTy.any())

        /** `dict[typing.Any, typing.Any]`. */
        fun anyDict(): TyBasic = dict(Ty.any(), Ty.any())

        /** `set[typing.Any]`. */
        fun anySet(): TyBasic = Set(ArcTy.any())

        /** Create an iterable type. */
        fun iter(item: Ty): TyBasic = Iter(ArcTy.new(item))

        /** Create a dictionary type. */
        fun dict(key: Ty, value: Ty): TyBasic = Dict(ArcTy.new(key), ArcTy.new(value))

        /** Create a set type. */
        fun set(item: Ty): TyBasic = Set(ArcTy.new(item))

        /** Create a custom type. */
        fun custom(custom: TyCustom): TyBasic = Custom(custom)
    }

    /**
     * Turn a type back into a name, potentially erasing some structure.
     * E.g. the type `[bool]` would return `list`.
     * Types like [Ty.any] will return `null`.
     */
    fun asName(): String? = when (this) {
        is StarlarkValue -> value.asName()
        is List -> "list"
        is Tuple -> "tuple"
        is Dict -> "dict"
        is Type -> "type"
        is Custom -> custom.asName()
        is Any, is Iter, is Callable -> null
        is Set -> "set"
    }

    /** If this type is a function, return the function type. */
    fun asFunction(): TyFunction? = when (this) {
        is Custom -> custom.asFunctionDyn()
        else -> null
    }

    /** Type is a tuple, with specified or unspecified member types. */
    fun isTuple(): Boolean = this is Tuple

    /** Type is a list, with specified or unspecified member types. */
    fun isList(): Boolean = asName() == "list"

    /** Format with a custom rendering configuration. */
    fun fmtWithConfig(config: TypeRenderConfig): String = when (this) {
        is Any -> "typing.Any"
        is StarlarkValue -> value.fmtWithConfig(config)
        is Iter -> if ((item).isAny()) {
            "typing.Iterable"
        } else {
            "typing.Iterable[${item.displayWith(config)}]"
        }
        is Callable -> callable.fmtWithConfig(config)
        is List -> if (element.isAny()) {
            "list"
        } else {
            "list[${element.displayWith(config)}]"
        }
        is Tuple -> tuple.fmtWithConfig(config)
        is Dict -> if (key.isAny() && value.isAny()) {
            "dict"
        } else {
            "dict[${key.displayWith(config)}, ${value.displayWith(config)}]"
        }
        is Type -> "type"
        is Custom -> custom.toString()
        is Set -> "set[${item.displayWith(config)}]"
    }

    override fun toString(): String = fmtWithConfig(TypeRenderConfig.Default)

    override fun compareTo(other: TyBasic): Int {
        val thisOrdinal = ordinal()
        val otherOrdinal = other.ordinal()
        if (thisOrdinal != otherOrdinal) return thisOrdinal.compareTo(otherOrdinal)
        return when {
            this is StarlarkValue && other is StarlarkValue -> this.value.compareTo(other.value)
            this is Iter && other is Iter -> this.item.compareTo(other.item)
            this is Callable && other is Callable -> this.callable.compareTo(other.callable)
            this is List && other is List -> this.element.compareTo(other.element)
            this is Tuple && other is Tuple -> this.tuple.compareTo(other.tuple)
            this is Dict && other is Dict -> {
                val keyComp = this.key.compareTo(other.key)
                if (keyComp != 0) keyComp else this.value.compareTo(other.value)
            }
            this is Custom && other is Custom -> this.custom.compareTo(other.custom)
            this is Set && other is Set -> this.item.compareTo(other.item)
            else -> 0
        }
    }

    private fun ordinal(): Int = when (this) {
        is Any -> 0
        is StarlarkValue -> 1
        is Iter -> 2
        is Callable -> 3
        is Type -> 4
        is List -> 5
        is Tuple -> 6
        is Dict -> 7
        is Custom -> 8
        is Set -> 9
    }
}
