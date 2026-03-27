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

interface TypeMatcherFactory<T> {
    fun int(): T
    fun bool(): T
    fun none(): T
    fun str(): T
    fun callable(): T
    fun alloc(matcher: Any): T = byTypeName(TyStarlarkValue("unknown"))
    fun byTypeName(ty: TyStarlarkValue): T
}
