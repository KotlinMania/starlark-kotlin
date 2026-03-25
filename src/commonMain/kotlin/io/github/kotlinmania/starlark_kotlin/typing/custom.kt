// port-lint: source src/typing/custom.rs
package io.github.kotlinmania.starlark_kotlin.typing.custom

import io.github.kotlinmania.starlark_kotlin.codemap.Span

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

// Placeholder types referenced from other modules
// These will be replaced with real imports as the port progresses
class Ty
class TyBasic {
    sealed class Variant {
        class Custom(val custom: TyCustom) : Variant()
        class Callable(val callable: TyCallable) : Variant()
        class Other : Variant()
    }

    var variant: Variant = Variant.Other()
}

class TyFunction
class TypingBinOp
class TypingOracleCtx {
    fun msgError(span: Span, message: String): TypingOrInternalError {
        return TypingOrInternalError.Typing(TypingError())
    }

    fun callablesIntersect(a: TyCallable, b: TyCallable): Result<Boolean> {
        return Result.success(false)
    }
}

class TyCallArgs
class TyCallable
class TypingError
class TypingNoContextError
class TypeMatcherBox
class TypeCompiled<T>
class TypeCompiledFactory<A, B>
class TypeMatcherBoxAlloc

sealed class TypingOrInternalError {
    class Typing(val error: TypingError) : TypingOrInternalError()
    class Internal(val error: InternalError) : TypingOrInternalError()
}

sealed class TypingNoContextOrInternalError {
    data object Typing : TypingNoContextOrInternalError()
    class Internal(val error: InternalError) : TypingNoContextOrInternalError()
}

class InternalError

/// Custom type implementation. Display must implement the representation of the type.
interface TyCustomImpl : Comparable<TyCustomImpl> {
    fun asName(): String?

    fun validateCall(
        span: Span,
        args: TyCallArgs,
        oracle: TypingOracleCtx,
    ): kotlin.Result<Ty> {
        return kotlin.Result.failure(
            Exception("Value of type `${this}` is not callable")
        )
    }

    /// Must override if implementing `validateCall`.
    fun asCallable(): TyCallable? = null

    fun asFunction(): TyFunction? = null

    fun binOp(
        binOp: TypingBinOp,
        rhs: TyBasic,
        ctx: TypingOracleCtx,
    ): kotlin.Result<Ty> {
        return kotlin.Result.failure(Exception("typing no context"))
    }

    fun iterItem(): kotlin.Result<Ty> {
        return kotlin.Result.failure(Exception("typing no context"))
    }

    fun index(
        item: TyBasic,
        ctx: TypingOracleCtx,
    ): kotlin.Result<Ty> {
        return kotlin.Result.failure(Exception("typing no context"))
    }

    fun attribute(attr: String): kotlin.Result<Ty>

    fun intersectsWith(other: TyBasic): Boolean = false

    fun hashCodeImpl(): Int

    override fun hashCode(): Int
    override fun equals(other: Any?): Boolean
}

/// Dynamic dispatch version of [TyCustomImpl] for type-erased usage.
internal interface TyCustomDyn {
    fun eqToken(): Any
    fun hashCodeDyn(): Long
    fun cmpToken(): Pair<Comparable<Any>, String>
    fun asAny(): Any

    fun asNameDyn(): String?

    fun validateCallDyn(
        span: Span,
        args: TyCallArgs,
        oracle: TypingOracleCtx,
    ): kotlin.Result<Ty>

    fun isIntersectsWithDyn(other: TyBasic): Boolean
    fun asCallableDyn(): TyCallable?
    fun asFunctionDyn(): TyFunction?
    fun iterItemDyn(): kotlin.Result<Ty>

    fun indexDyn(
        index: TyBasic,
        ctx: TypingOracleCtx,
    ): kotlin.Result<Ty>

    fun attributeDyn(attr: String): kotlin.Result<Ty>

    fun binOpDyn(
        binOp: TypingBinOp,
        rhs: TyBasic,
        ctx: TypingOracleCtx,
    ): kotlin.Result<Ty>

    fun union2Dyn(
        other: TyCustomDyn,
    ): kotlin.Result<TyCustomDyn>

    fun intersectsDyn(other: TyCustomDyn): Boolean

    fun matcherBoxDyn(): TypeMatcherBox
}

/// Adapter that bridges [TyCustomImpl] to [TyCustomDyn].
internal class TyCustomDynAdapter<T : TyCustomImpl>(val impl: T) : TyCustomDyn {
    override fun eqToken(): Any = impl

    override fun hashCodeDyn(): Long {
        return impl.hashCodeImpl().toLong()
    }

    override fun cmpToken(): Pair<Comparable<Any>, String> {
        @Suppress("UNCHECKED_CAST")
        return (impl as Comparable<Any>) to impl::class.qualifiedName.orEmpty()
    }

    override fun asAny(): Any = impl

    override fun asNameDyn(): String? = impl.asName()

    override fun validateCallDyn(
        span: Span,
        args: TyCallArgs,
        oracle: TypingOracleCtx,
    ): kotlin.Result<Ty> {
        return impl.validateCall(span, args, oracle)
    }

    override fun asCallableDyn(): TyCallable? = impl.asCallable()

    override fun isIntersectsWithDyn(other: TyBasic): Boolean = impl.intersectsWith(other)

    override fun asFunctionDyn(): TyFunction? = impl.asFunction()

    override fun attributeDyn(attr: String): kotlin.Result<Ty> = impl.attribute(attr)

    override fun iterItemDyn(): kotlin.Result<Ty> = impl.iterItem()

    override fun indexDyn(
        index: TyBasic,
        ctx: TypingOracleCtx,
    ): kotlin.Result<Ty> {
        return impl.index(index, ctx)
    }

    override fun binOpDyn(
        binOp: TypingBinOp,
        rhs: TyBasic,
        ctx: TypingOracleCtx,
    ): kotlin.Result<Ty> {
        return impl.binOp(binOp, rhs, ctx)
    }

    override fun union2Dyn(
        other: TyCustomDyn,
    ): kotlin.Result<TyCustomDyn> {
        if (other is TyCustomDynAdapter<*> && other.impl::class == impl::class) {
            @Suppress("UNCHECKED_CAST")
            val otherImpl = other.impl as T
            return if (impl == otherImpl) {
                kotlin.Result.success(this)
            } else {
                kotlin.Result.failure(Exception("union2 failed"))
            }
        }
        return kotlin.Result.failure(Exception("union2 type mismatch"))
    }

    override fun intersectsDyn(other: TyCustomDyn): Boolean {
        if (other is TyCustomDynAdapter<*> && other.impl::class == impl::class) {
            return true
        }
        return false
    }

    override fun matcherBoxDyn(): TypeMatcherBox = TypeMatcherBox()
}

class TyCustom internal constructor(internal val inner: TyCustomDyn) {
    companion object {
        fun <T : TyCustomImpl> new(ty: T): TyCustom {
            return TyCustom(TyCustomDynAdapter(ty))
        }

        internal fun union2(x: TyCustom, y: TyCustom): kotlin.Result<TyCustom> {
            return x.inner.union2Dyn(y.inner).map { TyCustom(it) }
        }

        internal fun intersects(x: TyCustom, y: TyCustom): Boolean {
            return x.inner.intersectsDyn(y.inner)
        }
    }

    internal fun asName(): String? = inner.asNameDyn()

    internal fun intersectsWith(
        other: TyBasic,
        ctx: TypingOracleCtx,
    ): kotlin.Result<Boolean> {
        if (inner.isIntersectsWithDyn(other)) {
            return kotlin.Result.success(true)
        }
        return when (val variant = other.variant) {
            is TyBasic.Variant.Custom -> kotlin.Result.success(intersects(this, variant.custom))
            is TyBasic.Variant.Callable -> when (val thisCallable = inner.asCallableDyn()) {
                null -> kotlin.Result.success(false)
                else -> ctx.callablesIntersect(thisCallable, variant.callable)
            }
            else -> kotlin.Result.success(false)
        }
    }

    internal fun matcherWithBox(): TypeMatcherBox {
        return inner.matcherBoxDyn()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is TyCustom) return false
        return inner.eqToken() == other.inner.eqToken()
    }

    override fun hashCode(): Int {
        return inner.hashCodeDyn().toInt()
    }

    override fun toString(): String {
        return inner.toString()
    }
}

internal fun TyCustom.compareTo(other: TyCustom): Int {
    val (aCmp, aTypeName) = this.inner.cmpToken()
    val (bCmp, bTypeName) = other.inner.cmpToken()

    // Type ids are comparable, but we want comparison independent of hashing.
    if (aTypeName != bTypeName) {
        val typeNameCmp = aTypeName.compareTo(bTypeName)
        if (typeNameCmp != 0) {
            return typeNameCmp
        }
        // This is unreachable: if the type names are the same,
        // the type ids should be the same.
    }

    return aCmp.compareTo(bCmp)
}
