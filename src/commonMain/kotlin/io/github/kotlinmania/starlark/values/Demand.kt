// port-lint: source src/values/demand.rs
package io.github.kotlinmania.starlark.values.demand

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
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.Heap

/**
 * Taken by [StarlarkValue.provide]
 * to provide different data depending on the type.
 */
class Demand @PublishedApi internal constructor(
    @PublishedApi internal val typeIdOfT: KClass<*>,
    @PublishedApi internal var option: Any? = null,
    @PublishedApi internal var filled: Boolean = false,
) {
    /**
     * Provide a value of given type.
     *
     * If type matches the type requested from [Value.requestValue], the value is stored
     * inside the [Demand] and later returned, otherwise the value is discarded.
     */
    fun provideValue(value: Any) {
        if (this.typeIdOfT.isInstance(value)) {
            this.option = value
            this.filled = true
        }
    }

    /**
     * Similar to [provideValue], but does not require implementing `ProvidesStaticType`.
     */
    internal fun provideRefStatic(value: Any) {
        if (this.typeIdOfT.isInstance(value)) {
            this.option = value
            this.filled = true
        }
    }

    companion object {
        @PublishedApi
        internal inline fun <reified T : Any> new(): Demand {
            return Demand(typeIdOfT = T::class, option = null, filled = false)
        }
    }
}

/**
 * Non-inline helper that invokes provide on a Value.
 * Separated from [requestValueImpl] to avoid PublishedApi visibility issues
 * with internal classes (AValueDyn).
 */
@PublishedApi
internal fun fillDemand(value: Value, demand: Demand) {
    value.getRef().provide(demand)
}

@PublishedApi
internal inline fun <reified T : Any> requestValueImpl(value: Value): T? {
    val demand: Demand = Demand.new<T>()
    fillDemand(value, demand)
    return if (demand.filled) demand.option as? T else null
}
