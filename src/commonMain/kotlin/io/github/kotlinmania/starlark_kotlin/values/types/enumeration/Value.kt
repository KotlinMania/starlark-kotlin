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

// Placeholder types referenced from other modules
// These will be replaced with real imports as the port progresses
// TODO: stub - Value needs real import
class Value(private val repr: String = "") {
    override fun toString(): String = repr
    fun writeHash(hasher: StarlarkHasher): Result<Unit> = Result.success(Unit)
    fun serialize(serializer: Any): Result<Any> = Result.success(repr)
    fun toValue(): Value = this
}

// TODO: stub - Ty needs real import
class Ty {
    fun dupe(): Ty = this
}

class StarlarkHasher

// TODO: stub - TypeInstanceId needs real import
class TypeInstanceId

class TyEnumData(val name: String, val tyEnumValue: Ty)

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

class Methods
class MethodsBuilder
// TODO: stub - MethodsStatic needs real import
class MethodsStatic {
    companion object {
        fun new(): MethodsStatic = MethodsStatic()
    }
    fun methods(init: (MethodsBuilder) -> Unit): Methods? = null
}

/// A value from an enumeration.
class EnumValueGen(
    // Must ignore value.typ or type.elements, since they are circular
    internal val typ: Value, // Must be EnumType it points back to (so it can get the type)
    internal val value: Value,   // The value of this enumeration
    internal val index: Int, // The index in the enumeration
    internal val id: TypeInstanceId,
) {
    companion object {
        /// The result of calling `type()` on an enum value.
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

    fun writeHash(hasher: StarlarkHasher): Result<Unit> {
        return value.writeHash(hasher)
    }

    fun getMethods(): Methods? {
        val res = MethodsStatic.new()
        return res.methods(::enumValueMethods)
    }

    fun typecheckerTy(): Ty? {
        val tyEnumType = when (val enumTypeRef = getEnumType()) {
            is EnumTypeRef.Unfrozen -> enumTypeRef.value.tyEnumData() ?: return null
            is EnumTypeRef.Frozen -> enumTypeRef.value.tyEnumData() ?: return null
            null -> return null
        }
        return tyEnumType.tyEnumValue.dupe()
    }

    fun serialize(serializer: Any): Result<Any> {
        return value.serialize(serializer)
    }
}

// Enum value type aliases
typealias EnumValue = EnumValueGen
typealias FrozenEnumValue = EnumValueGen

fun enumValueMethods(methods: MethodsBuilder) {
    // #[starlark(attribute)]
    // fn index(this: &EnumValue) -> starlark::Result<i32>
    fun index(thisVal: EnumValue): Result<Int> {
        return Result.success(thisVal.index)
    }

    // #[starlark(attribute)]
    // fn value(this: &EnumValue) -> starlark::Result<Value>
    fun value(thisVal: EnumValue): Result<Value> {
        return Result.success(thisVal.value.toValue())
    }
}
