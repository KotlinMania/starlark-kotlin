// port-lint: source src/values/types/structs/refs.rs
package io.github.kotlinmania.starlark.values.types.structs

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

import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark.values.UnpackValue
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.starlarktypeid.StarlarkTypeId

/**
 * Reference to a struct allocated on the heap.
 *
 * Struct implementation (for example, memory layout) may change,
 * this type provides implementation agnostics API to it.
 */
@ConsistentCopyVisibility
data class StructRef internal constructor(
    private val struct: Struct,
) {
    companion object {
        /**
         * Downcast a value to a struct reference.
         */
        fun fromValue(value: Value): StructRef? = StructGen.fromValue(value)?.let { StructRef(it) }

        internal fun isInstance(value: Value): Boolean {
            // debug_assert in Rust: StarlarkTypeId::of::<Struct>() == StarlarkTypeId::of::<FrozenStruct>()
            return value.starlarkTypeId() == StarlarkTypeId.of(StructGen::class)
        }
    }

    /**
     * Iterate over struct fields.
     */
    fun iter(): Sequence<Pair<String, Value>> = struct.iter()
}

/**
 * Reference to the frozen struct.
 */
@ConsistentCopyVisibility
data class FrozenStructRef internal constructor(
    internal val struct: FrozenStruct,
) {
    /**
     * Iterate over struct fields.
     */
    fun iter(): Sequence<Pair<String, FrozenValue>> = struct.iter()

    companion object {
        /**
         * Downcast a value to a struct reference.
         */
        fun fromValue(value: FrozenValue): FrozenStructRef? =
            value
                .downcastRef<FrozenStruct>()
                ?.let { FrozenStructRef(it) }
    }
}

// impl StarlarkTypeRepr for StructRef
object StructRefStarlarkTypeRepr : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty = Ty.anyStruct()
}

// impl UnpackValue for StructRef
object StructRefUnpackValue : UnpackValue<StructRef> {
    override fun starlarkTypeRepr(): Ty = StructRefStarlarkTypeRepr.starlarkTypeRepr()

    override fun unpackValueImpl(value: Value): Result<StructRef?> = Result.success(StructRef.fromValue(value))
}
