// port-lint: source src/typing/starlark_value.rs
package io.github.kotlinmania.starlark_kotlin.typing

import io.github.kotlinmania.starlark_kotlin.tests.assert



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

private sealed class TyStarlarkValueError : Exception() {
    data class NotCallable(val ty: TyStarlarkValue) : TyStarlarkValueError() {
        override val message: String get() = "Type `$ty` is not callable"
    }
}

enum class TypingUnOp {
    Plus,
    Minus,
    BitNot,
}

// This is a bit suboptimal for binary size:
// we have two vtable instances for each type: this one, and the one within `AValue` vtable.

/**
 * VTable holding type-level information for a [TyStarlarkValue].
 *
 * In Rust this is `TyStarlarkValueVTable` which stores a static reference to type name,
 * the `StarlarkValueVTable`, and `StarlarkTypeId` fields for canonical type checking.
 * In Kotlin we flatten this into a data class since we don't have static vtable pointers.
 */
private data class TyStarlarkValueVTable(
    val typeName: String,
    // TODO(nga): put these into generated `StarlarkValueVTable`.
    val hasPlus: Boolean = false,
    val hasMinus: Boolean = false,
    val hasBitNot: Boolean = false,
    val hasAt: Boolean = false,
    val hasSlice: Boolean = false,
    val hasInvoke: Boolean = false,
    val hasIterate: Boolean = false,
    val hasIterateCollect: Boolean = false,
    val hasEvalType: Boolean = false,
    val starlarkTypeId: String = typeName,
    /// `starlark_type_id` is `TypeId` of `T::Canonical`.
    /// This is `TypeId` of `T::Canonical::Canonical`.
    val starlarkTypeIdCheck: String = typeName,
)

/**
 * Provides a const VTABLE for a given StarlarkValue implementation type.
 *
 * In Rust: `struct TyStarlarkValueVTableGet<'v, T: StarlarkValue<'v>>(PhantomData<&'v T>)`
 * with a const `VTABLE: TyStarlarkValueVTable` field.
 */
private object TyStarlarkValueVTableGet {
    // Pre-built vtables for known types, analogous to Rust's const generics.
    val INT_VTABLE = TyStarlarkValueVTable(
        typeName = "int",
        hasPlus = true, hasMinus = true, hasBitNot = true,
        hasAt = false, hasSlice = false, hasInvoke = false,
        hasIterate = false, hasIterateCollect = false, hasEvalType = false,
    )
    val FLOAT_VTABLE = TyStarlarkValueVTable(
        typeName = "float",
        hasPlus = true, hasMinus = true, hasBitNot = false,
        hasAt = false, hasSlice = false, hasInvoke = false,
        hasIterate = false, hasIterateCollect = false, hasEvalType = false,
    )
    val BOOL_VTABLE = TyStarlarkValueVTable(
        typeName = "bool",
        hasPlus = false, hasMinus = false, hasBitNot = false,
        hasAt = false, hasSlice = false, hasInvoke = false,
        hasIterate = false, hasIterateCollect = false, hasEvalType = false,
    )
    val STRING_VTABLE = TyStarlarkValueVTable(
        typeName = "string",
        hasPlus = false, hasMinus = false, hasBitNot = false,
        hasAt = true, hasSlice = true, hasInvoke = false,
        hasIterate = true, hasIterateCollect = false, hasEvalType = false,
    )
    val NONE_VTABLE = TyStarlarkValueVTable(
        typeName = "NoneType",
        hasPlus = false, hasMinus = false, hasBitNot = false,
        hasAt = false, hasSlice = false, hasInvoke = false,
        hasIterate = false, hasIterateCollect = false, hasEvalType = false,
    )
    val LIST_VTABLE = TyStarlarkValueVTable(
        typeName = "list",
        hasPlus = false, hasMinus = false, hasBitNot = false,
        hasAt = true, hasSlice = true, hasInvoke = false,
        hasIterate = true, hasIterateCollect = true, hasEvalType = false,
    )
    val DICT_VTABLE = TyStarlarkValueVTable(
        typeName = "dict",
        hasPlus = false, hasMinus = false, hasBitNot = false,
        hasAt = true, hasSlice = false, hasInvoke = false,
        hasIterate = true, hasIterateCollect = true, hasEvalType = false,
    )
    val TUPLE_VTABLE = TyStarlarkValueVTable(
        typeName = "tuple",
        hasPlus = false, hasMinus = false, hasBitNot = false,
        hasAt = true, hasSlice = true, hasInvoke = false,
        hasIterate = true, hasIterateCollect = true, hasEvalType = false,
    )
    val SET_VTABLE = TyStarlarkValueVTable(
        typeName = "set",
        hasPlus = false, hasMinus = false, hasBitNot = false,
        hasAt = false, hasSlice = false, hasInvoke = false,
        hasIterate = true, hasIterateCollect = true, hasEvalType = false,
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

/** Type implementation where typing is handled by the `StarlarkValue` trait implementation. */
data class TyStarlarkValue(
    val typeName: String
) : Comparable<TyStarlarkValue> {

    private val vtable: TyStarlarkValueVTable = TyStarlarkValueVTableGet.forType(typeName)

    companion object {
        /** Create a type instance from an implementation of `StarlarkValue`. */
        fun new(typeName: String): TyStarlarkValue = TyStarlarkValue(typeName)

        fun none(): TyStarlarkValue = TyStarlarkValue("NoneType")
        fun string(): TyStarlarkValue = TyStarlarkValue("string")
        fun int(): TyStarlarkValue = TyStarlarkValue("int")
        fun float(): TyStarlarkValue = TyStarlarkValue("float")
        fun bool(): TyStarlarkValue = TyStarlarkValue("bool")
        fun tuple(): TyStarlarkValue = TyStarlarkValue("tuple")

        fun isIterable(vtable: TyStarlarkValueVTable): Boolean {
            return vtable.hasIterate || vtable.hasIterateCollect
        }

        fun isIterable(typeName: String): Boolean {
            return isIterable(TyStarlarkValueVTableGet.forType(typeName))
        }

        /** Instance of this type can be evaluated as a type. */
        fun isTypeFromVtable(vtable: TyStarlarkValueVTable): Boolean {
            return vtable.hasEvalType
        }
    }

    fun starlarkTypeId(): String = vtable.starlarkTypeId

    // Cannot have this check in constructor where it belongs because `new` is `const`.
    fun selfCheck() {
        assert(vtable.starlarkTypeId == vtable.starlarkTypeIdCheck) {
            "`Canonical` for `${vtable.typeName}` is not canonical"
        }
    }

    fun asName(): String {
        selfCheck()
        return vtable.typeName
    }

    fun isStr(): Boolean {
        selfCheck()
        return this == TyStarlarkValue("string")
    }

    fun isInt(): Boolean {
        selfCheck()
        return this == int()
    }

    fun isList(): Boolean {
        selfCheck()
        return this == TyStarlarkValue("list")
    }

    fun isDict(): Boolean {
        selfCheck()
        return this == TyStarlarkValue("dict")
    }

    fun isTuple(): Boolean {
        selfCheck()
        return this == tuple()
    }

    fun isSet(): Boolean {
        selfCheck()
        return this == TyStarlarkValue("set")
    }

    /** Result of applying unary operator to this type. */
    fun unOp(unOp: TypingUnOp): Result<TyStarlarkValue> {
        val has = when (unOp) {
            TypingUnOp.Plus -> vtable.hasPlus
            TypingUnOp.Minus -> vtable.hasMinus
            TypingUnOp.BitNot -> vtable.hasBitNot
        }
        return if (has) {
            Result.success(this)
        } else {
            Result.failure(TypingNoContextError())
        }
    }

    fun binOp(op: TypingBinOp, rhs: TyBasic): Result<Ty> {
        // In Rust: match (self.vtable.vtable.bin_op_ty)(op, rhs)
        return Result.failure(TypingNoContextError())
    }

    fun rbinOp(op: TypingBinOp, lhs: TyBasic): Result<Ty> {
        // In Rust: match (self.vtable.vtable.rbin_op_ty)(lhs, op)
        return Result.failure(TypingNoContextError())
    }

    fun index(index: TyBasic): Result<Ty> {
        if (vtable.hasAt) {
            return Result.success(Ty.any())
        }
        return Result.failure(TypingNoContextError())
    }

    /** If this type can be sliced, return the result type of slicing. */
    fun slice(): Result<Ty> {
        if (vtable.hasSlice) {
            // All known implementations of slice return self type.
            return Result.success(Ty.basic(TyBasic.StarlarkValue(this)))
        }
        return Result.failure(TypingNoContextError())
    }

    fun isIndexable(): Boolean = vtable.hasAt

    fun attrFromMethods(name: String): Result<Ty> {
        // In Rust: (self.vtable.vtable.get_methods)() -> methods.get_ty(name)
        return Result.failure(TypingNoContextError())
    }

    fun attr(name: String): Result<Ty> {
        val methodResult = attrFromMethods(name)
        if (methodResult.isSuccess) {
            return methodResult
        }
        // In Rust: (self.vtable.vtable.attr_ty)(name)
        return Result.failure(TypingNoContextError())
    }

    fun isCallable(): Boolean = vtable.hasInvoke

    fun validateCall(): Result<Ty> {
        if (isCallable()) {
            return Result.success(Ty.any())
        }
        return Result.failure(TyStarlarkValueError.NotCallable(this))
    }

    fun isType(): Boolean {
        selfCheck()
        return isTypeFromVtable(vtable)
    }

    fun iterItem(): Result<Ty> {
        if (isIterable(vtable)) {
            return Result.success(Ty.any())
        }
        return Result.failure(TypingNoContextError())
    }

    /** Convert to runtime type matcher. */
    fun <T> matcher(factory: TypeMatcherFactory<T>): T {
        selfCheck()
        // First handle special cases that can match faster than default matcher.
        // These are optimizations.
        if (vtable.starlarkTypeId == "int") {
            return factory.int()
        } else if (vtable.starlarkTypeId == "bool") {
            return factory.bool()
        } else if (vtable.starlarkTypeId == "NoneType") {
            return factory.none()
        } else if (vtable.starlarkTypeId == "string") {
            return factory.str()
        } else {
            return factory.alloc(StarlarkTypeIdMatcher(this))
        }
    }

    fun fmtWithConfig(config: TypeRenderConfig): String {
        val displayName = when (vtable.typeName) {
            "string" -> "str"
            "NoneType" -> "None"
            else -> vtable.typeName
        }
        return when (config) {
            is TypeRenderConfig.Default -> displayName
            is TypeRenderConfig.LinkedType -> config.renderLinkedTyStarlarkValue(this)
        }
    }

    override fun compareTo(other: TyStarlarkValue): Int =
        vtable.typeName.compareTo(other.vtable.typeName)

    override fun toString(): String = fmtWithConfig(TypeRenderConfig.Default)
}

/**
 * Matcher that checks if a value's type id matches a [TyStarlarkValue].
 */
data class StarlarkTypeIdMatcher(val ty: TyStarlarkValue) {
    fun matches(typeName: String): Boolean = typeName == ty.typeName
}

interface TypeMatcherFactory<T> {
    fun int(): T
    fun bool(): T
    fun none(): T
    fun str(): T
    fun callable(): T
    fun alloc(matcher: Any): T = byTypeName(TyStarlarkValue("unknown"))
    fun byTypeName(ty: TyStarlarkValue): T
}
