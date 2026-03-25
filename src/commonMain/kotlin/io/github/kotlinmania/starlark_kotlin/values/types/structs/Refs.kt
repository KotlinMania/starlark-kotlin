// port-lint: source src/values/types/structs/refs.rs
package io.github.kotlinmania.starlark_kotlin.values.types.structs

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

import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.UnpackValue
import io.github.kotlinmania.starlark_kotlin.values.types.string.intern.FrozenStringValue
import io.github.kotlinmania.starlark_kotlin.values.types.string.StringValue
import io.github.kotlinmania.starlark_kotlin.values.starlark_type_id.StarlarkTypeId
import io.github.kotlinmania.starlark_kotlin.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenStringValue
import io.github.kotlinmania.starlark_kotlin.values.owned.asRef
import io.github.kotlinmania.starlark_kotlin.tests.derive.starlarkTypeRepr
import io.github.kotlinmania.starlark_kotlin.values.starlark_type_id.starlarkTypeId
import io.github.kotlinmania.starlark_kotlin.eval.compiler.call.downcastFrozenRef
import io.github.kotlinmania.starlark_kotlin.analysis.iter
import io.github.kotlinmania.starlark_kotlin.values.owned_frozen_ref.asRef

/**
 * Reference to a struct allocated on the heap.
 *
 * Struct implementation (for example, memory layout) may change,
 * this type provides implementation agnostics API to it.
 */
data class StructRef<V_> internal constructor(
    private val struct: Struct<V_>
) {
    companion object {
        /**
         * Downcast a value to a struct reference.
         */
        fun <V_> fromValue(value: Value<V_>): StructRef<V_>? {
            return Struct.fromValue(value)?.let { StructRef(it) }
        }

        internal fun <V_> isInstance(value: Value<V_>): Boolean {
            // debug_assert in Rust: StarlarkTypeId::of::<Struct>() == StarlarkTypeId::of::<FrozenStruct>()
            return value.starlarkTypeId() == StarlarkTypeId.of<Struct<Any>>()
        }
    }

    /**
     * Iterate over struct fields.
     */
    fun iter(): Iterator<Pair<StringValue<V_>, Value<V_>>> {
        return struct.iter()
    }
}

/**
 * Reference to the frozen struct.
 */
data class FrozenStructRef internal constructor(
    internal val struct: FrozenStruct
) {
    /**
     * Iterate over struct fields.
     */
    fun iter(): Iterator<Pair<FrozenStringValue, FrozenValue>> {
        return struct.iterFrozen()
    }

    companion object {
        /**
         * Downcast a value to a struct reference.
         */
        fun fromValue(value: FrozenValue): FrozenStructRef? {
            return value.downcastFrozenRef<FrozenStruct>()
                ?.let { FrozenStructRef(it.asRef()) }
        }
    }
}

// impl<V_> StarlarkTypeRepr for StructRef<V_>
object StructRefStarlarkTypeRepr : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty {
        return FrozenStruct.starlarkTypeRepr()
    }
}

// impl<V_> UnpackValue<V_> for StructRef<V_>
object StructRefUnpackValue : UnpackValue<Nothing> {
    override fun <V_> unpackValueImpl(value: Value<V_>): Result<StructRef<V_>?> {
        return Result.success(StructRef.fromValue(value))
    }
}

