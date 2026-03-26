// port-lint: source src/values/starlark_type_id.rs
package io.github.kotlinmania.starlark_kotlin.values.starlark_type_id

/*
 * Copyright 2018 The Starlark in Rust Authors.
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

import io.github.kotlinmania.starlark_kotlin.values.layout.const_type_id.ConstTypeId
import kotlin.reflect.KClass

/**
 * Identifier of a starlark type.
 *
 * This is different from `TypeId` of `StarlarkValue` implementation:
 * multiple Rust types can share the same `StarlarkTypeId`.
 * For example, mutable and frozen list share the same `StarlarkTypeId`.
 */
internal data class StarlarkTypeId(
    // ConstTypeId
    private val typeId: ConstTypeId,
) {
    // impl StarlarkTypeId

    companion object {
        // pub(crate) const fn from_type_id(type_id: ConstTypeId) -> StarlarkTypeId
        fun fromTypeId(typeId: ConstTypeId): StarlarkTypeId {
            return StarlarkTypeId(typeId)
        }

        // pub(crate) const fn of<'v, T: StarlarkValue<'v>>() -> StarlarkTypeId
        // Kotlin: Use KClass instead of type parameters with compile-time TypeId.
        fun of(klass: KClass<*>): StarlarkTypeId {
            return StarlarkTypeId(ConstTypeId.of(klass))
        }

        // pub(crate) const fn of_canonical<'v, T: StarlarkValue<'v>>() -> StarlarkTypeId
        fun ofCanonical(klass: KClass<*>): StarlarkTypeId {
            return StarlarkTypeId(ConstTypeId.of(klass))
        }
    }
}

/**
 * We require alignment 8 for `StarlarkValue`.
 * `TypeId` is 16 bytes aligned on Rust 1.72 on Apple Silicon.
 * Use this struct to put `ConstTypeId` in a `StarlarkValue`.
 *
 * In Kotlin, there are no alignment concerns; this is a simple wrapper.
 */
internal data class StarlarkTypeIdAligned(
    private val starlarkTypeId: StarlarkTypeId,
) {
    // impl StarlarkTypeIdAligned

    companion object {
        // pub(crate) const fn new(starlark_type_id: StarlarkTypeId) -> StarlarkTypeIdAligned
        fun new(starlarkTypeId: StarlarkTypeId): StarlarkTypeIdAligned {
            return StarlarkTypeIdAligned(starlarkTypeId)
        }
    }

    // pub(crate) const fn get(&self) -> StarlarkTypeId
    fun get(): StarlarkTypeId = starlarkTypeId
}
