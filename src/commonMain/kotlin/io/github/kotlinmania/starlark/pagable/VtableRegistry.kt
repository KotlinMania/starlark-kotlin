// port-lint: source pagable/vtable_registry.rs
package io.github.kotlinmania.starlark.pagable

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

import io.github.kotlinmania.starlark.values.layout.AValueVTable
import io.github.kotlinmania.starlark.values.StarlarkValue
import kotlin.reflect.KClass

/**
 * VTable registry for Starlark value deserialization.
 *
 * This module provides a mechanism for registering and looking up vtables
 * by their deserialization type identifiers. During deserialization, we need
 * to know which vtable to import for a given type, and this registry provides
 * that mapping.
 */

/**
 * Deserialization type identifier for vtable lookup.
 *
 * This is a newtype wrapper around a type-name string. It uniquely identifies a
 * concrete type for deserialization purposes, unlike `StarlarkValue::TYPE` which
 * can be shared (e.g., "function" for EnumType and NativeFunction).
 */
data class DeserTypeId(val name: String) {
    /** Get the underlying type name string. */
    fun asStr(): String {
        val this0 = name
        return this0
    }

    fun fmt(): String {
        val display = name
        val formatter = StringBuilder()
        formatter.append(display)
        return formatter.toString()
    }

    override fun toString(): String = fmt()

    companion object {
        /** Create a `DeserTypeId` for a type. */
        inline fun <reified T : Any> of(): DeserTypeId =
            DeserTypeId(T::class.qualifiedName ?: T::class.toString())

        fun of(type: KClass<*>): DeserTypeId =
            DeserTypeId(type.qualifiedName ?: type.toString())
    }
}

/**
 * Registry entry for vtable lookup during deserialization.
 * Collected at compile time.
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

/** Lookup table mapping deserTypeId to vtable, built lazily. */
private val VTABLE_REGISTRY: MutableMap<DeserTypeId, AValueVTable> = mutableMapOf()

/**
 * Submit a vtable entry. Call this during module initialisation for each
 * registered type.
 */
fun submitVtable(entry: VTableRegistryEntry) {
    VTABLE_REGISTRY[entry.deserTypeId] = entry.vtable
}

/**
 * Look up a vtable by its deserialization type id.
 * Returns an error if the type is not registered.
 */
fun lookupVtable(deserTypeId: DeserTypeId): Result<AValueVTable> {
    val vt = VTABLE_REGISTRY[deserTypeId]
    return if (vt != null) {
        Result.success(vt)
    } else {
        Result.failure(PagableError.TypeNotRegistered(typeId = deserTypeId.asStr()))
    }
}

/** Get a list of all registered type IDs (for debugging/testing). */
internal fun registeredTypeIds(): List<DeserTypeId> = VTABLE_REGISTRY.keys.toList()

// -----------------------------------------------------------------
// Upstream `vtableRegistry.rs` tests (ported as pure functions).
// -----------------------------------------------------------------

internal class TestSimpleType : StarlarkValue {
    override val TYPE: String get() = "TestSimpleType"
}

internal class FrozenTestComplex : StarlarkValue {
    override val TYPE: String get() = "TestComplex"
}

internal class StarlarkStr : StarlarkValue {
    override val TYPE: String get() = "StarlarkStr"
}

internal class FrozenTuple : StarlarkValue {
    override val TYPE: String get() = "FrozenTuple"
}

internal class FrozenList : StarlarkValue {
    override val TYPE: String get() = "FrozenList"
}

internal class TypeCompiledNonGenericMatcher : StarlarkValue {
    override val TYPE: String get() = "TypeCompiledNonGenericMatcher"
}

internal class TypeCompiledGenericMatcher : StarlarkValue {
    override val TYPE: String get() = "TypeCompiledGenericMatcher"
}

internal fun testSimpleTypeIsRegistered() {
    val deserTypeId = DeserTypeId.of<TestSimpleType>()
    submitVtable(VTableRegistryEntry(deserTypeId = deserTypeId, vtable = AValueVTable.forType(TestSimpleType::class)))
    val vtable = lookupVtable(deserTypeId)
    check(vtable.isSuccess) { "Expected TestSimpleType to be registered. Available types: ${registeredTypeIds()}" }
    val vt = vtable.getOrThrow()
    check(vt.typeName == "TestSimpleType")
}

internal fun testComplexTypeFrozenIsRegistered() {
    val typeId = DeserTypeId.of<FrozenTestComplex>()
    submitVtable(VTableRegistryEntry(deserTypeId = typeId, vtable = AValueVTable.forType(FrozenTestComplex::class)))
    val vtable = lookupVtable(typeId)
    check(vtable.isSuccess) { "Expected FrozenTestComplex to be registered. Available types: ${registeredTypeIds()}" }
    val vt = vtable.getOrThrow()
    check(vt.typeName == "FrozenTestComplex" || vt.typeName == "TestComplex")
}

internal fun testLookupNonexistentType() {
    val result = lookupVtable(DeserTypeId("this_type_does_not_exist_12345"))
    check(result.isFailure)
    val err = result.exceptionOrNull()
    check(err is PagableError.TypeNotRegistered)
}

internal fun testStarlarkStrIsRegistered() {
    val deserTypeId = DeserTypeId.of<StarlarkStr>()
    submitVtable(VTableRegistryEntry(deserTypeId = deserTypeId, vtable = AValueVTable.forType(StarlarkStr::class)))
    val vtable = lookupVtable(deserTypeId)
    check(vtable.isSuccess) { "Expected StarlarkStr to be registered. Available types: ${registeredTypeIds()}" }
    val vt = vtable.getOrThrow()
    check(vt.typeName == "StarlarkStr")
}

internal fun testFrozenTupleIsRegistered() {
    val deserTypeId = DeserTypeId.of<FrozenTuple>()
    submitVtable(VTableRegistryEntry(deserTypeId = deserTypeId, vtable = AValueVTable.forType(FrozenTuple::class)))
    val vtable = lookupVtable(deserTypeId)
    check(vtable.isSuccess) { "Expected FrozenTuple to be registered. Available types: ${registeredTypeIds()}" }
    val vt = vtable.getOrThrow()
    check(vt.typeName == "FrozenTuple")
}

internal fun testFrozenListIsRegistered() {
    val deserTypeId = DeserTypeId.of<FrozenList>()
    submitVtable(VTableRegistryEntry(deserTypeId = deserTypeId, vtable = AValueVTable.forType(FrozenList::class)))
    val vtable = lookupVtable(deserTypeId)
    check(vtable.isSuccess) { "Expected FrozenList to be registered. Available types: ${registeredTypeIds()}" }
    val vt = vtable.getOrThrow()
    check(vt.typeName == "FrozenList")
}

internal fun testTypeCompiledNonGenericMatcherIsRegistered() {
    val deserTypeId = DeserTypeId.of<TypeCompiledNonGenericMatcher>()
    submitVtable(
        VTableRegistryEntry(
            deserTypeId = deserTypeId,
            vtable = AValueVTable.forType(TypeCompiledNonGenericMatcher::class),
        ),
    )
    val vtable = lookupVtable(deserTypeId)
    check(vtable.isSuccess) { "Expected TypeCompiledNonGenericMatcher to be registered. Available types: ${registeredTypeIds()}" }
    val vt = vtable.getOrThrow()
    check(vt.typeName == "TypeCompiledNonGenericMatcher")
}

internal fun testTypeCompiledGenericMatcherIsRegistered() {
    val deserTypeId = DeserTypeId.of<TypeCompiledGenericMatcher>()
    submitVtable(
        VTableRegistryEntry(
            deserTypeId = deserTypeId,
            vtable = AValueVTable.forType(TypeCompiledGenericMatcher::class),
        ),
    )
    val vtable = lookupVtable(deserTypeId)
    check(vtable.isSuccess) { "Expected TypeCompiledGenericMatcher to be registered. Available types: ${registeredTypeIds()}" }
    val vt = vtable.getOrThrow()
    check(vt.typeName == "TypeCompiledGenericMatcher")
}
