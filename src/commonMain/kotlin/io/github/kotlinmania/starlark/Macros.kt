// port-lint: source src/macros.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)
package io.github.kotlinmania.starlark

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

import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import kotlin.native.HiddenFromObjC
import kotlin.reflect.KClass

/**
 * Reduce boilerplate when making types instances of ComplexValue.
 */
@HiddenFromObjC
fun <T : StarlarkValue> starlarkComplexValue(
    unfrozenType: KClass<T>,
    frozenType: KClass<out StarlarkValue>,
    allocValue: (T, Heap) -> Value,
    allocFrozenValue: (Any, FrozenHeap) -> FrozenValue,
    fromValue: (Value) -> T?,
) {
    ComplexValueRegistry.register(
        unfrozenType = unfrozenType,
        frozenType = frozenType,
        allocValue = allocValue,
        allocFrozenValue = allocFrozenValue,
        fromValue = fromValue,
    )
}

/**
 * Similar to [starlarkComplexValue], but [fromValue] can return either the
 * unfrozen value or its frozen counterpart.
 */
@HiddenFromObjC
fun <T : StarlarkValue, F : StarlarkValue> starlarkComplexValues(
    unfrozenType: KClass<T>,
    frozenType: KClass<F>,
    allocValue: (T, Heap) -> Value,
    allocFrozenValue: (F, FrozenHeap) -> FrozenValue,
    fromValue: (Value) -> Either<T, F>?,
) {
    ComplexValuesRegistry.register(
        unfrozenType = unfrozenType,
        frozenType = frozenType,
        allocValue = allocValue,
        allocFrozenValue = allocFrozenValue,
        fromValue = fromValue,
    )
}

/**
 * A macro reducing boilerplate defining Starlark values which are simple - they
 * aren't mutable and can't contain references to other Starlark values.
 */
@HiddenFromObjC
fun <T : StarlarkValue> starlarkSimpleValue(
    type: KClass<T>,
    allocValue: (T, Heap) -> Value,
    allocFrozenValue: (T, FrozenHeap) -> FrozenValue,
    fromValue: (Value) -> T?,
) {
    SimpleValueRegistry.register(
        type = type,
        allocValue = allocValue,
        allocFrozenValue = allocFrozenValue,
        fromValue = fromValue,
    )
}

/** Registry for complex value type registrations. */
@HiddenFromObjC
object ComplexValueRegistry {
    private val entries = mutableMapOf<KClass<*>, ComplexValueEntry<*>>()

    fun <T : StarlarkValue> register(
        unfrozenType: KClass<T>,
        frozenType: KClass<out StarlarkValue>,
        allocValue: (T, Heap) -> Value,
        allocFrozenValue: (Any, FrozenHeap) -> FrozenValue,
        fromValue: (Value) -> T?,
    ) {
        entries[unfrozenType] =
            ComplexValueEntry(
                unfrozenType = unfrozenType,
                frozenType = frozenType,
                allocValue = allocValue,
                allocFrozenValue = allocFrozenValue,
                fromValue = fromValue,
            )
    }

    fun get(type: KClass<out StarlarkValue>): ComplexValueEntry<*>? = entries[type]
}

@HiddenFromObjC
data class ComplexValueEntry<T : StarlarkValue>(
    val unfrozenType: KClass<T>,
    val frozenType: KClass<out StarlarkValue>,
    val allocValue: (T, Heap) -> Value,
    val allocFrozenValue: (Any, FrozenHeap) -> FrozenValue,
    val fromValue: (Value) -> T?,
)

/** Registry for complex values (Either variant) type registrations. */
@HiddenFromObjC
object ComplexValuesRegistry {
    private val entries = mutableMapOf<KClass<*>, ComplexValuesEntry<*, *>>()

    fun <T : StarlarkValue, F : StarlarkValue> register(
        unfrozenType: KClass<T>,
        frozenType: KClass<F>,
        allocValue: (T, Heap) -> Value,
        allocFrozenValue: (F, FrozenHeap) -> FrozenValue,
        fromValue: (Value) -> Either<T, F>?,
    ) {
        entries[unfrozenType] =
            ComplexValuesEntry(
                unfrozenType = unfrozenType,
                frozenType = frozenType,
                allocValue = allocValue,
                allocFrozenValue = allocFrozenValue,
                fromValue = fromValue,
            )
    }

    fun get(type: KClass<out StarlarkValue>): ComplexValuesEntry<*, *>? = entries[type]
}

@HiddenFromObjC
data class ComplexValuesEntry<T : StarlarkValue, F : StarlarkValue>(
    val unfrozenType: KClass<T>,
    val frozenType: KClass<F>,
    val allocValue: (T, Heap) -> Value,
    val allocFrozenValue: (F, FrozenHeap) -> FrozenValue,
    val fromValue: (Value) -> Either<T, F>?,
)

/** Registry for simple value type registrations. */
@HiddenFromObjC
object SimpleValueRegistry {
    private val entries = mutableMapOf<KClass<*>, SimpleValueEntry<*>>()

    fun <T : StarlarkValue> register(
        type: KClass<T>,
        allocValue: (T, Heap) -> Value,
        allocFrozenValue: (T, FrozenHeap) -> FrozenValue,
        fromValue: (Value) -> T?,
    ) {
        entries[type] =
            SimpleValueEntry(
                type = type,
                allocValue = allocValue,
                allocFrozenValue = allocFrozenValue,
                fromValue = fromValue,
            )
    }

    fun get(type: KClass<out StarlarkValue>): SimpleValueEntry<*>? = entries[type]
}

@HiddenFromObjC
data class SimpleValueEntry<T : StarlarkValue>(
    val type: KClass<T>,
    val allocValue: (T, Heap) -> Value,
    val allocFrozenValue: (T, FrozenHeap) -> FrozenValue,
    val fromValue: (Value) -> T?,
)

/** Either type used by [starlarkComplexValues] for [fromValue] results. */
@HiddenFromObjC
sealed class Either<out L, out R> {
    data class Left<out L>(
        val value: L,
    ) : Either<L, Nothing>()

    data class Right<out R>(
        val value: R,
    ) : Either<Nothing, R>()
}
