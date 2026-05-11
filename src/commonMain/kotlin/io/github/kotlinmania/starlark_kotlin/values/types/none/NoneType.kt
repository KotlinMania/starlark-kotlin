// port-lint: source src/values/types/none/none_type.rs
package io.github.kotlinmania.starlark_kotlin.values.types.none

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

// use std::convert::Infallible;
// use std::hash::Hasher;

// use allocative::Allocative;
// use derive_more::Display;
// use dupe::Dupe;
// use serde::Serialize;
// use serde::Serializer;
// use starlark_derive::starlark_value;

// use crate::collections::StarlarkHashValue;
// use crate::collections::StarlarkHasher;
// use crate::typing::Ty;
// use crate::values::AllocFrozenValue;
// use crate::values::AllocStaticSimple;
// use crate::values::AllocValue;
// use crate::values::FrozenHeap;
// use crate::values::FrozenValue;
// use crate::values::Heap;
// use crate::values::StarlarkValue;
// use crate::values::UnpackValue;
// use crate::values::Value;

import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.collections.StarlarkHashValue
import io.github.kotlinmania.starlark_kotlin.collections.StarlarkHasher
import io.github.kotlinmania.starlark_kotlin.values.AllocFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.AllocValue
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.UnpackValue
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.AllocStaticSimple
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value

/// Define the None type, use [`NoneType`] in Rust.
// #[derive(Debug, Clone, Dupe, ProvidesStaticType, Display, Allocative)]
// #[display("None")]
// pub struct NoneType;
object NoneType : StarlarkValue, AllocValue, AllocFrozenValue, UnpackValue<NoneType> {
    // impl NoneType
    /// The result of `type(None)`.
    // pub const TYPE: &'static str = "NoneType";
    override val TYPE: String = "NoneType"
    override val HAS_eval_type: Boolean get() = true

    // #[display("None")]
    override fun toString(): String = "None"

    // #[starlark_value(type = NoneType::TYPE)]
    // impl<'v> StarlarkValue<'v> for NoneType {

    // fn is_special(_: Private) -> bool { true }
    override fun isSpecial(): Boolean = true

    // fn to_bool(&self) -> bool { false }
    override fun toBool(): Boolean = false

    // fn write_hash(&self, hasher: &mut StarlarkHasher) -> crate::Result<()>
    override fun writeHash(hasher: StarlarkHasher): Result<Unit> {
        // just took the result of hash(None) in macos python 2.7.10 interpreter.
        hasher.writeU64(9_223_380_832_852_120_682UL)
        return Result.success(Unit)
    }

    // fn get_hash(&self, _private: Private) -> crate::Result<StarlarkHashValue>
    override fun getHash(): Result<StarlarkHashValue> {
        // Just a random number.
        return Result.success(StarlarkHashValue.newUnchecked(0xf9c2263dU))
    }

    override fun starlarkTypeRepr(): Ty {
        return Ty.none()
    }

    // fn get_type_starlark_repr() -> Ty
    override fun getTypeStarlarkRepr(): Ty {
        return Ty.none()
    }

    // fn typechecker_ty(&self) -> Option<Ty>
    override fun typecheckerTy(): Ty? {
        return Ty.none()
    }

    // fn eval_type(&self) -> Option<Ty>
    override fun evalType(): Ty? {
        return Ty.none()
    }

    // impl<'v> AllocValue<'v> for NoneType
    // fn alloc_value(self, _heap: Heap<'v>) -> Value<'v> { Value::new_none() }
    override fun allocValue(_heap: Heap): Value {
        return Value.newNone()
    }

    // impl Serialize for NoneType {
    //     fn serialize<S>(&self, serializer: S) -> Result<S::Ok, S::Error> {
    //         serializer.serialize_none()
    //     }
    // }
    fun serialize(serializer: Any): Result<Unit> {
        // serializer.serialize_none()
        (serializer as kotlinx.serialization.encoding.Encoder).encodeNull()
        return Result.success(Unit)
    }

    // impl AllocFrozenValue for NoneType
    // fn alloc_frozen_value(self, _heap: &FrozenHeap) -> FrozenValue { FrozenValue::new_none() }
    override fun allocFrozenValue(_heap: FrozenHeap): FrozenValue {
        return FrozenValue.newNone()
    }

    // impl<'v> UnpackValue<'v> for NoneType {
    //     type Error = Infallible;
    //     fn unpack_value_impl(value: Value<'v>) -> Result<Option<Self>, Self::Error>
    override fun unpackValueImpl(value: Value): Result<NoneType?> {
        return if (value.isNone()) {
            Result.success(NoneType)
        } else {
            Result.success(null)
        }
    }
}

// pub(crate) static VALUE_NONE: AllocStaticSimple<NoneType> = AllocStaticSimple::alloc(NoneType);
internal val VALUE_NONE: AllocStaticSimple<NoneType> = AllocStaticSimple.alloc(NoneType)

fun getTypeStarlarkRepr(): Ty {
    return NoneType.getTypeStarlarkRepr()
}

fun unpackValueImpl(value: Value): Result<NoneType?> {
    return NoneType.unpackValueImpl(value)
}
