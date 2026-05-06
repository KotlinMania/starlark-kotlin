// port-lint: source typing/custom.rs
package io.github.kotlinmania.starlark.typing

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

import io.github.kotlinmania.cmpany.OrdAny
import io.github.kotlinmania.starlarksyntax.codemap.Span as Span
import io.github.kotlinmania.starlark.typing.oracle.TypingOracleCtx
import io.github.kotlinmania.starlark.values.typing.typecompiled.TypeMatcherAlloc
import io.github.kotlinmania.starlark.values.typing.typecompiled.TypeMatcherBox
import io.github.kotlinmania.starlark.values.typing.typecompiled.TypeMatcherBoxAllocImpl
import io.github.kotlinmania.starlark.values.typing.typecompiled.TypeCompiled
import io.github.kotlinmania.starlark.values.typing.typecompiled.TypeCompiledFactory

enum class TypingBinOp(private val symbol: String) {
    Less("<"),
    BitOr("|"),
    In("in"),
    Add("+"),
    Sub("-"),
    Mul("*"),
    Div("/"),
    FloorDiv("/"),
    Percent("%"),
    BitAnd("&"),
    BitXor("^"),
    LeftShift("<<"),
    RightShift(">>");

    override fun toString(): String = symbol

    /**
     * Result type is always `bool`.
     */
    fun alwaysBool(): Boolean {
        return this == In || this == Less
    }
}

/** Custom type implementation. [`Display`] must implement the representation of the type. */
interface TyCustomImpl : Comparable<TyCustomImpl> {
    fun asName(): String?

    fun validateCall(span: Span, args: TyCallArgs, oracle: TypingOracleCtx): Result<Ty> =
        Result.failure(oracle.msgError(span, "Value of type `$this` is not callable"))

    /** Must override if implementing `validateCall`. */
    fun asCallable(): TyCallable? = null

    fun asFunction(): TyFunction? = null

    fun binOp(binOp: TypingBinOp, rhs: TyBasic, ctx: TypingOracleCtx): Result<Ty> =
        Result.failure(TypingNoContextOrInternalError.Typing)

    fun iterItem(): Result<Ty> = Result.failure(TypingNoContextError)

    fun index(item: TyBasic, ctx: TypingOracleCtx): Result<Ty> =
        Result.failure(TypingNoContextOrInternalError.Typing)

    fun attribute(attr: String): Result<Ty>

    fun union2(other: TyCustomImpl): TyCustomImpl? = if (this == other) this else null

    fun intersects(other: TyCustomImpl): Boolean = true

    /** Additional types that this type intersects with. */
    fun intersectsWith(other: TyBasic): Boolean = false

    /** Create runtime type matcher for values. */
    fun <R> matcher(factory: TypeMatcherAlloc<R>): R
}

/**
 * Dynamic dispatch interface for custom types.
 *
 * separate interface that `TyCustomImpl` implementations satisfy through the
 * [TyCustomDynBridge] adapter.
 *
 * The Dyn trait adds dynamic dispatch methods: eqToken, hashCode, cmpToken,
 * intoAny, asAny, plus _dyn variants of all TyCustomImpl methods, and
 * union2Dyn/intersectsDyn for cross-type operations.
 */
internal interface TyCustomDyn {
    fun eqToken(): Any
    fun hashCodeDyn(): Int
    fun cmpToken(): Pair<OrdAny, String>
    fun intoAny(): Any
    fun asAny(): Any

    fun asNameDyn(): String?
    fun validateCallDyn(span: Span, args: TyCallArgs, oracle: TypingOracleCtx): Result<Ty>
    fun asCallableDyn(): TyCallable?
    fun isIntersectsWithDyn(other: TyBasic): Boolean
    fun asFunctionDyn(): TyFunction?
    fun attributeDyn(attr: String): Result<Ty>
    fun iterItemDyn(): Result<Ty>
    fun indexDyn(index: TyBasic, ctx: TypingOracleCtx): Result<Ty>
    fun binOpDyn(binOp: TypingBinOp, rhs: TyBasic, ctx: TypingOracleCtx): Result<Ty>

    fun union2Dyn(other: TyCustomDyn): Result<TyCustomDyn>
    fun intersectsDyn(other: TyCustomDyn): Boolean

    fun matcherWithTypeCompiledFactoryDyn(factory: TypeCompiledFactory): TypeCompiled
    fun matcherBoxDyn(): TypeMatcherBox
}

/**
 *
 * Bridges a concrete [TyCustomImpl] to the [TyCustomDyn] dynamic dispatch interface.
 */
internal class TyCustomDynBridge<T : TyCustomImpl>(val inner: T) : TyCustomDyn {
    override fun eqToken(): Any = inner

    override fun hashCodeDyn(): Int = inner.hashCode()

    override fun cmpToken(): Pair<OrdAny, String> =
        Pair(OrdAny.new<TyCustomImpl>(inner), inner::class.simpleName ?: "unknown")

    override fun intoAny(): Any = inner

    override fun asAny(): Any = inner

    override fun asNameDyn(): String? = inner.asName()

    override fun validateCallDyn(span: Span, args: TyCallArgs, oracle: TypingOracleCtx): Result<Ty> =
        inner.validateCall(span, args, oracle)

    override fun asCallableDyn(): TyCallable? = inner.asCallable()

    override fun isIntersectsWithDyn(other: TyBasic): Boolean =
        inner.intersectsWith(other)

    override fun asFunctionDyn(): TyFunction? = inner.asFunction()

    override fun attributeDyn(attr: String): Result<Ty> = inner.attribute(attr)

    override fun iterItemDyn(): Result<Ty> = inner.iterItem()

    override fun indexDyn(index: TyBasic, ctx: TypingOracleCtx): Result<Ty> = inner.index(index, ctx)

    override fun binOpDyn(binOp: TypingBinOp, rhs: TyBasic, ctx: TypingOracleCtx): Result<Ty> =
        inner.binOp(binOp, rhs, ctx)

    override fun union2Dyn(other: TyCustomDyn): Result<TyCustomDyn> {
        val otherAny = other.asAny()
        if (inner::class == otherAny::class) {
            val otherTyped = otherAny as T
            val merged = inner.union2(otherTyped)
            if (merged != null) {
                return Result.success(TyCustomDynBridge(merged) as TyCustomDyn)
            }
        }
        return Result.failure(IllegalArgumentException("Cannot merge custom types"))
    }

    override fun intersectsDyn(other: TyCustomDyn): Boolean {
        val otherAny = other.asAny()
        if (inner::class == otherAny::class) {
            val otherTyped = otherAny as T
            return inner.intersects(otherTyped)
        }
        return false
    }

    override fun matcherWithTypeCompiledFactoryDyn(factory: TypeCompiledFactory): TypeCompiled =
        inner.matcher(factory)

    override fun matcherBoxDyn(): TypeMatcherBox = inner.matcher(TypeMatcherBoxAllocImpl())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TyCustomDynBridge<*>) return false
        return inner == other.inner
    }

    override fun hashCode(): Int = inner.hashCode()

    override fun toString(): String = inner.toString()
}

/**
 * A custom type, wrapping a [TyCustomDyn] instance.
 *
 */
class TyCustom internal constructor(internal val inner: TyCustomDyn) : Comparable<TyCustom> {
    companion object {
        fun <T : TyCustomImpl> new(ty: T): TyCustom = TyCustom(TyCustomDynBridge(ty))

        internal fun union2(x: TyCustom, y: TyCustom): Result<TyCustom> {
            return x.inner.union2Dyn(y.inner).map { TyCustom(it) }
        }

        internal fun intersects(x: TyCustom, y: TyCustom): Boolean {
            return x.inner.intersectsDyn(y.inner)
        }
    }

    fun asName(): String? = inner.asNameDyn()

    fun asCallableDyn(): TyCallable? = inner.asCallableDyn()

    fun asFunctionDyn(): TyFunction? = inner.asFunctionDyn()

    internal fun validateCallDyn(span: Span, args: TyCallArgs, oracle: TypingOracleCtx): Result<Ty> =
        inner.validateCallDyn(span, args, oracle)

    internal fun iterItemDyn(): Result<Ty> = inner.iterItemDyn()

    internal fun indexDyn(index: TyBasic, ctx: TypingOracleCtx): Result<Ty> =
        inner.indexDyn(index, ctx)

    internal fun attributeDyn(attr: String): Result<Ty> = inner.attributeDyn(attr)

    internal fun binOpDyn(binOp: TypingBinOp, rhs: TyBasic, ctx: TypingOracleCtx): Result<Ty> =
        inner.binOpDyn(binOp, rhs, ctx)

    internal fun intersectsWith(other: TyBasic, ctx: TypingOracleCtx): Result<Boolean> {
        if (inner.isIntersectsWithDyn(other)) {
            return Result.success(true)
        }
        return when (other) {
            is TyBasic.Custom -> Result.success(intersects(this, other.custom))
            is TyBasic.Callable -> {
                val thisCallable = inner.asCallableDyn()
                if (thisCallable != null) ctx.callablesIntersect(thisCallable, other.callable)
                else Result.success(false)
            }
            else -> Result.success(false)
        }
    }

    fun matcherWithTypeCompiledFactory(factory: TypeCompiledFactory): TypeCompiled =
        inner.matcherWithTypeCompiledFactoryDyn(factory)

    internal fun matcherWithBox(): TypeMatcherBox = inner.matcherBoxDyn()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TyCustom) return false
        return inner.eqToken() == other.inner.eqToken()
    }

    override fun hashCode(): Int = inner.hashCodeDyn()

    override fun toString(): String = inner.toString()

    /** Compare by type name first, then by value. */
    override fun compareTo(other: TyCustom): Int {
        val (aCmp, aTypeName) = this.inner.cmpToken()
        val (bCmp, bTypeName) = other.inner.cmpToken()

        // Type ids are comparable, but we want comparison independent of hashing.
        if (aCmp.typeId() != bCmp.typeId()) {
            val typeNameCmp = aTypeName.compareTo(bTypeName)
            if (typeNameCmp != 0) {
                return typeNameCmp
            }

            // This is unreachable: if the type names are the same,
            // the type ids should be the same.
        }

        return aCmp.compareTo(bCmp)
    }
}
