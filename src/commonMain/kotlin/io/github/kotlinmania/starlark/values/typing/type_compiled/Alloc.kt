// port-lint: source src/values/typing/type_compiled/alloc.rs
package io.github.kotlinmania.starlark.values.typing.typecompiled

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

import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.typing.TyBasic
import io.github.kotlinmania.starlark.typing.TyCustom
import io.github.kotlinmania.starlark.typing.TyStarlarkValue
import io.github.kotlinmania.starlark.values.layout.Value

/**
 * Runtime type matcher interface.
 *
 * Implementations check whether a [Value] matches a particular Starlark type at runtime.
 */
interface TypeMatcher {
    fun matches(value: Value): Boolean

    fun isWildcard(): Boolean = false
}

class TypeMatcherBoxAlloc : TypeMatcherAlloc<TypeMatcher> {
    override fun alloc(matcher: TypeMatcher): TypeMatcher = matcher

    override fun custom(custom: TyCustom): TypeMatcher =
        object : TypeMatcher {
            override fun matches(value: Value) = false
        }

    override fun fromTypeMatcherFactory(factory: TypeMatcherFactory): TypeMatcher =
        object : TypeMatcher {
            override fun matches(value: Value) = false
        }

    companion object : TypeMatcherAlloc<TypeMatcher> {
        override fun alloc(matcher: TypeMatcher): TypeMatcher = matcher

        override fun custom(custom: TyCustom): TypeMatcher = TypeMatcherBoxAlloc().custom(custom)

        override fun fromTypeMatcherFactory(factory: TypeMatcherFactory): TypeMatcher =
            TypeMatcherBoxAlloc().fromTypeMatcherFactory(factory)
    }
}

/** Allocate runtime type matcher, either in starlark heap or in malloc. */
interface TypeMatcherAlloc<R> {
    fun alloc(matcher: TypeMatcher): R

    fun custom(custom: TyCustom): R

    fun fromTypeMatcherFactory(factory: TypeMatcherFactory): R

    fun unreachableCannotAppearInTypeExpr(): Nothing = throw IllegalStateException("type cannot appear in type expressions")

    /** `typing.Any`. */
    fun any(): R = alloc(IsAny)

    /** `typing.Never`. */
    fun never(): R = alloc(IsNever)

    /** `None`. */
    fun none(): R = alloc(IsNone)

    /** `bool`. */
    fun bool(): R = alloc(IsBool)

    /** `int`. */
    fun int(): R = alloc(IsInt)

    /** `str`. */
    fun str(): R = alloc(IsStr)

    fun ty(ty: Ty): R {
        val union = ty.iterUnion()
        return when (union.size) {
            0 -> never()
            1 -> tyBasic(union[0])
            2 -> anyOfTwoBasic(union[0], union[1])
            else -> {
                val matchers = union.map { TypeMatcherBoxAlloc.tyBasic(it) }
                if (matchers.any { it.isWildcard() }) {
                    any()
                } else {
                    alloc(IsAnyOf(matchers))
                }
            }
        }
    }

    /** `A | B`. */
    fun anyOfTwoMatcher(m0: TypeMatcher, m1: TypeMatcher): R =
        if (m0.isWildcard()) {
            alloc(m1)
        } else if (m1.isWildcard()) {
            alloc(m0)
        } else {
            alloc(IsAnyOfTwo(m0, m1))
        }

    /** `A | B`. */
    fun anyOfTwoBasic(ty0: TyBasic, ty1: TyBasic): R =
        when {
            ty0 is TyBasic.Any -> tyBasic(ty1)
            ty1 is TyBasic.Any -> tyBasic(ty0)
            ty0 == TyBasic.none() -> noneOrBasic(ty1)
            ty1 == TyBasic.none() -> noneOrBasic(ty0)
            else -> {
                val m0 = TypeMatcherBoxAlloc.tyBasic(ty0)
                val m1 = TypeMatcherBoxAlloc.tyBasic(ty1)
                anyOfTwoMatcher(m0, m1)
            }
        }

    fun tyBasic(ty: TyBasic): R =
        when (ty) {
            is TyBasic.Any -> any()
            is TyBasic.StarlarkValue -> ty.value.matcher(this)
            is TyBasic.List -> listOf(ty.item.toTy())
            is TyBasic.Tuple -> ty.tuple.matcher(this)
            is TyBasic.Dict -> dictOf(ty.key.toTy(), ty.value.toTy())
            is TyBasic.Iter -> alloc(IsIterable)
            is TyBasic.Callable -> alloc(IsCallable)
            is TyBasic.TypeObject -> alloc(IsType)
            is TyBasic.Custom -> custom(ty.custom)
            is TyBasic.Set -> setOf(ty.item.toTy())
        }

    fun callable(): R = alloc(IsCallable)

    /** `A | None`. */
    fun noneOrStarlarkValue(ty: TyStarlarkValue): R =
        alloc(IsAnyOfTwo(IsNone, ty.matcher(TypeMatcherBoxAlloc)))

    /** `A | None`. */
    fun noneOrBasic(ty: TyBasic): R =
        when {
            ty is TyBasic.Any -> alloc(IsNone)
            ty == TyBasic.anyList() -> alloc(IsAnyOfTwo(IsNone, IsList))
            ty is TyBasic.StarlarkValue -> noneOrStarlarkValue(ty.value)
            else -> alloc(IsAnyOfTwo(IsNone, TypeMatcherBoxAlloc.tyBasic(ty)))
        }

    /** `list`. */
    fun list(): R = alloc(IsList)

    /** `list[Item]`. */
    fun listOfMatcher(item: TypeMatcher): R =
        if (item.isWildcard()) {
            list()
        } else {
            alloc(IsListOf(item))
        }

    /** `list[Item]`. */
    fun listOfStarlarkValue(item: TyStarlarkValue): R =
        listOfMatcher(item.matcher(TypeMatcherBoxAlloc))

    /** `list[Item]`. */
    fun listOfBasic(item: TyBasic): R =
        when (item) {
            is TyBasic.Any -> list()
            is TyBasic.StarlarkValue -> listOfStarlarkValue(item.value)
            else -> listOfMatcher(TypeMatcherBoxAlloc.tyBasic(item))
        }

    /** `list[Item]`. */
    fun listOf(item: Ty): R =
        when {
            item.isAny() -> list()
            item.iterUnion().size == 1 -> listOfBasic(item.iterUnion()[0])
            else -> {
                val matcher = TypeMatcherBoxAlloc.ty(item)
                listOfMatcher(matcher)
            }
        }

    /** `dict`. */
    fun dict(): R = alloc(IsDict)

    /** `dict[Key, Value]`. */
    fun dictOfMatcher(k: TypeMatcher, v: TypeMatcher): R =
        when {
            k.isWildcard() && v.isWildcard() -> dict()
            k.isWildcard() -> alloc(IsDictOf(IsAny, v))
            v.isWildcard() -> alloc(IsDictOf(k, IsAny))
            else -> alloc(IsDictOf(k, v))
        }

    /** `dict[Key, Value]`. */
    fun dictOfStarlarkValueToSomething(k: TyStarlarkValue, v: Ty): R =
        dictOfMatcher(k.matcher(TypeMatcherBoxAlloc), TypeMatcherBoxAlloc.ty(v))

    /** `dict[Key, Value]`. */
    fun dictOf(k: Ty, v: Ty): R =
        when {
            k.isAny() && v.isAny() -> dict()
            k.isStarlarkValue() != null -> dictOfStarlarkValueToSomething(k.isStarlarkValue()!!, v)
            else -> {
                val km = TypeMatcherBoxAlloc.ty(k)
                val vm = TypeMatcherBoxAlloc.ty(v)
                dictOfMatcher(km, vm)
            }
        }

    /** `set`. */
    fun set(): R = alloc(IsSet)

    /** `set[Item]`. */
    fun setOfMatcher(item: TypeMatcher): R =
        if (item.isWildcard()) {
            set()
        } else {
            alloc(IsSetOf(item))
        }

    /** `set[Item]`. */
    fun setOfStarlarkValue(item: TyStarlarkValue): R =
        setOfMatcher(item.matcher(TypeMatcherBoxAlloc))

    /** `set[Item]`. */
    fun setOfBasic(item: TyBasic): R =
        when (item) {
            is TyBasic.Any -> set()
            is TyBasic.StarlarkValue -> setOfStarlarkValue(item.value)
            else -> setOfMatcher(TypeMatcherBoxAlloc.tyBasic(item))
        }

    /** `set[Item]`. */
    fun setOf(item: Ty): R =
        when {
            item.isAny() -> set()
            item.iterUnion().size == 1 -> setOfBasic(item.iterUnion()[0])
            else -> {
                val matcher = TypeMatcherBoxAlloc.ty(item)
                setOfMatcher(matcher)
            }
        }
}
