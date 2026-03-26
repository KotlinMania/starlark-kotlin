// port-lint: source src/values/demand.rs
package io.github.kotlinmania.starlark_kotlin.values.demand

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

import kotlin.reflect.KClass
import io.github.kotlinmania.starlark_kotlin.values.layout.Value

/**
 * Taken by `StarlarkValue.provide` to provide different data depending on the type.
 */
class Demand internal constructor(
    // Kotlin: use KClass for type identification instead of TypeId + raw pointer.
    private val requestedType: KClass<*>,
) {
    // The provided value, if type matched.
    internal var result: Any? = null
    internal var filled: Boolean = false

    // impl Demand

    companion object {
        // fn new<T: AnyLifetime<'v>>(option: &mut Option<T>) -> Demand<'a, 'v>
        // Kotlin: constructors are used directly.
    }

    /**
     * Provide a value of given type.
     *
     * If type matches the type requested from `Value.requestValue`, the value is stored
     * inside the [Demand] and later returned, otherwise the value is discarded.
     */
    fun provideValue(value: Any) {
        if (requestedType.isInstance(value)) {
            result = value
            filled = true
        }
    }

    /** Similar to [provideValue], but does not require implementing `ProvidesStaticType`. */
    internal fun provideRefStatic(value: Any) {
        if (requestedType.isInstance(value)) {
            result = value
            filled = true
        }
    }
}

// pub(crate) fn request_value_impl<'v, T: AnyLifetime<'v>>(value: Value<'v>) -> Option<T>
internal inline fun <reified T : Any> requestValueImpl(value: Value): T? {
    val demand = Demand(T::class)
    value.getRef().provide(demand)
    return if (demand.filled) {
        @Suppress("UNCHECKED_CAST")
        demand.result as? T
    } else {
        null
    }
}

// #[cfg(test)] mod tests
// Tests are in commonTest, not here.
