// port-lint: source src/typing/structs.rs
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

import io.github.kotlinmania.starlark_kotlin.typing.TypingNoContextOrInternalError
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.TypeMatcherAlloc
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.TypeMatcher
import io.github.kotlinmania.starlark_kotlin.values.types.structs.StructRef
import io.github.kotlinmania.starlark_kotlin.typing.oracle.intersectsBasic
import io.github.kotlinmania.starlark_kotlin.typing.oracle.TypingOracleCtx

// #[derive(Allocative, Eq, PartialEq, Hash, Debug, Clone, Copy, Dupe)]
// struct StructMatcher;
// #[type_matcher]
// impl TypeMatcher for StructMatcher
private object StructMatcher : TypeMatcher {
    // fn matches(&self, value: Value) -> bool
    override fun matches(value: Value): Boolean {
        return StructRef.isInstance(value)
    }
}

/// Struct type.
// #[derive(Debug, Clone, PartialEq, Eq, Hash, PartialOrd, Ord, Allocative)]
// pub struct TyStruct
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
        fun any(): TyStruct = TyStruct(fields = sortedMapOf(), extra = true)
    }

    override fun asName(): String = "struct"

    // fn bin_op(&self, bin_op, rhs, ctx) -> Result<Ty, TypingNoContextOrInternalError>
    fun binOp(binOp: TypingBinOp, rhs: TyBasic, ctx: TypingOracleCtx): Result<Ty> {
        return when (binOp) {
            TypingBinOp.Less -> {
                if (ctx.intersectsBasic(TyBasic.custom(this), rhs)) {
                    Result.success(Ty.bool())
                } else {
                    Result.failure(TypingNoContextOrInternalError.Typing)
                }
            }
            else -> Result.failure(TypingNoContextOrInternalError.Typing)
        }
    }

    // fn attribute(&self, attr: &str) -> Result<Ty, TypingNoContextError>
    override fun attribute(attr: String): Result<Ty> {
        val ty = fields[attr]
        return when {
            ty != null -> Result.success(ty)
            extra -> Result.success(Ty.any())
            else -> Result.failure(TypingNoContextError())
        }
    }

    override fun union2(other: TyCustomImpl): TyCustomImpl? {
        if (other !is TyStruct) return null
        if (this == other) return this
        if (extra != other.extra) return null
        if (fields.keys != other.fields.keys) return null

        val mergedFields = sortedMapOf<String, Ty>()
        for ((key, thisVal) in fields) {
            val otherVal = other.fields[key] ?: return null
            mergedFields[key] = Ty.union2(thisVal, otherVal)
        }
        return TyStruct(fields = mergedFields, extra = extra)
    }

    // fn matcher<T: TypeMatcherAlloc>(&self, factory: T) -> T::Result
    fun <R, T : TypeMatcherAlloc<R>> matcher(factory: T): R {
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
