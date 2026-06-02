// port-lint: source src/pagable/vtable_registry.rs
package io.github.kotlinmania.starlark.pagable

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

/**
 * VTable registry for Starlark value deserialization.
 *
 * This module provides a mechanism for registering and looking up vtables
 * by their deserialization type identifiers. During deserialization, we need
 * to know which vtable to use for a given type, and this registry provides
 * that mapping.
 */

import io.github.kotlinmania.starlark.values.layout.AValueVTable
import kotlin.reflect.KClass

/**
 * Deserialization type identifier for vtable lookup.
 *
 * This is a wrapper around [KClass] that uniquely identifies a concrete type
 * for deserialization purposes, unlike `StarlarkValue.TYPE` which can be shared
 * (e.g., "function" for EnumType and NativeFunction).
 *
 * In Rust, this uses `std::any::type_name::<T>()` which returns a `&'static str`.
 * In Kotlin, we use [KClass] as the unique identifier and derive the string name
 * from [KClass.qualifiedName].
 */
data class DeserTypeId(
    val typeClass: KClass<*>,
) {
    companion object {
        /** Create a [DeserTypeId] for a type. */
        inline fun <reified T : Any> of(): DeserTypeId = DeserTypeId(T::class)
    }

    /** Get the underlying type name string. */
    fun asStr(): String = typeClass.simpleName ?: typeClass.toString()

    override fun toString(): String = asStr()
}

/**
 * Registry entry for vtable lookup during deserialization.
 * In Rust, these are collected at compile time via the `inventory` crate.
 * In Kotlin, entries are registered manually at initialization time.
 */
class VTableRegistryEntry(
    /**
     * Deserialization type identifier.
     * Used as the key for vtable lookup during deserialization.
     */
    val deserTypeId: DeserTypeId,
    /** The vtable for this type. */
    val vtable: AValueVTable,
)

/**
 * Lookup table mapping deser_type_id to vtable.
 *
 * In Rust, this is built lazily from `inventory::iter`. In Kotlin, entries are
 * registered imperatively via [registerVTableEntry].
 */
private object VTableRegistry {
    val registry: MutableMap<DeserTypeId, AValueVTable> = mutableMapOf()
}

/**
 * Register a vtable entry in the global registry.
 *
 * This replaces Rust's `inventory::collect!` / `inventory::submit!` pattern.
 * Call this during module initialization for each type that needs vtable lookup
 * during deserialization.
 */
fun registerVTableEntry(entry: VTableRegistryEntry) {
    VTableRegistry.registry[entry.deserTypeId] = entry.vtable
}

/**
 * Look up a vtable by its deserialization type id.
 * Returns a failure result if the type is not registered.
 */
fun lookupVtable(deserTypeId: DeserTypeId): Result<AValueVTable> =
    VTableRegistry.registry[deserTypeId]?.let {
        Result.success(it)
    } ?: Result.failure(
        PagableError.TypeNotRegistered(typeId = deserTypeId.asStr()),
    )

/** Get a list of all registered type IDs (for debugging/testing). */
internal fun registeredTypeIds(): List<DeserTypeId> = VTableRegistry.registry.keys.toList()
