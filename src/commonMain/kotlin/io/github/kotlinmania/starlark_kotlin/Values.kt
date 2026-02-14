// port-lint: source src/values.rs
package io.github.kotlinmania.starlark_kotlin

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

//! Defines a runtime Starlark value ([Value]) and traits for defining custom values ([StarlarkValue]).
//!
//! This module contains code for working with Starlark values:
//!
//! * Most code dealing with Starlark will use [Value], as it represents the fundamental values used in
//!   Starlark. When frozen, they become [FrozenValue].
//! * Values are garbage-collected, so a given [Value] lives on a [Heap].
//! * Rust values (e.g. String, Vec) can be added to the [Heap] with [AllocValue],
//!   and deconstructed from a [Value] with [UnpackValue]
//!   (or specialised methods like [Value.unpackStr]).
//! * To define your own data type that can live in a [Value] it must implement the [StarlarkValue]
//!   trait.
//! * All the nested modules represent the built-in Starlark values.

// Re-exports from values subpackages.
// In Kotlin, these types are accessed directly from their defining packages:
//
// pub use owned_frozen_ref::OwnedFrozenRef
// pub use owned_frozen_ref::OwnedRefFrozenRef
// pub use alloc_value::AllocFrozenValue
// pub use alloc_value::AllocValue
// pub use demand::Demand
// pub use error::ValueError
// pub use freeze::Freeze
// pub use freeze_error::FreezeError
// pub use freeze_error::FreezeErrorContext
// pub use freeze_error::FreezeResult
// pub use frozen_ref::FrozenRef
// pub use iter::StarlarkIterator
// pub use layout::avalues::static_::AllocStaticSimple
// pub use layout::complex::ValueTypedComplex
// pub use layout::freezer::Freezer
// pub use layout::heap::heap_type::FrozenHeap
// pub use layout::heap::heap_type::FrozenHeapRef
// pub use layout::heap::heap_type::Heap
// pub use layout::heap::heap_type::Tracer
// pub use layout::heap::send::DynStarlark
// pub use layout::heap::send::HeapSendable
// pub use layout::identity::ValueIdentity
// pub use layout::static_string::StarlarkStrNRepr
// pub use layout::static_string::constant_string
// pub use layout::typed::FrozenValueTyped
// pub use layout::typed::ValueTyped
// pub use layout::typed::string::FrozenStringValue
// pub use layout::typed::string::StringValue
// pub use layout::typed::string::StringValueLike
// pub use layout::value::FrozenValue
// pub use layout::value::Value
// pub use layout::value::ValueLike
// pub use layout::value_lifetimeless::ValueLifetimeless
// pub use owned::OwnedFrozenValue
// pub use owned::OwnedFrozenValueTyped
// pub use thin_box_slice_frozen_value::packed_impl::ThinBoxSliceFrozenValue
// pub use trace::Trace
// pub use traits::ComplexValue
// pub use traits::StarlarkValue
// pub use unpack::UnpackValue
// pub use unpack::UnpackValueError
// pub use unpack::UnpackValueErrorInfallible
// pub use unpack_and_discard::UnpackAndDiscard
// pub use value_of::ValueOf
// pub use value_of_unchecked::FrozenValueOfUnchecked
// pub use value_of_unchecked::ValueOfUnchecked
// pub use value_of_unchecked::ValueOfUncheckedGeneric
//
// Submodules:
// mod alloc_value
// mod comparison
// pub(crate) mod demand
// pub(crate) mod error
// mod freeze
// mod freeze_error
// pub(crate) mod frozen_ref
// mod index
// pub(crate) mod iter
// pub(crate) mod layout
// mod owned
// pub(crate) mod owned_frozen_ref
// pub(crate) mod recursive_repr_or_json_guard
// mod stack_guard
// pub(crate) mod starlark_type_id
// pub(crate) mod thin_box_slice_frozen_value
// mod trace
// pub(crate) mod traits
// pub mod type_repr
// pub(crate) mod types
// pub mod typing
// mod unpack
// mod unpack_and_discard
// pub(crate) mod value_of
// pub(crate) mod value_of_unchecked
