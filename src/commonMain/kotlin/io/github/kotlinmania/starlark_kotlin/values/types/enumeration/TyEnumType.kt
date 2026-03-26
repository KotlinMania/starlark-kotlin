// port-lint: source src/values/types/enumeration/ty_enum_type.rs
package io.github.kotlinmania.starlark_kotlin.values.types.enumeration

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
import io.github.kotlinmania.starlark_kotlin.values.types.type_instance_id.TypeInstanceId

class TyEnumData(
    /** Name of the enum type. */
    internal val name: String,
    /** Globally unique id of the enum type. */
    // Id must be last so Ord is deterministic.
    internal val id: TypeInstanceId,
    /** Type of enum variant. */
    internal val tyEnumValue: Ty,
    /** Type of enum type value. */
    internal val tyEnumType: Ty,
) : Comparable<TyEnumData> {

    // impl PartialEq for TyEnumData
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TyEnumData) return false
        return id == other.id
    }

    // impl Hash for TyEnumData
    override fun hashCode(): Int {
        // Do not hash `id` because hashing should be deterministic.
        return name.hashCode()
    }

    // impl Ord for TyEnumData
    override fun compareTo(other: TyEnumData): Int {
        val nameCmp = name.compareTo(other.name)
        if (nameCmp != 0) return nameCmp
        return id.compareTo(other.id)
    }
}
