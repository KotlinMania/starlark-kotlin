// port-lint: source src/values/types/namespace/typing.rs
package io.github.kotlinmania.starlark.values.types.namespace

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
import io.github.kotlinmania.starlark.typing.ParamSpec
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.typing.TyCallArgs
import io.github.kotlinmania.starlark.typing.TyCallable
import io.github.kotlinmania.starlark.typing.TyCustomFunctionImpl
import io.github.kotlinmania.starlark.typing.TyCustomImpl
import io.github.kotlinmania.starlark.typing.TypingNoContextError
import io.github.kotlinmania.starlark.typing.oracle.TypingOracleCtx
import io.github.kotlinmania.starlark.util.ArcStr
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.starlarktypeid.StarlarkTypeId
import io.github.kotlinmania.starlark.values.typing.typecompiled.TypeMatcher
import io.github.kotlinmania.starlark.values.typing.typecompiled.TypeMatcherAlloc

internal object NamespaceMatcher : TypeMatcher {
    // #[type_matcher]
    override fun matches(value: Value): Boolean =
        value.starlarkTypeId() == StarlarkTypeId.of(FrozenNamespace::class)
}

internal object TyNamespaceFunction : TyCustomFunctionImpl {
    override fun asCallable(): TyCallable =
        TyCallable.new(
            ParamSpec.kwargs(Ty.any()),
            Ty.custom(
                TyNamespace(
                    fields = emptyMap(),
                    extra = true,
                ),
            ),
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
        return Result.success(
            Ty.custom(
                TyNamespace(
                    fields = fields.sortedBy { it.first.toString() }.toMap(),
                    extra = extra,
                ),
            ),
        )
    }
}

data class TyNamespace(
    val fields: Map<ArcStr, Ty>,
    /** `true` if there might be additional fields not captured above,
     *  `false` if this struct has no extra members. */
    val extra: Boolean,
) : TyCustomImpl,
    Comparable<TyCustomImpl> {
    override fun asName(): String = "namespace"

    override fun attribute(attr: String): Result<Ty> =
        when (val ty = fields[ArcStr.from(attr)]) {
            null ->
                if (extra) {
                    Result.success(Ty.any())
                } else {
                    Result.failure(TypingNoContextError)
                }
            else -> Result.success(ty)
        }

    override fun <R> matcher(factory: TypeMatcherAlloc<R>): R =
        factory.alloc(NamespaceMatcher)

    override fun compareTo(other: TyCustomImpl): Int {
        if (other !is TyNamespace) {
            return this::class.simpleName.orEmpty().compareTo(other::class.simpleName.orEmpty())
        }
        val extraComp = extra.compareTo(other.extra)
        if (extraComp != 0) return extraComp
        val sizeComp = fields.size.compareTo(other.fields.size)
        if (sizeComp != 0) return sizeComp
        val thisEntries = fields.entries.sortedBy { it.key.toString() }
        val otherEntries = other.fields.entries.sortedBy { it.key.toString() }
        for ((a, b) in thisEntries.zip(otherEntries)) {
            val keyComp = a.key.toString().compareTo(b.key.toString())
            if (keyComp != 0) return keyComp
            val valComp = a.value.compareTo(b.value)
            if (valComp != 0) return valComp
        }
        return 0
    }

    // impl Display for TyNamespace
    override fun toString(): String {
        val items =
            buildList {
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
