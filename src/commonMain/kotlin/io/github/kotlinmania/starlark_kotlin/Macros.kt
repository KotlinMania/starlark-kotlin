// port-lint: source src/macros.rs
package io.github.kotlinmania.starlark_kotlin

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

import io.github.kotlinmania.starlark_kotlin.values.AllocFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.AllocValue
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.UnpackValue
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import kotlin.reflect.KClass
import io.github.kotlinmania.starlark_kotlin.values.layout.ValueLike
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value

/**
 * Reduce boilerplate when making types instances of ComplexValue.
 *
 * In Rust, this macro generates AllocValue, AllocFrozenValue, UnpackValue,
 * StarlarkTypeRepr, and from_value implementations for complex value types
 * (types that can contain references to other Starlark values).
 *
 * In Kotlin, we use a registration function that wires up the same capabilities
 * at runtime via the type registry.
 */
// macro_rules! starlark_complex_value
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
 * Similar to starlark_complex_value but from_value returns Either<unfrozen, frozen>.
 *
 * In Rust, this macro generates AllocValue, AllocFrozenValue, and a from_value
 * that returns Either<&Self, &FrozenX> instead of coercing to unfrozen.
 */
// macro_rules! starlark_complex_values
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
 *
 * In Rust, this macro generates AllocValue, AllocFrozenValue, UnpackValue,
 * StarlarkTypeRepr, and from_value implementations for simple value types.
 *
 * In Kotlin, we use a registration function that wires up the same capabilities
 * at runtime via the type registry.
 */
// macro_rules! starlark_simple_value
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
// (corresponds to the compile-time macro expansions in Rust)
object ComplexValueRegistry {
    private val entries = mutableMapOf<KClass<*>, ComplexValueEntry<*>>()

    fun <T : StarlarkValue> register(
        unfrozenType: KClass<T>,
        frozenType: KClass<out StarlarkValue>,
        allocValue: (T, Heap) -> Value,
        allocFrozenValue: (Any, FrozenHeap) -> FrozenValue,
        fromValue: (Value) -> T?,
    ) {
        entries[unfrozenType] = ComplexValueEntry(
            unfrozenType = unfrozenType,
            frozenType = frozenType,
            allocValue = allocValue,
            allocFrozenValue = allocFrozenValue,
            fromValue = fromValue,
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : StarlarkValue> get(type: KClass<T>): ComplexValueEntry<T>? =
        entries[type] as? ComplexValueEntry<T>
}

data class ComplexValueEntry<T : StarlarkValue>(
    val unfrozenType: KClass<T>,
    val frozenType: KClass<out StarlarkValue>,
    val allocValue: (T, Heap) -> Value,
    val allocFrozenValue: (Any, FrozenHeap) -> FrozenValue,
    val fromValue: (Value) -> T?,
)

/** Registry for complex values (Either variant) type registrations. */
object ComplexValuesRegistry {
    private val entries = mutableMapOf<KClass<*>, ComplexValuesEntry<*, *>>()

    fun <T : StarlarkValue, F : StarlarkValue> register(
        unfrozenType: KClass<T>,
        frozenType: KClass<F>,
        allocValue: (T, Heap) -> Value,
        allocFrozenValue: (F, FrozenHeap) -> FrozenValue,
        fromValue: (Value) -> Either<T, F>?,
    ) {
        entries[unfrozenType] = ComplexValuesEntry(
            unfrozenType = unfrozenType,
            frozenType = frozenType,
            allocValue = allocValue,
            allocFrozenValue = allocFrozenValue,
            fromValue = fromValue,
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : StarlarkValue, F : StarlarkValue> get(type: KClass<T>): ComplexValuesEntry<T, F>? =
        entries[type] as? ComplexValuesEntry<T, F>
}

data class ComplexValuesEntry<T : StarlarkValue, F : StarlarkValue>(
    val unfrozenType: KClass<T>,
    val frozenType: KClass<F>,
    val allocValue: (T, Heap) -> Value,
    val allocFrozenValue: (F, FrozenHeap) -> FrozenValue,
    val fromValue: (Value) -> Either<T, F>?,
)

/** Registry for simple value type registrations. */
object SimpleValueRegistry {
    private val entries = mutableMapOf<KClass<*>, SimpleValueEntry<*>>()

    fun <T : StarlarkValue> register(
        type: KClass<T>,
        allocValue: (T, Heap) -> Value,
        allocFrozenValue: (T, FrozenHeap) -> FrozenValue,
        fromValue: (Value) -> T?,
    ) {
        entries[type] = SimpleValueEntry(
            type = type,
            allocValue = allocValue,
            allocFrozenValue = allocFrozenValue,
            fromValue = fromValue,
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : StarlarkValue> get(type: KClass<T>): SimpleValueEntry<T>? =
        entries[type] as? SimpleValueEntry<T>
}

data class SimpleValueEntry<T : StarlarkValue>(
    val type: KClass<T>,
    val allocValue: (T, Heap) -> Value,
    val allocFrozenValue: (T, FrozenHeap) -> FrozenValue,
    val fromValue: (Value) -> T?,
)

/** Either type used by starlark_complex_values for from_value return. */
// (corresponds to either::Either in Rust)
sealed class Either<out L, out R> {
    data class Left<out L>(val value: L) : Either<L, Nothing>()
    data class Right<out R>(val value: R) : Either<Nothing, R>()
}
