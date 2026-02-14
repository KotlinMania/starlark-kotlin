// port-lint: source src/typing/callable.rs
package io.github.kotlinmania.starlark_kotlin.typing.callable

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
class Span
class ParamSpec(private val params: Any? = null, private val isAnyFlag: Boolean = false) {
    companion object {
        fun any(): ParamSpec = ParamSpec(isAnyFlag = true)
    }
    fun isAny(): Boolean = isAnyFlag
    fun allRequiredPosOnly(): List<Ty>? = null
    fun displayWith(config: TypeRenderConfig): String = ""
    override fun equals(other: Any?): Boolean =
        other is ParamSpec && params == other.params && isAnyFlag == other.isAnyFlag
    override fun hashCode(): Int = (params?.hashCode() ?: 0) * 31 + isAnyFlag.hashCode()
}
class Ty(private val repr: String = "") {
    companion object {
        fun any(): Ty = Ty("any")
    }
    fun displayWith(config: TypeRenderConfig): String = repr
    fun fmtWithConfig(sb: StringBuilder, config: TypeRenderConfig) { sb.append(repr) }
    override fun equals(other: Any?): Boolean = other is Ty && repr == other.repr
    override fun hashCode(): Int = repr.hashCode()
}
class TypingOracleCtx {
    fun validateFnCall(span: Span, callable: TyCallable, args: TyCallArgs): Result<Ty> =
        Result.success(Ty.any())
}
class TyCallArgs
sealed class TypingOrInternalError {
    class Typing(val error: Any) : TypingOrInternalError()
    class Internal(val error: Any) : TypingOrInternalError()
}
enum class TypeRenderConfig {
    Default,
}

/// `typing.Callable`.
class TyCallable private constructor(
    private val params: ParamSpec,
    private val result: Ty,
) : Comparable<TyCallable> {
    companion object {
        /// Create a new callable type.
        fun new(params: ParamSpec, result: Ty): TyCallable {
            return TyCallable(params, result)
        }

        private val ANY: TyCallable by lazy {
            TyCallable(ParamSpec.any(), Ty.any())
        }

        internal fun any(): TyCallable = ANY
    }

    internal fun validateCall(
        span: Span,
        args: TyCallArgs,
        oracle: TypingOracleCtx,
    ): Result<Ty> {
        return oracle.validateFnCall(span, this, args)
    }

    internal fun params(): ParamSpec = params

    internal fun result(): Ty = result

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
                        p.fmtWithConfig(sb, config)
                    }
                    sb.append("]")
                } else {
                    sb.append("\"${params().displayWith(config)}\"")
                }
            }
            sb.append(", ${result().displayWith(config)}]")
        }
    }

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
