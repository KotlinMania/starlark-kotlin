// port-lint: source src/values/types/namespace/typing.rs
package io.github.kotlinmania.starlark_kotlin.values.types.namespace

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
import io.github.kotlinmania.starlark_kotlin.typing.call_args.TyCallArgs
import io.github.kotlinmania.starlark_kotlin.typing.TyCallable
import io.github.kotlinmania.starlark_kotlin.typing.custom.TyCustomImpl
import io.github.kotlinmania.starlark_kotlin.typing.TypingNoContextError
import io.github.kotlinmania.starlark_kotlin.typing.function.TyCustomFunctionImpl
import io.github.kotlinmania.starlark_kotlin.typing.oracle.TypingOracleCtx
import io.github.kotlinmania.starlark_kotlin.util.ArcStr
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.starlark_type_id.StarlarkTypeId
import io.github.kotlinmania.starlark_kotlin.values.starlark_type_id.starlarkTypeId
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.TypeMatcher
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.TypeMatcherAlloc
import io.github.kotlinmania.starlark_kotlin.typing.TyCallArgs
import io.github.kotlinmania.starlark_kotlin.typing.TyCustomFunctionImpl
import io.github.kotlinmania.starlark_kotlin.typing.TyCustomImpl

// #[derive(Allocative, Eq, PartialEq, Hash, Debug, Clone, Copy, Dupe)]
internal object NamespaceMatcher : TypeMatcher {
    // #[type_matcher]
    override fun matches(value: Value): Boolean =
        value.starlarkTypeId() == StarlarkTypeId.of<FrozenNamespace>()
}

// #[derive(Allocative, Clone, Copy, Dupe, Debug, Eq, PartialEq, Hash, Ord, PartialOrd)]
internal object TyNamespaceFunction : TyCustomFunctionImpl {

    override fun asCallable(): TyCallable =
        TyCallable.new(
            ParamSpec.kwargs(Ty.any()),
            Ty.custom(TyNamespace(
                fields = sortedMapOf(),
                extra = true,
            )),
        )

    override fun validateCall(
        span: Span,
        args: TyCallArgs,
        oracle: TypingOracleCtx,
    ): Result<Ty> {
        if (args.pos.isNotEmpty()) {
            val pos = args.pos.first()
            return Result.failure(oracle.msgError(pos.span, "Positional arguments not allowed"))
        }
        val fields = mutableListOf<Pair<ArcStr, Ty>>()
        for (named in args.named) {
            val (name, ty) = named.node
            fields.add(ArcStr.from(name) to ty)
        }
        val extra = args.kwargs != null
        return Result.success(Ty.custom(TyNamespace(
            fields = sortedMapOf(*fields.toTypedArray()),
            extra = extra,
        )))
    }
}

// #[derive(Debug, Clone, PartialEq, Eq, Hash, PartialOrd, Ord, Allocative)]
data class TyNamespace(
    val fields: Map<ArcStr, Ty>,
    /** `true` if there might be additional fields not captured above,
     *  `false` if this struct has no extra members. */
    val extra: Boolean,
) : TyCustomImpl {

    override fun asName(): String? = "namespace"

    override fun attribute(attr: String): Result<Ty> =
        when (val ty = fields[attr]) {
            null -> if (extra) {
                Result.success(Ty.any())
            } else {
                Result.failure(TypingNoContextError)
            }
            else -> Result.success(ty)
        }

    override fun <T : TypeMatcherAlloc> matcher(factory: T): Any =
        factory.alloc(NamespaceMatcher)

    // impl Display for TyNamespace
    override fun toString(): String {
        val items = buildList {
            for ((k, v) in fields) {
                add("$k = $v")
            }
            if (extra) {
                add("..")
            }
        }
        return "namespace(${items.joinToString(", ")})"
    }
}
