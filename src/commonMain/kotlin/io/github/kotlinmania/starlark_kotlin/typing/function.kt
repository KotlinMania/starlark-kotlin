// port-lint: source src/typing/function.rs
package io.github.kotlinmania.starlark_kotlin.typing.function

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

import io.github.kotlinmania.starlark_kotlin.codemap.Span
import io.github.kotlinmania.starlark_kotlin.typing.ParamSpec
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.typing.TyBasic
import io.github.kotlinmania.starlark_kotlin.typing.TypingBinOp
import io.github.kotlinmania.starlark_kotlin.typing.TypingOracleCtx
import io.github.kotlinmania.starlark_kotlin.typing.call_args.TyCallArgs
import io.github.kotlinmania.starlark_kotlin.typing.callable.TyCallable
import io.github.kotlinmania.starlark_kotlin.typing.custom.TyCustomImpl
import io.github.kotlinmania.starlark_kotlin.typing.error.TypingNoContextError
import io.github.kotlinmania.starlark_kotlin.typing.error.TypingNoContextOrInternalError
import io.github.kotlinmania.starlark_kotlin.typing.error.TypingOrInternalError
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.alloc.TypeMatcherAlloc

/// Custom function typechecker.
// pub trait TyCustomFunctionImpl: Debug + Eq + Ord + Hash + Allocative + Send + Sync + 'static
interface TyCustomFunctionImpl {
    // fn is_type(&self) -> bool
    fun isType(): Boolean = false

    // fn validate_call(&self, span, args, oracle) -> Result<Ty, TypingOrInternalError>
    fun validateCall(
        span: Span,
        args: TyCallArgs,
        oracle: TypingOracleCtx,
    ): Ty

    // fn as_callable(&self) -> TyCallable
    fun asCallable(): TyCallable

    // fn as_function(&self) -> Option<&TyFunction>
    fun asFunction(): TyFunction? = null
}

// #[derive(Allocative, Eq, PartialEq, Hash, Ord, PartialOrd, Debug, derive_more::Display)]
// pub struct TyCustomFunction<F: TyCustomFunctionImpl>(pub F);
class TyCustomFunction<F : TyCustomFunctionImpl>(
    val inner: F,
) : TyCustomImpl {

    // impl TyCustomImpl for TyCustomFunction

    // fn as_name(&self) -> Option<&str>
    override fun asName(): String? = "function"

    // fn validate_call(&self, span, args, oracle) -> Result<Ty, TypingOrInternalError>
    override fun validateCall(
        span: Span,
        args: TyCallArgs,
        oracle: TypingOracleCtx,
    ): Ty {
        return inner.validateCall(span, args, oracle)
    }

    // fn as_callable(&self) -> Option<TyCallable>
    override fun asCallable(): TyCallable? {
        return inner.asCallable()
    }

    // fn as_function(&self) -> Option<&TyFunction>
    override fun asFunction(): TyFunction? {
        return inner.asFunction()
    }

    // fn bin_op(&self, bin_op, rhs, ctx) -> Result<Ty, TypingNoContextOrInternalError>
    override fun binOp(
        binOp: TypingBinOp,
        rhs: TyBasic,
        ctx: TypingOracleCtx,
    ): Ty {
        return when {
            // `str | list`.
            binOp == TypingBinOp.BitOr && inner.isType() -> Ty.basic(TyBasic.Type)
            else -> throw TypingNoContextOrInternalError.Typing
        }
    }

    // fn index(&self, item, ctx) -> Result<Ty, TypingNoContextOrInternalError>
    override fun index(
        item: TyBasic,
        ctx: TypingOracleCtx,
    ): Ty {
        // TODO(nga): this is hack for `enum` (type) which pretends to be a function.
        //   Should be a custom type.
        return Ty.any()
    }

    // fn attribute(&self, attr: &str) -> Result<Ty, TypingNoContextError>
    override fun attribute(attr: String): Ty {
        throw TypingNoContextError()
    }

    // fn matcher<T: TypeMatcherAlloc>(&self, factory: T) -> T::Result
    override fun <T : TypeMatcherAlloc> matcher(factory: T): Any {
        return factory.callable()
    }

    // impl Display for TyCustomFunction
    override fun toString(): String {
        return "def(${inner.asCallable().params()}) -> ${inner.asCallable().result()}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TyCustomFunction<*>) return false
        return inner == other.inner
    }

    override fun hashCode(): Int = inner.hashCode()
}

/// A function.
// #[derive(Debug, Clone, PartialEq, Eq, Hash, PartialOrd, Ord, Allocative)]
// pub struct TyFunction {
//     pub(crate) type_attr: Option<Ty>,
//     pub(crate) callable: TyCallable,
// }
class TyFunction(
    /// The `.type` property of the function, often `""`.
    internal val typeAttr: Ty?,
    internal val callable: TyCallable,
) : TyCustomFunctionImpl {

    companion object {
        /// Constructor.
        // pub fn new_with_type_attr(params, result, type_attr) -> Self
        fun newWithTypeAttr(params: ParamSpec, result: Ty, typeAttr: Ty): TyFunction {
            return TyFunction(
                typeAttr = typeAttr,
                callable = TyCallable.new(params, result),
            )
        }

        /// Constructor.
        // pub fn new(params, result) -> Self
        fun new(params: ParamSpec, result: Ty): TyFunction {
            return TyFunction(
                typeAttr = null,
                callable = TyCallable.new(params, result),
            )
        }
    }

    /// Callable signature of the function.
    // pub fn callable(&self) -> &TyCallable
    fun callable(): TyCallable = callable

    // impl TyCustomFunctionImpl for TyFunction

    // fn is_type(&self) -> bool
    override fun isType(): Boolean = typeAttr != null

    // fn validate_call(&self, span, args, oracle) -> Result<Ty, TypingOrInternalError>
    override fun validateCall(
        span: Span,
        args: TyCallArgs,
        oracle: TypingOracleCtx,
    ): Ty {
        return oracle.validateFnCall(span, callable, args)
    }

    // fn as_callable(&self) -> TyCallable
    override fun asCallable(): TyCallable = callable

    // fn as_function(&self) -> Option<&TyFunction>
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
