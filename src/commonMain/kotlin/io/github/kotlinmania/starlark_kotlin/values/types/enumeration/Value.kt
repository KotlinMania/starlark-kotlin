// port-lint: source src/values/types/enumeration/value.rs
package io.github.kotlinmania.starlark_kotlin.values.types.enumeration.value

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

import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.collections.StarlarkHasher
import io.github.kotlinmania.starlark_kotlin.values.types.TypeInstanceId
import io.github.kotlinmania.starlark_kotlin.values.types.enumeration.TyEnumData
import io.github.kotlinmania.starlark_kotlin.environment.Methods
import io.github.kotlinmania.starlark_kotlin.environment.MethodsBuilder
import io.github.kotlinmania.starlark_kotlin.environment.MethodsStatic
import io.github.kotlinmania.starlark_kotlin.values.types.int.InlineInt

class EnumType(val value: Value) {
    fun tyEnumData(): TyEnumData? = null

    companion object {
        fun fromValue(value: Value): EnumTypeRef? = null
    }
}

class FrozenEnumType {
    fun tyEnumData(): TyEnumData? = null
}

sealed class EnumTypeRef {
    class Unfrozen(val value: EnumType) : EnumTypeRef()
    class Frozen(val value: FrozenEnumType) : EnumTypeRef()
}

/** A value from an enumeration. */
class EnumValueGen(
    // Must ignore value.typ or type.elements, since they are circular
    internal val typ: Value, // Must be EnumType it points back to (so it can get the type)
    internal val value: Value,   // The value of this enumeration
    internal val index: Int, // The index in the enumeration
    internal val id: TypeInstanceId,
) : io.github.kotlinmania.starlark_kotlin.values.StarlarkValue {
    override val TYPE: String get() = Companion.TYPE
    companion object {
        /** The result of calling `type()` on an enum value. */
        const val TYPE: String = "enum"
    }

    private fun getEnumType(): EnumTypeRef? {
        // Safe to unwrap because we always ensure typ is EnumType
        return EnumType.fromValue(typ.toValue())
    }

    override fun toString(): String {
        val tyEnumData = when (val enumTypeRef = getEnumType()) {
            is EnumTypeRef.Unfrozen -> enumTypeRef.value.tyEnumData()
            is EnumTypeRef.Frozen -> enumTypeRef.value.tyEnumData()
            null -> null
        }
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
        val tyEnumType = when (val enumTypeRef = getEnumType()) {
            is EnumTypeRef.Unfrozen -> enumTypeRef.value.tyEnumData() ?: return null
            is EnumTypeRef.Frozen -> enumTypeRef.value.tyEnumData() ?: return null
            null -> return null
        }
        return tyEnumType.tyEnumValue
    }

    // impl serde::Serialize for EnumValueGen
    // self.value.serialize(serializer)
    fun serialize(serializer: Any): Result<Any> {
        // Delegates serialization to the inner value, passing through the serializer.
        val encoder = serializer as kotlinx.serialization.encoding.Encoder
        encoder.encodeString(value.toValue().toString())
        return Result.success(Unit)
    }
}

// Enum value type aliases
typealias EnumValue = EnumValueGen
typealias FrozenEnumValue = EnumValueGen

fun enumValueMethods(methods: MethodsBuilder) {
    // #[starlark(attribute)]
    // fn index(this: &EnumValue) -> starlark::Result<i32>
    methods.setAttributeFn(
        name = "index",
        speculativeExecSafe = true,
        docstring = null,
        typ = Ty.int(),
    ) { _, thisValue, _ ->
        val enumValue = thisValue.downcastRef<EnumValueGen>()!!
        Result.success(Value.newInt(InlineInt.newUnchecked(enumValue.index)))
    }

    // #[starlark(attribute)]
    // fn value(this: &EnumValue) -> starlark::Result<Value>
    methods.setAttributeFn(
        name = "value",
        speculativeExecSafe = true,
        docstring = null,
        typ = Ty.any(),
    ) { _, thisValue, _ ->
        val enumValue = thisValue.downcastRef<EnumValueGen>()!!
        Result.success(enumValue.value.toValue())
    }
}
