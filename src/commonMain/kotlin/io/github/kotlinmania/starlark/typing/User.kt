// port-lint: source src/typing/user.rs
package io.github.kotlinmania.starlark.typing

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
import io.github.kotlinmania.starlark.typing.oracle.TypingOracleCtx
import io.github.kotlinmania.starlark.values.types.TypeInstanceId
import io.github.kotlinmania.starlark.values.typing.type_compiled.TypeMatcherAlloc
import io.github.kotlinmania.starlark.values.typing.type_compiled.TypeMatcherFactory as TypeMatcherFactoryBoxed

// #[derive(Debug, thiserror::Error)]
// enum TyUserError
private sealed class TyUserError(override val message: String) : Exception(message) {
    // #[error("Type `{0}` specifies custom callable, but underlying `StarlarkValue` is not callable")]
    class CallableNotCallable(name: String) : TyUserError(
        "Type `$name` specifies custom callable, but underlying `StarlarkValue` is not callable"
    )

    // #[error("Type `{0}` specifies custom indexable, but underlying `StarlarkValue` is not indexable")]
    class IndexableNotIndexable(name: String) : TyUserError(
        "Type `$name` specifies custom indexable, but underlying `StarlarkValue` is not indexable"
    )

    // #[error("Type `{0}` specifies custom iterable, but underlying `StarlarkValue` is not iterable")]
    class IterableNotIterable(name: String) : TyUserError(
        "Type `$name` specifies custom iterable, but underlying `StarlarkValue` is not iterable"
    )
}

/**
 * Types of `[]` operator.
 */
// #[derive(Allocative, Debug)]
// pub struct TyUserIndex
class TyUserIndex(
    /** Type of index argument. */
    internal val index: Ty,
    /** Type of result. */
    internal val result: Ty,
)

/**
 * Fields of the struct.
 */
// #[derive(Allocative, Debug, Ord, PartialOrd, Eq, PartialEq, Hash)]
// pub struct TyUserFields
data class TyUserFields(
    /** Known fields. */
    val known: Map<String, Ty>,
    /**
     * Are there unknown fields?
     * Unknown fields are possible if this type represents an abstract type like a provider.
     */
    val unknown: Boolean,
) : Comparable<TyUserFields> {

    companion object {
        /** No fields. */
        // Safe default: assuming the type is not abstract,
        // so fields are provided by `TyStarlarkValue`.
        fun noFields(): TyUserFields = TyUserFields(
            known = emptyMap(),
            unknown = false,
        )

        /** All fields are not known. */
        fun unknown(): TyUserFields = TyUserFields(
            known = emptyMap(),
            unknown = true,
        )

        // impl Default for TyUserFields
        fun default(): TyUserFields = noFields()
    }

    // impl Ord for TyUserFields
    override fun compareTo(other: TyUserFields): Int {
        // Compare known fields lexicographically by entries.
        val thisEntries = known.entries.sortedBy { it.key }
        val otherEntries = other.known.entries.sortedBy { it.key }
        val minSize = minOf(thisEntries.size, otherEntries.size)
        for (i in 0 until minSize) {
            val keyCmp = thisEntries[i].key.compareTo(otherEntries[i].key)
            if (keyCmp != 0) return keyCmp
            val valCmp = thisEntries[i].value.compareTo(otherEntries[i].value)
            if (valCmp != 0) return valCmp
        }
        val sizeCmp = thisEntries.size.compareTo(otherEntries.size)
        if (sizeCmp != 0) return sizeCmp
        return unknown.compareTo(other.unknown)
    }
}

/**
 * Optional parameters to [TyUser.new].
 */
// #[derive(Default)]
// pub struct TyUserParams
class TyUserParams(
    /** Super types for this type (`base` is included in this list implicitly). */
    val supertypes: List<TyBasic> = emptyList(),
    /** Runtime type matcher for this type (use `TyStarlarkValue` matcher if not specified). */
    val matcher: TypeMatcherFactoryBoxed? = null,
    /** Custom fields for this type (use `TyStarlarkValue` fields if not specified). */
    val fields: TyUserFields = TyUserFields.default(),
    /** Set if more precise callable signature is known than `base` provides. */
    val callable: TyCallable? = null,
    /** Set if more precise index signature is known than `base` provides. */
    val index: TyUserIndex? = null,
    /** Set if more precise iter item is known than `base` provides. */
    val iterItem: Ty? = null,
) {
    companion object {
        fun default(): TyUserParams = TyUserParams()
    }
}

/**
 * Type description for arbitrary type.
 */
// #[derive(Allocative, Debug, derive_more::Display)]
// #[display("{}", name)]
// pub struct TyUser
class TyUser private constructor(
    private val name: String,
    /** Base type for this custom type, e.g. generic record for record with known fields. */
    private val base: TyStarlarkValue,
    /** Super types for this type (`base` is included in this list implicitly). */
    private val supertypes: List<TyBasic>,
    private val matcher: TypeMatcherFactoryBoxed?,
    private val id: TypeInstanceId,
    private val fields: TyUserFields,
    /** Set if more precise callable signature is known than `base` provides. */
    private val callable: TyCallable?,
    /** Set if more precise index signature is known than `base` provides. */
    private val index: TyUserIndex?,
    /** Set if more precise iter item is known than `base` provides. */
    private val iterItem: Ty?,
) : TyCustomImpl, Comparable<TyCustomImpl> {

    companion object {
        /** Constructor. */
        // pub fn new(name, base, id, params) -> crate::Result<TyUser>
        fun new(
            name: String,
            base: TyStarlarkValue,
            id: TypeInstanceId,
            params: TyUserParams = TyUserParams.default(),
        ): Result<TyUser> {
            val supertypes = params.supertypes
            val matcher = params.matcher
            val fields = params.fields
            val callable = params.callable
            val index = params.index
            val iterItem = params.iterItem

            if (callable != null && !base.isCallable()) {
                return Result.failure(TyUserError.CallableNotCallable(name))
            }
            if (index != null && !base.isIndexable()) {
                return Result.failure(TyUserError.IndexableNotIndexable(name))
            }
            if (iterItem != null && base.iterItem().isFailure) {
                return Result.failure(TyUserError.IterableNotIterable(name))
            }

            return Result.success(
                TyUser(
                    name = name,
                    base = base,
                    supertypes = supertypes,
                    matcher = matcher,
                    id = id,
                    fields = fields,
                    callable = callable,
                    index = index,
                    iterItem = iterItem,
                )
            )
        }

        // fn intersects(x: &Self, y: &Self) -> bool
        fun intersects(x: TyUser, y: TyUser): Boolean = x == y
    }

    // impl Display for TyUser
    // #[display("{}", name)]
    override fun toString(): String = name

    // impl PartialEq for TyUser: compare by id only.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TyUser) return false
        return id == other.id
    }

    // impl Hash for TyUser: by name and fields.
    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + fields.hashCode()
        return result
    }

    // impl Ord for TyUser: by (name, fields, id).
    override fun compareTo(other: TyCustomImpl): Int {
        if (other !is TyUser) {
            return this::class.simpleName.orEmpty().compareTo(other::class.simpleName.orEmpty())
        }
        val nameCmp = name.compareTo(other.name)
        if (nameCmp != 0) return nameCmp
        val fieldsCmp = fields.compareTo(other.fields)
        if (fieldsCmp != 0) return fieldsCmp
        return id.compareTo(other.id)
    }

    // --- TyCustomImpl implementation ---

    // fn as_name(&self) -> Option<&str>
    override fun asName(): String = name

    // fn attribute(&self, attr: &str) -> Result<Ty, TypingNoContextError>
    override fun attribute(attr: String): Result<Ty> {
        // First try base methods.
        val methodResult = base.attrFromMethods(attr)
        if (methodResult.isSuccess) return methodResult

        // Then try known fields.
        val fieldTy = fields.known[attr]
        if (fieldTy != null) return Result.success(fieldTy)

        // If unknown fields allowed, return any.
        return if (fields.unknown) {
            Result.success(Ty.any())
        } else {
            Result.failure(TypingNoContextError)
        }
    }

    // fn index(&self, item: &TyBasic, ctx: &TypingOracleCtx) -> Result<Ty, TypingNoContextOrInternalError>
    override fun index(item: TyBasic, ctx: TypingOracleCtx): Result<Ty> {
        val idx = index
        if (idx != null) {
            val doesIntersect = ctx.intersects(Ty.basic(item), idx.index).getOrElse { return Result.failure(it) }
            if (!doesIntersect) {
                return Result.failure(TypingNoContextOrInternalError.Typing)
            }
            return Result.success(idx.result)
        }
        return base.index(item)
    }

    // fn iter_item(&self) -> Result<Ty, TypingNoContextError>
    override fun iterItem(): Result<Ty> {
        val iter = iterItem
        if (iter != null) {
            return Result.success(iter)
        }
        return base.iterItem()
    }

    // fn as_callable(&self) -> Option<TyCallable>
    override fun asCallable(): TyCallable? {
        return if (base.isCallable()) {
            TyCallable.any()
        } else {
            null
        }
    }

    // fn validate_call(&self, span, args, oracle) -> Result<Ty, TypingOrInternalError>
    override fun validateCall(span: Span, args: TyCallArgs, oracle: TypingOracleCtx): Result<Ty> {
        val c = callable
        if (c != null) {
            return c.validateCall(span, args, oracle)
        }
        return base.validateCall(span, oracle)
    }

    // fn matcher<T: TypeMatcherAlloc>(&self, factory: T) -> T::Result
    override fun <R> matcher(factory: TypeMatcherAlloc<R>): R {
        val m = matcher
        if (m != null) {
            return factory.fromTypeMatcherFactory(m)
        }
        return base.matcher(factory)
    }

    // fn intersects_with(&self, other: &TyBasic) -> bool
    override fun intersectsWith(other: TyBasic): Boolean {
        if (other is TyBasic.StarlarkValue) {
            if (base == other.value) {
                return true
            }
        }
        return supertypes.any { it == other }
    }
}

/** Destructuring support for [TyUserParams]. */
private operator fun TyUserParams.component1(): List<TyBasic> = supertypes
private operator fun TyUserParams.component2(): TypeMatcherFactoryBoxed? = matcher
private operator fun TyUserParams.component3(): TyUserFields = fields
private operator fun TyUserParams.component4(): TyCallable? = callable
private operator fun TyUserParams.component5(): TyUserIndex? = index
private operator fun TyUserParams.component6(): Ty? = iterItem
