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

import io.github.kotlinmania.starlark_kotlin.typing.ParamSpec
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.typing.call_args.TyCallArgs
import io.github.kotlinmania.starlark_kotlin.typing.callable.TyCallable
import io.github.kotlinmania.starlark_kotlin.typing.custom.TyCustomImpl
import io.github.kotlinmania.starlark_kotlin.typing.error.TypingNoContextError
import io.github.kotlinmania.starlark_kotlin.typing.error.TypingOrInternalError
import io.github.kotlinmania.starlark_kotlin.typing.function.TyCustomFunctionImpl
import io.github.kotlinmania.starlark_kotlin.typing.oracle.ctx.TypingOracleCtx
import io.github.kotlinmania.starlark_kotlin.values.starlark_type_id.StarlarkTypeId
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.TypeMatcherAlloc
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.TypeMatcher
import io.github.kotlinmania.starlark_kotlin.util.ArcStr
import io.github.kotlinmania.starlark_kotlin.syntax.ast.Result
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.types.enumeration.value.dupe
import io.github.kotlinmania.starlark_kotlin.values.starlark_type_id.starlarkTypeId
import io.github.kotlinmania.starlark_kotlin.typing.ctx.Result
import io.github.kotlinmania.starlark_kotlin.typing.oracle.ctx.success
import io.github.kotlinmania.starlark_kotlin.analysis.span
import io.github.kotlinmania.starlark_kotlin.analysis.node
import io.github.kotlinmania.starlark_kotlin.codemap.Span

internal data class NamespaceMatcher(
    private val dummy: Unit = Unit
) : TypeMatcher {
    override fun matches(value: Value<*>): Boolean {
        return value.starlarkTypeId() == StarlarkTypeId.of<Namespace<Nothing>>()
    }
}

internal data class TyNamespaceFunction(
    private val dummy: Unit = Unit
) : TyCustomFunctionImpl {
    override fun asCallable(): TyCallable {
        // TODO(nga): this should be obtained from function signature from function definition.
        return TyCallable.new(
            ParamSpec.kwargs(Ty.any()),
            Ty.custom(TyNamespace(
                fields = emptyMap(),
                extra = true
            ))
        )
    }

    override fun validateCall(
        @Suppress("UNUSED_PARAMETER") span: Span,
        args: TyCallArgs,
        oracle: TypingOracleCtx
    ): Result<Ty> {
        if (args.pos.isNotEmpty()) {
            val pos = args.pos.first()
            return Result.failure(oracle.msgError(pos.span, "Positional arguments not allowed"))
        }
        val fields = mutableMapOf<ArcStr, Ty>()
        for (named in args.named) {
            val (name, ty) = named.node
            fields[ArcStr.from(name)] = ty
        }
        val extra = args.kwargs != null
        return Result.success(Ty.custom(TyNamespace(
            fields = fields.toSortedMap(),
            extra = extra
        )))
    }
}

data class TyNamespace(
    val fields: Map<ArcStr, Ty>,
    /** `true` if there might be additional fields not captured above,
     * `false` if this struct has no extra members. */
    val extra: Boolean
) : TyCustomImpl, Comparable<TyNamespace> {
    override fun asName(): String? {
        return "namespace"
    }

    override fun attribute(attr: String): Result<Ty> {
        return when (val ty = fields[attr]) {
            null -> {
                if (extra) {
                    Result.success(Ty.any())
                } else {
                    Result.failure(TypingNoContextError())
                }
            }
            else -> Result.success(ty.dupe())
        }
    }

    override fun <T : TypeMatcherAlloc> matcher(factory: T): T.Result {
        return factory.allocMatcher(NamespaceMatcher())
    }

    override fun compareTo(other: TyNamespace): Int {
        val extraCmp = extra.compareTo(other.extra)
        if (extraCmp != 0) return extraCmp

        val sizeCmp = fields.size.compareTo(other.fields.size)
        if (sizeCmp != 0) return sizeCmp

        val thisIter = fields.entries.iterator()
        val otherIter = other.fields.entries.iterator()

        while (thisIter.hasNext() && otherIter.hasNext()) {
            val thisEntry = thisIter.next()
            val otherEntry = otherIter.next()

            val keyCmp = thisEntry.key.compareTo(otherEntry.key)
            if (keyCmp != 0) return keyCmp

            val valueCmp = thisEntry.value.compareTo(otherEntry.value)
            if (valueCmp != 0) return valueCmp
        }

        return 0
    }

    override fun toString(): String {
        val items = mutableListOf<String>()
        for ((k, v) in fields) {
            items.add("$k = $v")
        }
        if (extra) {
            items.add("..")
        }
        return "namespace(${items.joinToString(", ")})"
    }
}
