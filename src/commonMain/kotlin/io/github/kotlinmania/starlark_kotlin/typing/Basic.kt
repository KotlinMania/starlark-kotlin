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

/// Type that is not a union.
sealed class TyBasic : Comparable<TyBasic> {
    /// Type that contain anything
    data object Any : TyBasic()
    /// Type is handled by `StarlarkValue` trait implementation.
    class StarlarkValue(val value: TyStarlarkValue) : TyBasic()
    /// Iter is a type that supports iteration, only used as arguments to primitive functions.
    /// The inner type is applicable for each iteration element.
    class Iter(val item: ArcTy) : TyBasic()
    /// `typing.Callable`.
    class Callable(val callable: TyCallable) : TyBasic()
    /// `type`.
    data object Type : TyBasic()
    /// A list.
    class List(val item: ArcTy) : TyBasic()
    /// A tuple. May be empty, to indicate the empty tuple.
    class Tuple(val tuple: TyTuple) : TyBasic()
    /// A dictionary, with key and value types
    class Dict(val key: ArcTy, val value: ArcTy) : TyBasic()
    /// Custom type.
    class Custom(val custom: TyCustom) : TyBasic()
    /// A set.
    class Set(val item: ArcTy) : TyBasic()

    companion object {
        fun none(): TyBasic = starlarkValue<NoneType>()

        fun <T> starlarkValue(): TyBasic = StarlarkValue(TyStarlarkValue.new<T>())

        fun string(): TyBasic = starlarkValue<StarlarkStr>()

        fun int(): TyBasic = StarlarkValue(TyStarlarkValue.int())

        fun float(): TyBasic = StarlarkValue(TyStarlarkValue.float())

        /// Create a list type.
        fun list(element: Ty): TyBasic = List(ArcTy.new(element))

        /// `list[typing.Any]`.
        fun anyList(): TyBasic = List(ArcTy.any())

        /// `dict[typing.Any, typing.Any]`.
        fun anyDict(): TyBasic = dict(Ty.any(), Ty.any())

        fun anySet(): TyBasic = Set(ArcTy.any())

        /// Create a iterable type.
        fun iter(item: Ty): TyBasic = Iter(ArcTy.new(item))

        /// Create a dictionary type.
        fun dict(key: Ty, value: Ty): TyBasic = Dict(ArcTy.new(key), ArcTy.new(value))

        /// Create a set type.
        fun set(item: Ty): TyBasic = Set(ArcTy.new(item))

        fun custom(custom: TyCustomImpl): TyBasic = Custom(TyCustom.new(custom))
    }

    /// Turn a type back into a name, potentially erasing some structure.
    /// E.g. the type `[bool]` would return `list`.
    /// Types like [`Ty::any`] will return `None`.
    fun asName(): String? {
        return when (this) {
            is StarlarkValue -> value.asName()
            is List -> "list"
            is Tuple -> "tuple"
            is Dict -> "dict"
            is Type -> "type"
            is Custom -> custom.asName()
            is Any, is Iter, is Callable -> null
            is Set -> "set"
        }
    }

    /// If this type is function, return the function type.
    internal fun asFunction(): TyFunction? {
        return when (this) {
            is Custom -> custom.asFunctionDyn()
            else -> null
        }
    }

    /// Type is a tuple, with specified or unspecified member types.
    internal fun isTuple(): Boolean = this is Tuple

    /// Type is a list, with specified or unspecified member types.
    internal fun isList(): Boolean = asName() == "list"

    internal fun fmtWithConfig(sb: StringBuilder, config: TypeRenderConfig) {
        when (this) {
            is Any -> sb.append(TypingAny.TYPE)
            is StarlarkValue -> value.fmtWithConfig(sb, config)
            is Iter -> {
                if (item.isAny()) {
                    sb.append("typing.Iterable")
                } else {
                    sb.append("typing.Iterable[${item.displayWith(config)}]")
                }
            }
            is Callable -> callable.fmtWithConfig(sb, config)
            is List -> {
                if (item.isAny()) {
                    sb.append("list")
                } else {
                    sb.append("list[${item.displayWith(config)}]")
                }
            }
            is Tuple -> tuple.fmtWithConfig(sb, config)
            is Dict -> {
                if (key.isAny() && value.isAny()) {
                    sb.append("dict")
                } else {
                    sb.append("dict[${key.displayWith(config)}, ${value.displayWith(config)}]")
                }
            }
            is Type -> sb.append("type")
            is Custom -> sb.append(custom.toString())
            is Set -> sb.append("set[${item.displayWith(config)}]")
        }
    }

    override fun toString(): String {
        val sb = StringBuilder()
        fmtWithConfig(sb, TypeRenderConfig.Default)
        return sb.toString()
    }

    override fun compareTo(other: TyBasic): Int {
        return toString().compareTo(other.toString())
    }
}
