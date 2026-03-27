// port-lint: source src/values/types/any.rs
package io.github.kotlinmania.starlark_kotlin.values.types

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

/// A type [StarlarkAny] which can cheaply wrap any value into a [Value].
///
/// This is intended to be a low cost way to quickly wrap types without much boilerplate.
/// For more advanced uses you should define an instance of [StarlarkValue].
///
/// To use this type, usually you will return a [StarlarkAny] from a module function,
/// and consume it in another.

// Placeholder types referenced from other modules
// These will be replaced with real imports as the port progresses
// TODO: stub - Value needs real import
open class Value {
    fun toValue(): Value = this
    fun <T> downcastRef(): T? = null
}
// TODO: stub - FrozenValue needs real import
class FrozenValue : Value()
// TODO: stub - Heap needs real import
class Heap {
    fun <T> allocSimple(value: T): Value = Value()
}
class FrozenHeap {
    fun <T> allocSimpleTypedStatic(value: T): FrozenRef<T> = FrozenRef(value)
}
class FrozenRef<T>(val value: T) {
    fun asFrozenRef(): FrozenRef<T> = this
    fun <R> map(transform: (T) -> R): FrozenRef<R> = FrozenRef(transform(value))
}

/// A type that can be passed around as a Starlark [Value], but in most
/// ways is uninteresting/opaque to Starlark. Constructed with
/// [new] and decomposed with [get].
///
/// This is version for "simple" values (not requiring trace during GC).
/// For "complex" version check [StarlarkAnyComplex].
class StarlarkAny<T>(
    val inner: T,
) {
    companion object {
        const val TYPE: String = "any"

        /// Create a new [StarlarkAny] value. Such a value can be allocated on a heap with
        /// `heap.alloc(StarlarkAny.new(x))`.
        fun <T> new(x: T): StarlarkAny<T> {
            return StarlarkAny(x)
        }

        /// Extract from a [Value] that contains a [StarlarkAny] underneath. Returns null if
        /// the value does not match the expected type.
        inline fun <reified T> get(x: Value): T? {
            val starlarkAny: StarlarkAny<T> = x.downcastRef() ?: return null
            return starlarkAny.inner
        }
    }

    /// AllocValue implementation: allocate this value on a heap.
    fun allocValue(heap: Heap): Value {
        return heap.allocSimple(this)
    }

    override fun toString(): String {
        return inner.toString()
    }
}

/// Allocate any value in the frozen heap.
fun <T> FrozenHeap.allocAny(value: T): FrozenRef<T> {
    return this.allocSimpleTypedStatic(StarlarkAny.new(value))
        .asFrozenRef()
        .map { r -> (r as StarlarkAny<T>).inner }
}
