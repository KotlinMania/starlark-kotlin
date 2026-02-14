// port-lint: source src/typing/arc_ty.rs
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
 * Internal representation for [ArcTy].
 *
 * These are shortcuts to avoid allocations for common cases.
 */
private sealed class ArcTyInner : Comparable<ArcTyInner> {
    data object Any : ArcTyInner()
    data object Never : ArcTyInner()
    data object Str : ArcTyInner()
    data object Int : ArcTyInner()
    data object Bool : ArcTyInner()
    data object None : ArcTyInner()
    /** Default implementation backed by an actual [Ty] instance. */
    data class Arc(val ty: Ty) : ArcTyInner()

    override fun compareTo(other: ArcTyInner): kotlin.Int {
        val thisOrdinal = ordinal()
        val otherOrdinal = other.ordinal()
        if (thisOrdinal != otherOrdinal) return thisOrdinal.compareTo(otherOrdinal)
        return when {
            this is Arc && other is Arc -> this.ty.compareTo(other.ty)
            else -> 0
        }
    }

    private fun ordinal(): kotlin.Int = when (this) {
        is Any -> 0
        is Never -> 1
        is Str -> 2
        is Int -> 3
        is Bool -> 4
        is None -> 5
        is Arc -> 6
    }

    override fun toString(): kotlin.String = when (this) {
        is Any -> Ty.any().toString()
        is Never -> Ty.never().toString()
        is Str -> Ty.string().toString()
        is Int -> Ty.int().toString()
        is Bool -> Ty.bool().toString()
        is None -> Ty.none().toString()
        is Arc -> ty.toString()
    }
}

/**
 * Wrapper for [Ty] which is smaller than [Ty].
 *
 * Avoids heap allocation for common type singletons (any, never, str, int, bool, none).
 * Acts as Rust's `Deref<Target = Ty>` by providing [toTy] to extract the underlying type.
 */
class ArcTy internal constructor(
    private val inner: ArcTyInner
) : Comparable<ArcTy> {

    companion object {
        /** Create an [ArcTy] representing the `any` type. */
        fun any(): ArcTy = ArcTy(ArcTyInner.Any)

        /** Create an [ArcTy] from a [Ty], choosing optimized representation where possible. */
        fun new(ty: Ty): ArcTy = when {
            ty.isAny() -> ArcTy(ArcTyInner.Any)
            ty.isNever() -> ArcTy(ArcTyInner.Never)
            ty == Ty.string() -> ArcTy(ArcTyInner.Str)
            ty == Ty.int() -> ArcTy(ArcTyInner.Int)
            ty == Ty.bool() -> ArcTy(ArcTyInner.Bool)
            ty == Ty.none() -> ArcTy(ArcTyInner.None)
            else -> ArcTy(ArcTyInner.Arc(ty))
        }

        /** Create the union of two [ArcTy] values. */
        fun union2(a: ArcTy, b: ArcTy): ArcTy {
            return if (a == b) {
                a
            } else {
                new(Ty.union2(a.toTy(), b.toTy()))
            }
        }
    }

    /** Convert this [ArcTy] back to a [Ty]. */
    fun toTy(): Ty = when (val i = inner) {
        is ArcTyInner.Any -> Ty.any()
        is ArcTyInner.Never -> Ty.never()
        is ArcTyInner.Str -> Ty.string()
        is ArcTyInner.Int -> Ty.int()
        is ArcTyInner.Bool -> Ty.bool()
        is ArcTyInner.None -> Ty.none()
        is ArcTyInner.Arc -> i.ty
    }

    /** Check if this type is the `any` type. */
    fun isAny(): Boolean = inner is ArcTyInner.Any

    /** Display with a custom configuration. */
    fun displayWith(config: TypeRenderConfig): String {
        return toTy().fmtWithConfig(config)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ArcTy) return false
        return inner == other.inner
    }

    override fun hashCode(): Int = inner.hashCode()

    override fun toString(): String = inner.toString()

    override fun compareTo(other: ArcTy): Int = inner.compareTo(other.inner)
}
