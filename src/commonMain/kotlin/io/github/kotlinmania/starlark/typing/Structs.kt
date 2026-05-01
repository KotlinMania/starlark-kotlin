// port-lint: source structs.rs
package io.github.kotlinmania.starlark.typing

/*
 * Copyright 2019 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not import this file except in compliance with the License.
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

import io.github.kotlinmania.starlark.typing.TypingNoContextOrInternalError
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.typing.typecompiled.TypeMatcherAlloc
import io.github.kotlinmania.starlark.values.typing.typecompiled.TypeMatcher
import io.github.kotlinmania.starlark.values.types.structs.StructRef
import io.github.kotlinmania.starlark.typing.oracle.TypingOracleCtx

private object StructMatcher : TypeMatcher {
    override fun matches(value: Value): Boolean {
        return StructRef.isInstance(value)
    }
}

/** Struct type. */
data class TyStruct(
    /** The fields that are definitely present in the struct, with their types. */
    internal val fields: Map<String, Ty>,
    /**
     * `true` if there might be additional fields not captured above,
     * `false` if this struct has no extra members.
     */
    internal val extra: Boolean,
) : TyCustomImpl, Comparable<TyCustomImpl> {

    companion object {
        /** Any struct. */
        fun any(): TyStruct = TyStruct(fields = emptyMap(), extra = true)
    }

    override fun asName(): String = "struct"

    override fun binOp(binOp: TypingBinOp, rhs: TyBasic, ctx: TypingOracleCtx): Result<Ty> {
        return when (binOp) {
            TypingBinOp.Less -> {
                val ir = ctx.intersectsBasic(TyBasic.custom(this), rhs)
                if (ir.isFailure) return Result.failure(ir.exceptionOrNull()!!)
                if (ir.getOrThrow()) {
                    Result.success(Ty.bool())
                } else {
                    Result.failure(TypingNoContextOrInternalError.Typing)
                }
            }
            else -> Result.failure(TypingNoContextOrInternalError.Typing)
        }
    }

    override fun attribute(attr: String): Result<Ty> {
        val ty = fields[attr]
        return when {
            ty != null -> Result.success(ty)
            extra -> Result.success(Ty.any())
            else -> Result.failure(TypingNoContextError)
        }
    }

    override fun union2(other: TyCustomImpl): TyCustomImpl? {
        if (other !is TyStruct) return null
        if (this == other) return this
        if (extra != other.extra) return null
        if (fields.keys != other.fields.keys) return null

        val mergedFields = mutableMapOf<String, Ty>()
        for ((key, thisVal) in fields) {
            val otherVal = other.fields[key] ?: return null
            mergedFields[key] = Ty.union2(thisVal, otherVal)
        }
        return TyStruct(fields = mergedFields, extra = extra)
    }

    override fun <R> matcher(factory: TypeMatcherAlloc<R>): R {
        return factory.alloc(StructMatcher)
    }

    override fun compareTo(other: TyCustomImpl): Int {
        if (other !is TyStruct) {
            return this::class.simpleName.orEmpty().compareTo(other::class.simpleName.orEmpty())
        }
        val extraComp = extra.compareTo(other.extra)
        if (extraComp != 0) return extraComp
        val sizeComp = fields.size.compareTo(other.fields.size)
        if (sizeComp != 0) return sizeComp
        val thisEntries = fields.entries.sortedBy { it.key }
        val otherEntries = other.fields.entries.sortedBy { it.key }
        for ((a, b) in thisEntries.zip(otherEntries)) {
            val keyComp = a.key.compareTo(b.key)
            if (keyComp != 0) return keyComp
            val valComp = a.value.compareTo(b.value)
            if (valComp != 0) return valComp
        }
        return 0
    }

    override fun toString(): String {
        return buildString {
            append("struct(")
            var first = true
            for ((k, v) in fields.entries.sortedBy { it.key }) {
                if (!first) append(", ")
                first = false
                append("$k = $v")
            }
            if (extra) {
                if (!first) append(", ")
                append("..")
            }
            append(")")
        }
    }
}
