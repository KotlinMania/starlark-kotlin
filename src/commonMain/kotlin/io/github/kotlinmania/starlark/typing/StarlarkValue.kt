// port-lint: source src/typing/starlark_value.rs
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
import io.github.kotlinmania.starlark.environment.Methods
import io.github.kotlinmania.starlark.typing.oracle.TypingOracleCtx
import io.github.kotlinmania.starlark.typing.oracle.TypingUnOp
import io.github.kotlinmania.starlark.values.typing.typecompiled.TypeMatcherAlloc
import io.github.kotlinmania.starlark.values.typing.typecompiled.StarlarkTypeIdMatcher

private sealed class TyStarlarkValueError : Exception() {
    data class NotCallable(val ty: TyStarlarkValue) : TyStarlarkValueError() {
        override val message: String get() = "Type `$ty` is not callable"
    }
}

// This is a bit suboptimal for binary size:
// we have two vtable instances for each type: this one, and the one within `AValue` vtable.

/**
 * VTable holding type-level information for a [TyStarlarkValue].
 *
 * In Rust this stores a static reference to type name, the `StarlarkValueVTable`,
 * and `StarlarkTypeId` fields for canonical type checking.
 * In Kotlin we store the type name, capability flags, and function references
 * for type-level dispatch.
 */
private class TyStarlarkValueVTable(
    val typeName: String,
    val starlarkTypeId: String = typeName,
    /**
     * `starlark_type_id` is the canonical type id.
     * `starlark_type_id_check` is the canonical-of-canonical check.
     */
    val starlarkTypeIdCheck: String = typeName,
    // Capability flags mirroring Rust's StarlarkValueVTable HAS_* constants.
    val hasPlus: Boolean = false,
    val hasMinus: Boolean = false,
    val hasBitNot: Boolean = false,
    val hasAt: Boolean = false,
    val hasSlice: Boolean = false,
    val hasInvoke: Boolean = false,
    val hasIterate: Boolean = false,
    val hasIterateCollect: Boolean = false,
    val hasEvalType: Boolean = false,
    // Function references for type-level dispatch (mirrors Rust vtable function pointers).
    val binOpTy: (TypingBinOp, TyBasic) -> Ty? = { _, _ -> null },
    val rbinOpTy: (TyBasic, TypingBinOp) -> Ty? = { _, _ -> null },
    val getMethods: () -> Methods? = { null },
    val attrTy: (String) -> Ty? = { null },
)

/**
 * Pre-built vtables for known Starlark types.
 *
 * In Rust this is `TyStarlarkValueVTableGet<'v, T: StarlarkValue<'v>>` which uses
 * const generics to extract vtable data at compile time. In Kotlin we pre-build
 * vtables for all known types and provide a lookup mechanism.
 */
private object TyStarlarkValueVTableGet {
    val INT_VTABLE = TyStarlarkValueVTable(
        typeName = "int",
        hasPlus = true, hasMinus = true, hasBitNot = true,
    )
    val FLOAT_VTABLE = TyStarlarkValueVTable(
        typeName = "float",
        hasPlus = true, hasMinus = true,
    )
    val BOOL_VTABLE = TyStarlarkValueVTable(
        typeName = "bool",
    )
    val STRING_VTABLE = TyStarlarkValueVTable(
        typeName = "string",
        hasAt = true, hasSlice = true,
        hasIterate = true,
    )
    val NONE_VTABLE = TyStarlarkValueVTable(
        typeName = "NoneType",
    )
    val LIST_VTABLE = TyStarlarkValueVTable(
        typeName = "list",
        hasAt = true, hasSlice = true,
        hasIterate = true, hasIterateCollect = true,
    )
    val DICT_VTABLE = TyStarlarkValueVTable(
        typeName = "dict",
        hasAt = true,
        hasIterate = true, hasIterateCollect = true,
    )
    val TUPLE_VTABLE = TyStarlarkValueVTable(
        typeName = "tuple",
        hasAt = true, hasSlice = true,
        hasIterate = true, hasIterateCollect = true,
    )
    val SET_VTABLE = TyStarlarkValueVTable(
        typeName = "set",
        hasIterate = true, hasIterateCollect = true,
    )

    private val vtablesByName = mapOf(
        "int" to INT_VTABLE,
        "float" to FLOAT_VTABLE,
        "bool" to BOOL_VTABLE,
        "string" to STRING_VTABLE,
        "NoneType" to NONE_VTABLE,
        "list" to LIST_VTABLE,
        "dict" to DICT_VTABLE,
        "tuple" to TUPLE_VTABLE,
        "set" to SET_VTABLE,
    )

    fun forType(typeName: String): TyStarlarkValueVTable {
        return vtablesByName[typeName] ?: TyStarlarkValueVTable(typeName)
    }
}

/**
 * Type implementation where typing is handled by the [StarlarkValue] trait implementation.
 *
 * Wraps a vtable that captures type metadata and behavior flags from a `StarlarkValue`
 * implementation, then exposes query methods the type checker uses to determine what
 * operations a type supports.
 */
class TyStarlarkValue private constructor(
    private val vtable: TyStarlarkValueVTable,
) : Comparable<TyStarlarkValue> {

    // -- Debug --
    // Rust: impl Debug for TyStarlarkValue
    internal fun debugString(): String {
        return "TyStarlarkValue { type_name: \"${vtable.typeName}\", .. }"
    }

    // -- Display --
    // Rust: impl Display for TyStarlarkValue { fn fmt ... }
    override fun toString(): String {
        return fmtWithConfig(TypeRenderConfig.Default)
    }

    // -- PartialEq / Eq --
    // Rust: compares starlark_type_id
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TyStarlarkValue) return false
        return vtable.starlarkTypeId == other.vtable.starlarkTypeId
    }

    // -- Hash --
    // Rust: hashes type_name because type id is not stable
    override fun hashCode(): Int {
        return vtable.typeName.hashCode()
    }

    // -- Ord --
    // Rust: compares type_name lexicographically
    override fun compareTo(other: TyStarlarkValue): Int {
        return vtable.typeName.compareTo(other.vtable.typeName)
    }

    // Cannot have this check in constructor where it belongs because new() needs to be lightweight.
    private fun selfCheck() {
        check(vtable.starlarkTypeId == vtable.starlarkTypeIdCheck) {
            "`Canonical` for `${vtable.typeName}` is not canonical"
        }
    }

    internal fun starlarkTypeId(): String {
        return vtable.starlarkTypeId
    }

    internal fun asName(): String {
        selfCheck()
        return vtable.typeName
    }

    fun isStr(): Boolean {
        selfCheck()
        return this == new("string")
    }

    fun isInt(): Boolean {
        selfCheck()
        return this == new("int")
    }

    fun isList(): Boolean {
        selfCheck()
        return this == new("list")
    }

    fun isDict(): Boolean {
        selfCheck()
        return this == new("dict")
    }

    fun isTuple(): Boolean {
        selfCheck()
        return this == new("tuple")
    }

    fun isSet(): Boolean {
        selfCheck()
        return this == new("set")
    }

    /** Result of applying unary operator to this type. */
    internal fun unOp(unOp: TypingUnOp): Result<TyStarlarkValue> {
        val has = when (unOp) {
            TypingUnOp.PLUS -> vtable.hasPlus
            TypingUnOp.MINUS -> vtable.hasMinus
            TypingUnOp.BIT_NOT -> vtable.hasBitNot
        }
        return if (has) {
            Result.success(this)
        } else {
            Result.failure(TypingNoContextError)
        }
    }

    internal fun binOp(op: TypingBinOp, rhs: TyBasic): Result<Ty> {
        val ty = vtable.binOpTy(op, rhs)
        return if (ty != null) {
            Result.success(ty)
        } else {
            Result.failure(TypingNoContextError)
        }
    }

    internal fun rbinOp(op: TypingBinOp, lhs: TyBasic): Result<Ty> {
        val ty = vtable.rbinOpTy(lhs, op)
        return if (ty != null) {
            Result.success(ty)
        } else {
            Result.failure(TypingNoContextError)
        }
    }

    internal fun index(_index: TyBasic): Result<Ty> {
        return if (vtable.hasAt) {
            Result.success(Ty.any())
        } else {
            Result.failure(TypingNoContextError)
        }
    }

    /** If this type can be sliced, return the result type of slicing. */
    internal fun slice(): Result<Ty> {
        return if (vtable.hasSlice) {
            // All known implementations of slice return self type.
            Result.success(Ty.basic(TyBasic.StarlarkValue(this)))
        } else {
            Result.failure(TypingNoContextError)
        }
    }

    internal fun isIndexable(): Boolean {
        return vtable.hasAt
    }

    internal fun attrFromMethods(name: String): Result<Ty> {
        val methods = vtable.getMethods()
        if (methods != null) {
            val ty = methods.getTy(name)
            if (ty != null) {
                return Result.success(ty)
            }
        }
        return Result.failure(TypingNoContextError)
    }

    internal fun attr(name: String): Result<Ty> {
        attrFromMethods(name).onSuccess { return Result.success(it) }
        val ty = vtable.attrTy(name)
        if (ty != null) {
            return Result.success(ty)
        }
        return Result.failure(TypingNoContextError)
    }

    internal fun isCallable(): Boolean {
        return vtable.hasInvoke
    }

    /**
     * Validate that this type is callable.
     *
     * In Rust: `fn validate_call(self, span: Span, oracle: TypingOracleCtx) -> Result<Ty, TypingError>`
     * Returns [Ty.any] if callable, throws if not.
     */
    internal fun validateCall(
        span: Span,
        _oracle: TypingOracleCtx,
    ): Ty {
        if (isCallable()) {
            return Ty.any()
        } else {
            throw TyStarlarkValueError.NotCallable(this)
        }
    }

    /** Instance of this type can be evaluated as a type. */
    internal fun isType(): Boolean {
        selfCheck()
        return isTypeFromVtable(vtable)
    }

    internal fun iterItem(): Result<Ty> {
        return if (isIterableFromVtable(vtable)) {
            Result.success(Ty.any())
        } else {
            Result.failure(TypingNoContextError)
        }
    }

    /** Convert to runtime type matcher. */
    internal fun <R> matcher(alloc: TypeMatcherAlloc<R>): R {
        selfCheck()

        // First handle special cases that can match faster than default matcher.
        // These are optimizations.
        return if (vtable.starlarkTypeId == "int") {
            alloc.int()
        } else if (vtable.starlarkTypeId == "bool") {
            alloc.bool()
        } else if (vtable.starlarkTypeId == "NoneType") {
            alloc.none()
        } else if (vtable.starlarkTypeId == "string") {
            alloc.str()
        } else {
            alloc.alloc(StarlarkTypeIdMatcher(vtable.starlarkTypeId))
        }
    }

    internal fun fmtWithConfig(config: TypeRenderConfig): String {
        val typeName = when (vtable.typeName) {
            "string" -> "str"
            "NoneType" -> "None"
            else -> vtable.typeName
        }
        return when (config) {
            is TypeRenderConfig.Default -> typeName
            is TypeRenderConfig.LinkedType -> config.renderLinkedTyStarlarkValue(this)
        }
    }

    companion object {
        /**
         * Create a type instance from a type name.
         *
         * In Rust: `pub const fn new<'v, T: StarlarkValue<'v>>() -> TyStarlarkValue`
         * In Kotlin, since `StarlarkValue.TYPE` is an instance property and we cannot
         * extract it from a KClass alone, callers pass the type name string directly.
         */
        fun new(typeName: String): TyStarlarkValue {
            return TyStarlarkValue(TyStarlarkValueVTableGet.forType(typeName))
        }

        fun int(): TyStarlarkValue = TyStarlarkValue(TyStarlarkValueVTableGet.INT_VTABLE)

        fun float(): TyStarlarkValue = TyStarlarkValue(TyStarlarkValueVTableGet.FLOAT_VTABLE)

        fun bool(): TyStarlarkValue = TyStarlarkValue(TyStarlarkValueVTableGet.BOOL_VTABLE)

        fun tuple(): TyStarlarkValue = TyStarlarkValue(TyStarlarkValueVTableGet.TUPLE_VTABLE)

        fun string(): TyStarlarkValue = TyStarlarkValue(TyStarlarkValueVTableGet.STRING_VTABLE)

        fun none(): TyStarlarkValue = TyStarlarkValue(TyStarlarkValueVTableGet.NONE_VTABLE)

        fun list(): TyStarlarkValue = TyStarlarkValue(TyStarlarkValueVTableGet.LIST_VTABLE)

        fun dict(): TyStarlarkValue = TyStarlarkValue(TyStarlarkValueVTableGet.DICT_VTABLE)

        fun set(): TyStarlarkValue = TyStarlarkValue(TyStarlarkValueVTableGet.SET_VTABLE)

        /** Check if a vtable indicates the type is iterable. */
        private fun isIterableFromVtable(vtable: TyStarlarkValueVTable): Boolean {
            return vtable.hasIterate || vtable.hasIterateCollect
        }

        /** Check if a vtable indicates the type can be evaluated as a type. */
        private fun isTypeFromVtable(vtable: TyStarlarkValueVTable): Boolean {
            return vtable.hasEvalType
        }
    }
}
