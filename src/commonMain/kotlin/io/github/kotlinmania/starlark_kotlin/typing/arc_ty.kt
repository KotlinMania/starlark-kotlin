// port-lint: source src/typing/arc_ty.rs
package io.github.kotlinmania.starlark_kotlin.typing.arc_ty

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

import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.typing.ty.TypeRenderConfig

// #[derive(Dupe, Clone, Eq, PartialEq, Ord, PartialOrd, Hash, Debug, Allocative)]
// enum ArcTyInner {
//     Any,
//     Never,
//     Str,
//     Int,
//     Bool,
//     None,
//     Arc(Arc<Ty>),
// }
private sealed class ArcTyInner : Comparable<ArcTyInner> {
    // These are shortcuts to avoid allocations for common cases.
    data object Any : ArcTyInner()
    data object Never : ArcTyInner()
    data object Str : ArcTyInner()
    data object Int : ArcTyInner()
    data object Bool : ArcTyInner()
    data object None : ArcTyInner()
    /// Default implementation.
    // Arc(Arc<Ty>)
    data class Wrapped(val ty: Ty) : ArcTyInner()

    // Ordering: Any < Never < Str < Int < Bool < None < Wrapped
    private fun ordinal(): kotlin.Int = when (this) {
        is Any -> 0
        is Never -> 1
        is Str -> 2
        is Int -> 3
        is Bool -> 4
        is None -> 5
        is Wrapped -> 6
    }

    override fun compareTo(other: ArcTyInner): kotlin.Int {
        val cmp = ordinal().compareTo(other.ordinal())
        if (cmp != 0) return cmp
        return if (this is Wrapped && other is Wrapped) {
            this.ty.compareTo(other.ty)
        } else {
            0
        }
    }

    // impl Display for ArcTyInner
    override fun toString(): String {
        return when (this) {
            is Any -> Ty.any().toString()
            is Never -> Ty.never().toString()
            is Str -> Ty.string().toString()
            is Int -> Ty.int().toString()
            is Bool -> Ty.bool().toString()
            is None -> Ty.none().toString()
            is Wrapped -> ty.toString()
        }
    }
}

/// Wrapper for `Ty` which is smaller than `Ty`.
// #[derive(Dupe, Clone, Eq, PartialEq, Ord, PartialOrd, Hash, derive_more::Display, Debug, Allocative)]
// pub struct ArcTy(ArcTyInner);
class ArcTy private constructor(
    private val inner: ArcTyInner,
) : Comparable<ArcTy> {

    override fun compareTo(other: ArcTy): Int {
        return inner.compareTo(other.inner)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ArcTy) return false
        return inner == other.inner
    }

    override fun hashCode(): Int = inner.hashCode()

    override fun toString(): String = inner.toString()

    // impl ArcTy

    companion object {
        // pub(crate) fn any() -> ArcTy
        internal fun any(): ArcTy {
            return ArcTy(ArcTyInner.Any)
        }

        // pub(crate) fn new(ty: Ty) -> ArcTy
        internal fun new(ty: Ty): ArcTy {
            return if (ty.isAny()) {
                any()
            } else if (ty.isNever()) {
                ArcTy(ArcTyInner.Never)
            } else if (ty == Ty.string()) {
                ArcTy(ArcTyInner.Str)
            } else if (ty == Ty.int()) {
                ArcTy(ArcTyInner.Int)
            } else if (ty == Ty.bool()) {
                ArcTy(ArcTyInner.Bool)
            } else if (ty == Ty.none()) {
                ArcTy(ArcTyInner.None)
            } else {
                ArcTy(ArcTyInner.Wrapped(ty))
            }
        }

        // pub(crate) fn union2(a: ArcTy, b: ArcTy) -> ArcTy
        internal fun union2(a: ArcTy, b: ArcTy): ArcTy {
            return if (a == b) {
                a
            } else {
                new(Ty.union2(a.toTy(), b.toTy()))
            }
        }
    }

    // pub(crate) fn to_ty(&self) -> Ty
    internal fun toTy(): Ty {
        return deref()
    }

    // pub(crate) fn display_with<'a>(&'a self, config: &'a TypeRenderConfig) -> ArcTyDisplay<'a>
    internal fun displayWith(config: TypeRenderConfig): ArcTyDisplay {
        return ArcTyDisplay(this, config)
    }

    // impl Deref for ArcTy
    // fn deref(&self) -> &Ty
    fun deref(): Ty {
        return when (val i = inner) {
            is ArcTyInner.Any -> Ty.any()
            is ArcTyInner.Never -> Ty.never()
            is ArcTyInner.Str -> Ty.string()
            is ArcTyInner.Int -> Ty.int()
            is ArcTyInner.Bool -> Ty.bool()
            is ArcTyInner.None -> Ty.none()
            is ArcTyInner.Wrapped -> i.ty
        }
    }
}

// pub(crate) struct ArcTyDisplay<'a> {
//     ty: &'a ArcTy,
//     config: &'a TypeRenderConfig,
// }
internal class ArcTyDisplay(
    private val ty: ArcTy,
    private val config: TypeRenderConfig,
) {
    // impl Display for ArcTyDisplay
    override fun toString(): String {
        return ty.deref().fmtWithConfig(config)
    }
}
