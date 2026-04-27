// port-lint: source src/values/starlarkTypeId.rs
package io.github.kotlinmania.starlark.values.starlarktypeid

/*
 * Copyright 2018 The Starlark in Rust Authors.
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

import io.github.kotlinmania.starlark.values.layout.ConstTypeId
import kotlin.reflect.KClass

/**
 * Identifier of a starlark type.
 *
 * This is different from `TypeId` of `StarlarkValue` implementation:
 * multiple Rust types can share the same `StarlarkTypeId`.
 * For example, mutable and frozen list share the same `StarlarkTypeId`.
 */
data class StarlarkTypeId(
    private val typeId: ConstTypeId,
) {
    companion object {
        fun fromTypeId(typeId: ConstTypeId): StarlarkTypeId {
            return StarlarkTypeId(typeId)
        }

        fun of(klass: KClass<*>): StarlarkTypeId {
            return StarlarkTypeId(ConstTypeId.of(klass))
        }

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
data class StarlarkTypeIdAligned(
    private val starlarkTypeId: StarlarkTypeId,
) {
    companion object {
        fun new(starlarkTypeId: StarlarkTypeId): StarlarkTypeIdAligned {
            return StarlarkTypeIdAligned(starlarkTypeId)
        }
    }

    fun get(): StarlarkTypeId = starlarkTypeId

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StarlarkTypeIdAligned) return false
        return this.get() == other.get()
    }

    override fun hashCode(): Int = starlarkTypeId.hashCode()
}
