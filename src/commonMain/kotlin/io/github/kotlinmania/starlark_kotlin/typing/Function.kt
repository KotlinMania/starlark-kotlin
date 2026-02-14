// port-lint: source src/typing/function.rs
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
 * Custom function typechecker.
 *
 * Corresponds to Rust's `TyCustomFunctionImpl` trait.
 */
interface TyCustomFunctionImpl : Comparable<TyCustomFunctionImpl> {
    fun isType(): Boolean = false

    fun validateCall(args: TyCallArgs): Result<Ty>

    fun asCallable(): TyCallable

    fun asFunction(): TyFunction? = null
}

/**
 * A custom function type that wraps a [TyCustomFunctionImpl].
 *
 * Display format: `def({params}) -> {result}`.
 *
 * Corresponds to Rust's `TyCustomFunction<F>`.
 */
data class TyCustomFunction(
    val inner: TyCustomFunctionImpl
) : TyCustomImpl, Comparable<TyCustomFunction> {

    override fun asName(): String = "function"

    override fun validateCall(args: TyCallArgs): Result<Ty> =
        inner.validateCall(args)

    override fun asCallable(): TyCallable? = inner.asCallable()

    override fun asFunction(): TyFunction? = inner.asFunction()

    override fun binOp(binOp: TypingBinOp, rhs: TyBasic): Result<Ty> {
        return when {
            // `str | list`.
            binOp == TypingBinOp.BitOr && inner.isType() ->
                Result.success(Ty.basic(TyBasic.Type))
            else -> Result.failure(TypingNoContextError())
        }
    }

    override fun index(item: TyBasic): Result<Ty> {
        // Hack for `enum` (type) which pretends to be a function.
        return Result.success(Ty.any())
    }

    override fun attribute(attr: String): Result<Ty> =
        Result.failure(TypingNoContextError())

    /** Create runtime type matcher. In Rust, returns `factory.callable()`. */
    override fun <T> matcher(factory: TypeMatcherFactory<T>): T {
        return factory.callable()
    }

    override fun compareTo(other: TyCustomImpl): Int {
        if (other !is TyCustomFunction) {
            return this::class.simpleName.orEmpty().compareTo(other::class.simpleName.orEmpty())
        }
        return inner.compareTo(other.inner)
    }

    fun compareTo(other: TyCustomFunction): Int =
        inner.compareTo(other.inner)

    override fun toString(): String =
        "def(${inner.asCallable().params()}) -> ${inner.asCallable().result()}"
}

/**
 * A function.
 *
 * Corresponds to Rust's `TyFunction` struct.
 */
data class TyFunction(
    /** The `.type` property of the function, often `""`. */
    internal val typeAttr: Ty?,
    internal val callable: TyCallable
) : TyCustomFunctionImpl, Comparable<TyCustomFunctionImpl> {

    companion object {
        /** Constructor. */
        fun newWithTypeAttr(params: ParamSpec, result: Ty, typeAttr: Ty): TyFunction =
            TyFunction(typeAttr = typeAttr, callable = TyCallable.new(params, result))

        /** Constructor. */
        fun new(params: ParamSpec, result: Ty): TyFunction =
            TyFunction(typeAttr = null, callable = TyCallable.new(params, result))
    }

    /** Callable signature of the function. */
    fun callable(): TyCallable = callable

    override fun isType(): Boolean = typeAttr != null

    override fun validateCall(args: TyCallArgs): Result<Ty> {
        // In Rust: oracle.validate_fn_call(span, &self.callable, args)
        // Simplified: full implementation delegates to TypingOracleCtx.
        return callable.validateCall(args)
    }

    override fun asCallable(): TyCallable = callable

    override fun asFunction(): TyFunction = this

    override fun compareTo(other: TyCustomFunctionImpl): Int {
        if (other !is TyFunction) {
            return this::class.simpleName.orEmpty().compareTo(other::class.simpleName.orEmpty())
        }
        val typeAttrComp = compareTypeAttrs(typeAttr, other.typeAttr)
        if (typeAttrComp != 0) return typeAttrComp
        return callable.compareTo(other.callable)
    }

    override fun toString(): String =
        "def(${callable.params()}) -> ${callable.result()}"
}

private fun compareTypeAttrs(a: Ty?, b: Ty?): Int {
    if (a == null && b == null) return 0
    if (a == null) return -1
    if (b == null) return 1
    return a.compareTo(b)
}
