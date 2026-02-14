// port-lint: source src/typing/callable.rs
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

private data class TyCallableInner(
    val params: ParamSpec,
    val result: Ty,
) : Comparable<TyCallableInner> {
    override fun compareTo(other: TyCallableInner): Int {
        val paramsComp = params.compareTo(other.params)
        if (paramsComp != 0) return paramsComp
        return result.compareTo(other.result)
    }
}

/// `typing.Callable`.
class TyCallable private constructor(
    private val inner: TyCallableInner
) : Comparable<TyCallable> {

    companion object {
        /** Create a new callable type. */
        fun new(params: ParamSpec, result: Ty): TyCallable =
            TyCallable(TyCallableInner(params, result))

        private val ANY_INSTANCE: TyCallable by lazy {
            TyCallable(TyCallableInner(ParamSpec.any(), Ty.any()))
        }

        fun any(): TyCallable = ANY_INSTANCE
    }

    fun validateCall(args: TyCallArgs): Result<Ty> {
        // In Rust: oracle.validate_fn_call(span, self, args)
        return Result.success(inner.result)
    }

    fun params(): ParamSpec = inner.params

    fun result(): Ty = inner.result

    fun fmtWithConfig(config: TypeRenderConfig): String {
        return if (params() == ParamSpec.any() && result() == Ty.any()) {
            "typing.Callable"
        } else {
            buildString {
                append("typing.Callable[")
                if (params().isAny()) {
                    append("...")
                } else {
                    val posOnly = params().allRequiredPosOnly()
                    if (posOnly != null) {
                        append("[")
                        posOnly.forEachIndexed { i, p ->
                            if (i != 0) append(", ")
                            append(p.fmtWithConfig(config))
                        }
                        append("]")
                    } else {
                        append("\"${params().displayWith(config)}\"")
                    }
                }
                append(", ${result().fmtWithConfig(config)}]")
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TyCallable) return false
        return inner == other.inner
    }

    override fun hashCode(): Int = inner.hashCode()

    override fun toString(): String = fmtWithConfig(TypeRenderConfig.Default)

    override fun compareTo(other: TyCallable): Int = inner.compareTo(other.inner)
}
