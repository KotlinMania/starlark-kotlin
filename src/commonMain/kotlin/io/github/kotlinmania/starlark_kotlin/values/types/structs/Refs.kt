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
import io.github.kotlinmania.starlark_kotlin.values.FrozenStringValue
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StringValue
import io.github.kotlinmania.starlark_kotlin.values.UnpackValue
import io.github.kotlinmania.starlark_kotlin.values.Value
import io.github.kotlinmania.starlark_kotlin.values.starlarkTypeId.StarlarkTypeId
import io.github.kotlinmania.starlark_kotlin.values.typeRepr.StarlarkTypeRepr

/**
 * Reference to a struct allocated on the heap.
 *
 * Struct implementation (for example, memory layout) may change,
 * this type provides implementation agnostics API to it.
 */
data class StructRef<'v> internal constructor(
    private val struct: Struct<'v>
) {
    companion object {
        /**
         * Downcast a value to a struct reference.
         */
        fun <'v> fromValue(value: Value<'v>): StructRef<'v>? {
            return Struct.fromValue(value)?.let { StructRef(it) }
        }

        internal fun <'v> isInstance(value: Value<'v>): Boolean {
            // debug_assert in Rust: StarlarkTypeId::of::<Struct>() == StarlarkTypeId::of::<FrozenStruct>()
            return value.starlarkTypeId() == StarlarkTypeId.of<Struct<Any>>()
        }
    }

    /**
     * Iterate over struct fields.
     */
    fun iter(): Iterator<Pair<StringValue<'v>, Value<'v>>> {
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

// impl<'v> StarlarkTypeRepr for StructRef<'v>
object StructRefStarlarkTypeRepr : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty {
        return FrozenStruct.starlarkTypeRepr()
    }
}

// impl<'v> UnpackValue<'v> for StructRef<'v>
object StructRefUnpackValue : UnpackValue<Nothing> {
    override fun <'v> unpackValueImpl(value: Value<'v>): Result<StructRef<'v>?> {
        return Result.success(StructRef.fromValue(value))
    }
}

