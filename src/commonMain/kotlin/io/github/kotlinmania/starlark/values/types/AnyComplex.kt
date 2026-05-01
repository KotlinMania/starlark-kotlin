// port-lint: source src/values/types/any_complex.rs
package io.github.kotlinmania.starlark.values.types.anycomplex

/*
 * Copyright 2019 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not import this file except in compliance with the License.
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

/** A type [StarlarkAnyComplex] which can wrap any Rust value into a [Value]. */

import io.github.kotlinmania.starlark.values.ComplexValue
import io.github.kotlinmania.starlark.values.Trace
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.allocComplex
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
/**
 * Allocate arbitrary value on the starlark heap without implementing full [StarlarkValue].
 *
 * This is useful for data not directly visible to starlark code.
 *
 * This type is for "complex" values (with tracing during GC). For no GC version check
 * [StarlarkAny].
 */
class StarlarkAnyComplex<T : Any>(
    /** The value. */
    val value: T,
) : ComplexValue, Trace {
    companion object {
        /** Construct a new `StarlarkAnyComplex` value, which can be allocated on the heap. */
        fun <T : Any> new(value: T): StarlarkAnyComplex<T> {
            return StarlarkAnyComplex(value)
        }

        /** Obtain the value from a `Value`, if it is a `StarlarkAnyComplex<T>`. */
        inline fun <reified T : Any> get(value: Any): T? {
            val complex = value as? StarlarkAnyComplex<*> ?: return null
            return complex.value as? T
        }

        /** Obtain the value from a `Value`, if it is a `StarlarkAnyComplex<T>`. */
        inline fun <reified T : Any> getErr(value: Any): T {
            val complex = value as? StarlarkAnyComplex<*>
                ?: throw IllegalArgumentException("Value is not StarlarkAnyComplex")
            return complex.value as? T
                ?: throw IllegalArgumentException(
                    "StarlarkAnyComplex value is not of expected type ${T::class}"
                )
        }
    }

    override fun toString(): String {
        return "${value::class.simpleName ?: "StarlarkAnyComplex"} { .. }"
    }

    override val TYPE: String get() = "any_complex"

    override fun trace(tracer: Tracer) {
        if (value is Trace) {
            (value as Trace).trace(tracer)
        }
    }

    fun allocValue(heap: Heap): Value {
        return heap.allocComplex(this)
    }
}

// Tests are in commonTest.
