// port-lint: source src/values/types/dict/refs.rs
package io.github.kotlinmania.starlark_kotlin.values.types.dict

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

// use std::cell::Ref;
// use std::cell::RefCell;
// use std::cell::RefMut;
// use std::convert::Infallible;
// use std::ops::Deref;

// use dupe::Dupe;
// use either::Either;

// use crate::coerce::coerce;
// use crate::typing::Ty;
// use crate::values::FrozenValue;
// use crate::values::UnpackValue;
// use crate::values::Value;
// use crate::values::ValueError;
// use crate::values::ValueLike;
// use crate::values::dict::Dict;
// use crate::values::dict::value::DictGen;
// use crate::values::dict::value::FrozenDictData;
// use crate::values::type_repr::StarlarkTypeRepr;
// use crate::values::types::dict::dict_type::DictType;

import starlark_map.small_map.SmallMap
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.UnpackValue
import io.github.kotlinmania.starlark_kotlin.values.ValueError
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.StarlarkTypeRepr

sealed class Either<out L, out R> {
    data class Left<out L>(val value: L) : Either<L, Nothing>()
    data class Right<out R>(val value: R) : Either<Nothing, R>()
}

/// Borrowed `Dict`.
// pub struct DictRef<'v> {
//     pub(crate) aref: Either<Ref<'v, Dict<'v>>, &'v Dict<'v>>,
// }
class DictRef internal constructor(
    internal val aref: Either<Ref<Dict>, Dict>
)

// impl<'v> Clone for DictRef<'v>
//     fn clone(&self) -> Self
fun DictRef.clone(): DictRef = when (val ref = this.aref) {
    is Either.Left -> DictRef(Either.Left(ref.value.clone()))
    is Either.Right -> DictRef(Either.Right(ref.value))
}

// impl<'v> DictRef<'v>
/// Downcast the value to a dict.
// pub fn from_value(x: Value<'v>) -> Option<DictRef<'v>>
fun dictRefFromValue(x: Value): DictRef? =
    if (x.unpackFrozen() != null) {
        x.downcastRef<DictGen<FrozenDictData>>()
            ?.let { DictRef(Either.Right(coerce(it.inner))) }
    } else {
        val ptr = x.downcastRef<DictGen<AtomicRef<Dict>>>() ?: return null
        DictRef(Either.Left(ptr.inner.borrow()))
    }

// impl<'v> Deref for DictRef<'v> {
//     type Target = Dict<'v>;
//     fn deref(&self) -> &Self::Target
// }
operator fun DictRef.getValue(thisRef: Any?, property: Any?): Dict = when (val ref = aref) {
    is Either.Left -> ref.value.value
    is Either.Right -> ref.value
}

/** Iterate over key/value pairs, mirroring Rust's `Deref<Target = Dict>` on DictRef. */
fun DictRef.iter(): Sequence<Pair<Value, Value>> = when (val ref = aref) {
    is Either.Left -> ref.value.value.iter()
    is Either.Right -> ref.value.iter()
}

/// Mutably borrowed `Dict`.
// pub struct DictMut<'v> {
//     pub aref: RefMut<'v, Dict<'v>>,
// }
class DictMut(
    /// Mutable reference to the dict
    val aref: RefMut<Dict>
)

// impl<'v> DictMut<'v>
/// Downcast the value to a mutable dict reference.
// #[inline]
// pub fn from_value(x: Value<'v>) -> anyhow::Result<DictMut<'v>>
fun dictMutFromValue(x: Value): Result<DictMut> {
    class NotDictError(typeName: String) : Exception("Value is not dict, value type: `$typeName`")

    fun error(x: Value): Throwable =
        if (x.downcastRef<DictGen<FrozenDictData>>() != null) ValueError.CannotMutateImmutableValue
        else NotDictError(x.getType())

    val ptr = x.downcastRef<DictGen<AtomicRef<Dict>>>() ?: return Result.failure(error(x))
    return when (val borrowed = ptr.inner.tryBorrowMut()) {
        null -> Result.failure(ValueError.MutationDuringIteration)
        else -> Result.success(DictMut(borrowed))
    }
}

/// Reference to frozen `Dict`.
// pub struct FrozenDictRef {
//     dict: &'static FrozenDictData,
// }
class FrozenDictRef internal constructor(
    private val dict: FrozenDictData
) {
    // impl FrozenDictRef
    companion object {
        /// Downcast to frozen dict.
        // pub fn from_frozen_value(x: FrozenValue) -> Option<FrozenDictRef>
        fun fromFrozenValue(x: FrozenValue): FrozenDictRef? =
            x.downcastRef<DictGen<FrozenDictData>>()?.let { FrozenDictRef(it.inner) }
    }

    /// Get value by a string key.
    // pub fn get_str(&self, key: &str) -> Option<FrozenValue>
    fun getStr(key: String): FrozenValue? = dict.getStr(key)

    /// Iterate over dict entries.
    // pub fn iter(&self) -> impl ExactSizeIterator<Item = (FrozenValue, FrozenValue)> + use<>
    fun iter(): Sequence<Pair<FrozenValue, FrozenValue>> = dict.iter()
}

// impl<'v> StarlarkTypeRepr for DictRef<'v>
//     fn starlark_type_repr() -> Ty
object DictRefStarlarkTypeRepr : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty =
        Ty.dict(Ty.any(), Ty.any())
}

// impl<'v> UnpackValue<'v> for DictRef<'v> {
//     type Error = Infallible;
//     fn unpack_value_impl(value: Value<'v>) -> Result<Option<DictRef<'v>>, Infallible>
object DictRefUnpackValue : UnpackValue<DictRef> {
    override fun starlarkTypeRepr(): Ty = DictRefStarlarkTypeRepr.starlarkTypeRepr()

    override fun unpackValueImpl(value: Value): Result<DictRef?> =
        Result.success(dictRefFromValue(value))
}

class Ref<T>(val value: T) {
    fun clone(): Ref<T> = Ref(value)
}

class RefMut<T>(val value: T)

@Suppress("UNCHECKED_CAST")
private fun coerce(data: FrozenDictData): Dict =
    Dict(data.content as SmallMap<Value, Value>)
