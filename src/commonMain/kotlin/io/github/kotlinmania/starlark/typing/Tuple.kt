// port-lint: source src/typing/tuple.rs
package io.github.kotlinmania.starlark.typing

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

import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.typing.typecompiled.IsTupleElems
import io.github.kotlinmania.starlark.values.typing.typecompiled.IsTupleElems0
import io.github.kotlinmania.starlark.values.typing.typecompiled.IsTupleElems1
import io.github.kotlinmania.starlark.values.typing.typecompiled.IsTupleElems2
import io.github.kotlinmania.starlark.values.typing.typecompiled.IsTupleOf
import io.github.kotlinmania.starlark.values.typing.typecompiled.StarlarkTypeIdMatcher
import io.github.kotlinmania.starlark.values.typing.typecompiled.TypeMatcherAlloc
import io.github.kotlinmania.starlark.values.typing.typecompiled.TypeMatcherBox
import io.github.kotlinmania.starlark.values.typing.typecompiled.TypeMatcherBoxAlloc
import io.github.kotlinmania.starlark.values.typing.typecompiled.TypeMatcherT

/**
 * A tuple type in the Starlark type system.
 *
 * Can represent either a fixed-element tuple (`tuple[T0, T1, T2]`)
 * or a variable-element tuple (`tuple[T, ...]`).
 */
sealed class TyTuple : Comparable<TyTuple> {
    /** `tuple[T0, T1, T2]` -- a tuple with specific element types. */
    data class Elems(
        val elems: List<Ty>,
    ) : TyTuple()

    /** `tuple[T, ...]` -- a tuple where all elements have the same type. */
    // Of(ArcTy)
    data class Of(
        val item: ArcTy,
    ) : TyTuple()


    /** Get the type at index [i], or `null` for `Of` (any index valid). */
    fun get(i: Int): Ty? =
        when (this) {
            is Elems -> elems.getOrNull(i)
            is Of -> item.toTy()
        }


    /** Union of all element types (identity for [Of]). */
    fun itemTy(): Ty =
        when (this) {
            is Elems -> Ty.unions(elems)
            is Of -> item.toTy()
        }


    /**
     * Check if this tuple type could intersect with [other].
     *
     * [intersectsCheck] tests pairwise element intersection.
     */
    fun intersects(
        other: TyTuple,
        intersectsCheck: (Ty, Ty) -> Boolean,
    ): Boolean =
        when {
            this is Elems && other is Elems -> {
                this.elems.size == other.elems.size &&
                    this.elems.zip(other.elems).all { (x, y) -> intersectsCheck(x, y) }
            }
            this is Of && other is Of -> {
                intersectsCheck(this.item.toTy(), other.item.toTy())
            }
            // e.g. tuple[str, int] does not intersect with tuple[str, ...]
            this is Elems && other is Of -> {
                this.elems.all { x -> intersectsCheck(x, other.item.toTy()) }
            }
            this is Of && other is Elems -> {
                other.elems.all { x -> intersectsCheck(x, this.item.toTy()) }
            }
            else -> false
        }


    /** Allocate a runtime type matcher for this tuple type. */
    fun <R> matcher(factory: TypeMatcherAlloc<R>): R =
        when (this) {
            is Elems ->
                when (elems.size) {
                    // [] => type_compiled_factory.alloc(IsTupleElems0)
                    0 -> factory.alloc(IsTupleElems0)
                    // [x0] => type_compiled_factory.alloc(IsTupleElems1(...))
                    1 -> factory.alloc(IsTupleElems1(TypeMatcherBoxAlloc.ty(elems[0])))
                    // [x0, x1] => optimised 2-element path
                    2 -> {
                        val x0 = elems[0]
                        val x1 = elems[1]
                        val sv0 = x0.isStarlarkValue()
                        val sv1 = x1.isStarlarkValue()
                        if (sv0 != null && sv1 != null) {
                            factory.alloc(
                                IsTupleElems2(
                                    StarlarkTypeIdMatcher.new(sv0),
                                    StarlarkTypeIdMatcher.new(sv1),
                                ),
                            )
                        } else {
                            factory.alloc(
                                IsTupleElems2(
                                    TypeMatcherBoxAlloc.ty(x0),
                                    TypeMatcherBoxAlloc.ty(x1),
                                ),
                            )
                        }
                    }
                    // xs => general N-element path
                    else -> {
                        val matchers =
                            elems.map { e ->
                                val m = TypeMatcherBoxAlloc.ty(e)
                                TypeMatcherBox.new(
                                    object : TypeMatcherT {
                                        override fun matches(value: Value): Boolean = m.matches(value)

                                        override fun isWildcard(): Boolean = m.isWildcard()
                                    },
                                )
                            }
                        factory.alloc(IsTupleElems(matchers))
                    }
                }
            is Of -> {
                if (item.isAny()) {
                    // tuple[any, ...] is the same as just "tuple"
                    TyStarlarkValue.tuple().matcher(factory)
                } else {
                    val sv = item.toTy().isStarlarkValue()
                    if (sv != null) {
                        factory.alloc(IsTupleOf(StarlarkTypeIdMatcher.new(sv)))
                    } else {
                        val m = TypeMatcherBoxAlloc.ty(item.toTy())
                        factory.alloc(IsTupleOf(m))
                    }
                }
            }
        }


    /** Format with a custom rendering configuration. */
    fun fmtWithConfig(config: TypeRenderConfig): String =
        when (this) {
            is Elems ->
                when {
                    elems.size == 1 -> "(${elems[0].fmtWithConfig(config)},)"
                    else ->
                        elems.joinToString(
                            separator = ", ",
                            prefix = "(",
                            postfix = ")",
                        ) { it.fmtWithConfig(config) }
                }
            is Of ->
                when {
                    item.isAny() -> "tuple"
                    else -> "tuple[${item.displayWith(config)}, ...]"
                }
        }

    override fun toString(): String =
        when (this) {
            is Elems ->
                when {
                    elems.size == 1 -> "(${elems[0]},)"
                    else -> elems.joinToString(separator = ", ", prefix = "(", postfix = ")")
                }
            is Of ->
                when {
                    item.isAny() -> "tuple"
                    else -> "tuple[$item, ...]"
                }
        }

    // Derived: Ord, PartialOrd
    override fun compareTo(other: TyTuple): Int {
        val thisOrdinal = if (this is Elems) 0 else 1
        val otherOrdinal = if (other is Elems) 0 else 1
        if (thisOrdinal != otherOrdinal) return thisOrdinal.compareTo(otherOrdinal)
        return when {
            this is Elems && other is Elems -> {
                val sizeComp = this.elems.size.compareTo(other.elems.size)
                if (sizeComp != 0) return sizeComp
                for ((a, b) in this.elems.zip(other.elems)) {
                    val cmp = a.compareTo(b)
                    if (cmp != 0) return cmp
                }
                0
            }
            this is Of && other is Of -> this.item.compareTo(other.item)
            else -> 0
        }
    }
}
