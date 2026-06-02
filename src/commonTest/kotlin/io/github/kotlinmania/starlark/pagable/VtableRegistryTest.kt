// port-lint: tests src/pagable/vtable_registry.rs (tests)
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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// / Tests for the vtable registry.
// /
// / The Rust tests use `inventory::collect!` and derive macros
// / (`starlark_simple_value!`, `#[starlark_value]`, `#[derive(Trace, Freeze, Coerce)]`)
// / to register test types at compile time. In Kotlin, we register entries
// / manually to test the same registry lookup behavior.
class VtableRegistryTest {
    @Test
    fun testLookupRegisteredType() {
        // Register a test entry and verify we can look it up
        val typeId = DeserTypeId.of<TestRegistryType>()
        val vtable = TestVTableFactory.createTestVTable("TestRegistryType")
        registerVTableEntry(
            VTableRegistryEntry(
                deserTypeId = typeId,
                vtable = vtable,
            ),
        )

        val result = lookupVtable(typeId)
        assertTrue(result.isSuccess, "Expected TestRegistryType to be registered")
        assertEquals("TestRegistryType", result.getOrThrow().typeName)
    }

    @Test
    fun testLookupNonexistentType() {
        // Looking up a non-existent type should return an error
        val typeId = DeserTypeId.of<NonExistentType>()
        val result = lookupVtable(typeId)
        assertTrue(result.isFailure, "Expected error for non-existent type")
        val error = result.exceptionOrNull()
        assertTrue(
            error is PagableError.TypeNotRegistered,
            "Expected TypeNotRegistered error",
        )
    }

    @Test
    fun testDeserTypeIdEquality() {
        val a = DeserTypeId.of<TestRegistryType>()
        val b = DeserTypeId.of<TestRegistryType>()
        assertEquals(a, b, "Same type should produce equal DeserTypeId")
    }

    @Test
    fun testDeserTypeIdToString() {
        val typeId = DeserTypeId.of<TestRegistryType>()
        val str = typeId.toString()
        assertTrue(str.isNotEmpty(), "DeserTypeId.toString() should not be empty")
    }

    @Test
    fun testRegisteredTypeIds() {
        // Register a type and check it appears in the list
        val typeId = DeserTypeId.of<AnotherTestType>()
        val vtable = TestVTableFactory.createTestVTable("AnotherTestType")
        registerVTableEntry(
            VTableRegistryEntry(
                deserTypeId = typeId,
                vtable = vtable,
            ),
        )

        val ids = registeredTypeIds()
        assertTrue(
            ids.contains(typeId),
            "Expected AnotherTestType to appear in registered type ids",
        )
    }
}

// / Test types used for registry tests.
private class TestRegistryType

private class NonExistentType

private class AnotherTestType

// / Factory to create test vtables.
// / In Rust, vtables are created by proc macros. Here we create minimal test instances.
private object TestVTableFactory {
    fun createTestVTable(typeName: String): io.github.kotlinmania.starlark.values.layout.AValueVTable =
        io.github.kotlinmania.starlark.values.layout.AValueVTable(
            staticTypeOfValue =
                io.github.kotlinmania.starlark.values.layout.ConstTypeId
                    .of<Any>(),
            starlarkTypeId =
                io.github.kotlinmania.starlark.values.starlarktypeid.StarlarkTypeId.fromTypeId(
                    io.github.kotlinmania.starlark.values.layout.ConstTypeId
                        .of<Any>(),
                ),
            typeName = typeName,
            isStr = false,
            memorySizeFn = {
                io.github.kotlinmania.starlark.values.layout
                    .ValueAllocSize(
                        io.github.kotlinmania.starlark.values.layout
                            .AlignedSize(0u),
                    )
            },
            heapFreezeFn = { _, _ -> error("test vtable") },
            heapCopyFn = { _, _ -> error("test vtable") },
            starlarkValue = object : io.github.kotlinmania.starlark.values.StarlarkValue {},
        )
}
