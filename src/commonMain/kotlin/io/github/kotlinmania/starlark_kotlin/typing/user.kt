// port-lint: source src/typing/user.rs
package io.github.kotlinmania.starlark_kotlin.typing.user

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
import io.github.kotlinmania.starlark_kotlin.typing.TyStarlarkValue
import io.github.kotlinmania.starlark_kotlin.typing.TypingNoContextError
import io.github.kotlinmania.starlark_kotlin.typing.TypingOrInternalError
import io.github.kotlinmania.starlark_kotlin.typing.TypingNoContextOrInternalError
import io.github.kotlinmania.starlark_kotlin.typing.basic.TyBasic
import io.github.kotlinmania.starlark_kotlin.typing.basic.TyCustom
import io.github.kotlinmania.starlark_kotlin.typing.basic.Ty
import io.github.kotlinmania.starlark_kotlin.typing.callable.TyCallable
import io.github.kotlinmania.starlark_kotlin.typing.callable.TyCallArgs
import io.github.kotlinmania.starlark_kotlin.typing.custom.TyCustomImpl
import io.github.kotlinmania.starlark_kotlin.typing.oracle.ctx.TypingOracleCtx
import io.github.kotlinmania.starlark_kotlin.values.types.TypeInstanceId
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.alloc.TypeMatcherAlloc
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.type_matcher_factory.TypeMatcherFactory

private sealed class TyUserError(override val message: String) : Exception(message) {
    class CallableNotCallable(name: String) : TyUserError(
        "Type `$name` specifies custom callable, but underlying `StarlarkValue` is not callable"
    )

    class IndexableNotIndexable(name: String) : TyUserError(
        "Type `$name` specifies custom indexable, but underlying `StarlarkValue` is not indexable"
    )

    class IterableNotIterable(name: String) : TyUserError(
        "Type `$name` specifies custom iterable, but underlying `StarlarkValue` is not iterable"
    )
}

/// Types of `[]` operator.
class TyUserIndex(
    /// Type of index argument.
    internal val index: Ty,
    /// Type of result.
    internal val result: Ty,
)

/// Fields of the struct.
data class TyUserFields(
    /// Known fields.
    val known: Map<String, Ty>,
    /// Are there unknown fields?
    /// Unknown fields are possible if this type represents an abstract type like a provider.
    val unknown: Boolean,
) : Comparable<TyUserFields> {

    companion object {
        /// No fields.
        fun noFields(): TyUserFields = TyUserFields(
            known = sortedMapOf(),
            unknown = false,
        )

        /// All fields are not known.
        fun unknown(): TyUserFields = TyUserFields(
            known = sortedMapOf(),
            unknown = true,
        )

        fun default(): TyUserFields = noFields()
    }

    override fun compareTo(other: TyUserFields): Int {
        val knownCmp = known.entries.toList().toString().compareTo(other.known.entries.toList().toString())
        if (knownCmp != 0) return knownCmp
        return unknown.compareTo(other.unknown)
    }
}

/// Optional parameters to [TyUser.new].
class TyUserParams(
    /// Super types for this type (`base` is included in this list implicitly).
    val supertypes: List<TyBasic> = emptyList(),
    /// Runtime type matcher for this type (use `TyStarlarkValue` matcher if not specified).
    val matcher: TypeMatcherFactory? = null,
    /// Custom fields for this type (use `TyStarlarkValue` fields if not specified).
    val fields: TyUserFields = TyUserFields.default(),
    /// Set if more precise callable signature is known than `base` provides.
    val callable: TyCallable? = null,
    /// Set if more precise index signature is known than `base` provides.
    val index: TyUserIndex? = null,
    /// Set if more precise iter item is known than `base` provides.
    val iterItem: Ty? = null,
) {
    companion object {
        fun default(): TyUserParams = TyUserParams()
    }
}

/// Type description for arbitrary type.
class TyUser private constructor(
    private val name: String,
    /// Base type for this custom type, e.g. generic record for record with known fields.
    private val base: TyStarlarkValue,
    /// Super types for this type (`base` is included in this list implicitly).
    private val supertypes: List<TyBasic>,
    private val matcher: TypeMatcherFactory?,
    private val id: TypeInstanceId,
    private val fields: TyUserFields,
    /// Set if more precise callable signature is known than `base` provides.
    private val callable: TyCallable?,
    /// Set if more precise index signature is known than `base` provides.
    private val index: TyUserIndex?,
    /// Set if more precise iter item is known than `base` provides.
    private val iterItem: Ty?,
) : TyCustomImpl, Comparable<TyUser> {

    companion object {
        /// Constructor.
        fun new(
            name: String,
            base: TyStarlarkValue,
            id: TypeInstanceId,
            params: TyUserParams,
        ): Result<TyUser> {
            val (supertypes, matcher, fields, callable, index, iterItem) = params

            if (callable != null && !base.isCallable()) {
                return Result.failure(TyUserError.CallableNotCallable(name))
            }
            if (index != null && !base.isIndexable()) {
                return Result.failure(TyUserError.IndexableNotIndexable(name))
            }
            if (iterItem != null && base.iterItem().isFailure) {
                return Result.failure(TyUserError.IterableNotIterable(name))
            }

            return Result.success(TyUser(
                name = name,
                base = base,
                supertypes = supertypes,
                matcher = matcher,
                id = id,
                fields = fields,
                callable = callable,
                index = index,
                iterItem = iterItem,
            ))
        }

        fun intersects(x: TyUser, y: TyUser): Boolean = x == y
    }

    override fun toString(): String = name

    // PartialEq: compare by id only.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TyUser) return false
        return id == other.id
    }

    // Hash: by name and fields.
    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + fields.hashCode()
        return result
    }

    override fun hashCodeImpl(): Int = hashCode()

    // Ord: by (name, fields, id).
    override fun compareTo(other: TyUser): Int {
        val nameCmp = name.compareTo(other.name)
        if (nameCmp != 0) return nameCmp
        val fieldsCmp = fields.compareTo(other.fields)
        if (fieldsCmp != 0) return fieldsCmp
        return id.compareTo(other.id)
    }

    // TyCustomImpl implementation.

    override fun asName(): String? = name

    override fun attribute(attr: String): kotlin.Result<Ty> {
        val methodResult = base.attrFromMethods(attr)
        if (methodResult.isSuccess) return methodResult

        val fieldTy = fields.known[attr]
        if (fieldTy != null) return kotlin.Result.success(fieldTy)

        return if (fields.unknown) {
            kotlin.Result.success(Ty.any())
        } else {
            kotlin.Result.failure(TypingNoContextError())
        }
    }

    fun index(
        item: TyBasic,
        ctx: TypingOracleCtx,
    ): kotlin.Result<Ty> {
        val idx = index
        if (idx != null) {
            val intersects = ctx.intersects(Ty.basic(item), idx.index)
            if (intersects.isFailure) {
                return kotlin.Result.failure(intersects.exceptionOrNull()!!)
            }
            if (intersects.getOrThrow() != true) {
                return kotlin.Result.failure(TypingNoContextError())
            }
            return kotlin.Result.success(idx.result)
        }
        return base.index(item)
    }

    fun iterItem(): kotlin.Result<Ty> {
        val iter = iterItem
        if (iter != null) {
            return kotlin.Result.success(iter)
        }
        return base.iterItem()
    }

    override fun asCallable(): TyCallable? {
        return if (base.isCallable()) {
            TyCallable.any()
        } else {
            null
        }
    }

    override fun validateCall(
        span: Span,
        args: TyCallArgs,
        oracle: TypingOracleCtx,
    ): kotlin.Result<Ty> {
        val c = callable
        if (c != null) {
            return c.validateCall(span, args, oracle)
        }
        return base.validateCall()
    }

    fun <T> matcher(factory: TypeMatcherAlloc<T>): T {
        val m = matcher
        if (m != null) {
            return factory.fromTypeMatcherFactory(m)
        }
        return base.matcher(factory)
    }

    override fun intersectsWith(other: TyBasic): Boolean {
        if (other is TyBasic.StarlarkValue) {
            if (base == other.value) {
                return true
            }
        }
        return supertypes.any { it == other }
    }

}

/// Destructuring support for [TyUserParams].
private operator fun TyUserParams.component1(): List<TyBasic> = supertypes
private operator fun TyUserParams.component2(): TypeMatcherFactory? = matcher
private operator fun TyUserParams.component3(): TyUserFields = fields
private operator fun TyUserParams.component4(): TyCallable? = callable
private operator fun TyUserParams.component5(): TyUserIndex? = index
private operator fun TyUserParams.component6(): Ty? = iterItem
