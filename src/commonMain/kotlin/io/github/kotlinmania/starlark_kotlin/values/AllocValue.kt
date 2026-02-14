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

/** This module defines utilities to easily create values as Starlark values. */

import io.github.kotlinmania.starlark_kotlin.values.type_repr.StarlarkTypeRepr

/**
 * Trait for things that can be created on a [Heap] producing a [Value].
 *
 * Note, this interface does not represent Starlark types.
 * For example, this interface could be implemented for `Char`,
 * but there's no Starlark type for `Char`, this interface
 * is implemented for `Char` to construct Starlark `str`.
 *
 * For types that implement [StarlarkValue][io.github.kotlinmania.starlark_kotlin.values.StarlarkValue] a typical implementation
 * will probably call either [Heap.allocSimple] or [Heap.allocComplex],
 * e.g.
 *
 * ```kotlin
 * class MySimpleValue : StarlarkValue, AllocValue {
 *     override fun allocValue(heap: Heap): Value {
 *         return heap.allocSimple(this)
 *     }
 * }
 * ```
 *
 * ## Derive
 *
 * `AllocValue` can be implemented for sealed classes, like this:
 *
 * ```kotlin
 * sealed class AllocIntOrStr : StarlarkTypeRepr, AllocValue {
 *     data class Int(val value: kotlin.Int) : AllocIntOrStr()
 *     data class Str(val value: String) : AllocIntOrStr()
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
fun FrozenValue.allocValue(@Suppress("UNUSED_PARAMETER") heap: Heap): Value {
    return toValue()
}

// impl AllocValue for Value
fun Value.allocValue(@Suppress("UNUSED_PARAMETER") heap: Heap): Value {
    return this
}

// impl<A: AllocValue, B: AllocValue> AllocValue for Either<A, B>
fun <A : AllocValue, B : AllocValue> allocValueEither(either: Any, heap: Heap): Value {
    return when (either) {
        is AllocValue -> either.allocValue(heap)
        else -> error("Expected AllocValue")
    }
}

// impl<A: AllocFrozenValue, B: AllocFrozenValue> AllocFrozenValue for Either<A, B>
fun <A : AllocFrozenValue, B : AllocFrozenValue> allocFrozenValueEither(either: Any, heap: FrozenHeap): FrozenValue {
    return when (either) {
        is AllocFrozenValue -> either.allocFrozenValue(heap)
        else -> error("Expected AllocFrozenValue")
    }
}

/**
 * Trait for things that can be allocated on a [FrozenHeap] producing a [FrozenValue].
 *
 * ## Derive
 *
 * `AllocFrozenValue` can be implemented for sealed classes, like this:
 *
 * ```kotlin
 * sealed class AllocIntOrStr : StarlarkTypeRepr, AllocFrozenValue {
 *     data class Int(val value: kotlin.Int) : AllocIntOrStr()
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
fun FrozenValue.allocFrozenValue(@Suppress("UNUSED_PARAMETER") heap: FrozenHeap): FrozenValue {
    return this
}
