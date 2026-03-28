// port-lint: source src/values.rs
package io.github.kotlinmania.starlark_kotlin
import io.github.kotlinmania.starlark_kotlin.values.demand.Demand
import io.github.kotlinmania.starlark_kotlin.values.freeze_error.FreezeError
import io.github.kotlinmania.starlark_kotlin.values.freeze_error.FreezeResult
import io.github.kotlinmania.starlark_kotlin.values.owned_frozen_ref.OwnedFrozenRef
import io.github.kotlinmania.starlark_kotlin.values.owned.OwnedFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.value_of.ValueOf

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

/**
 * Defines a runtime Starlark value ([Value]) and traits for defining custom values ([StarlarkValue]).
 *
 * This module contains code for working with Starlark values:
 *
 * * Most code dealing with Starlark will use [Value], as it represents the fundamental values used in
 *   Starlark. When frozen, they become [FrozenValue].
 * * Values are garbage-collected, so a given [Value] lives on a [Heap].
 * * Kotlin values (e.g. [String], [List]) can be added to the [Heap] with [AllocValue],
 *   and deconstructed from a [Value] with [UnpackValue]
 *   (or specialised methods like [Value.unpackStr]).
 * * To define your own data type that can live in a [Value] it must implement the [StarlarkValue]
 *   interface.
 * * All the nested modules represent the built-in Starlark values. These are all defined using
 *   [StarlarkValue], so may serve as interesting inspiration for writing your own values, in
 *   addition to occurring in Starlark programs.
 */

// Re-exports from derive macros equivalent
// In Kotlin, these are interfaces/annotations defined in the values package

// Re-exports from values subpackages (mirrors Rust's extensive pub use block)
internal typealias OwnedFrozenRefExport = io.github.kotlinmania.starlark_kotlin.values.OwnedFrozenRef
internal typealias AllocValueExport = io.github.kotlinmania.starlark_kotlin.values.AllocValue
internal typealias DemandExport = io.github.kotlinmania.starlark_kotlin.values.Demand
internal typealias ValueErrorExport = io.github.kotlinmania.starlark_kotlin.values.ValueError
internal typealias FreezeExport = io.github.kotlinmania.starlark_kotlin.values.Freeze
internal typealias FreezeErrorExport = io.github.kotlinmania.starlark_kotlin.values.FreezeError
internal typealias FreezeResultExport<T> = io.github.kotlinmania.starlark_kotlin.values.FreezeResult<T>
internal typealias FrozenRefExport = io.github.kotlinmania.starlark_kotlin.values.FrozenRef
internal typealias StarlarkIteratorExport = io.github.kotlinmania.starlark_kotlin.values.StarlarkIterator
internal typealias FreezerExport = io.github.kotlinmania.starlark_kotlin.values.layout.Freezer
internal typealias FrozenHeapExport = io.github.kotlinmania.starlark_kotlin.values.layout.heap.FrozenHeap
internal typealias HeapExport = io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
internal typealias TracerExport = io.github.kotlinmania.starlark_kotlin.values.layout.heap.Tracer
internal typealias ValueIdentityExport = io.github.kotlinmania.starlark_kotlin.values.layout.ValueIdentity
internal typealias FrozenValueTypedExport = io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValueTyped
internal typealias ValueTypedExport = io.github.kotlinmania.starlark_kotlin.values.layout.ValueTyped
internal typealias FrozenValueExport = io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValue
internal typealias ValueExport = io.github.kotlinmania.starlark_kotlin.values.layout.Value
internal typealias ValueLikeExport = io.github.kotlinmania.starlark_kotlin.values.layout.ValueLike
internal typealias ValueLifetimelessExport = io.github.kotlinmania.starlark_kotlin.values.layout.ValueLifetimeless
internal typealias OwnedFrozenValueExport = io.github.kotlinmania.starlark_kotlin.values.OwnedFrozenValue
internal typealias TraceExport = io.github.kotlinmania.starlark_kotlin.values.Trace
internal typealias ComplexValueExport = io.github.kotlinmania.starlark_kotlin.values.ComplexValue
internal typealias StarlarkValueExport = io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
internal typealias UnpackValueExport = io.github.kotlinmania.starlark_kotlin.values.UnpackValue
internal typealias ValueOfExport = io.github.kotlinmania.starlark_kotlin.values.ValueOf
internal typealias ValueOfUncheckedExport = io.github.kotlinmania.starlark_kotlin.values.ValueOfUnchecked

// Submodules (in Kotlin, these are separate packages under values/)
// alloc_value, comparison, demand, error, freeze, freeze_error,
// frozen_ref, index, iter, layout, owned, owned_frozen_ref,
// recursive_repr_or_json_guard, stack_guard, starlark_type_id,
// thin_box_slice_frozen_value, trace, traits, type_repr, types,
// typing, unpack, unpack_and_discard, value_of, value_of_unchecked
