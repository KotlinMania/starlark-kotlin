// port-lint: source src/typing/callable.rs
package io.github.kotlinmania.starlark.typing

// use std::fmt::Display;
// use std::sync::OnceLock;

// use allocative::Allocative;
// use dupe::Dupe;
// use starlark_syntax::codemap::Span;

// use crate::typing::ParamSpec;
// use crate::typing::Ty;
// use crate::typing::TypingOracleCtx;
// use crate::typing::call_args::TyCallArgs;
// use crate::typing::error::TypingOrInternalError;
// use crate::typing::ty::TypeRenderConfig;
// use crate::util::arc_or_static::ArcOrStatic;

import io.github.kotlinmania.starlark.codemap.Span
import io.github.kotlinmania.starlark.typing.oracle.TypingOracleCtx

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

/// `typing.Callable`.
// #[derive(Debug, Dupe, Clone, Eq, PartialEq, Ord, PartialOrd, Hash, Allocative)]
// pub struct TyCallable {
//     inner: ArcOrStatic<TyCallableInner>,
// }
class TyCallable private constructor(
    private val params: ParamSpec,
    private val result: Ty,
) : Comparable<TyCallable> {
    companion object {
        // impl TyCallable

        /// Create a new callable type.
        // pub fn new(params: ParamSpec, result: Ty) -> TyCallable
        fun new(params: ParamSpec, result: Ty): TyCallable {
            return TyCallable(params, result)
        }

        // pub(crate) fn any() -> TyCallable
        private val ANY: TyCallable by lazy {
            TyCallable(ParamSpec.any(), Ty.any())
        }

        internal fun any(): TyCallable = ANY
    }

    // pub(crate) fn validate_call(&self, span: Span, args: &TyCallArgs, oracle: TypingOracleCtx)
    //     -> Result<Ty, TypingOrInternalError>
    internal fun validateCall(
        span: Span,
        args: TyCallArgs,
        oracle: TypingOracleCtx,
    ): Result<Ty> {
        return oracle.validateFnCall(span, this, args)
    }

    // pub(crate) fn params(&self) -> &ParamSpec
    internal fun params(): ParamSpec = params

    // pub(crate) fn result(&self) -> &Ty
    internal fun result(): Ty = result

    // pub(crate) fn fmt_with_config(&self, f: &mut Formatter, config: &TypeRenderConfig) -> fmt::Result
    internal fun fmtWithConfig(sb: StringBuilder, config: TypeRenderConfig) {
        if (params() == ParamSpec.any() && result() == Ty.any()) {
            sb.append("typing.Callable")
        } else {
            sb.append("typing.Callable[")
            if (params().isAny()) {
                sb.append("...")
            } else {
                val pos = params().allRequiredPosOnly()
                if (pos != null) {
                    sb.append("[")
                    for ((i, p) in pos.withIndex()) {
                        if (i != 0) {
                            sb.append(", ")
                        }
                        sb.append(p.fmtWithConfig(config))
                    }
                    sb.append("]")
                } else {
                    sb.append("\"")
                    sb.append(params().displayWith(config))
                    sb.append("\"")
                }
            }
            sb.append(", ")
            sb.append(result().displayWith(config))
            sb.append("]")
        }
    }

    // impl Display for TyCallable
    // fn fmt(&self, f: &mut Formatter) -> fmt::Result
    override fun toString(): String {
        val sb = StringBuilder()
        fmtWithConfig(sb, TypeRenderConfig.Default)
        return sb.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TyCallable) return false
        return params == other.params && result == other.result
    }

    override fun hashCode(): Int {
        var h = params.hashCode()
        h = h * 31 + result.hashCode()
        return h
    }

    override fun compareTo(other: TyCallable): Int {
        val cmp = params.hashCode().compareTo(other.params.hashCode())
        if (cmp != 0) return cmp
        return result.hashCode().compareTo(other.result.hashCode())
    }
}
