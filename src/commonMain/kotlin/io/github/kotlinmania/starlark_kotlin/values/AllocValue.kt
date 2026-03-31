// port-lint: source src/values/alloc_value.rs
package io.github.kotlinmania.starlark_kotlin.values

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

import io.github.kotlinmania.starlark_kotlin.Either
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.StringValue

/**
 * This module defines utilities to easily create Rust values as Starlark values.
 */

/**
 * Trait for things that can be created on a [Heap] producing a [Value].
 *
 * Note, this trait does not represent Starlark types.
 * For example, this trait is implemented for `Char`, but there's no Starlark type for `Char`;
 * this trait is implemented for `Char` to construct Starlark `str`.
 *
 * For types that implement [StarlarkValue], a typical implementation will probably call either
 * [Heap.allocSimple] or [Heap.allocComplex], for example:
 *
 * ```kotlin
 * // #[derive(Debug, Display, NoSerialize, ProvidesStaticType)]
 * // struct MySimpleValue;
 * //
 * // #[starlark_value(type = "MySimpleValue", UnpackValue, StarlarkTypeRepr)]
 * // impl<'v> StarlarkValue<'v> for MySimpleValue {}
 *
 * class MySimpleValue : StarlarkValue, AllocValue {
 *     override val TYPE: String get() = "MySimpleValue"
 *
 *     override fun allocValue(heap: Heap): Value {
 *         return heap.allocSimple(this)
 *     }
 * }
 * ```
 *
 * # Derive
 *
 * `AllocValue` can be derived for enums, like this:
 *
 * ```kotlin
 * // #[derive(StarlarkTypeRepr, AllocValue)]
 * // enum AllocIntOrStr {
 * //     Int(i32),
 * //     Str(String),
 * // }
 *
 * sealed class AllocIntOrStr : StarlarkTypeRepr, AllocValue {
 *     data class Int(val value: kotlin.Int) : AllocIntOrStr()
 *     data class Str(val value: kotlin.String) : AllocIntOrStr()
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

// impl AllocValue for FrozenValue
fun FrozenValue.allocValue(_heap: Heap): Value = toValue()

// impl AllocValue for Value
fun Value.allocValue(_heap: Heap): Value = this

// impl<A: AllocValue, B: AllocValue> AllocValue for Either<A, B>
inline fun <A : AllocValue, B : AllocValue> Either<A, B>.allocValue(heap: Heap): Value =
    when (this) {
        is Either.Left -> value.allocValue(heap)
        is Either.Right -> value.allocValue(heap)
    }

/**
 * Trait for things that can be allocated on a [FrozenHeap] producing a [FrozenValue].
 *
 * ## Derive
 *
 * `AllocFrozenValue` can be derived for enums, like this:
 *
 * ```kotlin
 * sealed class AllocIntOrStr : StarlarkTypeRepr, AllocFrozenValue {
 *     data class Int(val value: Int) : AllocIntOrStr()
 *     data class Str(val value: String) : AllocIntOrStr()
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

// impl AllocFrozenValue for FrozenValue
fun FrozenValue.allocFrozenValue(_heap: FrozenHeap): FrozenValue = this

// impl<A: AllocFrozenValue, B: AllocFrozenValue> AllocFrozenValue for Either<A, B>
inline fun <A : AllocFrozenValue, B : AllocFrozenValue> Either<A, B>.allocFrozenValue(heap: FrozenHeap): FrozenValue =
    when (this) {
        is Either.Left -> value.allocFrozenValue(heap)
        is Either.Right -> value.allocFrozenValue(heap)
    }

// --- Rust name parity aliases (for AST distance + line-by-line ports) ---
// Rust methods use snake_case like `alloc_value`/`alloc_frozen_value`.
// Kotlin uses camelCase, so we provide thin wrappers for parity.
@Suppress("FunctionName")
fun AllocValue.alloc_value(heap: Heap): Value = allocValue(heap)

@Suppress("FunctionName")
fun AllocStringValue.alloc_string_value(heap: Heap): StringValue = allocStringValue(heap)

@Suppress("FunctionName")
fun FrozenValue.alloc_value(heap: Heap): Value = allocValue(heap)

@Suppress("FunctionName")
fun Value.alloc_value(heap: Heap): Value = allocValue(heap)

@Suppress("FunctionName")
inline fun <A : AllocValue, B : AllocValue> Either<A, B>.alloc_value(heap: Heap): Value = allocValue(heap)

@Suppress("FunctionName")
fun AllocFrozenValue.alloc_frozen_value(heap: FrozenHeap): FrozenValue = allocFrozenValue(heap)

@Suppress("FunctionName")
fun AllocFrozenStringValue.alloc_frozen_string_value(heap: FrozenHeap): FrozenStringValue = allocFrozenStringValue(heap)

@Suppress("FunctionName")
fun FrozenValue.alloc_frozen_value(heap: FrozenHeap): FrozenValue = allocFrozenValue(heap)

@Suppress("FunctionName")
inline fun <A : AllocFrozenValue, B : AllocFrozenValue> Either<A, B>.alloc_frozen_value(heap: FrozenHeap): FrozenValue =
    allocFrozenValue(heap)
