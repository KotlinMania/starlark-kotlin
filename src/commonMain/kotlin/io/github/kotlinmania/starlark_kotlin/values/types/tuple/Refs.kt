// port-lint: source src/values/types/tuple/refs.rs
package io.github.kotlinmania.starlark_kotlin.values.types.tuple

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

// use std::convert::Infallible;
// use std::iter;
// use std::slice;

// use ref_cast::RefCastCustom;
// use ref_cast::ref_cast_custom;

// use crate::typing::Ty;
// use crate::values::FrozenValue;
// use crate::values::UnpackValue;
// use crate::values::Value;
// use crate::values::ValueLike;
// use crate::values::tuple::UnpackTuple;
// use crate::values::type_repr::StarlarkTypeRepr;
// use crate::values::types::tuple::value::FrozenTuple;
// use crate::values::types::tuple::value::Tuple;

import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.layout.Value

/// Reference to tuple data in Starlark heap.
// #[derive(RefCastCustom, Debug)]
// #[repr(transparent)]
// pub struct TupleRef<'v> {
//     contents: [Value<'v>],
// }
class TupleRef(
    private val contents: List<Value>,
) {
    // impl<'v> TupleRef<'v>

    /// `type(())`, which is `"tuple"`.
    // pub const TYPE: &'static str = FrozenTupleRef::TYPE;

    /// Number of elements.
    // pub fn len(&self) -> usize
    fun len(): Int = contents.size

    /// Tuple elements.
    // pub fn content(&self) -> &[Value<'v>]
    fun content(): List<Value> = contents

    /// Iterate over the contents.
    // pub fn iter<'a>(&'a self) -> iter::Copied<slice::Iter<'a, Value<'v>>>
    fun iter(): Iterator<Value> = contents.iterator()

    companion object {
        const val TYPE: String = TupleGen.TYPE

        // #[ref_cast_custom]
        // fn new(slice: &'v [Value<'v>]) -> &'v TupleRef<'v>;
        private fun new(slice: List<Value>): TupleRef = TupleRef(slice)

        /// Downcast a value to a tuple.
        // pub fn from_value(value: Value<'v>) -> Option<&'v TupleRef<'v>>
        fun fromValue(value: Value): TupleRef? {
            val tuple = Tuple.fromValue(value) ?: return null
            return new(tuple.content())
        }

        /// Downcast a value to a tuple.
        // pub fn from_frozen_value(value: FrozenValue) -> Option<&'v TupleRef<'v>>
        fun fromFrozenValue(value: FrozenValue): TupleRef? {
            return fromValue(value.toValue())
        }

        // impl<'v> StarlarkTypeRepr for &'v TupleRef<'v>
        //     fn starlark_type_repr() -> Ty
        fun starlarkTypeRepr(): Ty = Ty.anyTuple()
    }
}

/// Reference to tuple data in frozen Starlark heap.
// #[repr(transparent)]
// #[derive(RefCastCustom, Debug)]
// pub struct FrozenTupleRef {
//     contents: [FrozenValue],
// }
class FrozenTupleRef(
    private val contents: List<FrozenValue>,
) {
    // impl FrozenTupleRef

    /// Number of elements.
    // pub fn len(&self) -> usize
    fun len(): Int = contents.size

    /// Tuple elements.
    // pub fn content(&self) -> &[FrozenValue]
    fun content(): List<FrozenValue> = contents

    /// Iterate over contents.
    // pub fn iter(&self) -> impl ExactSizeIterator<Item = FrozenValue> + '_
    fun iter(): Iterator<FrozenValue> = contents.iterator()

    companion object {
        /// `type(())`, which is `"tuple"`.
        // pub const TYPE: &'static str = FrozenTuple::TYPE;
        const val TYPE: String = TupleGen.TYPE

        // #[ref_cast_custom]
        // fn new(slice: &'static [FrozenValue]) -> &'static FrozenTupleRef;
        private fun new(slice: List<FrozenValue>): FrozenTupleRef = FrozenTupleRef(slice)

        /// Downcast a value to a tuple.
        // pub fn from_frozen_value(value: FrozenValue) -> Option<&'static FrozenTupleRef>
        fun fromFrozenValue(value: FrozenValue): FrozenTupleRef? {
            val tuple = value.downcastRef<FrozenTuple>() ?: return null
            return new(tuple.content())
        }

        // impl<'a> StarlarkTypeRepr for &'a FrozenTupleRef
        //     fn starlark_type_repr() -> Ty
        fun starlarkTypeRepr(): Ty = Ty.anyTuple()
    }
}

// impl<'v> UnpackValue<'v> for &'v TupleRef<'v> {
//     type Error = Infallible;
//     fn unpack_value_impl(value: Value<'v>) -> Result<Option<Self>, Self::Error>
fun unpackTupleRef(value: Value): TupleRef? {
    return TupleRef.fromValue(value)
}

// impl<'v> UnpackValue<'v> for &'v FrozenTupleRef {
//     type Error = crate::Error;
//     fn unpack_value_impl(value: Value<'v>) -> crate::Result<Option<Self>>
fun unpackFrozenTupleRef(value: Value): FrozenTupleRef? {
    val frozen = value.unpackFrozen() ?: return null
    return FrozenTupleRef.fromFrozenValue(frozen)
}
