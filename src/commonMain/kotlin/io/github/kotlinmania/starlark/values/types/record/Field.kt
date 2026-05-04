// port-lint: source values/types/record/field.rs
package io.github.kotlinmania.starlark.values.types.record

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

import io.github.kotlinmania.starlarkmap.StarlarkHasher
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.typing.TyStarlarkValue
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.typing.typecompiled.TypeCompiled

/**
 * The result of `field()`.
 */
class Field internal constructor(
    internal val typ: TypeCompiled,
    internal val default: Value?,
) : StarlarkValue {

    companion object {
        internal fun new(typ: TypeCompiled, default: Value?): Field {
            return Field(typ = typ, default = default)
        }

        /** Downcast a value to a Field. */
        internal fun fromValue(value: Value): Field? =
            value.downcastRef()
    }

    internal fun ty(): Ty {
        return typ.asTy()
    }

    override val TYPE: String get() = "field"

    override fun writeHash(hasher: StarlarkHasher): Result<Unit> {
        typ.writeHash(hasher).getOrElse { return Result.failure(it) }
        hasher.writeU8(if (default != null) 1u else 0u)
        if (default != null) {
            default.writeHash(hasher).getOrElse { return Result.failure(it) }
        }
        return Result.success(Unit)
    }

    override fun typecheckerTy(): Ty? {
        return Ty.starlarkValue(TyStarlarkValue.new("field"))
    }

    override fun toString(): String {
        return buildString {
            append("field(")
            append(typ)
            if (default != null) {
                append(", ")
                append(default)
            }
            append(")")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Field) return false
        return typ == other.typ && default == other.default
    }

    override fun hashCode(): Int {
        var result = typ.hashCode()
        result = 31 * result + (default?.hashCode() ?: 0)
        return result
    }
}
