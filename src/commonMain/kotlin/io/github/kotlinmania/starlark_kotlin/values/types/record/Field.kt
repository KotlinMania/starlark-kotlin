// port-lint: source src/values/types/record/field.rs
package io.github.kotlinmania.starlark_kotlin.values.types.record

/*
 * Copyright 2019 The Starlark in Rust Authors.
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

import io.github.kotlinmania.starlark_kotlin.collections.StarlarkHasher
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.TypeCompiled

/**
 * The result of `field()`.
 */
// #[derive(Clone, Debug, Dupe, Trace, Freeze, NoSerialize, ProvidesStaticType, Allocative)]
// pub struct FieldGen<V: ValueLifetimeless> {
//     pub(crate) typ: TypeCompiled<V>,
//     pub(crate) default: Option<V>,
// }
// starlark_complex_value!(pub(crate) Field);
// Kotlin: single class, no lifetime parameterization.
class Field internal constructor(
    internal val typ: TypeCompiled,
    internal val default: Value?,
) : StarlarkValue {

    // impl FieldGen

    companion object {
        // pub(crate) fn new(typ, default) -> Self
        internal fun new(typ: TypeCompiled, default: Value?): Field {
            return Field(typ = typ, default = default)
        }
    }

    // pub(crate) fn ty(&self) -> Ty
    internal fun ty(): Ty {
        return typ.asTy()
    }

    // #[starlark_value(type = "field")]
    // impl StarlarkValue for FieldGen

    // fn write_hash(&self, hasher: &mut StarlarkHasher) -> crate::Result<()>
    override fun writeHash(hasher: StarlarkHasher): Result<Unit> {
        typ.writeHash(hasher).getOrElse { return Result.failure(it) }
        hasher.writeU8(if (default != null) 1u else 0u)
        if (default != null) {
            default.writeHash(hasher).getOrElse { return Result.failure(it) }
        }
        return Result.success(Unit)
    }

    // fn typechecker_ty(&self) -> Option<Ty>
    override fun typecheckerTy(): Ty? {
        return Ty.starlarkValue<Field>()
    }

    // impl Display for FieldGen
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
