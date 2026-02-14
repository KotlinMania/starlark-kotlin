// port-lint: source src/typing/tuple.rs
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
 * A tuple type in the Starlark type system.
 *
 * Can represent either a fixed-element tuple (`tuple[T0, T1, T2]`)
 * or a variable-element tuple (`tuple[T, ...]`).
 */
sealed class TyTuple : Comparable<TyTuple> {
    /**
     * `tuple[T0, T1, T2]` — a tuple with specific element types.
     */
    data class Elems(val elems: List<Ty>) : TyTuple()

    /**
     * `tuple[T, ...]` — a tuple where all elements have the same type.
     */
    data class Of(val item: ArcTy) : TyTuple()

    /** Get the type at index [i], if it exists. */
    fun get(i: Int): Ty? = when (this) {
        is Elems -> elems.getOrNull(i)
        is Of -> item.toTy()
    }

    /** Get the item type of the tuple (union of all element types). */
    fun itemTy(): Ty = when (this) {
        is Elems -> Ty.unions(elems)
        is Of -> item.toTy()
    }

    /** Check if two tuple types could intersect. */
    fun intersects(
        other: TyTuple,
        intersectsCheck: (Ty, Ty) -> Boolean
    ): Boolean = when {
        this is Elems && other is Elems -> {
            this.elems.size == other.elems.size &&
                this.elems.zip(other.elems).all { (x, y) -> intersectsCheck(x, y) }
        }
        this is Of && other is Of -> {
            intersectsCheck(this.item.toTy(), other.item.toTy())
        }
        this is Elems && other is Of -> {
            this.elems.all { x -> intersectsCheck(x, other.item.toTy()) }
        }
        this is Of && other is Elems -> {
            other.elems.all { x -> intersectsCheck(x, this.item.toTy()) }
        }
        else -> false
    }

    /** Format with a custom rendering configuration. */
    fun fmtWithConfig(config: TypeRenderConfig): String = when (this) {
        is Elems -> when {
            elems.size == 1 -> "(${elems[0].fmtWithConfig(config)},)"
            else -> elems.joinToString(
                separator = ", ",
                prefix = "(",
                postfix = ")"
            ) { it.fmtWithConfig(config) }
        }
        is Of -> when {
            item.isAny() -> "tuple"
            else -> "tuple[${item.displayWith(config)}, ...]"
        }
    }

    override fun toString(): String = when (this) {
        is Elems -> when {
            elems.size == 1 -> "(${elems[0]},)"
            else -> elems.joinToString(separator = ", ", prefix = "(", postfix = ")")
        }
        is Of -> when {
            item.isAny() -> "tuple"
            else -> "tuple[$item, ...]"
        }
    }

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
