// port-lint: source src/pagable/vtableRegister.rs
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

import kotlin.reflect.KClass
import io.github.kotlinmania.starlark.values.layout.AValueVTable

/**
 * for deserialization via the `inventory` crate (compile-time global registry).
 *
 * In Kotlin, we import a runtime-based registry instead of macros.
 * Types are registered via function calls during module initialization.
 */

/**
 * Register a frozen value type for deserialization.
 *
 * Invoke this function for each frozen StarlarkValue type that needs to be
 * deserializable. In most cases, the StarlarkValue annotation handles registration
 * automatically. Use this function only when auto-registration doesn't apply.
 *
 * Without registration, attempting to deserialize a heap containing that type
 * will fail.
 */
fun registerAvalueSimpleFrozen(type: KClass<*>) {
    registerVTableEntry(
        VTableRegistryEntry(
            deserTypeId = DeserTypeId(type),
            vtable = AValueVTable.forType(type),
        )
    )
}

/**
 * Register a vtable for a special type with a custom AValue implementation.
 *
 * This function is for special types (like StarlarkStr, FrozenTuple, FrozenListData)
 * that import custom AValue implementations instead of the simple wrapper.
 */
internal fun registerSpecialAvalueFrozen(starlarkValue: KClass<*>, avalue: KClass<*>) {
    registerVTableEntry(
        VTableRegistryEntry(
            deserTypeId = DeserTypeId(starlarkValue),
            vtable = AValueVTable.forType(avalue),
        )
    )
}

/**
 * Register a vtable for a generic TypeMatcher implementation.
 *
 * This function is for generic TypeMatcher types that cannot import the typeMatcher
 * annotation (which only supports non-generic types).
 */
fun registerTypeMatcher(matcher: KClass<*>) {
    registerAvalueSimpleFrozen(matcher)
}
