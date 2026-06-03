// port-lint: source src/values/alloc_value.rs
package io.github.kotlinmania.starlark.values

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

import io.github.kotlinmania.starlark.Either
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark.values.layout.typed.StringValue

/**
 * This mod defines utilities to easily create Rust values as Starlark values.
 */

/**
 * Trait for things that can be created on a [Heap] producing a [Value].
 *
 * Note, this trait does not represent Starlark types.
 * For example, this trait is implemented for `char`,
 * but there's no Starlark type for `char`, this trait
 * is implemented for `char` to construct Starlark `str`.
 *
 * For types that implement [`crate::values::StarlarkValue`] a typical implementation
 * will probably call either [`Heap::alloc_simple`] or [`Heap::alloc_complex`],
 * e.g.
 *
 * ```kotlin
 * # use allocative::Allocative;
 * # use starlark::any::ProvidesStaticType;
 * # use starlark::values::{AllocValue, Heap, NoSerialize, starlark_value, StarlarkValue, Value};
 *
 * #[derive(Debug, derive_more::Display, Allocative, NoSerialize, ProvidesStaticType)]
 * struct MySimpleValue;
 *
 * #[starlark_value(type = "MySimpleValue", UnpackValue, StarlarkTypeRepr)]
 * impl<'v> StarlarkValue<'v> for MySimpleValue {}
 *
 * impl<'v> AllocValue<'v> for MySimpleValue {
 *     fn alloc_value(self, heap: Heap<'v>) -> Value<'v> {
 *         heap.alloc_simple(self)
 *     }
 * }
 * ```
 *
 * # Derive
 *
 * `AllocValue` can be derived for enums, like this:
 *
 * ```kotlin
 * use starlark::values::AllocValue;
 * use starlark::values::type_repr::StarlarkTypeRepr;
 *
 * #[derive(StarlarkTypeRepr, AllocValue)]
 * enum AllocIntOrStr {
 *     Int(i32),
 *     Str(String),
 * }
 * ```
 */
interface AllocValue : StarlarkTypeRepr {
    /**
     * Allocate the value on a heap and return a reference to the allocated value.
     *
     * Note, for certain values (e.g. empty strings) no allocation is actually performed,
     * and a reference to the statically allocated object is returned.
     */
    fun allocValue(heap: Heap): Value
}

/** Type which allocates a string. */
interface AllocStringValue : AllocValue {
    /** Allocate a string. */
    fun allocStringValue(heap: Heap): StringValue
}

internal fun FrozenValue.allocValue(heap: Heap): Value = this.toValue()

internal fun Value.allocValue(heap: Heap): Value = this

internal fun <A : AllocValue, B : AllocValue> Either<A, B>.allocValue(heap: Heap): Value =
    when (this) {
        is Either.Left -> {
            val a = value
            a.allocValue(heap)
        }

        is Either.Right -> {
            val b = value
            b.allocValue(heap)
        }
    }

internal fun <A : AllocFrozenValue, B : AllocFrozenValue> Either<A, B>.allocFrozenValue(heap: FrozenHeap): FrozenValue =
    when (this) {
        is Either.Left -> {
            val a = value
            a.allocFrozenValue(heap)
        }

        is Either.Right -> {
            val b = value
            b.allocFrozenValue(heap)
        }
    }

/**
 * Trait for things that can be allocated on a [FrozenHeap] producing a [FrozenValue].
 *
 * # Derive
 *
 * `AllocFrozenValue` can be derived for enums, like this:
 *
 * ```kotlin
 * use starlark::values::AllocFrozenValue;
 * use starlark::values::type_repr::StarlarkTypeRepr;
 *
 * #[derive(StarlarkTypeRepr, AllocFrozenValue)]
 * enum AllocIntOrStr {
 *     Int(i32),
 *     Str(String),
 * }
 * ```
 */
interface AllocFrozenValue : StarlarkTypeRepr {
    /** Allocate a value in the frozen heap and return a reference to the allocated value. */
    fun allocFrozenValue(heap: FrozenHeap): FrozenValue
}

/** Type which allocates a string. */
interface AllocFrozenStringValue : AllocFrozenValue {
    /** Allocate a string. */
    fun allocFrozenStringValue(heap: FrozenHeap): FrozenStringValue
}

internal fun FrozenValue.allocFrozenValue(heap: FrozenHeap): FrozenValue = this
