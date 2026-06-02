// port-lint: source src/typing/function.rs
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

import io.github.kotlinmania.starlark.codemap.Span
import io.github.kotlinmania.starlark.typing.oracle.TypingOracleCtx
import io.github.kotlinmania.starlark.values.typing.typecompiled.TypeMatcherAlloc

/** Custom function typechecker. */
interface TyCustomFunctionImpl {
    fun isType(): Boolean = false

    fun validateCall(
        span: Span,
        args: TyCallArgs,
        oracle: TypingOracleCtx,
    ): Result<Ty>

    fun asCallable(): TyCallable

    fun asFunction(): TyFunction? = null
}

class TyCustomFunction<F : TyCustomFunctionImpl>(
    val inner: F,
) : TyCustomImpl {

    override fun asName(): String? = "function"

    override fun validateCall(span: Span, args: TyCallArgs, oracle: TypingOracleCtx): Result<Ty> = inner.validateCall(span, args, oracle)

    override fun asCallable(): TyCallable? = inner.asCallable()

    override fun asFunction(): TyFunction? = inner.asFunction()

    override fun binOp(binOp: TypingBinOp, rhs: TyBasic, ctx: TypingOracleCtx): Result<Ty> =
        when {
            // `str | list`.
            binOp == TypingBinOp.BitOr && inner.isType() -> Result.success(Ty.basic(TyBasic.Type))
            else -> Result.failure(TypingNoContextOrInternalError.Typing)
        }

    override fun index(item: TyBasic, ctx: TypingOracleCtx): Result<Ty> {
        // TODO(nga): this is hack for `enum` (type) which pretends to be a function.
        //   Should be a custom type.
        return Result.success(Ty.any())
    }

    override fun attribute(attr: String): Result<Ty> = Result.failure(TypingNoContextError)

    override fun <R> matcher(factory: TypeMatcherAlloc<R>): R = factory.callable()

    override fun toString(): String = "def(${inner.asCallable().params()}) -> ${inner.asCallable().result()}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TyCustomFunction<*>) return false
        return inner == other.inner
    }

    override fun hashCode(): Int = inner.hashCode()

    // Derived from Rust's #[derive(Ord, PartialOrd)] on TyCustomFunction<F>.
    // Comparison delegates to the string representation as a stable ordering.
    override fun compareTo(other: TyCustomImpl): Int {
        if (other !is TyCustomFunction<*>) return this::class.simpleName.orEmpty().compareTo(other::class.simpleName.orEmpty())
        return toString().compareTo(other.toString())
    }
}

/** A function. */
class TyFunction(
    /** The `.type` property of the function, often `""`. */
    internal val typeAttr: Ty?,
    internal val callable: TyCallable,
) : TyCustomFunctionImpl {
    companion object {
        /** Constructor. */
        fun newWithTypeAttr(params: ParamSpec, result: Ty, typeAttr: Ty): TyFunction =
            TyFunction(
                typeAttr = typeAttr,
                callable = TyCallable.new(params, result),
            )

        /** Constructor. */
        fun new(params: ParamSpec, result: Ty): TyFunction =
            TyFunction(
                typeAttr = null,
                callable = TyCallable.new(params, result),
            )
    }

    /** Callable signature of the function. */
    fun callable(): TyCallable = callable


    override fun isType(): Boolean = typeAttr != null

    override fun validateCall(span: Span, args: TyCallArgs, oracle: TypingOracleCtx): Result<Ty> = oracle.validateFnCall(span, callable, args)

    override fun asCallable(): TyCallable = callable

    override fun asFunction(): TyFunction = this

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TyFunction) return false
        return typeAttr == other.typeAttr && callable == other.callable
    }

    override fun hashCode(): Int {
        var result = typeAttr?.hashCode() ?: 0
        result = 31 * result + callable.hashCode()
        return result
    }

    override fun toString(): String = "TyFunction(type_attr=$typeAttr, callable=$callable)"
}
