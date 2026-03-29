// port-lint: source src/values/layout/const_type_id.rs
package io.github.kotlinmania.starlark_kotlin.values.layout

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

import kotlin.reflect.KClass

/**
 * `TypeId` wrapper/provider.
 *
 * Kotlin: Uses [KClass] for type identification instead of Rust's `TypeId`.
 */
data class ConstTypeId(
    private val klass: KClass<*>,
) {
    // impl ConstTypeId

    /** Get the underlying [KClass]. */
    fun get(): KClass<*> = klass

    companion object {
        // const fn of<T: ?Sized + 'static>() -> ConstTypeId
        inline fun <reified T : Any> of(): ConstTypeId = ConstTypeId(T::class)

        fun of(klass: KClass<*>): ConstTypeId = ConstTypeId(klass)
    }

    // Debug is handled by data class toString()
    // PartialEq is handled by data class equals()
    // Eq is handled by data class equals()
    // Hash is handled by data class hashCode()
}
