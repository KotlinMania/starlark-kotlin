// port-lint: source src/values/types/enumeration/value.rs
package io.github.kotlinmania.starlark.values.types.enumeration.value

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

import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.typing.Ty
import starlarkmap.StarlarkHasher
import io.github.kotlinmania.starlark.values.types.TypeInstanceId
import io.github.kotlinmania.starlark.values.types.enumeration.TyEnumData
import io.github.kotlinmania.starlark.environment.Methods
import io.github.kotlinmania.starlark.environment.MethodsBuilder
import io.github.kotlinmania.starlark.environment.MethodsStatic
import io.github.kotlinmania.starlark.values.types.enumeration.enumtype.EnumTypeGen

/** A value from an enumeration. */
class EnumValueGen(
    // Must ignore value.typ or type.elements, since they are circular
    internal val typ: Value, // Must be EnumType it points back to (so it can get the type)
    internal val value: Value,   // The value of this enumeration
    internal val index: Int, // The index in the enumeration
    internal val id: TypeInstanceId,
) : io.github.kotlinmania.starlark.values.StarlarkValue {
    override val TYPE: String get() = Companion.TYPE
    companion object {
        /** The result of calling `type()` on an enum value. */
        const val TYPE: String = "enum"
    }

    private fun getEnumType(): EnumTypeGen? {
        // Safe to unwrap because we always ensure typ is EnumTypeGen
        return typ.downcastRef<EnumTypeGen>()
    }

    override fun toString(): String {
        val tyEnumData = getEnumType()?.tyEnumData()
        return when {
            tyEnumData != null -> "${tyEnumData.name}($value)"
            else -> "enum()($value)"
        }
    }

    override fun writeHash(hasher: StarlarkHasher): Result<Unit> {
        return value.writeHash(hasher)
    }

    override fun getMethods(): Methods? {
        val res = MethodsStatic()
        return res.methods(::enumValueMethods)
    }

    override fun typecheckerTy(): Ty? {
        val tyEnumType = getEnumType()?.tyEnumData() ?: return null
        return tyEnumType.tyEnumValue
    }

    // Delegates serialization to the inner value.
    fun serialize(serializer: Any): Result<Any> {
        return Result.success(value)
    }
}

// Frozen and unfrozen enum values share `EnumValueGen`; the inner value type distinguishes them.

fun enumValueMethods(methods: MethodsBuilder) {
    fun index(thisVal: EnumValueGen): Result<Int> {
        return Result.success(thisVal.index)
    }

    fun value(thisVal: EnumValueGen): Result<Value> {
        return Result.success(thisVal.value)
    }
}
