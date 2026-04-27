// port-lint: source src/values/layout/constTypeId.rs
package io.github.kotlinmania.starlark.values.layout

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

import kotlin.reflect.KClass

/** Type-id wrapper/provider keyed off [KClass]. */
data class ConstTypeId(
    private val klass: KClass<*>,
) {

    override fun toString(): String {
        return "ConstTypeId(type_id=${get()})"
    }

    companion object {
        inline fun <reified T : Any> of(): ConstTypeId = ConstTypeId(T::class)

        fun of(klass: KClass<*>): ConstTypeId = ConstTypeId(klass)
    }

    fun get(): KClass<*> = klass
}
